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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Renders the balloon body — background, border, padding, sizing, margin, overlay-less
 * decorations and animations — around [content], using the shape derived from [style] and
 * the resolved [arrowOrientation] / [arrowCenterFromCardStart].
 *
 * ## Box model
 *
 * The composable reproduces the View implementation's popup box so that a ported call-site
 * lands on the same pixels. In the original (`balloon_layout_body.xml` plus
 * `Balloon.initializeBalloonRoot` / `initializeBalloonContent`) the popup is
 *
 * ```
 * popup ─ margin ─ reserve ─ card(padding ─ content)
 * ```
 *
 * and `initializeBalloonContent` sets that reserve to
 *
 * ```
 * arrow axis: effectiveArrowHeight - 1px on BOTH sides
 * cross axis: elevation on both sides
 * ```
 *
 * Both are reproduced here, because both are load-bearing:
 *
 * - the reserve on the arrow side is the visible protrusion, and is carved into the shape;
 * - the reserve on the far side and the cross-axis `elevation` are what make a wrap-width
 *   balloon stop short of the window edges, and are what every width spec — `setWidth`,
 *   `setWidthRatio`, `setMaxWidth`, … — is measured against, exactly as in the original
 *   where those specs size the *popup*, not the card.
 *
 * The one deliberate departure: when the arrow is hidden the arrow-axis reserve collapses,
 * so the body sits flush against its anchor. The View implementation reserves it regardless
 * of `isVisibleArrow`, leaving an unexplained gap the size of an arrow nobody can see.
 *
 * This composable is intentionally pure (no popup, no anchor positioning); it is hosted
 * inside a `Popup` by [BalloonPopupLayer].
 *
 * @param style the resolved visual configuration.
 * @param arrowOrientation the final (possibly flipped) arrow edge.
 * @param arrowCenterFromCardStart the arrow center, in pixels from the card's leading edge
 *   along that side, as resolved by [BalloonPopupPositionProvider]. `null` centers the arrow.
 * @param onClick invoked when the body is tapped.
 * @param content the balloon body.
 */
@Composable
internal fun BalloonContent(
  style: BalloonStyle,
  arrowOrientation: ArrowOrientation,
  arrowCenterFromCardStart: Float?,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current
  val side = arrowOrientation.resolve(layoutDirection)
  val arrowSize = style.effectiveArrowSize
  val hasArrow = arrowSize != DpSize.Zero

  val shape = remember(style, arrowOrientation, arrowCenterFromCardStart) {
    BalloonShape(
      cornerRadius = style.cornerRadius,
      arrowWidth = arrowSize.width,
      arrowHeight = arrowSize.height,
      arrowOrientation = arrowOrientation,
      arrowCenterFromRectStart = arrowCenterFromCardStart,
    )
  }

  // ---- The popup box around the card. See the KDoc: `protrusion` is carved out of the
  // shape on the arrow side, the other three sides are padding on the outer Box.
  val reserve = remember(style, side, density) { style.reserve(side, density) }

  val borderModifier = if (
    style.borderThickness > 0.dp && style.borderColor.isSpecified
  ) {
    Modifier.border(style.borderThickness, style.borderColor, shape)
  } else {
    Modifier
  }

  // Decide whether the arrow needs a separate paint pass on top of the body fill.
  val needsArrowOverlay = hasArrow &&
    style.arrowColor.isSpecified &&
    style.arrowColor != style.backgroundColor

  val arrowOverlayModifier = if (needsArrowOverlay) {
    // `drawBehind` paints the arrow on the background fill but BEHIND the children
    // (everything later in the chain), so a differently-colored arrow never covers
    // the balloon content.
    Modifier.drawBehind {
      val arrowPath = buildArrowTrianglePath(
        size = size,
        cornerRadiusPx = with(density) { style.cornerRadius.toPx() },
        arrowWidthPx = with(density) { arrowSize.width.toPx() },
        arrowHeightPx = with(density) { arrowSize.height.toPx() },
        side = side,
        arrowCenterFromRectStart = arrowCenterFromCardStart,
      )
      drawPath(arrowPath, color = style.arrowColor)
    }
  } else {
    Modifier
  }

  // [BalloonShape] carves the arrow protrusion INTO the box on the arrow side, which would
  // otherwise eat into the content padding there. The original reserves that protrusion as
  // ADDITIONAL space, so we add a matching absolute padding on the arrow side (applied
  // inside the clip, after style.padding). `absolutePadding` keeps LEFT/RIGHT correct
  // under RTL.
  val arrowSpacingModifier = if (hasArrow) {
    when (side) {
      ResolvedArrowSide.TOP -> Modifier.absolutePadding(top = reserve.protrusion)
      ResolvedArrowSide.BOTTOM -> Modifier.absolutePadding(bottom = reserve.protrusion)
      ResolvedArrowSide.LEFT -> Modifier.absolutePadding(left = reserve.protrusion)
      ResolvedArrowSide.RIGHT -> Modifier.absolutePadding(right = reserve.protrusion)
    }
  } else {
    Modifier
  }

  // The far-side / cross-axis reserve, as padding on the outer Box.
  val outerReserveModifier = when (side) {
    ResolvedArrowSide.TOP -> Modifier.absolutePadding(
      left = reserve.cross,
      right = reserve.cross,
      bottom = reserve.farSide,
    )
    ResolvedArrowSide.BOTTOM -> Modifier.absolutePadding(
      left = reserve.cross,
      right = reserve.cross,
      top = reserve.farSide,
    )
    ResolvedArrowSide.LEFT -> Modifier.absolutePadding(
      top = reserve.cross,
      bottom = reserve.cross,
      right = reserve.farSide,
    )
    ResolvedArrowSide.RIGHT -> Modifier.absolutePadding(
      top = reserve.cross,
      bottom = reserve.cross,
      left = reserve.farSide,
    )
  }

  // ---- Sizing. Every width spec describes the POPUP box, exactly like
  // `Balloon.getMeasuredWidth()` / `getWidthMeasureSpec()`, which set `bodyWindow.width`.
  // The inner Box is therefore the spec minus the margins and the outer reserve.
  val windowWidth = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
  val marginHorizontal = style.margin.calculateLeftPadding(layoutDirection) +
    style.margin.calculateRightPadding(layoutDirection)
  val marginVertical = style.margin.calculateTopPadding() +
    style.margin.calculateBottomPadding()

  fun popupToInnerWidth(popupWidth: Dp): Dp =
    (popupWidth - marginHorizontal - reserve.horizontalOuter).coerceAtLeast(0.dp)

  val sizeModifier = when {
    // setWidthRatio: an EXACT popup width, measured against the whole window.
    style.widthRatio > 0f ->
      Modifier.requiredWidth(popupToInnerWidth(windowWidth * style.widthRatio))

    // setMinWidthRatio / setMaxWidthRatio: a wrap bounded by fractions of the window. The
    // original checks these BEFORE setWidth, and defaults an unset max ratio to 1f.
    style.minWidthRatio > 0f || style.maxWidthRatio > 0f -> {
      val maxRatio = if (style.maxWidthRatio > 0f) style.maxWidthRatio else 1f
      Modifier.widthIn(
        min = popupToInnerWidth(windowWidth * style.minWidthRatio),
        max = popupToInnerWidth(windowWidth * maxRatio),
      )
    }

    // setWidth: an EXACT popup width, capped at the window.
    style.width != Dp.Unspecified ->
      Modifier.requiredWidth(popupToInnerWidth(style.width.coerceAtMost(windowWidth)))

    // Otherwise wrap, bounded by the min/max specs and the window.
    else -> {
      val maxPopup = minOfDp(style.maxWidth, windowWidth)
      val minPopup = (
        if (style.minWidth == Dp.Unspecified) 0.dp else style.minWidth
        ).coerceAtMost(maxPopup)
      Modifier.widthIn(
        min = popupToInnerWidth(minPopup),
        max = popupToInnerWidth(maxPopup),
      )
    }
  }

  // setHeight likewise sizes the popup; the original applies no window clamp to it.
  val heightModifier = if (style.height != Dp.Unspecified) {
    Modifier.height(
      (style.height - marginVertical - reserve.verticalOuter).coerceAtLeast(0.dp),
    )
  } else {
    Modifier
  }

  val alphaModifier =
    if (style.alpha != 1f) Modifier.graphicsLayer { alpha = style.alpha } else Modifier

  // `pointerInput` would otherwise hold on to the lambda it was first given; reading
  // through `rememberUpdatedState` keeps the tap wired to the current listener and the
  // current `dismissWhenClicked` without restarting the gesture detector.
  val currentOnClick by rememberUpdatedState(onClick)
  val clickModifier = Modifier.pointerInput(Unit) { detectTapGestures { currentOnClick() } }

  Box(
    modifier = Modifier
      // The margin is outside everything: it keeps the balloon off the window edges and is
      // part of the popup box, exactly like the `balloonWrapper` margins in the original.
      .padding(style.margin)
      .then(outerReserveModifier)
      .then(alphaModifier)
      .balloonHighlight(
        animation = style.highlightAnimation,
        arrowSide = side,
        isArrowVisible = style.isArrowVisible,
        startDelayMillis = style.highlightAnimationStartDelayMillis,
        rotate = style.rotateAnimation,
      )
      .balloonCircularReveal(
        enabled = style.animation == BalloonAnimation.CIRCULAR,
        durationMillis = style.circularDurationMillis,
      ),
  ) {
    // Modifier order matters here. For draw modifiers the EARLIER one is the outer
    // node: `background` / `drawBehind` paint themselves first and let the rest of
    // the chain draw on top, whereas `border` draws its content first and strokes
    // last. The resulting bottom-to-top paint order is therefore:
    //   1. Background filled to the shape outline (earliest `background`).
    //   2. Arrow (if any) painted on the fill, BEHIND the children, and BEFORE the
    //      clip so the protrusion outside the body isn't clipped away.
    //   3. Children, clipped to the shape and padded inside it.
    //   4. Border stroke on top of everything — it's the outer `border` node, so it
    //      strokes last. On an Outline.Generic, `border` draws an INNER-aligned stroke
    //      (Stroke(width*2) then clears the outer half), so it sits flush against the
    //      shape edge, fully inside the layout bounds — no inset needed and no gap.
    Box(
      modifier = Modifier
        .then(sizeModifier)
        .then(heightModifier)
        .then(borderModifier)
        .background(color = style.backgroundColor, shape = shape)
        .then(arrowOverlayModifier)
        .clip(shape)
        .then(clickModifier)
        .padding(style.padding)
        .then(arrowSpacingModifier),
    ) {
      content()
    }
  }
}

/**
 * The space the popup box holds around the visible card, split the way
 * `Balloon.initializeBalloonContent` splits it.
 *
 * @property protrusion how far the arrow tip sticks out past the card, on the arrow side.
 *   Carved into [BalloonShape] rather than added as padding, so the notch and the body are
 *   one path.
 * @property farSide the reserve on the side OPPOSITE the arrow.
 * @property cross the reserve on each of the two sides orthogonal to the arrow.
 */
internal data class BalloonReserve(
  val protrusion: Dp,
  val farSide: Dp,
  val cross: Dp,
  val horizontalOuter: Dp,
  val verticalOuter: Dp,
)

/** Computes the [BalloonReserve] for [side] at the current [density]. */
internal fun BalloonStyle.reserve(side: ResolvedArrowSide, density: Density): BalloonReserve {
  val arrowSize = effectiveArrowSize
  val protrusion = if (arrowSize == DpSize.Zero) {
    0.dp
  } else {
    with(density) { arrowProtrusionPx(arrowSize.height.toPx()).toDp() }
  }
  val elevationInset = elevation.coerceAtLeast(0.dp)
  // `initializeBalloonContent` uses `paddingSize.coerceAtLeast(elevation)` on the far side,
  // which also keeps a hidden arrow's box symmetric at exactly the elevation inset.
  val farSide = maxOf(protrusion, elevationInset)
  val horizontal = side == ResolvedArrowSide.LEFT || side == ResolvedArrowSide.RIGHT
  return BalloonReserve(
    protrusion = protrusion,
    farSide = farSide,
    cross = elevationInset,
    horizontalOuter = if (horizontal) farSide else elevationInset * 2,
    verticalOuter = if (horizontal) elevationInset * 2 else farSide,
  )
}

/** Smallest of the given values; [Dp.Unspecified] entries are ignored. */
private fun minOfDp(a: Dp, b: Dp): Dp =
  listOf(a, b).filter { it != Dp.Unspecified }.minOrNull() ?: Dp.Infinity

/**
 * Reproduces `ViewAnimationUtils.createCircularReveal`, which the View implementation runs
 * on the balloon's content view for [BalloonAnimation.CIRCULAR]: a circle centred on the
 * body grows from radius `0` to `max(width, height)` over [durationMillis], using
 * `ValueAnimator`'s default accelerate/decelerate curve.
 *
 * Compose has no circular-reveal primitive, so this clips the content to that growing
 * circle, which is what the platform API does under the hood.
 */
@Composable
private fun Modifier.balloonCircularReveal(
  enabled: Boolean,
  durationMillis: Long,
): Modifier {
  if (!enabled) return this
  val progress = remember { Animatable(0f) }
  LaunchedEffect(durationMillis) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis.toInt(), easing = AccelerateDecelerateEasing),
    )
  }
  return this.drawWithContent {
    val radius = max(size.width, size.height) * progress.value
    if (radius <= 0f) return@drawWithContent
    val path = Path().apply {
      addOval(
        Rect(
          offset = Offset(size.width / 2f - radius, size.height / 2f - radius),
          size = Size(radius * 2f, radius * 2f),
        ),
      )
    }
    clipPath(path) { this@drawWithContent.drawContent() }
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
    private var arrowWidth: Dp = 12.dp
    private var arrowHeight: Dp = 12.dp
    private var arrowOrientation: ArrowOrientation? = null
    private var arrowPosition: Float = 0.5f
    private var arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON
    private var arrowOrientationRules: ArrowOrientationRules = ArrowOrientationRules.ALIGN_ANCHOR
    private var arrowAlignAnchorPadding: Dp = 0.dp
    private var arrowAlignAnchorPaddingRatio: Float = 2.5f
    private var isArrowVisible: Boolean = true
    private var backgroundColor: Color = Color.Black
    private var arrowColor: Color = Color.Unspecified
    private var borderColor: Color = Color.Unspecified
    private var borderThickness: Dp = 0.dp
    private var paddingStart: Dp = 0.dp
    private var paddingTop: Dp = 0.dp
    private var paddingEnd: Dp = 0.dp
    private var paddingBottom: Dp = 0.dp
    private var marginStart: Dp = 0.dp
    private var marginTop: Dp = 0.dp
    private var marginEnd: Dp = 0.dp
    private var marginBottom: Dp = 0.dp
    private var elevation: Dp = 2.dp
    private var width: Dp = Dp.Unspecified
    private var widthRatio: Float = 0f
    private var minWidthRatio: Float = 0f
    private var maxWidthRatio: Float = 0f
    private var minWidth: Dp = Dp.Unspecified
    private var maxWidth: Dp = Dp.Unspecified
    private var height: Dp = Dp.Unspecified
    private var animation: BalloonAnimation = BalloonAnimation.FADE
    private var circularDurationMillis: Long = 500L
    private var highlightAnimation: BalloonHighlightAnimation = BalloonHighlightAnimation.NONE
    private var highlightAnimationStartDelayMillis: Long = 0L
    private var rotateAnimation: BalloonRotateAnimation = BalloonRotateAnimation()
    private var alpha: Float = 1f
    private var isVisibleOverlay: Boolean = false
    private var overlayColor: Color = Color.Transparent
    private var overlayPaddingStart: Dp = 0.dp
    private var overlayPaddingTop: Dp = 0.dp
    private var overlayPaddingEnd: Dp = 0.dp
    private var overlayPaddingBottom: Dp = 0.dp
    private var overlayShape: BalloonOverlayShape = BalloonOverlayShape.Oval
    private var overlayAnimation: BalloonOverlayAnimation = BalloonOverlayAnimation.FADE
    private var dismissWhenOverlayClicked: Boolean = true
    private var dismissWhenClicked: Boolean = false
    private var dismissWhenShowAgain: Boolean = false
    private var focusable: Boolean = true
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

    /**
     * Sets whether the arrow edge may follow the balloon when a lack of room flips it to the
     * opposite side. Only has an effect together with [setArrowOrientation].
     */
    public fun setArrowOrientationRules(value: ArrowOrientationRules): Builder = apply {
      arrowOrientationRules = value
    }

    /**
     * Sets the extra clearance kept between the arrow and the balloon's corners under
     * [ArrowPositionRules.ALIGN_ANCHOR].
     */
    public fun setArrowAlignAnchorPadding(value: Dp): Builder = apply {
      arrowAlignAnchorPadding = value
    }

    /**
     * Sets the multiplier on the arrow's own size in the `ALIGN_ANCHOR` clamp: the arrow
     * never comes closer to a corner than `arrowSize * value + arrowAlignAnchorPadding`.
     */
    public fun setArrowAlignAnchorPaddingRatio(value: Float): Builder = apply {
      arrowAlignAnchorPaddingRatio = value
    }

    /**
     * Whether the arrow notch is rendered. When false the balloon is a plain rounded
     * rectangle AND the space the arrow would have occupied is released, so the body sits
     * flush against its anchor.
     */
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

    /** Sets the start padding. */
    public fun setPaddingStart(value: Dp): Builder = apply { paddingStart = value }

    /** Sets the top padding. */
    public fun setPaddingTop(value: Dp): Builder = apply { paddingTop = value }

    /** Sets the end padding. */
    public fun setPaddingEnd(value: Dp): Builder = apply { paddingEnd = value }

    /** Sets the bottom padding. */
    public fun setPaddingBottom(value: Dp): Builder = apply { paddingBottom = value }

    /** Sets a uniform margin on all four sides, keeping the balloon off the window edges. */
    public fun setMargin(value: Dp): Builder = apply {
      marginStart = value
      marginTop = value
      marginEnd = value
      marginBottom = value
    }

    /** Sets directional margins around the balloon. */
    public fun setMargin(start: Dp, top: Dp, end: Dp, bottom: Dp): Builder = apply {
      marginStart = start
      marginTop = top
      marginEnd = end
      marginBottom = bottom
    }

    /** Sets the horizontal (start + end) margin, leaving vertical margins untouched. */
    public fun setMarginHorizontal(value: Dp): Builder = apply {
      marginStart = value
      marginEnd = value
    }

    /** Sets the vertical (top + bottom) margin, leaving horizontal margins untouched. */
    public fun setMarginVertical(value: Dp): Builder = apply {
      marginTop = value
      marginBottom = value
    }

    /** Sets the start margin. */
    public fun setMarginStart(value: Dp): Builder = apply { marginStart = value }

    /** Sets the top margin. */
    public fun setMarginTop(value: Dp): Builder = apply { marginTop = value }

    /** Sets the end margin. */
    public fun setMarginEnd(value: Dp): Builder = apply { marginEnd = value }

    /** Sets the bottom margin. */
    public fun setMarginBottom(value: Dp): Builder = apply { marginBottom = value }

    /**
     * Sets a fixed body width. Pass [Dp.Unspecified] to wrap the content instead
     * (the equivalent of `BalloonSizeSpec.WRAP`).
     */
    public fun setWidth(value: Dp): Builder = apply { width = value }

    /**
     * Sets the body width as a fraction of the window width. Takes precedence over
     * [setWidth] and [setMaxWidth]. Pass `0f` to disable.
     */
    public fun setWidthRatio(value: Float): Builder = apply { widthRatio = value }

    /** Sets a lower bound on the body width as a fraction of the window width. */
    public fun setMinWidthRatio(value: Float): Builder = apply { minWidthRatio = value }

    /** Sets an upper bound on the body width as a fraction of the window width. */
    public fun setMaxWidthRatio(value: Float): Builder = apply { maxWidthRatio = value }

    /** Sets a lower bound on the body width. */
    public fun setMinWidth(value: Dp): Builder = apply { minWidth = value }

    /**
     * Sets the maximum width constraint of the balloon body.
     * Pass [Dp.Unspecified] to remove the constraint.
     */
    public fun setMaxWidth(value: Dp): Builder = apply { maxWidth = value }

    /**
     * Sets a fixed body height. Pass [Dp.Unspecified] to wrap the content instead
     * (the equivalent of `BalloonSizeSpec.WRAP`).
     */
    public fun setHeight(value: Dp): Builder = apply { height = value }

    /** Sets a fixed body width and height at once. */
    public fun setSize(width: Dp, height: Dp): Builder = apply {
      this.width = width
      this.height = height
    }

    /**
     * Sets the inset reserved on the axis orthogonal to the arrow, mirroring
     * `Balloon.Builder.setElevation` (default `2.dp`). It is part of the popup box the width
     * specs measure against; pass `0.dp` for a body that fills its width spec exactly.
     */
    public fun setElevation(value: Dp): Builder = apply { elevation = value }

    /**
     * Sets the enter / exit transition family.
     *
     * Mirrors the original's side effect: [BalloonAnimation.CIRCULAR] also turns
     * [setFocusable] off, because a focusable popup would swallow the touches the reveal is
     * meant to play under.
     */
    public fun setBalloonAnimation(value: BalloonAnimation): Builder = apply {
      animation = value
      if (value == BalloonAnimation.CIRCULAR) focusable = false
    }

    /** Sets the duration of the [BalloonAnimation.CIRCULAR] reveal, in milliseconds. */
    public fun setCircularDuration(value: Long): Builder = apply {
      circularDurationMillis = value.coerceAtLeast(0L)
    }

    /** Sets the looping animation played while the balloon is showing. */
    public fun setBalloonHighlightAnimation(
      value: BalloonHighlightAnimation,
      startDelayMillis: Long = 0L,
    ): Builder = apply {
      highlightAnimation = value
      highlightAnimationStartDelayMillis = startDelayMillis.coerceAtLeast(0L)
    }

    /** Sets the parameters of [BalloonHighlightAnimation.ROTATE]. */
    public fun setBalloonRotationAnimation(value: BalloonRotateAnimation): Builder = apply {
      rotateAnimation = value
    }

    /** Sets the opacity of the whole balloon body, `0f..1f`. */
    public fun setAlpha(value: Float): Builder = apply { alpha = value.coerceIn(0f, 1f) }

    /** Whether a dimming scrim with a cut-out around the anchor is drawn behind the balloon. */
    public fun setIsVisibleOverlay(value: Boolean): Builder = apply { isVisibleOverlay = value }

    /** Sets the color of the overlay scrim. */
    public fun setOverlayColor(value: Color): Builder = apply { overlayColor = value }

    /** Sets extra space added around the anchor before the cut-out shape is drawn. */
    public fun setOverlayPadding(value: Dp): Builder = apply {
      overlayPaddingStart = value
      overlayPaddingTop = value
      overlayPaddingEnd = value
      overlayPaddingBottom = value
    }

    /** Sets directional space added around the anchor before the cut-out shape is drawn. */
    public fun setOverlayPadding(start: Dp, top: Dp, end: Dp, bottom: Dp): Builder = apply {
      overlayPaddingStart = start
      overlayPaddingTop = top
      overlayPaddingEnd = end
      overlayPaddingBottom = bottom
    }

    /** Sets how the overlay scrim appears and disappears. */
    public fun setBalloonOverlayAnimation(value: BalloonOverlayAnimation): Builder = apply {
      overlayAnimation = value
    }

    /** Sets the shape of the anchor cut-out in the overlay scrim. */
    public fun setOverlayShape(value: BalloonOverlayShape): Builder = apply {
      overlayShape = value
    }

    /** Whether tapping the overlay scrim should dismiss the balloon. */
    public fun setDismissWhenOverlayClicked(value: Boolean): Builder = apply {
      dismissWhenOverlayClicked = value
    }

    /** Whether tapping the balloon body itself should dismiss it. */
    public fun setDismissWhenClicked(value: Boolean): Builder = apply {
      dismissWhenClicked = value
    }

    /**
     * Whether showing an already-visible balloon dismisses it instead of re-showing it in
     * place. Mirrors `setDismissWhenShowAgain`.
     */
    public fun setDismissWhenShowAgain(value: Boolean): Builder = apply {
      dismissWhenShowAgain = value
    }

    /**
     * Whether the balloon's window takes input focus, mirroring the original `setFocusable`
     * (which also defaults to `true`).
     *
     * On Android this is required for the back press to reach the balloon, but it also makes
     * the popup touch-modal and steals IME focus while the balloon is showing. Pass `false`
     * for a balloon that lets touches through to the content underneath.
     */
    public fun setFocusable(value: Boolean): Builder = apply { focusable = value }

    /**
     * Whether tapping outside the balloon should dismiss it.
     *
     * Mirrors the original's side effect: turning this off also turns [setFocusable] off, so
     * a balloon that ignores outside taps does not sit there swallowing them either.
     */
    public fun setDismissWhenTouchOutside(value: Boolean): Builder = apply {
      dismissOnClickOutside = value
      if (!value) focusable = false
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
      arrowOrientationRules = arrowOrientationRules,
      arrowAlignAnchorPadding = arrowAlignAnchorPadding,
      arrowAlignAnchorPaddingRatio = arrowAlignAnchorPaddingRatio,
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
      margin = PaddingValues(
        start = marginStart,
        top = marginTop,
        end = marginEnd,
        bottom = marginBottom,
      ),
      elevation = elevation,
      width = width,
      widthRatio = widthRatio,
      minWidthRatio = minWidthRatio,
      maxWidthRatio = maxWidthRatio,
      minWidth = minWidth,
      maxWidth = maxWidth,
      height = height,
      animation = animation,
      circularDurationMillis = circularDurationMillis,
      highlightAnimation = highlightAnimation,
      highlightAnimationStartDelayMillis = highlightAnimationStartDelayMillis,
      rotateAnimation = rotateAnimation,
      alpha = alpha,
      isVisibleOverlay = isVisibleOverlay,
      overlayColor = overlayColor,
      overlayPadding = PaddingValues(
        start = overlayPaddingStart,
        top = overlayPaddingTop,
        end = overlayPaddingEnd,
        bottom = overlayPaddingBottom,
      ),
      overlayShape = overlayShape,
      overlayAnimation = overlayAnimation,
      dismissWhenOverlayClicked = dismissWhenOverlayClicked,
      dismissWhenClicked = dismissWhenClicked,
      dismissWhenShowAgain = dismissWhenShowAgain,
      focusable = focusable,
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
