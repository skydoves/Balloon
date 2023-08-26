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

package com.skydoves.balloon.compose

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

internal fun SemanticsPropertyReceiver.balloon() {
  this[IsBalloon] = Unit
}

internal val IsBalloon = SemanticsPropertyKey<Unit>(
  name = "IsBalloon",
  mergePolicy = { _, _ ->
    throw IllegalStateException(
      "merge function called on unmergeable property IsBalloon. " +
        "A dialog should not be a child of a clickable/focusable node.",
    )
  },
)
