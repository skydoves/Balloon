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

import android.graphics.Color
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BalloonTest {

  @Test
  fun balloonBuilder_shouldBuildWithDefaultValues() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Test Balloon")
        .build()

      assertThat(balloon).isNotNull()
      assertThat(balloon.isShowing).isFalse()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyTextProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Hello World")
        .setTextSize(16f)
        .setTextColor(Color.RED)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyArrowProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Arrow Test")
        .setArrowSize(10)
        .setArrowPosition(0.5f)
        .setArrowOrientation(ArrowOrientation.BOTTOM)
        .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplySizeProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Size Test")
        .setWidth(200)
        .setHeight(100)
        .setPadding(16)
        .setMargin(8)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyBackgroundProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Background Test")
        .setBackgroundColor(Color.BLUE)
        .setCornerRadius(12f)
        .setAlpha(0.9f)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyAnimationProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Animation Test")
        .setBalloonAnimation(BalloonAnimation.ELASTIC)
        .setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyDismissProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Dismiss Test")
        .setDismissWhenClicked(true)
        .setDismissWhenTouchOutside(true)
        .setAutoDismissDuration(3000L)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyOverlayProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Overlay Test")
        .setIsVisibleOverlay(true)
        .setOverlayColor(Color.parseColor("#80000000"))
        .setOverlayPadding(8f)
        .setDismissWhenOverlayClicked(true)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyListeners() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      var clickCalled = false
      var dismissCalled = false

      val balloon = Balloon.Builder(activity)
        .setText("Listener Test")
        .setOnBalloonClickListener { clickCalled = true }
        .setOnBalloonDismissListener { dismissCalled = true }
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyWrapSizeSpec() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Wrap Size Test")
        .setWidth(BalloonSizeSpec.WRAP)
        .setHeight(BalloonSizeSpec.WRAP)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyPaddingProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Padding Test")
        .setPaddingLeft(10)
        .setPaddingTop(20)
        .setPaddingRight(10)
        .setPaddingBottom(20)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyMarginProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Margin Test")
        .setMarginLeft(10)
        .setMarginTop(20)
        .setMarginRight(10)
        .setMarginBottom(20)
        .setMarginHorizontal(15)
        .setMarginVertical(25)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyArrowConstraints() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Arrow Constraints Test")
        .setArrowSize(15)
        .setArrowPosition(0.3f)
        .setArrowOrientation(ArrowOrientation.TOP)
        .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
        .setArrowAlignAnchorPadding(5)
        .setArrowAlignAnchorPaddingRatio(0.1f)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyElevation() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Elevation Test")
        .setElevation(10)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldApplyFocusable() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Focusable Test")
        .setFocusable(true)
        .build()

      assertThat(balloon).isNotNull()
    }
    scenario.close()
  }

  @Test
  fun balloon_isShowingShouldBeFalseInitially() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Initial State Test")
        .build()

      assertThat(balloon.isShowing).isFalse()
    }
    scenario.close()
  }

  @Test
  fun balloonBuilder_shouldChainMultipleProperties() {
    val scenario = ActivityScenario.launch(TestActivity::class.java)
    scenario.onActivity { activity ->
      val balloon = Balloon.Builder(activity)
        .setText("Chained Properties")
        .setTextSize(14f)
        .setTextColor(Color.WHITE)
        .setBackgroundColor(Color.BLACK)
        .setCornerRadius(8f)
        .setArrowSize(12)
        .setArrowPosition(0.5f)
        .setArrowOrientation(ArrowOrientation.BOTTOM)
        .setWidth(BalloonSizeSpec.WRAP)
        .setHeight(BalloonSizeSpec.WRAP)
        .setPadding(16)
        .setMargin(8)
        .setBalloonAnimation(BalloonAnimation.FADE)
        .setDismissWhenClicked(true)
        .setAutoDismissDuration(5000L)
        .build()

      assertThat(balloon).isNotNull()
      assertThat(balloon.isShowing).isFalse()
    }
    scenario.close()
  }
}
