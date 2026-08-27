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

@file:Suppress("MagicNumber")

package com.skydoves.balloon.kmpdemo.parity

/**
 * One balloon configuration to render identically on both stacks so the results can be
 * compared pixel for pixel.
 *
 * The declaration deliberately uses only primitives so the exact same file can live in the
 * View demo (`:app`) and the Compose Multiplatform demo (`:androidApp`) — each module maps
 * it onto its own builder API, and any divergence in the rendered result is a real parity
 * defect rather than a difference in how the case was described.
 *
 * Sizes are in dp; `-1` means "unset" for the size specs, matching `BalloonSizeSpec.WRAP` /
 * `Dp.Unspecified`.
 *
 * The anchor and the balloon body are fixed-size colored boxes rather than text: the two
 * Compose runtimes measure text a pixel or two differently, which would swamp the geometry
 * signal this harness exists to measure.
 */
data class ParityCase(
  val id: String,
  // --- anchor placement (absolute, from the top-left of the window)
  val anchorLeftDp: Int = 130,
  val anchorTopDp: Int = 300,
  val anchorWidthDp: Int = 100,
  val anchorHeightDp: Int = 60,
  // --- how the balloon is shown
  /**
   * TOP | BOTTOM | START | END | CENTER |
   * CENTER_TOP | CENTER_BOTTOM | CENTER_START | CENTER_END
   */
  val align: String = "BOTTOM",
  val xOffDp: Int = 0,
  val yOffDp: Int = 0,
  // --- balloon body content (a plain opaque box, so both stacks measure it identically)
  val contentWidthDp: Int = 120,
  val contentHeightDp: Int = 40,
  // --- shape
  val cornerRadiusDp: Float = 5f,
  val arrowWidthDp: Int = 12,
  val arrowHeightDp: Int = 12,
  val arrowVisible: Boolean = true,
  val arrowPosition: Float = 0.5f,
  /** ALIGN_BALLOON | ALIGN_ANCHOR */
  val arrowPositionRules: String = "ALIGN_BALLOON",
  /** null = leave at the library default; otherwise TOP | BOTTOM | START | END */
  val arrowOrientation: String? = null,
  /** ALIGN_ANCHOR | ALIGN_FIXED */
  val arrowOrientationRules: String = "ALIGN_ANCHOR",
  val arrowAlignAnchorPaddingDp: Int = 0,
  val arrowAlignAnchorPaddingRatio: Float = 2.5f,
  // --- padding / margin
  val padLeftDp: Int = 6,
  val padTopDp: Int = 6,
  val padRightDp: Int = 6,
  val padBottomDp: Int = 6,
  val marginLeftDp: Int = 0,
  val marginTopDp: Int = 0,
  val marginRightDp: Int = 0,
  val marginBottomDp: Int = 0,
  // --- sizing
  val widthDp: Int = -1,
  val widthRatio: Float = 0f,
  val minWidthDp: Int = -1,
  val maxWidthDp: Int = -1,
  val minWidthRatio: Float = 0f,
  val maxWidthRatio: Float = 0f,
  val heightDp: Int = -1,
  val elevationDp: Int = 2,
  // --- paint
  val borderThicknessDp: Float = 0f,
  val alpha: Float = 1f,
  /** When true the arrow is painted in [PARITY_ARROW_COLOR] so it can be measured separately. */
  val distinctArrowColor: Boolean = true,
  // --- overlay
  /** null = no overlay; otherwise EMPTY | RECT | OVAL | CIRCLE | ROUNDRECT */
  val overlayShape: String? = null,
  val overlayPaddingDp: Float = 0f,
  val overlayPadStartDp: Float = -1f,
  val overlayPadTopDp: Float = -1f,
  val overlayPadEndDp: Float = -1f,
  val overlayPadBottomDp: Float = -1f,
  val overlayRadiusDp: Float = 20f,
)

/** Opaque blue: the balloon body fill. Its bounding box is the card rect. */
const val PARITY_BODY_COLOR: Int = 0xFF0000FF.toInt()

/** Opaque red: the arrow fill, painted over the body so the arrow can be measured alone. */
const val PARITY_ARROW_COLOR: Int = 0xFFFF0000.toInt()

/** Opaque yellow: the fixed-size balloon content, so content padding can be measured. */
const val PARITY_CONTENT_COLOR: Int = 0xFFFFFF00.toInt()

/** Opaque green: the anchor. */
const val PARITY_ANCHOR_COLOR: Int = 0xFF00C000.toInt()

/** Opaque cyan: the balloon border stroke. */
const val PARITY_BORDER_COLOR: Int = 0xFF00FFFF.toInt()

/** Opaque magenta: the overlay scrim (opaque so the cut-out reads as an exact hole). */
const val PARITY_OVERLAY_COLOR: Int = 0xFFFF00FF.toInt()

/** Opaque white: the window background. */
const val PARITY_BACKGROUND_COLOR: Int = 0xFFFFFFFF.toInt()

/**
 * Every case the sweep renders. Case ids are stable and shared by both harnesses; the driver
 * scripts key their screenshots on them.
 */
val PARITY_CASES: List<ParityCase> = buildList {
  // ---------------------------------------------------------------- align
  add(ParityCase(id = "align-bottom", align = "BOTTOM"))
  add(ParityCase(id = "align-top", align = "TOP"))
  // The View's `arrowOrientation` defaults to BOTTOM and its auto-rule only corrects within
  // an axis, so a side-aligned balloon keeps a downward arrow unless the orientation is
  // named explicitly. The KMP port derives it from the align instead — these cases pin it on
  // both sides so what is compared is the geometry, not that (separately reported) default.
  // The anchor also sits far enough from both edges that neither stack clamps or flips.
  add(
    ParityCase(
      id = "align-start",
      align = "START",
      anchorLeftDp = 160,
      arrowOrientation = "END",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(
    ParityCase(
      id = "align-end",
      align = "END",
      anchorLeftDp = 160,
      arrowOrientation = "START",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(ParityCase(id = "align-center-top", align = "CENTER_TOP"))
  add(ParityCase(id = "align-center-bottom", align = "CENTER_BOTTOM"))
  add(
    ParityCase(
      id = "align-center-start",
      align = "CENTER_START",
      anchorLeftDp = 160,
      arrowOrientation = "END",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(
    ParityCase(
      id = "align-center-end",
      align = "CENTER_END",
      anchorLeftDp = 160,
      arrowOrientation = "START",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  // Default arrow orientation, no pinning: documents the View's legacy BOTTOM default
  // against the KMP port's align-derived orientation.
  add(ParityCase(id = "align-start-defaultarrow", align = "START", anchorLeftDp = 160))
  add(ParityCase(id = "align-end-defaultarrow", align = "END", anchorLeftDp = 160))

  // ---------------------------------------------------------------- corner radius
  for (r in listOf(0f, 5f, 12f, 24f, 60f)) {
    add(ParityCase(id = "corner-$r", cornerRadiusDp = r))
  }

  // ---------------------------------------------------------------- arrow size
  add(ParityCase(id = "arrow-0", arrowWidthDp = 0, arrowHeightDp = 0))
  add(ParityCase(id = "arrow-6", arrowWidthDp = 6, arrowHeightDp = 6))
  add(ParityCase(id = "arrow-12", arrowWidthDp = 12, arrowHeightDp = 12))
  add(ParityCase(id = "arrow-20", arrowWidthDp = 20, arrowHeightDp = 20))
  add(ParityCase(id = "arrow-w20-h8", arrowWidthDp = 20, arrowHeightDp = 8))
  add(ParityCase(id = "arrow-w8-h20", arrowWidthDp = 8, arrowHeightDp = 20))
  add(ParityCase(id = "arrow-hidden", arrowVisible = false))

  // ---------------------------------------------------------------- arrow position (ALIGN_BALLOON)
  for (p in listOf(0f, 0.25f, 0.5f, 0.75f, 1f)) {
    add(ParityCase(id = "arrowpos-balloon-$p", arrowPosition = p))
  }
  // ALIGN_ANCHOR, with the anchor deliberately off-center so the two rules differ.
  for (p in listOf(0f, 0.25f, 0.5f, 0.75f, 1f)) {
    add(
      ParityCase(
        id = "arrowpos-anchor-$p",
        arrowPosition = p,
        arrowPositionRules = "ALIGN_ANCHOR",
        anchorLeftDp = 40,
        anchorWidthDp = 60,
      ),
    )
  }
  // Vertical arrow edges (START/END align) exercise the other axis of the same code.
  add(
    ParityCase(
      id = "arrowpos-vert-balloon-025",
      align = "END",
      anchorLeftDp = 160,
      arrowPosition = 0.25f,
      arrowOrientation = "START",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(
    ParityCase(
      id = "arrowpos-vert-anchor-025",
      align = "END",
      anchorLeftDp = 160,
      arrowPosition = 0.25f,
      arrowPositionRules = "ALIGN_ANCHOR",
      arrowOrientation = "START",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )

  // ---------------------------------------------------------------- pinned arrow orientation
  for (o in listOf("TOP", "BOTTOM", "START", "END")) {
    add(
      ParityCase(
        id = "arroworientation-$o",
        arrowOrientation = o,
        arrowOrientationRules = "ALIGN_FIXED",
      ),
    )
  }

  // ---------------------------------------------------------------- padding
  add(ParityCase(id = "padding-0", padLeftDp = 0, padTopDp = 0, padRightDp = 0, padBottomDp = 0))
  add(ParityCase(id = "padding-8", padLeftDp = 8, padTopDp = 8, padRightDp = 8, padBottomDp = 8))
  add(
    ParityCase(
      id = "padding-16",
      padLeftDp = 16,
      padTopDp = 16,
      padRightDp = 16,
      padBottomDp = 16,
    ),
  )
  add(
    ParityCase(
      id = "padding-asym",
      padLeftDp = 4,
      padTopDp = 10,
      padRightDp = 20,
      padBottomDp = 30,
    ),
  )

  // ---------------------------------------------------------------- margin
  add(ParityCase(id = "margin-0"))
  add(
    ParityCase(
      id = "margin-16",
      marginLeftDp = 16,
      marginTopDp = 16,
      marginRightDp = 16,
      marginBottomDp = 16,
    ),
  )
  add(
    ParityCase(
      id = "margin-asym",
      marginLeftDp = 4,
      marginTopDp = 10,
      marginRightDp = 20,
      marginBottomDp = 30,
    ),
  )
  // A wide balloon against a margin: the margin has to actually shrink it.
  add(
    ParityCase(
      id = "margin-wide",
      widthRatio = 1f,
      marginLeftDp = 24,
      marginRightDp = 24,
    ),
  )

  // ---------------------------------------------------------------- width specs
  add(ParityCase(id = "width-wrap"))
  add(ParityCase(id = "width-200", widthDp = 200))
  add(ParityCase(id = "width-320", widthDp = 320))
  for (r in listOf(0.3f, 0.5f, 0.8f, 1.0f)) {
    add(ParityCase(id = "widthratio-$r", widthRatio = r))
  }
  add(ParityCase(id = "minwidth-260", minWidthDp = 260))
  add(ParityCase(id = "maxwidth-90", maxWidthDp = 90))
  add(ParityCase(id = "minwidthratio-06", minWidthRatio = 0.6f))
  add(ParityCase(id = "maxwidthratio-03", maxWidthRatio = 0.3f))
  add(ParityCase(id = "width-clamped-by-window", widthDp = 600))

  // ---------------------------------------------------------------- height
  add(ParityCase(id = "height-wrap"))
  add(ParityCase(id = "height-120", heightDp = 120))
  add(ParityCase(id = "height-40", heightDp = 40))

  // ---------------------------------------------------------------- offsets
  add(ParityCase(id = "offset-x40", xOffDp = 40))
  add(ParityCase(id = "offset-y40", yOffDp = 40))
  add(ParityCase(id = "offset-neg", xOffDp = -30, yOffDp = -30))

  // ---------------------------------------------------------------- edge clamping / flipping
  add(ParityCase(id = "edge-top", align = "TOP", anchorTopDp = 10))
  add(ParityCase(id = "edge-bottom", align = "BOTTOM", anchorTopDp = 720))
  add(
    ParityCase(
      id = "edge-left",
      align = "START",
      anchorLeftDp = 4,
      arrowOrientation = "END",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(
    ParityCase(
      id = "edge-right",
      align = "END",
      anchorLeftDp = 300,
      arrowOrientation = "START",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(ParityCase(id = "edge-left-wide", align = "BOTTOM", anchorLeftDp = 0, widthDp = 260))
  add(ParityCase(id = "edge-right-wide", align = "BOTTOM", anchorLeftDp = 290, widthDp = 260))

  // ---------------------------------------------------------------- border
  add(ParityCase(id = "border-1", borderThicknessDp = 1f))
  add(ParityCase(id = "border-3", borderThicknessDp = 3f))
  add(ParityCase(id = "border-6", borderThicknessDp = 6f))

  // ---------------------------------------------------------------- elevation
  add(ParityCase(id = "elevation-0", elevationDp = 0, widthRatio = 1f))
  add(ParityCase(id = "elevation-2", elevationDp = 2, widthRatio = 1f))
  add(ParityCase(id = "elevation-8", elevationDp = 8, widthRatio = 1f))

  // ---------------------------------------------------------------- alpha
  add(ParityCase(id = "alpha-05", alpha = 0.5f))

  // ---------------------------------------------------------------- overlay
  add(ParityCase(id = "overlay-oval", overlayShape = "OVAL"))
  add(ParityCase(id = "overlay-rect", overlayShape = "RECT"))
  add(ParityCase(id = "overlay-circle", overlayShape = "CIRCLE"))
  add(ParityCase(id = "overlay-roundrect", overlayShape = "ROUNDRECT", overlayRadiusDp = 12f))
  add(ParityCase(id = "overlay-empty", overlayShape = "EMPTY"))
  add(ParityCase(id = "overlay-padding-12", overlayShape = "OVAL", overlayPaddingDp = 12f))

  // ------------------------------------------- ALIGN_ANCHOR clamp band + drop-down
  // A wider-than-balloon anchor is what pushes `getArrowConstraintPositionX` into its
  // `minPosition` band, which is where these two settings actually bite.
  add(
    ParityCase(
      id = "arrowalign-pad-0",
      anchorLeftDp = 20,
      anchorWidthDp = 300,
      arrowPosition = 0.33f,
      arrowPositionRules = "ALIGN_ANCHOR",
    ),
  )
  add(
    ParityCase(
      id = "arrowalign-pad-16",
      anchorLeftDp = 20,
      anchorWidthDp = 300,
      arrowPosition = 0.33f,
      arrowPositionRules = "ALIGN_ANCHOR",
      arrowAlignAnchorPaddingDp = 16,
    ),
  )
  add(
    ParityCase(
      id = "arrowalign-ratio-05",
      anchorLeftDp = 20,
      anchorWidthDp = 300,
      arrowPosition = 0.33f,
      arrowPositionRules = "ALIGN_ANCHOR",
      arrowAlignAnchorPaddingRatio = 0.5f,
    ),
  )
  add(
    ParityCase(
      id = "dropdown",
      align = "DROP_DOWN",
      arrowOrientation = "TOP",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(
    ParityCase(
      id = "dropdown-offset",
      align = "DROP_DOWN",
      xOffDp = 24,
      yOffDp = 12,
      arrowOrientation = "TOP",
      arrowOrientationRules = "ALIGN_FIXED",
    ),
  )
  add(
    ParityCase(
      id = "overlay-padding-asym",
      overlayShape = "RECT",
      overlayPadStartDp = 4f,
      overlayPadTopDp = 10f,
      overlayPadEndDp = 20f,
      overlayPadBottomDp = 30f,
    ),
  )
}

/** Case lookup by [ParityCase.id]. */
fun parityCase(id: String): ParityCase? = PARITY_CASES.firstOrNull { it.id == id }
