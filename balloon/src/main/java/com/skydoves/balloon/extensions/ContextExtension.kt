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
import android.graphics.Point
import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat

/** gets display size as a point. */
internal fun Context.displaySize(): Point {
  return Point(
    resources.displayMetrics.widthPixels,
    resources.displayMetrics.heightPixels
  )
}

/** dp size to px size. */
internal fun Context.dp2Px(dp: Int): Int {
  val scale = resources.displayMetrics.density
  return (dp * scale).toInt()
}

/** dp size to px size. */
internal fun Context.dp2Px(dp: Float): Float {
  val scale = resources.displayMetrics.density
  return (dp * scale)
}

/** gets a dimension pixel size from dimension resource. */
internal fun Context.dimen(@DimenRes dimenRes: Int): Int {
  return resources.getDimensionPixelSize(dimenRes)
}

/** gets a color from the resource. */
internal fun Context.contextColor(resource: Int): Int {
  return ContextCompat.getColor(this, resource)
}

/** gets a drawable from the resource. */
internal fun Context.contextDrawable(resource: Int): Drawable? {
  return AppCompatResources.getDrawable(this, resource)
}
