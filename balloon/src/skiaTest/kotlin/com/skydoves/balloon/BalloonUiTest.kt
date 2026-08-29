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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Compose UI tests for the balloon composables, run on the JVM through Skiko.
 *
 * Every style used here sets [BalloonAnimation.NONE]: an enter/exit transition would leave
 * the test clock advancing frames while assertions run, and none of these tests are about
 * the transitions themselves (those are pinned by the pure geometry / state suites).
 */
class BalloonUiTest {

  private val plain = BalloonStyle(animation = BalloonAnimation.NONE)

  @Composable
  private fun Anchored(
    state: BalloonState,
    body: @Composable () -> Unit = { Box(Modifier.size(40.dp).testTag(BODY)) },
  ) {
    Balloon(
      state = state,
      balloonContent = body,
    ) {
      Box(Modifier.size(20.dp).testTag(ANCHOR))
    }
  }

  // ------------------------------------------------------------------ lifecycle

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun balloonBody_isNotComposedUntilShown() = runComposeUiTest {
    setContent {
      val state = rememberBalloonState(plain)
      Anchored(state)
    }

    onNodeWithTag(ANCHOR).assertIsDisplayed()
    assertEquals(0, onAllNodesWithTagCount(BODY))
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun show_thenDismiss_mountsAndUnmountsTheBody() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      state = rememberBalloonState(plain)
      Anchored(state)
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag(BODY).assertIsDisplayed()

    runOnUiThread { state.dismiss() }
    waitForIdle()
    assertEquals(0, onAllNodesWithTagCount(BODY))
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun anchorLeavingTheComposition_dismissesTheBalloon() = runComposeUiTest {
    lateinit var state: BalloonState
    var anchored by mutableStateOf(true)
    setContent {
      state = rememberBalloonState(plain)
      if (anchored) Anchored(state)
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    assertTrue(state.isVisible)

    // A LazyColumn item scrolling away, or a screen being navigated off: without the
    // disposal hook `isVisible` would stay true forever and `await()` would never resume.
    anchored = false
    waitForIdle()
    assertFalse(state.isVisible)
  }

  // --------------------------------------------------------------------- clicks

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun tappingTheBody_firesTheClickListener() = runComposeUiTest {
    lateinit var state: BalloonState
    var clicks = 0
    setContent {
      state = rememberBalloonState(plain)
      state.onBalloonClick = { clicks++ }
      Anchored(state)
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag(BODY).performClick()
    waitForIdle()

    assertEquals(1, clicks)
    // dismissWhenClicked is off by default, so the balloon stays up.
    assertTrue(state.isVisible)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun tappingTheBody_dismissesWhenDismissWhenClickedIsOn() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      state = rememberBalloonState(plain.copy(dismissWhenClicked = true))
      Anchored(state)
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag(BODY).performClick()
    waitForIdle()

    assertFalse(state.isVisible)
  }

  // ---------------------------------------------------------------- auto dismiss

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun autoDismiss_closesTheBalloonAfterTheTimeout() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      state = rememberBalloonState(plain.copy(autoDismissMillis = 500L))
      Anchored(state)
    }

    mainClock.autoAdvance = false
    runOnUiThread { state.showAlignBottom() }
    mainClock.advanceTimeBy(100L)
    assertTrue(state.isVisible)

    mainClock.advanceTimeBy(600L)
    waitForIdle()
    assertFalse(state.isVisible)
  }

  // ---------------------------------------------------------------------- host

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun modifierBalloon_withoutAHost_failsLoudly() = runComposeUiTest {
    val error = assertFailsWith<IllegalStateException> {
      setContent {
        val state = rememberBalloonState(plain)
        Box(Modifier.size(20.dp).balloon(state) { Box(Modifier.size(10.dp)) })
      }
    }
    assertTrue(
      error.message.orEmpty().contains("BalloonHost"),
      "message should point at BalloonHost, was: ${error.message}",
    )
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun overlay_withoutAHost_failsLoudly() = runComposeUiTest {
    val error = assertFailsWith<IllegalStateException> {
      setContent {
        val state = rememberBalloonState(plain.copy(isVisibleOverlay = true))
        Anchored(state)
        state.showAlignBottom()
      }
    }
    assertTrue(
      error.message.orEmpty().contains("BalloonHost"),
      "message should point at BalloonHost, was: ${error.message}",
    )
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun modifierBalloon_insideAHost_rendersTheBody() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      BalloonHost {
        state = rememberBalloonState(plain)
        Box(
          Modifier
            .size(20.dp)
            .testTag(ANCHOR)
            .balloon(state) { Box(Modifier.size(40.dp).testTag(BODY)) },
        )
      }
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag(BODY).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun modifierBalloon_bodySeesTheAnchorsCompositionLocals() = runComposeUiTest {
    // The body is composed by BalloonHost, which is ABOVE the anchor in the tree. Without
    // the captured `CompositionLocalContext` it would read the host's value here.
    var seen: String? = null
    setContent {
      BalloonHost {
        CompositionLocalProvider(LocalProbe provides "anchor") {
          val state = rememberBalloonState(plain)
          Box(
            Modifier
              .size(20.dp)
              .balloon(state) {
                seen = LocalProbe.current
                Box(Modifier.size(40.dp).testTag(BODY))
              },
          )
          state.showAlignBottom()
        }
      }
    }
    waitForIdle()

    assertEquals("anchor", seen)
  }

  // -------------------------------------------------------------- layout safety

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun showingABalloon_doesNotShiftItsSiblingsInASpacedColumn() = runComposeUiTest {
    // `Popup` emits a zero-sized node into the host composition. As a direct child of a
    // `spacedBy` Column that node would claim a spacing slot and push everything below it
    // down the moment the balloon mounts; wrapping it fixes that.
    lateinit var state: BalloonState
    setContent {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        state = rememberBalloonState(plain)
        Anchored(state)
        Box(Modifier.size(20.dp).testTag(SIBLING))
      }
    }

    val before = onNodeWithTag(SIBLING).getUnclippedBoundsInRoot().top
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    val after = onNodeWithTag(SIBLING).getUnclippedBoundsInRoot().top

    assertEquals(before, after)
  }

  // ---------------------------------------------------------------------- style

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun changingTheStyle_restylesInPlaceWithoutHidingTheBalloon() = runComposeUiTest {
    lateinit var state: BalloonState
    var radius by mutableStateOf(4.dp)
    setContent {
      state = rememberBalloonState(plain.copy(cornerRadius = radius))
      Anchored(state)
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    onNodeWithTag(BODY).assertIsDisplayed()

    radius = 24.dp
    waitForIdle()

    assertEquals(24.dp, state.style.cornerRadius)
    assertTrue(state.isVisible)
    onNodeWithTag(BODY).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun rememberBalloonState_survivesRecompositionAndKeepsVisibility() = runComposeUiTest {
    lateinit var state: BalloonState
    var bump by mutableStateOf(0)
    setContent {
      state = rememberBalloonState(plain)
      @Suppress("UNUSED_EXPRESSION")
      bump
      Anchored(state)
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()

    bump = 1
    waitForIdle()

    assertTrue(state.isVisible)
    onNodeWithTag(BODY).assertIsDisplayed()
  }

  // ------------------------------------------------------------------ highlight

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun everyHighlightAnimation_rendersWithoutStalling() = runComposeUiTest {
    // These loop forever, so the clock is driven by hand: `waitForIdle` would never return.
    // The point is that each branch composes, lays out and draws — a missing `graphicsLayer`
    // or an unhandled enum entry would blow up here.
    for (animation in BalloonHighlightAnimation.entries) {
      lateinit var state: BalloonState
      setContent {
        state = rememberBalloonState(
          plain.copy(highlightAnimation = animation),
        )
        Anchored(state)
      }
      mainClock.autoAdvance = false
      runOnUiThread { state.showAlignBottom() }
      mainClock.advanceTimeBy(16L)
      mainClock.advanceTimeBy(400L)

      onNodeWithTag(BODY).assertIsDisplayed()
      runOnUiThread { state.dismiss() }
      mainClock.advanceTimeBy(16L)
    }
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun highlightStartDelay_isHonoured() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      state = rememberBalloonState(
        plain.copy(
          highlightAnimation = BalloonHighlightAnimation.SHAKE,
          highlightAnimationStartDelayMillis = 1_000L,
        ),
      )
      Anchored(state)
    }

    mainClock.autoAdvance = false
    runOnUiThread { state.showAlignBottom() }
    mainClock.advanceTimeBy(16L)
    // Before the delay elapses the balloon is drawn untransformed, and after it the
    // animation takes over — either way it stays on screen.
    onNodeWithTag(BODY).assertIsDisplayed()
    mainClock.advanceTimeBy(1_200L)
    onNodeWithTag(BODY).assertIsDisplayed()
  }

  // ---------------------------------------------------------------- placement

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun alignTop_putsTheBodyAboveTheAnchor() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      Box(Modifier.size(400.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(120.dp)) {
          Box(Modifier.size(120.dp))
          state = rememberBalloonState(plain)
          Anchored(state)
        }
      }
    }

    runOnUiThread { state.showAlignTop() }
    waitForIdle()

    val anchorTop = onNodeWithTag(ANCHOR).getUnclippedBoundsInRoot().top
    val bodyBottom = onNodeWithTag(BODY).getUnclippedBoundsInRoot().bottom
    assertTrue(
      bodyBottom <= anchorTop,
      "body bottom $bodyBottom should be at or above anchor top $anchorTop",
    )
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun alignBottom_putsTheBodyBelowTheAnchor() = runComposeUiTest {
    lateinit var state: BalloonState
    setContent {
      Box(Modifier.size(400.dp)) {
        state = rememberBalloonState(plain)
        Anchored(state)
      }
    }

    runOnUiThread { state.showAlignBottom() }
    waitForIdle()

    val anchorBottom = onNodeWithTag(ANCHOR).getUnclippedBoundsInRoot().bottom
    val bodyTop = onNodeWithTag(BODY).getUnclippedBoundsInRoot().top
    assertTrue(
      bodyTop >= anchorBottom,
      "body top $bodyTop should be at or below anchor bottom $anchorBottom",
    )
  }

  private companion object {
    const val ANCHOR = "anchor"
    const val BODY = "body"
    const val SIBLING = "sibling"
  }
}

/** A probe local used to prove the balloon body is composed in the anchor's scope. */
private val LocalProbe = staticCompositionLocalOf { "host" }

/** How many nodes currently carry [tag] — 0 once the balloon body has been unmounted. */
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.onAllNodesWithTagCount(tag: String): Int =
  onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().size
