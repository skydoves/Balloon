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

/**
 * BalloonAnimation describes the enter/exit transition style applied to a balloon
 * when it is shown or dismissed.
 *
 * Each entry reproduces the corresponding window animation of the View implementation, with
 * the same duration, the same interpolator, and the same centre pivot. The transitions
 * themselves are produced by [balloonEnterTransition] and [balloonExitTransition].
 *
 * - [NONE]: no animation; the balloon appears and disappears instantly.
 * - [FADE]: alpha 0 to 1 over 200ms, linear, and the reverse on exit.
 * - [ELASTIC]: scale 0.5 to 1 over 250ms with `BounceInterpolator`.
 * - [OVERSHOOT]: scale 0.5 to 1 over 250ms with `OvershootInterpolator(2f)`.
 * - [CIRCULAR]: a true clip-circle reveal growing from radius 0 to `max(width, height)` over
 *   [BalloonStyle.circularDurationMillis], matching `ViewAnimationUtils.createCircularReveal`.
 *   Also turns [BalloonStyle.focusable] off, as the original did.
 *
 * ELASTIC and OVERSHOOT share a 250ms scale-to-zero exit; CIRCULAR uses a 200ms one.
 */
public enum class BalloonAnimation {
  NONE,
  FADE,
  ELASTIC,
  OVERSHOOT,
  CIRCULAR,
}
