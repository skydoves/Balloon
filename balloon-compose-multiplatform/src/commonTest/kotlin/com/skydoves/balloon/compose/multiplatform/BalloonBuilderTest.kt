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

package com.skydoves.balloon.compose.multiplatform

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Covers [Balloon.Builder] end to end: every setter lands on the right [BalloonStyle] field,
 * the defaults match the Android `Balloon.Builder` they were ported from, and the two
 * setters that carry hidden side effects in the original carry them here too.
 */
class BalloonBuilderTest {

  private val ltr = LayoutDirection.Ltr

  // ------------------------------------------------------------------- defaults

  @Test
  fun defaults_matchTheAndroidBuilderDefaults() {
    val style = Balloon.Builder().build()

    assertEquals(5.dp, style.cornerRadius)
    assertEquals(DpSize(12.dp, 12.dp), style.arrowSize)
    assertEquals(null, style.arrowOrientation)
    assertEquals(0.5f, style.arrowPosition)
    assertEquals(ArrowPositionRules.ALIGN_BALLOON, style.arrowPositionRules)
    assertEquals(ArrowOrientationRules.ALIGN_ANCHOR, style.arrowOrientationRules)
    assertEquals(0.dp, style.arrowAlignAnchorPadding)
    assertEquals(2.5f, style.arrowAlignAnchorPaddingRatio)
    assertTrue(style.isArrowVisible)
    assertEquals(Color.Black, style.backgroundColor)
    assertEquals(Color.Unspecified, style.arrowColor)
    assertEquals(Color.Unspecified, style.borderColor)
    assertEquals(0.dp, style.borderThickness)
    assertEquals(0.dp, style.padding.calculateTopPadding())
    assertEquals(0.dp, style.margin.calculateTopPadding())
    assertEquals(2.dp, style.elevation)
    assertEquals(Dp.Unspecified, style.width)
    assertEquals(0f, style.widthRatio)
    assertEquals(0f, style.minWidthRatio)
    assertEquals(0f, style.maxWidthRatio)
    assertEquals(Dp.Unspecified, style.minWidth)
    assertEquals(Dp.Unspecified, style.maxWidth)
    assertEquals(Dp.Unspecified, style.height)
    assertEquals(BalloonAnimation.FADE, style.animation)
    assertEquals(500L, style.circularDurationMillis)
    assertEquals(BalloonHighlightAnimation.NONE, style.highlightAnimation)
    assertEquals(0L, style.highlightAnimationStartDelayMillis)
    assertEquals(BalloonRotateAnimation(), style.rotateAnimation)
    assertEquals(1f, style.alpha)
    assertFalse(style.isVisibleOverlay)
    assertEquals(Color.Transparent, style.overlayColor)
    assertEquals(0.dp, style.overlayPadding.calculateTopPadding())
    assertEquals(BalloonOverlayShape.Oval, style.overlayShape)
    assertEquals(BalloonOverlayAnimation.FADE, style.overlayAnimation)
    assertTrue(style.dismissWhenOverlayClicked)
    assertFalse(style.dismissWhenClicked)
    assertFalse(style.dismissWhenShowAgain)
    assertTrue(style.focusable)
    assertTrue(style.dismissOnClickOutside)
    assertTrue(style.dismissOnBackPress)
    assertEquals(0L, style.autoDismissMillis)
  }

  @Test
  fun defaultBalloonStyle_isTheBuilderDefault() {
    assertEquals(Balloon.Builder().build(), DefaultBalloonStyle)
  }

  // --------------------------------------------------------------------- shape

  @Test
  fun shapeSetters_land() {
    val style = Balloon.Builder()
      .setCornerRadius(9.dp)
      .setArrowSize(20.dp, 8.dp)
      .setArrowOrientation(ArrowOrientation.START)
      .setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
      .setArrowPosition(0.8f)
      .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
      .setArrowAlignAnchorPadding(4.dp)
      .setArrowAlignAnchorPaddingRatio(1.5f)
      .setIsVisibleArrow(false)
      .build()

    assertEquals(9.dp, style.cornerRadius)
    assertEquals(DpSize(20.dp, 8.dp), style.arrowSize)
    assertEquals(ArrowOrientation.START, style.arrowOrientation)
    assertEquals(ArrowOrientationRules.ALIGN_FIXED, style.arrowOrientationRules)
    assertEquals(0.8f, style.arrowPosition)
    assertEquals(ArrowPositionRules.ALIGN_ANCHOR, style.arrowPositionRules)
    assertEquals(4.dp, style.arrowAlignAnchorPadding)
    assertEquals(1.5f, style.arrowAlignAnchorPaddingRatio)
    assertFalse(style.isArrowVisible)
  }

  @Test
  fun uniformArrowSize_setsBothAxes() {
    val style = Balloon.Builder().setArrowSize(7.dp).build()
    assertEquals(DpSize(7.dp, 7.dp), style.arrowSize)
  }

  @Test
  fun arrowWidthAndHeight_areIndependent() {
    val style = Balloon.Builder().setArrowWidth(18.dp).setArrowHeight(4.dp).build()
    assertEquals(DpSize(18.dp, 4.dp), style.arrowSize)
  }

  @Test
  fun arrowPosition_isClampedToTheUnitRange() {
    assertEquals(0f, Balloon.Builder().setArrowPosition(-3f).build().arrowPosition)
    assertEquals(1f, Balloon.Builder().setArrowPosition(4f).build().arrowPosition)
  }

  @Test
  fun effectiveArrowSize_collapsesForHiddenOrDegenerateArrows() {
    assertEquals(DpSize.Zero, Balloon.Builder().setIsVisibleArrow(false).build().effectiveArrowSize)
    assertEquals(DpSize.Zero, Balloon.Builder().setArrowSize(0.dp).build().effectiveArrowSize)
    assertEquals(DpSize.Zero, Balloon.Builder().setArrowWidth(0.dp).build().effectiveArrowSize)
    assertEquals(DpSize.Zero, Balloon.Builder().setArrowHeight(0.dp).build().effectiveArrowSize)
    assertEquals(DpSize(12.dp, 12.dp), Balloon.Builder().build().effectiveArrowSize)
  }

  // -------------------------------------------------------------------- colors

  @Test
  fun colorSetters_land() {
    val style = Balloon.Builder()
      .setBackgroundColor(Color.Red)
      .setArrowColor(Color.Green)
      .setBorder(Color.Blue, 3.dp)
      .build()

    assertEquals(Color.Red, style.backgroundColor)
    assertEquals(Color.Green, style.arrowColor)
    assertEquals(Color.Blue, style.borderColor)
    assertEquals(3.dp, style.borderThickness)
  }

  @Test
  fun setBalloonStroke_isAnAliasForSetBorder() {
    val a = Balloon.Builder().setBalloonStroke(Color.Cyan, 2.dp).build()
    val b = Balloon.Builder().setBorder(Color.Cyan, 2.dp).build()
    assertEquals(b, a)
  }

  // ------------------------------------------------------------ padding/margin

  @Test
  fun paddingSetters_land() {
    val style = Balloon.Builder().setPadding(1.dp, 2.dp, 3.dp, 4.dp).build()

    assertEquals(1.dp, style.padding.calculateLeftPadding(ltr))
    assertEquals(2.dp, style.padding.calculateTopPadding())
    assertEquals(3.dp, style.padding.calculateRightPadding(ltr))
    assertEquals(4.dp, style.padding.calculateBottomPadding())
  }

  @Test
  fun paddingHorizontalAndVertical_preserveTheOtherAxis() {
    val style = Balloon.Builder()
      .setPadding(1.dp, 2.dp, 3.dp, 4.dp)
      .setPaddingHorizontal(10.dp)
      .build()

    assertEquals(10.dp, style.padding.calculateLeftPadding(ltr))
    assertEquals(10.dp, style.padding.calculateRightPadding(ltr))
    assertEquals(2.dp, style.padding.calculateTopPadding())
    assertEquals(4.dp, style.padding.calculateBottomPadding())
  }

  @Test
  fun individualPaddingSetters_land() {
    val style = Balloon.Builder()
      .setPaddingStart(1.dp)
      .setPaddingTop(2.dp)
      .setPaddingEnd(3.dp)
      .setPaddingBottom(4.dp)
      .build()

    assertEquals(1.dp, style.padding.calculateLeftPadding(ltr))
    assertEquals(2.dp, style.padding.calculateTopPadding())
    assertEquals(3.dp, style.padding.calculateRightPadding(ltr))
    assertEquals(4.dp, style.padding.calculateBottomPadding())
  }

  @Test
  fun marginSetters_land() {
    val style = Balloon.Builder()
      .setMarginStart(1.dp)
      .setMarginTop(2.dp)
      .setMarginEnd(3.dp)
      .setMarginBottom(4.dp)
      .build()

    assertEquals(1.dp, style.margin.calculateLeftPadding(ltr))
    assertEquals(2.dp, style.margin.calculateTopPadding())
    assertEquals(3.dp, style.margin.calculateRightPadding(ltr))
    assertEquals(4.dp, style.margin.calculateBottomPadding())
  }

  @Test
  fun paddingIsRtlAware() {
    val style = Balloon.Builder().setPaddingStart(6.dp).build()

    assertEquals(6.dp, style.padding.calculateLeftPadding(LayoutDirection.Ltr))
    assertEquals(6.dp, style.padding.calculateRightPadding(LayoutDirection.Rtl))
  }

  // -------------------------------------------------------------------- sizing

  @Test
  fun sizingSetters_land() {
    val style = Balloon.Builder()
      .setWidth(200.dp)
      .setWidthRatio(0.6f)
      .setMinWidth(50.dp)
      .setMaxWidth(300.dp)
      .setMinWidthRatio(0.2f)
      .setMaxWidthRatio(0.9f)
      .setHeight(120.dp)
      .setElevation(6.dp)
      .build()

    assertEquals(200.dp, style.width)
    assertEquals(0.6f, style.widthRatio)
    assertEquals(50.dp, style.minWidth)
    assertEquals(300.dp, style.maxWidth)
    assertEquals(0.2f, style.minWidthRatio)
    assertEquals(0.9f, style.maxWidthRatio)
    assertEquals(120.dp, style.height)
    assertEquals(6.dp, style.elevation)
  }

  @Test
  fun setSize_setsBothAxes() {
    val style = Balloon.Builder().setSize(100.dp, 60.dp).build()
    assertEquals(100.dp, style.width)
    assertEquals(60.dp, style.height)
  }

  // ---------------------------------------------------------------- animations

  @Test
  fun animationSetters_land() {
    val rotate = BalloonRotateAnimation(turns = 3, speedMillis = 900)
    val style = Balloon.Builder()
      .setBalloonAnimation(BalloonAnimation.ELASTIC)
      .setCircularDuration(750L)
      .setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE, startDelayMillis = 250L)
      .setBalloonRotationAnimation(rotate)
      .setBalloonOverlayAnimation(BalloonOverlayAnimation.NONE)
      .setAlpha(0.4f)
      .build()

    assertEquals(BalloonAnimation.ELASTIC, style.animation)
    assertEquals(750L, style.circularDurationMillis)
    assertEquals(BalloonHighlightAnimation.SHAKE, style.highlightAnimation)
    assertEquals(250L, style.highlightAnimationStartDelayMillis)
    assertEquals(rotate, style.rotateAnimation)
    assertEquals(BalloonOverlayAnimation.NONE, style.overlayAnimation)
    assertEquals(0.4f, style.alpha)
  }

  @Test
  fun negativeDurations_areClampedToZero() {
    val style = Balloon.Builder()
      .setCircularDuration(-1L)
      .setAutoDismissDuration(-5L)
      .setBalloonHighlightAnimation(BalloonHighlightAnimation.BREATH, startDelayMillis = -9L)
      .build()

    assertEquals(0L, style.circularDurationMillis)
    assertEquals(0L, style.autoDismissMillis)
    assertEquals(0L, style.highlightAnimationStartDelayMillis)
  }

  @Test
  fun alpha_isClampedToTheUnitRange() {
    assertEquals(0f, Balloon.Builder().setAlpha(-2f).build().alpha)
    assertEquals(1f, Balloon.Builder().setAlpha(9f).build().alpha)
  }

  // ------------------------------------------------------------------- overlay

  @Test
  fun overlaySetters_land() {
    val style = Balloon.Builder()
      .setIsVisibleOverlay(true)
      .setOverlayColor(Color.Magenta)
      .setOverlayPadding(1.dp, 2.dp, 3.dp, 4.dp)
      .setOverlayShape(BalloonOverlayShape.Circle(16.dp))
      .setDismissWhenOverlayClicked(false)
      .build()

    assertTrue(style.isVisibleOverlay)
    assertEquals(Color.Magenta, style.overlayColor)
    assertEquals(1.dp, style.overlayPadding.calculateLeftPadding(ltr))
    assertEquals(2.dp, style.overlayPadding.calculateTopPadding())
    assertEquals(3.dp, style.overlayPadding.calculateRightPadding(ltr))
    assertEquals(4.dp, style.overlayPadding.calculateBottomPadding())
    assertEquals(BalloonOverlayShape.Circle(16.dp), style.overlayShape)
    assertFalse(style.dismissWhenOverlayClicked)
  }

  @Test
  fun uniformOverlayPadding_setsAllFourSides() {
    val style = Balloon.Builder().setOverlayPadding(5.dp).build()

    assertEquals(5.dp, style.overlayPadding.calculateLeftPadding(ltr))
    assertEquals(5.dp, style.overlayPadding.calculateTopPadding())
    assertEquals(5.dp, style.overlayPadding.calculateRightPadding(ltr))
    assertEquals(5.dp, style.overlayPadding.calculateBottomPadding())
  }

  // ------------------------------------------------------------------ behavior

  @Test
  fun behaviorSetters_land() {
    val style = Balloon.Builder()
      .setDismissWhenClicked(true)
      .setDismissWhenShowAgain(true)
      .setDismissWhenBackPressed(false)
      .setAutoDismissDuration(1500L)
      .build()

    assertTrue(style.dismissWhenClicked)
    assertTrue(style.dismissWhenShowAgain)
    assertFalse(style.dismissOnBackPress)
    assertEquals(1500L, style.autoDismissMillis)
  }

  @Test
  fun setDismissWhenTouchOutsideFalse_alsoDropsFocusability() {
    // The Android original's `setDismissWhenTouchOutside(false)` calls `setFocusable(false)`;
    // without that the popup would keep swallowing the very taps it now ignores.
    val style = Balloon.Builder().setDismissWhenTouchOutside(false).build()

    assertFalse(style.dismissOnClickOutside)
    assertFalse(style.focusable)
  }

  @Test
  fun setDismissWhenTouchOutsideTrue_leavesFocusabilityAlone() {
    val style = Balloon.Builder().setFocusable(false).setDismissWhenTouchOutside(true).build()

    assertTrue(style.dismissOnClickOutside)
    assertFalse(style.focusable)
  }

  @Test
  fun circularAnimation_alsoDropsFocusability() {
    // Mirrors `setBalloonAnimation(CIRCULAR) -> setFocusable(false)` in the original.
    val style = Balloon.Builder().setBalloonAnimation(BalloonAnimation.CIRCULAR).build()

    assertEquals(BalloonAnimation.CIRCULAR, style.animation)
    assertFalse(style.focusable)
  }

  @Test
  fun otherAnimations_leaveFocusabilityAlone() {
    assertTrue(Balloon.Builder().setBalloonAnimation(BalloonAnimation.FADE).build().focusable)
  }

  @Test
  fun focusable_canBeRestoredAfterASideEffect() {
    // Order matters: the caller can opt back in by setting focusability last.
    val style = Balloon.Builder()
      .setBalloonAnimation(BalloonAnimation.CIRCULAR)
      .setFocusable(true)
      .build()

    assertTrue(style.focusable)
  }

  // ------------------------------------------------------------------ equality

  @Test
  fun builtStyles_areValueEqual() {
    val a = Balloon.Builder().setBackgroundColor(Color.Red).setPadding(8.dp).build()
    val b = Balloon.Builder().setBackgroundColor(Color.Red).setPadding(8.dp).build()

    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }
}
