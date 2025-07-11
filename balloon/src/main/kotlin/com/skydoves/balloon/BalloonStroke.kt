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

import androidx.annotation.ColorInt
import com.skydoves.balloon.annotations.Dp

public data class BalloonStroke(
  @ColorInt public val color: Int,
  @Dp public val thickness: Float = 1f,
) {
  public companion object {
    /**
     * The stroke looks visually thinner due to how it's drawn over the path,
     * so we multiply thickness to compensate for better visibility.
     * this is a workaround for the issue.
     **/
    public const val STROKE_THICKNESS_MULTIPLIER: Float = 1.5f
  }
}
