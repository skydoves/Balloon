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

import androidx.compose.ui.unit.LayoutDirection

/**
 * ArrowOrientation determines the orientation of the arrow.
 *
 * - [TOP]: arrow points up; the arrow notch is on the top edge of the balloon body.
 * - [BOTTOM]: arrow points down; the arrow notch is on the bottom edge.
 * - [START]: arrow points toward the leading edge (left in LTR, right in RTL).
 * - [END]: arrow points toward the trailing edge (right in LTR, left in RTL).
 */
public enum class ArrowOrientation {
  TOP,
  BOTTOM,
  START,
  END,
}

/**
 * Resolves a directional [ArrowOrientation] (START/END) to its absolute equivalent
 * (LEFT/RIGHT) for the given [layoutDirection].
 *
 * Because [ArrowOrientation] only exposes TOP/BOTTOM/START/END, this internal helper
 * uses [ResolvedArrowSide] to express the four absolute sides used by the path builder.
 */
internal fun ArrowOrientation.resolve(layoutDirection: LayoutDirection): ResolvedArrowSide {
  val isRtl = layoutDirection == LayoutDirection.Rtl
  return when (this) {
    ArrowOrientation.TOP -> ResolvedArrowSide.TOP
    ArrowOrientation.BOTTOM -> ResolvedArrowSide.BOTTOM
    ArrowOrientation.START -> if (isRtl) ResolvedArrowSide.RIGHT else ResolvedArrowSide.LEFT
    ArrowOrientation.END -> if (isRtl) ResolvedArrowSide.LEFT else ResolvedArrowSide.RIGHT
  }
}

/**
 * Absolute resolution of an [ArrowOrientation] after taking layout direction into account.
 * Used internally by the path builder so it doesn't have to reason about RTL.
 */
internal enum class ResolvedArrowSide {
  TOP,
  BOTTOM,
  LEFT,
  RIGHT,
}
