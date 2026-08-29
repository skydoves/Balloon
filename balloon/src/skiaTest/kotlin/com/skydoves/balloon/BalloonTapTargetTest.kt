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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Which parts of the popup box count as "the balloon" for a tap.
 *
 * The popup box is `margin + reserve + (protrusion band + card)`, and the card is drawn as an
 * `Outline.Generic`. `Modifier.clip` gates hit testing as well as drawing, so a tap detector
 * placed after the clip is unreachable on the rounded corners and on the flat band beside the
 * arrow. Those taps used to fall through to the margin detector and dismiss the balloon; the
 * View original reports them as ordinary balloon clicks, because `balloonWrapper` is a plain
 * rectangle and `dismissWhenTouchMargin` only fires outside its horizontal span.
 *
 * Offsets below are relative to the content slot's top-left, at density 1:
 * `padding` = 8px, `protrusion` = `arrowHeight - 1` = 11px, `elevation` = 0.
 */
class BalloonTapTargetTest {

  private val arrow = 12.dp

  @OptIn(ExperimentalTestApi::class)
  private fun tap(
    marginPx: Int = 0,
    dismissWhenTouchMargin: Boolean = true,
    dx: Float,
    dy: Float,
    assertions: (balloonClicks: Int, isVisible: Boolean) -> Unit,
  ) = runComposeUiTest {
    lateinit var state: BalloonState
    var balloonClicks = 0
    setContent {
      BalloonHost(modifier = Modifier.fillMaxSize().testTag("host")) {
        state = rememberBalloonState(
          BalloonStyle(
            animation = BalloonAnimation.NONE,
            arrowSize = DpSize(arrow, arrow),
            cornerRadius = 24.dp,
            padding = PaddingValues(8.dp),
            margin = PaddingValues(marginPx.dp),
            elevation = 0.dp,
            dismissWhenTouchMargin = dismissWhenTouchMargin,
          ),
        )
        state.onBalloonClick = { balloonClicks++ }
        Balloon(
          state = state,
          balloonContent = { Box(Modifier.size(100.dp, 40.dp).testTag("content")) },
        ) { Box(Modifier.size(20.dp).testTag("anchor")) }
      }
    }
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    assertTrue(state.isVisible, "precondition: the balloon should be showing")
    onNodeWithTag("content").performTouchInput { click(Offset(dx, dy)) }
    waitForIdle()
    assertions(balloonClicks, state.isVisible)
  }

  @Test
  fun tappingTheBodyReportsABalloonClick() = tap(dx = 50f, dy = 20f) { clicks, visible ->
    assertEquals(1, clicks)
    assertTrue(visible, "setDismissWhenClicked defaults to false")
  }

  @Test
  fun tappingARoundedCornerReportsABalloonClick() =
    // Outside the generic path, inside the card rect: `clip` would reject the hit.
    tap(dx = -6f, dy = -6f) { clicks, visible ->
      assertEquals(1, clicks, "a corner sliver is still the balloon, not the margin")
      assertTrue(visible, "a corner tap must not dismiss")
    }

  @Test
  fun tappingTheBandBesideTheArrowReportsABalloonClick() =
    // y = -17 puts the tap in the protrusion band (11px tall), x = 6 is clear of the triangle.
    tap(dx = 6f, dy = -17f) { clicks, visible ->
      assertEquals(1, clicks, "the flat band beside the arrow belongs to the card")
      assertTrue(visible, "an arrow-band tap must not dismiss")
    }

  // Measured, not assumed: with a 24dp margin the content slot sits at (32, 63) in the
  // scene and the popup box starts at (0, 20), so (-28, -39) from the content's top-left is
  // (4, 24) in the scene -- 4px inside the popup, squarely in the margin strip. Landing even
  // one pixel outside would make the platform treat it as an outside click and dismiss for
  // an entirely different reason, which is exactly the trap these two cases guard against.
  @Test
  fun tappingTheMarginDismisses() =
    // The detector has to sit BEFORE `padding(margin)` to cover this at all.
    tap(marginPx = 24, dx = -28f, dy = -39f) { clicks, visible ->
      assertEquals(0, clicks, "the margin is not the balloon")
      assertTrue(!visible, "setDismissWhenTouchMargin defaults to on")
    }

  @Test
  fun tappingTheMarginIsInertWhenTouchMarginIsOff() =
    tap(marginPx = 24, dismissWhenTouchMargin = false, dx = -28f, dy = -39f) { clicks, visible ->
      assertEquals(0, clicks)
      assertTrue(visible, "setDismissWhenTouchMargin(false) must swallow the tap")
    }
}
