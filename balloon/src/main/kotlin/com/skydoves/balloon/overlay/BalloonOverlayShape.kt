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
public sealed class BalloonOverlayShape

/** draw nothing over an anchor. */
public object BalloonOverlayEmpty : BalloonOverlayShape()

/** draw a Rect for overlaying over an anchor. */
public object BalloonOverlayRect : BalloonOverlayShape()

/** draw an Oval for overlaying over an anchor. */
public object BalloonOverlayOval : BalloonOverlayShape()

/** draw an rounded Rect for overlaying over an anchor. */
public class BalloonOverlayRoundRect private constructor(
  public val radiusPair: Pair<Float, Float>? = null,
  public val radiusResPair: Pair<Int, Int>? = null
) : BalloonOverlayShape() {
  public constructor(radiusX: Float, radiusY: Float) : this(radiusPair = Pair(radiusX, radiusY))
  public constructor(
    @DimenRes radiusXRes: Int,
    @DimenRes radiusYRes: Int
  ) : this(radiusResPair = Pair(radiusXRes, radiusYRes))
}

/** draw a Circle for overlaying over an anchor. */
public class BalloonOverlayCircle private constructor(
  public val radius: Float? = null,
  public val radiusRes: Int? = null
) : BalloonOverlayShape() {
  public constructor(radius: Float) : this(radius, null)
  public constructor(@DimenRes radiusRes: Int) : this(null, radiusRes)
}
