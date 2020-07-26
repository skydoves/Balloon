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
import androidx.annotation.DrawableRes
import androidx.annotation.Px

@DslMarker
annotation class IconFormDsl

/** creates an instance of [IconForm] from [IconForm.Builder] using kotlin dsl. */
inline fun iconForm(context: Context, block: IconForm.Builder.() -> Unit): IconForm =
  IconForm.Builder(context).apply(block).build()

/**
 * IconForm is an attribute class which has TextView attributes
 * for customizing popup icons easily.
 */
class IconForm(builder: Builder) {
  val drawable = builder.drawable
  @DrawableRes var drawableRes = builder.drawableRes
  val iconGravity = builder.iconGravity
  @Px val iconSize = builder.iconSize
  @Px val iconSpace = builder.iconSpace
  @ColorInt val iconColor = builder.iconColor

  /** Builder class for [IconForm]. */
  @IconFormDsl
  class Builder(val context: Context) {
    @JvmField
    var drawable: Drawable? = null
    @JvmField @DrawableRes
    var drawableRes: Int? = null
    @JvmField
    var iconGravity = IconGravity.LEFT
    @JvmField @Px
    var iconSize: Int = context.dp2Px(28)
    @JvmField @Px
    var iconSpace: Int = context.dp2Px(8)
    @JvmField @ColorInt
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
    /** sets the size of the icon. */
    fun setIconSize(@Px value: Int): Builder = apply { this.iconSize = value }

    /** sets the space between the icon and the main text content. */
    fun setIconSpace(@Px value: Int): Builder = apply { this.iconSpace = value }

    /** sets the color of the icon. */
    fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon using resource */
    fun setIconColorResource(@ColorInt value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    fun build() = IconForm(this)
  }
}
