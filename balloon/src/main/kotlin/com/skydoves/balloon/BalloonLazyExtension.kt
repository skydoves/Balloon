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
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.skydoves.balloon.internals.ActivityBalloonLazy
import com.skydoves.balloon.internals.FragmentBalloonLazy
import com.skydoves.balloon.internals.ViewBalloonLazy

/**
 * Returns a [Lazy] delegate to access the [ComponentActivity]'s Balloon property.
 * The balloon property will be initialized lazily.
 *
 * @see [Lazy Initialization](https://github.com/skydoves/Balloon#lazy-initialization)
 */
@MainThread
@JvmSynthetic
@BalloonInlineDsl
public inline fun <reified T : Balloon.Factory> ComponentActivity.balloon(): Lazy<Balloon> {
  return ActivityBalloonLazy(context = this, lifecycleOwner = this, factory = T::class)
}

/**
 * Returns a [Lazy] delegate to access the [Fragment]'s Balloon property.
 * The balloon property will be initialized lazily.
 *
 * @see [Lazy Initialization](https://github.com/skydoves/Balloon#lazy-initialization)
 */
@MainThread
@JvmSynthetic
@BalloonInlineDsl
public inline fun <reified T : Balloon.Factory> Fragment.balloon(): Lazy<Balloon> {
  return FragmentBalloonLazy(fragment = this, factory = T::class)
}

/**
 * Returns a [Lazy] delegate to access the custom [View]'s Balloon property.
 * The balloon property will be initialized lazily.
 *
 * @see [Lazy Initialization](https://github.com/skydoves/Balloon#lazy-initialization)
 */
@MainThread
@JvmSynthetic
@BalloonInlineDsl
public inline fun <reified T : Balloon.Factory> View.balloon(): Lazy<Balloon> {
  return ViewBalloonLazy(this, T::class)
}

/**
 * Returns a [Lazy] delegate to access the custom [ViewBinding]'s Balloon property.
 * The balloon property will be initialized lazily.
 *
 * @see [Lazy Initialization](https://github.com/skydoves/Balloon#lazy-initialization)
 */
@MainThread
@JvmSynthetic
@BalloonInlineDsl
public inline fun <reified T : Balloon.Factory> ViewBinding.balloon(): Lazy<Balloon> {
  return ViewBalloonLazy(root, T::class)
}
