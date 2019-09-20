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

import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.ImageViewCompat

/** applies icon form attributes to a ImageView instance. */
internal fun ImageView.applyIconForm(iconForm: IconForm) {
  iconForm.drawable?.let {
    setImageDrawable(it)
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(iconForm.iconColor))
    val params = LinearLayout.LayoutParams(iconForm.iconSize, iconForm.iconSize)
    params.setMargins(0, 0, iconForm.iconSpace, 0)
    layoutParams = params
    visible(true)
  }
}
