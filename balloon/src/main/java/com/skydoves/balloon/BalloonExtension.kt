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

@file:Suppress("unused")

package com.skydoves.balloon

import android.view.View
import androidx.annotation.MainThread

/** shows the balloon on the center of an anchor view. */
@Deprecated(
  message = "show() method will be deprecated since `1.3.8`. Use showAtCenter() instead.",
  replaceWith = ReplaceWith(
    "showAtCenter(anchor)",
    imports = ["com.skydoves.balloon.showAtCenter"]
  ),
)
@JvmSynthetic
fun View.showBalloon(balloon: Balloon) = showAtCenter(balloon)

/** shows the balloon on the center of an anchor view. */
@JvmOverloads
@JvmSynthetic
fun View.showAtCenter(
  balloon: Balloon,
  xOff: Int = 0,
  yOff: Int = 0,
  centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP
) {
  balloon { balloon.showAtCenter(this, xOff, yOff, centerAlign) }
}

/** shows the balloon on an anchor view with x-off and y-off. */
@Deprecated(
  message = "show() method will be deprecated since `1.3.8`. Use showAsDropDown() instead.",
  replaceWith = ReplaceWith(
    "showAsDropDown(anchor, xOff, yOff)",
    imports = ["com.skydoves.balloon.showAsDropDown"]
  ),
)
@JvmSynthetic
fun View.showBalloon(balloon: Balloon, xOff: Int, yOff: Int) =
  showAsDropDown(balloon, xOff, yOff)

/** shows the balloon on an anchor view as drop down with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
fun View.showAsDropDown(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAsDropDown(this, xOff, yOff) }
}

@JvmOverloads
@JvmSynthetic
/** shows the balloon on an anchor view as the top alignment with x-off and y-off. */
fun View.showAlignTop(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignTop(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the bottom alignment with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
fun View.showAlignBottom(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignBottom(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the right alignment with x-off and y-off. */
@JvmOverloads
@JvmSynthetic
fun View.showAlignRight(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignRight(this, xOff, yOff) }
}

/** shows the balloon on an anchor view as the left alignment with x-off and y-off.. */
@JvmOverloads
@JvmSynthetic
fun View.showAlignLeft(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
  balloon { balloon.showAlignLeft(this, xOff, yOff) }
}

@MainThread
@JvmSynthetic
internal inline fun View.balloon(crossinline block: () -> Unit) {
  post { block() }
}
