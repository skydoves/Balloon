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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
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
        "the overlay scrim is drawn by the host, because a Popup cannot cover the system " +
        "bars. Wrap your screen in BalloonHost, at the root and with Modifier.fillMaxSize() " +
        "if you want the scrim to dim the whole window."
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

  // The framework's `windowSize` is NOT in the same coordinate space as the anchor
  // rectangles: on Android it is derived from the popup window's own metrics and excludes
  // the system bars, while `boundsInWindow()` measures from the top of an edge-to-edge
  // window. Mixing them makes a balloon flip above its anchor although there is room below,
  // and makes the bottom strip of the window unreachable by the final clamp. The container
  // size is the app window's own size, i.e. exactly the space the anchor rectangles live
  // in, so we use that and ignore the framework's value.
  val windowSize = LocalWindowInfo.current.containerSize

  // An anchor can also be scrolled clean out of the window while the balloon is up. Left
  // alone the balloon would sit clamped against a window edge pointing at nothing, so it is
  // dismissed the moment its anchor is fully outside — the same outcome as the anchor
  // leaving the composition, just triggered by geometry.
  LaunchedEffect(anchorBounds, windowSize, state.isVisible) {
    val bounds = anchorBounds
    if (state.isVisible && bounds != null && !bounds.isEmpty &&
      (
        bounds.right <= 0 || bounds.bottom <= 0 ||
          bounds.left >= windowSize.width || bounds.top >= windowSize.height
        )
    ) {
      state.dismiss()
    }
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

    val positionProvider = remember(
      state.align,
      state.centerAlign,
      anchorBounds,
      offsetPx,
      style,
      windowSize,
      density,
    ) {
      BalloonPopupPositionProvider(
        state = state,
        anchorBounds = anchorBounds,
        align = state.align,
        centerAlign = state.centerAlign,
        userOffsetPx = offsetPx,
        windowSize = windowSize,
        density = density,
      )
    }

    // Prefer the orientation written back by the position provider (it accounts for
    // flips when the requested side has no room); fall back to the align-derived
    // orientation on the very first frame before the provider runs.
    val resolvedOrientation =
      state.resolvedArrowOrientation
        ?: resolveArrowOrientation(state.align, state.centerAlign, style)

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
        modifier = Modifier.semantics { balloon() },
        visibleState = visibleState,
        enter = balloonEnterTransition(style.animation),
        exit = balloonExitTransition(style.animation),
      ) {
        BalloonContent(
          style = style,
          arrowOrientation = resolvedOrientation,
          // Resolved by the position provider against the final placement. Before its first
          // pass of this show it is null, which centers the arrow for one frame.
          arrowCenterFromCardStart = state.resolvedArrowCenterPx,
          onClick = {
            state.onBalloonClick?.invoke()
            if (style.dismissWhenClicked) state.dismiss()
          },
          onMarginTap = { state.dismiss() },
          content = { currentBalloonContent() },
        )
      }
    }
  }
}

/**
 * Resolves the [ArrowOrientation] implied by an alignment, i.e. the edge whose arrow points
 * back at the anchor: a balloon shown above the anchor carries its arrow on the BOTTOM edge.
 *
 * An explicit [BalloonStyle.arrowOrientation] overrides the derivation. For
 * [BalloonAlign.CENTER] the side comes from [centerAlign] when the balloon was shown with
 * `showAtCenter`; a dead-center overlay (`centerAlign == null`) has no edge to point at, so
 * an arbitrary [ArrowOrientation.BOTTOM] is returned for [BalloonContent] to render with —
 * users are expected to call `setIsVisibleArrow(false)` for a clean overlay. We deliberately
 * do NOT silently force `isArrowVisible = false` here, because that would be a surprising
 * side-effect that violates the principle of explicit user intent.
 */
internal fun resolveArrowOrientation(
  align: BalloonAlign,
  centerAlign: BalloonCenterAlign?,
  style: BalloonStyle,
): ArrowOrientation {
  style.arrowOrientation?.let { return it }
  // Both [align] and [ArrowOrientation] are expressed in logical START/END terms, and
  // `ArrowOrientation.resolve` is what turns those into physical sides. So this mapping must
  // NOT consult the layout direction: doing so applies the mirroring twice and lands the
  // first frame's arrow on the wrong edge under RTL, disagreeing with the position provider
  // (which works from already-resolved absolute sides and therefore does need `isRtl`).
  if (align == BalloonAlign.CENTER) {
    return when (centerAlign) {
      BalloonCenterAlign.TOP -> ArrowOrientation.BOTTOM
      BalloonCenterAlign.BOTTOM -> ArrowOrientation.TOP
      BalloonCenterAlign.START -> ArrowOrientation.END
      BalloonCenterAlign.END -> ArrowOrientation.START
      // No meaningful arrow direction in overlay mode; the caller hides the arrow.
      null -> ArrowOrientation.BOTTOM
    }
  }
  return when (align) {
    BalloonAlign.TOP -> ArrowOrientation.BOTTOM
    BalloonAlign.BOTTOM -> ArrowOrientation.TOP
    // Balloon on the leading side -> arrow points back to the trailing side.
    BalloonAlign.START -> ArrowOrientation.END
    // Balloon on the trailing side -> arrow points back to the leading side.
    BalloonAlign.END -> ArrowOrientation.START
    BalloonAlign.DROP_DOWN -> ArrowOrientation.TOP
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
 * resolved arrow orientation / center onto [state] so [BalloonContent] can draw
 * the arrow against the FINAL on-screen placement.
 *
 * The math follows the same conventions as `Balloon.calculateAlignOffset` /
 * `calculateCenterOffset`, which pass their result to
 * `PopupWindow.showAsDropDown(anchor, xOff, yOff)` — i.e. an offset from the anchor's
 * bottom-left corner:
 * - TOP: balloon sits above the anchor; popup bottom-edge meets anchor top-edge,
 *   so y = anchor.top - popup.height.
 * - BOTTOM: balloon sits below; y = anchor.bottom.
 * - START/END (resolved against [layoutDirection]): horizontal placement relative
 *   to the anchor; vertical centering on the anchor's vertical axis.
 * - CENTER: either a dead-center overlay (when [centerAlign] is `null`) or, when
 *   [centerAlign] is set, placed adjacent to the anchor's center on that side
 *   (original `showAtCenter` parity).
 *
 * Two things the original leaves to `PopupWindow` are done here instead, because a
 * Compose `Popup` does neither: placement FLIPS to the opposite side when the requested
 * side has no room and the opposite side does (the arrow follows, unless
 * [ArrowOrientationRules.ALIGN_FIXED] pins it), and a final [coerceIn] keeps the popup
 * on-screen. The flip test includes the caller's offset, so a balloon pushed down by
 * `yOffset` flips on the room it will actually need.
 *
 * The arrow is then re-anchored against the final position, reproducing
 * `Balloon.getArrowConstraintPositionX` / `...Y` — including their `ALIGN_ANCHOR`
 * clamp band, which keeps the arrow `arrowWidth * arrowAlignAnchorPaddingRatio +
 * arrowAlignAnchorPadding` clear of the balloon's ends.
 */
internal class BalloonPopupPositionProvider(
  private val state: BalloonState,
  private val anchorBounds: IntRect,
  private val align: BalloonAlign,
  private val centerAlign: BalloonCenterAlign?,
  private val userOffsetPx: IntOffset,
  private val windowSize: IntSize,
  private val density: Density,
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
    val offX = userOffsetPx.x
    val offY = userOffsetPx.y

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
          if (captured.top - popupH + offY < 0 &&
            captured.bottom + popupH + offY <= windowSize.height
          ) {
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
          if (captured.bottom + popupH + offY > windowSize.height &&
            captured.top - popupH + offY >= 0
          ) {
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
          if (captured.left - popupW + offX < 0 &&
            captured.right + popupW + offX <= windowSize.width
          ) {
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
          if (captured.right + popupW + offX > windowSize.width &&
            captured.left - popupW + offX >= 0
          ) {
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
        AbsoluteBalloonAlign.DROP_DOWN -> {
          // Leading edges aligned rather than centered — `Balloon.showAsDropDown`.
          baseX = if (isRtl) captured.right - popupW else captured.left
          if (captured.bottom + popupH + offY > windowSize.height &&
            captured.top - popupH + offY >= 0
          ) {
            baseY = captured.top - popupH
            orientation = ArrowOrientation.BOTTOM
            flipped = true
          } else {
            baseY = captured.bottom
            orientation = ArrowOrientation.TOP
          }
        }
        else -> { // CENTER overlay (centerAlign == null): dead-center, no flip.
          baseX = captured.left + halfAnchorW - halfPopupW
          baseY = captured.top + captured.height - halfPopupH - halfAnchorH
          orientation = resolveArrowOrientation(align, centerAlign, style)
        }
      }
    }

    // A pinned orientation wins, except under the default ALIGN_ANCHOR rule when the
    // placement actually flipped — then the arrow must follow the balloon to keep pointing
    // at the anchor. ALIGN_FIXED keeps it wherever the caller put it.
    //
    // A flip never moves the arrow to a different AXIS, though: the arrow axis decides how
    // the popup reserves space, so the popup's own size would change, and that size is the
    // input this flip decision was made from. The two would chase each other across measure
    // passes. A pinned cross-axis arrow therefore stays where the caller put it, and only
    // the balloon moves.
    val pinned = style.arrowOrientation
    val resolvedOrientation = when {
      pinned == null -> orientation
      style.arrowOrientationRules == ArrowOrientationRules.ALIGN_FIXED -> pinned
      flipped && pinned.isVertical == orientation.isVertical -> orientation
      else -> pinned
    }

    // ---- 2. Apply user offset, then clamp to keep the popup on-screen.
    val finalX = (baseX + offX).coerceIn(0, maxX)
    val finalY = (baseY + offY).coerceIn(0, maxY)

    // ---- 3. Re-anchor the arrow against the FINAL placement, in card coordinates.
    val side = resolvedOrientation.resolve(layoutDirection)
    val arrowCenter = resolveArrowCenter(
      style = style,
      side = side,
      layoutDirection = layoutDirection,
      captured = captured,
      popupOrigin = IntOffset(finalX, finalY),
      popupSize = popupContentSize,
    )

    // ---- 4. Write back, guarding against recomposition loops (only on change),
    // mirroring the anchorBounds change-guard in the Balloon anchor composable.
    if (state.resolvedArrowOrientation != resolvedOrientation) {
      state.resolvedArrowOrientation = resolvedOrientation
    }
    if (state.resolvedArrowCenterPx != arrowCenter) {
      state.resolvedArrowCenterPx = arrowCenter
    }

    return IntOffset(x = finalX, y = finalY)
  }

  /**
   * Where the arrow's center belongs along [side], in pixels from the card's leading edge.
   *
   * Reproduces `Balloon.getArrowConstraintPositionX`, which works in the *wrapper's*
   * coordinate space (the popup minus its margins). The card sits `elevation` further in
   * than that wrapper on the axis the arrow runs along, so the wrapper result is shifted by
   * that inset on the way out — which is why an `ALIGN_BALLOON` position of `0.5f` lands
   * dead-centre while `0.25f` does *not* land at a quarter of the card.
   *
   * One deliberate departure: the original's Y-axis twin, `getArrowConstraintPositionY`, is
   * an older copy that lacks the two "the anchor fits inside the balloon" early returns, so
   * a side-aligned balloon snaps its arrow to the `minPosition` band instead of pointing at
   * the anchor. The X algorithm is used on both axes here.
   */
  private fun resolveArrowCenter(
    style: BalloonStyle,
    side: ResolvedArrowSide,
    layoutDirection: LayoutDirection,
    captured: IntRect,
    popupOrigin: IntOffset,
    popupSize: IntSize,
  ): Float {
    // The axis the arrow runs ALONG: horizontal for a TOP/BOTTOM arrow, vertical otherwise.
    val alongY = side == ResolvedArrowSide.LEFT || side == ResolvedArrowSide.RIGHT
    val reserve = style.reserve(side, density)
    val marginLead = with(density) {
      if (alongY) {
        style.margin.calculateTopPadding().toPx()
      } else {
        style.margin.calculateLeftPadding(layoutDirection).toPx()
      }
    }
    val marginTrail = with(density) {
      if (alongY) {
        style.margin.calculateBottomPadding().toPx()
      } else {
        style.margin.calculateRightPadding(layoutDirection).toPx()
      }
    }
    // Along its own axis the card is inset from the wrapper by the cross reserve on both
    // sides, whichever way the arrow points.
    val cardInset = with(density) { reserve.cross.toPx() }
    val arrowBase = with(density) { style.effectiveArrowSize.width.toPx() }
    val alignAnchorPad = with(density) { style.arrowAlignAnchorPadding.toPx() }

    val popupExtent = (if (alongY) popupSize.height else popupSize.width).toFloat()
    val anchorStart = (if (alongY) captured.top else captured.left).toFloat()
    val anchorExtent = (if (alongY) captured.height else captured.width).toFloat()
    val popupStart = (if (alongY) popupOrigin.y else popupOrigin.x).toFloat()
    val wrapperExtent = popupExtent - marginLead - marginTrail
    val wrapperStart = popupStart + marginLead

    val arrowHalf = arrowBase / 2f
    val wrapperArrowCenter = when (style.arrowPositionRules) {
      ArrowPositionRules.ALIGN_BALLOON -> wrapperExtent * style.arrowPosition

      ArrowPositionRules.ALIGN_ANCHOR -> {
        val minPosition = arrowBase * style.arrowAlignAnchorPaddingRatio + alignAnchorPad
        val maxPosition = popupExtent - minPosition - marginLead - marginTrail
        val tip = anchorStart + anchorExtent * style.arrowPosition
        val position = tip - arrowHalf - wrapperStart
        val left = when {
          // The anchor is entirely before / after the balloon: pin to the near end.
          anchorStart + anchorExtent < wrapperStart -> minPosition
          wrapperStart + popupExtent < anchorStart -> maxPosition
          // The anchor's arrow point is at or before the balloon's start.
          tip - arrowHalf <= wrapperStart -> 0f
          // The usual case — the anchor is no larger than the balloon, so the arrow can
          // point straight at it.
          anchorExtent <= popupExtent - marginLead - marginTrail -> position
          // A wider anchor: keep the arrow inside the `minPosition` band.
          position <= arrowBase * 2f -> minPosition
          position > popupExtent - arrowBase * 2f -> maxPosition
          else -> position
        }
        left + arrowHalf
      }
    }
    return wrapperArrowCenter - cardInset
  }
}

/**
 * Absolute (LTR-resolved) version of [BalloonAlign] used inside the popup
 * position math so that it doesn't have to reason about RTL.
 */
private enum class AbsoluteBalloonAlign { TOP, BOTTOM, LEFT, RIGHT, DROP_DOWN, CENTER }

private fun BalloonAlign.resolveAbsolute(isRtl: Boolean): AbsoluteBalloonAlign = when (this) {
  BalloonAlign.TOP -> AbsoluteBalloonAlign.TOP
  BalloonAlign.BOTTOM -> AbsoluteBalloonAlign.BOTTOM
  BalloonAlign.START -> if (isRtl) AbsoluteBalloonAlign.RIGHT else AbsoluteBalloonAlign.LEFT
  BalloonAlign.END -> if (isRtl) AbsoluteBalloonAlign.LEFT else AbsoluteBalloonAlign.RIGHT
  BalloonAlign.DROP_DOWN -> AbsoluteBalloonAlign.DROP_DOWN
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
