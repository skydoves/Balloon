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
 * - [ALIGN_BALLOON]: the position is a fraction of the balloon's own popup box, so an
 *   `arrowPosition` of `0.5f` puts the arrow in the middle of the tooltip.
 * - [ALIGN_ANCHOR]: the position is a fraction of the ANCHOR, so an `arrowPosition` of
 *   `0.5f` points the arrow at the middle of the anchor wherever the balloon ends up.
 *   The arrow is kept `arrowSize * arrowAlignAnchorPaddingRatio + arrowAlignAnchorPadding`
 *   clear of the balloon's ends.
 */
public enum class ArrowPositionRules {
  ALIGN_BALLOON,
  ALIGN_ANCHOR,
}
