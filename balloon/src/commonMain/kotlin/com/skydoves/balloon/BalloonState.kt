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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
   * When the balloon is shown via [showAtCenter] / [awaitAtCenter], the side of
   * the anchor's center the balloon is placed on. `null` for normal aligns and
   * for the dead-center overlay produced by `show(BalloonAlign.CENTER)`.
   */
  internal var centerAlign: BalloonCenterAlign? by mutableStateOf(null)

  /**
   * Monotonically increasing counter bumped on every [show] / [showAtCenter]
   * call — even when the balloon is already visible. The auto-dismiss timer
   * keys on this so re-showing an already-visible balloon restarts the timeout
   * instead of letting the original countdown run to completion.
   */
  internal var showGeneration: Int by mutableStateOf(0)
    private set

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
   * Listener invoked when the balloon body is tapped, mirroring
   * `Balloon.Builder.setOnBalloonClickListener`. It fires before any dismissal caused by
   * [BalloonStyle.dismissWhenClicked].
   */
  public var onBalloonClick: (() -> Unit)? = null

  /**
   * Listener invoked when the overlay scrim is tapped, mirroring
   * `Balloon.Builder.setOnBalloonOverlayClickListener`. It fires before any dismissal
   * caused by [BalloonStyle.dismissWhenOverlayClicked].
   */
  public var onOverlayClick: (() -> Unit)? = null

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
    // `setDismissWhenShowAgain` parity: showing an already-visible balloon closes it.
    if (isVisible && style.dismissWhenShowAgain) {
      dismiss()
      return
    }
    this.centerAlign = null
    this.align = align
    this.offset = DpOffset(xOffset, yOffset)
    // Bumping the generation also re-keys the popup's `BalloonArrowPlacement`, so the first
    // frame of this show falls back to the align-derived orientation and a centred arrow
    // instead of briefly drawing the arrow where the PREVIOUS show resolved to.
    this.showGeneration++
    this.isVisible = true
  }

  /**
   * Shows the balloon directly below its anchor with its leading edges aligned, mirroring
   * the original `Balloon.showAsDropDown`. Unlike [showAlignBottom] the balloon is not
   * centered on the anchor: it starts where the anchor starts, plus the given offsets.
   */
  public fun showAsDropDown(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    show(BalloonAlign.DROP_DOWN, xOffset, yOffset)

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
    if (isVisible && style.dismissWhenShowAgain) {
      dismiss()
      return
    }
    this.centerAlign = centerAlign
    this.align = BalloonAlign.CENTER
    this.offset = DpOffset(xOffset, yOffset)
    this.showGeneration++
    this.isVisible = true
  }

  /**
   * Moves an already-visible balloon without re-running its enter animation or restarting
   * its auto-dismiss timer — the counterpart of `Balloon.update` / `updateAlign*`.
   *
   * Does nothing when the balloon is hidden. Pass [align] to move it to a different side of
   * the anchor, or leave it out to only change the offsets.
   */
  public fun update(
    align: BalloonAlign = this.align,
    xOffset: Dp = 0.dp,
    yOffset: Dp = 0.dp,
  ) {
    if (!isVisible) return
    // Moving to any align other than the one `showAtCenter` set has to drop that side, or a
    // later `update(BalloonAlign.CENTER)` would silently keep placing the balloon against the
    // anchor's centre instead of producing the dead-centre overlay it documents.
    if (align != BalloonAlign.CENTER) centerAlign = null
    this.align = align
    this.offset = DpOffset(xOffset, yOffset)
  }

  /**
   * Dismisses the balloon after [delayMillis], mirroring `Balloon.dismissWithDelay`.
   * Returns `false` (and schedules nothing) when the balloon is not showing.
   *
   * The delay runs on the caller's [scope], so it is cancelled with it.
   */
  public fun dismissWithDelay(
    scope: CoroutineScope,
    delayMillis: Long,
  ): Boolean {
    if (!isVisible) return false
    pendingDismiss?.cancel()
    pendingDismiss = scope.launch {
      delay(delayMillis)
      dismiss()
    }
    return true
  }

  /**
   * The job scheduled by [dismissWithDelay], kept so it can be cancelled.
   *
   * Without this a pending dismissal outlives the balloon it was scheduled for: dismiss at
   * one second, show again at two, and the delayed job still fires at five and closes a
   * balloon it knows nothing about. The View implementation removed its callback in
   * `dismiss()` for the same reason.
   */
  private var pendingDismiss: Job? = null

  /**
   * Monotonically increasing counter bumped each time the balloon actually
   * transitions from visible to hidden. [await] observes this (not just [isVisible])
   * so a same-frame dismiss→show — where `snapshotFlow` would conflate the
   * intermediate `false` away — still resumes the awaiting coroutine.
   */
  internal var dismissGeneration: Int by mutableStateOf(0)
    private set

  /**
   * Dismisses the balloon. If the balloon is already hidden this is a no-op
   * and [onDismiss] is NOT invoked.
   */
  public fun dismiss() {
    pendingDismiss?.cancel()
    pendingDismiss = null
    if (isVisible) {
      isVisible = false
      dismissGeneration++
      onDismiss?.invoke()
    }
  }

  /**
   * Toggles the balloon: if currently visible it is dismissed, otherwise it is
   * shown with the given [align] (defaulting to the most recent alignment).
   */
  public fun toggle(align: BalloonAlign = this.align) {
    if (isVisible) {
      dismiss()
      return
    }
    // Preserve a `showAtCenter` placement instead of silently degrading it to the
    // dead-centre overlay that `show(BalloonAlign.CENTER)` produces.
    val center = centerAlign
    if (align == BalloonAlign.CENTER && center != null) showAtCenter(center) else show(align)
  }

  /**
   * Suspends the caller until the balloon is dismissed (i.e. [isVisible]
   * becomes false). Returns immediately if the balloon is already hidden.
   */
  public suspend fun await() {
    if (!isVisible) return
    val startGeneration = dismissGeneration
    // Observe a monotonic dismiss counter alongside `isVisible`: a same-frame
    // dismiss→show conflates the intermediate `false` away in snapshotFlow, but the
    // counter still advances, so the awaiter resumes on the dismiss that occurred.
    snapshotFlow { !isVisible || dismissGeneration != startGeneration }.first { it }
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

  /**
   * Shows the balloon as a drop-down under its anchor (see [showAsDropDown]) and suspends
   * until it is dismissed.
   */
  public suspend fun awaitAsDropDown(xOffset: Dp = 0.dp, yOffset: Dp = 0.dp): Unit =
    awaitAlign(BalloonAlign.DROP_DOWN, xOffset, yOffset)

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
