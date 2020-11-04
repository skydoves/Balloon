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

import androidx.fragment.app.Fragment
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * An implementation of [Lazy] used by [Fragment]
 *
 * tied to the given fragment's lifecycle, [clazz].
 */
class FragmentBalloonLazy<out T : Balloon.Factory>(
  private val fragment: Fragment,
  private val clazz: KClass<T>
) : Lazy<Balloon?>, Serializable {

  private var cached: Balloon? = null

  override val value: Balloon?
    get() {
      var instance = cached
      if (instance === null && fragment.context !== null) {
        val factory = clazz::java.get().newInstance()
        val lifecycle = if (fragment.view !== null) {
          fragment.viewLifecycleOwner
        } else {
          fragment
        }
        instance = factory.create(fragment.requireActivity(), lifecycle)
        cached = instance
      }

      return instance
    }

  override fun isInitialized() = cached !== null

  override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}
