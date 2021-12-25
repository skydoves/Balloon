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

@file:Suppress("unused", "RedundantVisibilityModifier")

package com.skydoves.balloon

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.MovementMethod
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import com.skydoves.balloon.annotations.Sp
import com.skydoves.balloon.extensions.contextColor
import com.skydoves.balloon.extensions.dimen
import com.skydoves.balloon.extensions.px2Sp

@DslMarker
internal annotation class TextFormDsl

/** creates an instance of [TextForm] from [TextForm.Builder] using kotlin dsl. */
@MainThread
@TextFormDsl
@JvmSynthetic
public inline fun textForm(
  context: Context,
  crossinline block: TextForm.Builder.() -> Unit
): TextForm =
  TextForm.Builder(context).apply(block).build()

/**
 * TextFrom is an attribute class what has some attributes about TextView
 * for customizing popup texts easily.
 */
public class TextForm private constructor(
  builder: Builder
) {

  public val text: CharSequence = builder.text

  @Sp
  public val textSize: Float = builder.textSize

  @ColorInt
  public val textColor: Int = builder.textColor

  public val textIsHtml: Boolean = builder.textIsHtml

  public val movementMethod: MovementMethod? = builder.movementMethod

  public val textStyle: Int = builder.textTypeface

  public val textTypeface: Typeface? = builder.textTypefaceObject

  public val textGravity: Int = builder.textGravity

  /** Builder class for [TextForm]. */
  @TextFormDsl
  public class Builder(public val context: Context) {
    @set:JvmSynthetic
    public var text: CharSequence = ""

    @Sp
    @set:JvmSynthetic
    public var textSize: Float = 12f

    @ColorInt
    @set:JvmSynthetic
    public var textColor: Int = Color.WHITE

    @set:JvmSynthetic
    public var textIsHtml: Boolean = false

    @set:JvmSynthetic
    public var movementMethod: MovementMethod? = null

    @set:JvmSynthetic
    public var textTypeface: Int = Typeface.NORMAL

    @set:JvmSynthetic
    public var textTypefaceObject: Typeface? = null

    @set:JvmSynthetic
    public var textGravity: Int = Gravity.CENTER

    /** sets the content text of the form. */
    public fun setText(value: CharSequence): Builder = apply { this.text = value }

    /** sets the content text of the form using string resource. */
    public fun setTextResource(@StringRes value: Int): Builder = apply {
      this.text = context.getString(value)
    }

    /** sets the size of the text. */
    public fun setTextSize(@Sp value: Float): Builder = apply { this.textSize = value }

    /** sets the size of the main text content using dimension resource. */
    public fun setTextSizeResource(@DimenRes value: Int): Builder = apply {
      this.textSize = context.px2Sp(context.dimen(value))
    }

    /** sets the color of the text. */
    public fun setTextColor(@ColorInt value: Int): Builder = apply { this.textColor = value }

    /** sets whether the text will be parsed as HTML (using Html.fromHtml(..)) */
    public fun setTextIsHtml(value: Boolean): Builder = apply { this.textIsHtml = value }

    /** sets the movement method for TextView. */
    public fun setMovementMethod(value: MovementMethod): Builder = apply {
      this.movementMethod = value
    }

    /** sets the color of the text using resource. */
    public fun setTextColorResource(@ColorRes value: Int): Builder = apply {
      this.textColor = context.contextColor(value)
    }

    /** sets the [Typeface] of the text. */
    public fun setTextTypeface(value: Int): Builder = apply { this.textTypeface = value }

    /** sets the [Typeface] of the text. */
    public fun setTextTypeface(value: Typeface?): Builder =
      apply { this.textTypefaceObject = value }

    /** sets gravity of the text. */
    public fun setTextGravity(value: Int): Builder = apply {
      this.textGravity = value
    }

    public fun build(): TextForm = TextForm(this)
  }
}
