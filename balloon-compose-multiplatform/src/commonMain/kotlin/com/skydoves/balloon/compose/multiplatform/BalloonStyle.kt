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
 * @property arrowSize width and height of the triangular arrow notch. Set width or
 *   height to `0.dp` to hide the arrow.
 * @property arrowOrientation explicit orientation of the arrow. When `null` the
 *   orientation is derived automatically from the [BalloonAlign] used to show the
 *   balloon (e.g. [BalloonAlign.BOTTOM] -> arrow points up).
 * @property arrowPosition fraction along the relevant edge (0.0..1.0) where the
 *   arrow is anchored. `0.5f` centers the arrow.
 * @property arrowPositionRules whether [arrowPosition] is interpreted relative to
 *   the balloon body or the anchor.
 * @property isArrowVisible Whether the arrow notch is drawn. When `false` the
 *   balloon renders as a plain rounded rectangle.
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
 * @property width fixed body width. [Dp.Unspecified] (the default) wraps the content,
 *   mirroring `BalloonSizeSpec.WRAP`.
 * @property widthRatio body width as a fraction of the window width. `0f` (the default)
 *   disables it. Takes precedence over [width] and [maxWidth], mirroring `setWidthRatio`.
 * @property minWidthRatio lower bound of the body width as a fraction of the window
 *   width. `0f` disables it.
 * @property maxWidthRatio upper bound of the body width as a fraction of the window
 *   width. `0f` disables it.
 * @property minWidth lower bound of the body width. [Dp.Unspecified] disables it.
 * @property maxWidth upper bound of the body width. [Dp.Unspecified] means no constraint
 *   beyond the window width.
 * @property height fixed body height. [Dp.Unspecified] (the default) wraps the content.
 * @property animation enter / exit transition family.
 * @property circularDurationMillis duration of the [BalloonAnimation.CIRCULAR] reveal.
 * @property highlightAnimation looping animation played while the balloon is showing.
 * @property highlightAnimationStartDelayMillis delay before [highlightAnimation] starts.
 * @property alpha opacity of the whole balloon body, mirroring `setAlpha`.
 * @property isVisibleOverlay whether a dimming scrim with a cut-out around the anchor is
 *   drawn behind the balloon.
 * @property overlayColor color of that scrim.
 * @property overlayPadding extra space added around the anchor before the cut-out shape
 *   is drawn.
 * @property overlayShape shape of the anchor cut-out.
 * @property dismissWhenOverlayClicked whether tapping the scrim dismisses the balloon.
 * @property dismissWhenClicked whether tapping the balloon body itself dismisses it.
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
public data class BalloonStyle(
  val cornerRadius: Dp = 5.dp,
  val arrowSize: DpSize = DpSize(12.dp, 12.dp),
  val arrowOrientation: ArrowOrientation? = null,
  val arrowPosition: Float = 0.5f,
  val arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
  val isArrowVisible: Boolean = true,
  val backgroundColor: Color = Color.Black,
  val arrowColor: Color = Color.Unspecified,
  val borderColor: Color = Color.Unspecified,
  val borderThickness: Dp = 0.dp,
  val padding: PaddingValues = PaddingValues(0.dp),
  val margin: PaddingValues = PaddingValues(0.dp),
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
  val alpha: Float = 1f,
  val isVisibleOverlay: Boolean = false,
  val overlayColor: Color = Color.Transparent,
  val overlayPadding: Dp = 0.dp,
  val overlayShape: BalloonOverlayShape = BalloonOverlayShape.Oval,
  val dismissWhenOverlayClicked: Boolean = true,
  val dismissWhenClicked: Boolean = false,
  val focusable: Boolean = true,
  val dismissOnClickOutside: Boolean = true,
  val dismissOnBackPress: Boolean = true,
  val autoDismissMillis: Long = 0L,
)

/**
 * The default [BalloonStyle], matching the defaults of the Android `Balloon.Builder`:
 * a black body with a 5dp corner radius, a 12x12dp arrow, and no padding or margin.
 * Useful as a starting point for `copy(...)`-based customization.
 */
public val DefaultBalloonStyle: BalloonStyle = BalloonStyle()
