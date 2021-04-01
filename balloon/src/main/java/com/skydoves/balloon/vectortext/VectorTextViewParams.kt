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

package com.skydoves.balloon.vectortext

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.Px

/** VectorTextViewParams is a collection of [VectorTextView]'s parameters. */
data class VectorTextViewParams(
  var drawableStartRes: Int? = null,
  var drawableEndRes: Int? = null,
  var drawableBottomRes: Int? = null,
  var drawableTopRes: Int? = null,
  var drawableStart: Drawable? = null,
  var drawableEnd: Drawable? = null,
  var drawableBottom: Drawable? = null,
  var drawableTop: Drawable? = null,
  var isRtlLayout: Boolean = false,
  @Px val compoundDrawablePadding: Int? = null,
  @Px val iconWidth: Int? = null,
  @Px val iconHeight: Int? = null,
  @DimenRes var compoundDrawablePaddingRes: Int? = null,
  @ColorInt var tintColor: Int? = null,
  @DimenRes var widthRes: Int? = null,
  @DimenRes var heightRes: Int? = null,
  @DimenRes var squareSizeRes: Int? = null
)
