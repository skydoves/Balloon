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
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Layouts and interactions that were reported as broken — the first two against the 1.x
 * implementations, the rest against 2.0.0.
 *
 * The rewrite makes the 1.x pair structural rather than incidental, but "structural" is exactly
 * the kind of claim that quietly stops being true, and every case here cost a user a bug report
 * once already. They are cheap to keep.
 */
class ReportedScenarioTest {

  /**
   * A height on the anchor's PARENT must not clamp the balloon.
   *
   * Reported as #952 against `balloon-compose`, which measured the body against
   * `constraints.maxHeight`, so wrapping the anchor in a 44dp-tall Box gave a 44dp-tall balloon.
   * Here the body lives in a `Popup` and is measured against the window, and `setHeight` maps to
   * `requiredHeight`, so neither the anchor's parent nor an incoming constraint can squeeze it.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun aHeightOnTheAnchorsParentDoesNotClampTheBalloon() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      BalloonHost(modifier = Modifier.fillMaxSize()) {
        state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
        Box(Modifier.height(44.dp).width(80.dp)) {
          Balloon(
            state = state,
            balloonContent = { Box(Modifier.size(120.dp, 200.dp).testTag("body")) },
          ) { Box(Modifier.size(40.dp).testTag("anchor")) }
        }
      }
    }
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()

    val body = onNodeWithTag("body").fetchSemanticsNode().size
    assertEquals(200, body.height, "the 44dp parent must not clamp the balloon body")
    assertEquals(120, body.width, "nor the 80dp parent width")
  }

  /**
   * An anchor inside a `Dialog` still gets a balloon.
   *
   * Reported as #918, where `balloon-compose` crashed casting the dialog's layout params to
   * `FrameLayout.LayoutParams`, and then, once that no longer crashed, showed nothing at all.
   * There are no layout params to cast here, but a dialog is still its own window, so this
   * checks the balloon is really composed rather than just flagged visible.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun anAnchorInsideADialogStillGetsABalloon() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      Dialog(onDismissRequest = {}) {
        BalloonHost {
          state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
          Balloon(
            state = state,
            balloonContent = { Box(Modifier.size(120.dp, 40.dp).testTag("body")) },
          ) { Box(Modifier.size(40.dp).testTag("anchor")) }
        }
      }
    }
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()

    assertTrue(state.isVisible)
    onNodeWithTag("body").assertIsDisplayed()
  }

  // ------------------------------------------------------- #1022: scrolled-away anchors

  /**
   * An anchor scrolled out of a scrolling container must not strand the balloon at the
   * window's origin.
   *
   * Reported as #1022: the balloon's arrow jumped to the top-left corner of the window as soon
   * as the anchor was scrolled off-screen. The anchor bounds were captured with
   * `boundsInWindow()`, which clips, and clipping an anchor away entirely yields `Rect.Zero` —
   * so the balloon was positioned against a 0 x 0 anchor at the window origin, and the
   * "anchor left the window" dismissal never fired because it skipped empty rects.
   * [toBalloonAnchor] now reports the anchor's true rect plus a separate verdict on whether it
   * is still on screen.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun anAnchorScrolledOutOfAScrollingColumnDismissesTheBalloon() = runComposeUiTest {
    lateinit var state: BalloonState
    val scroll = ScrollState(0)
    setContent {
      Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Spacer(Modifier.height(600.dp))
        state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
        ScrollAnchoredBalloon(state)
        Spacer(Modifier.height(2000.dp))
      }
    }
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag("body").assertIsDisplayed()

    // Far enough that the anchor is well clear of the top of the window.
    runOnUiThread { scroll.dispatchRawDelta(2000f) }
    waitForIdle()

    assertFalse(state.isVisible, "a balloon whose anchor scrolled away must dismiss itself")
    assertEquals(0, nodeCount("body"), "and its body must not be left sitting in the window")
  }

  /**
   * A balloon points at where its anchor really is, even while a scrolling container is
   * clipping part of the anchor away.
   *
   * The other half of #1022, and the half that is invisible in a full-window scroll: with
   * clipped bounds a half-scrolled anchor reports the visible remainder, so the balloon drifts
   * off the anchor's real edge as it scrolls rather than staying glued to it. A 200dp viewport
   * sitting 300dp down the window keeps the arithmetic clear of the final on-screen clamp,
   * which would otherwise mask the difference.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun aBalloonTracksItsAnchorsRealEdgeWhileTheAnchorIsPartlyClipped() = runComposeUiTest {
    lateinit var state: BalloonState
    val scroll = ScrollState(0)
    setContent {
      Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(300.dp))
        Column(Modifier.height(200.dp).verticalScroll(scroll)) {
          state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
          ScrollAnchoredBalloon(state)
          Spacer(Modifier.height(1000.dp))
        }
      }
    }
    runOnUiThread { state.showAlignTop() }
    waitForIdle()
    // The gap the balloon holds off its anchor: the body tag is on the content box, which sits
    // inside the card's margin and the space reserved for the arrow. Its exact value is the
    // shape suite's business — all that matters here is that scrolling does not change it.
    val gapWhenFullyVisible = anchorTopMinusBodyBottom()

    // Scroll the anchor half out of the top of the viewport: its real top edge is now 30dp
    // above the viewport, while the part of it that survives clipping starts at the viewport.
    runOnUiThread { scroll.dispatchRawDelta(30f) }
    waitForIdle()

    assertEquals(
      gapWhenFullyVisible,
      anchorTopMinusBodyBottom(),
      absoluteTolerance = 0.5f,
      message = "the balloon should stay the same distance off the anchor's real top edge " +
        "once the anchor is partly clipped, not follow the edge of the clipping viewport",
    )
  }

  /**
   * An anchor clipped away by a scrolling container it sits inside still dismisses the balloon,
   * even though the anchor never leaves the window.
   *
   * This is the case the old geometry test could not see at all: it compared the anchor rect
   * against the window, and an anchor scrolled out of a 200dp viewport in the middle of the
   * screen is still very much inside the window. It is also the one place where a
   * `Modifier.verticalScroll` column behaves unlike a `LazyColumn`, which disposes the item and
   * so has always dismissed through `onDispose`.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun anAnchorClippedAwayWithoutLeavingTheWindowAlsoDismissesTheBalloon() = runComposeUiTest {
    lateinit var state: BalloonState
    val scroll = ScrollState(0)
    setContent {
      Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(300.dp))
        Column(Modifier.height(200.dp).verticalScroll(scroll)) {
          state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
          ScrollAnchoredBalloon(state)
          Spacer(Modifier.height(1000.dp))
        }
      }
    }
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag("body").assertIsDisplayed()

    // Past the anchor's own height, so nothing of it survives the viewport's clip — but the
    // anchor's real rect is still inside the window, 150dp down from the top.
    runOnUiThread { scroll.dispatchRawDelta(150f) }
    waitForIdle()

    assertTrue(
      onNodeWithTag("anchor").getUnclippedBoundsInRoot().top.value > 0f,
      "the anchor should still be within the window for this to test what it claims",
    )
    assertFalse(state.isVisible, "a balloon whose anchor was clipped away must dismiss itself")
    assertEquals(0, nodeCount("body"))
  }

  /**
   * The boundary of that dismissal: an anchor is allowed to arrive late.
   *
   * `AnimatedVisibility(enter = expandVertically())` clips its content to nothing on the frame
   * the animation starts, so "the anchor is clipped away" is also true of an anchor that has not
   * appeared yet. A balloon shown in that frame must wait for it — dismissing would be
   * permanent, leaving `show()` looking like it did nothing at all.
   */
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun aBalloonWaitsForAnAnchorThatIsStillAnimatingIn() = runComposeUiTest {
    lateinit var state: BalloonState
    var revealed by mutableStateOf(false)
    setContent {
      Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(200.dp))
        state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
        AnimatedVisibility(
          visible = revealed,
          enter = expandVertically(animationSpec = tween(durationMillis = 400)),
        ) { ScrollAnchoredBalloon(state) }
      }
    }

    mainClock.autoAdvance = false
    // The anchor begins appearing and the balloon is shown in the very same frame.
    runOnUiThread {
      revealed = true
      state.showAlignBottom()
    }
    mainClock.advanceTimeByFrame()
    mainClock.advanceTimeByFrame()
    assertTrue(state.isVisible, "the balloon must not dismiss itself while its anchor expands in")

    mainClock.advanceTimeBy(500)
    assertTrue(state.isVisible, "and must still be up once the anchor has finished appearing")
    onNodeWithTag("body").assertIsDisplayed()
  }

  /** How far the balloon body sits above the anchor's real (unclipped) top edge. */
  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.anchorTopMinusBodyBottom(): Float =
    onNodeWithTag("anchor").getUnclippedBoundsInRoot().top.value -
      onNodeWithTag("body").getUnclippedBoundsInRoot().bottom.value

  @Composable
  private fun ScrollAnchoredBalloon(state: BalloonState) {
    Balloon(
      state = state,
      balloonContent = { Box(Modifier.size(120.dp, 40.dp).testTag("body")) },
    ) { Box(Modifier.size(60.dp).testTag("anchor")) }
  }
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.nodeCount(tag: String): Int =
  onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().size
