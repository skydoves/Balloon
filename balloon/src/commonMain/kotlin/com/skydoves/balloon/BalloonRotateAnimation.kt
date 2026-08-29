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

import androidx.compose.runtime.Immutable

/** Which way [BalloonRotateAnimation] spins around the Y axis. */
public enum class BalloonRotateDirection(public val value: Int) {
  /** Clockwise seen from above. */
  RIGHT(1),

  /** Counter-clockwise seen from above. */
  LEFT(-1),
}

/**
 * Parameters of [BalloonHighlightAnimation.ROTATE] — a 3D spin of the whole balloon.
 *
 * Ports `com.skydoves.balloon.animations.BalloonRotateAnimation`, which drives an
 * `android.graphics.Camera` from a plain `Animation`: the balloon turns
 * `360 * turns * direction` degrees around Y (plus optional fixed X / Z sweeps) over
 * [speedMillis], repeating [loops] times about its center. The Android version's
 * default interpolator is accelerate/decelerate, which [AccelerateDecelerateEasing]
 * reproduces here.
 *
 * @property direction which way the Y rotation goes.
 * @property turns how many full turns each loop performs.
 * @property loops how many times the animation runs. [INFINITE] (the default) repeats forever.
 * @property speedMillis duration of a single loop.
 * @property degreeX total rotation around the X axis per loop.
 * @property degreeZ total rotation around the Z axis per loop.
 */
@Immutable
public data class BalloonRotateAnimation(
  val direction: BalloonRotateDirection = BalloonRotateDirection.RIGHT,
  val turns: Int = 1,
  val loops: Int = INFINITE,
  val speedMillis: Int = 2500,
  val degreeX: Int = 0,
  val degreeZ: Int = 0,
) {
  /** Total Y rotation of one loop, in degrees. */
  internal val degreeY: Float get() = (360 * turns).toFloat() * direction.value

  public companion object {
    /** Repeat the rotation forever, mirroring `android.view.animation.Animation.INFINITE`. */
    public const val INFINITE: Int = -1
  }
}
