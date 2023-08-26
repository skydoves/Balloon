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
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Px
import androidx.annotation.StringRes
import com.skydoves.balloon.extensions.Empty
import com.skydoves.balloon.extensions.contextColor
import com.skydoves.balloon.extensions.dp

@DslMarker
internal annotation class IconFormDsl

/** creates an instance of [IconForm] from [IconForm.Builder] using kotlin dsl. */
@MainThread
@IconFormDsl
@JvmSynthetic
public inline fun iconForm(
  context: Context,
  crossinline block: IconForm.Builder.() -> Unit,
): IconForm =
  IconForm.Builder(context).apply(block).build()

/**
 * IconForm is an attribute class which has TextView attributes
 * for customizing popup icons easily.
 */
public class IconForm private constructor(
  builder: Builder,
) {

  public val drawable: Drawable? = builder.drawable

  @DrawableRes
  public var drawableRes: Int? = builder.drawableRes

  public val iconGravity: IconGravity = builder.iconGravity

  @Px
  public val iconWidth: Int = builder.iconWidth

  @Px
  public val iconHeight: Int = builder.iconHeight

  @Px
  public val iconSpace: Int = builder.iconSpace

  @ColorInt
  public val iconColor: Int = builder.iconColor

  public val iconContentDescription: CharSequence = builder.iconContentDescription

  /** Builder class for [IconForm]. */
  @IconFormDsl
  public class Builder(public val context: Context) {
    @set:JvmSynthetic
    public var drawable: Drawable? = null

    @DrawableRes
    @set:JvmSynthetic
    public var drawableRes: Int? = null

    @set:JvmSynthetic
    public var iconGravity: IconGravity = IconGravity.START

    @Px
    @set:JvmSynthetic
    public var iconWidth: Int = 28.dp

    @Px
    @set:JvmSynthetic
    public var iconHeight: Int = 28.dp

    @Px
    @set:JvmSynthetic
    public var iconSpace: Int = 8.dp

    @ColorInt
    @set:JvmSynthetic
    public var iconColor: Int = Color.WHITE

    @set:JvmSynthetic
    public var iconContentDescription: CharSequence = String.Empty

    /** sets the [Drawable] of the icon. */
    public fun setDrawable(value: Drawable?): Builder = apply { this.drawable = value }

    /** sets the [Drawable] of the icon using resource. */
    public fun setDrawableResource(@DrawableRes value: Int): Builder = apply {
      this.drawableRes = value
    }

    /** sets gravity of the [Drawable] of the icon using resource. */
    public fun setDrawableGravity(value: IconGravity): Builder = apply {
      this.iconGravity = value
    }

    /** sets the width size of the icon. */
    public fun setIconWidth(@Px value: Int): Builder = apply {
      this.iconWidth = value
    }

    /** sets the height size of the icon. */
    public fun setIconHeight(@Px value: Int): Builder = apply {
      this.iconHeight = value
    }

    /** sets the size of the icon. */
    public fun setIconSize(@Px value: Int): Builder = apply {
      setIconWidth(value)
      setIconHeight(value)
    }

    /** sets the content description accessibility. */
    public fun setIconContentDescription(value: CharSequence): Builder = apply {
      this.iconContentDescription = value
    }

    /** sets the content description accessibility using resource. */
    public fun setIconContentDescriptionResource(@StringRes value: Int): Builder = apply {
      this.iconContentDescription = context.getString(value)
    }

    /** sets the space between the icon and the main text content. */
    public fun setIconSpace(@Px value: Int): Builder = apply { this.iconSpace = value }

    /** sets the color of the icon. */
    public fun setIconColor(@ColorInt value: Int): Builder = apply { this.iconColor = value }

    /** sets the color of the icon using resource */
    public fun setIconColorResource(@ColorRes value: Int): Builder = apply {
      this.iconColor = context.contextColor(value)
    }

    public fun build(): IconForm = IconForm(this)
  }
}
