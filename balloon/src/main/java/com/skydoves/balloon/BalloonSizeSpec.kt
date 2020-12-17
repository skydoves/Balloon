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

/**
 * A specification interface for determining sizes of the Balloon materials.
 * We can wrap some materials depending on their size of content using this interface.
 */
object BalloonSizeSpec {

  /** Wraps the material depending on the size of the content.  */
  const val WRAP: Int = NO_INT_VALUE
}
