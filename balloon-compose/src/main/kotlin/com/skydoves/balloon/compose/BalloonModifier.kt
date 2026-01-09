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

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
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
 * This modifier allows you to add balloon tooltips using a modifier chain pattern.
 *
 * Example usage:
 * ```
 * val balloonState = rememberBalloonState(builder)
 *
 * Button(
 *   onClick = { balloonState.showAlignTop() },
 *   modifier = Modifier.balloon(balloonState) { Text("Tooltip") }
 * ) {
 *   Text("Show Balloon")
 * }
 * ```
 *
 * When using inside a BottomSheet or Dialog, provide the [anchorParent] parameter to ensure
 * the balloon appears above the sheet:
 * ```
 * ModalBottomSheet(onDismissRequest = { ... }) {
 *   val view = LocalView.current
 *   val anchorParent = remember { (view.parent as? ViewGroup) }
 *
 *   Button(
 *     onClick = { balloonState.showAlignTop() },
 *     modifier = Modifier.balloon(
 *       state = balloonState,
 *       anchorParent = anchorParent,
 *     ) { Text("Tooltip") }
 *   ) {
 *     Text("Show Balloon")
 *   }
 * }
 * ```
 *
 * @param state The [BalloonState] used to control the balloon.
 * @param key Optional key to trigger recomposition of balloon content.
 * @param anchorParent Optional custom parent ViewGroup for the anchor view. When displaying
 *   balloons inside a BottomSheet or Dialog, pass the sheet's content container to ensure
 *   the balloon renders above the sheet. If null (default), uses the activity's decor view.
 * @param balloonContent The composable content to display inside the balloon.
 * @return A modifier that attaches the balloon to this composable.
 */
@Composable
public fun Modifier.balloon(
  state: BalloonState,
  key: Any? = null,
  anchorParent: ViewGroup? = null,
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

  // Get the parent view for the anchor. Use custom anchorParent if provided,
  // otherwise fall back to the activity's decor view.
  val parentView = remember(context, anchorParent) {
    anchorParent ?: (context as? Activity)?.window?.decorView as? ViewGroup
  }

  // Create an invisible anchor view that will be added to the parent view
  val anchorView = remember {
    View(context).apply {
      // Make it invisible but still part of the hierarchy
      visibility = View.INVISIBLE
      setViewTreeLifecycleOwner(lifecycleOwner)
      setViewTreeViewModelStoreOwner(viewModelStoreOwner)
      setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
    }
  }

  // Track balloon layout info for measurement
  val balloonLayoutInfo = remember { mutableStateOf<BalloonLayoutInfo?>(null) }

  // Connect layout info to state for update() functionality
  remember(balloonLayoutInfo) {
    state._balloonLayoutInfo = balloonLayoutInfo
  }

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

  // Add anchor view to parent view and handle disposal
  DisposableEffect(key, parentView) {
    // Add the anchor view to the parent view
    parentView?.addView(
      anchorView,
      FrameLayout.LayoutParams(0, 0).apply {
        leftMargin = 0
        topMargin = 0
      },
    )

    onDispose {
      balloonComposeView.dispose()
      // Remove anchor view from parent view
      parentView?.removeView(anchorView)
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

  // Pre-measure balloon content if not yet measured (or if fixed-width target changed)
  val fixedWidthMode = builder.widthRatio > 0f || builder.maxWidthRatio > 0f

  // In fixed-width mode, the target width is screen-based, not anchor-constraint-based.
  // We can compute it outside the measurePolicy so we can re-trigger this Layout when it changes.
  val desiredFixedWidth = if (fixedWidthMode) {
    val w = when {
      builder.widthRatio > 0f ->
        (screenWidth * builder.widthRatio - horizontalPadding).toInt()

      builder.maxWidthRatio > 0f ->
        (screenWidth * builder.maxWidthRatio - horizontalPadding).toInt()

      else -> 0
    }.coerceAtLeast(0)
    w
  } else {
    null
  }

  if (balloonLayoutInfo.value == null ||
    (desiredFixedWidth != null && balloonLayoutInfo.value?.width != desiredFixedWidth)
  ) {
    Layout(
      content = { balloonContent() },
      measurePolicy = { measurables, constraints ->
        val isUnboundedWidth = constraints.maxWidth == Constraints.Infinity
        val isUnboundedHeight = constraints.maxHeight == Constraints.Infinity ||
          constraints.maxHeight == 0

        if (!fixedWidthMode && isUnboundedWidth) {
          return@Layout layout(0, 0) {}
        }

        val effectiveMaxWidth = if (isUnboundedWidth) screenWidth else constraints.maxWidth
        val effectiveMaxHeight = if (isUnboundedHeight) screenWidth * 2 else constraints.maxHeight

        val maxContentWidth = when {
          builder.widthRatio > 0f ->
            (screenWidth * builder.widthRatio - horizontalPadding).toInt()

          builder.maxWidthRatio > 0f ->
            (screenWidth * builder.maxWidthRatio - horizontalPadding).toInt()

          else ->
            (effectiveMaxWidth - horizontalPadding).coerceAtLeast(0)
        }.coerceAtLeast(0)

        val targetWidth = if (fixedWidthMode) {
          // IMPORTANT: do NOT clamp to constraints.maxWidth (anchor width)
          maxContentWidth
        } else {
          // Non-fixed mode: behave like before, limited by the anchor constraints.
          maxContentWidth.coerceAtMost((effectiveMaxWidth - horizontalPadding).coerceAtLeast(0))
        }.coerceAtLeast(0)

        val contentConstraints = Constraints(
          // IMPORTANT: in fixed mode, force the content host to measure at EXACT width
          minWidth = if (fixedWidthMode) targetWidth else 0,
          maxWidth = targetWidth,
          minHeight = 0,
          maxHeight = effectiveMaxHeight,
        )

        val placeables = measurables.map { it.measure(contentConstraints) }

        if (placeables.isNotEmpty()) {
          val measuredHeight = placeables.maxOf { it.height }.coerceAtLeast(0)
          val measuredWidth = if (fixedWidthMode) {
            targetWidth
          } else {
            placeables.maxOf { it.width }.coerceAtLeast(0)
          }

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

  // Track position and size using onGloballyPositioned
  return this.onGloballyPositioned { coordinates ->
    val position = coordinates.positionInWindow()
    val size = coordinates.size

    (anchorView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
      params.width = size.width
      params.height = size.height
      params.leftMargin = position.x.toInt()
      params.topMargin = position.y.toInt()
      anchorView.layoutParams = params
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
