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

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.OnBalloonDismissListener
import com.skydoves.balloon.OnBalloonInitializedListener
import com.skydoves.balloon.OnBalloonOutsideTouchListener
import com.skydoves.balloon.OnBalloonOverlayClickListener
import com.skydoves.balloon.annotations.InternalBalloonApi
import com.skydoves.balloon.showAlign
import com.skydoves.balloon.showAlignBottom
import com.skydoves.balloon.showAlignTop
import com.skydoves.balloon.showAsDropDown
import com.skydoves.balloon.showAtCenter

/**
 * BalloonWindow is an interface that define all executable behaviors of the balloon's window.
 */
public interface BalloonWindow {

  public val anchorView: View

  public val balloon: Balloon

  /** Represents if the balloon should be displayed according to the internal persistence..*/
  public fun shouldShowUp(): Boolean

  /**
   * Shows the balloon over the anchor view (overlap) as the center aligns.
   * Even if you use with the [ArrowOrientationRules.ALIGN_ANCHOR], the alignment will not be guaranteed.
   * So if you use the function, use with [ArrowOrientationRules.ALIGN_FIXED] and fixed [ArrowOrientation].
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   * @param centerAlign A rule for deciding the alignment of the balloon.
   */
  public fun showAtCenter(
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  )

  /**
   * Coroutine alternative for [showAtCenter]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   * @param centerAlign A rule for deciding the alignment of the balloon.
   */
  public suspend fun awaitAtCenter(
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  )

  /**
   * Shows the balloon on an anchor view as the center alignment with x-off and y-off and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   * @param centerAlign A rule for deciding the align of the balloon.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAtCenter(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as drop down with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun showAsDropDown(xOff: Int = 0, yOff: Int = 0)

  /**
   * Coroutine alternative for [showAsDropDown]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public suspend fun awaitAsDropDown(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as drop down with x-off and y-off and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAsDropDown(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as the top alignment with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun showAlignTop(xOff: Int = 0, yOff: Int = 0)

  /**
   * Coroutine alternative for [showAlignTop]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public suspend fun awaitAlignTop(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as the top alignment with x-off and y-off and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAlignTop(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as the bottom alignment with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun showAlignBottom(xOff: Int = 0, yOff: Int = 0)

  /**
   * Coroutine alternative for [showAlignBottom]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public suspend fun awaitAlignBottom(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as the bottom alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAlignBottom(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as the right alignment with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @Deprecated(
    message = "Use showAlignEnd instead.",
    replaceWith = ReplaceWith("showAlignEnd(xOff, yOff)"),
  )
  public fun showAlignRight(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as the end alignment with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun showAlignEnd(xOff: Int = 0, yOff: Int = 0)

  /**
   * Coroutine alternative for [showAlignEnd]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public suspend fun awaitAlignEnd(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as the right alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @Deprecated(
    message = "Use relayShowAlignEnd instead.",
    replaceWith = ReplaceWith("relayShowAlignEnd(balloon, xOff, yOff)"),
  )
  public fun relayShowAlignRight(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as the end alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAlignEnd(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as the left alignment with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  @Deprecated(
    message = "Use showAlignStart instead.",
    replaceWith = ReplaceWith("showAlignStart(xOff, yOff)"),
  )
  public fun showAlignLeft(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as the start alignment with x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun showAlignStart(xOff: Int = 0, yOff: Int = 0)

  /**
   * Coroutine alternative for [showAlignStart]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public suspend fun awaitAlignStart(xOff: Int = 0, yOff: Int = 0)

  /**
   * Shows the balloon on an anchor view as the left alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  @Deprecated(
    message = "Use relayShowAlignStart instead.",
    replaceWith = ReplaceWith("relayShowAlignStart(balloon, xOff, yOff)"),
  )
  public fun relayShowAlignLeft(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view as the start alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAlignStart(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * Shows the balloon on an anchor view depending on the [align] alignment with x-off and y-off.
   *
   * @param align Decides where the balloon should be placed.
   * @param mainAnchor A target view which popup will be displayed.
   * @param subAnchorList A list of anchors to display focuses on the overlay view.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun showAlign(
    align: BalloonAlign,
    mainAnchor: View,
    subAnchorList: List<View> = listOf(),
    xOff: Int = 0,
    yOff: Int = 0,
  )

  /**
   * Coroutine alternative for [showAlign]. This method suspends until the popup is displayed.
   * Can be used to show popups sequentially without using relay methods.
   *
   * @param align Decides where the balloon should be placed.
   * @param mainAnchor A target view which popup will be displayed.
   * @param subAnchorList A list of anchors to display focuses on the overlay view.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public suspend fun awaitAlign(
    align: BalloonAlign,
    mainAnchor: View,
    subAnchorList: List<View> = listOf(),
    xOff: Int = 0,
    yOff: Int = 0,
  )

  /**
   * Shows the balloon on an anchor view depending on the [align] alignment with x-off and y-off
   * and shows the next balloon sequentially.
   * This function returns the next balloon.
   *
   * @param balloon A next [Balloon] that will be shown sequentially after dismissing this popup.
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   *
   * @return A next [balloon].
   *
   * @see [Show sequentially](https://github.com/skydoves/Balloon#show-sequentially)
   */
  public fun relayShowAlign(
    align: BalloonAlign,
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
  ): Balloon

  /**
   * updates popup and arrow position of the popup based on
   * a new target anchor view with additional x-off and y-off.
   *
   * @param xOff A horizontal offset from the anchor in pixels.
   * @param yOff A vertical offset from the anchor in pixels.
   */
  public fun update(xOff: Int = 0, yOff: Int = 0)

  /** updates the size of the balloon card. */
  @InternalBalloonApi
  public fun updateSizeOfBalloonCard(width: Int, height: Int)

  /** dismiss the popup menu. */
  public fun dismiss()

  /** dismiss the popup menu with milliseconds delay. */
  public fun dismissWithDelay(delay: Long): Boolean

  /** sets a [OnBalloonClickListener] to the popup. */
  public fun setOnBalloonClickListener(onBalloonClickListener: OnBalloonClickListener?)

  /** clears all persisted preferences. */
  public fun clearAllPreferences()

  /** sets a [OnBalloonClickListener] to the popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonClickListener(block: (View) -> Unit)

  /**
   * sets a [OnBalloonInitializedListener] to the popup.
   * The [OnBalloonInitializedListener.onBalloonInitialized] will be invoked when inflating the
   * body content of the balloon is finished.
   */
  public fun setOnBalloonInitializedListener(
    onBalloonInitializedListener: OnBalloonInitializedListener?,
  )

  /**
   * sets a [OnBalloonInitializedListener] to the popup using a lambda.
   * The [OnBalloonInitializedListener.onBalloonInitialized] will be invoked when inflating the
   * body content of the balloon is finished.
   */
  @JvmSynthetic
  public fun setOnBalloonInitializedListener(block: (View) -> Unit)

  /** sets a [OnBalloonDismissListener] to the popup. */
  public fun setOnBalloonDismissListener(onBalloonDismissListener: OnBalloonDismissListener?)

  /** sets a [OnBalloonDismissListener] to the popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonDismissListener(block: () -> Unit)

  /** sets a [OnBalloonOutsideTouchListener] to the popup. */
  public fun setOnBalloonOutsideTouchListener(
    onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener?,
  )

  /** sets a [OnBalloonOutsideTouchListener] to the popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit)

  /** sets a [View.OnTouchListener] to the popup. */
  public fun setOnBalloonTouchListener(onTouchListener: View.OnTouchListener?)

  /** sets a [View.OnTouchListener] to the overlay popup */
  public fun setOnBalloonOverlayTouchListener(onTouchListener: View.OnTouchListener?)

  /** sets a [View.OnTouchListener] to the overlay popup using lambda. */
  public fun setOnBalloonOverlayTouchListener(block: (View, MotionEvent) -> Boolean)

  /** sets a [OnBalloonOverlayClickListener] to the overlay popup. */
  public fun setOnBalloonOverlayClickListener(
    onBalloonOverlayClickListener: OnBalloonOverlayClickListener?,
  )

  /** sets a [OnBalloonOverlayClickListener] to the overlay popup using lambda. */
  @JvmSynthetic
  public fun setOnBalloonOverlayClickListener(block: () -> Unit)

  /**
   * sets whether the popup window will be attached in the decor frame of its parent window.
   * If you want to show up balloon on your DialogFragment, it's recommended to use with true. (#131)
   */
  public fun setIsAttachedInDecor(value: Boolean): Balloon

  /** gets measured width size of the balloon popup. */
  public fun getMeasuredWidth(): Int

  /** gets measured height size of the balloon popup. */
  public fun getMeasuredHeight(): Int

  /** gets a content view of the balloon popup window. */
  public fun getContentView(): ViewGroup

  /** gets a arrow view of the balloon popup window. */
  public fun getBalloonArrowView(): View
}
