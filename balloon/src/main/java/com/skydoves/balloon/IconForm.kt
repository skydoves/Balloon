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
import android.graphics.drawable.Drawable

@DslMarker
annotation class IconFormDsl

/** creates an instance of [IconForm] from [IconForm.Builder] using kotlin dsl. */
fun iconForm(context: Context, block: IconForm.Builder.() -> Unit): IconForm =
  IconForm.Builder(context).apply(block).build()

/**
 * IconForm is an attribute class which has TextView attributes
 * for customizing popup texts easily.
 */
class IconForm(builder: Builder) {

  val drawable = builder.drawable
  val iconSize = builder.iconSize
  val iconSpace = builder.iconSpace
  val iconColor = builder.iconColor

  /** Builder class for [IconForm]. */
  @IconFormDsl
  class Builder(context: Context) {
    @JvmField
    var drawable: Drawable? = null
    @JvmField
    var iconSize: Int = context.dp2Px(28)
    @JvmField
    var iconSpace: Int = context.dp2Px(8)
    @JvmField
    var iconColor: Int = -3

    fun setDrawable(value: Drawable?): Builder = apply { this.drawable = value }
    fun setIconSize(value: Int): Builder = apply { this.iconSize = value }
    fun setIconSpace(value: Int): Builder = apply { this.iconSpace = value }
    fun setIconColor(value: Int): Builder = apply { this.iconColor = value }

    fun build(): IconForm {
      return IconForm(this)
    }
  }
}
