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
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Px
import com.skydoves.balloon.extensions.contextColor
import com.skydoves.balloon.extensions.dp

@DslMarker
internal annotation class IconFormDsl

/** creates an instance of [IconForm] from [IconForm.Builder] using kotlin dsl. */
@MainThread
@IconFormDsl
@JvmSynthetic
inline fun iconForm(context: Context, crossinline block: IconForm.Builder.() -> Unit): IconForm =
  IconForm.Builder(context).apply(block).build()

/**
 * IconForm is an attribute class which has TextView attributes
 * for customizing popup icons easily.
 */
class IconForm private constructor(
  builder: Builder
) {

  val drawable = builder.drawable

  @DrawableRes
  var drawableRes = builder.drawableRes

  val iconGravity = builder.iconGravity

  @Px
  val iconWidth = builder.iconWidth

  @Px
  val iconHeight = builder.iconHeight

  @Px
  val iconSpace = builder.iconSpace

  @ColorInt
  val iconColor = builder.iconColor

  /** Builder class for [IconForm]. */
  @IconFormDsl
  class Builder(val context: Context) {
    @set:JvmSynthetic
    var drawable: Drawable? = null

    @DrawableRes
    @set:JvmSynthetic
    var drawableRes: Int? = null

    @set:JvmSynthetic
    var iconGravity = IconGravity.START

    @Px
    @set:JvmSynthetic
    var iconWidth: Int = 28.dp

    @Px
    @set:JvmSynthetic
    var iconHeight: Int = 28.dp

    @Px
    @set:JvmSynthetic
    var iconSpace: Int = 8.dp

    @ColorInt
    @set:JvmSynthetic
    var iconColor: Int = Color.WHITE

    /** sets the [Drawable] of the icon. */
    fun setDrawable(value: Drawable?): Builder = apply { this.drawable = value }

    /** sets the [Drawable] of the icon using resource. */
    fun setDrawableResource(@DrawableRes value: Int): Builder = apply {
      this.drawableRes = value
    }

    /** sets gravity of the [Drawable] of the icon using resource. */
    fun setDrawableGravity(value: IconGravity): Builder = apply {
      this.iconGravity = value
    }

    /** sets the width size of the icon. */
    fun setIconWidth(@Px value: Int): Builder = apply {
      this.iconWidth = value
    }

    /** sets the height size of the icon. */
    fun setIconHeight(@Px value: Int): Builder = apply {
      this.iconHeight = value
    }

    /** sets the size of the icon. */
    fun setIconSize(@Px value: Int): Builder = apply {
      setIconWidth(value)
      setIconHeight(value)
    }

    /** sets the space between the icon and the main text content. */
    fun setIconSpace(@Px value: Int): Builder = apply { this.iconSpace = value }

    /** sets the color of the icon. */
    fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon using resource */
    fun setIconColorResource(@ColorRes value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    fun build() = IconForm(this)
  }
}
