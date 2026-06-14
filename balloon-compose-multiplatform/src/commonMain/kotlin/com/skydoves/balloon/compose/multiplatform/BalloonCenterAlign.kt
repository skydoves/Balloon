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
 * BalloonCenterAlign describes on which side of the anchor's center the balloon is
 * placed when shown via [BalloonState.showAtCenter] / [BalloonState.awaitAtCenter].
 *
 * Unlike [BalloonAlign], the balloon is positioned relative to the anchor's center
 * point rather than its edges — mirroring the original Android `Balloon.showAtCenter`
 * behavior. The arrow points back toward the anchor center.
 *
 * - [TOP]: balloon is shown above the anchor center; arrow points down.
 * - [BOTTOM]: balloon is shown below the anchor center; arrow points up.
 * - [START]: balloon is shown on the leading side of the anchor center
 *   (left in LTR, right in RTL); arrow points toward the trailing side.
 * - [END]: balloon is shown on the trailing side of the anchor center
 *   (right in LTR, left in RTL); arrow points toward the leading side.
 */
public enum class BalloonCenterAlign {
  TOP,
  BOTTOM,
  START,
  END,
}
