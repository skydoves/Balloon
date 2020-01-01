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
import android.content.SharedPreferences

/** BalloonPreferenceManager helps to persist showing times. */
internal class BalloonPersistence {

  /** should show or not the popup. */
  fun shouldShowUP(name: String, times: Int): Boolean {
    return getTimes(name) < times
  }

  /** gets show-up times from the preference. */
  fun getTimes(name: String): Int {
    return sharedPreferenceManager.getInt(getPersistName(name), 0)
  }

  /** puts show-up times to the preference. */
  fun putTimes(name: String, times: Int) {
    sharedPreferenceManager.edit().putInt(getPersistName(name), times).apply()
  }

  /** puts a incremented show-up times to the preference. */
  fun putIncrementedTimes(name: String) {
    putTimes(name, getTimes(name) + 1)
  }

  /** removes a showed times history from the preference. */
  fun removePersistedData(name: String) =
    sharedPreferenceManager.edit().remove(SHOWED_UP + name).apply()

  /** clears all of [Balloon] showed times history on the application. */
  fun clearAllPersistedData() = sharedPreferenceManager.edit().clear().apply()

  companion object {
    @Volatile
    private var instance: BalloonPersistence? = null
    private lateinit var sharedPreferenceManager: SharedPreferences
    private const val SHOWED_UP = "SHOWED_UP"

    @JvmStatic
    fun getInstance(context: Context): BalloonPersistence =
      instance ?: synchronized(this) {
        instance ?: BalloonPersistence().also {
          instance = it
          sharedPreferenceManager =
            context.getSharedPreferences("com.skydoves.balloon", Context.MODE_PRIVATE)
        }
      }

    fun getPersistName(name: String) = SHOWED_UP + name
  }
}
