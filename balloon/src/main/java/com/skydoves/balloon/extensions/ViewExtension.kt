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
import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.MainThread
import kotlin.math.max

/** sets visibility of the view based on the given parameter. */
@JvmSynthetic
internal fun View.visible(shouldVisible: Boolean) {
  visibility = if (shouldVisible) {
    View.VISIBLE
  } else {
    View.GONE
  }
}

/** computes and returns the coordinates of this view on the screen. */
@JvmSynthetic
internal fun View.getViewPointOnScreen(): Point {
  val location: IntArray = intArrayOf(0, 0)
  getLocationOnScreen(location)
  return Point(location[0], location[1])
}

/** returns the status bar height if the anchor is on the Activity. */
@JvmSynthetic
internal fun View.getStatusBarHeight(isStatusBarVisible: Boolean): Int {
  val rectangle = Rect()
  val context = context
  return if (context is Activity && isStatusBarVisible) {
    context.window.decorView.getWindowVisibleDisplayFrame(rectangle)
    rectangle.top
  } else 0
}

/** shows circular revealed animation to a view. */
@MainThread
@JvmSynthetic
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal fun View.circularRevealed(circularDuration: Long) {
  visibility = View.INVISIBLE
  runOnAfterSDK21 {
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
@JvmSynthetic
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal inline fun View.circularUnRevealed(
  circularDuration: Long,
  crossinline doAfterFinish: () -> Unit
) {
  runOnAfterSDK21 {
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
