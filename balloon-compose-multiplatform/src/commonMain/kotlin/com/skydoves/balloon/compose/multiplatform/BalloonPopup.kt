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

package com.skydoves.balloon.compose.multiplatform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Anchors a balloon-style popup to [content]. The anchor composable is wrapped in
 * a [Box] whose bounds in window coordinates are observed via
 * [onGloballyPositioned]; the popup is hosted by an
 * [androidx.compose.ui.window.Popup] whose visibility is controlled by [state]
 * and placed precisely against the anchor regardless of layout changes.
 *
 * This mirrors the `Balloon(...)` composable of the original `balloon-compose`
 * Android library: the anchor goes in the trailing [content] slot and the balloon
 * body in [balloonContent].
 *
 * Implementation notes:
 * - The popup MUST be emitted inside the wrapper [Box], never as a sibling of the
 *   anchor in the caller's layout. `Popup` emits a zero-sized layout node into the
 *   host composition; as a direct child of a `Column`/`Row` with
 *   `Arrangement.spacedBy` (or `SpaceEvenly`/`SpaceBetween`) that node receives
 *   its own spacing slot, which visibly shifts the anchor whenever the balloon
 *   mounts or unmounts. Inside the wrapper Box the node is inert. (This is why
 *   the earlier `Modifier.balloon(...)` API — which emitted the Popup at the
 *   modifier call-site — was removed.)
 * - The popup's offset is resolved by [BalloonPopupPositionProvider] from the
 *   captured anchor [IntRect], the requested [BalloonAlign] and any manual
 *   offset stored on [state].
 * - Enter / exit animations are driven by [AnimatedVisibility] using
 *   [balloonEnterTransition] / [balloonExitTransition].
 *
 * Example:
 * ```
 * val style = rememberBalloonBuilder { setBackgroundColor(Color(0xFF1E88E5)) }
 * val state = rememberBalloonState(style)
 *
 * Balloon(
 *   state = state,
 *   balloonContent = { Text("Tooltip!") },
 * ) {
 *   Button(onClick = { state.showAlignTop() }) { Text("Show") }
 * }
 * ```
 *
 * @param state The [BalloonState] controlling visibility, alignment and offset.
 * @param modifier Applied to the wrapper [Box] around [content]. Layout-affecting
 *   modifiers for the anchor (e.g. `Modifier.weight(...)` in a Row) go here.
 * @param key Optional key used to reset the captured anchor bounds.
 * @param balloonContent The composable rendered inside the balloon body.
 * @param content The anchor composable the balloon points at.
 */
@Composable
public fun Balloon(
  state: BalloonState,
  modifier: Modifier = Modifier,
  key: Any? = null,
  balloonContent: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  val anchorBoundsState: MutableState<IntRect?> = remember(key) { mutableStateOf(null) }

  Box(
    modifier = modifier.onGloballyPositioned { coordinates ->
      val newBounds = coordinates.boundsInWindow().toIntRect()
      // Avoid recomposition cascades: only push when bounds actually change.
      if (anchorBoundsState.value != newBounds) {
        anchorBoundsState.value = newBounds
      }
    },
  ) {
    content()
    // The popup layer MUST live inside this wrapper Box (never as a sibling of the
    // anchor in the caller's Column/Row), so the zero-sized node `Popup` emits can't
    // claim a spacing slot and shift the anchor.
    BalloonPopupLayer(
      state = state,
      anchorBounds = anchorBoundsState.value,
      balloonContent = balloonContent,
    )
  }
}

/**
 * Emits the balloon [Popup] (with enter/exit animation and auto-dismiss) for [state],
 * positioned against [anchorBounds]. Shared by the [Balloon] anchor wrapper and by
 * [BalloonHost] (the `Modifier.balloon` path) so both render identically.
 *
 * Must be hosted inside a container that is NOT a spacing-based `Column`/`Row`
 * (a plain `Box` is ideal): `Popup` emits a zero-sized node into the host
 * composition, which would otherwise receive a spacing slot and shift its siblings.
 */
@Composable
internal fun BalloonPopupLayer(
  state: BalloonState,
  anchorBounds: IntRect?,
  balloonContent: @Composable () -> Unit,
) {
  val style = state.style
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current
  val currentBalloonContent by rememberUpdatedState(balloonContent)

  // Auto-dismiss after a configurable timeout. Keying on `state.showGeneration`
  // (bumped by every show/showAtCenter, even while already visible) restarts the
  // timer on each show; keying on `state.isVisible` cancels the pending delay the
  // moment the balloon is dismissed early; keying on `style.autoDismissMillis` lets
  // a changed timeout take effect without waiting for the next show.
  if (style.autoDismissMillis > 0L) {
    LaunchedEffect(state, state.isVisible, state.showGeneration, style.autoDismissMillis) {
      if (state.isVisible) {
        delay(style.autoDismissMillis)
        if (state.isVisible) state.dismiss()
      }
    }
  }

  // Overlays are drawn by BalloonHost across its whole Box, because a Popup's window
  // cannot cover the system bars (see `BalloonOverlayScrim`). Both this layer's callers —
  // `Modifier.balloon` and the `Balloon(...)` wrapper — therefore register the request here
  // rather than emitting the scrim themselves.
  val registry = LocalBalloonRegistry.current
  if (style.isVisibleOverlay) {
    checkNotNull(registry) {
      "A balloon with setIsVisibleOverlay(true) must be shown inside a BalloonHost { ... }: " +
        "the overlay scrim is drawn by the host so it can cover the whole window, which a " +
        "Popup cannot do. Wrap your screen in BalloonHost."
    }
    val request = remember(state) { BalloonOverlayRequest(state) }
    request.anchorBounds = anchorBounds
    DisposableEffect(registry, request) {
      registry.registerOverlay(request)
      onDispose { registry.unregisterOverlay(request) }
    }
  }

  // The anchor can leave the composition while the balloon is still showing (a LazyColumn
  // item scrolling out of the viewport, a screen being navigated away from). Without this
  // the popup would simply stop being composed while `isVisible` stayed true: `onDismiss`
  // would never fire, `await()` would hang forever, and the next `toggle()` would dismiss
  // instead of show. The Android reference does the same thing in `Modifier.balloon`'s
  // `onDispose`, which calls `BalloonComposeView.dispose()` -> `balloon.dismiss()`.
  DisposableEffect(state) {
    onDispose { state.dismiss() }
  }

  // Drive visibility through a transition state rather than gating the Popup directly
  // on `state.isVisible`, so both the enter and exit animations get to play.
  // Keyed on `state` so swapping the BalloonState at this call-site starts from a clean
  // transition rather than inheriting the previous balloon's animation progress.
  val visibleState = remember(state) { MutableTransitionState(false) }
  visibleState.targetState = state.isVisible
  val popupActive = visibleState.currentState || visibleState.targetState || !visibleState.isIdle

  if (popupActive && anchorBounds != null) {
    val offsetPx = with(density) {
      IntOffset(
        state.offset.x.roundToPx(),
        state.offset.y.roundToPx(),
      )
    }

    // The framework's `windowSize` is NOT in the same coordinate space as the anchor
    // rectangles: on Android it is derived from the popup window's own metrics and excludes
    // the system bars, while `boundsInWindow()` measures from the top of an edge-to-edge
    // window. Mixing them makes a balloon flip above its anchor although there is room
    // below, and makes the bottom strip of the window unreachable by the final clamp. The
    // container size is the app window's own size, i.e. exactly the space the anchor
    // rectangles live in, so we use that and ignore the framework's value.
    val windowSize = LocalWindowInfo.current.containerSize

    val positionProvider =
      remember(state.align, state.centerAlign, anchorBounds, offsetPx, style, windowSize) {
        BalloonPopupPositionProvider(
          state = state,
          anchorBounds = anchorBounds,
          align = state.align,
          centerAlign = state.centerAlign,
          userOffsetPx = offsetPx,
          windowSize = windowSize,
        )
      }

    // Prefer the orientation written back by the position provider (it accounts for
    // flips when the requested side has no room); fall back to the align-derived
    // orientation on the very first frame before the provider runs.
    val resolvedOrientation =
      state.resolvedArrowOrientation
        ?: resolveArrowOrientation(state.align, style, layoutDirection)

    Popup(
      popupPositionProvider = positionProvider,
      // Always provide a dismiss callback. PopupProperties decides which inputs
      // (back-press, outside-click) actually trigger it; gating onDismissRequest
      // here would suppress dismisses that the framework correctly invokes.
      onDismissRequest = { state.dismiss() },
      // Routed through expect/actual so the Skia targets can set
      // `usePlatformInsets = false`. Our provider positions in raw window
      // coordinates (from boundsInWindow), but skiko's Popup otherwise runs
      // providers in an inset-excluded space and re-adds the system-bar insets,
      // which would shift every balloon by the status-bar height on iOS.
      properties = balloonPopupProperties(
        // Its own knob, mirroring the original `Balloon.Builder.setFocusable` (default
        // true). Deriving it from `dismissOnBackPress` would be a trap: a balloon
        // configured with `setDismissWhenTouchOutside(false)` would still be focusable and
        // therefore touch-modal, which on iOS / Wasm (no back key) leaves the app with no
        // way to dismiss it and every touch swallowed.
        focusable = style.focusable,
        dismissOnBackPress = style.dismissOnBackPress,
        dismissOnClickOutside = style.dismissOnClickOutside,
      ),
    ) {
      AnimatedVisibility(
        visibleState = visibleState,
        enter = balloonEnterTransition(style.animation),
        exit = balloonExitTransition(style.animation),
      ) {
        BalloonContent(
          style = style,
          arrowOrientation = resolvedOrientation,
          // The provider writes resolvedArrowRatio = style.arrowPosition in the
          // ALIGN_BALLOON case, so reading it back is correct once it has run; before the
          // first placement pass of this show it is null and the configured position is
          // the right fallback.
          arrowPositionRatio = state.resolvedArrowRatio ?: style.arrowPosition,
          onClick = { state.dismiss() },
          content = { currentBalloonContent() },
        )
      }
    }
  }
}

/**
 * Resolves the effective [ArrowOrientation] for a given [align].
 *
 * If [BalloonStyle.arrowOrientation] is non-null it overrides the auto-derivation;
 * otherwise the orientation is the one that points back toward the anchor (e.g.
 * a balloon shown above the anchor has its arrow on the BOTTOM edge pointing
 * down at the anchor).
 *
 * For [BalloonAlign.CENTER] the arrow has no anchor edge to point at; we return
 * an arbitrary [ArrowOrientation.BOTTOM] for [BalloonContent] to render with —
 * users are expected to call `setIsVisibleArrow(false)` (or set
 * [BalloonStyle.isArrowVisible] to `false`) for a clean overlay. We deliberately
 * do NOT silently force `isArrowVisible = false` here because that would be a
 * surprising side-effect that violates the principle of explicit user intent.
 */
private fun resolveArrowOrientation(
  align: BalloonAlign,
  style: BalloonStyle,
  layoutDirection: LayoutDirection,
): ArrowOrientation {
  style.arrowOrientation?.let { return it }
  val isRtl = layoutDirection == LayoutDirection.Rtl
  return when (align) {
    BalloonAlign.TOP -> ArrowOrientation.BOTTOM
    BalloonAlign.BOTTOM -> ArrowOrientation.TOP
    // Balloon on the leading side -> arrow points back to the trailing side.
    BalloonAlign.START -> if (isRtl) ArrowOrientation.START else ArrowOrientation.END
    // Balloon on the trailing side -> arrow points back to the leading side.
    BalloonAlign.END -> if (isRtl) ArrowOrientation.END else ArrowOrientation.START
    // No meaningful arrow direction in overlay mode — caller hides the arrow.
    BalloonAlign.CENTER -> ArrowOrientation.BOTTOM
  }
}

/**
 * Helper to convert a `Rect` (window-pixel coordinates) into an [IntRect] using
 * [Float.roundToInt] on each edge. Mirrors the rounding the framework uses
 * internally for popup placement.
 */
internal fun androidx.compose.ui.geometry.Rect.toIntRect(): IntRect = IntRect(
  left = left.roundToInt(),
  top = top.roundToInt(),
  right = right.roundToInt(),
  bottom = bottom.roundToInt(),
)

/**
 * Computes the popup offset from the captured anchor bounds, the requested
 * alignment, the arrow size and the user-supplied offset, and writes back the
 * resolved arrow orientation / ratio onto [state] so [BalloonContent] can draw
 * the arrow against the FINAL on-screen placement.
 *
 * The math follows the same conventions used in the existing Android Balloon:
 * - TOP: balloon sits above the anchor; popup bottom-edge meets anchor top-edge,
 *   so y = anchor.top - popup.height.
 * - BOTTOM: balloon sits below; y = anchor.bottom.
 * - START/END (resolved against [layoutDirection]): horizontal placement relative
 *   to the anchor; vertical centering on the anchor's vertical axis.
 * - CENTER: either a dead-center overlay (when [centerAlign] is `null`) or, when
 *   [centerAlign] is set, placed adjacent to the anchor's center on that side
 *   (original `showAtCenter` parity).
 *
 * Placement automatically FLIPS to the opposite side when the requested side has
 * no room AND the opposite side does, flipping the arrow orientation to match
 * (Fix C). A final [coerceIn] clamp keeps the popup on-screen as a last resort.
 * The arrow is RE-ANCHORED against the final position: for `ALIGN_ANCHOR` (and
 * center-align) it points at the anchor; for `ALIGN_BALLOON` it stays at
 * [BalloonStyle.arrowPosition] (Fix B).
 */
internal class BalloonPopupPositionProvider(
  private val state: BalloonState,
  private val anchorBounds: IntRect,
  private val align: BalloonAlign,
  private val centerAlign: BalloonCenterAlign?,
  private val userOffsetPx: IntOffset,
  private val windowSize: IntSize,
) : PopupPositionProvider {

  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize,
  ): IntOffset {
    // Deliberately shadowing the framework's `windowSize` parameter — see the call site for
    // why the container size is the only one in the same coordinate space as the anchor.
    @Suppress("NAME_SHADOWING")
    val windowSize = this.windowSize
    // Note: the `anchorBounds` argument supplied by the framework is the bounds
    // of the *parent* of the Popup composable, which is not necessarily the
    // anchor we care about. We therefore use the captured [anchorBounds] from
    // construction time, which is precisely the anchor's window-rect.
    val captured = this.anchorBounds
    val style = state.style
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val popupW = popupContentSize.width
    val popupH = popupContentSize.height
    // `Balloon.calculateAlignOffset` halves every extent with `(x * 0.5f).roundToInt()`,
    // so we round the same way instead of using integer division — otherwise odd anchor
    // or popup sizes land one pixel off the View implementation.
    val halfAnchorW = (captured.width * 0.5f).roundToInt()
    val halfAnchorH = (captured.height * 0.5f).roundToInt()
    val halfPopupW = (popupW * 0.5f).roundToInt()
    val halfPopupH = (popupH * 0.5f).roundToInt()
    val anchorCenterX = captured.left + halfAnchorW
    val anchorCenterY = captured.top + halfAnchorH

    val maxX = (windowSize.width - popupW).coerceAtLeast(0)
    val maxY = (windowSize.height - popupH).coerceAtLeast(0)

    // ---- 1. Resolve the base placement + (possibly flipped) arrow orientation.
    val baseX: Int
    val baseY: Int
    // `orientation` is the geometry-derived arrow edge; `flipped` records whether
    // the requested side had to flip to the opposite side for lack of room.
    val orientation: ArrowOrientation
    var flipped = false

    if (align == BalloonAlign.CENTER && centerAlign != null) {
      // Original showAtCenter parity: place adjacent to the anchor CENTER. No
      // flip (clamp still applies). The arrow points back at the anchor center.
      when (centerAlign.resolveAbsolute(isRtl)) {
        AbsoluteBalloonAlign.TOP -> {
          baseX = anchorCenterX - halfPopupW
          baseY = anchorCenterY - popupH
          orientation = ArrowOrientation.BOTTOM
        }
        AbsoluteBalloonAlign.BOTTOM -> {
          baseX = anchorCenterX - halfPopupW
          baseY = anchorCenterY
          orientation = ArrowOrientation.TOP
        }
        AbsoluteBalloonAlign.LEFT -> {
          baseX = anchorCenterX - popupW
          baseY = anchorCenterY - halfPopupH
          orientation = if (isRtl) ArrowOrientation.START else ArrowOrientation.END
        }
        else -> { // RIGHT
          baseX = anchorCenterX
          baseY = anchorCenterY - halfPopupH
          orientation = if (isRtl) ArrowOrientation.END else ArrowOrientation.START
        }
      }
    } else {
      when (align.resolveAbsolute(isRtl)) {
        AbsoluteBalloonAlign.TOP -> {
          baseX = captured.left + halfAnchorW - halfPopupW
          // Requested above: flip BELOW when there's no room above but room below.
          if (captured.top - popupH < 0 && captured.bottom + popupH <= windowSize.height) {
            baseY = captured.bottom
            orientation = ArrowOrientation.TOP
            flipped = true
          } else {
            baseY = captured.top - popupH
            orientation = ArrowOrientation.BOTTOM
          }
        }
        AbsoluteBalloonAlign.BOTTOM -> {
          baseX = captured.left + halfAnchorW - halfPopupW
          // Requested below: flip ABOVE when there's no room below but room above.
          if (captured.bottom + popupH > windowSize.height && captured.top - popupH >= 0) {
            baseY = captured.top - popupH
            orientation = ArrowOrientation.BOTTOM
            flipped = true
          } else {
            baseY = captured.bottom
            orientation = ArrowOrientation.TOP
          }
        }
        AbsoluteBalloonAlign.LEFT -> {
          baseY = captured.top + captured.height - halfPopupH - halfAnchorH
          // Requested left: flip RIGHT when there's no room left but room right.
          if (captured.left - popupW < 0 && captured.right + popupW <= windowSize.width) {
            baseX = captured.right
            // Flipped to the right of the anchor -> arrow on the balloon's physical
            // LEFT edge (START in LTR, END in RTL).
            orientation = if (isRtl) ArrowOrientation.END else ArrowOrientation.START
            flipped = true
          } else {
            baseX = captured.left - popupW
            orientation = if (isRtl) ArrowOrientation.START else ArrowOrientation.END
          }
        }
        AbsoluteBalloonAlign.RIGHT -> {
          baseY = captured.top + captured.height - halfPopupH - halfAnchorH
          // Requested right: flip LEFT when there's no room right but room left.
          if (captured.right + popupW > windowSize.width && captured.left - popupW >= 0) {
            baseX = captured.left - popupW
            // Flipped to the left of the anchor -> arrow on the balloon's physical
            // RIGHT edge (END in LTR, START in RTL).
            orientation = if (isRtl) ArrowOrientation.START else ArrowOrientation.END
            flipped = true
          } else {
            baseX = captured.right
            orientation = if (isRtl) ArrowOrientation.END else ArrowOrientation.START
          }
        }
        else -> { // CENTER overlay (centerAlign == null): dead-center, no flip.
          baseX = captured.left + halfAnchorW - halfPopupW
          baseY = captured.top + captured.height - halfPopupH - halfAnchorH
          orientation = resolveArrowOrientation(align, style, layoutDirection)
        }
      }
    }

    // An explicitly pinned orientation wins UNLESS the placement actually flipped
    // (then the arrow must follow the balloon to keep pointing at the anchor).
    val pinned = style.arrowOrientation
    val resolvedOrientation = if (pinned != null && !flipped) pinned else orientation

    // ---- 2. Apply user offset, then clamp to keep the popup on-screen.
    val finalX = (baseX + userOffsetPx.x).coerceIn(0, maxX)
    val finalY = (baseY + userOffsetPx.y).coerceIn(0, maxY)

    // ---- 3. Re-anchor the arrow ratio against the FINAL placement.
    val absoluteSide = resolvedOrientation.resolve(layoutDirection)
    val ratio = when (absoluteSide) {
      ResolvedArrowSide.TOP, ResolvedArrowSide.BOTTOM -> {
        if (style.arrowPositionRules == ArrowPositionRules.ALIGN_ANCHOR ||
          (align == BalloonAlign.CENTER && centerAlign != null)
        ) {
          val anchorArrowX = captured.left + captured.width * style.arrowPosition
          if (popupW > 0) ((anchorArrowX - finalX) / popupW).coerceIn(0f, 1f) else 0.5f
        } else {
          style.arrowPosition
        }
      }
      ResolvedArrowSide.LEFT, ResolvedArrowSide.RIGHT -> {
        if (style.arrowPositionRules == ArrowPositionRules.ALIGN_ANCHOR ||
          (align == BalloonAlign.CENTER && centerAlign != null)
        ) {
          val anchorArrowY = captured.top + captured.height * style.arrowPosition
          if (popupH > 0) ((anchorArrowY - finalY) / popupH).coerceIn(0f, 1f) else 0.5f
        } else {
          style.arrowPosition
        }
      }
    }

    // ---- 4. Write back, guarding against recomposition loops (only on change),
    // mirroring the anchorBounds change-guard in the Balloon anchor composable.
    if (state.resolvedArrowOrientation != resolvedOrientation) {
      state.resolvedArrowOrientation = resolvedOrientation
    }
    if (state.resolvedArrowRatio != ratio) {
      state.resolvedArrowRatio = ratio
    }

    return IntOffset(x = finalX, y = finalY)
  }
}

/**
 * Absolute (LTR-resolved) version of [BalloonAlign] used inside the popup
 * position math so that it doesn't have to reason about RTL.
 */
private enum class AbsoluteBalloonAlign { TOP, BOTTOM, LEFT, RIGHT, CENTER }

private fun BalloonAlign.resolveAbsolute(isRtl: Boolean): AbsoluteBalloonAlign = when (this) {
  BalloonAlign.TOP -> AbsoluteBalloonAlign.TOP
  BalloonAlign.BOTTOM -> AbsoluteBalloonAlign.BOTTOM
  BalloonAlign.START -> if (isRtl) AbsoluteBalloonAlign.RIGHT else AbsoluteBalloonAlign.LEFT
  BalloonAlign.END -> if (isRtl) AbsoluteBalloonAlign.LEFT else AbsoluteBalloonAlign.RIGHT
  BalloonAlign.CENTER -> AbsoluteBalloonAlign.CENTER
}

/**
 * Absolute (LTR-resolved) placement side for a [BalloonCenterAlign]. Vertical
 * sides are direction-independent; START/END resolve against [isRtl].
 */
private fun BalloonCenterAlign.resolveAbsolute(isRtl: Boolean): AbsoluteBalloonAlign = when (this) {
  BalloonCenterAlign.TOP -> AbsoluteBalloonAlign.TOP
  BalloonCenterAlign.BOTTOM -> AbsoluteBalloonAlign.BOTTOM
  BalloonCenterAlign.START -> if (isRtl) AbsoluteBalloonAlign.RIGHT else AbsoluteBalloonAlign.LEFT
  BalloonCenterAlign.END -> if (isRtl) AbsoluteBalloonAlign.LEFT else AbsoluteBalloonAlign.RIGHT
}
