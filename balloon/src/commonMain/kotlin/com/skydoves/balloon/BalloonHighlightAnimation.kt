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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * A looping animation played on the balloon *while it is showing*, to draw the user's eye.
 *
 * These mirror `BalloonHighlightAnimation` in the Android-only library, including the exact
 * durations, magnitudes and pivots of the corresponding `res/anim` resources. Every one of
 * them repeats infinitely in [RepeatMode.Reverse] with a linear interpolator.
 *
 * - [NONE]: no highlight animation (the default).
 * - [HEARTBEAT]: pulses between 100% and 90% scale over 800ms
 *   (`balloon_heartbeat_*`), pivoting on the arrow edge so the arrow stays pinned to the
 *   anchor. Pivots on the centre when the arrow is hidden.
 * - [SHAKE]: translates 13% of the balloon's size AWAY from the arrow edge and back over
 *   650ms (`balloon_shake_*`).
 * - [BREATH]: fades between 75% and 100% alpha over 800ms (`balloon_fade`).
 * - [ROTATE]: spins the balloon in 3D, configured by [BalloonStyle.rotateAnimation].
 */
public enum class BalloonHighlightAnimation {
  NONE,
  HEARTBEAT,
  SHAKE,
  BREATH,
  ROTATE,
}

/** Duration of `balloon_heartbeat_*` and `balloon_fade`. */
private const val HEARTBEAT_DURATION = 800

/** Duration of `balloon_shake_*`. */
private const val SHAKE_DURATION = 650

/** `toXScale` / `toYScale` of `balloon_heartbeat_*`. */
private const val HEARTBEAT_TO_SCALE = 0.9f

/** `fromAlpha` of `balloon_fade`. */
private const val BREATH_FROM_ALPHA = 0.75f

/** `toXDelta` / `toYDelta` of `balloon_shake_*`, as a fraction of the balloon's own size. */
private const val SHAKE_DELTA_FRACTION = 0.13f

/**
 * Applies the looping [animation] to this balloon body.
 *
 * The View implementation starts the highlight animation on the *whole* balloon root after
 * [startDelayMillis], and the animation runs for as long as the balloon is showing; this
 * modifier reproduces both the motion and that start delay.
 *
 * @param animation which highlight animation to run. [BalloonHighlightAnimation.NONE] returns
 *   this modifier unchanged, so there is no cost when the feature is unused.
 * @param arrowSide the resolved arrow edge, which decides the pivot / shake direction exactly
 *   as `getBalloonHighlightAnimation()` picks between the `_top` / `_bottom` / `_left` /
 *   `_right` animation resources.
 * @param isArrowVisible when false, HEARTBEAT falls back to `balloon_heartbeat_center`.
 * @param startDelayMillis delay before the animation starts, mirroring
 *   `balloonHighlightAnimationStartDelay`.
 * @param rotate parameters for [BalloonHighlightAnimation.ROTATE]; ignored otherwise.
 */
@Composable
internal fun Modifier.balloonHighlight(
  animation: BalloonHighlightAnimation,
  arrowSide: ResolvedArrowSide,
  isArrowVisible: Boolean,
  startDelayMillis: Long,
  rotate: BalloonRotateAnimation,
): Modifier {
  if (animation == BalloonHighlightAnimation.NONE) return this

  // The View version posts the animation start behind `balloonHighlightAnimationStartDelay`;
  // until it fires the balloon is drawn untransformed.
  var started by remember(animation, startDelayMillis) { mutableStateOf(startDelayMillis <= 0L) }
  LaunchedEffect(animation, startDelayMillis) {
    if (startDelayMillis > 0L) {
      delay(startDelayMillis)
      started = true
    }
  }
  if (!started) return this

  // ROTATE is the one highlight that does not repeat in reverse — `BalloonRotateAnimation`
  // is a plain `Animation` with RESTART repeat and a bounded loop count — so it is driven by
  // its own `Animatable` rather than the shared infinite transition below.
  if (animation == BalloonHighlightAnimation.ROTATE) return this.balloonRotate(rotate)

  val transition = rememberInfiniteTransition(label = "balloonHighlight")

  return when (animation) {
    BalloonHighlightAnimation.NONE, BalloonHighlightAnimation.ROTATE -> this

    BalloonHighlightAnimation.HEARTBEAT -> {
      val scale by transition.animateFloatReverse(
        from = 1f,
        to = HEARTBEAT_TO_SCALE,
        durationMillis = HEARTBEAT_DURATION,
        label = "heartbeat",
      )
      val origin = heartbeatOrigin(arrowSide, isArrowVisible)
      this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        transformOrigin = origin
      }
    }

    BalloonHighlightAnimation.SHAKE -> {
      val fraction by transition.animateFloatReverse(
        from = 0f,
        to = SHAKE_DELTA_FRACTION,
        durationMillis = SHAKE_DURATION,
        label = "shake",
      )
      val direction = shakeDirection(arrowSide)
      this.graphicsLayer {
        translationX = size.width * fraction * direction.x
        translationY = size.height * fraction * direction.y
      }
    }

    BalloonHighlightAnimation.BREATH -> {
      val value by transition.animateFloatReverse(
        from = BREATH_FROM_ALPHA,
        to = 1f,
        durationMillis = HEARTBEAT_DURATION,
        label = "breath",
      )
      this.graphicsLayer { alpha = value }
    }
  }
}

/**
 * The pivot [BalloonHighlightAnimation.HEARTBEAT] scales about.
 *
 * The View implementation picks the animation resource named after the side OPPOSITE the
 * arrow, and that resource pivots on the ARROW side: an arrow on TOP selects
 * `balloon_heartbeat_bottom`, whose pivot is `(50%, 0%)`. So the arrow edge stays pinned to
 * the anchor while the rest of the balloon pulses toward it. A hidden arrow uses
 * `balloon_heartbeat_center`.
 */
internal fun heartbeatOrigin(
  arrowSide: ResolvedArrowSide,
  isArrowVisible: Boolean,
): TransformOrigin = if (!isArrowVisible) {
  TransformOrigin.Center
} else {
  when (arrowSide) {
    ResolvedArrowSide.TOP -> TransformOrigin(0.5f, 0f)
    ResolvedArrowSide.BOTTOM -> TransformOrigin(0.5f, 1f)
    ResolvedArrowSide.LEFT -> TransformOrigin(0f, 0.5f)
    ResolvedArrowSide.RIGHT -> TransformOrigin(1f, 0.5f)
  }
}

/**
 * The unit direction [BalloonHighlightAnimation.SHAKE] slides in, as a fraction of the
 * balloon's own size.
 *
 * Same opposite-name selection as [heartbeatOrigin]: an arrow on TOP selects
 * `balloon_shake_bottom`, whose `toYDelta` is `+13%`. The balloon slides AWAY from the arrow,
 * so the tooltip tugs against the anchor it points at.
 */
internal fun shakeDirection(arrowSide: ResolvedArrowSide): Offset = when (arrowSide) {
  ResolvedArrowSide.TOP -> Offset(0f, 1f)
  ResolvedArrowSide.BOTTOM -> Offset(0f, -1f)
  ResolvedArrowSide.LEFT -> Offset(1f, 0f)
  ResolvedArrowSide.RIGHT -> Offset(-1f, 0f)
}

/**
 * Drives [BalloonHighlightAnimation.ROTATE].
 *
 * `BalloonRotateAnimation` in the Android original runs an `android.graphics.Camera` from a
 * plain `Animation`: `degree{X,Y,Z} * interpolatedTime` about the view's center, over
 * `speeds` ms, repeating `loops` times with the default RESTART mode and the default
 * accelerate/decelerate interpolator. All of that is reproduced here.
 *
 * The perspective is not identical. `android.graphics.Camera` sits at a fixed 576px
 * regardless of density, whereas `graphicsLayer`'s `cameraDistance` is scaled by it, so the
 * same rotation reads slightly flatter on a high density screen. Matching the original
 * exactly would mean pinning the camera to a raw pixel distance, which would in turn make
 * the effect vary by screen. Density-relative depth is the better default here.
 */
@Composable
private fun Modifier.balloonRotate(rotate: BalloonRotateAnimation): Modifier {
  val progress = remember(rotate) { Animatable(0f) }
  LaunchedEffect(rotate) {
    val spec = tween<Float>(rotate.speedMillis, easing = AccelerateDecelerateEasing)
    if (rotate.loops == BalloonRotateAnimation.INFINITE) {
      progress.animateTo(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(spec, RepeatMode.Restart),
      )
    } else {
      repeat(rotate.loops.coerceAtLeast(0)) {
        progress.snapTo(0f)
        progress.animateTo(1f, spec)
      }
    }
  }
  return this.graphicsLayer {
    val t = progress.value
    rotationX = rotate.degreeX * t
    rotationY = rotate.degreeY * t
    rotationZ = rotate.degreeZ * t
    transformOrigin = TransformOrigin.Center
  }
}

/**
 * Shorthand for the one spec every highlight animation shares: a linear tween that repeats
 * forever in reverse — the Compose equivalent of `repeatCount="infinite"` +
 * `repeatMode="reverse"` + `@android:anim/linear_interpolator`.
 */
@Composable
private fun InfiniteTransition.animateFloatReverse(
  from: Float,
  to: Float,
  durationMillis: Int,
  label: String,
) = animateFloat(
  initialValue = from,
  targetValue = to,
  animationSpec = reverseSpec(durationMillis),
  label = label,
)

private fun reverseSpec(durationMillis: Int): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
  animation = tween(durationMillis, easing = LinearEasing),
  repeatMode = RepeatMode.Reverse,
)
