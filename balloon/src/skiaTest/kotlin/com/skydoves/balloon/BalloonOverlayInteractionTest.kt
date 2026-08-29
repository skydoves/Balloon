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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Whether a tap on the overlay scrim reaches [BalloonState.onOverlayClick].
 *
 * The scrim is drawn by [BalloonHost], in the app's own window, because a `Popup` cannot cover
 * the system bars. A focusable popup is touch-modal, so while one is up the host window
 * receives no touches at all and the scrim is unreachable — the tap is consumed by the popup
 * and, with `dismissOnClickOutside` on, turned straight into a dismissal.
 *
 * This is not a port artefact. The View implementation has the identical coupling: its
 * `bodyWindow` is a focusable `PopupWindow` sitting above a separate `overlayWindow`, and
 * `Builder.setDismissWhenTouchOutside(false)` calls `setFocusable(false)` for exactly this
 * reason. `Balloon.Builder.setDismissWhenTouchOutside` mirrors that side effect, so the
 * documented recipe — turn outside-dismissal off, then handle the scrim yourself — behaves
 * the same on both. These cases pin all three combinations so the coupling cannot be
 * "simplified" away later.
 */
class BalloonOverlayInteractionTest {

  @OptIn(ExperimentalTestApi::class)
  private fun tapScrim(
    focusable: Boolean,
    dismissOnClickOutside: Boolean,
    assertions: (overlayClicks: Int, isVisible: Boolean) -> Unit,
  ) = runComposeUiTest {
    lateinit var state: BalloonState
    var overlayClicks = 0
    setContent {
      BalloonHost(modifier = Modifier.fillMaxSize().testTag("host")) {
        state = rememberBalloonState(
          BalloonStyle(
            animation = BalloonAnimation.NONE,
            isVisibleOverlay = true,
            overlayColor = Color(0x99000000),
            dismissWhenOverlayClicked = false,
            focusable = focusable,
            dismissOnClickOutside = dismissOnClickOutside,
          ),
        )
        state.onOverlayClick = { overlayClicks++ }
        Balloon(
          state = state,
          balloonContent = { Box(Modifier.size(40.dp).testTag("body")) },
        ) { Box(Modifier.size(20.dp).testTag("anchor")) }
      }
    }
    runOnUiThread { state.showAlignBottom() }
    waitForIdle()
    assertTrue(state.isVisible, "precondition: the balloon should be showing")
    // Bottom-right of the host, far from both the anchor and the balloon: pure scrim.
    onNodeWithTag("host").performTouchInput { click(Offset(width - 5f, height - 5f)) }
    waitForIdle()
    assertions(overlayClicks, state.isVisible)
  }

  @Test
  fun aNonFocusableBalloonLetsScrimTapsThrough() =
    // What `setDismissWhenTouchOutside(false)` configures, and the only combination in which
    // `setDismissWhenOverlayClicked` / `onOverlayClick` are live.
    tapScrim(focusable = false, dismissOnClickOutside = false) { clicks, visible ->
      assertEquals(1, clicks, "the scrim should receive the tap")
      assertTrue(visible, "setDismissWhenOverlayClicked(false) should keep it up")
    }

  @Test
  fun aFocusableBalloonSwallowsScrimTaps() =
    tapScrim(focusable = true, dismissOnClickOutside = false) { clicks, visible ->
      assertEquals(0, clicks, "a touch-modal popup keeps the host window from seeing the tap")
      assertTrue(visible, "with outside-dismissal off there is nothing left to dismiss it")
    }

  @Test
  fun theDefaultBalloonDismissesOnAScrimTap() =
    tapScrim(focusable = true, dismissOnClickOutside = true) { clicks, visible ->
      assertEquals(0, clicks, "the popup consumes the tap before the scrim can see it")
      assertTrue(!visible, "and reports it as an outside click, which dismisses")
    }
}
