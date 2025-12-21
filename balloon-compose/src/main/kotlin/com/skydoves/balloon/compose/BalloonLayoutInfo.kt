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

package com.skydoves.balloon.compose

/**
 * Represent layout information of a Composable node.
 *
 * @property x the x coordinate of the composable.
 * @property y the y coordinate of the composable.
 * @property width the width size of the composable.
 * @property height the height size of the composable.
 */
internal data class BalloonLayoutInfo(
  val x: Float,
  val y: Float,
  val width: Int,
  val height: Int,
)
