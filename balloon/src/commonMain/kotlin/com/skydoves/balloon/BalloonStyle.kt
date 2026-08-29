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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Immutable visual configuration for a Compose Multiplatform balloon.
 *
 * This is the KMP-clean counterpart to the Android `Balloon.Builder` configuration —
 * everything required to render a balloon body and arrow without any reference to
 * `Context`, `View`, `Drawable`, resources or Lifecycle. Construct directly via the
 * data-class constructor, or fluently through [Balloon.Builder] for migration parity
 * with the original Android API.
 *
 * Defaults match the Android `Balloon.Builder` defaults, so a ported call-site renders
 * identically without having to restate them.
 *
 * @property cornerRadius radius of the balloon body's rounded corners.
 * @property arrowSize width and height of the triangular arrow notch. Width is the base
 *   along the balloon edge and height is how far the tip sticks out. Set either to `0.dp`
 *   to hide the arrow.
 * @property arrowOrientation explicit orientation of the arrow. When `null` (the default)
 *   the orientation is derived from the [BalloonAlign] used to show the balloon (e.g.
 *   [BalloonAlign.BOTTOM] -> arrow points up), so the arrow points at the anchor without
 *   having to be named.
 * @property arrowPosition fraction along the relevant edge (0.0..1.0) where the
 *   arrow is anchored. `0.5f` centers the arrow.
 * @property arrowPositionRules whether [arrowPosition] is interpreted relative to
 *   the balloon body or the anchor.
 * @property arrowOrientationRules whether the arrow edge may follow the balloon when a lack
 *   of room flips it to the opposite side. Only meaningful together with an explicit
 *   [arrowOrientation].
 * @property arrowAlignAnchorPadding extra clearance kept between the arrow and the balloon's
 *   corners under [ArrowPositionRules.ALIGN_ANCHOR]. Mirrors `setArrowAlignAnchorPadding`.
 * @property arrowAlignAnchorPaddingRatio multiplier on the arrow's own size in that same
 *   clamp: the arrow never comes closer to a corner than
 *   `arrowSize * ratio + arrowAlignAnchorPadding`. Mirrors `setArrowAlignAnchorPaddingRatio`.
 * @property isArrowVisible whether the arrow notch is drawn. When `false` the balloon
 *   renders as a plain rounded rectangle **and** the space the arrow would occupy is
 *   released, so the body sits flush against the anchor.
 * @property backgroundColor fill color of the balloon body and (by default) the arrow.
 * @property arrowColor fill color of the arrow. When [Color.Unspecified] (the default)
 *   the arrow inherits [backgroundColor]. When specified and different from
 *   [backgroundColor], the arrow is painted in a separate layer on top of the body.
 * @property borderColor color of the optional outline. [Color.Unspecified] disables
 *   the border regardless of [borderThickness].
 * @property borderThickness thickness of the optional outline. `0.dp` disables it.
 * @property padding inner padding applied around the balloon's content, inside the body.
 * @property margin outer margin keeping the balloon away from the window edges. Mirrors
 *   `setMargin*`: it both insets the on-screen clamp and reduces the width available
 *   to the body.
 * @property elevation the inset the balloon reserves on the axis orthogonal to the arrow,
 *   mirroring `Balloon.Builder.setElevation` (which likewise defaults to `2.dp`). In the
 *   Android original this is the shadow's room; here it is kept because it is part of the
 *   popup box the width specs measure against, so a ported `setWidthRatio` lands on the same
 *   pixels. Set it to `0.dp` for a body that fills its width spec exactly.
 * @property width fixed width of the POPUP BOX, which is the visible card plus [margin] and
 *   the [elevation] inset, exactly as `setWidth` sized `bodyWindow.width` in the original.
 *   [Dp.Unspecified] (the default) wraps the content, mirroring `BalloonSizeSpec.WRAP`.
 *   Set [elevation] to `0.dp` and no margin for a card of precisely this width.
 * @property widthRatio popup-box width as a fraction of the window width. `0f` (the default)
 *   disables it. Takes precedence over every other width spec, mirroring `setWidthRatio`.
 * @property minWidthRatio lower bound of the popup-box width as a fraction of the window
 *   width. `0f` disables it. Together with [maxWidthRatio] it outranks [width].
 * @property maxWidthRatio upper bound of the popup-box width as a fraction of the window
 *   width. `0f` disables it, and an unset value behaves as `1f` once [minWidthRatio] is set.
 * @property minWidth lower bound of the popup-box width. [Dp.Unspecified] disables it.
 * @property maxWidth upper bound of the popup-box width. [Dp.Unspecified] means no
 *   constraint beyond the window width.
 * @property height fixed height of the popup box. [Dp.Unspecified] (the default) wraps the
 *   content. There is no height ratio, matching the original.
 * @property animation enter / exit transition family.
 * @property circularDurationMillis duration of the [BalloonAnimation.CIRCULAR] reveal.
 * @property highlightAnimation looping animation played while the balloon is showing.
 * @property highlightAnimationStartDelayMillis delay before [highlightAnimation] starts.
 * @property rotateAnimation parameters of [BalloonHighlightAnimation.ROTATE].
 * @property alpha opacity of the whole balloon body, mirroring `setAlpha`.
 * @property isVisibleOverlay whether a dimming scrim with a cut-out around the anchor is
 *   drawn behind the balloon.
 * @property overlayColor color of that scrim.
 * @property overlayPadding extra space added around the anchor before the cut-out shape
 *   is drawn.
 * @property overlayPaddingColor fill painted into the band [overlayPadding] opens up around
 *   the anchor, mirroring `setOverlayPaddingColor`. [Color.Unspecified] (the default) leaves
 *   the band transparent along with the rest of the cut-out.
 * @property overlayShape shape of the anchor cut-out.
 * @property overlayAnimation how the scrim appears and disappears.
 * @property dismissWhenOverlayClicked whether tapping the scrim dismisses the balloon.
 * @property dismissWhenClicked whether tapping the balloon body itself dismisses it.
 * @property dismissWhenTouchMargin whether a tap that lands in the balloon's margin, or in the
 *   space it reserves for the arrow and the elevation inset, dismisses it. That band is inside
 *   the balloon's own popup, so the framework does not treat a tap there as an outside click.
 *   Mirrors `setDismissWhenTouchMargin`, which likewise defaults to `true`, and like the
 *   original it only acts when [dismissOnClickOutside] is also on.
 * @property dismissWhenShowAgain whether showing an already-visible balloon dismisses it
 *   instead of re-showing it in place. Mirrors `setDismissWhenShowAgain`.
 * @property focusable whether the balloon's window takes input focus. Mirrors the original's
 *   `setFocusable`, which likewise defaults to `true`. On Android this is what lets the
 *   popup receive the back press, but it also makes the popup touch-modal (taps outside are
 *   consumed rather than passed through to the anchor) and steals IME focus. Set it to
 *   `false` for a non-modal, pass-through balloon.
 * @property dismissOnClickOutside whether tapping outside the balloon dismisses it.
 * @property dismissOnBackPress whether the back button / Escape key dismisses it.
 * @property autoDismissMillis when greater than zero, the balloon is automatically
 *   dismissed this many milliseconds after it becomes visible. `0L` (the default)
 *   disables auto-dismiss.
 */
@Immutable
@ConsistentCopyVisibility
public data class BalloonStyle internal constructor(
  val cornerRadius: Dp = 5.dp,
  val arrowSize: DpSize = DpSize(12.dp, 12.dp),
  val arrowOrientation: ArrowOrientation? = null,
  val arrowPosition: Float = 0.5f,
  val arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
  val arrowOrientationRules: ArrowOrientationRules = ArrowOrientationRules.ALIGN_ANCHOR,
  val arrowAlignAnchorPadding: Dp = 0.dp,
  val arrowAlignAnchorPaddingRatio: Float = 2.5f,
  val isArrowVisible: Boolean = true,
  val backgroundColor: Color = Color.Black,
  val arrowColor: Color = Color.Unspecified,
  val borderColor: Color = Color.Unspecified,
  val borderThickness: Dp = 0.dp,
  val padding: PaddingValues = PaddingValues(0.dp),
  val margin: PaddingValues = PaddingValues(0.dp),
  val elevation: Dp = 2.dp,
  val width: Dp = Dp.Unspecified,
  val widthRatio: Float = 0f,
  val minWidthRatio: Float = 0f,
  val maxWidthRatio: Float = 0f,
  val minWidth: Dp = Dp.Unspecified,
  val maxWidth: Dp = Dp.Unspecified,
  val height: Dp = Dp.Unspecified,
  val animation: BalloonAnimation = BalloonAnimation.FADE,
  val circularDurationMillis: Long = 500L,
  val highlightAnimation: BalloonHighlightAnimation = BalloonHighlightAnimation.NONE,
  val highlightAnimationStartDelayMillis: Long = 0L,
  val rotateAnimation: BalloonRotateAnimation = BalloonRotateAnimation(),
  val alpha: Float = 1f,
  val isVisibleOverlay: Boolean = false,
  val overlayColor: Color = Color.Transparent,
  val overlayPadding: PaddingValues = PaddingValues(0.dp),
  val overlayPaddingColor: Color = Color.Unspecified,
  val overlayShape: BalloonOverlayShape = BalloonOverlayShape.Oval,
  val overlayAnimation: BalloonOverlayAnimation = BalloonOverlayAnimation.FADE,
  val dismissWhenOverlayClicked: Boolean = true,
  val dismissWhenClicked: Boolean = false,
  val dismissWhenTouchMargin: Boolean = true,
  val dismissWhenShowAgain: Boolean = false,
  val focusable: Boolean = true,
  val dismissOnClickOutside: Boolean = true,
  val dismissOnBackPress: Boolean = true,
  val autoDismissMillis: Long = 0L,
) {
  /**
   * The arrow size actually used for layout and painting: [DpSize.Zero] when the arrow is
   * hidden or degenerate, so every caller collapses the notch — and the space reserved for
   * it — through one place.
   */
  internal val effectiveArrowSize: DpSize
    get() = if (isArrowVisible && arrowSize.width > 0.dp && arrowSize.height > 0.dp) {
      arrowSize
    } else {
      DpSize.Zero
    }
}

/**
 * The default [BalloonStyle], matching the defaults of the Android `Balloon.Builder`:
 * a black body with a 5dp corner radius, a 12x12dp arrow, and no padding or margin.
 * A convenient starting point for [derive].
 */
public val DefaultBalloonStyle: BalloonStyle = BalloonStyle()

/**
 * Derives a variant of this style, changing only what [block] sets.
 *
 * ```kotlin
 * val base = rememberBalloonBuilder {
 *   setCornerRadius(8.dp)
 *   setPadding(12.dp)
 * }
 * val warning = base.derive { setBackgroundColor(Color(0xFFFF6F00)) }
 * ```
 *
 * This is what a data class `copy` would give you, minus the binary-compatibility trap.
 * A generated `copy` pins all 43 properties into the published interface, so adding a 44th
 * option in any later 2.x would break every caller compiled against 2.0.0. This signature
 * never has to change, which is also why [BalloonStyle]'s constructor is internal and
 * [Balloon.Builder] is the only way to make one.
 *
 * Cheap enough to call while recomposing, which is how an animated style restyles a balloon
 * that is already showing.
 */
public fun BalloonStyle.derive(block: Balloon.Builder.() -> Unit): BalloonStyle =
  Balloon.Builder().loadFrom(this).apply(block).build()
