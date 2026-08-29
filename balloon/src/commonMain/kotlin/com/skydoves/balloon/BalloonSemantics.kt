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

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * Marks a subtree as balloon content, ported from `BalloonSemantics` in the Android-only
 * `balloon-compose`.
 *
 * The key is deliberately unmergeable: balloon content lives in its own window, so a
 * screen reader must not fold it into a clickable ancestor the way it would fold an
 * ordinary child. Merging is a programming error — a balloon nested under a
 * clickable/focusable node — and throws rather than silently producing a broken
 * accessibility tree.
 */
internal fun SemanticsPropertyReceiver.balloon() {
  this[BalloonSemanticsProperties.IsBalloon] = Unit
}

private object BalloonSemanticsProperties {
  val IsBalloon: SemanticsPropertyKey<Unit> = SemanticsPropertyKey(
    name = "IsBalloon",
    mergePolicy = { _, _ ->
      throw IllegalStateException(
        "merge function called on unmergeable property IsBalloon. " +
          "A balloon should not be a child of a clickable/focusable node.",
      )
    },
  )
}
