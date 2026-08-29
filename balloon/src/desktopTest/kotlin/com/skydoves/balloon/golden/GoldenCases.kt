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

package com.skydoves.balloon.golden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.BalloonOverlayAnimation
import com.skydoves.balloon.BalloonOverlayShape
import com.skydoves.balloon.BalloonStyle

// ---------------------------------------------------------------------------- fixtures

/**
 * Builds a case's style, always through [Balloon.Builder].
 *
 * Going through the builder rather than constructing a [BalloonStyle] directly is what makes
 * these goldens cover the migration surface: a setter that stops landing on the field it
 * names is exactly the kind of port bug this suite exists to catch, and it would be invisible
 * to a matrix that set the fields itself.
 *
 * [BalloonAnimation.NONE] is not a stylistic choice. A golden captured while an enter
 * transition is still running pins a frame nothing reproduces, so the whole suite renders
 * settled balloons only. The same reasoning leaves the highlight, rotate and auto-dismiss
 * knobs at their inert defaults everywhere below.
 */
private fun style(block: Balloon.Builder.() -> Unit = {}): BalloonStyle =
  Balloon.Builder()
    .setBalloonAnimation(BalloonAnimation.NONE)
    .apply(block)
    .build()

/** A style with a visible scrim and, again, no fade to catch mid-frame. */
private fun overlayStyle(block: Balloon.Builder.() -> Unit = {}): BalloonStyle = style {
  setIsVisibleOverlay(true)
  setBalloonOverlayAnimation(BalloonOverlayAnimation.NONE)
  setOverlayColor(scrim)
  block()
}

/** `ALIGN_ANCHOR` positioning, the rule whose clamp band the wide-anchor cases explore. */
private fun alignAnchorStyle(block: Balloon.Builder.() -> Unit = {}): BalloonStyle = style {
  setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
  block()
}

/** Shorthand so the RTL rows stay inside the line budget. */
private val rtl = LayoutDirection.Rtl

private val red = Color(0xFFE53935)
private val blue = Color(0xFF1E88E5)
private val green = Color(0xFF43A047)
private val amber = Color(0xFFFFC107)
private val scrim = Color(0x99000000)

/** Wider than the balloon, which is what pushes `ALIGN_ANCHOR` into its constraint band. */
private val wideAnchor = DpSize(240.dp, 32.dp)

/** The vertical twin of [wideAnchor], for arrows that run down a side edge. */
private val tallAnchor = DpSize(32.dp, 240.dp)

/** Big enough for an overlay cut-out to have a shape worth looking at. */
private val bigAnchor = DpSize(200.dp, 120.dp)

private fun wideAnchorCase(name: String, style: BalloonStyle): GoldenCase =
  GoldenCase(name = name, style = style, anchorSize = wideAnchor)

/**
 * The five-point `arrowPosition` sweep, generated so the two rules and the two axes stay
 * genuinely comparable rather than drifting apart as someone edits one list by hand.
 */
private fun arrowPositionSweep(
  prefix: String,
  rules: ArrowPositionRules,
  align: BalloonAlign,
  anchorSize: DpSize,
): List<GoldenCase> {
  // `arrowCenterAlong` clamps the centre to `radius + arrowWidth / 2` from either end, so the
  // range the fraction can actually move over is the body's extent along the arrow's edge
  // minus 22dp. The default 96x36 body leaves 74dp horizontally but only 14dp vertically,
  // which is tight enough that three of the five fractions land on the same pixel. A side
  // arrow therefore gets a tall body, so every fraction in the sweep is distinguishable.
  val alongVertical = align == BalloonAlign.START || align == BalloonAlign.END
  return listOf(
    0f to "000",
    0.25f to "025",
    0.5f to "050",
    0.75f to "075",
    1f to "100",
  ).map { (position, suffix) ->
    GoldenCase(
      name = "$prefix-$suffix",
      style = style {
        setArrowPosition(position)
        setArrowPositionRules(rules)
      },
      align = align,
      anchorSize = anchorSize,
      content = if (alongVertical) {
        { GoldenBody(width = 72, height = 160) }
      } else {
        { GoldenBody() }
      },
    )
  }
}

/** A body with several children, used by the content group. */
@Composable
private fun MultiElementBody() {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      Box(Modifier.size(24.dp).background(red))
      Box(Modifier.size(24.dp).background(blue))
      Box(Modifier.size(24.dp).background(green))
    }
    Box(Modifier.size(104.dp, 10.dp).background(amber))
    Box(Modifier.size(64.dp, 10.dp).background(Color.White))
  }
}

// ------------------------------------------------------------------------------- groups

/**
 * Catches an arrow that no longer agrees with the space reserved for it.
 *
 * The notch is cut into the body path while the protrusion is held by the popup box, so a
 * regression in either shows up as a wedge that has drifted off the card or as a card that
 * has stopped making room for one. The height sweep is what pins the `arrowHeight - 1px`
 * protrusion; the width sweep pins the base. The last rows are the ones that used to break
 * outright: a zero extent has to collapse the reserve entirely rather than leave a gap, and
 * a base wider than the edge it sits on has to clamp instead of emitting vertices outside the
 * layout bounds, which is the case the View original throws on.
 */
private val arrowSizeCases: List<GoldenCase> = listOf(
  GoldenCase("arrow-size-zero", style { setArrowSize(0.dp) }),
  GoldenCase("arrow-size-w0-h12", style { setArrowSize(0.dp, 12.dp) }),
  GoldenCase("arrow-size-w12-h0", style { setArrowSize(12.dp, 0.dp) }),
  GoldenCase("arrow-size-w12-h12", style()),
  GoldenCase("arrow-size-w20-h12", style { setArrowWidth(20.dp) }),
  GoldenCase("arrow-size-w32-h12", style { setArrowWidth(32.dp) }),
  GoldenCase("arrow-size-w12-h4", style { setArrowHeight(4.dp) }),
  GoldenCase("arrow-size-w12-h20", style { setArrowHeight(20.dp) }),
  GoldenCase("arrow-size-w12-h32", style { setArrowHeight(32.dp) }),
  GoldenCase("arrow-size-w28-h6", style { setArrowSize(28.dp, 6.dp) }),
  GoldenCase("arrow-size-w6-h28", style { setArrowSize(6.dp, 28.dp) }),
  GoldenCase(
    name = "arrow-size-wider-than-edge-horizontal",
    style = style { setArrowSize(200.dp, 16.dp) },
    content = { GoldenBody(40, 24) },
  ),
  GoldenCase(
    name = "arrow-size-wider-than-edge-vertical",
    style = style { setArrowSize(200.dp, 16.dp) },
    align = BalloonAlign.END,
    content = { GoldenBody(40, 24) },
  ),
  GoldenCase(
    name = "arrow-size-wider-than-edge-colored",
    style = style {
      setArrowSize(200.dp, 16.dp)
      setArrowColor(red)
    },
    content = { GoldenBody(40, 24) },
  ),
)

/**
 * Catches a hidden arrow that still costs space, or a visible one that draws none.
 *
 * Hiding the arrow has to release the protrusion as well as the triangle, so the card sits
 * flush against the anchor; a regression that only stops painting leaves the balloon hovering
 * a notch away from where it belongs. The zero-sized-but-flagged-visible row is the other
 * half of that contract, routed through `effectiveArrowSize`.
 */
private val arrowVisibilityCases: List<GoldenCase> = listOf(
  GoldenCase("arrow-visible-off", style { setIsVisibleArrow(false) }),
  GoldenCase(
    name = "arrow-visible-off-rounded",
    style = style {
      setIsVisibleArrow(false)
      setCornerRadius(16.dp)
    },
  ),
  GoldenCase(
    name = "arrow-visible-off-side",
    style = style { setIsVisibleArrow(false) },
    align = BalloonAlign.END,
  ),
  GoldenCase(
    name = "arrow-visible-on-but-zero-sized",
    style = style {
      setIsVisibleArrow(true)
      setArrowSize(0.dp, 24.dp)
    },
  ),
  GoldenCase(
    name = "arrow-visible-off-with-elevation",
    style = style {
      setIsVisibleArrow(false)
      setElevation(12.dp)
    },
  ),
)

/**
 * Catches an arrow that ends up on the wrong edge.
 *
 * A pinned orientation has to survive the align-derived default, and the two
 * [ArrowOrientationRules] have to disagree exactly where the position provider flips the
 * balloon for lack of room: `ALIGN_ANCHOR` lets the arrow follow the balloon so it keeps
 * pointing at the anchor, `ALIGN_FIXED` leaves it where the caller put it. The cross-axis row
 * guards the subtler rule that a flip never moves the arrow to the other axis, because the
 * axis is what decides how the popup reserves space, and letting it change would make the
 * size and the flip decision chase each other across measure passes.
 */
private val arrowOrientationCases: List<GoldenCase> = listOf(
  GoldenCase("arrow-orientation-top", style { setArrowOrientation(ArrowOrientation.TOP) }),
  GoldenCase(
    name = "arrow-orientation-bottom",
    style = style { setArrowOrientation(ArrowOrientation.BOTTOM) },
  ),
  GoldenCase("arrow-orientation-start", style { setArrowOrientation(ArrowOrientation.START) }),
  GoldenCase("arrow-orientation-end", style { setArrowOrientation(ArrowOrientation.END) }),
  GoldenCase(
    name = "arrow-orientation-start-on-side-align",
    style = style { setArrowOrientation(ArrowOrientation.START) },
    align = BalloonAlign.END,
  ),
  GoldenCase(
    name = "arrow-orientation-rules-anchor-follows-flip",
    style = style {
      setArrowOrientation(ArrowOrientation.TOP)
      setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR)
    },
    anchorAlignment = Alignment.BottomCenter,
  ),
  GoldenCase(
    name = "arrow-orientation-rules-fixed-ignores-flip",
    style = style {
      setArrowOrientation(ArrowOrientation.TOP)
      setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
    },
    anchorAlignment = Alignment.BottomCenter,
  ),
  GoldenCase(
    name = "arrow-orientation-rules-anchor-keeps-cross-axis",
    style = style {
      setArrowOrientation(ArrowOrientation.START)
      setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR)
    },
    anchorAlignment = Alignment.BottomCenter,
  ),
  GoldenCase(
    name = "arrow-orientation-rules-anchor-horizontal-flip",
    style = style {
      setArrowOrientation(ArrowOrientation.END)
      setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR)
    },
    align = BalloonAlign.START,
    anchorAlignment = Alignment.CenterStart,
  ),
)

/**
 * Catches an arrow that slides along its edge to the wrong place.
 *
 * `ALIGN_BALLOON` measures the fraction against the balloon's own wrapper and `ALIGN_ANCHOR`
 * against the anchor, so the two sweeps only agree at `0.5f` and a regression that confuses
 * them is invisible unless both are captured. The `0f` and `1f` ends are where the corner
 * clamp lives: the arrow base has to stay on the straight part of the edge instead of
 * floating clear of the body, which is what the View original does. Both axes are swept
 * because the vertical arrow runs through the same code with the extents transposed.
 */
private val arrowPositionCases: List<GoldenCase> =
  arrowPositionSweep(
    prefix = "arrow-pos-balloon-h",
    rules = ArrowPositionRules.ALIGN_BALLOON,
    align = BalloonAlign.BOTTOM,
    anchorSize = DpSize(64.dp, 32.dp),
  ) +
    arrowPositionSweep(
      prefix = "arrow-pos-balloon-v",
      rules = ArrowPositionRules.ALIGN_BALLOON,
      align = BalloonAlign.END,
      anchorSize = DpSize(32.dp, 64.dp),
    ) +
    arrowPositionSweep(
      prefix = "arrow-pos-anchor-h",
      rules = ArrowPositionRules.ALIGN_ANCHOR,
      align = BalloonAlign.BOTTOM,
      anchorSize = DpSize(64.dp, 32.dp),
    ) +
    arrowPositionSweep(
      prefix = "arrow-pos-anchor-v",
      rules = ArrowPositionRules.ALIGN_ANCHOR,
      align = BalloonAlign.END,
      anchorSize = DpSize(32.dp, 64.dp),
    )

/**
 * Catches the `ALIGN_ANCHOR` clamp band collapsing or running away.
 *
 * `minPosition` is `arrowWidth * ratio + padding`, and it only bites when the arrow WANTS to
 * sit near one of the card's ends. A wide anchor alone is not enough: with the default
 * `arrowPosition` of `0.5f` the tip lands on the anchor's centre, which is also the balloon's
 * centre, so the clamp never engages and every value here renders identically. Each case
 * below therefore drives the arrow to the anchor's leading edge as well, which is the only
 * configuration where the padding and the ratio are observable at all.
 *
 * A ratio of `0` with no padding is the degenerate end: nothing keeps the arrow off the
 * corner except the shape builder's own radius clamp.
 */
private fun anchorClampStyle(block: Balloon.Builder.() -> Unit = {}): BalloonStyle =
  alignAnchorStyle {
    // 0.35f, not 0f. The band is narrower than it looks: `resolveArrowCenter` only reaches
    // its `minPosition` arm when the anchor is wider than the popup AND the arrow's wanted
    // position is within a double arrow-width of the card's start. Push the arrow further
    // left than that and an earlier arm pins it flush at `0f` without consulting the padding
    // at all, which makes every value in this group render identically.
    setArrowPosition(0.35f)
    block()
  }

/** The `maxPosition` twin of [anchorClampStyle], on the far end of the same band. */
private fun anchorClampEndStyle(block: Balloon.Builder.() -> Unit = {}): BalloonStyle =
  alignAnchorStyle {
    setArrowPosition(0.7f)
    block()
  }
private val arrowAnchorPaddingCases: List<GoldenCase> = listOf(
  wideAnchorCase("arrow-anchor-pad-8", anchorClampStyle { setArrowAlignAnchorPadding(8.dp) }),
  wideAnchorCase("arrow-anchor-pad-24", anchorClampStyle { setArrowAlignAnchorPadding(24.dp) }),
  wideAnchorCase("arrow-anchor-pad-48", anchorClampStyle { setArrowAlignAnchorPadding(48.dp) }),
  wideAnchorCase(
    name = "arrow-anchor-ratio-0",
    style = anchorClampStyle { setArrowAlignAnchorPaddingRatio(0f) },
  ),
  wideAnchorCase(
    name = "arrow-anchor-ratio-1",
    style = anchorClampStyle { setArrowAlignAnchorPaddingRatio(1f) },
  ),
  wideAnchorCase("arrow-anchor-ratio-2p5", anchorClampStyle()),
  wideAnchorCase(
    name = "arrow-anchor-ratio-5",
    style = anchorClampStyle { setArrowAlignAnchorPaddingRatio(5f) },
  ),
  wideAnchorCase(
    name = "arrow-anchor-pad-and-ratio",
    style = anchorClampStyle {
      setArrowAlignAnchorPadding(16.dp)
      setArrowAlignAnchorPaddingRatio(4f)
    },
  ),
  wideAnchorCase(
    name = "arrow-anchor-end-pad-8",
    style = anchorClampEndStyle { setArrowAlignAnchorPadding(8.dp) },
  ),
  wideAnchorCase(
    name = "arrow-anchor-end-pad-48",
    style = anchorClampEndStyle { setArrowAlignAnchorPadding(48.dp) },
  ),
  wideAnchorCase("arrow-anchor-wide-pos-000", alignAnchorStyle { setArrowPosition(0f) }),
  wideAnchorCase("arrow-anchor-wide-pos-100", alignAnchorStyle { setArrowPosition(1f) }),
  wideAnchorCase(
    name = "arrow-anchor-wide-arrow-and-pad",
    style = alignAnchorStyle {
      setArrowSize(24.dp, 16.dp)
      setArrowAlignAnchorPadding(12.dp)
    },
  ),
  // The vertical band sits at a different fraction than the horizontal one: the popup is much
  // shorter than it is wide, so the arrow reaches the `minPosition` arm later down the anchor.
  // A tall body as well as a tall anchor: with the default 36dp body the shape builder's own
  // radius clamp swallows the whole padding band before it can be seen.
  GoldenCase(
    name = "arrow-anchor-vertical-tall",
    style = alignAnchorStyle { setArrowPosition(0.35f) },
    align = BalloonAlign.END,
    anchorSize = tallAnchor,
    content = { GoldenBody(width = 72, height = 160) },
  ),
  GoldenCase(
    name = "arrow-anchor-vertical-tall-pad-32",
    style = alignAnchorStyle {
      setArrowPosition(0.35f)
      setArrowAlignAnchorPadding(32.dp)
    },
    align = BalloonAlign.END,
    anchorSize = tallAnchor,
    content = { GoldenBody(width = 72, height = 160) },
  ),
)

/**
 * Catches the separately painted arrow drifting away from the notch it fills.
 *
 * A distinct arrow colour is the only configuration that draws the triangle twice, once as
 * part of the body path and once as an overlay, and the two are clamped independently. Let
 * them disagree and a seam of background colour opens along the base or the wedge slides off
 * the corner. The same-colour row is the control: it must take the single-pass path.
 */
private val arrowColorCases: List<GoldenCase> = listOf(
  GoldenCase("arrow-color-red", style { setArrowColor(red) }),
  GoldenCase("arrow-color-same-as-background", style { setArrowColor(Color.Black) }),
  GoldenCase(
    name = "arrow-color-white-on-blue",
    style = style {
      setBackgroundColor(blue)
      setArrowColor(Color.White)
    },
  ),
  GoldenCase(
    name = "arrow-color-with-rounded-corners",
    style = style {
      setArrowColor(red)
      setCornerRadius(20.dp)
      setArrowPosition(0f)
    },
  ),
  GoldenCase("arrow-color-side", style { setArrowColor(red) }, align = BalloonAlign.END),
  GoldenCase("arrow-color-translucent", style { setArrowColor(Color(0x80E53935)) }),
  GoldenCase(
    name = "arrow-color-with-border",
    style = style {
      setArrowColor(red)
      setBorder(green, 3.dp)
    },
  ),
)

/**
 * Catches a corner radius that stops being clamped to the body it rounds.
 *
 * Beyond half the shorter side the radius has to saturate rather than fold the path in on
 * itself, and the same clamp feeds the arrow's own corner inset, so an over-large radius that
 * escapes it takes the arrow with it. The `0` end matters too: it takes a different branch
 * that emits straight `lineTo` corners instead of quadratics.
 */
private val cornerCases: List<GoldenCase> = listOf(
  GoldenCase("corner-0", style { setCornerRadius(0.dp) }),
  GoldenCase("corner-8", style { setCornerRadius(8.dp) }),
  GoldenCase("corner-16", style { setCornerRadius(16.dp) }),
  GoldenCase("corner-32", style { setCornerRadius(32.dp) }),
  GoldenCase("corner-200-clamped", style { setCornerRadius(200.dp) }),
  GoldenCase(
    name = "corner-200-clamped-no-arrow",
    style = style {
      setCornerRadius(200.dp)
      setIsVisibleArrow(false)
    },
  ),
  GoldenCase("corner-32-side", style { setCornerRadius(32.dp) }, align = BalloonAlign.END),
)

/**
 * Catches the body fill and the layer opacity being applied in the wrong order or place.
 *
 * `alpha` is a whole-layer property, so it has to dim the border and the separately painted
 * arrow along with the body rather than only the background fill. A translucent background
 * with an opaque arrow is the case that exposes a stray extra draw: any double-painting shows
 * up immediately as a darker band where the two overlap.
 */
private val fillCases: List<GoldenCase> = listOf(
  GoldenCase("fill-bg-blue", style { setBackgroundColor(blue) }),
  GoldenCase("fill-bg-translucent", style { setBackgroundColor(Color(0x801E88E5)) }),
  GoldenCase("fill-bg-transparent", style { setBackgroundColor(Color.Transparent) }),
  GoldenCase("fill-alpha-075", style { setAlpha(0.75f) }),
  GoldenCase("fill-alpha-050", style { setAlpha(0.5f) }),
  GoldenCase("fill-alpha-000", style { setAlpha(0f) }),
  GoldenCase(
    name = "fill-alpha-050-with-border",
    style = style {
      setAlpha(0.5f)
      setBorder(red, 4.dp)
    },
  ),
  GoldenCase(
    name = "fill-bg-translucent-arrow-opaque",
    style = style {
      setBackgroundColor(Color(0x801E88E5))
      setArrowColor(red)
    },
  ),
)

/**
 * Catches a border that has stopped hugging the shape.
 *
 * On an `Outline.Generic` the stroke is drawn inner-aligned, so it must sit fully inside the
 * layout bounds without inset and without shortening the arrow tip. A thick border on a small
 * radius is where an off-by-one inset becomes a visible transparent ring, and the two disabled
 * rows pin the contract that either half of the pair being unset turns the border off entirely
 * rather than drawing an unspecified colour.
 */
private val borderCases: List<GoldenCase> = listOf(
  GoldenCase("border-1", style { setBorder(red, 1.dp) }),
  GoldenCase("border-4", style { setBorder(red, 4.dp) }),
  GoldenCase("border-8", style { setBorder(red, 8.dp) }),
  GoldenCase(
    name = "border-with-large-arrow",
    style = style {
      setBorder(green, 4.dp)
      setArrowSize(28.dp, 24.dp)
    },
  ),
  GoldenCase(
    name = "border-no-arrow-rounded",
    style = style {
      setBorder(amber, 4.dp)
      setIsVisibleArrow(false)
    },
  ),
  GoldenCase("border-color-unspecified", style { setBorder(Color.Unspecified, 6.dp) }),
  GoldenCase("border-thickness-zero", style { setBorder(red, 0.dp) }),
  GoldenCase("border-side-arrow", style { setBorder(red, 3.dp) }, align = BalloonAlign.END),
  GoldenCase(
    name = "border-corner-0",
    style = style {
      setBorder(red, 4.dp)
      setCornerRadius(0.dp)
    },
  ),
  GoldenCase(
    name = "border-corner-24",
    style = style {
      setBorder(red, 4.dp)
      setCornerRadius(24.dp)
    },
  ),
)

/**
 * Catches the elevation inset drifting out of the popup box.
 *
 * There is no shadow here, but the inset is still part of the box every width spec measures
 * against, so a ported `setWidthRatio` only lands on the same pixels while the reserve is held
 * on the right sides. A regression shows up as a card that has grown or shrunk by twice the
 * elevation, which is why the width-pinned rows are in this group rather than in sizing.
 */
private val elevationCases: List<GoldenCase> = listOf(
  GoldenCase("elevation-0", style { setElevation(0.dp) }),
  GoldenCase("elevation-12", style { setElevation(12.dp) }),
  GoldenCase("elevation-24", style { setElevation(24.dp) }),
  GoldenCase("elevation-0-side", style { setElevation(0.dp) }, align = BalloonAlign.END),
  GoldenCase("elevation-24-side", style { setElevation(24.dp) }, align = BalloonAlign.END),
  GoldenCase(
    name = "elevation-0-with-width-240",
    style = style {
      setElevation(0.dp)
      setWidth(240.dp)
    },
  ),
  GoldenCase(
    name = "elevation-24-with-width-240",
    style = style {
      setElevation(24.dp)
      setWidth(240.dp)
    },
  ),
)

/**
 * Catches inner padding landing outside the clip or fighting the arrow's own spacing.
 *
 * The arrow protrusion is carved into the shape rather than added around it, so the padding on
 * the arrow side has to be topped up separately; get that wrong and the content creeps into
 * the notch on one edge only. Asymmetric values are what expose a start/end mix-up that a
 * uniform padding hides completely, and the per-axis setters have to leave the other axis
 * alone rather than resetting it.
 */
private val paddingCases: List<GoldenCase> = listOf(
  GoldenCase("padding-12", style { setPadding(12.dp) }),
  GoldenCase("padding-24", style { setPadding(24.dp) }),
  GoldenCase("padding-40", style { setPadding(40.dp) }),
  GoldenCase(
    name = "padding-asymmetric",
    style = style { setPadding(start = 4.dp, top = 20.dp, end = 32.dp, bottom = 8.dp) },
  ),
  GoldenCase("padding-horizontal-only", style { setPaddingHorizontal(28.dp) }),
  GoldenCase("padding-vertical-only", style { setPaddingVertical(28.dp) }),
  GoldenCase("padding-start-only", style { setPaddingStart(32.dp) }),
  GoldenCase("padding-bottom-only", style { setPaddingBottom(32.dp) }),
  GoldenCase(
    name = "padding-24-side-arrow",
    style = style { setPadding(24.dp) },
    align = BalloonAlign.END,
  ),
)

/**
 * Catches the outer margin failing to do both of its jobs.
 *
 * The margin has to inset the on-screen clamp AND shrink the width available to the body, so a
 * regression is either a balloon that touches the window edge it was told to stay off, or one
 * whose card ignores the margin while the popup box still reserves it. The edge rows are where
 * those two failures actually separate; in the middle of the window they look alike.
 */
private val marginCases: List<GoldenCase> = listOf(
  GoldenCase("margin-12", style { setMargin(12.dp) }),
  GoldenCase("margin-32", style { setMargin(32.dp) }),
  GoldenCase(
    name = "margin-asymmetric",
    style = style { setMargin(start = 4.dp, top = 24.dp, end = 40.dp, bottom = 8.dp) },
  ),
  GoldenCase("margin-horizontal-only", style { setMarginHorizontal(32.dp) }),
  GoldenCase("margin-vertical-only", style { setMarginVertical(32.dp) }),
  GoldenCase(
    name = "margin-32-at-bottom-end",
    style = style { setMargin(32.dp) },
    anchorAlignment = Alignment.BottomEnd,
  ),
  GoldenCase(
    name = "margin-32-at-top-start",
    style = style { setMargin(32.dp) },
    anchorAlignment = Alignment.TopStart,
  ),
  GoldenCase(
    name = "margin-48-with-widthratio-1",
    style = style {
      setMargin(48.dp)
      setWidthRatio(1f)
    },
  ),
  GoldenCase(
    name = "margin-asymmetric-side-arrow",
    style = style { setMargin(start = 4.dp, top = 24.dp, end = 40.dp, bottom = 8.dp) },
    align = BalloonAlign.END,
  ),
)

/**
 * Catches the width specs being applied to the wrong box or in the wrong order.
 *
 * Every spec sizes the POPUP, which is the visible card plus the margin and the elevation
 * inset, so a regression that applies one to the card instead makes the balloon wider than
 * asked by exactly that reserve. The precedence rows are the ones no single-knob case can
 * cover: `widthRatio` outranks everything, the min/max ratios outrank `setWidth`, and
 * `setWidth` is exact rather than a bound, so it ignores `minWidth`/`maxWidth` entirely.
 */
private val sizeCases: List<GoldenCase> = listOf(
  GoldenCase("size-wrap-wide-content", style(), content = { GoldenBody(320, 36) }),
  GoldenCase("size-width-240", style { setWidth(240.dp) }),
  GoldenCase("size-width-1200-clamped", style { setWidth(1200.dp) }),
  GoldenCase("size-widthratio-025", style { setWidthRatio(0.25f) }),
  GoldenCase("size-widthratio-050", style { setWidthRatio(0.5f) }),
  GoldenCase("size-widthratio-075", style { setWidthRatio(0.75f) }),
  GoldenCase("size-widthratio-100", style { setWidthRatio(1f) }),
  GoldenCase("size-minwidth-240", style { setMinWidth(240.dp) }),
  GoldenCase("size-maxwidth-64", style { setMaxWidth(64.dp) }),
  GoldenCase("size-minwidthratio-060", style { setMinWidthRatio(0.6f) }),
  GoldenCase("size-maxwidthratio-035", style { setMaxWidthRatio(0.35f) }),
  GoldenCase("size-height-160", style { setHeight(160.dp) }),
  GoldenCase("size-height-24", style { setHeight(24.dp) }),
  GoldenCase("size-width-240-height-160", style { setSize(240.dp, 160.dp) }),
  GoldenCase(
    name = "size-precedence-widthratio-over-width",
    style = style {
      setWidthRatio(0.5f)
      setWidth(320.dp)
    },
  ),
  GoldenCase(
    name = "size-precedence-ratios-over-width",
    style = style {
      setMinWidthRatio(0.6f)
      setWidth(100.dp)
    },
  ),
  GoldenCase(
    name = "size-precedence-width-over-minmax",
    style = style {
      setWidth(300.dp)
      setMinWidth(40.dp)
      setMaxWidth(120.dp)
    },
  ),
  GoldenCase(
    name = "size-widthratio-with-margin-and-elevation",
    style = style {
      setWidthRatio(0.8f)
      setMargin(16.dp)
      setElevation(8.dp)
    },
  ),
)

/**
 * Catches a balloon placed against the wrong part of its anchor.
 *
 * The four edge aligns centre the balloon on the anchor, `DROP_DOWN` lines their leading edges
 * up instead, `showAtCenter` measures from the anchor's centre point rather than its edges,
 * and `CENTER` alone is a dead-centre overlay with no side to point at. Those are four
 * different formulas that all look similar with a small square anchor, so the wide and tall
 * anchors here are what actually separate them.
 */
private val placementCases: List<GoldenCase> = listOf(
  GoldenCase("place-align-top", style(), align = BalloonAlign.TOP),
  GoldenCase("place-align-start", style(), align = BalloonAlign.START),
  GoldenCase("place-align-end", style(), align = BalloonAlign.END),
  GoldenCase("place-align-dropdown", style(), align = BalloonAlign.DROP_DOWN),
  GoldenCase("place-align-center", style(), align = BalloonAlign.CENTER),
  GoldenCase(
    name = "place-align-center-no-arrow",
    style = style { setIsVisibleArrow(false) },
    align = BalloonAlign.CENTER,
  ),
  GoldenCase(
    name = "place-center-at-top",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.TOP,
  ),
  GoldenCase(
    name = "place-center-at-bottom",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.BOTTOM,
  ),
  GoldenCase(
    name = "place-center-at-start",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.START,
  ),
  GoldenCase(
    name = "place-center-at-end",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.END,
  ),
  GoldenCase(
    name = "place-dropdown-wide-anchor",
    style = style(),
    align = BalloonAlign.DROP_DOWN,
    anchorSize = wideAnchor,
  ),
  GoldenCase(
    name = "place-align-bottom-wide-anchor",
    style = style(),
    align = BalloonAlign.BOTTOM,
    anchorSize = wideAnchor,
  ),
  GoldenCase(
    name = "place-align-end-tall-anchor",
    style = style(),
    align = BalloonAlign.END,
    anchorSize = tallAnchor,
  ),
)

/**
 * Catches the flip and the final clamp, the two things `PopupWindow` did for the original and
 * this implementation has to do itself.
 *
 * A balloon asked for a side with no room must flip to the opposite one only when that side
 * actually has room, and whatever is left must be coerced back inside the window. Get the flip
 * test wrong and the balloon hangs off the edge pointing at nothing; get the clamp wrong and it
 * is silently pinned to the origin. Every corner and edge is here because the two axes fail
 * independently, and the small-window row is the case where neither side has room and only the
 * clamp can save it.
 */
private val edgeCases: List<GoldenCase> = listOf(
  GoldenCase("edge-top-start-bottom", style(), anchorAlignment = Alignment.TopStart),
  GoldenCase(
    name = "edge-top-center-top-flips",
    style = style(),
    align = BalloonAlign.TOP,
    anchorAlignment = Alignment.TopCenter,
  ),
  GoldenCase(
    name = "edge-top-end-top-flips",
    style = style(),
    align = BalloonAlign.TOP,
    anchorAlignment = Alignment.TopEnd,
  ),
  GoldenCase(
    name = "edge-center-start-start-flips",
    style = style(),
    align = BalloonAlign.START,
    anchorAlignment = Alignment.CenterStart,
  ),
  GoldenCase(
    name = "edge-center-end-end-flips",
    style = style(),
    align = BalloonAlign.END,
    anchorAlignment = Alignment.CenterEnd,
  ),
  GoldenCase(
    name = "edge-bottom-start-bottom-flips",
    style = style(),
    anchorAlignment = Alignment.BottomStart,
  ),
  GoldenCase(
    name = "edge-bottom-center-bottom-flips",
    style = style(),
    anchorAlignment = Alignment.BottomCenter,
  ),
  GoldenCase(
    name = "edge-bottom-end-bottom-flips",
    style = style(),
    anchorAlignment = Alignment.BottomEnd,
  ),
  GoldenCase(
    name = "edge-bottom-end-end-clamped",
    style = style(),
    align = BalloonAlign.END,
    anchorAlignment = Alignment.BottomEnd,
  ),
  GoldenCase(
    name = "edge-top-start-start-flips",
    style = style(),
    align = BalloonAlign.START,
    anchorAlignment = Alignment.TopStart,
  ),
  GoldenCase(
    name = "edge-dropdown-bottom-end",
    style = style(),
    align = BalloonAlign.DROP_DOWN,
    anchorAlignment = Alignment.BottomEnd,
  ),
  GoldenCase(
    name = "edge-full-width-at-top-start",
    style = style { setWidthRatio(1f) },
    anchorAlignment = Alignment.TopStart,
  ),
  GoldenCase(
    name = "edge-no-room-either-side",
    style = style(),
    align = BalloonAlign.TOP,
    windowSize = IntSize(220, 220),
    content = { GoldenBody(160, 140) },
  ),
  GoldenCase(
    name = "edge-center-at-top-clamped",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.TOP,
    anchorAlignment = Alignment.TopCenter,
  ),
)

/**
 * Catches START/END being mirrored twice, or not at all.
 *
 * Layout direction is resolved in three separate places - the align, the arrow orientation and
 * the padding/margin edges - and each one is capable of mirroring independently. Doing it twice
 * puts the arrow back on the LTR edge while the balloon sits on the RTL one, which no LTR case
 * can see. The clamp rows matter because the flip decision is made in absolute coordinates
 * after the mirror, so a direction bug there moves the balloon to the wrong window edge
 * entirely.
 */
private val rtlCases: List<GoldenCase> = listOf(
  GoldenCase("rtl-align-start", style(), align = BalloonAlign.START, layoutDirection = rtl),
  GoldenCase("rtl-align-end", style(), align = BalloonAlign.END, layoutDirection = rtl),
  GoldenCase(
    name = "rtl-dropdown",
    style = style(),
    align = BalloonAlign.DROP_DOWN,
    layoutDirection = rtl,
  ),
  GoldenCase(
    name = "rtl-arrow-orientation-start",
    style = style { setArrowOrientation(ArrowOrientation.START) },
    layoutDirection = rtl,
  ),
  GoldenCase(
    name = "rtl-arrow-orientation-end",
    style = style { setArrowOrientation(ArrowOrientation.END) },
    layoutDirection = rtl,
  ),
  GoldenCase(
    name = "rtl-padding-asymmetric",
    style = style { setPadding(start = 4.dp, top = 20.dp, end = 32.dp, bottom = 8.dp) },
    layoutDirection = rtl,
  ),
  GoldenCase(
    name = "rtl-margin-asymmetric",
    style = style { setMargin(start = 4.dp, top = 24.dp, end = 40.dp, bottom = 8.dp) },
    layoutDirection = rtl,
  ),
  GoldenCase(
    name = "rtl-edge-start-flips",
    style = style(),
    align = BalloonAlign.START,
    layoutDirection = rtl,
    anchorAlignment = Alignment.CenterEnd,
  ),
  GoldenCase(
    name = "rtl-edge-end-clamped",
    style = style(),
    align = BalloonAlign.END,
    layoutDirection = rtl,
    anchorAlignment = Alignment.CenterStart,
  ),
  GoldenCase(
    name = "rtl-center-at-start",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.START,
    layoutDirection = rtl,
  ),
  GoldenCase(
    name = "rtl-arrow-pos-anchor-025",
    style = alignAnchorStyle { setArrowPosition(0.25f) },
    layoutDirection = rtl,
    anchorSize = wideAnchor,
  ),
  GoldenCase(
    name = "rtl-overlay-roundrect-per-corner",
    style = overlayStyle {
      setOverlayShape(BalloonOverlayShape.RoundRectPerCorner(0.dp, 12.dp, 28.dp, 4.dp))
    },
    layoutDirection = rtl,
    anchorSize = bigAnchor,
  ),
)

/**
 * Catches the scrim cut-out being drawn with the wrong shape, in the wrong place, or through
 * the wrong blend.
 *
 * The anchor hole is erased from an offscreen layer; lose the offscreen compositing and the
 * clear punches through everything already on the canvas instead of only through the scrim,
 * which is a spectacular failure that no behavioural test notices. `overlayPaddingColor` is the
 * subtler one: the band is produced by painting the padded shape and then clearing the bare
 * one, and `Circle` with an explicit radius needs its own inset or the two draws cancel out and
 * leave no ring at all.
 */
private val overlayCases: List<GoldenCase> = listOf(
  GoldenCase(
    name = "overlay-shape-empty",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.Empty) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-rect",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.Rect) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-oval",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.Oval) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-circle-default-radius",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.Circle()) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-circle-radius-40",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.Circle(40.dp)) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-roundrect-12",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.RoundRect(12.dp)) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-roundrect-24x6",
    style = overlayStyle { setOverlayShape(BalloonOverlayShape.RoundRect(24.dp, 6.dp)) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-shape-roundrect-per-corner",
    style = overlayStyle {
      setOverlayShape(BalloonOverlayShape.RoundRectPerCorner(0.dp, 12.dp, 28.dp, 4.dp))
    },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-color-blue-40",
    style = overlayStyle { setOverlayColor(Color(0x661E88E5)) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-padding-16",
    style = overlayStyle { setOverlayPadding(16.dp) },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-padding-asymmetric",
    style = overlayStyle {
      setOverlayPadding(start = 4.dp, top = 16.dp, end = 28.dp, bottom = 8.dp)
    },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-padding-color-oval",
    style = overlayStyle {
      setOverlayShape(BalloonOverlayShape.Oval)
      setOverlayPadding(16.dp)
      setOverlayPaddingColor(amber)
    },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-padding-color-circle-radius-70",
    style = overlayStyle {
      setOverlayShape(BalloonOverlayShape.Circle(70.dp))
      setOverlayPadding(12.dp)
      setOverlayPaddingColor(amber)
    },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-padding-color-rect",
    style = overlayStyle {
      setOverlayShape(BalloonOverlayShape.Rect)
      setOverlayPadding(16.dp)
      setOverlayPaddingColor(green)
    },
    anchorSize = bigAnchor,
  ),
  GoldenCase(
    name = "overlay-small-anchor-oval",
    style = overlayStyle(),
    anchorSize = DpSize(32.dp, 32.dp),
  ),
  GoldenCase(
    name = "overlay-with-balloon-above",
    style = overlayStyle(),
    align = BalloonAlign.TOP,
    anchorSize = bigAnchor,
  ),
)

/**
 * Catches the balloon sizing itself to something other than what it was handed.
 *
 * Everything above uses one fixed block as the body, so a wrap that quietly ignored the
 * content's measurement would still look correct. These vary the body instead of the style: a
 * body far larger than the default has to grow the card and push the arrow's constraint band, a
 * tiny one has to shrink below the arrow's own base, and a multi-element layout is the case
 * where an extra intrinsic-measurement pass would show up as a different width.
 */
private val contentCases: List<GoldenCase> = listOf(
  GoldenCase("content-large", style(), content = { GoldenBody(240, 160) }),
  GoldenCase("content-tiny", style(), content = { GoldenBody(8, 8) }),
  GoldenCase("content-tall-narrow", style(), content = { GoldenBody(24, 220) }),
  GoldenCase("content-wide-flat", style(), content = { GoldenBody(320, 12) }),
  GoldenCase("content-multi-element", style(), content = { MultiElementBody() }),
  GoldenCase(
    name = "content-multi-element-padded",
    style = style {
      setPadding(16.dp)
      setCornerRadius(12.dp)
    },
    content = { MultiElementBody() },
  ),
  GoldenCase(
    name = "content-large-with-maxwidth",
    style = style { setMaxWidth(160.dp) },
    content = { GoldenBody(240, 160) },
  ),
  GoldenCase(
    name = "content-tiny-with-minwidth",
    style = style { setMinWidth(200.dp) },
    content = { GoldenBody(8, 8) },
  ),
)

/**
 * Every configuration the golden suite renders.
 *
 * Order is the group order above and is not significant; names are, because each one is a
 * golden's filename. Renaming a case orphans its golden, so prefer adding a new one.
 */
/**
 * Show offsets.
 *
 * An offset is not a cosmetic nudge applied after the fact: the position provider adds it
 * before it decides whether there is room, so it can change which side the balloon lands on.
 * If these ever start matching the zero-offset cases, the offset stopped reaching the
 * provider; if the flip case stops flipping, it stopped reaching the room check.
 */
private val offsetCases: List<GoldenCase> = listOf(
  GoldenCase("offset-x-positive", style(), offset = DpOffset(40.dp, 0.dp)),
  GoldenCase("offset-x-negative", style(), offset = DpOffset((-40).dp, 0.dp)),
  GoldenCase("offset-y-positive", style(), offset = DpOffset(0.dp, 40.dp)),
  GoldenCase("offset-y-negative", style(), offset = DpOffset(0.dp, (-40).dp)),
  GoldenCase("offset-both", style(), offset = DpOffset(24.dp, 24.dp)),
  GoldenCase(
    name = "offset-align-top",
    style = style(),
    align = BalloonAlign.TOP,
    offset = DpOffset(0.dp, (-24).dp),
  ),
  GoldenCase(
    name = "offset-align-start",
    style = style(),
    align = BalloonAlign.START,
    offset = DpOffset((-24).dp, 0.dp),
  ),
  // Enough downward offset that the balloon no longer fits below, so the provider has to
  // flip it above the anchor rather than just pushing it off the bottom edge.
  GoldenCase(
    name = "offset-forces-flip",
    style = style(),
    offset = DpOffset(0.dp, 260.dp),
    anchorAlignment = Alignment.BottomCenter,
  ),
  GoldenCase(
    name = "offset-center-at-top",
    style = style(),
    align = BalloonAlign.CENTER,
    centerAlign = BalloonCenterAlign.TOP,
    offset = DpOffset(20.dp, (-20).dp),
  ),
  // RTL mirrors the x offset along with the align, so this must not land where its LTR twin
  // does. It is the one place the two mirrorings could cancel each other out.
  GoldenCase(
    name = "offset-rtl-x-positive",
    style = style(),
    align = BalloonAlign.START,
    offset = DpOffset(40.dp, 0.dp),
    layoutDirection = LayoutDirection.Rtl,
  ),
)

internal val GOLDEN_CASES: List<GoldenCase> = buildList {
  addAll(arrowSizeCases)
  addAll(arrowVisibilityCases)
  addAll(arrowOrientationCases)
  addAll(arrowPositionCases)
  addAll(arrowAnchorPaddingCases)
  addAll(arrowColorCases)
  addAll(cornerCases)
  addAll(fillCases)
  addAll(borderCases)
  addAll(elevationCases)
  addAll(paddingCases)
  addAll(marginCases)
  addAll(sizeCases)
  addAll(placementCases)
  addAll(edgeCases)
  addAll(rtlCases)
  addAll(overlayCases)
  addAll(offsetCases)
  addAll(contentCases)
}
