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

package com.skydoves.balloon.internals

import android.view.View
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * An extension for properties on View classes to initialize with [ViewPropertyDelegate].
 *
 * @param defaultValue A default value for this property.
 *
 * @return A [ViewPropertyDelegate] which is readable and writable property.
 */
@JvmSynthetic
internal fun <T : Any?> View.viewProperty(defaultValue: T): ViewPropertyDelegate<T> {
  return ViewPropertyDelegate(defaultValue) {
    invalidate()
  }
}

/**
 * A delegate class to invalidate View class if the [propertyValue] has been updated by a new value.
 *
 * @param defaultValue A default value for this property.
 * @param invalidator An executable lambda to invalidate [View].
 *
 * @return A readable and writable property.
 */
internal class ViewPropertyDelegate<T : Any?>(
  defaultValue: T,
  private val invalidator: () -> Unit
) : ReadWriteProperty<Any?, T> {

  private var propertyValue: T = defaultValue

  override fun getValue(thisRef: Any?, property: KProperty<*>): T {
    return propertyValue
  }

  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    if (propertyValue != value) {
      propertyValue = value
      invalidator()
    }
  }
}
