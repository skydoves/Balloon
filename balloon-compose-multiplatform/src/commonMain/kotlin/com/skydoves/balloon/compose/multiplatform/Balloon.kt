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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Renders the balloon body — background, border, padding and width constraints —
 * around [content], using the shape derived from [style] and the resolved
 * [arrowOrientation] / [arrowPositionRatio].
 *
 * This composable is intentionally pure (no popup, no anchor positioning); it is
 * intended to be hosted inside a `Popup` by the [Balloon] anchor composable.
 */
@Composable
internal fun BalloonContent(
  style: BalloonStyle,
  arrowOrientation: ArrowOrientation,
  arrowPositionRatio: Float,
  content: @Composable () -> Unit,
) {
  // When the arrow is hidden, collapse arrow size to zero so BalloonShape falls back
  // to a plain rounded rect (BalloonShapeBuilder already handles 0-sized arrows).
  val effectiveArrowSize = if (style.isArrowVisible) style.arrowSize else DpSize.Zero

  val shape = remember(style, arrowOrientation, arrowPositionRatio) {
    BalloonShape(
      cornerRadius = style.cornerRadius,
      arrowWidth = effectiveArrowSize.width,
      arrowHeight = effectiveArrowSize.height,
      arrowOrientation = arrowOrientation,
      arrowPositionRatio = arrowPositionRatio,
      strokeThickness = style.borderThickness,
    )
  }

  val borderModifier = if (
    style.borderThickness > 0.dp && style.borderColor.isSpecified
  ) {
    Modifier.border(style.borderThickness, style.borderColor, shape)
  } else {
    Modifier
  }

  val maxWidthModifier = if (style.maxWidth != Dp.Unspecified) {
    Modifier.widthIn(max = style.maxWidth)
  } else {
    Modifier
  }

  // Decide whether the arrow needs a separate paint pass on top of the body fill.
  val needsArrowOverlay = style.isArrowVisible &&
    style.arrowColor.isSpecified &&
    style.arrowColor != style.backgroundColor &&
    style.arrowSize.width > 0.dp &&
    style.arrowSize.height > 0.dp

  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current

  val arrowOverlayModifier = if (needsArrowOverlay) {
    // `drawBehind` paints the arrow on the background fill but BEHIND the children
    // (everything later in the chain), so a differently-colored arrow never covers
    // the balloon content.
    Modifier.drawBehind {
      val side = arrowOrientation.resolve(layoutDirection)
      val arrowPath = buildArrowTrianglePath(
        size = size,
        arrowWidthPx = with(density) { style.arrowSize.width.toPx() },
        arrowHeightPx = with(density) { style.arrowSize.height.toPx() },
        side = side,
        ratioInRect = arrowPositionRatio,
        halfStrokePx = with(density) { (style.borderThickness / 2).toPx() },
      )
      drawPath(arrowPath, color = style.arrowColor)
    }
  } else {
    Modifier
  }

  // [BalloonShape] carves the arrow protrusion (arrowHeight / 2) INTO the box on
  // the arrow side, which would otherwise eat into the content padding there.
  // The original Balloon reserves that protrusion as ADDITIONAL space, so we add
  // a matching absolute padding on the arrow side (applied inside the clip, after
  // style.padding). `absolutePadding` keeps LEFT/RIGHT correct under RTL.
  val arrowSpacingModifier = if (
    style.isArrowVisible &&
    effectiveArrowSize.width > 0.dp &&
    effectiveArrowSize.height > 0.dp
  ) {
    val protrusion = effectiveArrowSize.height / 2
    when (arrowOrientation.resolve(layoutDirection)) {
      ResolvedArrowSide.TOP -> Modifier.absolutePadding(top = protrusion)
      ResolvedArrowSide.BOTTOM -> Modifier.absolutePadding(bottom = protrusion)
      ResolvedArrowSide.LEFT -> Modifier.absolutePadding(left = protrusion)
      ResolvedArrowSide.RIGHT -> Modifier.absolutePadding(right = protrusion)
    }
  } else {
    Modifier
  }

  // Modifier order matters here. For draw modifiers the EARLIER one is the outer
  // node: `background` / `drawBehind` paint themselves first and let the rest of
  // the chain draw on top, whereas `border` draws its content first and strokes
  // last. The resulting bottom-to-top paint order is therefore:
  //   1. Background filled to the shape outline (earliest `background`).
  //   2. Arrow (if any) painted on the fill, BEHIND the children, and BEFORE the
  //      clip so the protrusion outside the body isn't clipped away.
  //   3. Children, clipped to the shape and padded inside it.
  //   4. Border stroke on top of everything — it's the outer `border` node, so it
  //      strokes last and is NOT clipped by the later `clip(shape)`.
  // Keeping `border` ahead of `clip` is what stops `clip(shape)` from shaving off
  // half of the centered border stroke.
  Box(
    modifier = Modifier
      .then(maxWidthModifier)
      .then(borderModifier)
      .background(color = style.backgroundColor, shape = shape)
      .then(arrowOverlayModifier)
      .clip(shape)
      .padding(style.padding)
      .then(arrowSpacingModifier),
  ) {
    content()
  }
}

/**
 * Migration-friendly facade for the Compose Multiplatform balloon library.
 *
 * The nested [Builder] mirrors the fluent setter API of the original Android
 * `com.skydoves.balloon.Balloon.Builder` so existing call-sites can be ported with
 * minimal edits — see `MIGRATION.md`. Setters that depend on Android primitives
 * (`Resources`, `View`, `Drawable`, `Lifecycle`, `Typeface`, etc.) are intentionally
 * absent; users adopting KMP migrate to direct `Color(...)` / `value.dp` calls.
 *
 * Unlike the original, [Builder.build] returns an immutable [BalloonStyle] (the
 * KMP visual config) rather than a stateful `Balloon` instance. Lifecycle and
 * presentation are handled separately by [BalloonState] / the `Balloon(...)`
 * anchor composable.
 */
public object Balloon {

  /**
   * Fluent builder mirroring the original Android `Balloon.Builder` API for
   * migration parity. Each setter returns this builder so calls can be chained
   * (or composed via [rememberBalloonBuilder]'s receiver lambda).
   *
   * Padding is tracked internally as four directional [Dp] values rather than as
   * a [PaddingValues] instance — this lets [setPaddingHorizontal] / [setPaddingVertical]
   * preserve the orthogonal axis without requiring a `LayoutDirection` to read
   * an existing [PaddingValues] back.
   *
   * Note: `setOnBalloonDismissListener` is intentionally NOT on the Builder; it's a
   * property on [BalloonState] because [BalloonStyle] is value-equal data and
   * lambdas break that. Migration: `balloonState.onDismiss = { ... }` instead of
   * `builder.setOnBalloonDismissListener {}`.
   */
  public class Builder {
    private var cornerRadius: Dp = 5.dp
    private var arrowWidth: Dp = 15.dp
    private var arrowHeight: Dp = 12.dp
    private var arrowOrientation: ArrowOrientation? = null
    private var arrowPosition: Float = 0.5f
    private var arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON
    private var isArrowVisible: Boolean = true
    private var backgroundColor: Color = Color(0xFF272727)
    private var arrowColor: Color = Color.Unspecified
    private var borderColor: Color = Color.Unspecified
    private var borderThickness: Dp = 0.dp
    private var paddingStart: Dp = 8.dp
    private var paddingTop: Dp = 8.dp
    private var paddingEnd: Dp = 8.dp
    private var paddingBottom: Dp = 8.dp
    private var maxWidth: Dp = Dp.Unspecified
    private var animation: BalloonAnimation = BalloonAnimation.FADE
    private var animationDurationMillis: Int = 250
    private var dismissOnClickOutside: Boolean = true
    private var dismissOnBackPress: Boolean = true
    private var autoDismissMillis: Long = 0L

    /** Sets the corner radius of the balloon body. */
    public fun setCornerRadius(value: Dp): Builder = apply { cornerRadius = value }

    /** Sets the arrow size as separate width and height. */
    public fun setArrowSize(width: Dp, height: Dp): Builder = apply {
      arrowWidth = width
      arrowHeight = height
    }

    /** Sets a uniform arrow size (width == height). */
    public fun setArrowSize(value: Dp): Builder = apply {
      arrowWidth = value
      arrowHeight = value
    }

    /** Sets the arrow base width along the balloon edge. */
    public fun setArrowWidth(value: Dp): Builder = apply { arrowWidth = value }

    /** Sets the arrow protrusion height. */
    public fun setArrowHeight(value: Dp): Builder = apply { arrowHeight = value }

    /**
     * Pins the arrow to a specific [ArrowOrientation]. When unset (the default),
     * the orientation is auto-derived from the [BalloonAlign] passed to `show(...)`.
     *
     * Note: when the balloon is shown with [BalloonAlign.CENTER] the arrow has no
     * meaningful edge to point at — call [setIsVisibleArrow] with `false` (or set
     * [BalloonStyle.isArrowVisible] to `false`) to hide it for a clean overlay.
     */
    public fun setArrowOrientation(value: ArrowOrientation): Builder = apply {
      arrowOrientation = value
    }

    /** Sets the arrow position along its edge as a fraction `0.0..1.0`. */
    public fun setArrowPosition(value: Float): Builder = apply {
      arrowPosition = value.coerceIn(0f, 1f)
    }

    /** Sets the rule used to interpret [setArrowPosition]. */
    public fun setArrowPositionRules(value: ArrowPositionRules): Builder = apply {
      arrowPositionRules = value
    }

    /** Whether the arrow notch is rendered. When false, the balloon is a plain rounded rectangle. */
    public fun setIsVisibleArrow(value: Boolean): Builder = apply { isArrowVisible = value }

    /** Sets the balloon body fill color. */
    public fun setBackgroundColor(value: Color): Builder = apply { backgroundColor = value }

    /**
     * Sets the arrow fill color. Pass [Color.Unspecified] to inherit
     * [setBackgroundColor] (the default).
     */
    public fun setArrowColor(value: Color): Builder = apply { arrowColor = value }

    /** Sets the balloon border color and thickness. */
    public fun setBorder(color: Color, thickness: Dp): Builder = apply {
      borderColor = color
      borderThickness = thickness
    }

    /** Alias for [setBorder], matching the original `setBalloonStroke` Android API. */
    public fun setBalloonStroke(color: Color, thickness: Dp): Builder = setBorder(color, thickness)

    /** Sets uniform padding on all four sides of the balloon's content. */
    public fun setPadding(value: Dp): Builder = apply {
      paddingStart = value
      paddingTop = value
      paddingEnd = value
      paddingBottom = value
    }

    /** Sets directional padding around the balloon's content. */
    public fun setPadding(start: Dp, top: Dp, end: Dp, bottom: Dp): Builder = apply {
      paddingStart = start
      paddingTop = top
      paddingEnd = end
      paddingBottom = bottom
    }

    /** Sets the horizontal (start + end) padding, leaving vertical padding untouched. */
    public fun setPaddingHorizontal(value: Dp): Builder = apply {
      paddingStart = value
      paddingEnd = value
    }

    /** Sets the vertical (top + bottom) padding, leaving horizontal padding untouched. */
    public fun setPaddingVertical(value: Dp): Builder = apply {
      paddingTop = value
      paddingBottom = value
    }

    /**
     * Sets the maximum width constraint of the balloon body.
     * Pass [Dp.Unspecified] to remove the constraint.
     */
    public fun setMaxWidth(value: Dp): Builder = apply { maxWidth = value }

    /** Sets the enter / exit transition family. */
    public fun setBalloonAnimation(value: BalloonAnimation): Builder = apply { animation = value }

    /** Sets the duration of the [BalloonAnimation] in milliseconds. */
    public fun setAnimationDurationMillis(value: Int): Builder = apply {
      animationDurationMillis = value
    }

    /** Whether tapping outside the balloon should dismiss it. */
    public fun setDismissWhenTouchOutside(value: Boolean): Builder = apply {
      dismissOnClickOutside = value
    }

    /** Whether the back button / Escape key should dismiss the balloon. */
    public fun setDismissWhenBackPressed(value: Boolean): Builder = apply {
      dismissOnBackPress = value
    }

    /**
     * Auto-dismiss the balloon [millis] milliseconds after it becomes visible.
     * Pass `0L` to disable. Mirrors `Balloon.Builder.setAutoDismissDuration` in the
     * original Android API.
     */
    public fun setAutoDismissDuration(millis: Long): Builder = apply {
      autoDismissMillis = millis.coerceAtLeast(0L)
    }

    /** Builds the immutable [BalloonStyle] from the current builder state. */
    public fun build(): BalloonStyle = BalloonStyle(
      cornerRadius = cornerRadius,
      arrowSize = DpSize(arrowWidth, arrowHeight),
      arrowOrientation = arrowOrientation,
      arrowPosition = arrowPosition,
      arrowPositionRules = arrowPositionRules,
      isArrowVisible = isArrowVisible,
      backgroundColor = backgroundColor,
      arrowColor = arrowColor,
      borderColor = borderColor,
      borderThickness = borderThickness,
      padding = PaddingValues(
        start = paddingStart,
        top = paddingTop,
        end = paddingEnd,
        bottom = paddingBottom,
      ),
      maxWidth = maxWidth,
      animation = animation,
      animationDurationMillis = animationDurationMillis,
      dismissOnClickOutside = dismissOnClickOutside,
      dismissOnBackPress = dismissOnBackPress,
      autoDismissMillis = autoDismissMillis,
    )
  }
}

/** DSL marker for the fluent [Balloon.Builder] receiver lambda. */
@DslMarker
public annotation class BalloonDsl

/**
 * Create and remember a [BalloonStyle] using the fluent [Balloon.Builder] DSL.
 *
 * Mirrors the original `rememberBalloonBuilder { ... }` API in `balloon-compose`
 * so existing builder blocks can be migrated with minimal edits — see
 * `MIGRATION.md`.
 *
 * @param key recomposition key. When it changes, the [Balloon.Builder] block is
 *   re-evaluated and a new [BalloonStyle] is produced.
 * @param block fluent receiver lambda invoked on a fresh [Balloon.Builder].
 */
@Composable
@BalloonDsl
public fun rememberBalloonBuilder(
  key: Any? = null,
  block: Balloon.Builder.() -> Unit,
): BalloonStyle = remember(key) { Balloon.Builder().apply(block).build() }
