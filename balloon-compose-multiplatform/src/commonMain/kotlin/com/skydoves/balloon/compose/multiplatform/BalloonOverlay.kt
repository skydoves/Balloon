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

package com.skydoves.balloon.compose.multiplatform

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
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
   *   anchor's longer side, matching the View implementation's fallback.
   */
  public data class Circle(val radius: Dp = Dp.Unspecified) : BalloonOverlayShape

  /**
   * Punches a rounded rectangle matching the anchor's bounds.
   *
   * @param radiusX horizontal corner radius.
   * @param radiusY vertical corner radius, defaulting to [radiusX].
   */
  public data class RoundRect(val radiusX: Dp, val radiusY: Dp = radiusX) : BalloonOverlayShape
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
 * supplies — which excludes the navigation bar. The scrim would stop short of the bottom of
 * the screen however large its content is. Drawing into the caller's own (edge-to-edge) window
 * has no such limit, needs no coordinate translation, and behaves identically on every target.
 * This is why overlays are rendered by [BalloonHost].
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

  // The View version animates the overlay window with `Balloon_Fade_Anim`
  // (`applyBalloonOverlayAnimation`, BalloonOverlayAnimation.FADE), so a plain alpha tween is
  // the faithful reproduction — and unlike `AnimatedVisibility` it adds no layout wrapper.
  val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(OVERLAY_FADE_DURATION, easing = LinearEasing),
    label = "balloonOverlayAlpha",
  )
  if (animatedAlpha <= 0f) return

  Box(
    modifier = Modifier
      .matchParentSize()
      .pointerInput(style.dismissWhenOverlayClicked) {
        // The tap is consumed either way: the scrim is modal, exactly like the View version's
        // overlay window, so a touch must not fall through to the content below.
        detectTapGestures { if (style.dismissWhenOverlayClicked) state.dismiss() }
      }
      .graphicsLayer {
        alpha = animatedAlpha
        compositingStrategy = CompositingStrategy.Offscreen
      }
      .drawBehind {
        drawRect(color = style.overlayColor)
        drawAnchorCutout(style, anchorBounds, originInWindow)
      },
  )
}

/** Erases the anchor's shape from the scrim already drawn into the current layer. */
private fun DrawScope.drawAnchorCutout(
  style: BalloonStyle,
  anchorBounds: IntRect,
  originInWindow: IntOffset,
) {
  if (style.overlayShape == BalloonOverlayShape.Empty) return

  val pad = style.overlayPadding.toPx()
  val rect = Rect(
    left = (anchorBounds.left - originInWindow.x) - pad,
    top = (anchorBounds.top - originInWindow.y) - pad,
    right = (anchorBounds.right - originInWindow.x) + pad,
    bottom = (anchorBounds.bottom - originInWindow.y) + pad,
  )
  if (rect.width <= 0f || rect.height <= 0f) return

  when (val shape = style.overlayShape) {
    BalloonOverlayShape.Empty -> Unit

    BalloonOverlayShape.Rect -> drawRect(
      color = Color.Transparent,
      topLeft = rect.topLeft,
      size = rect.size,
      blendMode = BlendMode.Clear,
    )

    BalloonOverlayShape.Oval -> drawOval(
      color = Color.Transparent,
      topLeft = rect.topLeft,
      size = rect.size,
      blendMode = BlendMode.Clear,
    )

    is BalloonOverlayShape.Circle -> drawCircle(
      color = Color.Transparent,
      radius = if (shape.radius == Dp.Unspecified) {
        max(rect.width, rect.height) / 2f
      } else {
        shape.radius.toPx()
      },
      center = rect.center,
      blendMode = BlendMode.Clear,
    )

    is BalloonOverlayShape.RoundRect -> drawRoundRect(
      color = Color.Transparent,
      topLeft = rect.topLeft,
      size = rect.size,
      cornerRadius = CornerRadius(shape.radiusX.toPx(), shape.radiusY.toPx()),
      style = Fill,
      blendMode = BlendMode.Clear,
    )
  }
}
