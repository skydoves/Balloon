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
 * @property padding inner padding applied around the balloon's content.
 * @property maxWidth maximum width constraint for the balloon body. [Dp.Unspecified]
 *   means no constraint (the body wraps its content).
 * @property animation enter / exit transition family.
 * @property animationDurationMillis duration of [animation] in milliseconds.
 * @property dismissOnClickOutside whether tapping outside the balloon dismisses it.
 * @property dismissOnBackPress whether the back button / Escape key dismisses it.
 * @property autoDismissMillis when greater than zero, the balloon is automatically
 *   dismissed this many milliseconds after it becomes visible. `0L` (the default)
 *   disables auto-dismiss.
 */
@Immutable
public data class BalloonStyle(
  val cornerRadius: Dp = 5.dp,
  val arrowSize: DpSize = DpSize(15.dp, 12.dp),
  val arrowOrientation: ArrowOrientation? = null,
  val arrowPosition: Float = 0.5f,
  val arrowPositionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
  val isArrowVisible: Boolean = true,
  val backgroundColor: Color = Color(0xFF272727),
  val arrowColor: Color = Color.Unspecified,
  val borderColor: Color = Color.Unspecified,
  val borderThickness: Dp = 0.dp,
  val padding: PaddingValues = PaddingValues(8.dp),
  val maxWidth: Dp = Dp.Unspecified,
  val animation: BalloonAnimation = BalloonAnimation.FADE,
  val animationDurationMillis: Int = 250,
  val dismissOnClickOutside: Boolean = true,
  val dismissOnBackPress: Boolean = true,
  val autoDismissMillis: Long = 0L,
)

/**
 * The default [BalloonStyle], matching the visual defaults of the original Android
 * `Balloon.Builder`. Useful as a starting point for `copy(...)`-based customization.
 */
public val DefaultBalloonStyle: BalloonStyle = BalloonStyle()
