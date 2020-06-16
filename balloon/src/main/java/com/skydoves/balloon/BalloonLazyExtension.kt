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

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/** returns a [Lazy] delegate to access the [ComponentActivity]'s Balloon property. */
@MainThread
inline fun <reified T : Balloon.Factory> ComponentActivity.balloon(
  factory: KClass<T>
): Lazy<Balloon> {
  return ActivityBalloonLazy(this, this, factory)
}

/** returns a [Lazy] delegate to access the [Fragment]'s Balloon property. */
@MainThread
inline fun <reified T : Balloon.Factory> Fragment.balloon(
  factory: KClass<T>
): Lazy<Balloon?> {
  return FragmentBalloonLazy(this, viewLifecycleOwner, factory)
}
