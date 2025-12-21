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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
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
  onComposedAnchor: (ComposeView) -> Unit = {},
  onBalloonWindowInitialized: (BalloonWindow) -> Unit = {},
  balloonContent: (@Composable () -> Unit)? = null,
  content: @Composable (BalloonWindow) -> Unit,
) {
  val context = LocalContext.current
  // Use composition locals to get properly-scoped owners that respect navigation destinations.
  // This fixes memory leaks when Balloon is used inside NavHost destinations (issue #879).
  val lifecycleOwner = LocalLifecycleOwner.current
  val viewModelStoreOwner = LocalViewModelStoreOwner.current
  val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
  val anchorView = remember {
    ComposeView(context).also { composeView ->
      composeView.setViewTreeLifecycleOwner(lifecycleOwner)
      composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
      composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
    }.apply {
      post { onComposedAnchor.invoke(this) }
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

  if (LocalInspectionMode.current) {
    val balloonWindow = BalloonComposeView(
      anchorView = anchorView,
      isComposableContent = true,
      builder = builder,
      balloonID = id,
    )
    Box(modifier = modifier) {
      content.invoke(balloonWindow)
    }
    return
  }

  LaunchedEffect(Unit) {
    onBalloonWindowInitialized.invoke(balloonComposeView)
  }

  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val screenWidth = remember { with(density) { configuration.screenWidthDp.dp.toPx() }.toInt() }

  // Use SubcomposeLayout to measure balloon content without creating a Popup window.
  // This fixes the black screen issue during SplashScreen (issue #786).
  SubcomposeLayout(modifier = modifier) { constraints ->
    // Calculate width constraints for balloon content (issue #779)
    val horizontalPadding = builder.paddingLeft + builder.paddingRight +
      builder.marginLeft + builder.marginRight
    val maxContentWidth = when {
      builder.widthRatio > 0f ->
        (screenWidth * builder.widthRatio - horizontalPadding).toInt()
      builder.maxWidthRatio > 0f ->
        (screenWidth * builder.maxWidthRatio - horizontalPadding).toInt()
      else -> constraints.maxWidth - horizontalPadding
    }.coerceAtLeast(0)

    // Measure balloon content if needed (only when content exists and size not yet calculated)
    if (isComposableContent && balloonComposeView.balloonLayoutInfo.value == null) {
      val balloonContentConstraints = Constraints(
        minWidth = 0,
        maxWidth = maxContentWidth.coerceAtMost(constraints.maxWidth),
        minHeight = 0,
        maxHeight = constraints.maxHeight,
      )
      val balloonPlaceables = subcompose("balloon_measurement") {
        // balloonContent is guaranteed non-null here since isComposableContent is true
        balloonContent.invoke()
      }.map { it.measure(balloonContentConstraints) }

      if (balloonPlaceables.isNotEmpty()) {
        val measuredWidth = balloonPlaceables.maxOf { it.width }
        val measuredHeight = balloonPlaceables.maxOf { it.height }

        if (measuredWidth > 0 && measuredHeight > 0) {
          // The size should be the content size only - the balloon View applies padding externally.
          // Don't add padding here as it would be applied twice.
          val size = IntSize(width = measuredWidth, height = measuredHeight)
          balloonComposeView.updateSizeOfBalloonCard(size)
          balloonComposeView.balloonLayoutInfo.value = BalloonLayoutInfo(
            x = 0f,
            y = 0f,
            width = size.width,
            height = size.height,
          )
        }
      }
    }

    // Measure and place the main content
    val mainPlaceables = subcompose("balloon_content") {
      Box(
        modifier = Modifier.onSizeChanged {
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
    }.map { it.measure(constraints) }

    val layoutWidth = mainPlaceables.maxOfOrNull { it.width } ?: 0
    val layoutHeight = mainPlaceables.maxOfOrNull { it.height } ?: 0

    layout(layoutWidth, layoutHeight) {
      mainPlaceables.forEach { it.place(0, 0) }
    }
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
