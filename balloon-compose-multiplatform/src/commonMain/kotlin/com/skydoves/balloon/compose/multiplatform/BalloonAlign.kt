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
 * BalloonAlign describes the position of the balloon relative to its anchor.
 *
 * - [TOP]: balloon is shown above the anchor; arrow points down toward the anchor.
 * - [BOTTOM]: balloon is shown below the anchor; arrow points up toward the anchor.
 * - [START]: balloon is shown on the leading side of the anchor (left in LTR / right in RTL);
 *   arrow points toward the anchor.
 * - [END]: balloon is shown on the trailing side of the anchor (right in LTR / left in RTL);
 *   arrow points toward the anchor.
 * - [CENTER]: balloon is rendered centered on top of the anchor (overlay-style). The
 *   arrow has no anchor edge to point at — call `setIsVisibleArrow(false)` (or set
 *   [BalloonStyle.isArrowVisible] to `false`) to hide it for a clean overlay.
 */
public enum class BalloonAlign {
  TOP,
  BOTTOM,
  START,
  END,
  CENTER,
}
