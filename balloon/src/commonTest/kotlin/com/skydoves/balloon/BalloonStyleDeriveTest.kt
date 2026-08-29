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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * `derive` replaces the data class `copy` that used to be public, so it has to be just as
 * lossless.
 *
 * `Balloon.Builder.loadFrom` copies 43 style properties across by hand into 54 builder fields.
 * A missed or transposed line there is invisible at the call site: the derived style simply
 * carries a default where the original had a value, and only shows up as a balloon that
 * quietly renders wrong. Comparing whole `BalloonStyle` values catches every one of them at
 * once, because the data class equality covers all 43 properties whether or not anyone
 * remembered to assert on them individually.
 */
class BalloonStyleDeriveTest {

  /**
   * A style with every property moved off its default, so a dropped line in `loadFrom` shows
   * up as a difference rather than as a coincidental match.
   */
  private fun fullyPopulated(): BalloonStyle = Balloon.Builder().apply {
    setCornerRadius(9.dp)
    setArrowSize(width = 14.dp, height = 11.dp)
    setArrowOrientation(ArrowOrientation.START)
    setArrowPosition(0.25f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
    setArrowAlignAnchorPadding(3.dp)
    setArrowAlignAnchorPaddingRatio(1.75f)
    setIsVisibleArrow(false)
    setBackgroundColor(Color(0xFF112233))
    setArrowColor(Color(0xFF445566))
    setBorder(color = Color(0xFF778899), thickness = 2.dp)
    setPadding(start = 1.dp, top = 2.dp, end = 3.dp, bottom = 4.dp)
    setMargin(start = 5.dp, top = 6.dp, end = 7.dp, bottom = 8.dp)
    setElevation(4.dp)
    setWidth(180.dp)
    setWidthRatio(0.4f)
    setMinWidthRatio(0.2f)
    setMaxWidthRatio(0.9f)
    setMinWidth(120.dp)
    setMaxWidth(300.dp)
    setHeight(70.dp)
    setBalloonAnimation(BalloonAnimation.OVERSHOOT)
    setCircularDuration(750L)
    setBalloonRotationAnimation(BalloonRotateAnimation(loops = 3))
    setAlpha(0.65f)
    setIsVisibleOverlay(true)
    setOverlayColor(Color(0x80123456))
    setOverlayPadding(start = 9.dp, top = 10.dp, end = 11.dp, bottom = 12.dp)
    setOverlayPaddingColor(Color(0xFFAABBCC))
    setOverlayShape(BalloonOverlayShape.Circle(24.dp))
    setBalloonOverlayAnimation(BalloonOverlayAnimation.NONE)
    setDismissWhenOverlayClicked(false)
    setDismissWhenClicked(true)
    setDismissWhenTouchMargin(false)
    setDismissWhenShowAgain(true)
    setDismissWhenBackPressed(false)
    setAutoDismissDuration(1_500L)
    // Last: it clears focusability as a documented side effect, so setting it earlier would
    // let a later setFocusable silently undo the pairing this test wants to round trip.
    setDismissWhenTouchOutside(false)
  }.build()

  @Test
  fun deriveWithAnEmptyBlockChangesNothing() {
    val style = fullyPopulated()

    assertEquals(style, style.derive { })
  }

  @Test
  fun deriveWithAnEmptyBlockChangesNothingForTheDefaults() {
    assertEquals(DefaultBalloonStyle, DefaultBalloonStyle.derive { })
  }

  @Test
  fun deriveChangesOnlyWhatTheBlockSets() {
    val style = fullyPopulated()

    val derived = style.derive { setBackgroundColor(Color.Red) }

    assertNotEquals(style, derived)
    assertEquals(Color.Red, derived.backgroundColor)
    // Everything else has to survive, which comparing against a hand-built expectation would
    // only prove for the properties the expectation happens to name.
    assertEquals(style, derived.derive { setBackgroundColor(style.backgroundColor) })
  }

  @Test
  fun derivedStylesChain() {
    val base = DefaultBalloonStyle.derive { setCornerRadius(8.dp) }

    val chained = base
      .derive { setPadding(12.dp) }
      .derive { setBackgroundColor(Color.Blue) }

    assertEquals(8.dp, chained.cornerRadius)
    assertEquals(PaddingValues(12.dp), chained.padding)
    assertEquals(Color.Blue, chained.backgroundColor)
  }

  @Test
  fun asymmetricPaddingSurvivesTheRoundTrip() {
    // `PaddingValues` only exposes its edges through `calculate*Padding(layoutDirection)`, so
    // this is the part of `loadFrom` most likely to quietly collapse to a uniform value.
    val style = Balloon.Builder().apply {
      setPadding(start = 1.dp, top = 2.dp, end = 3.dp, bottom = 4.dp)
      setMargin(start = 5.dp, top = 6.dp, end = 7.dp, bottom = 8.dp)
      setOverlayPadding(start = 9.dp, top = 10.dp, end = 11.dp, bottom = 12.dp)
    }.build()

    assertEquals(style, style.derive { })
  }
}
