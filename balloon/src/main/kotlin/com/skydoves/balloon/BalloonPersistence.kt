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
import androidx.core.content.edit

/** BalloonPreferenceManager helps to persist showing counts. */
public class BalloonPersistence private constructor() {

  /** should show or not the popup. */
  public fun shouldShowUp(name: String, counts: Int): Boolean {
    return getPersistedCounts(name) < counts
  }

  /** puts a incremented show-up counts to the preference. */
  public fun putIncrementedCounts(name: String) {
    putCounts(name, getPersistedCounts(name) + 1)
  }

  /** clears all persisted preferences. */
  public fun clearAllPreferences() {
    sharedPreferenceManager.edit { clear() }
  }

  /** gets show-up counts from the preference. */
  private fun getPersistedCounts(name: String): Int {
    return sharedPreferenceManager.getInt(getPersistName(name), 0)
  }

  /** puts show-up counts to the preference. */
  private fun putCounts(name: String, counts: Int) {
    sharedPreferenceManager.edit { putInt(getPersistName(name), counts) }
  }

  public companion object {
    @Volatile
    private var instance: BalloonPersistence? = null
    private lateinit var sharedPreferenceManager: SharedPreferences
    private const val SHOWED_UP = "SHOWED_UP"

    @JvmStatic
    public fun getInstance(context: Context): BalloonPersistence =
      instance ?: synchronized(this) {
        instance ?: BalloonPersistence().also {
          instance = it
          sharedPreferenceManager =
            context.getSharedPreferences("com.skydoves.balloon", Context.MODE_PRIVATE)
        }
      }

    @JvmStatic
    public fun getPersistName(name: String): String = SHOWED_UP + name
  }
}
