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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
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
  val style = state.style
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current
  val currentBalloonContent by rememberUpdatedState(balloonContent)

  // Local fast-path mirror of the anchor bounds. We also push it onto `state`
  // so the popup-position-provider can read it via state observation.
  val anchorBoundsState: MutableState<IntRect?> = remember(key) { mutableStateOf(null) }

  // Auto-dismiss after a configurable timeout. Keying on `state.isVisible` means
  // the timer restarts every time the balloon becomes visible. Cancellation of
  // this LaunchedEffect (e.g. visibility flips back to false early) cancels the
  // delay automatically.
  if (style.autoDismissMillis > 0L) {
    LaunchedEffect(state, state.isVisible) {
      if (state.isVisible) {
        delay(style.autoDismissMillis)
        if (state.isVisible) state.dismiss()
      }
    }
  }

  val anchorBounds = anchorBoundsState.value

  // Drive visibility through a transition state rather than gating the Popup
  // directly on `state.isVisible`. If the Popup were born already-visible the
  // enter transition would be skipped, and unmounting it on dismiss would skip
  // the exit transition. Keeping the Popup mounted while the transition runs
  // lets both animations play. See Fix A.
  val visibleState = remember { MutableTransitionState(false) }
  visibleState.targetState = state.isVisible
  val popupActive = visibleState.currentState || visibleState.targetState || !visibleState.isIdle

  Box(
    modifier = modifier.onGloballyPositioned { coordinates ->
      val newBounds = coordinates.boundsInWindow().toIntRect()
      // Avoid recomposition cascades: only push when bounds actually change.
      if (anchorBoundsState.value != newBounds) {
        anchorBoundsState.value = newBounds
        state.anchorBounds = newBounds
      }
    },
  ) {
    content()

    if (popupActive && anchorBounds != null) {
      val offsetPx = with(density) {
        IntOffset(
          state.offset.x.roundToPx(),
          state.offset.y.roundToPx(),
        )
      }

      val positionProvider =
        remember(state.align, state.centerAlign, anchorBounds, offsetPx, style) {
          BalloonPopupPositionProvider(
            state = state,
            anchorBounds = anchorBounds,
            align = state.align,
            centerAlign = state.centerAlign,
            userOffsetPx = offsetPx,
          )
        }

      // Prefer the orientation written back by the position provider (it accounts
      // for flips when the requested side has no room); fall back to the
      // align-derived orientation on the very first frame before the provider runs.
      val resolvedOrientation =
        state.resolvedArrowOrientation
          ?: resolveArrowOrientation(state.align, style, layoutDirection)

      // Pivot scale animations around the arrow edge so the balloon appears to grow
      // from / collapse toward its arrow. When the placement flips, the resolved
      // orientation already reflects the new edge, so the origin follows along.
      // The dead-center overlay (CENTER align with no center-align side) has no
      // arrow edge, so it falls back to the geometric center.
      val transformOrigin =
        remember(resolvedOrientation, state.align, state.centerAlign, layoutDirection) {
          if (state.align == BalloonAlign.CENTER && state.centerAlign == null) {
            transformOriginFor(BalloonAlign.CENTER)
          } else {
            transformOriginForArrow(resolvedOrientation, layoutDirection)
          }
        }

      Popup(
        popupPositionProvider = positionProvider,
        // Always provide a dismiss callback. PopupProperties decides which inputs
        // (back-press, outside-click) actually trigger it; gating onDismissRequest
        // here would suppress dismisses that the framework correctly invokes.
        onDismissRequest = { state.dismiss() },
        properties = PopupProperties(
          // Focusable is required on Android for back-press to be captured by
          // the Popup. We tie it to dismissOnBackPress so popups that don't need
          // to dismiss on back-press don't steal IME / D-pad focus.
          focusable = style.dismissOnBackPress,
          dismissOnBackPress = style.dismissOnBackPress,
          dismissOnClickOutside = style.dismissOnClickOutside,
        ),
      ) {
        AnimatedVisibility(
          visibleState = visibleState,
          enter = balloonEnterTransition(
            animation = style.animation,
            durationMillis = style.animationDurationMillis,
            transformOrigin = transformOrigin,
          ),
          exit = balloonExitTransition(
            animation = style.animation,
            durationMillis = style.animationDurationMillis,
            transformOrigin = transformOrigin,
          ),
        ) {
          BalloonContent(
            style = style,
            arrowOrientation = resolvedOrientation,
            // The provider writes resolvedArrowRatio = style.arrowPosition in the
            // ALIGN_BALLOON case, so always reading it back is correct.
            arrowPositionRatio = state.resolvedArrowRatio,
            content = { currentBalloonContent() },
          )
        }
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
private fun androidx.compose.ui.geometry.Rect.toIntRect(): IntRect = IntRect(
  left = left.roundToInt(),
  top = top.roundToInt(),
  right = right.roundToInt(),
  bottom = bottom.roundToInt(),
)

/**
 * Computes the [TransformOrigin] that scale-based transitions should pivot around
 * for a balloon whose (possibly flipped) arrow sits on the edge described by
 * [orientation]. The origin lands on that arrow edge so the balloon appears to
 * grow from / collapse toward its arrow regardless of any placement flip.
 */
private fun transformOriginForArrow(
  orientation: ArrowOrientation,
  layoutDirection: LayoutDirection,
): TransformOrigin = when (orientation.resolve(layoutDirection)) {
  ResolvedArrowSide.TOP -> TransformOrigin(0.5f, 0f)
  ResolvedArrowSide.BOTTOM -> TransformOrigin(0.5f, 1f)
  ResolvedArrowSide.LEFT -> TransformOrigin(0f, 0.5f)
  ResolvedArrowSide.RIGHT -> TransformOrigin(1f, 0.5f)
}

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
) : PopupPositionProvider {

  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize,
  ): IntOffset {
    // Note: the `anchorBounds` argument supplied by the framework is the bounds
    // of the *parent* of the Popup composable, which is not necessarily the
    // anchor we care about. We therefore use the captured [anchorBounds] from
    // construction time, which is precisely the anchor's window-rect.
    val captured = this.anchorBounds
    val style = state.style
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val popupW = popupContentSize.width
    val popupH = popupContentSize.height
    val anchorCenterX = captured.left + captured.width / 2
    val anchorCenterY = captured.top + captured.height / 2

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
          baseX = anchorCenterX - popupW / 2
          baseY = anchorCenterY - popupH
          orientation = ArrowOrientation.BOTTOM
        }
        AbsoluteBalloonAlign.BOTTOM -> {
          baseX = anchorCenterX - popupW / 2
          baseY = anchorCenterY
          orientation = ArrowOrientation.TOP
        }
        AbsoluteBalloonAlign.LEFT -> {
          baseX = anchorCenterX - popupW
          baseY = anchorCenterY - popupH / 2
          orientation = if (isRtl) ArrowOrientation.START else ArrowOrientation.END
        }
        else -> { // RIGHT
          baseX = anchorCenterX
          baseY = anchorCenterY - popupH / 2
          orientation = if (isRtl) ArrowOrientation.END else ArrowOrientation.START
        }
      }
    } else {
      when (align.resolveAbsolute(isRtl)) {
        AbsoluteBalloonAlign.TOP -> {
          baseX = captured.left + (captured.width - popupW) / 2
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
          baseX = captured.left + (captured.width - popupW) / 2
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
          baseY = captured.top + (captured.height - popupH) / 2
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
          baseY = captured.top + (captured.height - popupH) / 2
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
          baseX = captured.left + (captured.width - popupW) / 2
          baseY = captured.top + (captured.height - popupH) / 2
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
