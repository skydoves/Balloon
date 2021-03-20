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

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * An implementation of [Lazy] for creating an instance of the [Balloon] lazily in Activities.
 * Tied to the given Activity's lifecycleOwner, [factory].
 *
 * @param context A context for creating resources of the [Balloon] lazily.
 * This will prevents memory leak: [Avoid Memory Leak](https://github.com/skydoves/balloon#avoid-memory-leak).
 * @param factory A [Balloon.Factory] kotlin class for creating a new instance of the Balloon.
 */
@PublishedApi
internal class ViewBalloonLazy<out T : Balloon.Factory>(
  private val view: View,
  private val factory: KClass<T>
) : Lazy<Balloon>, Serializable {

  private var cached: Balloon? = null

  override val value: Balloon
    get() {
      var instance = cached
      if (instance === null) {
        val context = view.context
        if (context is LifecycleOwner) {
          val factory = factory::java.get().newInstance()
          instance = factory.create(context, context)
          cached = instance
        } else {
          try {
            val fragment = view.findFragment<Fragment>()
            if (fragment.context != null) {
              val factory = factory::java.get().newInstance()
              val lifecycle = if (fragment.view !== null) {
                fragment.viewLifecycleOwner
              } else {
                fragment
              }
              instance = factory.create(fragment.requireActivity(), lifecycle)
              cached = instance
            } else {
              throw IllegalArgumentException(
                "Balloon can not be initialized. The passed fragment's context is null."
              )
            }
          } catch (e: Exception) {
            throw IllegalArgumentException(
              "Balloon can not be initialized. The passed context is not an instance of the LifecycleOwner."
            )
          }
        }
      }

      return instance
    }

  override fun isInitialized(): Boolean = cached !== null

  override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}
