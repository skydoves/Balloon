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

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Pure, deterministic unit tests for [BalloonPopupPositionProvider.calculatePosition].
 *
 * The provider is `internal`, so this same-module commonTest can construct and exercise
 * it directly. Every input is a fixed [IntRect] / [IntSize] / [IntOffset]; there is no
 * RNG, no wall-clock and no Compose runtime — we only assert the integer geometry the
 * FINAL popup-placement code in `BalloonPopup.kt` actually computes.
 *
 * Conventions used throughout:
 * - [LayoutDirection.Ltr] only (RTL resolution is covered by other suites / not in scope).
 * - The framework-supplied `anchorBounds` argument to `calculatePosition` is ignored by
 *   the implementation (it uses the captured constructor `anchorBounds`); we still pass
 *   the same rect for clarity.
 */
class PositionProviderTest {

  private val ltr = LayoutDirection.Ltr

  /** A roomy anchor well away from every window edge: 100 x 60 at (200, 200). */
  private val anchor = IntRect(left = 200, top = 200, right = 300, bottom = 260)

  // Derived once so the assertions read like the spec.
  private val anchorCenterX = anchor.left + anchor.width / 2 // 250
  private val anchorCenterY = anchor.top + anchor.height / 2 // 230

  /** A large window so nothing clamps unless a test deliberately shrinks it. */
  private val bigWindow = IntSize(width = 1000, height = 1000)

  /** A small popup that comfortably fits inside [anchor]'s neighbourhood. */
  private val popup = IntSize(width = 80, height = 40)

  private fun stateOf(style: BalloonStyle = BalloonStyle()): BalloonState = BalloonState(style)

  /**
   * Builds the provider with the captured [anchor] (override per-test) and runs
   * `calculatePosition`, returning the resolved popup top-left offset. The same `state`
   * instance is the one the provider writes its resolved arrow data back onto.
   */
  private fun calc(
    state: BalloonState,
    align: BalloonAlign,
    centerAlign: BalloonCenterAlign? = null,
    captured: IntRect = anchor,
    window: IntSize = bigWindow,
    popupSize: IntSize = popup,
    userOffset: IntOffset = IntOffset.Zero,
    layoutDirection: LayoutDirection = ltr,
  ): IntOffset {
    val provider = BalloonPopupPositionProvider(
      state = state,
      anchorBounds = captured,
      align = align,
      centerAlign = centerAlign,
      userOffsetPx = userOffset,
    )
    return provider.calculatePosition(
      anchorBounds = captured,
      windowSize = window,
      layoutDirection = layoutDirection,
      popupContentSize = popupSize,
    )
  }

  @Test
  fun bottom_centersPopupOnAnchorAndSitsBelow_whenItFits() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.BOTTOM)

    // x centers the popup on the anchor; y meets the anchor's bottom edge.
    assertEquals(anchorCenterX - popup.width / 2, offset.x)
    assertEquals(anchor.bottom, offset.y)
  }

  @Test
  fun top_sitsAboveAnchor_whenItFits() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.TOP)

    assertEquals(anchorCenterX - popup.width / 2, offset.x)
    assertEquals(anchor.top - popup.height, offset.y)
  }

  @Test
  fun startLtr_sitsLeftOfAnchorAndVerticallyCentered() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.START)

    assertEquals(anchor.left - popup.width, offset.x)
    assertEquals(anchorCenterY - popup.height / 2, offset.y)
  }

  @Test
  fun endLtr_sitsRightOfAnchorAndVerticallyCentered() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.END)

    assertEquals(anchor.right, offset.x)
    assertEquals(anchorCenterY - popup.height / 2, offset.y)
  }

  @Test
  fun clamp_keepsPopupWithinWindow_whenAnchorAtCornerWithLargePopup() {
    val state = stateOf()
    val window = IntSize(300, 300)
    val largePopup = IntSize(200, 200)
    val cornerAnchor = IntRect(left = 0, top = 0, right = 10, bottom = 10)

    val offset = calc(
      state = state,
      align = BalloonAlign.TOP,
      captured = cornerAnchor,
      window = window,
      popupSize = largePopup,
    )

    val maxX = window.width - largePopup.width // 100
    val maxY = window.height - largePopup.height // 100
    // Final offset is coerced into [0, max] on both axes.
    assertTrue(offset.x in 0..maxX, "x=${offset.x} out of [0,$maxX]")
    assertTrue(offset.y in 0..maxY, "y=${offset.y} out of [0,$maxY]")
    // The centered base x was negative here, so the lower clamp pins it to 0.
    assertEquals(0, offset.x)
  }

  @Test
  fun verticalFlip_bottomRequested_flipsAboveWhenNoRoomBelow() {
    val state = stateOf()
    // Window just tall enough to hold the anchor but not the popup beneath it.
    // anchor.bottom (260) + popupH (40) = 300 > 280  -> no room below.
    // anchor.top (200) - popupH (40)   = 160 >= 0     -> room above -> flip up.
    val window = IntSize(1000, 280)

    val offset = calc(state, BalloonAlign.BOTTOM, window = window)

    assertEquals(anchor.top - popup.height, offset.y) // flipped to sit above
    // The provider writes back the flipped orientation: arrow now on the BOTTOM edge.
    assertEquals(ArrowOrientation.BOTTOM, state.resolvedArrowOrientation)
  }

  @Test
  fun centerAlignTop_putsPopupBottomAtAnchorVerticalCenter() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.CENTER, centerAlign = BalloonCenterAlign.TOP)

    assertEquals(anchorCenterX - popup.width / 2, offset.x)
    // popup bottom edge lands on the anchor's vertical center.
    assertEquals(anchorCenterY, offset.y + popup.height)
    // Arrow points back down at the anchor center.
    assertEquals(ArrowOrientation.BOTTOM, state.resolvedArrowOrientation)
  }

  @Test
  fun centerAlignBottom_putsPopupTopAtAnchorVerticalCenter() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.CENTER, centerAlign = BalloonCenterAlign.BOTTOM)

    assertEquals(anchorCenterX - popup.width / 2, offset.x)
    assertEquals(anchorCenterY, offset.y)
    assertEquals(ArrowOrientation.TOP, state.resolvedArrowOrientation)
  }

  @Test
  fun alignBalloon_keepsArrowRatioAtStyleArrowPosition() {
    val style = BalloonStyle(
      arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
      arrowPosition = 0.3f,
    )
    val state = stateOf(style)

    // Plain BOTTOM placement, nothing shifted: ALIGN_BALLOON must echo arrowPosition.
    calc(state, BalloonAlign.BOTTOM)

    assertEquals(0.3f, state.resolvedArrowRatio)
  }

  @Test
  fun alignAnchor_reAnchorsArrowTowardAnchorCenter_whenPopupIsClampShifted() {
    // A wide popup against the right window edge: its centered base x (850) exceeds
    // maxX (800) and is clamped left by 50px, so ALIGN_ANCHOR must re-anchor the arrow
    // rightward (toward the anchor) instead of staying at the body-relative 0.5.
    val window = IntSize(1000, 1000)
    val widePopup = IntSize(200, 40)
    val rightAnchor = IntRect(left = 900, top = 100, right = 1000, bottom = 160)

    val anchorStyle = BalloonStyle(
      arrowPositionRules = ArrowPositionRules.ALIGN_ANCHOR,
      arrowPosition = 0.5f,
    )
    val balloonStyle = anchorStyle.copy(arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON)

    val anchorState = stateOf(anchorStyle)
    val balloonState = stateOf(balloonStyle)

    calc(
      anchorState,
      BalloonAlign.BOTTOM,
      captured = rightAnchor,
      window = window,
      popupSize = widePopup,
    )
    calc(
      balloonState,
      BalloonAlign.BOTTOM,
      captured = rightAnchor,
      window = window,
      popupSize = widePopup,
    )

    // ALIGN_BALLOON: arrow stays at the body-relative position regardless of the shift.
    assertEquals(0.5f, balloonState.resolvedArrowRatio)
    // ALIGN_ANCHOR: arrowX = 900 + 100*0.5 = 950; finalX = 800 (clamped);
    // ratio = (950 - 800) / 200 = 0.75 -> re-anchored toward the anchor center.
    assertEquals(0.75f, anchorState.resolvedArrowRatio)
    // The two rules must diverge once the popup is shifted off the anchor center.
    assertNotEquals(balloonState.resolvedArrowRatio, anchorState.resolvedArrowRatio)
  }

  @Test
  fun rtlHorizontalFlip_endRequested_flipsToRightWithArrowOnPhysicalLeftEdge() {
    // RTL: END resolves to the LEFT absolute branch (balloon requested on the
    // anchor's physical left). With no room on the left it flips to the right, so
    // the arrow must move to the balloon's physical LEFT edge — which under RTL is
    // ArrowOrientation.END (END.resolve(Rtl) == LEFT). The old code hardcoded START
    // here, which under RTL would wrongly point the arrow at the physical RIGHT edge.
    val rtl = LayoutDirection.Rtl
    val window = IntSize(1000, 1000)
    val leftAnchor = IntRect(left = 10, top = 200, right = 110, bottom = 260)
    val state = stateOf()

    val offset = calc(
      state = state,
      align = BalloonAlign.END,
      captured = leftAnchor,
      window = window,
      layoutDirection = rtl,
    )

    assertEquals(leftAnchor.right, offset.x) // flipped to sit on the right
    assertEquals(ArrowOrientation.END, state.resolvedArrowOrientation)
    assertEquals(ResolvedArrowSide.LEFT, state.resolvedArrowOrientation?.resolve(rtl))
  }

  @Test
  fun rtlHorizontalFlip_startRequested_flipsToLeftWithArrowOnPhysicalRightEdge() {
    // RTL: START resolves to the RIGHT absolute branch. With no room on the right it
    // flips to the left, so the arrow must move to the balloon's physical RIGHT edge —
    // under RTL that is ArrowOrientation.START (START.resolve(Rtl) == RIGHT). The old
    // code hardcoded END here, which under RTL would wrongly point at the LEFT edge.
    val rtl = LayoutDirection.Rtl
    val window = IntSize(1000, 1000)
    val rightAnchor = IntRect(left = 900, top = 200, right = 1000, bottom = 260)
    val state = stateOf()

    val offset = calc(
      state = state,
      align = BalloonAlign.START,
      captured = rightAnchor,
      window = window,
      layoutDirection = rtl,
    )

    assertEquals(rightAnchor.left - popup.width, offset.x) // flipped to sit on the left
    assertEquals(ArrowOrientation.START, state.resolvedArrowOrientation)
    assertEquals(ResolvedArrowSide.RIGHT, state.resolvedArrowOrientation?.resolve(rtl))
  }
}
