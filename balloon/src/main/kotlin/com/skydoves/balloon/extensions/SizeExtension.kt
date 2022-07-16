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

import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue
import kotlin.math.roundToInt

/** returns integer dimensional value from the integer px value. */
internal val Int.dp: Int
  @JvmSynthetic inline get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics
  ).roundToInt()

/** returns float dimensional value from the float px value. */
internal val Float.dp: Float
  @JvmSynthetic inline get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    Resources.getSystem().displayMetrics
  )

/** gets display size as a point. */
internal val displaySize: Point
  @JvmSynthetic inline get() = Point(
    Resources.getSystem().displayMetrics.widthPixels,
    Resources.getSystem().displayMetrics.heightPixels
  )
