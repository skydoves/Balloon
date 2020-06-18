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

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.TextView

/** applies text form attributes to a TextView instance. */
@Suppress("unused")
internal fun TextView.applyTextForm(textForm: TextForm) {
  text = when (textForm.textIsHtml) {
    true -> fromHtml(textForm.text)
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
