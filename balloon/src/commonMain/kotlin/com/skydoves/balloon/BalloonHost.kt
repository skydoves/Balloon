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

package com.skydoves.balloon

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import kotlin.math.roundToInt

/**
 * A single registered [Modifier.balloon] anchor: its [state], the latest captured
 * anchor bounds, and the balloon body to render. Owned by a [BalloonRegistry] and
 * consumed by [BalloonHost].
 */
@Stable
internal class BalloonEntry(val state: BalloonState) {
  /** Anchor bounds in window coordinates, updated by the modifier's `onGloballyPositioned`. */
  var anchorBounds: IntRect? by mutableStateOf(null)

  /**
   * The balloon body; set once to a stable lambda that always reads the latest content —
   * and re-provides the CompositionLocals that were in scope where the modifier was called.
   */
  var content: @Composable () -> Unit by mutableStateOf({})
}

/**
 * A request for [BalloonHost] to draw an overlay scrim behind one balloon.
 *
 * Overlays cannot be drawn by the balloon's own `Popup` — see the KDoc on
 * [BalloonOverlayScrim] — so every balloon that wants one, whether it came from
 * [Modifier.balloon] or from the [Balloon] wrapper composable, registers here and the host
 * draws it across its whole (full-window) Box.
 */
@Stable
internal class BalloonOverlayRequest(val state: BalloonState) {
  var anchorBounds: IntRect? by mutableStateOf(null)
}

/**
 * Holds the [BalloonEntry]s registered by [Modifier.balloon], and the overlay requests
 * registered by any balloon under this [BalloonHost]. Backed by snapshot lists so the host
 * recomposes as anchors mount / unmount.
 */
@Stable
internal class BalloonRegistry {
  val entries = mutableStateListOf<BalloonEntry>()
  val overlays = mutableStateListOf<BalloonOverlayRequest>()

  fun register(entry: BalloonEntry) {
    if (entry !in entries) entries.add(entry)
  }

  fun unregister(entry: BalloonEntry) {
    entries.remove(entry)
  }

  fun registerOverlay(request: BalloonOverlayRequest) {
    if (request !in overlays) overlays.add(request)
  }

  fun unregisterOverlay(request: BalloonOverlayRequest) {
    overlays.remove(request)
  }
}

internal val LocalBalloonRegistry = staticCompositionLocalOf<BalloonRegistry?> { null }

/**
 * Hosts the balloons declared with [Modifier.balloon] anywhere inside [content].
 *
 * A Compose [Modifier] cannot emit composable content, so the modifier only *registers*
 * an anchor (and tracks its bounds); [BalloonHost] is what actually emits each balloon's
 * `Popup`. Emitting here — inside a plain [Box] that wraps the whole subtree rather than
 * as a sibling of an individual anchor — is also what avoids the layout shift that a
 * `Popup` placed directly in a spacing-based `Column`/`Row` would cause.
 *
 * Wrap a screen (or any subtree that contains balloon anchors) once:
 * ```
 * BalloonHost {
 *   Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
 *     val state = rememberBalloonState(style)
 *     Button(
 *       modifier = Modifier.balloon(state) { Text("Tooltip!") },
 *       onClick = { state.showAlignTop() },
 *     ) { Text("Anchor") }
 *   }
 * }
 * ```
 *
 * The alternative [Balloon] wrapper composable does not require a host; prefer this
 * modifier-based API when you want to decorate an existing anchor in place.
 *
 * @param modifier Applied to the [Box] that wraps [content].
 * @param content The subtree containing [Modifier.balloon] anchors.
 */
@Composable
public fun BalloonHost(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val registry = remember { BalloonRegistry() }
  // Where this host sits in the window, so anchor bounds (captured in window coordinates)
  // can be translated into the scrim's local drawing space.
  var originInWindow by remember { mutableStateOf(IntOffset.Zero) }
  CompositionLocalProvider(LocalBalloonRegistry provides registry) {
    Box(
      modifier = modifier.onGloballyPositioned { coordinates ->
        val position = coordinates.positionInWindow()
        val newOrigin = IntOffset(position.x.roundToInt(), position.y.roundToInt())
        if (originInWindow != newOrigin) originInWindow = newOrigin
      },
    ) {
      content()

      // Overlay scrims are drawn INSIDE this Box, above the content but below the balloon
      // popups (which are separate windows and therefore always on top). A `Popup` cannot
      // be used for them — see the KDoc on `BalloonOverlayScrim`.
      registry.overlays.forEach { request ->
        val bounds = request.anchorBounds
        if (bounds != null) {
          key(request) {
            BalloonOverlayScrim(
              state = request.state,
              anchorBounds = bounds,
              originInWindow = originInWindow,
              visible = request.state.isVisible,
            )
          }
        }
      }

      registry.entries.forEach { entry ->
        key(entry) {
          BalloonPopupLayer(
            state = entry.state,
            anchorBounds = entry.anchorBounds,
            balloonContent = entry.content,
          )
        }
      }
    }
  }
}

/**
 * Attaches a balloon to this anchor. The balloon's visibility, alignment and offset are
 * driven by [state] (e.g. `state.showAlignTop()`), and [balloonContent] is the body shown
 * inside the balloon.
 *
 * This mirrors the `Modifier.balloon` API of the Android-only `balloon-compose`, adapted
 * to Compose Multiplatform: because a modifier cannot emit composable content, the anchor
 * must live under a [BalloonHost], which renders the balloon. The modifier itself only
 * registers the anchor and reports its window bounds — it never emits into the caller's
 * layout, so it never shifts siblings in a `Column`/`Row`.
 *
 * @param state The [BalloonState] controlling this balloon.
 * @param key Optional key that resets the registered entry (and its captured bounds).
 * @param balloonContent The composable rendered inside the balloon body.
 * @throws IllegalStateException if there is no [BalloonHost] ancestor.
 */
@Composable
public fun Modifier.balloon(
  state: BalloonState,
  key: Any? = null,
  balloonContent: @Composable () -> Unit,
): Modifier {
  val registry = LocalBalloonRegistry.current
  checkNotNull(registry) {
    "Modifier.balloon(...) must be used inside a BalloonHost { ... }. Wrap the screen " +
      "(or the subtree that contains your anchors) in BalloonHost."
  }
  val updatedContent by rememberUpdatedState(balloonContent)
  // The body is composed by [BalloonHost], which sits ABOVE the anchor in the tree, so on
  // its own it would read the host's `MaterialTheme` / `LocalLayoutDirection` /
  // `LocalContentColor` instead of the anchor's. Capturing the locals here and re-providing
  // them around the body puts it back in the caller's scope. (The Android original gets this
  // for free by capturing a `rememberCompositionContext()` at the modifier call-site.)
  val updatedLocals by rememberUpdatedState(currentCompositionLocalContext)
  val entry = remember(state, key) {
    BalloonEntry(state).also {
      it.content = { CompositionLocalProvider(updatedLocals) { updatedContent() } }
    }
  }
  DisposableEffect(registry, entry) {
    registry.register(entry)
    onDispose { registry.unregister(entry) }
  }
  return this.onGloballyPositioned { coordinates ->
    val bounds = coordinates.boundsInWindow().toIntRect()
    if (entry.anchorBounds != bounds) {
      entry.anchorBounds = bounds
    }
  }
}
