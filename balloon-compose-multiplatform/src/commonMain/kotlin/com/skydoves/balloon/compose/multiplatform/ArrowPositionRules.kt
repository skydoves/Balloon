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
 * ArrowPositionRules determines the position of the arrow depending on the aligning rules.
 *
 * - [ALIGN_BALLOON]: Align the arrow position depending on the balloon popup body.
 *   If `arrowPositionRatio` is `0.5f`, the arrow will be located in the middle of the tooltip.
 * - [ALIGN_ANCHOR]: Align the arrow position depending on an anchor.
 *   If `arrowPositionRatio` is `0.5f`, the arrow will be located in the middle of the anchor.
 */
public enum class ArrowPositionRules {
  ALIGN_BALLOON,
  ALIGN_ANCHOR,
}
