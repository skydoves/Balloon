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

import com.skydoves.balloon.annotations.InternalBalloonApi

/**
 * Represents padding values to be applied around a [Balloon] overlay.
 *
 * @property left The padding on the left side in pixels.
 * @property top The padding on the top side in pixels.
 * @property right The padding on the right side in pixels.
 * @property bottom The padding on the bottom side in pixels.
 */
@InternalBalloonApi
public data class BalloonOverlayPadding(
  val left: Float = 0f,
  val top: Float = 0f,
  val right: Float = 0f,
  val bottom: Float = 0f
)