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

/** BalloonAlign decides where the balloon should be placed. */
public enum class BalloonAlign {
  START,
  END,
  TOP,
  BOTTOM;

  internal companion object {

    /** Return [BalloonAlign] depending on the [isRtlLayout].] */
    internal fun BalloonAlign.getRTLSupportAlign(isRtlLayout: Boolean): BalloonAlign {
      return if (!isRtlLayout) {
        this
      } else {
        when (this) {
          START -> END
          END -> START
          else -> this
        }
      }
    }
  }
}
