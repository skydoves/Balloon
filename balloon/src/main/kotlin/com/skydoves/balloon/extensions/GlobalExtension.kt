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

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Runs a [block] lambda when the device's SDK level is 21 or higher.
 *
 * @param block A lambda that should be run when the device's SDK level is 21 or higher.
 */
@JvmSynthetic
@PublishedApi
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.LOLLIPOP, lambda = 0)
internal inline fun runOnAfterSDK21(block: () -> Unit) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    block()
  }
}

/**
 * Runs a [block] lambda when the device's SDK level is 22 or higher.
 *
 * @param block A lambda that should be run when the device's SDK level is 22 or higher.
 */
@JvmSynthetic
@PublishedApi
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.LOLLIPOP_MR1, lambda = 0)
internal inline fun runOnAfterSDK22(block: () -> Unit) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
    block()
  }
}

/**
 * Checks if the current device's API level is higher than 23 (M).
 */
@JvmSynthetic
internal fun isAPILevelHigherThan23(): Boolean {
  return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}
