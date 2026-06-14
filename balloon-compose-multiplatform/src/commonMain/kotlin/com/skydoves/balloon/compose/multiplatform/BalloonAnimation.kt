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

/**
 * BalloonAnimation describes the enter/exit transition style applied to a balloon
 * when it is shown or dismissed.
 *
 * This is the Compose Multiplatform counterpart to the View-based animation enum in the
 * core balloon module. The actual transitions are produced by [balloonEnterTransition]
 * and [balloonExitTransition].
 *
 * - [NONE]: no animation; the balloon appears and disappears instantly.
 * - [FADE]: pure alpha cross-fade (~150ms originally).
 * - [ELASTIC]: scale-in with a bouncy/overshoot spring (originally `OvershootInterpolator(2f)`).
 * - [OVERSHOOT]: scale-in with a milder overshoot than [ELASTIC].
 * - [CIRCULAR]: an approximation of a circular reveal (Compose has no built-in circular
 *   reveal primitive, so this is implemented as a scale-from-zero with eased curve plus a
 *   shorter fade — see [balloonEnterTransition]).
 */
public enum class BalloonAnimation {
  NONE,
  FADE,
  ELASTIC,
  OVERSHOOT,
  CIRCULAR,
}
