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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin

/*
 * Enter / exit transitions reproducing the window animations of the Android-only Balloon
 * library one-for-one.
 *
 * The View implementation drives these through `PopupWindow.animationStyle`, i.e. the
 * `Balloon_*_Anim` styles in `balloon/src/main/res/values/styles.xml`. Each branch below
 * matches the corresponding `res/anim` resource — same duration, same interpolator, same
 * scale endpoints, and (importantly) the same CENTRE pivot: every scale animation in the
 * original uses `pivotX="50%" pivotY="50%"`, regardless of where the arrow sits.
 *
 * | animation | enter                                                    | exit                     |
 * |-----------|----------------------------------------------------------|--------------------------|
 * | NONE      | `balloon_none_in` — alpha 0→1, 0ms                        | `balloon_none_out`, 0ms  |
 * | FADE      | `balloon_fade_in` — alpha 0→1, 200ms linear               | `balloon_fade_out`, 200ms|
 * | ELASTIC   | `balloon_elastic_center` — scale .5→1, 250ms bounce       | `balloon_dispose_center` |
 * | OVERSHOOT | `balloon_overshoot_center` — scale .5→1, 250ms overshoot  | `balloon_dispose_center` |
 * | CIRCULAR  | circular reveal (see `Modifier.balloonCircularReveal`)    | `balloon_show_down_center`|
 *
 * `BalloonAnimation.CIRCULAR`'s enter is not expressible as an [EnterTransition]; it is a
 * true clip-circle reveal applied by `Modifier.balloonCircularReveal` in `BalloonContent`,
 * matching `ViewAnimationUtils.createCircularReveal`. Only its exit lives here — which is
 * also what the View version does, since `Balloon_Normal_Dispose_Anim` declares an exit
 * animation only.
 */

/** Duration of `balloon_fade_in` / `balloon_fade_out`. */
private const val FADE_DURATION = 200

/** Duration of `balloon_elastic_center`, `balloon_overshoot_center`, `balloon_dispose_center`. */
private const val SCALE_DURATION = 250

/** Duration of `balloon_show_down_center`, the CIRCULAR exit. */
private const val SHOW_DOWN_DURATION = 200

/** Initial scale of `balloon_elastic_center` / `balloon_overshoot_center`. */
private const val SCALE_FROM = 0.5f

/**
 * Returns the [EnterTransition] matching the window enter animation of [animation].
 *
 * @param animation the chosen animation family.
 */
internal fun balloonEnterTransition(
  animation: BalloonAnimation,
): EnterTransition = when (animation) {
  BalloonAnimation.NONE -> EnterTransition.None
  BalloonAnimation.FADE -> fadeIn(tween(FADE_DURATION, easing = LinearEasing))
  BalloonAnimation.ELASTIC -> scaleIn(
    animationSpec = tween(SCALE_DURATION, easing = BounceEasing),
    initialScale = SCALE_FROM,
    transformOrigin = TransformOrigin.Center,
  )
  BalloonAnimation.OVERSHOOT -> scaleIn(
    animationSpec = tween(SCALE_DURATION, easing = OvershootEasing),
    initialScale = SCALE_FROM,
    transformOrigin = TransformOrigin.Center,
  )
  BalloonAnimation.CIRCULAR -> EnterTransition.None
}

/**
 * Returns the [ExitTransition] matching the window exit animation of [animation].
 *
 * @param animation the chosen animation family.
 */
internal fun balloonExitTransition(
  animation: BalloonAnimation,
): ExitTransition = when (animation) {
  BalloonAnimation.NONE -> ExitTransition.None
  BalloonAnimation.FADE -> fadeOut(tween(FADE_DURATION, easing = LinearEasing))
  // `balloon_dispose_center`: scale 1 -> 0, 250ms, implicit accelerate/decelerate.
  BalloonAnimation.ELASTIC, BalloonAnimation.OVERSHOOT -> scaleOut(
    animationSpec = tween(SCALE_DURATION, easing = AccelerateDecelerateEasing),
    targetScale = 0f,
    transformOrigin = TransformOrigin.Center,
  )
  // `balloon_show_down_center`: scale 1 -> 0, 200ms, implicit accelerate/decelerate.
  BalloonAnimation.CIRCULAR -> scaleOut(
    animationSpec = tween(SHOW_DOWN_DURATION, easing = AccelerateDecelerateEasing),
    targetScale = 0f,
    transformOrigin = TransformOrigin.Center,
  )
}
