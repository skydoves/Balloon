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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Geometry tests for the balloon silhouette — the pure path builders behind [BalloonShape].
 *
 * These pin down the numbers a screenshot comparison against the View implementation
 * measured: the arrow protrudes `arrowHeight - 1px` (the 1px being `SIZE_ARROW_BOUNDARY`,
 * the seam the original overlaps its arrow `ImageView` onto the card by), the body rect is
 * inset by exactly that much on the arrow side and by nothing on the other three, and the
 * arrow base never wanders into a rounded corner.
 */
class BalloonShapeGeometryTest {

  private val size = Size(200f, 100f)

  private fun path(
    side: ResolvedArrowSide,
    arrowWidth: Float = 20f,
    arrowHeight: Float = 11f,
    cornerRadius: Float = 8f,
    center: Float? = null,
    size: Size = this.size,
  ) = buildBalloonPath(
    size = size,
    cornerRadiusPx = cornerRadius,
    arrowWidthPx = arrowWidth,
    arrowHeightPx = arrowHeight,
    side = side,
    arrowCenterFromRectStart = center,
  )

  private fun assertClose(expected: Float, actual: Float, tolerance: Float = 0.01f) {
    assertTrue(
      abs(expected - actual) <= tolerance,
      "expected $expected but was $actual (tolerance $tolerance)",
    )
  }

  // ---------------------------------------------------------------- protrusion

  @Test
  fun protrusion_isArrowHeightMinusTheOnePixelSeam() {
    assertEquals(11f, arrowProtrusionPx(12f))
    assertEquals(0f, arrowProtrusionPx(1f))
  }

  @Test
  fun protrusion_neverGoesNegative() {
    assertEquals(0f, arrowProtrusionPx(0f))
    assertEquals(0f, arrowProtrusionPx(-5f))
  }

  // --------------------------------------------------------------- body extent

  @Test
  fun topArrow_pathSpansTheWholeBoxAndInsetsOnlyTheTop() {
    val bounds = path(ResolvedArrowSide.TOP).getBounds()

    // The tip reaches y = 0 (the box's top), and the body fills the rest.
    assertClose(0f, bounds.top)
    assertClose(size.height, bounds.bottom)
    assertClose(0f, bounds.left)
    assertClose(size.width, bounds.right)
  }

  @Test
  fun bottomArrow_tipReachesTheBottomOfTheBox() {
    val bounds = path(ResolvedArrowSide.BOTTOM).getBounds()

    assertClose(0f, bounds.top)
    assertClose(size.height, bounds.bottom)
  }

  @Test
  fun leftArrow_tipReachesTheLeftOfTheBox() {
    val bounds = path(ResolvedArrowSide.LEFT).getBounds()

    assertClose(0f, bounds.left)
    assertClose(size.width, bounds.right)
  }

  @Test
  fun rightArrow_tipReachesTheRightOfTheBox() {
    val bounds = path(ResolvedArrowSide.RIGHT).getBounds()

    assertClose(0f, bounds.left)
    assertClose(size.width, bounds.right)
  }

  // ------------------------------------------------------------ no-arrow cases

  @Test
  fun zeroArrowWidth_producesAPlainRoundedRect() {
    val bounds = path(ResolvedArrowSide.TOP, arrowWidth = 0f).getBounds()

    assertClose(0f, bounds.top)
    assertClose(size.height, bounds.bottom)
    assertClose(0f, bounds.left)
    assertClose(size.width, bounds.right)
  }

  @Test
  fun zeroArrowHeight_producesAPlainRoundedRect() {
    val withArrow = path(ResolvedArrowSide.TOP)
    val without = path(ResolvedArrowSide.TOP, arrowHeight = 0f)

    // With a zero-height arrow the notch disappears, so the two paths must differ.
    assertNotEquals(withArrow.getBounds().height, 0f)
    assertClose(size.height, without.getBounds().height)
  }

  @Test
  fun degenerateSize_producesAnEmptyPath() {
    assertTrue(path(ResolvedArrowSide.TOP, size = Size(0f, 100f)).isEmpty)
    assertTrue(path(ResolvedArrowSide.TOP, size = Size(200f, 0f)).isEmpty)
  }

  // ------------------------------------------------------------- arrow triangle

  @Test
  fun arrowTriangle_matchesTheNotchItPaintsOver() {
    val triangle = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 11f,
      side = ResolvedArrowSide.TOP,
      arrowCenterFromRectStart = 60f,
    ).getBounds()

    // A TOP arrow insets only the top, so the body rect still starts at x = 0: the base is
    // 20 wide centred at 60 -> 50..70.
    assertClose(50f, triangle.left)
    assertClose(70f, triangle.right)
    // Tip at the box top, base sunk 1px into the body (rectTop 10 + 1).
    assertClose(0f, triangle.top)
    assertClose(11f, triangle.bottom)
  }

  @Test
  fun arrowTriangle_isEmptyWhenTheArrowIsCollapsed() {
    val zero = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 0f,
      arrowHeightPx = 11f,
      side = ResolvedArrowSide.TOP,
      arrowCenterFromRectStart = null,
    )
    assertTrue(zero.isEmpty)
  }

  @Test
  fun arrowTriangle_forSideArrows_runsDownTheVerticalEdge() {
    val triangle = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 11f,
      side = ResolvedArrowSide.RIGHT,
      arrowCenterFromRectStart = 50f,
    ).getBounds()

    assertClose(40f, triangle.top)
    assertClose(60f, triangle.bottom)
    assertClose(size.width, triangle.right)
  }

  // ------------------------------------------------------------ centre clamping

  @Test
  fun arrowCentre_isClampedInsideTheRoundedCorners() {
    // Asking for the arrow at the very start of the edge must not push its base into the
    // corner curve: it clamps to radius + halfArrow = 8 + 10 = 18 from the body's left.
    val clamped = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 11f,
      side = ResolvedArrowSide.TOP,
      arrowCenterFromRectStart = 0f,
    ).getBounds()

    assertClose(8f, clamped.left)
    assertClose(28f, clamped.right)
  }

  @Test
  fun arrowCentre_null_centresTheArrow() {
    val centered = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 11f,
      side = ResolvedArrowSide.TOP,
      arrowCenterFromRectStart = null,
    ).getBounds()

    assertClose(size.width / 2f, (centered.left + centered.right) / 2f)
  }

  @Test
  fun arrowCentre_fallsBackToTheMidpointWhenTheEdgeIsTooShortToClamp() {
    // A 20px-wide arrow on a 24px-wide box leaves no straight edge at all; the builder
    // has to fall back to the midpoint instead of producing an inverted clamp.
    val tiny = Size(24f, 40f)
    val bounds = buildArrowTrianglePath(
      size = tiny,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 11f,
      side = ResolvedArrowSide.TOP,
      arrowCenterFromRectStart = 0f,
    ).getBounds()

    assertClose(12f, (bounds.left + bounds.right) / 2f)
  }

  // ---------------------------------------------------------- corner clamping

  @Test
  fun cornerRadius_isClampedToHalfTheShorterSide() {
    // A 500px radius on a 200x100 box must not invert the path.
    val bounds = path(ResolvedArrowSide.TOP, cornerRadius = 500f).getBounds()

    assertClose(0f, bounds.left)
    assertClose(size.width, bounds.right)
    assertClose(size.height, bounds.bottom)
  }

  // ------------------------------------------------------------------- shape

  @Test
  fun shape_resolvesStartAndEndAgainstLayoutDirection() {
    assertEquals(ResolvedArrowSide.LEFT, ArrowOrientation.START.resolve(LayoutDirection.Ltr))
    assertEquals(ResolvedArrowSide.RIGHT, ArrowOrientation.START.resolve(LayoutDirection.Rtl))
    assertEquals(ResolvedArrowSide.RIGHT, ArrowOrientation.END.resolve(LayoutDirection.Ltr))
    assertEquals(ResolvedArrowSide.LEFT, ArrowOrientation.END.resolve(LayoutDirection.Rtl))

    // The silhouettes fill the same box either way (the tip reaches the box edge), so the
    // mirror shows up in where the notch is: compare the triangle the notch is cut with.
    val ltrTip = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 12f,
      side = ArrowOrientation.START.resolve(LayoutDirection.Ltr),
      arrowCenterFromRectStart = 50f,
    ).getBounds()
    val rtlTip = buildArrowTrianglePath(
      size = size,
      cornerRadiusPx = 8f,
      arrowWidthPx = 20f,
      arrowHeightPx = 12f,
      side = ArrowOrientation.START.resolve(LayoutDirection.Rtl),
      arrowCenterFromRectStart = 50f,
    ).getBounds()

    assertClose(0f, ltrTip.left)
    assertClose(size.width, rtlTip.right)
    assertNotEquals(ltrTip, rtlTip)
  }

  @Test
  fun shape_producesAGenericOutline() {
    val outline = BalloonShape(
      cornerRadius = 8.dp,
      arrowWidth = 20.dp,
      arrowHeight = 12.dp,
      arrowOrientation = ArrowOrientation.TOP,
    ).createOutline(size, LayoutDirection.Ltr, Density(1f))

    assertTrue(outline is Outline.Generic)
  }

  @Test
  fun shape_equalityTracksEveryInput() {
    fun shape(center: Float?) = BalloonShape(
      cornerRadius = 8.dp,
      arrowWidth = 20.dp,
      arrowHeight = 12.dp,
      arrowOrientation = ArrowOrientation.TOP,
      arrowCenterFromRectStart = center,
    )

    assertEquals(shape(10f), shape(10f))
    assertEquals(shape(10f).hashCode(), shape(10f).hashCode())
    assertNotEquals(shape(10f), shape(11f))
    assertNotEquals(shape(10f), shape(null))
  }

  // ------------------------------------------------------------------ reserve

  @Test
  fun reserve_splitsTheBoxTheWayTheViewImplementationDoes() {
    val style = BalloonStyle(arrowSize = androidx.compose.ui.unit.DpSize(12.dp, 12.dp))
    val reserve = style.reserve(ResolvedArrowSide.TOP, Density(1f))

    assertEquals(11.dp, reserve.protrusion) // arrowHeight - 1px
    assertEquals(11.dp, reserve.farSide) // max(protrusion, elevation)
    assertEquals(2.dp, reserve.cross) // the elevation inset
    assertEquals(4.dp, reserve.horizontalOuter) // 2 * elevation for a vertical arrow
    assertEquals(11.dp, reserve.verticalOuter)
  }

  @Test
  fun reserve_swapsAxesForASideArrow() {
    val reserve = BalloonStyle().reserve(ResolvedArrowSide.LEFT, Density(1f))

    assertEquals(11.dp, reserve.horizontalOuter)
    assertEquals(4.dp, reserve.verticalOuter)
  }

  @Test
  fun reserve_collapsesToTheElevationInsetWhenTheArrowIsHidden() {
    // A hidden arrow releases the space the View implementation keeps reserving, so the body
    // sits flush against its anchor instead of an arrow-sized gap away from it.
    val reserve = BalloonStyle(isArrowVisible = false).reserve(ResolvedArrowSide.TOP, Density(1f))

    assertEquals(0.dp, reserve.protrusion)
    assertEquals(2.dp, reserve.farSide)
    assertEquals(4.dp, reserve.horizontalOuter)
    assertEquals(2.dp, reserve.verticalOuter)
  }

  @Test
  fun reserve_followsTheElevation() {
    val reserve = BalloonStyle(elevation = 20.dp).reserve(ResolvedArrowSide.TOP, Density(1f))

    assertEquals(20.dp, reserve.cross)
    assertEquals(40.dp, reserve.horizontalOuter)
    // The far side takes the larger of the protrusion and the elevation.
    assertEquals(20.dp, reserve.farSide)
  }
}
