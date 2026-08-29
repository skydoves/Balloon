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

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behavioural tests for [BalloonState] — the visibility / alignment / listener state machine
 * that every balloon API funnels through. No Compose runtime is involved: snapshot state is
 * readable and writable outside composition, so these stay fast and deterministic.
 */
class BalloonStateTest {

  private fun stateOf(style: BalloonStyle = BalloonStyle()) = BalloonState(style)

  // ------------------------------------------------------------- show / dismiss

  @Test
  fun initialState_isHidden() {
    val state = stateOf()

    assertFalse(state.isVisible)
    assertFalse(state.isShowing)
    assertEquals(BalloonAlign.BOTTOM, state.align)
    assertEquals(DpOffset.Zero, state.offset)
  }

  @Test
  fun show_setsVisibilityAlignAndOffset() {
    val state = stateOf()
    state.show(BalloonAlign.TOP, xOffset = 4.dp, yOffset = (-8).dp)

    assertTrue(state.isVisible)
    assertEquals(BalloonAlign.TOP, state.align)
    assertEquals(DpOffset(4.dp, (-8).dp), state.offset)
  }

  @Test
  fun showConvenienceOverloads_selectTheRightAlign() {
    stateOf().apply { showAlignTop() }.let { assertEquals(BalloonAlign.TOP, it.align) }
    stateOf().apply { showAlignBottom() }.let { assertEquals(BalloonAlign.BOTTOM, it.align) }
    stateOf().apply { showAlignStart() }.let { assertEquals(BalloonAlign.START, it.align) }
    stateOf().apply { showAlignEnd() }.let { assertEquals(BalloonAlign.END, it.align) }
    stateOf().apply { showAsDropDown() }.let { assertEquals(BalloonAlign.DROP_DOWN, it.align) }
  }

  @Test
  fun dismiss_hidesAndFiresTheListenerExactlyOnce() {
    val state = stateOf()
    var dismissals = 0
    state.onDismiss = { dismissals++ }

    state.showAlignTop()
    state.dismiss()
    state.dismiss() // already hidden: a no-op

    assertFalse(state.isVisible)
    assertEquals(1, dismissals)
  }

  @Test
  fun dismiss_whileHidden_doesNotFireTheListener() {
    val state = stateOf()
    var dismissals = 0
    state.onDismiss = { dismissals++ }

    state.dismiss()

    assertEquals(0, dismissals)
  }

  // -------------------------------------------------------------------- toggle

  @Test
  fun toggle_alternatesVisibility() {
    val state = stateOf()

    state.toggle()
    assertTrue(state.isVisible)
    state.toggle()
    assertFalse(state.isVisible)
  }

  @Test
  fun toggle_afterShowAtCenter_keepsTheCenterPlacement() {
    // Regression: `toggle()` used to fall through to `show(BalloonAlign.CENTER)`, which is
    // the dead-centre overlay — silently losing the `showAtCenter` side.
    val state = stateOf()
    state.showAtCenter(BalloonCenterAlign.END)
    state.toggle() // dismiss
    state.toggle() // show again

    assertTrue(state.isVisible)
    assertEquals(BalloonAlign.CENTER, state.align)
    assertEquals(BalloonCenterAlign.END, state.centerAlign)
  }

  @Test
  fun show_afterShowAtCenter_clearsTheCenterAlign() {
    val state = stateOf()
    state.showAtCenter(BalloonCenterAlign.TOP)
    state.dismiss()
    state.showAlignBottom()

    assertNull(state.centerAlign)
  }

  // ----------------------------------------------------------- dismissWhenShowAgain

  @Test
  fun dismissWhenShowAgain_closesAnAlreadyVisibleBalloon() {
    val state = stateOf(BalloonStyle(dismissWhenShowAgain = true))

    state.showAlignTop()
    assertTrue(state.isVisible)
    state.showAlignTop()
    assertFalse(state.isVisible)
  }

  @Test
  fun dismissWhenShowAgain_offByDefault_reShowsInPlace() {
    val state = stateOf()

    state.showAlignTop()
    state.showAlignBottom()

    assertTrue(state.isVisible)
    assertEquals(BalloonAlign.BOTTOM, state.align)
  }

  @Test
  fun dismissWhenShowAgain_appliesToShowAtCenterToo() {
    val state = stateOf(BalloonStyle(dismissWhenShowAgain = true))

    state.showAtCenter(BalloonCenterAlign.TOP)
    state.showAtCenter(BalloonCenterAlign.TOP)

    assertFalse(state.isVisible)
  }

  // -------------------------------------------------------------------- update

  @Test
  fun update_movesAVisibleBalloonWithoutBumpingTheShowGeneration() {
    val state = stateOf()
    state.showAlignBottom()
    val generation = state.showGeneration

    state.update(BalloonAlign.TOP, xOffset = 5.dp, yOffset = 6.dp)

    assertEquals(BalloonAlign.TOP, state.align)
    assertEquals(DpOffset(5.dp, 6.dp), state.offset)
    // Not a fresh show: the auto-dismiss timer must not restart.
    assertEquals(generation, state.showGeneration)
  }

  @Test
  fun update_isANoOpWhileHidden() {
    val state = stateOf()
    state.update(BalloonAlign.TOP, xOffset = 5.dp)

    assertFalse(state.isVisible)
    assertEquals(BalloonAlign.BOTTOM, state.align)
    assertEquals(DpOffset.Zero, state.offset)
  }

  // ---------------------------------------------------------- resolved arrow reset

  @Test
  fun show_clearsThePreviousPlacementResolution() {
    val state = stateOf()
    state.showAlignTop()
    state.resolvedArrowOrientation = ArrowOrientation.BOTTOM
    state.resolvedArrowCenterPx = 42f
    state.dismiss()

    state.showAlignBottom()

    // Otherwise the first frame of the new show would draw the previous show's arrow.
    assertNull(state.resolvedArrowOrientation)
    assertNull(state.resolvedArrowCenterPx)
  }

  @Test
  fun showGeneration_advancesOnEveryShowEvenWhileVisible() {
    val state = stateOf()
    state.showAlignTop()
    val first = state.showGeneration
    state.showAlignTop()

    assertEquals(first + 1, state.showGeneration)
  }

  // --------------------------------------------------------------------- await

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun await_returnsImmediatelyWhenAlreadyHidden() = runTest {
    val state = stateOf()
    state.await() // must not hang
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun await_resumesOnDismiss() = runTest {
    val state = stateOf()
    state.showAlignTop()
    val resumed = CompletableDeferred<Unit>()

    val job = launch {
      state.await()
      resumed.complete(Unit)
    }
    advanceUntilIdle()
    assertFalse(resumed.isCompleted)

    state.dismiss()
    // `await` observes through `snapshotFlow`, which only sees a write once the global
    // snapshot is applied. Compose does that every frame; a plain unit test has to say so.
    Snapshot.sendApplyNotifications()
    advanceUntilIdle()

    assertTrue(resumed.isCompleted)
    job.cancel()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun awaitAlign_showsThenSuspendsUntilDismissed() = runTest {
    val state = stateOf()
    var finished = false

    val job = launch {
      state.awaitAlignEnd()
      finished = true
    }
    advanceUntilIdle()

    assertTrue(state.isVisible)
    assertEquals(BalloonAlign.END, state.align)
    assertFalse(finished)

    state.dismiss()
    Snapshot.sendApplyNotifications()
    advanceUntilIdle()
    assertTrue(finished)
    job.cancel()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun awaitAtCenter_keepsTheCenterAlign() = runTest {
    val state = stateOf()
    val job = launch { state.awaitAtCenter(BalloonCenterAlign.START) }
    advanceUntilIdle()

    assertEquals(BalloonAlign.CENTER, state.align)
    assertEquals(BalloonCenterAlign.START, state.centerAlign)

    state.dismiss()
    Snapshot.sendApplyNotifications()
    advanceUntilIdle()
    job.cancel()
  }

  // ------------------------------------------------------------ dismissWithDelay

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun dismissWithDelay_dismissesAfterTheDelay() = runTest {
    val state = stateOf()
    state.showAlignTop()

    assertTrue(state.dismissWithDelay(this, 300L))
    advanceTimeBy(299L)
    assertTrue(state.isVisible)

    advanceTimeBy(2L)
    assertFalse(state.isVisible)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun dismissWithDelay_returnsFalseWhenHidden() = runTest {
    val state = stateOf()
    assertFalse(state.dismissWithDelay(this, 100L))
  }

  // ------------------------------------------------------------------ listeners

  @Test
  fun clickListeners_defaultToNull() {
    val state = stateOf()

    assertNull(state.onBalloonClick)
    assertNull(state.onOverlayClick)
    assertNull(state.onDismiss)
  }

  // ---------------------------------------------------------------------- style

  @Test
  fun style_isMutableSoAnimatedStylesRestyleInPlace() {
    val state = stateOf()
    state.showAlignTop()
    state.style = BalloonStyle(cornerRadius = 20.dp)

    assertEquals(20.dp, state.style.cornerRadius)
    // Restyling must not disturb visibility.
    assertTrue(state.isVisible)
  }
}
