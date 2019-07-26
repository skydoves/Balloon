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

import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import kotlin.math.max

/** makes visible or invisible a View align the value parameter. */
internal fun View.visible(value: Boolean) {
  if (value) {
    this.visibility = View.VISIBLE
  } else {
    this.visibility = View.GONE
  }
}

/** shows circular revealed animation to a view. */
internal fun View.circularRevealed() {
  this.addOnLayoutChangeListener(
    object : View.OnLayoutChangeListener {
      override fun onLayoutChange(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
      ) {
        view.removeOnLayoutChangeListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          val animator = ViewAnimationUtils.createCircularReveal(
            view,
            (view.left + view.right) / 2,
            (view.top + view.bottom) / 2,
            0f,
            max(view.width, view.height).toFloat())
          animator.duration = 500
          animator.start()
        }
      }
    })
}
