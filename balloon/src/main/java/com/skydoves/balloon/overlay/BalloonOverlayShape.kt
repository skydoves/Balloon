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

package com.skydoves.balloon.overlay

import androidx.annotation.DimenRes

/** BalloonOverlay is a sealed interface for composing balloon overlay types. */
sealed class BalloonOverlayShape

/** draw a Rect for overlaying over an anchor. */
object BalloonOverlayRect : BalloonOverlayShape()

/** draw an Oval for overlaying over an anchor. */
object BalloonOverlayOval : BalloonOverlayShape()

/** draw an rounded Rect for overlaying over an anchor. */
class BalloonOverlayRoundRect private constructor(
  val radiusPair: Pair<Float, Float>? = null,
  val radiusResPair: Pair<Int, Int>? = null
) : BalloonOverlayShape() {
  constructor(radiusX: Float, radiusY: Float) : this(radiusPair = Pair(radiusX, radiusY))
  constructor(@DimenRes radiusXRes: Int, @DimenRes radiusYRes: Int) : this(radiusResPair = Pair(radiusXRes, radiusYRes))
}

/** draw a Circle for overlaying over an anchor. */
class BalloonOverlayCircle private constructor(
  val radius: Float? = null,
  val radiusRes: Int? = null
) : BalloonOverlayShape() {
  constructor(radius: Float) : this(radius, null)
  constructor(@DimenRes radiusRes: Int) : this(null, radiusRes)
}
