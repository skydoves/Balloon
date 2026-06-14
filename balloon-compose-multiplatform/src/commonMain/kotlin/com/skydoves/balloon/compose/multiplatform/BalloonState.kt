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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

/**
 * A state holder for managing balloon visibility, alignment and offset for the
 * Compose Multiplatform balloon implementation.
 *
 * Unlike the Android-only [com.skydoves.balloon.compose.BalloonState], this
 * implementation is fully Compose-state-driven: there is no underlying
 * `PopupWindow` or `View`, so toggling [isVisible] will recompose and either
 * mount or unmount the popup.
 *
 * Use [rememberBalloonState] to obtain an instance from a composable.
 *
 * Example:
 * ```
 * val balloonState = rememberBalloonState(style = balloonStyle)
 *
 * Balloon(
 *   state = balloonState,
 *   balloonContent = { Text("Tooltip content") },
 * ) {
 *   Text(
 *     text = "Click me",
 *     modifier = Modifier.clickable { balloonState.showAlignTop() },
 *   )
 * }
 * ```
 *
 * @property style The visual configuration applied when the balloon is shown.
 */
@Stable
public class BalloonState internal constructor(
  style: BalloonStyle,
) {

  /**
   * The visual configuration applied when the balloon is shown. Backed by
   * snapshot state so that style edits (e.g. an animated `backgroundColor`)
   * recompose an already-visible balloon. Updated by [rememberBalloonState]
   * whenever the caller passes a different [BalloonStyle].
   */
  public var style: BalloonStyle by mutableStateOf(style)
    internal set

  /**
   * Whether the balloon should currently be visible.
   *
   * This is mutated by [show], [dismiss] and [toggle]. It can be observed in
   * composition to drive enter / exit animations.
   */
  public var isVisible: Boolean by mutableStateOf(false)
    private set

  /**
   * The current alignment of the balloon relative to its anchor.
   *
   * Updated whenever [show] (or any of its convenience overloads) is called.
   */
  public var align: BalloonAlign by mutableStateOf(BalloonAlign.BOTTOM)
    internal set

  /**
   * Additional manual offset applied on top of the computed popup position.
   *
   * Updated whenever [show] (or any of its convenience overloads) is called.
   */
  public var offset: DpOffset by mutableStateOf(DpOffset.Zero)
    internal set

  /**
   * The anchor's bounds in window coordinates, captured by the [Balloon]
   * anchor composable via `onGloballyPositioned`. `null` until the anchor has
   * been laid out.
   */
  internal var anchorBounds: IntRect? by mutableStateOf(null)

  /**
   * The arrow orientation resolved by [BalloonPopupPositionProvider] after
   * computing the final on-screen placement (which may flip to the opposite
   * side when there is not enough room). `null` until the first placement pass,
   * in which case the caller falls back to the align-derived orientation.
   */
  internal var resolvedArrowOrientation: ArrowOrientation? by mutableStateOf(null)

  /**
   * The arrow position ratio (0f..1f) resolved by [BalloonPopupPositionProvider]
   * against the final on-screen placement. For `ALIGN_BALLOON` this stays at
   * [BalloonStyle.arrowPosition]; for `ALIGN_ANCHOR` (and center-align) the arrow
   * is re-anchored so it points at the anchor.
   */
  internal var resolvedArrowRatio: Float by mutableStateOf(0.5f)

  /**
   * When the balloon is shown via [showAtCenter] / [awaitAtCenter], the side of
   * the anchor's center the balloon is placed on. `null` for normal aligns and
   * for the dead-center overlay produced by `show(BalloonAlign.CENTER)`.
   */
  internal var centerAlign: BalloonCenterAlign? by mutableStateOf(null)

  /** Returns whether the balloon is currently showing. */
  public val isShowing: Boolean
    get() = isVisible

  /**
   * Listener invoked when the balloon transitions from visible to hidden via
   * [dismiss]. The listener is fired exactly once per visible -> hidden
   * transition; calling [dismiss] when the balloon is already hidden is a
   * no-op and will not re-fire the listener.
   *
   * Migration: this is a property on [BalloonState] rather than a
   * `Balloon.Builder` setter because [BalloonStyle] is an immutable
   * value-equal data class and storing lambdas inside it would break
   * structural equality.
   */
  public var onDismiss: (() -> Unit)? = null

  /**
   * Shows the balloon with the given [align] and optional [xOffset]/[yOffset].
   *
   * Use [BalloonAlign.CENTER] to render the balloon as a dead-center overlay on
   * top of the anchor (a KMP-only convenience — arrows are visually meaningless
   * in this mode). For the original 4-way center-align behavior (placing the
   * balloon adjacent to the anchor center with the arrow pointing at it), use
   * [showAtCenter].
   */
  public fun show(
    align: BalloonAlign = BalloonAlign.BOTTOM,
    xOffset: Dp = 0.dp,
    yOffset: Dp = 0.dp,
  ) {
    this.centerAlign = null
    this.align = align
    this.offset = DpOffset(xOffset, yOffset)
    this.isVisible = true
  }

  /** Shows the balloon above its anchor. */
  public fun showAlignTop(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    show(BalloonAlign.TOP, xOffset, yOffset)

  /** Shows the balloon below its anchor. */
  public fun showAlignBottom(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    show(BalloonAlign.BOTTOM, xOffset, yOffset)

  /** Shows the balloon to the leading side of its anchor. */
  public fun showAlignStart(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    show(BalloonAlign.START, xOffset, yOffset)

  /** Shows the balloon to the trailing side of its anchor. */
  public fun showAlignEnd(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    show(BalloonAlign.END, xOffset, yOffset)

  /**
   * Shows the balloon adjacent to the anchor's center on the given [centerAlign]
   * side, with the arrow pointing back at the anchor center. Mirrors the original
   * Android `Balloon.showAtCenter`.
   *
   * This differs from `show(BalloonAlign.CENTER)`, which renders a dead-center
   * overlay on top of the anchor (a KMP-only convenience with no meaningful arrow).
   */
  public fun showAtCenter(
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
    xOffset: Dp = 0.dp,
    yOffset: Dp = 0.dp,
  ) {
    this.centerAlign = centerAlign
    this.align = BalloonAlign.CENTER
    this.offset = DpOffset(xOffset, yOffset)
    this.isVisible = true
  }

  /**
   * Dismisses the balloon. If the balloon is already hidden this is a no-op
   * and [onDismiss] is NOT invoked.
   */
  public fun dismiss() {
    if (isVisible) {
      isVisible = false
      onDismiss?.invoke()
    }
  }

  /**
   * Toggles the balloon: if currently visible it is dismissed, otherwise it is
   * shown with the given [align] (defaulting to the most recent alignment).
   */
  public fun toggle(align: BalloonAlign = this.align) {
    if (isVisible) dismiss() else show(align)
  }

  /**
   * Suspends the caller until the balloon is dismissed (i.e. [isVisible]
   * becomes false). Returns immediately if the balloon is already hidden.
   */
  public suspend fun await() {
    if (!isVisible) return
    snapshotFlow { isVisible }.first { !it }
  }

  /**
   * Shows the balloon with [show], then suspends until it is dismissed.
   *
   * Use [BalloonAlign.CENTER] to render the balloon centered on top of the
   * anchor (overlay-style).
   */
  public suspend fun awaitAlign(
    align: BalloonAlign = BalloonAlign.BOTTOM,
    xOffset: Dp = 0.dp,
    yOffset: Dp = 0.dp,
  ) {
    show(align, xOffset, yOffset)
    await()
  }

  /** Shows the balloon above its anchor and suspends until it is dismissed. */
  public suspend fun awaitAlignTop(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    awaitAlign(BalloonAlign.TOP, xOffset, yOffset)

  /** Shows the balloon below its anchor and suspends until it is dismissed. */
  public suspend fun awaitAlignBottom(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    awaitAlign(BalloonAlign.BOTTOM, xOffset, yOffset)

  /** Shows the balloon to the leading side of its anchor and suspends until it is dismissed. */
  public suspend fun awaitAlignStart(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    awaitAlign(BalloonAlign.START, xOffset, yOffset)

  /** Shows the balloon to the trailing side of its anchor and suspends until it is dismissed. */
  public suspend fun awaitAlignEnd(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    awaitAlign(BalloonAlign.END, xOffset, yOffset)

  /**
   * Shows the balloon adjacent to the anchor's center on the given [centerAlign]
   * side (see [showAtCenter]) and suspends until it is dismissed.
   */
  public suspend fun awaitAtCenter(
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
    xOffset: Dp = 0.dp,
    yOffset: Dp = 0.dp,
  ) {
    showAtCenter(centerAlign, xOffset, yOffset)
    await()
  }
}

/**
 * Creates and remembers a [BalloonState] keyed only by [key].
 *
 * The state instance is captured once on first composition (or when [key]
 * changes), so `isVisible`, `align` and `offset` survive recompositions. The
 * [style] is re-applied on every recomposition: passing an updated [BalloonStyle]
 * (e.g. `style.copy(backgroundColor = animated)`) restyles the balloon in place
 * without resetting its visibility. To force a fresh state instance, change [key].
 */
@Composable
public fun rememberBalloonState(
  style: BalloonStyle,
  key: Any? = null,
): BalloonState = remember(key) { BalloonState(style) }.also { it.style = style }
