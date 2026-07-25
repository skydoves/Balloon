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

import androidx.compose.animation.core.Easing
import kotlin.math.PI
import kotlin.math.cos

/**
 * Ports of the three platform interpolators the Android-only Balloon library drives its
 * window animations with, so the Compose Multiplatform balloon reproduces the exact same
 * motion curves on every target.
 *
 * The formulas are transcribed from AOSP `android.view.animation.*Interpolator`.
 */

/**
 * `android.view.animation.AccelerateDecelerateInterpolator` — the implicit default for an
 * `<set>` with no `android:interpolator`, which is what `balloon_dispose_center` and
 * `balloon_show_down_center` use.
 */
internal val AccelerateDecelerateEasing: Easing = Easing { fraction ->
  (cos((fraction + 1f) * PI).toFloat() / 2f) + 0.5f
}

/**
 * `android.view.animation.BounceInterpolator` — used by `balloon_elastic_center`, the enter
 * animation of [BalloonAnimation.ELASTIC].
 */
internal val BounceEasing: Easing = Easing { fraction ->
  fun bounce(t: Float) = t * t * 8f
  val t = fraction * 1.1226f
  when {
    t < 0.3535f -> bounce(t)
    t < 0.7408f -> bounce(t - 0.54719f) + 0.7f
    t < 0.9644f -> bounce(t - 0.8526f) + 0.9f
    else -> bounce(t - 1.0435f) + 0.95f
  }
}

/**
 * `android.view.animation.OvershootInterpolator` — used by `balloon_overshoot_center`, the
 * enter animation of [BalloonAnimation.OVERSHOOT].
 *
 * @param tension the amount of overshoot. AOSP's no-arg constructor uses `2.0f`, which is
 *   what the balloon animation resource inherits.
 */
internal fun overshootEasing(tension: Float = 2.0f): Easing = Easing { fraction ->
  val t = fraction - 1.0f
  t * t * ((tension + 1) * t + tension) + 1.0f
}

/** The [overshootEasing] instance matching the platform default tension. */
internal val OvershootEasing: Easing = overshootEasing()
