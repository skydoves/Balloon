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

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KClass

/**
 * An implementation of [Lazy] used by [ComponentActivity] and [Fragment]
 *
 * tied to the given [lifecycleOwner], [clazz].
 */
class BalloonLazy<out T : Balloon.Factory>(
  private val context: Context,
  private val lifecycleOwner: LifecycleOwner,
  private val clazz: KClass<T>
) : Lazy<Balloon> {

  private var cached: Balloon? = null

  override val value: Balloon
    get() {
      var instance = cached
      if (instance == null) {
        val factory = clazz::java.get().newInstance()
        instance = factory.create(context, lifecycleOwner)
        cached = instance
      }

      return instance
    }

  override fun isInitialized() = cached != null
}
