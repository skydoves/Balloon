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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.skydoves.balloon.Balloon
import java.lang.Integer.max
import java.util.UUID

/**
 * Balloon allows you to display tooltips, which is fully customizable with an arrow
 * and animations for Compose.
 *
 * @param modifier [Modifier] used to adjust the layout or drawing content.
 * @param builder [Balloon.Builder] that includes details of tooltips to be displayed.
 * @param key key to recompose the content of balloon.
 * @param balloonContent the content to be displayed inside the balloon.
 * @param content the main content of the screen. You should use the [BalloonWindow] to control balloon.
 */
@Composable
public fun Balloon(
  modifier: Modifier = Modifier,
  builder: Balloon.Builder,
  key: Any? = null,
  balloonContent: (@Composable () -> Unit)? = null,
  content: @Composable (BalloonWindow) -> Unit,
) {
  val context = LocalContext.current
  val view = LocalView.current
  val anchorView = remember {
    ComposeView(context).also { composeView ->
      composeView.setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
      composeView.setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
      composeView.setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())
    }
  }
  val compositionContext = rememberCompositionContext()
  val currentContent by rememberUpdatedState(balloonContent)
  val isComposableContent = balloonContent != null
  val id = rememberSaveable { UUID.randomUUID() }
  val balloonComposeView = remember(key) {
    BalloonComposeView(
      anchorView = anchorView,
      isComposableContent = isComposableContent,
      builder = builder,
      balloonID = id,
    ).apply {
      if (isComposableContent) {
        setContent(compositionContext) {
          BalloonLayout(
            modifier = Modifier.semantics { balloon() },
          ) {
            currentContent?.invoke()
          }
        }
      }
    }
  }

  if (isComposableContent && balloonComposeView.balloonLayoutInfo.value == null) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = remember { with(density) { configuration.screenWidthDp.dp.toPx() }.toInt() }
    val paddingStart =
      remember { with(density) { (builder.paddingLeft + builder.marginLeft).toDp() } }
    val paddingEnd =
      remember { with(density) { (builder.paddingRight + builder.marginRight).toDp() } }
    Popup(
      offset = IntOffset(
        Int.MAX_VALUE,
        Int.MAX_VALUE,
      ),
      properties = PopupProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
      ),
    ) {
      Box(
        modifier = Modifier
          .alpha(0f)
          .padding(start = paddingStart, end = paddingEnd)
          .onGloballyPositioned { coordinates ->
            val originalSize = coordinates.size
            val padding = builder.paddingLeft + builder.paddingRight
            val margin = builder.marginLeft + builder.marginRight
            val space = padding + margin + builder.arrowSize
            val calculatedWidth = if (originalSize.width + space > screenWidth) {
              screenWidth - space
            } else {
              originalSize.width
            }
            val size = IntSize(
              width = calculatedWidth,
              height = coordinates.size.height,
            )
            balloonComposeView.updateSizeOfBalloonCard(size)
            balloonComposeView.balloonLayoutInfo.value = BalloonLayoutInfo(
              x = coordinates.positionInWindow().x,
              y = coordinates.positionInWindow().y,
              width = size.width,
              height = size.height,
            )
          },
      ) {
        balloonContent?.invoke()
      }
    }
  }

  Box(
    modifier = modifier.onSizeChanged {
      anchorView.updateLayoutParams {
        width = it.width
        height = it.height
      }
    },
  ) {
    AndroidView(
      modifier = Modifier.matchParentSize(),
      factory = { anchorView },
    )

    content.invoke(balloonComposeView)
  }

  DisposableEffect(key1 = key) {
    onDispose {
      // dispose ComposeView and balloon whenever the balloon content should be recomposed.
      balloonComposeView.dispose()
      // clear anchor view's lifecycle.
      anchorView.apply {
        setViewTreeSavedStateRegistryOwner(null)
        setViewTreeLifecycleOwner(null)
        setViewTreeViewModelStoreOwner(null)
      }
    }
  }
}

@Composable
private fun BalloonLayout(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Layout(
    content = content,
    modifier = modifier,
  ) { measurables, constraints ->
    val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val placeables = measurables.map { it.measure(contentConstraints) }
    val maxWidth: Int = max(placeables.maxOf { it.width }, constraints.minWidth)
    val maxHeight = max(placeables.maxOf { it.height }, constraints.minHeight)
    // position the children.
    layout(maxWidth, maxHeight) {
      placeables.forEach {
        it.place(0, 0)
      }
    }
  }
}
