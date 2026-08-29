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

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
 * - Density is fixed at `1f`, so a `Dp` value and a pixel are interchangeable and the
 *   expected numbers can be written down by hand.
 * - The framework-supplied `anchorBounds` argument to `calculatePosition` is ignored by
 *   the implementation (it uses the captured constructor `anchorBounds`); we still pass
 *   the same rect for clarity.
 */
class PositionProviderTest {

  private val ltr = LayoutDirection.Ltr
  private val density = Density(1f)

  /** A roomy anchor well away from every window edge: 100 x 60 at (200, 200). */
  private val anchor = IntRect(left = 200, top = 200, right = 300, bottom = 260)

  // Derived once so the assertions read like the spec.
  private val anchorCenterY = anchor.top + anchor.height / 2 // 230

  // Centering assertions mirror the implementation's single-rounding form
  // `left + (width - popup) / 2`. This is NOT the same as `center - popup / 2` for odd
  // sizes (two truncations vs one), so asserting the impl form stops the test from
  // silently drifting — see [bottomCentering_isExactForOddSizes].
  private fun centeredX(popupWidth: Int, rect: IntRect = anchor): Int =
    rect.left + (rect.width - popupWidth) / 2

  private fun centeredY(popupHeight: Int, rect: IntRect = anchor): Int =
    rect.top + (rect.height - popupHeight) / 2

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
      windowSize = window,
      density = density,
    )
    return provider.calculatePosition(
      anchorBounds = captured,
      windowSize = window,
      layoutDirection = layoutDirection,
      popupContentSize = popupSize,
    )
  }

  // ------------------------------------------------------------------ placement

  @Test
  fun bottom_centersPopupOnAnchorAndSitsBelow_whenItFits() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.BOTTOM)

    // x centers the popup on the anchor; y meets the anchor's bottom edge.
    assertEquals(centeredX(popup.width), offset.x)
    assertEquals(anchor.bottom, offset.y)
  }

  @Test
  fun top_sitsAboveAnchor_whenItFits() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.TOP)

    assertEquals(centeredX(popup.width), offset.x)
    assertEquals(anchor.top - popup.height, offset.y)
  }

  @Test
  fun startLtr_sitsLeftOfAnchorAndVerticallyCentered() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.START)

    assertEquals(anchor.left - popup.width, offset.x)
    assertEquals(centeredY(popup.height), offset.y)
  }

  @Test
  fun endLtr_sitsRightOfAnchorAndVerticallyCentered() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.END)

    assertEquals(anchor.right, offset.x)
    assertEquals(centeredY(popup.height), offset.y)
  }

  @Test
  fun startRtl_sitsRightOfAnchor() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.START, layoutDirection = LayoutDirection.Rtl)

    assertEquals(anchor.right, offset.x)
    // The balloon sits on the anchor's physical right, so the arrow goes on its physical
    // LEFT edge — which under RTL is `END`.
    assertEquals(ArrowOrientation.END, state.resolvedArrowOrientation)
    assertEquals(
      ResolvedArrowSide.LEFT,
      state.resolvedArrowOrientation?.resolve(LayoutDirection.Rtl),
    )
  }

  @Test
  fun dropDown_alignsLeadingEdgesInsteadOfCentering() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.DROP_DOWN)

    // Unlike BOTTOM, the popup starts where the anchor starts.
    assertEquals(anchor.left, offset.x)
    assertEquals(anchor.bottom, offset.y)
    assertEquals(ArrowOrientation.TOP, state.resolvedArrowOrientation)
    assertNotEquals(centeredX(popup.width), offset.x)
  }

  @Test
  fun dropDownRtl_alignsTrailingEdges() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.DROP_DOWN, layoutDirection = LayoutDirection.Rtl)

    assertEquals(anchor.right - popup.width, offset.x)
    assertEquals(anchor.bottom, offset.y)
  }

  @Test
  fun bottomCentering_isExactForOddSizes() {
    // Odd popup width against the even 100-wide anchor: the impl's single-rounding form
    // `left + (100 - 81) / 2` = 209, whereas `anchorCenterX - popup.width / 2` would give
    // 210. Locks the exact rounding so a regression to the two-truncation form is caught.
    val state = stateOf()
    val oddPopup = IntSize(width = 81, height = 41)
    val offset = calc(state, BalloonAlign.BOTTOM, popupSize = oddPopup)

    assertEquals(209, offset.x)
    assertEquals(centeredX(oddPopup.width), offset.x)
    assertEquals(anchor.bottom, offset.y)
  }

  @Test
  fun userOffset_shiftsBasePosition() {
    val state = stateOf()
    val userOffset = IntOffset(x = 12, y = -7)
    val offset = calc(state, BalloonAlign.BOTTOM, userOffset = userOffset)

    assertEquals(centeredX(popup.width) + userOffset.x, offset.x)
    assertEquals(anchor.bottom + userOffset.y, offset.y)
  }

  // ------------------------------------------------------------------ clamping

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
  fun clamp_popupLargerThanWindow_pinsToOrigin() {
    val state = stateOf()
    val window = IntSize(100, 100)
    val hugePopup = IntSize(400, 400)

    val offset = calc(
      state = state,
      align = BalloonAlign.BOTTOM,
      captured = IntRect(10, 10, 40, 40),
      window = window,
      popupSize = hugePopup,
    )

    // maxX / maxY collapse to 0, so the popup pins to the window origin rather than
    // producing a negative offset.
    assertEquals(IntOffset(0, 0), offset)
  }

  // ------------------------------------------------------------------ flipping

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
  fun verticalFlip_topRequested_flipsBelowWhenNoRoomAbove() {
    val state = stateOf()
    val highAnchor = IntRect(left = 200, top = 10, right = 300, bottom = 70)

    val offset = calc(state, BalloonAlign.TOP, captured = highAnchor)

    assertEquals(highAnchor.bottom, offset.y)
    assertEquals(ArrowOrientation.TOP, state.resolvedArrowOrientation)
  }

  @Test
  fun verticalFlip_accountsForUserOffset() {
    val state = stateOf()
    // Below the anchor there is room for the popup (260 + 40 = 300 <= 320) but NOT once
    // the caller's +40px offset is added (340 > 320). The flip test has to see that
    // offset, otherwise the balloon is placed below and then clamped into the anchor.
    val window = IntSize(1000, 320)

    val offset = calc(
      state = state,
      align = BalloonAlign.BOTTOM,
      window = window,
      userOffset = IntOffset(0, 40),
    )

    assertEquals(anchor.top - popup.height + 40, offset.y)
    assertEquals(ArrowOrientation.BOTTOM, state.resolvedArrowOrientation)
  }

  @Test
  fun horizontalFlip_startRequested_flipsRightWhenNoRoomLeft() {
    val state = stateOf()
    val leftAnchor = IntRect(left = 10, top = 200, right = 110, bottom = 260)

    val offset = calc(state, BalloonAlign.START, captured = leftAnchor)

    assertEquals(leftAnchor.right, offset.x)
    assertEquals(ArrowOrientation.START, state.resolvedArrowOrientation)
  }

  @Test
  fun rtlHorizontalFlip_endRequested_flipsToRightWithArrowOnPhysicalLeftEdge() {
    // RTL: END resolves to the LEFT absolute branch (balloon requested on the
    // anchor's physical left). With no room on the left it flips to the right, so
    // the arrow must move to the balloon's physical LEFT edge — which under RTL is
    // ArrowOrientation.END (END.resolve(Rtl) == LEFT).
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

  @Test
  fun noFlip_whenNeitherSideHasRoom() {
    val state = stateOf()
    // No room below (70 + 40 > 100) and none above (10 - 40 < 0): the requested side must
    // be kept and clamped rather than flipped into an equally impossible spot.
    val window = IntSize(1000, 100)
    val tightAnchor = IntRect(left = 200, top = 10, right = 300, bottom = 70)
    val offset = calc(state, BalloonAlign.BOTTOM, captured = tightAnchor, window = window)

    assertEquals(ArrowOrientation.TOP, state.resolvedArrowOrientation)
    assertEquals(window.height - popup.height, offset.y)
  }

  // ------------------------------------------------------------ orientation rules

  @Test
  fun pinnedOrientation_isKeptWhenPlacementDoesNotFlip() {
    val state = stateOf(BalloonStyle(arrowOrientation = ArrowOrientation.END))
    calc(state, BalloonAlign.BOTTOM)

    assertEquals(ArrowOrientation.END, state.resolvedArrowOrientation)
  }

  @Test
  fun pinnedOrientation_followsTheBalloonOnFlip_underAlignAnchorRule() {
    val state = stateOf(
      BalloonStyle(
        arrowOrientation = ArrowOrientation.TOP,
        arrowOrientationRules = ArrowOrientationRules.ALIGN_ANCHOR,
      ),
    )
    // No room below -> flips above -> the arrow has to move to the BOTTOM edge to keep
    // pointing at the anchor, overriding the pin.
    calc(state, BalloonAlign.BOTTOM, window = IntSize(1000, 280))

    assertEquals(ArrowOrientation.BOTTOM, state.resolvedArrowOrientation)
  }

  @Test
  fun pinnedCrossAxisOrientation_isNotDraggedAcrossAxesByAFlip() {
    // A vertical flip must not move a horizontally-pinned arrow to a horizontal edge: the
    // arrow axis is what decides how the popup reserves space, so the popup's own size —
    // the input the flip decision was made from — would change under it, and the two would
    // chase each other across measure passes.
    val state = stateOf(
      BalloonStyle(
        arrowOrientation = ArrowOrientation.END,
        arrowOrientationRules = ArrowOrientationRules.ALIGN_ANCHOR,
      ),
    )
    val offset = calc(state, BalloonAlign.BOTTOM, window = IntSize(1000, 280))

    // The balloon still flips above the anchor...
    assertEquals(anchor.top - popup.height, offset.y)
    // ...but the arrow stays on the edge the caller pinned.
    assertEquals(ArrowOrientation.END, state.resolvedArrowOrientation)
  }

  @Test
  fun pinnedOrientation_survivesFlip_underAlignFixedRule() {
    val state = stateOf(
      BalloonStyle(
        arrowOrientation = ArrowOrientation.TOP,
        arrowOrientationRules = ArrowOrientationRules.ALIGN_FIXED,
      ),
    )
    calc(state, BalloonAlign.BOTTOM, window = IntSize(1000, 280))

    assertEquals(ArrowOrientation.TOP, state.resolvedArrowOrientation)
  }

  // --------------------------------------------------------------- center align

  @Test
  fun centerAlignTop_putsPopupBottomAtAnchorVerticalCenter() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.CENTER, centerAlign = BalloonCenterAlign.TOP)

    assertEquals(centeredX(popup.width), offset.x)
    // popup bottom edge lands on the anchor's vertical center.
    assertEquals(anchorCenterY, offset.y + popup.height)
    // Arrow points back down at the anchor center.
    assertEquals(ArrowOrientation.BOTTOM, state.resolvedArrowOrientation)
  }

  @Test
  fun centerAlignBottom_putsPopupTopAtAnchorVerticalCenter() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.CENTER, centerAlign = BalloonCenterAlign.BOTTOM)

    assertEquals(centeredX(popup.width), offset.x)
    assertEquals(anchorCenterY, offset.y)
    assertEquals(ArrowOrientation.TOP, state.resolvedArrowOrientation)
  }

  @Test
  fun centerAlignStart_putsPopupRightEdgeAtAnchorHorizontalCenter() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.CENTER, centerAlign = BalloonCenterAlign.START)

    val anchorCenterX = anchor.left + (anchor.width * 0.5f).toInt()
    assertEquals(anchorCenterX, offset.x + popup.width)
    assertEquals(ArrowOrientation.END, state.resolvedArrowOrientation)
  }

  @Test
  fun centerOverlay_withoutCenterAlign_sitsDeadCenterOnAnchor() {
    val state = stateOf()
    val offset = calc(state, BalloonAlign.CENTER, centerAlign = null)

    assertEquals(centeredX(popup.width), offset.x)
    assertEquals(centeredY(popup.height), offset.y)
  }

  // ------------------------------------------------- first-frame fallback agreement

  @Test
  fun firstFrameFallbackOrientation_agreesWithTheProvider_inBothDirections() {
    // Before the provider's first pass, `BalloonPopupLayer` draws with the align-derived
    // orientation. If the two disagree, the arrow jumps edges after one frame, and because
    // the reserve is taken from the arrow side the whole balloon shifts with it. The bug
    // this pins: the fallback used to mirror START/END itself, on top of the mirroring
    // `ArrowOrientation.resolve` already does, so RTL came out inverted.
    listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction ->
      listOf(BalloonAlign.TOP, BalloonAlign.BOTTOM, BalloonAlign.START, BalloonAlign.END)
        .forEach { align ->
          val state = stateOf()
          calc(state, align, layoutDirection = direction)
          assertEquals(
            state.resolvedArrowOrientation,
            resolveArrowOrientation(align, null, state.style),
            "fallback disagrees with the provider for $align under $direction",
          )
        }
    }
  }

  @Test
  fun firstFrameFallbackOrientation_agreesForCenterAlign_inBothDirections() {
    listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { direction ->
      BalloonCenterAlign.entries.forEach { centerAlign ->
        val state = stateOf()
        calc(state, BalloonAlign.CENTER, centerAlign = centerAlign, layoutDirection = direction)
        assertEquals(
          state.resolvedArrowOrientation,
          resolveArrowOrientation(BalloonAlign.CENTER, centerAlign, state.style),
          "fallback disagrees with the provider for $centerAlign under $direction",
        )
      }
    }
  }

  @Test
  fun sideAlignedArrow_alwaysResolvesToTheEdgeFacingTheAnchor() {
    // START puts the balloon on the leading side, so its arrow belongs on the trailing edge,
    // whichever way the layout runs.
    assertEquals(
      ResolvedArrowSide.RIGHT,
      resolveArrowOrientation(BalloonAlign.START, null, BalloonStyle())
        .resolve(LayoutDirection.Ltr),
    )
    assertEquals(
      ResolvedArrowSide.LEFT,
      resolveArrowOrientation(BalloonAlign.START, null, BalloonStyle())
        .resolve(LayoutDirection.Rtl),
    )
  }

  // ----------------------------------------------------------------- arrow center

  @Test
  fun alignBalloon_measuresArrowAgainstTheWrapperNotTheCard() {
    // `Balloon.getArrowConstraintPositionX` centers the arrow at `wrapperWidth * position`
    // in WRAPPER space, and the card is inset from that wrapper by `elevation` on each
    // side. So for a popup 80 wide with the default 2dp elevation the card is 76 wide, and
    // position 0.25 lands at 80*0.25 - 2 = 18 from the card's left edge — NOT at 19, which
    // is what measuring against the card would give.
    val state = stateOf(
      BalloonStyle(
        arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
        arrowPosition = 0.25f,
      ),
    )
    calc(state, BalloonAlign.BOTTOM)

    assertEquals(18f, state.resolvedArrowCenterPx)
  }

  @Test
  fun alignBalloon_halfPosition_landsDeadCentreOfTheCard() {
    val state = stateOf(
      BalloonStyle(
        arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
        arrowPosition = 0.5f,
      ),
    )
    calc(state, BalloonAlign.BOTTOM)

    // wrapper 80 -> 40; card inset 2 -> 38; card width is 80 - 2*2 = 76, whose centre is 38.
    assertEquals(38f, state.resolvedArrowCenterPx)
  }

  @Test
  fun alignAnchor_pointsArrowAtTheAnchor_whenAnchorFitsInsideBalloon() {
    // Anchor 40 wide inside a 200-wide popup: the arrow tracks the anchor exactly.
    val narrowAnchor = IntRect(left = 380, top = 200, right = 420, bottom = 260)
    val widePopup = IntSize(200, 40)
    val state = stateOf(
      BalloonStyle(
        arrowPositionRules = ArrowPositionRules.ALIGN_ANCHOR,
        arrowPosition = 0.5f,
      ),
    )

    val offset = calc(
      state,
      BalloonAlign.BOTTOM,
      captured = narrowAnchor,
      popupSize = widePopup,
    )

    // The arrow's absolute centre must land on the anchor's centre.
    val cardLeft = offset.x + 2 // margin 0 + elevation inset
    assertEquals(400f, cardLeft + state.resolvedArrowCenterPx!!)
  }

  @Test
  fun alignAnchor_reAnchorsArrow_whenClampShiftsThePopup() {
    // A wide popup against the right window edge: its centered base x exceeds maxX and is
    // clamped left, so ALIGN_ANCHOR must re-anchor the arrow rightward toward the anchor
    // instead of staying at the body-relative centre.
    val window = IntSize(1000, 1000)
    val widePopup = IntSize(200, 40)
    val rightAnchor = IntRect(left = 900, top = 100, right = 1000, bottom = 160)

    val anchorState = stateOf(
      BalloonStyle(arrowPositionRules = ArrowPositionRules.ALIGN_ANCHOR),
    )
    val balloonState = stateOf(
      BalloonStyle(arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON),
    )

    val offset = calc(
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

    // ALIGN_BALLOON: centre of the card regardless of the shift (200/2 - 2 = 98).
    assertEquals(98f, balloonState.resolvedArrowCenterPx)
    // ALIGN_ANCHOR: the arrow keeps pointing at the anchor centre (950).
    val cardLeft = offset.x + 2
    assertEquals(950f, cardLeft + anchorState.resolvedArrowCenterPx!!)
    assertNotEquals(balloonState.resolvedArrowCenterPx, anchorState.resolvedArrowCenterPx)
  }

  @Test
  fun alignAnchor_widerAnchorFallsBackToTheMinPositionBand() {
    // Anchor wider than the balloon: `getArrowConstraintPositionX`'s band kicks in and
    // keeps the arrow `arrowWidth * ratio + padding` clear of the balloon's start.
    val wideAnchor = IntRect(left = 200, top = 200, right = 600, bottom = 260)
    val state = stateOf(
      BalloonStyle(
        arrowPositionRules = ArrowPositionRules.ALIGN_ANCHOR,
        arrowPosition = 0.45f,
        arrowAlignAnchorPaddingRatio = 2.5f,
      ),
    )

    calc(state, BalloonAlign.BOTTOM, captured = wideAnchor)

    // The natural arrow position (14) sits inside the `2 * arrowWidth` band, so it snaps to
    // minPosition = arrowBase(12) * 2.5 = 30 (arrow LEFT), + half arrow (6), - inset (2).
    assertEquals(34f, state.resolvedArrowCenterPx)
  }

  @Test
  fun alignAnchor_honoursArrowAlignAnchorPadding() {
    val wideAnchor = IntRect(left = 200, top = 200, right = 600, bottom = 260)
    val state = stateOf(
      BalloonStyle(
        arrowPositionRules = ArrowPositionRules.ALIGN_ANCHOR,
        arrowPosition = 0.45f,
        arrowAlignAnchorPadding = 8.dp,
      ),
    )

    calc(state, BalloonAlign.BOTTOM, captured = wideAnchor)

    // minPosition = 12 * 2.5 + 8 = 38; + 6 - 2 = 42.
    assertEquals(42f, state.resolvedArrowCenterPx)
  }

  @Test
  fun arrowCenter_forSideAlignedBalloon_usesTheVerticalAxis() {
    // A START/END balloon's arrow runs down the vertical edge, so the resolved centre is
    // measured from the card's TOP.
    val state = stateOf(
      BalloonStyle(
        arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
        arrowPosition = 0.25f,
      ),
    )
    calc(state, BalloonAlign.END)

    // wrapper height 40 -> 10; minus the 2px card inset -> 8.
    assertEquals(8f, state.resolvedArrowCenterPx)
  }

  @Test
  fun arrowCenter_scalesWithElevation() {
    // The card inset is the elevation, so a zero-elevation balloon measures the arrow
    // against the card directly.
    val state = stateOf(
      BalloonStyle(
        elevation = 0.dp,
        arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
        arrowPosition = 0.25f,
      ),
    )
    calc(state, BalloonAlign.BOTTOM)

    assertEquals(20f, state.resolvedArrowCenterPx)
  }

  @Test
  fun arrowCenter_accountsForMargins() {
    // Margins shrink the wrapper the arrow position is measured against.
    val state = stateOf(
      BalloonStyle(
        margin = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
        arrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
        arrowPosition = 0.5f,
      ),
    )
    calc(state, BalloonAlign.BOTTOM)

    // wrapper = 80 - 20 = 60; 60*0.5 = 30; minus the 2px inset -> 28.
    assertEquals(28f, state.resolvedArrowCenterPx)
  }
}
