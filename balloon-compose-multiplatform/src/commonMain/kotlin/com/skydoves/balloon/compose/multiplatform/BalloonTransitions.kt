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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin

/**
 * Returns an [EnterTransition] for the given [BalloonAnimation].
 *
 * @param animation The chosen animation type.
 * @param durationMillis Total transition duration.
 * @param transformOrigin The transform origin for scale-based animations. For balloons
 *   pointing at an anchor, this should typically be on the side closest to the anchor
 *   (e.g., for a balloon BELOW the anchor with arrow pointing up, origin = (0.5f, 0f)).
 */
public fun balloonEnterTransition(
  animation: BalloonAnimation,
  durationMillis: Int = 250,
  transformOrigin: TransformOrigin = TransformOrigin.Center,
): EnterTransition = when (animation) {
  BalloonAnimation.NONE -> EnterTransition.None
  BalloonAnimation.FADE -> fadeIn(animationSpec = tween(durationMillis))
  BalloonAnimation.ELASTIC -> {
    // Spring with a bouncy effect, mirroring the original `OvershootInterpolator(2f)`.
    // Scale grows from 0.6 -> 1.0 and is paired with a fade so the bounce never reveals
    // a partially-positioned balloon abruptly.
    scaleIn(
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
      ),
      initialScale = 0.6f,
      transformOrigin = transformOrigin,
    ) + fadeIn(animationSpec = tween(durationMillis))
  }
  BalloonAnimation.OVERSHOOT -> {
    // Milder overshoot than ELASTIC: smaller scale delta and stiffer/less bouncy spring.
    scaleIn(
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
      ),
      initialScale = 0.85f,
      transformOrigin = transformOrigin,
    ) + fadeIn(animationSpec = tween(durationMillis))
  }
  BalloonAnimation.CIRCULAR -> {
    // Compose Multiplatform has no built-in circular reveal primitive, so we approximate
    // it with a scale-from-zero on an eased curve combined with a short cross-fade. The
    // [transformOrigin] parameter biases the growth direction toward the anchor side,
    // which visually reads as a directional reveal rather than a centered pop.
    scaleIn(
      animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
      initialScale = 0f,
      transformOrigin = transformOrigin,
    ) + fadeIn(animationSpec = tween(durationMillis / 2))
  }
}

/**
 * Returns an [ExitTransition] for the given [BalloonAnimation].
 *
 * @param animation The chosen animation type.
 * @param durationMillis Total transition duration.
 * @param transformOrigin The transform origin for scale-based animations. Mirror of the
 *   value used for [balloonEnterTransition] so the balloon collapses back toward its
 *   anchor side.
 */
public fun balloonExitTransition(
  animation: BalloonAnimation,
  durationMillis: Int = 200,
  transformOrigin: TransformOrigin = TransformOrigin.Center,
): ExitTransition = when (animation) {
  BalloonAnimation.NONE -> ExitTransition.None
  BalloonAnimation.FADE -> fadeOut(animationSpec = tween(durationMillis))
  BalloonAnimation.ELASTIC, BalloonAnimation.OVERSHOOT -> {
    // Use a deterministic tween on exit (no bounce) so the balloon dismisses cleanly even
    // if a new show is queued immediately afterward.
    scaleOut(
      animationSpec = tween(durationMillis),
      targetScale = 0.6f,
      transformOrigin = transformOrigin,
    ) + fadeOut(animationSpec = tween(durationMillis))
  }
  BalloonAnimation.CIRCULAR -> {
    scaleOut(
      animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
      targetScale = 0f,
      transformOrigin = transformOrigin,
    ) + fadeOut(animationSpec = tween(durationMillis / 2))
  }
}
