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
 * ArrowOrientationRules determines the orientation of the arrow depending on the aligning rules.
 *
 * [ArrowOrientationRules.ALIGN_ANCHOR]: Align depending on the position of an anchor.
 * [ArrowOrientationRules.ALIGN_FIXED]: Align to fixed [ArrowOrientation].
 */
public enum class ArrowOrientationRules {
  /**
   * Align depending on the position of an anchor.
   *
   * For example, [Balloon.Builder.arrowOrientation] is [ArrowOrientation.TOP] and we want to show up
   * the balloon under an anchor using the [Balloon.showAlignBottom].
   * However, if there is not enough free space to place the tooltip at the bottom of the anchor,
   * tooltips will be placed on top of the anchor and the orientation of the arrow will be [ArrowOrientation.BOTTOM].
   */
  ALIGN_ANCHOR,

  /**
   * Align to fixed [ArrowOrientation].
   *
   * The orientation of the arrow will be fixed by the specific [Balloon.Builder.arrowOrientation].
   */
  ALIGN_FIXED
}
