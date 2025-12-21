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

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.OnBalloonDismissListener
import com.skydoves.balloon.OnBalloonInitializedListener
import com.skydoves.balloon.OnBalloonOutsideTouchListener
import com.skydoves.balloon.OnBalloonOverlayClickListener
import com.skydoves.balloon.annotations.InternalBalloonApi

/**
 * Creates and remembers a [BalloonState] for controlling balloon display.
 *
 * @param builder The [Balloon.Builder] used to configure the balloon.
 * @param key Optional key to trigger recomposition.
 * @return A [BalloonState] instance for controlling the balloon.
 */
@Composable
public fun rememberBalloonState(
  builder: Balloon.Builder,
  key: Any? = null,
): BalloonState {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  return remember(key) {
    BalloonState(
      context = context,
      builder = builder,
      lifecycleOwner = lifecycleOwner,
    )
  }
}

/**
 * A state holder for managing balloon display and interactions.
 * This class implements [BalloonWindow] interface and provides control over the balloon lifecycle.
 *
 * Use [rememberBalloonState] to create an instance of this class in a composable context.
 *
 * Example usage:
 * ```
 * val balloonState = rememberBalloonState(builder)
 *
 * Text("Click me")
 *   .balloon(balloonState) {
 *     Text("Tooltip content")
 *   }
 *
 * // Show balloon
 * balloonState.showAlignTop()
 * ```
 */
@Stable
public class BalloonState internal constructor(
  private val context: Context,
  internal val builder: Balloon.Builder,
  private val lifecycleOwner: LifecycleOwner,
) : BalloonWindow {

  /** The anchor view for the balloon. Set internally by the balloon modifier. */
  internal var _anchorView: View? = null

  /** The internal balloon window delegate. Set internally by the balloon modifier. */
  internal var _balloonWindow: BalloonWindow? = null

  override val anchorView: View
    get() = _anchorView
      ?: error("BalloonState is not attached to a composable. Use Modifier.balloon() to attach.")

  override val balloon: Balloon
    get() = _balloonWindow?.balloon
      ?: error("BalloonState is not attached to a composable. Use Modifier.balloon() to attach.")

  /** Returns whether the balloon is currently showing. */
  public val isShowing: Boolean
    get() = _balloonWindow?.balloon?.isShowing == true

  /** Returns whether the balloon state is attached to a composable. */
  public val isAttached: Boolean
    get() = _anchorView != null && _balloonWindow != null

  override fun shouldShowUp(): Boolean = _balloonWindow?.shouldShowUp() ?: false

  override fun showAtCenter(xOff: Int, yOff: Int, centerAlign: BalloonCenterAlign) {
    _balloonWindow?.showAtCenter(xOff, yOff, centerAlign)
  }

  override suspend fun awaitAtCenter(xOff: Int, yOff: Int, centerAlign: BalloonCenterAlign) {
    _balloonWindow?.awaitAtCenter(xOff, yOff, centerAlign)
  }

  override fun relayShowAtCenter(
    balloon: Balloon,
    xOff: Int,
    yOff: Int,
    centerAlign: BalloonCenterAlign,
  ): Balloon = _balloonWindow?.relayShowAtCenter(balloon, xOff, yOff, centerAlign) ?: balloon

  override fun showAsDropDown(xOff: Int, yOff: Int) {
    _balloonWindow?.showAsDropDown(xOff, yOff)
  }

  override suspend fun awaitAsDropDown(xOff: Int, yOff: Int) {
    _balloonWindow?.awaitAsDropDown(xOff, yOff)
  }

  override fun relayShowAsDropDown(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    _balloonWindow?.relayShowAsDropDown(balloon, xOff, yOff) ?: balloon

  override fun showAlignTop(xOff: Int, yOff: Int) {
    _balloonWindow?.showAlignTop(xOff, yOff)
  }

  override suspend fun awaitAlignTop(xOff: Int, yOff: Int) {
    _balloonWindow?.awaitAlignTop(xOff, yOff)
  }

  override fun relayShowAlignTop(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    _balloonWindow?.relayShowAlignTop(balloon, xOff, yOff) ?: balloon

  override fun showAlignBottom(xOff: Int, yOff: Int) {
    _balloonWindow?.showAlignBottom(xOff, yOff)
  }

  override suspend fun awaitAlignBottom(xOff: Int, yOff: Int) {
    _balloonWindow?.awaitAlignBottom(xOff, yOff)
  }

  override fun relayShowAlignBottom(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    _balloonWindow?.relayShowAlignBottom(balloon, xOff, yOff) ?: balloon

  override fun showAlignEnd(xOff: Int, yOff: Int) {
    _balloonWindow?.showAlignEnd(xOff, yOff)
  }

  override suspend fun awaitAlignEnd(xOff: Int, yOff: Int) {
    _balloonWindow?.awaitAlignEnd(xOff, yOff)
  }

  override fun relayShowAlignEnd(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    _balloonWindow?.relayShowAlignEnd(balloon, xOff, yOff) ?: balloon

  override fun showAlignStart(xOff: Int, yOff: Int) {
    _balloonWindow?.showAlignStart(xOff, yOff)
  }

  override suspend fun awaitAlignStart(xOff: Int, yOff: Int) {
    _balloonWindow?.awaitAlignStart(xOff, yOff)
  }

  override fun relayShowAlignStart(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    _balloonWindow?.relayShowAlignStart(balloon, xOff, yOff) ?: balloon

  override fun showAlign(
    align: BalloonAlign,
    mainAnchor: View,
    subAnchorList: List<View>,
    xOff: Int,
    yOff: Int,
  ) {
    _balloonWindow?.showAlign(align, mainAnchor, subAnchorList, xOff, yOff)
  }

  override suspend fun awaitAlign(
    align: BalloonAlign,
    mainAnchor: View,
    subAnchorList: List<View>,
    xOff: Int,
    yOff: Int,
  ) {
    _balloonWindow?.awaitAlign(align, mainAnchor, subAnchorList, xOff, yOff)
  }

  override fun relayShowAlign(
    align: BalloonAlign,
    balloon: Balloon,
    xOff: Int,
    yOff: Int,
  ): Balloon = _balloonWindow?.relayShowAlign(align, balloon, xOff, yOff) ?: balloon

  override fun updateAlignTop(xOff: Int, yOff: Int) {
    _balloonWindow?.updateAlignTop(xOff, yOff)
  }

  override fun updateAlignBottom(xOff: Int, yOff: Int) {
    _balloonWindow?.updateAlignBottom(xOff, yOff)
  }

  override fun updateAlignEnd(xOff: Int, yOff: Int) {
    _balloonWindow?.updateAlignEnd(xOff, yOff)
  }

  override fun updateAlignStart(xOff: Int, yOff: Int) {
    _balloonWindow?.updateAlignStart(xOff, yOff)
  }

  override fun updateAlign(align: BalloonAlign, xOff: Int, yOff: Int) {
    _balloonWindow?.updateAlign(align, xOff, yOff)
  }

  override fun update(xOff: Int, yOff: Int) {
    _balloonWindow?.update(xOff, yOff)
  }

  @InternalBalloonApi
  override fun updateSizeOfBalloonCard(width: Int, height: Int) {
    _balloonWindow?.updateSizeOfBalloonCard(width, height)
  }

  override fun dismiss() {
    _balloonWindow?.dismiss()
  }

  override fun dismissWithDelay(delay: Long): Boolean =
    _balloonWindow?.dismissWithDelay(delay) ?: false

  override fun setOnBalloonClickListener(onBalloonClickListener: OnBalloonClickListener?) {
    _balloonWindow?.setOnBalloonClickListener(onBalloonClickListener)
  }

  override fun clearAllPreferences() {
    _balloonWindow?.clearAllPreferences()
  }

  override fun setOnBalloonClickListener(block: (View) -> Unit) {
    _balloonWindow?.setOnBalloonClickListener(block)
  }

  override fun setOnBalloonInitializedListener(
    onBalloonInitializedListener: OnBalloonInitializedListener?,
  ) {
    _balloonWindow?.setOnBalloonInitializedListener(onBalloonInitializedListener)
  }

  override fun setOnBalloonInitializedListener(block: (View) -> Unit) {
    _balloonWindow?.setOnBalloonInitializedListener(block)
  }

  override fun setOnBalloonDismissListener(onBalloonDismissListener: OnBalloonDismissListener?) {
    _balloonWindow?.setOnBalloonDismissListener(onBalloonDismissListener)
  }

  override fun setOnBalloonDismissListener(block: () -> Unit) {
    _balloonWindow?.setOnBalloonDismissListener(block)
  }

  override fun setOnBalloonOutsideTouchListener(
    onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener?,
  ) {
    _balloonWindow?.setOnBalloonOutsideTouchListener(onBalloonOutsideTouchListener)
  }

  override fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit) {
    _balloonWindow?.setOnBalloonOutsideTouchListener(block)
  }

  override fun setOnBalloonTouchListener(onTouchListener: View.OnTouchListener?) {
    _balloonWindow?.setOnBalloonTouchListener(onTouchListener)
  }

  override fun setOnBalloonOverlayTouchListener(onTouchListener: View.OnTouchListener?) {
    _balloonWindow?.setOnBalloonOverlayTouchListener(onTouchListener)
  }

  override fun setOnBalloonOverlayTouchListener(block: (View, MotionEvent) -> Boolean) {
    _balloonWindow?.setOnBalloonOverlayTouchListener(block)
  }

  override fun setOnBalloonOverlayClickListener(
    onBalloonOverlayClickListener: OnBalloonOverlayClickListener?,
  ) {
    _balloonWindow?.setOnBalloonOverlayClickListener(onBalloonOverlayClickListener)
  }

  override fun setOnBalloonOverlayClickListener(block: () -> Unit) {
    _balloonWindow?.setOnBalloonOverlayClickListener(block)
  }

  override fun setIsAttachedInDecor(value: Boolean): Balloon =
    _balloonWindow?.setIsAttachedInDecor(value) ?: balloon

  override fun getMeasuredWidth(): Int = _balloonWindow?.getMeasuredWidth() ?: 0

  override fun getMeasuredHeight(): Int = _balloonWindow?.getMeasuredHeight() ?: 0

  override fun getContentView(): ViewGroup =
    _balloonWindow?.getContentView() ?: error("BalloonState is not attached")

  override fun getBalloonArrowView(): View =
    _balloonWindow?.getBalloonArrowView() ?: error("BalloonState is not attached")

  internal fun dispose() {
    _balloonWindow?.dismiss()
    _balloonWindow = null
    _anchorView = null
  }
}
