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

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID

/**
 * A modifier that attaches a balloon tooltip to the composable.
 *
 * This modifier allows you to add balloon tooltips using a modifier chain pattern
 * instead of wrapping content with the [Balloon] composable.
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
 * Button(onClick = { balloonState.showAlignTop() }) {
 *   Text("Show")
 * }
 * ```
 *
 * @param state The [BalloonState] used to control the balloon.
 * @param key Optional key to trigger recomposition of balloon content.
 * @param balloonContent The composable content to display inside the balloon.
 * @return A modifier that attaches the balloon to this composable.
 */
@Composable
public fun Modifier.balloon(
  state: BalloonState,
  key: Any? = null,
  balloonContent: @Composable () -> Unit,
): Modifier {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val viewModelStoreOwner = LocalViewModelStoreOwner.current
  val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
  val compositionContext = rememberCompositionContext()
  val currentContent by rememberUpdatedState(balloonContent)
  val configuration = LocalConfiguration.current
  val density = LocalDensity.current
  val screenWidth = remember { with(density) { configuration.screenWidthDp.dp.toPx() }.toInt() }
  val isInspectionMode = LocalInspectionMode.current

  val id = rememberSaveable { UUID.randomUUID() }

  // Create anchor view
  val anchorView = remember {
    ComposeView(context).also { composeView ->
      composeView.setViewTreeLifecycleOwner(lifecycleOwner)
      composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
      composeView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
    }
  }

  // Track balloon layout info for measurement
  val balloonLayoutInfo = remember { mutableStateOf<BalloonLayoutInfo?>(null) }

  // Create BalloonComposeView and connect to state
  val balloonComposeView = remember(key) {
    BalloonComposeView(
      anchorView = anchorView,
      isComposableContent = true,
      builder = state.builder,
      balloonID = id,
    ).apply {
      setContent(compositionContext) {
        BalloonModifierLayout(
          modifier = Modifier.semantics { balloon() },
        ) {
          currentContent()
        }
      }
    }
  }

  // Attach to state
  remember(balloonComposeView) {
    state._anchorView = anchorView
    state._balloonWindow = balloonComposeView
    balloonComposeView
  }

  // Handle disposal
  DisposableEffect(key) {
    onDispose {
      balloonComposeView.dispose()
      anchorView.apply {
        setViewTreeSavedStateRegistryOwner(null)
        setViewTreeLifecycleOwner(null)
        setViewTreeViewModelStoreOwner(null)
      }
      state.dispose()
    }
  }

  // Skip in inspection mode
  if (isInspectionMode) {
    return this
  }

  // Measure balloon content
  val builder = state.builder
  val horizontalPadding = builder.paddingLeft + builder.paddingRight +
    builder.marginLeft + builder.marginRight

  // Pre-measure balloon content if not yet measured
  if (balloonLayoutInfo.value == null) {
    Layout(
      content = { balloonContent() },
      measurePolicy = { measurables, constraints ->
        val maxContentWidth = when {
          builder.widthRatio > 0f ->
            (screenWidth * builder.widthRatio - horizontalPadding).toInt()
          builder.maxWidthRatio > 0f ->
            (screenWidth * builder.maxWidthRatio - horizontalPadding).toInt()
          else -> constraints.maxWidth - horizontalPadding
        }.coerceAtLeast(0)

        val contentConstraints = Constraints(
          minWidth = 0,
          maxWidth = maxContentWidth.coerceAtMost(constraints.maxWidth),
          minHeight = 0,
          maxHeight = constraints.maxHeight,
        )

        val placeables = measurables.map { it.measure(contentConstraints) }

        if (placeables.isNotEmpty()) {
          val measuredWidth = placeables.maxOf { it.width }
          val measuredHeight = placeables.maxOf { it.height }

          if (measuredWidth > 0 && measuredHeight > 0) {
            val size = IntSize(width = measuredWidth, height = measuredHeight)
            balloonComposeView.updateSizeOfBalloonCard(size)
            balloonLayoutInfo.value = BalloonLayoutInfo(
              x = 0f,
              y = 0f,
              width = size.width,
              height = size.height,
            )
          }
        }

        layout(0, 0) {}
      },
    )
  }

  // Use Modifier.Node for size tracking
  return this then BalloonAnchorElement(anchorView)
}

/**
 * ModifierNodeElement for balloon anchor size tracking.
 */
private data class BalloonAnchorElement(
  private val anchorView: View,
) : ModifierNodeElement<BalloonAnchorNode>() {

  override fun create(): BalloonAnchorNode = BalloonAnchorNode(anchorView)

  override fun update(node: BalloonAnchorNode) {
    node.anchorView = anchorView
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "balloonAnchor"
  }
}

/**
 * Modifier.Node for tracking anchor size and updating the anchor view.
 */
private class BalloonAnchorNode(
  var anchorView: View,
) : Modifier.Node(), LayoutModifierNode {

  private var lastSize: IntSize = IntSize.Zero

  override fun MeasureScope.measure(
    measurable: Measurable,
    constraints: Constraints,
  ): MeasureResult {
    val placeable = measurable.measure(constraints)
    val newSize = IntSize(placeable.width, placeable.height)

    // Update anchor view size if changed
    if (newSize != lastSize) {
      lastSize = newSize
      updateAnchorSize(newSize)
    }

    return layout(placeable.width, placeable.height) {
      placeable.place(0, 0)
    }
  }

  private fun updateAnchorSize(size: IntSize) {
    val params = anchorView.layoutParams
    if (params != null) {
      params.width = size.width
      params.height = size.height
      anchorView.layoutParams = params
    } else {
      // Create new layout params if none exist
      anchorView.layoutParams = ViewGroup.LayoutParams(size.width, size.height)
    }
  }
}

@Composable
private fun BalloonModifierLayout(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Layout(
    content = content,
    modifier = modifier,
  ) { measurables, constraints ->
    val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val placeables = measurables.map { it.measure(contentConstraints) }
    val maxWidth = maxOf(placeables.maxOfOrNull { it.width } ?: 0, constraints.minWidth)
    val maxHeight = maxOf(placeables.maxOfOrNull { it.height } ?: 0, constraints.minHeight)
    layout(maxWidth, maxHeight) {
      placeables.forEach { it.place(0, 0) }
    }
  }
}
