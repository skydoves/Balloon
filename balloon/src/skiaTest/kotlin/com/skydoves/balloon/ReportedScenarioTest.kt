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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Layouts that were reported as broken against the 1.x implementations.
 *
 * The rewrite makes both of these structural rather than incidental, but "structural" is exactly
 * the kind of claim that quietly stops being true, and each of these cost a user a bug report
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
}
