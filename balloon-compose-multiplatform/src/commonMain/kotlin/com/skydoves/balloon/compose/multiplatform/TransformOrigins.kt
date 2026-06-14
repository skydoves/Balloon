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

import androidx.compose.ui.graphics.TransformOrigin

/**
 * Computes the [TransformOrigin] that scale-based balloon transitions should pivot around,
 * given the balloon's [BalloonAlign] relative to its anchor.
 *
 * The chosen origin sits on the edge of the balloon nearest the anchor, so scale-in/out
 * animations appear to grow from / collapse toward the arrow tip rather than the balloon's
 * geometric center.
 */
internal fun transformOriginFor(align: BalloonAlign): TransformOrigin = when (align) {
  // Balloon is above anchor, arrow at bottom -> animate from bottom-center
  BalloonAlign.TOP -> TransformOrigin(0.5f, 1f)
  // Balloon is below anchor, arrow at top -> animate from top-center
  BalloonAlign.BOTTOM -> TransformOrigin(0.5f, 0f)
  // Balloon is to the start side of anchor, arrow at end edge -> animate from end-center
  BalloonAlign.START -> TransformOrigin(1f, 0.5f)
  // Balloon is to the end side of anchor, arrow at start edge -> animate from start-center
  BalloonAlign.END -> TransformOrigin(0f, 0.5f)
  // Balloon is centered on the anchor (overlay) -> animate from the geometric center
  BalloonAlign.CENTER -> TransformOrigin.Center
}
