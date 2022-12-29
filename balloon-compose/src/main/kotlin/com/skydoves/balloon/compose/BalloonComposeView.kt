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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.R
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
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

@SuppressLint("ViewConstructor")
public class BalloonComposeView constructor(
  private val anchorView: View,
  isComposableContent: Boolean,
  builder: Balloon.Builder,
  balloonID: UUID
) : AbstractComposeView(anchorView.context), BalloonWindow {

  private val lifecycleOwner = ViewTreeLifecycleOwner.get(anchorView)

  public override val balloon: Balloon = builder
    .setLifecycleOwner(lifecycleOwner)
    .setIsComposableContent(isComposableContent)
    .apply {
      if (isComposableContent) {
        setLayout(this@BalloonComposeView)
      }
    }
    .build()

  private var content: @Composable (BalloonComposeView) -> Unit by mutableStateOf({})

  internal var internalBalloonLayoutInfo: MutableState<BalloonLayoutInfo?> = mutableStateOf(null)
  public val balloonLayoutInfo: State<BalloonLayoutInfo?> = internalBalloonLayoutInfo

  override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
    private set

  init {
    ViewTreeLifecycleOwner.set(this, lifecycleOwner)
    ViewTreeViewModelStoreOwner.set(this, ViewTreeViewModelStoreOwner.get(anchorView))
    setViewTreeSavedStateRegistryOwner(anchorView.findViewTreeSavedStateRegistryOwner())
    setTag(R.id.compose_view_saveable_id_tag, "BalloonComposeView:$balloonID")
  }

  @Composable
  override fun Content() {
    content.invoke(this@BalloonComposeView)
  }

  public fun setContent(
    compositionContext: CompositionContext,
    content: @Composable (BalloonComposeView) -> Unit
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
    centerAlign: BalloonCenterAlign
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
    yOff: Int
  ): Balloon = balloon.relayShowAlign(align, balloon, anchorView, xOff, yOff)

  override fun update(xOff: Int, yOff: Int): Unit = balloon.update(anchorView, xOff, yOff)

  override fun showAlign(
    align: BalloonAlign,
    mainAnchor: View,
    subAnchorList: List<View>,
    xOff: Int,
    yOff: Int
  ): Unit = balloon.showAlign(align, mainAnchor, subAnchorList, xOff, yOff)

  @InternalBalloonApi
  override fun updateHeightOfBalloonCard(height: Int): Unit =
    balloon.updateHeightOfBalloonCard(height)

  override fun dismiss(): Unit = balloon.dismiss()

  override fun dismissWithDelay(delay: Long): Boolean = balloon.dismissWithDelay(delay)

  override fun setOnBalloonClickListener(onBalloonClickListener: OnBalloonClickListener?): Unit =
    balloon.setOnBalloonClickListener(onBalloonClickListener)

  override fun clearAllPreferences(): Unit = balloon.clearAllPreferences()

  override fun setOnBalloonClickListener(block: (View) -> Unit): Unit =
    balloon.setOnBalloonClickListener(block)

  override fun setOnBalloonInitializedListener(onBalloonInitializedListener: OnBalloonInitializedListener?): Unit =
    balloon.setOnBalloonInitializedListener(onBalloonInitializedListener)

  override fun setOnBalloonInitializedListener(block: (View) -> Unit): Unit =
    balloon.setOnBalloonInitializedListener(block)

  override fun setOnBalloonDismissListener(onBalloonDismissListener: OnBalloonDismissListener?): Unit =
    balloon.setOnBalloonDismissListener(onBalloonDismissListener)

  override fun setOnBalloonDismissListener(block: () -> Unit): Unit =
    balloon.setOnBalloonDismissListener(block)

  override fun setOnBalloonOutsideTouchListener(onBalloonOutsideTouchListener: OnBalloonOutsideTouchListener?): Unit =
    balloon.setOnBalloonOutsideTouchListener(onBalloonOutsideTouchListener)

  override fun setOnBalloonOutsideTouchListener(block: (View, MotionEvent) -> Unit): Unit =
    balloon.setOnBalloonOutsideTouchListener(block)

  override fun setOnBalloonTouchListener(onTouchListener: OnTouchListener?): Unit =
    balloon.setOnBalloonTouchListener(onTouchListener)

  override fun setOnBalloonOverlayTouchListener(onTouchListener: OnTouchListener?): Unit =
    balloon.setOnBalloonOverlayTouchListener(onTouchListener)

  override fun setOnBalloonOverlayTouchListener(block: (View, MotionEvent) -> Boolean): Unit =
    balloon.setOnBalloonOverlayTouchListener(block)

  override fun setOnBalloonOverlayClickListener(onBalloonOverlayClickListener: OnBalloonOverlayClickListener?): Unit =
    balloon.setOnBalloonOverlayClickListener(onBalloonOverlayClickListener)

  override fun setOnBalloonOverlayClickListener(block: () -> Unit): Unit =
    balloon.setOnBalloonOverlayClickListener(block)

  override fun setIsAttachedInDecor(value: Boolean): Balloon = balloon.setIsAttachedInDecor(value)

  override fun getContentView(): ViewGroup = balloon.getContentView()

  override fun getBalloonArrowView(): View = balloon.getBalloonArrowView()

  internal fun updateHeightOfBalloonCard(size: IntSize) {
    balloon.updateHeightOfBalloonCard(height = size.height)
  }

  internal fun dispose() {
    balloon.dismiss()
    setViewTreeSavedStateRegistryOwner(null)
    ViewTreeLifecycleOwner.set(this@BalloonComposeView, null)
    ViewTreeViewModelStoreOwner.set(this@BalloonComposeView, null)
  }

  override fun getAccessibilityClassName(): CharSequence {
    return javaClass.name
  }
}
