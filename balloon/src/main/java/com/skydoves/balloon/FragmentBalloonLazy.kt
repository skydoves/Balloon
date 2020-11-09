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
 * An implementation of [Lazy] for creating an instance of the [Balloon] in Fragments.
 * Tied to the given fragment's lifecycle and, [clazz].
 *
 * @param fragment An instance of the [Balloon] will be created in this Fragment lazily.
 * This will prevents memory leak: [Avoid Memory Leak](https://github.com/skydoves/balloon#avoid-memory-leak).
 * @param clazz A [Balloon.Factory] kotlin class for creating a new instance of the Balloon.
 */
@PublishedApi
internal class FragmentBalloonLazy<out T : Balloon.Factory>(
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
