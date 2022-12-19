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
import com.skydoves.balloon.extensions.Empty

/** VectorTextViewParams is a collection of [VectorTextView]'s parameters. */
public data class VectorTextViewParams @JvmOverloads constructor(
  public var drawableStartRes: Int? = null,
  public var drawableEndRes: Int? = null,
  public var drawableBottomRes: Int? = null,
  public var drawableTopRes: Int? = null,
  public var drawableStart: Drawable? = null,
  public var drawableEnd: Drawable? = null,
  public var drawableBottom: Drawable? = null,
  public var drawableTop: Drawable? = null,
  public var isRtlLayout: Boolean = false,
  public var contentDescription: CharSequence = String.Empty,
  @Px public val compoundDrawablePadding: Int? = null,
  @Px public val iconWidth: Int? = null,
  @Px public val iconHeight: Int? = null,
  @DimenRes public var compoundDrawablePaddingRes: Int? = null,
  @ColorInt public var tintColor: Int? = null,
  @DimenRes public var widthRes: Int? = null,
  @DimenRes public var heightRes: Int? = null,
  @DimenRes public var squareSizeRes: Int? = null
)
