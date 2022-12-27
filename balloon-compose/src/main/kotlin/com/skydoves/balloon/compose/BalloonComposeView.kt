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
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.R
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.AbstractComposeView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.skydoves.balloon.Balloon
import java.util.UUID

@SuppressLint("ViewConstructor")
public class BalloonComposeView constructor(
  private val anchorView: View,
  builder: Balloon.Builder,
  balloonID: UUID
) : AbstractComposeView(anchorView.context) {

  private val balloon: Balloon = builder
    .setIsComposableContent(true)
    .setLayout(this)
    .build()

  private var content: @Composable (BalloonComposeView) -> Unit by mutableStateOf({})

  private var _balloonLayoutInfo: MutableState<BalloonLayoutInfo?> = mutableStateOf(null)
  public val balloonLayoutInfo: State<BalloonLayoutInfo?> = _balloonLayoutInfo

  override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
    private set

  init {
    ViewTreeLifecycleOwner.set(this, ViewTreeLifecycleOwner.get(anchorView))
    ViewTreeViewModelStoreOwner.set(this, ViewTreeViewModelStoreOwner.get(anchorView))
    setViewTreeSavedStateRegistryOwner(anchorView.findViewTreeSavedStateRegistryOwner())
    setTag(R.id.compose_view_saveable_id_tag, "BalloonComposeView:$balloonID")
  }

  @Composable
  override fun Content() {
    Box(
      modifier = Modifier
        .wrapContentSize()
        .onGloballyPositioned { coordinates ->
          _balloonLayoutInfo.value = BalloonLayoutInfo(
            x = coordinates.positionInWindow().x,
            y = coordinates.positionInWindow().y,
            width = coordinates.size.width,
            height = coordinates.size.height
          )
        }
    ) {
      content.invoke(this@BalloonComposeView)
    }

    DisposableEffect(key1 = Unit) {
      onDispose { dispose() }
    }
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

  public fun showAtCenter() {
    balloon.showAlignTop(anchor = anchorView)
  }

  private fun dispose() {
    balloon.dismiss()
    setViewTreeSavedStateRegistryOwner(null)
    ViewTreeLifecycleOwner.set(this@BalloonComposeView, null)
    ViewTreeViewModelStoreOwner.set(this@BalloonComposeView, null)
  }

  override fun getAccessibilityClassName(): CharSequence {
    return javaClass.name
  }
}
