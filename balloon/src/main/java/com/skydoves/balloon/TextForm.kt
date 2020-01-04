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

@file:Suppress("unused")

package com.skydoves.balloon

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt

@DslMarker
annotation class TextFormDsl

/** creates an instance of [TextForm] from [TextForm.Builder] using kotlin dsl. */
inline fun textForm(context: Context, block: TextForm.Builder.() -> Unit): TextForm =
  TextForm.Builder(context).apply(block).build()

/**
 * TextFrom is an attribute class what has some attributes about TextView
 * for customizing popup texts easily.
 */
class TextForm(builder: Builder) {

  val text = builder.text
  val textSize = builder.textSize
  val textColor = builder.textColor
  val textStyle = builder.textTypeface
  val textTypeface = builder.textTypefaceObject

  /** Builder class for [TextForm]. */
  @TextFormDsl
  class Builder(val context: Context) {
    @JvmField
    var text: String = ""
    @JvmField
    var textSize: Float = 12f
    @JvmField
    @ColorInt
    var textColor = Color.WHITE
    @JvmField
    var textTypeface = Typeface.NORMAL
    @JvmField
    var textTypefaceObject: Typeface? = null

    /** sets the content text of the form. */
    fun setText(value: String): Builder = apply { this.text = value }

    /** sets the size of the text. */
    fun setTextSize(value: Float): Builder = apply { this.textSize = value }

    /** sets the color of the text. */
    fun setTextColor(@ColorInt value: Int): Builder = apply { this.textColor = value }

    /** sets the color of the text using resource. */
    fun setTextColorResource(value: Int): Builder = apply { this.textColor = context.contextColor(value) }

    /** sets the [Typeface] of the text. */
    fun setTextTypeface(value: Int): Builder = apply { this.textTypeface = value }

    /** sets the [Typeface] of the text. */
    fun setTextTypeface(value: Typeface?): Builder = apply { this.textTypefaceObject = value }

    fun build() = TextForm(this)
  }
}
