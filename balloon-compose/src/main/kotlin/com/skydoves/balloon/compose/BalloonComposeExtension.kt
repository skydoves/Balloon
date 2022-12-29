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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.skydoves.balloon.Balloon

/** sets a color of the arrow. */
public fun Balloon.Builder.setArrowColor(color: Color): Balloon.Builder = apply {
  setArrowColor(color.toArgb())
}

/** sets the background color of the arrow and popup. */
public fun Balloon.Builder.setBackgroundColor(color: Color): Balloon.Builder = apply {
  setBackgroundColor(color.toArgb())
}

/** sets the color of the main text content. */
public fun Balloon.Builder.setTextColor(color: Color): Balloon.Builder = apply {
  setBackgroundColor(color.toArgb())
}

/** sets the color of the icon drawable. */
public fun Balloon.Builder.setIconColor(color: Color): Balloon.Builder = apply {
  setBackgroundColor(color.toArgb())
}

/** background color of the overlay. */
public fun Balloon.Builder.setOverlayColor(color: Color): Balloon.Builder = apply {
  setBackgroundColor(color.toArgb())
}

/** sets color of the overlay padding. */
public fun Balloon.Builder.setOverlayPaddingColor(color: Color): Balloon.Builder = apply {
  setBackgroundColor(color.toArgb())
}
