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
 *
 * The path spans the full [size] (minus the arrow protrusion on the arrow edge). No
 * stroke inset is applied: `Modifier.border` on an [androidx.compose.ui.graphics.Outline.Generic]
 * draws its stroke *inner-aligned* (inside the path), so an inset would only leave a
 * transparent ring and shorten the arrow tip.
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
): Path {
  val path = Path()
  val width = size.width
  val height = size.height
  if (width <= 0f || height <= 0f) return path

  val hasArrow = arrowWidthPx > 0f && arrowHeightPx > 0f

  // Mirror RadiusLayout: protrusion is half the arrow height, so the arrow tip extends
  // protrusion px outside the body rect on the corresponding edge.
  val protrusion = if (hasArrow) arrowHeightPx * 0.5f else 0f

  val rectLeft = if (hasArrow && side == ResolvedArrowSide.LEFT) protrusion else 0f
  val rectTop = if (hasArrow && side == ResolvedArrowSide.TOP) protrusion else 0f
  val rectRight = if (hasArrow && side == ResolvedArrowSide.RIGHT) width - protrusion else width
  val rectBottom = if (hasArrow && side == ResolvedArrowSide.BOTTOM) height - protrusion else height

  // Clamp the corner radius so it never exceeds half of the body rect's shorter side.
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

  val halfArrow = arrowWidthPx / 2f
  val arrowCenterX = arrowCenterAlong(width, ratioInRect, halfArrow, rectLeft, rectRight, radius)
  val arrowCenterY = arrowCenterAlong(height, ratioInRect, halfArrow, rectTop, rectBottom, radius)

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
 * Takes the same [cornerRadiusPx] as [buildBalloonPath] so the arrow center is
 * clamped identically — otherwise the colored triangle drifts from the body notch at
 * extreme [ratioInRect] with a non-zero corner radius.
 *
 * Returns an empty path if [arrowWidthPx] or [arrowHeightPx] is `<= 0`.
 */
internal fun buildArrowTrianglePath(
  size: Size,
  cornerRadiusPx: Float,
  arrowWidthPx: Float,
  arrowHeightPx: Float,
  side: ResolvedArrowSide,
  ratioInRect: Float,
): Path {
  val path = Path()
  if (arrowWidthPx <= 0f || arrowHeightPx <= 0f) return path
  if (size.width <= 0f || size.height <= 0f) return path

  val protrusion = arrowHeightPx * 0.5f
  val rectLeft = if (side == ResolvedArrowSide.LEFT) protrusion else 0f
  val rectTop = if (side == ResolvedArrowSide.TOP) protrusion else 0f
  val rectRight = if (side == ResolvedArrowSide.RIGHT) size.width - protrusion else size.width
  val rectBottom = if (side == ResolvedArrowSide.BOTTOM) size.height - protrusion else size.height

  val rectWidth = (rectRight - rectLeft).coerceAtLeast(0f)
  val rectHeight = (rectBottom - rectTop).coerceAtLeast(0f)
  val radius = cornerRadiusPx
    .coerceAtLeast(0f)
    .coerceAtMost(minOf(rectWidth, rectHeight) / 2f)

  val halfArrow = arrowWidthPx / 2f

  when (side) {
    ResolvedArrowSide.TOP -> {
      val centerX =
        arrowCenterAlong(size.width, ratioInRect, halfArrow, rectLeft, rectRight, radius)
      val tipY = rectTop - protrusion
      path.moveTo(centerX - halfArrow, rectTop)
      path.lineTo(centerX, tipY)
      path.lineTo(centerX + halfArrow, rectTop)
      path.close()
    }
    ResolvedArrowSide.BOTTOM -> {
      val centerX =
        arrowCenterAlong(size.width, ratioInRect, halfArrow, rectLeft, rectRight, radius)
      val tipY = rectBottom + protrusion
      path.moveTo(centerX - halfArrow, rectBottom)
      path.lineTo(centerX, tipY)
      path.lineTo(centerX + halfArrow, rectBottom)
      path.close()
    }
    ResolvedArrowSide.LEFT -> {
      val centerY =
        arrowCenterAlong(size.height, ratioInRect, halfArrow, rectTop, rectBottom, radius)
      val tipX = rectLeft - protrusion
      path.moveTo(rectLeft, centerY - halfArrow)
      path.lineTo(tipX, centerY)
      path.lineTo(rectLeft, centerY + halfArrow)
      path.close()
    }
    ResolvedArrowSide.RIGHT -> {
      val centerY =
        arrowCenterAlong(size.height, ratioInRect, halfArrow, rectTop, rectBottom, radius)
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
 * Computes the arrow center along one axis, shared by [buildBalloonPath] and
 * [buildArrowTrianglePath] so the body notch and the colored overlay triangle can
 * never drift apart.
 *
 * First maps [ratio] onto the full extent (keeping the base inside the edge), then
 * clamps it between the corner curves so the arrow base sits on the straight portion
 * of the edge. Falls back to the edge midpoint for degenerate sizes.
 */
private fun arrowCenterAlong(
  fullExtent: Float,
  ratio: Float,
  halfArrow: Float,
  rectStart: Float,
  rectEnd: Float,
  radius: Float,
): Float {
  val center = (fullExtent * ratio).coerceIn(halfArrow, fullExtent - halfArrow)
  val insetStart = rectStart + radius + halfArrow
  val insetEnd = rectEnd - radius - halfArrow
  return if (insetStart <= insetEnd) {
    center.coerceIn(insetStart, insetEnd)
  } else {
    (rectStart + rectEnd) / 2f
  }
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
