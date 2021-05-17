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

package com.skydoves.balloon.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.DrawableCompat

/** apply tinting to a drawable using a color. */
@JvmSynthetic
internal fun Drawable.tint(@ColorInt tintColor: Int?): Drawable = apply {
  tintColor?.let {
    DrawableCompat.wrap(this)
      .apply { DrawableCompat.setTintList(this, ColorStateList.valueOf(it)) }
  }
}

/** resize a drawable width and height size using specific pixel sizes.  */
@JvmSynthetic
internal fun Drawable.resize(context: Context, @Px width: Int?, @Px height: Int?): Drawable =
  if (width != null && height != null) {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, width, height)
    draw(canvas)
    BitmapDrawable(context.resources, bitmap)
  } else this

/** returns true if there is a start/left or end/right drawable in the array. */
@JvmSynthetic
internal fun Array<Drawable?>.isExistHorizontalDrawable(): Boolean {
  return this[0] != null || this[2] != null
}

/** returns intrinsic height size of a drawable array. */
@JvmSynthetic
internal fun Array<Drawable?>.getIntrinsicHeight(): Int {
  return this[0].getHeight().coerceAtLeast(this[2].getHeight())
}

/** returns intrinsic height size of a drawable. */
@JvmSynthetic
internal fun Drawable?.getHeight(): Int {
  return this?.intrinsicHeight ?: 0
}
