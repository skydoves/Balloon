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
 * Decides whether the arrow edge may be changed by where the balloon actually lands.
 *
 * - [ALIGN_ANCHOR] (the default): the arrow always points back at the anchor. The edge is
 *   derived from the alignment the balloon was shown with, and follows the balloon when a
 *   lack of room flips it to the opposite side.
 * - [ALIGN_FIXED]: the arrow stays on the edge named by
 *   [Balloon.Builder.setArrowOrientation] no matter where the balloon ends up. With no
 *   explicit orientation this is the same as [ALIGN_ANCHOR], since there is nothing to pin.
 *
 * Mirrors `com.skydoves.balloon.ArrowOrientationRules`. One difference is worth knowing when
 * porting: the Android original's `arrowOrientation` is a plain default of
 * `ArrowOrientation.BOTTOM` and its auto-rule only ever swaps an orientation for the opposite
 * one on the *same* axis, so `showAlignStart()` on a default builder leaves the arrow pointing
 * *down* rather than sideways. Here the orientation is derived from the alignment, so the
 * arrow points at the anchor without having to be named.
 */
public enum class ArrowOrientationRules {
  ALIGN_ANCHOR,
  ALIGN_FIXED,
}
