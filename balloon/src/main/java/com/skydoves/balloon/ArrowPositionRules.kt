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
 * ArrowPositionRules determines the position of the arrow depending on the aligning rules.
 *
 * [ArrowPositionRules.ALIGN_BALLOON]: Align the arrow position depending on the balloon popup body.
 * [ArrowPositionRules.ALIGN_ANCHOR]: Align the arrow position depending on an anchor.
 */
enum class ArrowPositionRules {
  /**
   * Align the arrow position depending on the balloon popup body.
   *
   * If [Balloon.Builder.arrowPosition] is 0.5, the arrow will be located in the middle of the tooltip.
   */
  ALIGN_BALLOON,

  /**
   * Align the arrow position depending on an anchor.
   *
   * If [Balloon.Builder.arrowPosition] is 0.5, the arrow will be located in the middle of an anchor.
   */
  ALIGN_ANCHOR
}
