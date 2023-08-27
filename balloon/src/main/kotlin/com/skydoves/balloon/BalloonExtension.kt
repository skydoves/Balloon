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

@file:Suppress("unused", "RedundantVisibilityModifier")

package com.skydoves.balloon

import android.view.View
import androidx.annotation.MainThread

/** shows the balloon on the center of an anchor view. */
@JvmOverloads
@JvmSynthetic
public fun View.showAtCenter(
  balloon: Balloon,
  xOff: Int = 0,
  yOff: Int = 0,
  centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
) {
  balloon { balloon.showAtCenter(this, xOff, yOff, centerAlign) }
}

/** shows the balloon on the center of an anchor view. */
public suspend fun View.awaitAtCenter(
  balloon: Balloon,
  xOff: Int = 0,
  yOff: Int = 0,
  centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
) {
  balloon.awaitAtCenter(this, xOff, yOff, centerAlign)
}

/** shows the balloon on an anchor view as drop down with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
public fun View.showAsDropDown(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAsDropDown(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as drop down with x-off and y-off. */
public suspend fun View.awaitAsDropDown(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon.awaitAsDropDown(this, xOff, yOff)
}

@JvmOverloads
@JvmSynthetic
/** shows the balloon on an anchor view as the top alignment with x-off and y-off. */
public fun View.showAlignTop(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignTop(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the top alignment with x-off and y-off. */
public suspend fun View.awaitAlignTop(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon.awaitAlignTop(this, xOff, yOff)
}

/** shows the balloon on an anchor view as the bottom alignment with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
public fun View.showAlignBottom(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignBottom(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the bottom alignment with x-off and y-off. */
public suspend fun View.awaitAlignBottom(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon.awaitAlignBottom(this, xOff, yOff)
}

/** shows the balloon on an anchor view as the right alignment with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
public fun View.showAlignRight(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignRight(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the right alignment with x-off and y-off. */
public suspend fun View.awaitAlignRight(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon.awaitAlignRight(this, xOff, yOff)
}

/** shows the balloon on an anchor view as the left alignment with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
public fun View.showAlignLeft(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignLeft(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the left alignment with x-off and y-off. */
public suspend fun View.awaitAlignLeft(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon.awaitAlignLeft(this, xOff, yOff)
}

/** shows the balloon on an anchor view depending on the [align] alignment with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
public fun View.showAlign(
  balloon: Balloon,
  align: BalloonAlign,
  subAnchorList: List<View> = listOf(),
  xOff: Int = 0,
  yOff: Int = 0,
) {
  balloon {
    balloon.showAlign(
      mainAnchor = this,
      subAnchorList = subAnchorList,
      align = align,
      xOff = xOff,
      yOff = yOff,
    )
  }
}

/** shows the balloon on an anchor view depending on the [align] alignment with x-off and y-off. */
public suspend fun View.awaitAlign(
  balloon: Balloon,
  align: BalloonAlign,
  subAnchorList: List<View> = listOf(),
  xOff: Int = 0,
  yOff: Int = 0,
) {
  balloon.awaitAlign(
    mainAnchor = this,
    subAnchorList = subAnchorList,
    align = align,
    xOff = xOff,
    yOff = yOff,
  )
}

@MainThread
@JvmSynthetic
internal inline fun View.balloon(crossinline block: () -> Unit) {
  post { block() }
}
