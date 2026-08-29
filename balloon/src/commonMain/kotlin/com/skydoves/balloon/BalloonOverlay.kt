/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.balloon

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max

/**
 * The cut-out drawn over the anchor when [BalloonStyle.isVisibleOverlay] is enabled.
 *
 * Mirrors `com.skydoves.balloon.overlay.BalloonOverlayShape` from the Android-only library.
 * Note that the original takes its round-rect radii in raw pixels; this API takes [Dp], like
 * every other size on [BalloonStyle].
 */
@Immutable
public sealed interface BalloonOverlayShape {

  /** Draws nothing over the anchor — the scrim dims it like everything else. */
  public data object Empty : BalloonOverlayShape

  /** Punches a rectangular hole matching the anchor's bounds. */
  public data object Rect : BalloonOverlayShape

  /** Punches an oval inscribed in the anchor's bounds. */
  public data object Oval : BalloonOverlayShape

  /**
   * Punches a circle centred on the anchor.
   *
   * @param radius the circle's radius. [Dp.Unspecified] (the default) uses half of the
   *   anchor's longer side. The View implementation had no default here: its radius was
   *   mandatory, and a `BalloonOverlayCircle` built without one simply drew nothing.
   */
  public data class Circle(val radius: Dp = Dp.Unspecified) : BalloonOverlayShape

  /**
   * Punches a rounded rectangle matching the anchor's bounds, with the same radius on
   * every corner.
   *
   * @param radiusX horizontal corner radius.
   * @param radiusY vertical corner radius, defaulting to [radiusX].
   */
  public data class RoundRect(val radiusX: Dp, val radiusY: Dp = radiusX) : BalloonOverlayShape

  /**
   * Punches a rounded rectangle with a different radius on each corner, mirroring the
   * four-argument `BalloonOverlayRoundRect` constructor of the View implementation.
   *
   * Corners are named in start-relative terms and resolve against the layout direction, so
   * [topStart] is the top-left corner in a left-to-right layout and the top-right one in a
   * right-to-left layout.
   */
  public data class RoundRectPerCorner(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomEnd: Dp,
    val bottomStart: Dp,
  ) : BalloonOverlayShape
}

/** Duration of `balloon_fade_in` / `balloon_fade_out`, which the overlay window also uses. */
private const val OVERLAY_FADE_DURATION = 200

/**
 * Draws the balloon's overlay: a scrim of [BalloonStyle.overlayColor] filling this [BoxScope],
 * with the anchor cut out of it so the anchor stays lit while everything around it is dimmed.
 *
 * This reproduces `BalloonAnchorOverlayView`, which draws the scrim into an offscreen layer
 * and erases the anchor shape from it with `PorterDuff.Mode.CLEAR`. The Compose equivalent is
 * a [BlendMode.Clear] draw inside a node composited offscreen — without
 * [CompositingStrategy.Offscreen] the clear would punch through everything already on the
 * canvas rather than only through the scrim.
 *
 * ## Why this is not a `Popup`
 *
 * The View implementation puts the scrim in its own `PopupWindow`. A Compose `Popup` cannot do
 * the same job on Android: its `WindowManager.LayoutParams` are `WRAP_CONTENT`, and the
 * composition root constrains the reported size to the measure spec the window manager
 * supplies, which excludes the navigation bar. The scrim would stop short of the bottom of the
 * screen however large its content is. Drawing into the caller's own window has no such limit
 * and behaves identically on every target, which is why overlays are rendered by [BalloonHost].
 *
 * ## How far it reaches
 *
 * The scrim is `matchParentSize()` inside the host's `Box` and is composited offscreen so the
 * cut-out can erase from it, which means it covers **the host's bounds**, not the window. The
 * View implementation always dimmed the whole display, because its scrim had a `MATCH_PARENT`
 * window of its own. To get the same result here, put `BalloonHost` at the root of an
 * edge-to-edge window and give it `Modifier.fillMaxSize()`. Wrapping only a subtree dims only
 * that subtree.
 *
 * @param state the balloon this overlay belongs to.
 * @param anchorBounds the anchor's bounds in window coordinates.
 * @param originInWindow the position of this Box in window coordinates, used to translate
 *   [anchorBounds] into local drawing coordinates.
 * @param visible whether the balloon is (becoming) visible; drives the scrim's fade.
 */
@Composable
internal fun BoxScope.BalloonOverlayScrim(
  state: BalloonState,
  anchorBounds: IntRect,
  originInWindow: IntOffset,
  visible: Boolean,
) {
  val style = state.style
  val layoutDirection = LocalLayoutDirection.current

  // The View version animates the overlay window with `Balloon_Fade_Anim`
  // (`applyBalloonOverlayAnimation`, BalloonOverlayAnimation.FADE), so a plain alpha tween is
  // the faithful reproduction — and unlike `AnimatedVisibility` it adds no layout wrapper.
  val fadeDuration = when (style.overlayAnimation) {
    BalloonOverlayAnimation.NONE -> 0
    BalloonOverlayAnimation.FADE -> OVERLAY_FADE_DURATION
  }
  val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(fadeDuration, easing = LinearEasing),
    label = "balloonOverlayAlpha",
  )
  if (animatedAlpha <= 0f) return

  Box(
    modifier = Modifier
      .matchParentSize()
      .pointerInput(style.dismissWhenOverlayClicked) {
        // The tap is consumed either way: the scrim is modal, exactly like the View version's
        // overlay window, so a touch must not fall through to the content below.
        detectTapGestures {
          state.onOverlayClick?.invoke()
          if (style.dismissWhenOverlayClicked) state.dismiss()
        }
      }
      .graphicsLayer {
        alpha = animatedAlpha
        compositingStrategy = CompositingStrategy.Offscreen
      }
      .drawBehind {
        drawRect(color = style.overlayColor)
        drawAnchorCutout(style, anchorBounds, originInWindow, layoutDirection)
      },
  )
}

/** Erases the anchor's shape from the scrim already drawn into the current layer. */
private fun DrawScope.drawAnchorCutout(
  style: BalloonStyle,
  anchorBounds: IntRect,
  originInWindow: IntOffset,
  layoutDirection: LayoutDirection,
) {
  if (style.overlayShape == BalloonOverlayShape.Empty) return

  val padStart = style.overlayPadding.calculateLeftPadding(layoutDirection).toPx()
  val padEnd = style.overlayPadding.calculateRightPadding(layoutDirection).toPx()
  val padTop = style.overlayPadding.calculateTopPadding().toPx()
  val padBottom = style.overlayPadding.calculateBottomPadding().toPx()
  val rect = Rect(
    left = (anchorBounds.left - originInWindow.x) - padStart,
    top = (anchorBounds.top - originInWindow.y) - padTop,
    right = (anchorBounds.right - originInWindow.x) + padEnd,
    bottom = (anchorBounds.bottom - originInWindow.y) + padBottom,
  )
  if (rect.width <= 0f || rect.height <= 0f) return

  drawOverlayShape(style.overlayShape, rect, Color.Transparent, BlendMode.Clear, layoutDirection)

  // `setOverlayPaddingColor` fills the band the padding opened up. The View implementation
  // strokes a ring there after clearing; painting the padded shape and then clearing the
  // unpadded one gets the same result and stays exact when the four paddings differ.
  val paddingColor = style.overlayPaddingColor
  val hasPadding = padStart > 0f || padTop > 0f || padEnd > 0f || padBottom > 0f
  if (paddingColor.isSpecified && paddingColor.alpha > 0f && hasPadding) {
    val anchorRect = Rect(
      left = (anchorBounds.left - originInWindow.x).toFloat(),
      top = (anchorBounds.top - originInWindow.y).toFloat(),
      right = (anchorBounds.right - originInWindow.x).toFloat(),
      bottom = (anchorBounds.bottom - originInWindow.y).toFloat(),
    )
    drawOverlayShape(style.overlayShape, rect, paddingColor, BlendMode.SrcOver, layoutDirection)
    if (anchorRect.width > 0f && anchorRect.height > 0f) {
      drawOverlayShape(
        style.overlayShape,
        anchorRect,
        Color.Transparent,
        BlendMode.Clear,
        layoutDirection,
        // A `Circle` carrying an explicit radius ignores the rect's size, so clearing it at
        // that same radius would erase exactly what was just painted and leave no band at
        // all. The View implementation strokes the ring at `radius - overlayPadding.top / 2`
        // with `strokeWidth = overlayPadding.top`, i.e. it spans `radius - top` to `radius`;
        // shrinking the inner circle by the top padding reproduces that band.
        radiusInset = padTop,
      )
    }
  }
}

/** Paints [shape] into [rect] with [color] and [blendMode]. */
private fun DrawScope.drawOverlayShape(
  shape: BalloonOverlayShape,
  rect: Rect,
  color: Color,
  blendMode: BlendMode,
  layoutDirection: LayoutDirection,
  radiusInset: Float = 0f,
) {
  when (shape) {
    BalloonOverlayShape.Empty -> Unit

    BalloonOverlayShape.Rect -> drawRect(
      color = color,
      topLeft = rect.topLeft,
      size = rect.size,
      blendMode = blendMode,
    )

    BalloonOverlayShape.Oval -> drawOval(
      color = color,
      topLeft = rect.topLeft,
      size = rect.size,
      blendMode = blendMode,
    )

    is BalloonOverlayShape.Circle -> drawCircle(
      color = color,
      radius = if (shape.radius == Dp.Unspecified) {
        // Derived from the rect, which is already the padded one for the outer draw and the
        // bare anchor for the inner draw, so the band falls out on its own.
        max(rect.width, rect.height) / 2f
      } else {
        (shape.radius.toPx() - radiusInset).coerceAtLeast(0f)
      },
      center = rect.center,
      blendMode = blendMode,
    )

    is BalloonOverlayShape.RoundRect -> drawRoundRect(
      color = color,
      topLeft = rect.topLeft,
      size = rect.size,
      cornerRadius = CornerRadius(shape.radiusX.toPx(), shape.radiusY.toPx()),
      style = Fill,
      blendMode = blendMode,
    )

    is BalloonOverlayShape.RoundRectPerCorner -> {
      val ltr = layoutDirection == LayoutDirection.Ltr
      val outline = RoundedCornerShape(
        topStart = CornerSize(shape.topStart),
        topEnd = CornerSize(shape.topEnd),
        bottomEnd = CornerSize(shape.bottomEnd),
        bottomStart = CornerSize(shape.bottomStart),
      ).createOutline(rect.size, if (ltr) LayoutDirection.Ltr else LayoutDirection.Rtl, this)
      translate(left = rect.left, top = rect.top) {
        drawOutline(
          outline = outline,
          color = color,
          style = Fill,
          blendMode = blendMode,
        )
      }
    }
  }
}
