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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

/**
 * Build a rounded-rectangle-with-arrow [Path] inside a box of [size].
 *
 * This is a pure function ported from `RadiusLayout.rebuildPath()` in the original
 * Android-only Balloon library so its geometry stays in lock-step with that
 * implementation.
 *
 * All inputs are in pixels. The caller is expected to convert from `Dp` already.
 *
 * - [side]: which absolute edge the arrow points away from (already RTL-resolved).
 *   Most callers should funnel through here so RTL handling happens exactly once at
 *   the [BalloonShape] boundary.
 * - [ratioInRect]: arrow center, expressed as a fraction (0f..1f) of the
 *   rect's width (for TOP/BOTTOM) or height (for LEFT/RIGHT).
 *   Clamped so the arrow base stays inside the rounded portion of the edge.
 * - [halfStrokePx]: half of the stroke thickness, used to inset the rect so the
 *   stroke draws fully inside the layout bounds.
 *
 * If [arrowWidthPx] or [arrowHeightPx] is `<= 0`, a plain rounded rectangle is built
 * (no arrow notch).
 */
internal fun buildBalloonPath(
  size: Size,
  cornerRadiusPx: Float,
  arrowWidthPx: Float,
  arrowHeightPx: Float,
  side: ResolvedArrowSide,
  ratioInRect: Float,
  halfStrokePx: Float,
): Path {
  val path = Path()
  val width = size.width
  val height = size.height
  if (width <= 0f || height <= 0f) return path

  val hasArrow = arrowWidthPx > 0f && arrowHeightPx > 0f

  // Mirror RadiusLayout: protrusion is half the arrow height, so the arrow tip extends
  // protrusion px outside the inset rect on the corresponding edge.
  val protrusion = if (hasArrow) arrowHeightPx * 0.5f else 0f
  val extra = protrusion + halfStrokePx

  val rectLeft = if (hasArrow && side == ResolvedArrowSide.LEFT) extra else halfStrokePx
  val rectTop = if (hasArrow && side == ResolvedArrowSide.TOP) extra else halfStrokePx
  val rectRight = if (hasArrow && side == ResolvedArrowSide.RIGHT) {
    width - extra
  } else {
    width - halfStrokePx
  }
  val rectBottom = if (hasArrow && side == ResolvedArrowSide.BOTTOM) {
    height - extra
  } else {
    height - halfStrokePx
  }

  // Clamp the corner radius so it never exceeds half of the inset rect's shorter side.
  val rectWidth = (rectRight - rectLeft).coerceAtLeast(0f)
  val rectHeight = (rectBottom - rectTop).coerceAtLeast(0f)
  val radius = cornerRadiusPx
    .coerceAtLeast(0f)
    .coerceAtMost(minOf(rectWidth, rectHeight) / 2f)

  if (!hasArrow) {
    // Plain rounded rect — same shape RadiusLayout falls back to via addRoundRect.
    addRoundedRect(path, rectLeft, rectTop, rectRight, rectBottom, radius)
    path.close()
    return path
  }

  // Match RadiusLayout: arrow center is computed from the *full* width/height and ratio,
  // then clamped so the arrow base stays inside the rounded portion of the edge.
  val halfArrow = arrowWidthPx / 2f
  val centerX = (width * ratioInRect)
    .coerceIn(halfArrow + halfStrokePx, width - halfArrow - halfStrokePx)
  val centerY = (height * ratioInRect)
    .coerceIn(halfArrow + halfStrokePx, height - halfArrow - halfStrokePx)

  // Additionally clamp the arrow center along the rect axis so the arrow base sits
  // between the corner curves. RadiusLayout effectively relied on width/height >>
  // 2*radius; do it explicitly here so degenerate sizes don't produce self-intersections.
  val cornerInsetH = rectLeft + radius + halfArrow
  val cornerInsetHEnd = rectRight - radius - halfArrow
  val cornerInsetV = rectTop + radius + halfArrow
  val cornerInsetVEnd = rectBottom - radius - halfArrow

  val arrowCenterX = if (cornerInsetH <= cornerInsetHEnd) {
    centerX.coerceIn(cornerInsetH, cornerInsetHEnd)
  } else {
    (rectLeft + rectRight) / 2f
  }
  val arrowCenterY = if (cornerInsetV <= cornerInsetVEnd) {
    centerY.coerceIn(cornerInsetV, cornerInsetVEnd)
  } else {
    (rectTop + rectBottom) / 2f
  }

  when (side) {
    ResolvedArrowSide.TOP -> {
      val tipY = rectTop - protrusion
      path.moveTo(rectLeft + radius, rectBottom)
      path.lineTo(rectRight - radius, rectBottom)
      path.quadraticTo(rectRight, rectBottom, rectRight, rectBottom - radius)
      path.lineTo(rectRight, rectTop + radius)
      path.quadraticTo(rectRight, rectTop, rectRight - radius, rectTop)
      path.lineTo(arrowCenterX + halfArrow, rectTop)
      path.lineTo(arrowCenterX, tipY)
      path.lineTo(arrowCenterX - halfArrow, rectTop)
      path.lineTo(rectLeft + radius, rectTop)
      path.quadraticTo(rectLeft, rectTop, rectLeft, rectTop + radius)
      path.lineTo(rectLeft, rectBottom - radius)
      path.quadraticTo(rectLeft, rectBottom, rectLeft + radius, rectBottom)
    }

    ResolvedArrowSide.BOTTOM -> {
      val tipY = rectBottom + protrusion
      path.moveTo(rectLeft + radius, rectTop)
      path.lineTo(rectRight - radius, rectTop)
      path.quadraticTo(rectRight, rectTop, rectRight, rectTop + radius)
      path.lineTo(rectRight, rectBottom - radius)
      path.quadraticTo(rectRight, rectBottom, rectRight - radius, rectBottom)
      path.lineTo(arrowCenterX + halfArrow, rectBottom)
      path.lineTo(arrowCenterX, tipY)
      path.lineTo(arrowCenterX - halfArrow, rectBottom)
      path.lineTo(rectLeft + radius, rectBottom)
      path.quadraticTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
      path.lineTo(rectLeft, rectTop + radius)
      path.quadraticTo(rectLeft, rectTop, rectLeft + radius, rectTop)
    }

    ResolvedArrowSide.LEFT -> {
      val tipX = rectLeft - protrusion
      path.moveTo(rectLeft + radius, rectTop)
      path.lineTo(rectRight - radius, rectTop)
      path.quadraticTo(rectRight, rectTop, rectRight, rectTop + radius)
      path.lineTo(rectRight, rectBottom - radius)
      path.quadraticTo(rectRight, rectBottom, rectRight - radius, rectBottom)
      path.lineTo(rectLeft + radius, rectBottom)
      path.quadraticTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
      path.lineTo(rectLeft, arrowCenterY + halfArrow)
      path.lineTo(tipX, arrowCenterY)
      path.lineTo(rectLeft, arrowCenterY - halfArrow)
      path.lineTo(rectLeft, rectTop + radius)
      path.quadraticTo(rectLeft, rectTop, rectLeft + radius, rectTop)
    }

    ResolvedArrowSide.RIGHT -> {
      val tipX = rectRight + protrusion
      path.moveTo(rectLeft + radius, rectTop)
      path.lineTo(rectRight - radius, rectTop)
      path.quadraticTo(rectRight, rectTop, rectRight, rectTop + radius)
      path.lineTo(rectRight, arrowCenterY - halfArrow)
      path.lineTo(tipX, arrowCenterY)
      path.lineTo(rectRight, arrowCenterY + halfArrow)
      path.lineTo(rectRight, rectBottom - radius)
      path.quadraticTo(rectRight, rectBottom, rectRight - radius, rectBottom)
      path.lineTo(rectLeft + radius, rectBottom)
      path.quadraticTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
      path.lineTo(rectLeft, rectTop + radius)
      path.quadraticTo(rectLeft, rectTop, rectLeft + radius, rectTop)
    }
  }
  path.close()
  return path
}

/**
 * Builds JUST the arrow triangle (without the rounded rect body) in the same
 * coordinate space as [buildBalloonPath]. Used to overlay-paint the arrow with a
 * different color than the body.
 *
 * Returns an empty path if [arrowWidthPx] or [arrowHeightPx] is `<= 0`.
 */
internal fun buildArrowTrianglePath(
  size: Size,
  arrowWidthPx: Float,
  arrowHeightPx: Float,
  side: ResolvedArrowSide,
  ratioInRect: Float,
  halfStrokePx: Float,
): Path {
  val path = Path()
  if (arrowWidthPx <= 0f || arrowHeightPx <= 0f) return path
  if (size.width <= 0f || size.height <= 0f) return path

  val protrusion = arrowHeightPx * 0.5f
  val extra = protrusion + halfStrokePx
  val rectLeft = if (side == ResolvedArrowSide.LEFT) extra else halfStrokePx
  val rectTop = if (side == ResolvedArrowSide.TOP) extra else halfStrokePx
  val rectRight = if (side == ResolvedArrowSide.RIGHT) {
    size.width - extra
  } else {
    size.width - halfStrokePx
  }
  val rectBottom = if (side == ResolvedArrowSide.BOTTOM) {
    size.height - extra
  } else {
    size.height - halfStrokePx
  }

  val halfArrow = arrowWidthPx / 2f

  when (side) {
    ResolvedArrowSide.TOP -> {
      val centerX = (size.width * ratioInRect)
        .coerceIn(halfArrow + halfStrokePx, size.width - halfArrow - halfStrokePx)
      val tipY = rectTop - protrusion
      path.moveTo(centerX - halfArrow, rectTop)
      path.lineTo(centerX, tipY)
      path.lineTo(centerX + halfArrow, rectTop)
      path.close()
    }
    ResolvedArrowSide.BOTTOM -> {
      val centerX = (size.width * ratioInRect)
        .coerceIn(halfArrow + halfStrokePx, size.width - halfArrow - halfStrokePx)
      val tipY = rectBottom + protrusion
      path.moveTo(centerX - halfArrow, rectBottom)
      path.lineTo(centerX, tipY)
      path.lineTo(centerX + halfArrow, rectBottom)
      path.close()
    }
    ResolvedArrowSide.LEFT -> {
      val centerY = (size.height * ratioInRect)
        .coerceIn(halfArrow + halfStrokePx, size.height - halfArrow - halfStrokePx)
      val tipX = rectLeft - protrusion
      path.moveTo(rectLeft, centerY - halfArrow)
      path.lineTo(tipX, centerY)
      path.lineTo(rectLeft, centerY + halfArrow)
      path.close()
    }
    ResolvedArrowSide.RIGHT -> {
      val centerY = (size.height * ratioInRect)
        .coerceIn(halfArrow + halfStrokePx, size.height - halfArrow - halfStrokePx)
      val tipX = rectRight + protrusion
      path.moveTo(rectRight, centerY - halfArrow)
      path.lineTo(tipX, centerY)
      path.lineTo(rectRight, centerY + halfArrow)
      path.close()
    }
  }
  return path
}

/**
 * Adds a rounded rectangle to [path] using `quadraticTo` corners so the geometry
 * matches what RadiusLayout produces for the no-arrow case via `addRoundRect`.
 */
private fun addRoundedRect(
  path: Path,
  left: Float,
  top: Float,
  right: Float,
  bottom: Float,
  radius: Float,
) {
  if (radius <= 0f) {
    path.moveTo(left, top)
    path.lineTo(right, top)
    path.lineTo(right, bottom)
    path.lineTo(left, bottom)
    path.lineTo(left, top)
    return
  }
  path.moveTo(left + radius, top)
  path.lineTo(right - radius, top)
  path.quadraticTo(right, top, right, top + radius)
  path.lineTo(right, bottom - radius)
  path.quadraticTo(right, bottom, right - radius, bottom)
  path.lineTo(left + radius, bottom)
  path.quadraticTo(left, bottom, left, bottom - radius)
  path.lineTo(left, top + radius)
  path.quadraticTo(left, top, left + radius, top)
}
