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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.skydoves.balloon.Balloon
import java.lang.Integer.max
import java.util.UUID

@Composable
public fun BalloonCompose(
  modifier: Modifier = Modifier,
  builder: Balloon.Builder,
  key: Any? = null,
  balloonContent: @Composable () -> Unit,
  content: @Composable (BalloonComposeView) -> Unit
) {
  val current = LocalContext.current
  val view = LocalView.current
  val anchorView = remember {
    ComposeView(current).also {
      ViewTreeLifecycleOwner.set(it, ViewTreeLifecycleOwner.get(view))
      ViewTreeViewModelStoreOwner.set(it, ViewTreeViewModelStoreOwner.get(view))
      it.setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())
    }
  }
  val compositionContext = rememberCompositionContext()
  val currentContent by rememberUpdatedState(balloonContent)
  val id = rememberSaveable { UUID.randomUUID() }
  val balloonComposeView = remember(key) {
    BalloonComposeView(
      anchorView = anchorView,
      builder = builder,
      balloonID = id
    ).apply {
      setContent(compositionContext) {
        BalloonLayout(
          Modifier.semantics { balloon() }
        ) {
          currentContent.invoke()
        }
      }
    }
  }

  if (balloonComposeView.balloonLayoutInfo.value == null) {
    Box(
      modifier = Modifier
        .alpha(0f)
        .onGloballyPositioned { coordinates ->
          val size = coordinates.size
          balloonComposeView.updateHeightOfBalloonCard(size)
          balloonComposeView.internalBalloonLayoutInfo.value = BalloonLayoutInfo(
            x = coordinates.positionInWindow().x,
            y = coordinates.positionInWindow().y,
            width = size.width,
            height = size.height
          )
        }
    ) {
      balloonContent.invoke()
    }
  }

  Box(modifier = modifier) {
    AndroidView(
      modifier = Modifier.matchParentSize(),
      factory = { anchorView }
    )

    content.invoke(balloonComposeView)
  }

  DisposableEffect(key1 = Unit) {
    onDispose {
      anchorView.apply {
        setViewTreeSavedStateRegistryOwner(null)
        ViewTreeLifecycleOwner.set(this, null)
        ViewTreeViewModelStoreOwner.set(this, null)
      }
    }
  }
}

@Composable
private fun BalloonLayout(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Layout(
    content = content,
    modifier = modifier
  ) { measurables, constraints ->
    val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val placeables = measurables.map { it.measure(contentConstraints) }
    val maxWidth: Int = max(placeables.maxOf { it.width }, constraints.minWidth)
    val maxHeight = max(placeables.maxOf { it.height }, constraints.minHeight)
    // Position the children.
    layout(maxWidth, maxHeight) {
      placeables.forEach {
        it.place(0, 0)
      }
    }
  }
}
