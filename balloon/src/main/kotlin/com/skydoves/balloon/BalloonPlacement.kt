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

import android.view.View

/** BalloonPlacement contains data to determinate position where balloon should be displayed  */
internal data class BalloonPlacement(
  val anchor: View,
  val subAnchors: List<View> = emptyList(),
  val align: BalloonAlign = BalloonAlign.TOP,
  val xOff: Int = 0,
  val yOff: Int = 0,
  val type: PlacementType = PlacementType.ALIGNMENT,
)
