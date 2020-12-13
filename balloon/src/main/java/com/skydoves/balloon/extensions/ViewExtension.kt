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

package com.skydoves.balloon.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.MainThread
import kotlin.math.max

/** sets visibility of the view based on the given parameter. */
internal fun View.visible(shouldVisible: Boolean) {
  visibility = if (shouldVisible) {
    View.VISIBLE
  } else {
    View.GONE
  }
}

/** shows circular revealed animation to a view. */
@MainThread
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal fun View.circularRevealed(circularDuration: Long) {
  visibility = View.INVISIBLE
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    post {
      if (isAttachedToWindow) {
        visibility = View.VISIBLE
        ViewAnimationUtils.createCircularReveal(
          this,
          (left + right) / 2,
          (top + bottom) / 2,
          0f,
          max(width, height).toFloat()
        ).apply {
          duration = circularDuration
          start()
        }
      }
    }
  }
}

/** shows circular unrevealed animation to a view. */
@MainThread
@PublishedApi
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal inline fun View.circularUnRevealed(
  circularDuration: Long,
  crossinline doAfterFinish: () -> Unit
) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    post {
      if (isAttachedToWindow) {
        ViewAnimationUtils.createCircularReveal(
          this,
          (left + right) / 2,
          (top + bottom) / 2,
          max(width, height).toFloat(),
          0f
        ).apply {
          duration = circularDuration
          start()
        }.addListener(
          object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
              super.onAnimationEnd(animation)
              doAfterFinish()
            }
          }
        )
      }
    }
  }
}
