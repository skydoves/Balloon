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
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px

/** VectorTextViewParams is a collection of [VectorTextView]'s parameters.  */
data class VectorTextViewParams(
  var drawableLeftRes: Int? = null,
  var drawableRightRes: Int? = null,
  var drawableBottomRes: Int? = null,
  var drawableTopRes: Int? = null,
  var drawableLeft: Drawable? = null,
  var drawableRight: Drawable? = null,
  var drawableBottom: Drawable? = null,
  var drawableTop: Drawable? = null,
  @Px val compoundDrawablePadding: Int? = null,
  @Px val iconSize: Int? = null,
  @DimenRes var compoundDrawablePaddingRes: Int? = null,
  @ColorRes var tintColorRes: Int? = null,
  @DimenRes var widthRes: Int? = null,
  @DimenRes var heightRes: Int? = null,
  @DimenRes var squareSizeRes: Int? = null
)
