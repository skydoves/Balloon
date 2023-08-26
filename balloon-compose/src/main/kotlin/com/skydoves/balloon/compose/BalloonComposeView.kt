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

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.R
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.IntSize
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.OnBalloonDismissListener
import com.skydoves.balloon.OnBalloonInitializedListener
import com.skydoves.balloon.OnBalloonOutsideTouchListener
import com.skydoves.balloon.OnBalloonOverlayClickListener
import com.skydoves.balloon.animations.InternalBalloonApi
import java.util.UUID

/**
 * An implementation of [AbstractComposeView] that binds view tree lifecycles with [anchorView] and
 * delegates [balloon] and composable contents.
 *
 * @property anchorView the anchor for a balloon to be displayed on the screen.
 * @param isComposableContent represents the internal content should be composable.
 * @param builder the builder of balloon.
 * @param balloonID unique UUID to restore the state of balloon.
 */
@SuppressLint("ViewConstructor")
internal class BalloonComposeView(
  private val anchorView: View,
  isComposableContent: Boolean,
  builder: Balloon.Builder,
  balloonID: UUID,
) : AbstractComposeView(anchorView.context), BalloonWindow {

  private val lifecycleOwner = anchorView.findViewTreeLifecycleOwner()

  override val balloon: Balloon = builder
    .setLifecycleOwner(lifecycleOwner)
    .setIsComposableContent(isComposableContent)
    .apply {
      if (isComposableContent) {
        setLayout(this@BalloonComposeView)
      }
    }
    .build()

  private var content: @Composable (BalloonComposeView) -> Unit by mutableStateOf({})

  internal var balloonLayoutInfo: MutableState<BalloonLayoutInfo?> = mutableStateOf(null)

  override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
    private set

  init {
    setViewTreeLifecycleOwner(lifecycleOwner)
    setViewTreeViewModelStoreOwner(anchorView.findViewTreeViewModelStoreOwner())
    setViewTreeSavedStateRegistryOwner(anchorView.findViewTreeSavedStateRegistryOwner())
    setTag(R.id.compose_view_saveable_id_tag, "BalloonComposeView:$balloonID")
  }

  @Composable
  override fun Content() {
    content.invoke(this@BalloonComposeView)
  }

  fun setContent(
    compositionContext: CompositionContext,
    content: @Composable (BalloonComposeView) -> Unit,
  ) {
    setParentCompositionContext(compositionContext)
    shouldCreateCompositionOnAttachedToWindow = true
    this.content = content
    if (isAttachedToWindow) {
      createComposition()
    }
  }

  override fun shouldShowUp(): Boolean = balloon.shouldShowUp()

  override fun showAtCenter(xOff: Int, yOff: Int, centerAlign: BalloonCenterAlign): Unit =
    balloon.showAtCenter(anchorView, xOff, yOff, centerAlign)

  override fun relayShowAtCenter(
    balloon: Balloon,
    xOff: Int,
    yOff: Int,
    centerAlign: BalloonCenterAlign,
  ): Balloon = balloon.relayShowAtCenter(balloon, anchorView, xOff, yOff, centerAlign)

  override fun showAsDropDown(xOff: Int, yOff: Int): Unit =
    balloon.showAsDropDown(anchorView, xOff, yOff)

  override fun relayShowAsDropDown(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    balloon.relayShowAsDropDown(balloon, anchorView, xOff, yOff)

  override fun showAlignTop(xOff: Int, yOff: Int): Unit =
    balloon.showAlignTop(anchorView, xOff, yOff)

  override fun relayShowAlignTop(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    balloon.relayShowAlignTop(balloon, anchorView, xOff, yOff)

  override fun showAlignBottom(xOff: Int, yOff: Int): Unit =
    balloon.showAlignBottom(anchorView, xOff, yOff)

  override fun relayShowAlignBottom(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    balloon.relayShowAlignBottom(balloon, anchorView, xOff, yOff)

  override fun showAlignRight(xOff: Int, yOff: Int): Unit =
    balloon.showAlignRight(anchorView, xOff, yOff)

  override fun relayShowAlignRight(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    balloon.relayShowAlignRight(balloon, anchorView, xOff, yOff)

  override fun showAlignLeft(xOff: Int, yOff: Int): Unit =
    balloon.showAlignLeft(anchorView, xOff, yOff)

  override fun relayShowAlignLeft(balloon: Balloon, xOff: Int, yOff: Int): Balloon =
    balloon.relayShowAlignLeft(balloon, anchorView, xOff, yOff)

  override fun relayShowAlign(
    align: BalloonAlign,
    balloon: Balloon,
    xOff: Int,
    yOff: Int,
  ): Balloon = balloon.relayShowAlign(align, balloon, anchorView, xOff, yOff)

  override fun update(xOff: Int, yOff: Int): Unit = balloon.update(anchorView, xOff, yOff)

  override fun showAlign(
    align: BalloonAlign,
    mainAnchor: View,
    subAnchorList: List<View>,
    xOff: Int,
    yOff: Int,
  ): Unit = balloon.showAlign(align, mainAnchor, subAnchorList, xOff, yOff)

  @InternalBalloonApi
  override fun updateSizeOfBalloonCard(width: Int, height: Int): Unit =
    balloon.updateSizeOfBalloonCard(width = width, height = height)

  override fun dismiss(): Unit = balloon.dismiss()

  override fun dismissWithDelay(delay: Long): Boolean = balloon.dismissWithDelay(delay)

  override fun setOnBalloonClickListener(onBalloonClickListener: OnBalloonClickListener?): Unit =
    balloon.setOnBalloonClickListener(onBalloonClickListener)

  override fun clearAllPreferences(): Unit = balloon.clearAllPreferences()

  override fun setOnBalloonClickListener(block: (View) -> Unit): Unit =
    balloon.setOnBalloonClickListener(block)

  override fun setOnBalloonInitializedListener(
    onBalloonInitializedListener: OnBalloonInitializedListener?,
  ): Unit = balloon.setOnBalloonInitializedListener(onBalloonInitializedListener)

  override fun setOnBalloonInitializedListener(block: (View) -> Unit): Unit =
    balloon.setOnBalloonInitializedListener(block)

  override fun setOnBalloonDismissListener(
    onBalloonDismissListener: OnBalloonDismissListener?,
  ): Unit = balloon.setOnBalloonDismissListener(onBalloonDismissListener)

  override fun setOnBalloonDismissListener(block: () -> Unit): Unit =
    balloon.setOnBalloonDismissListener(block)

  override fun setOnBalloonOutsideTouchListener(
    onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener?,
  ): Unit = balloon.setOnBalloonOutsideTouchListener(onBalloonOutsideTouchListener)

  override fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit): Unit =
    balloon.setOnBalloonOutsideTouchListener(block)

  override fun setOnBalloonTouchListener(onTouchListener: OnTouchListener?): Unit =
    balloon.setOnBalloonTouchListener(onTouchListener)

  override fun setOnBalloonOverlayTouchListener(onTouchListener: OnTouchListener?): Unit =
    balloon.setOnBalloonOverlayTouchListener(onTouchListener)

  override fun setOnBalloonOverlayTouchListener(block: (View, MotionEvent) -> Boolean): Unit =
    balloon.setOnBalloonOverlayTouchListener(block)

  override fun setOnBalloonOverlayClickListener(
    onBalloonOverlayClickListener: OnBalloonOverlayClickListener?,
  ): Unit = balloon.setOnBalloonOverlayClickListener(onBalloonOverlayClickListener)

  override fun setOnBalloonOverlayClickListener(block: () -> Unit): Unit =
    balloon.setOnBalloonOverlayClickListener(block)

  override fun setIsAttachedInDecor(value: Boolean): Balloon = balloon.setIsAttachedInDecor(value)

  override fun getContentView(): ViewGroup = balloon.getContentView()

  override fun getBalloonArrowView(): View = balloon.getBalloonArrowView()

  internal fun updateSizeOfBalloonCard(size: IntSize) {
    balloon.updateSizeOfBalloonCard(width = size.width, height = size.height)
    updateLayoutParams {
      width = size.width
      height = size.height
    }
  }

  internal fun dispose() {
    balloon.dismiss()
    setViewTreeLifecycleOwner(null)
    setViewTreeViewModelStoreOwner(null)
    setViewTreeSavedStateRegistryOwner(null)
  }

  override fun getAccessibilityClassName(): CharSequence {
    return javaClass.name
  }
}
