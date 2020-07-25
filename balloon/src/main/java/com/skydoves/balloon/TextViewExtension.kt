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

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.skydoves.balloon.custom.VectorTextViewModel
import com.skydoves.balloon.extensions.resize
import com.skydoves.balloon.extensions.tint

/** applies text form attributes to a TextView instance. */
@Suppress("unused")
internal fun TextView.applyTextForm(textForm: TextForm) {
  text = when (textForm.textIsHtml) {
    true -> fromHtml(textForm.text.toString())
    false -> textForm.text
  }
  textSize = textForm.textSize
  gravity = textForm.textGravity
  setTextColor(textForm.textColor)
  textForm.textTypeface?.let { typeface = it } ?: setTypeface(typeface, textForm.textStyle)
}

private fun fromHtml(text: String): Spanned? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
  } else {
    Html.fromHtml(text)
  }
}

fun TextView.applyDrawable(vectorTextViewModel: VectorTextViewModel) {
  val height = vectorTextViewModel.iconSize
    ?: vectorTextViewModel.heightRes?.let { context.resources.getDimensionPixelSize(it) }
    ?: vectorTextViewModel.squareSizeRes?.let { context.resources.getDimensionPixelSize(it) }

  val width = vectorTextViewModel.iconSize
    ?: vectorTextViewModel.widthRes?.let { context.resources.getDimensionPixelSize(it) }
    ?: vectorTextViewModel.squareSizeRes?.let { context.resources.getDimensionPixelSize(it) }

  val drawableLeft: Drawable? =
    vectorTextViewModel.drawableLeft ?: vectorTextViewModel.drawableLeftRes
      ?.let {
        AppCompatResources.getDrawable(context, it)
      }?.tint(context, vectorTextViewModel.tintColorRes)?.resize(context, width, height)

  val drawableRight: Drawable? =
    vectorTextViewModel.drawableRight ?: vectorTextViewModel.drawableRightRes?.let {
      AppCompatResources.getDrawable(context, it)
    }?.tint(context, vectorTextViewModel.tintColorRes)?.resize(context, width, height)

  val drawableBottom: Drawable? =
    vectorTextViewModel.drawableBottom ?: vectorTextViewModel.drawableBottomRes?.let {
      AppCompatResources.getDrawable(context, it)
    }?.tint(context, vectorTextViewModel.tintColorRes)?.resize(context, width, height)

  val drawableTop: Drawable? =
    vectorTextViewModel.drawableTop ?: vectorTextViewModel.drawableTopRes?.let {
      AppCompatResources.getDrawable(context, it)
    }?.tint(context, vectorTextViewModel.tintColorRes)?.resize(context, width, height)

  setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)

  vectorTextViewModel.compoundDrawablePadding?.let { compoundDrawablePadding = it }
    ?: vectorTextViewModel.compoundDrawablePaddingRes?.let {
      compoundDrawablePadding = context.resources.getDimensionPixelSize(it)
    }
}
