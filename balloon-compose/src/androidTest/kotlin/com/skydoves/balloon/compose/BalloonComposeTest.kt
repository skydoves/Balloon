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

package com.skydoves.balloon.compose

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BalloonComposeTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun balloonState_shouldBeAttached_afterBalloonModifierApplied() {
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      balloonState = rememberBalloonState(builder)

      Box(
        modifier = Modifier
          .size(100.dp)
          .testTag("anchor_box")
          .balloon(balloonState) {
            Text("Content")
          },
      )
    }

    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      assert(balloonState.isAttached) {
        "BalloonState should be attached after Modifier.balloon() is applied"
      }
    }
  }

  @Test
  fun balloonState_shouldNotBeShowing_beforeShowCalled() {
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      balloonState = rememberBalloonState(builder)

      Box(
        modifier = Modifier
          .size(100.dp)
          .testTag("anchor_box")
          .balloon(balloonState) {
            Text("Content")
          },
      )
    }

    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      assert(
        !balloonState.isShowing,
      ) { "BalloonState.isShowing should be false before show is called" }
    }
  }

  @Test
  fun balloonState_shouldProvideAnchorView_whenAttached() {
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      balloonState = rememberBalloonState(builder)

      Box(
        modifier = Modifier
          .size(100.dp)
          .testTag("anchor_box")
          .balloon(balloonState) {
            Text("Content")
          },
      )
    }

    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      val hasAnchorView = try {
        balloonState.anchorView != null
      } catch (e: Exception) {
        false
      }
      assert(hasAnchorView) { "BalloonState should provide anchorView when attached" }
    }
  }

  @Test
  fun rememberBalloonBuilder_shouldCreateBuilder() {
    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setText("Test Balloon")
      }

      Box(modifier = Modifier.size(100.dp).testTag("anchor"))
    }

    composeTestRule.onNodeWithTag("anchor").assertIsDisplayed()
  }

  @Test
  fun rememberBalloonBuilder_shouldApplyConfiguration() {
    composeTestRule.setContent {
      rememberBalloonBuilder {
        setArrowSize(15)
        setArrowPosition(0.5f)
        setArrowOrientation(ArrowOrientation.BOTTOM)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setPadding(12)
        setCornerRadius(8f)
        setBackgroundColor(Color.BLUE)
        setBalloonAnimation(BalloonAnimation.ELASTIC)
        setText("Configured Balloon")
      }

      Box(modifier = Modifier.size(100.dp).testTag("configured_anchor"))
    }

    composeTestRule.onNodeWithTag("configured_anchor").assertIsDisplayed()
  }

  @Test
  fun rememberBalloonState_shouldCreateState() {
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setText("State Test")
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      balloonState = rememberBalloonState(builder)

      Box(
        modifier = Modifier
          .size(100.dp)
          .testTag("state_box")
          .balloon(balloonState) {
            Text("Tooltip")
          },
      )
    }

    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      // Verify state is created and properly initialized
      assert(balloonState.isAttached) { "State should be attached" }
      assert(!balloonState.isShowing) { "State should not be showing initially" }
    }
  }

  @Test
  fun balloonModifier_shouldAttachToComposable() {
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setText("Modifier Test")
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      balloonState = rememberBalloonState(builder)

      Button(
        onClick = { balloonState.showAlignTop() },
        modifier = Modifier
          .testTag("balloon_button")
          .balloon(balloonState) {
            Text("Tooltip Content")
          },
      ) {
        Text("Click Me")
      }
    }

    composeTestRule.onNodeWithTag("balloon_button").assertIsDisplayed()

    composeTestRule.runOnIdle {
      assert(balloonState.isAttached) { "BalloonState should be attached" }
    }
  }

  @Test
  fun balloonBuilder_shouldSupportChaining() {
    composeTestRule.setContent {
      rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setArrowOrientation(ArrowOrientation.TOP)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setPadding(16)
        setMarginHorizontal(8)
        setCornerRadius(12f)
        setBackgroundColor(Color.parseColor("#FF5722"))
        setBalloonAnimation(BalloonAnimation.FADE)
        setDismissWhenClicked(true)
        setDismissWhenTouchOutside(true)
        setAutoDismissDuration(3000L)
        setText("Chained Configuration")
      }

      Box(
        modifier = Modifier
          .size(100.dp)
          .testTag("chained_anchor"),
      )
    }

    composeTestRule.onNodeWithTag("chained_anchor").assertIsDisplayed()
  }

  @Test
  fun balloonBuilder_shouldSupportOverlayConfiguration() {
    composeTestRule.setContent {
      rememberBalloonBuilder {
        setText("Overlay Test")
        setIsVisibleOverlay(true)
        setOverlayColor(Color.parseColor("#80000000"))
        setOverlayPadding(8f)
        setDismissWhenOverlayClicked(true)
      }

      Box(modifier = Modifier.size(100.dp).testTag("overlay_anchor"))
    }

    composeTestRule.onNodeWithTag("overlay_anchor").assertIsDisplayed()
  }

  @Test
  fun multipleBalloonStates_shouldBeIndependent() {
    lateinit var balloonState1: BalloonState
    lateinit var balloonState2: BalloonState

    composeTestRule.setContent {
      val builder1 = rememberBalloonBuilder {
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setBackgroundColor(Color.RED)
      }
      val builder2 = rememberBalloonBuilder {
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setBackgroundColor(Color.BLUE)
      }
      balloonState1 = rememberBalloonState(builder1)
      balloonState2 = rememberBalloonState(builder2)

      Column {
        Button(
          onClick = { },
          modifier = Modifier
            .testTag("button1")
            .balloon(balloonState1) {
              Text("Tooltip 1")
            },
        ) {
          Text("Button 1")
        }

        Button(
          onClick = { },
          modifier = Modifier
            .testTag("button2")
            .balloon(balloonState2) {
              Text("Tooltip 2")
            },
        ) {
          Text("Button 2")
        }
      }
    }

    composeTestRule.waitForIdle()

    // Both should be attached independently
    composeTestRule.runOnIdle {
      assert(balloonState1.isAttached) { "BalloonState1 should be attached" }
      assert(balloonState2.isAttached) { "BalloonState2 should be attached" }
      assert(!balloonState1.isShowing) { "BalloonState1 should not be showing" }
      assert(!balloonState2.isShowing) { "BalloonState2 should not be showing" }
    }
  }

  @Test
  fun balloonModifier_shouldWorkWithCustomConfiguration() {
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setArrowSize(15)
        setArrowPosition(0.5f)
        setArrowOrientation(ArrowOrientation.BOTTOM)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
        setPadding(12)
        setCornerRadius(8f)
        setBackgroundColor(Color.BLUE)
        setBalloonAnimation(BalloonAnimation.ELASTIC)
      }
      balloonState = rememberBalloonState(builder)

      Button(
        onClick = { },
        modifier = Modifier
          .testTag("configured_button")
          .balloon(balloonState) {
            Text("Custom Styled Tooltip")
          },
      ) {
        Text("Show Custom Balloon")
      }
    }

    composeTestRule.onNodeWithTag("configured_button").assertIsDisplayed()

    composeTestRule.runOnIdle {
      assert(
        balloonState.isAttached,
      ) { "BalloonState should be attached with custom configuration" }
    }
  }

  @Test
  fun balloonModifier_anchorButtonShouldBeClickable() {
    var clicked = false
    lateinit var balloonState: BalloonState

    composeTestRule.setContent {
      val builder = rememberBalloonBuilder {
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      balloonState = rememberBalloonState(builder)

      Button(
        onClick = { clicked = true },
        modifier = Modifier
          .testTag("clickable_button")
          .balloon(balloonState) {
            Text("Tooltip")
          },
      ) {
        Text("Click Me")
      }
    }

    composeTestRule.onNodeWithTag("clickable_button").performClick()
    composeTestRule.waitForIdle()

    assert(clicked) { "Button onClick should have been called" }
  }

  @Test
  fun balloonModifier_shouldWorkWithDifferentArrowOrientations() {
    lateinit var topState: BalloonState
    lateinit var bottomState: BalloonState
    lateinit var startState: BalloonState
    lateinit var endState: BalloonState

    composeTestRule.setContent {
      val topBuilder = rememberBalloonBuilder {
        setArrowOrientation(ArrowOrientation.TOP)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      val bottomBuilder = rememberBalloonBuilder {
        setArrowOrientation(ArrowOrientation.BOTTOM)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      val startBuilder = rememberBalloonBuilder {
        setArrowOrientation(ArrowOrientation.START)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }
      val endBuilder = rememberBalloonBuilder {
        setArrowOrientation(ArrowOrientation.END)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(BalloonSizeSpec.WRAP)
      }

      topState = rememberBalloonState(topBuilder)
      bottomState = rememberBalloonState(bottomBuilder)
      startState = rememberBalloonState(startBuilder)
      endState = rememberBalloonState(endBuilder)

      Column {
        Box(
          modifier = Modifier
            .size(50.dp)
            .testTag("top_anchor")
            .balloon(topState) { Text("Top") },
        )
        Box(
          modifier = Modifier
            .size(50.dp)
            .testTag("bottom_anchor")
            .balloon(bottomState) { Text("Bottom") },
        )
        Box(
          modifier = Modifier
            .size(50.dp)
            .testTag("start_anchor")
            .balloon(startState) { Text("Start") },
        )
        Box(
          modifier = Modifier
            .size(50.dp)
            .testTag("end_anchor")
            .balloon(endState) { Text("End") },
        )
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      assert(topState.isAttached) { "Top balloon state should be attached" }
      assert(bottomState.isAttached) { "Bottom balloon state should be attached" }
      assert(startState.isAttached) { "Start balloon state should be attached" }
      assert(endState.isAttached) { "End balloon state should be attached" }
    }
  }
}
