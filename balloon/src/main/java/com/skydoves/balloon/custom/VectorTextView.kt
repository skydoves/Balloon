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

package com.skydoves.balloon.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.skydoves.balloon.R
import com.skydoves.balloon.applyDrawable

open class VectorTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

  var drawableTextViewParams: VectorTextViewParams? = null
    set(value) {
      field = value?.also { applyDrawable(it) }
    }

  init {
    initAttrs(context, attrs)
  }

  private fun initAttrs(context: Context, attrs: AttributeSet?) {
    if (attrs != null) {
      val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.VectorTextView)
      drawableTextViewParams = VectorTextViewParams(
        drawableLeftRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableLeft, 0)
          .takeIf { it != 0 },
        drawableRightRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableRight, 0)
          .takeIf { it != 0 },
        drawableBottomRes = attributeArray.getResourceId(
          R.styleable.VectorTextView_drawableBottom,
          0
        ).takeIf { it != 0 },
        drawableTopRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableTop, 0)
          .takeIf { it != 0 },
        compoundDrawablePaddingRes = attributeArray.getResourceId(
          R.styleable.VectorTextView_drawablePadding,
          0
        ).takeIf { it != 0 },
        tintColorRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableTintColor, 0)
          .takeIf { it != 0 },
        widthRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableWidth, 0)
          .takeIf { it != 0 },
        heightRes = attributeArray.getResourceId(R.styleable.VectorTextView_drawableHeight, 0)
          .takeIf { it != 0 },
        squareSizeRes = attributeArray.getResourceId(
          R.styleable.VectorTextView_drawableSquareSize,
          0
        ).takeIf { it != 0 }
      )
      attributeArray.recycle()
    }
  }
}
