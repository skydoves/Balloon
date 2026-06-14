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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A Compose [Shape] that draws a rounded rectangle with a triangular arrow notch
 * on one of its four edges — the same shape produced by `RadiusLayout` in the
 * Android-only Balloon library.
 *
 * @param cornerRadius radius of the rounded-rect corners.
 * @param arrowWidth width of the arrow base along the rect edge. `0.dp` disables the arrow.
 * @param arrowHeight height of the arrow protrusion outside the rect edge.
 *   `0.dp` disables the arrow. The actual protrusion is `arrowHeight / 2`,
 *   matching `RadiusLayout`.
 * @param arrowOrientation which edge of the rect the arrow sits on.
 *   START/END are resolved against [LayoutDirection] at outline time.
 * @param arrowPositionRatio fraction along the rect's width (TOP/BOTTOM) or
 *   height (START/END) where the arrow's center is anchored. Clamped so the arrow
 *   stays within the rounded portion of the edge.
 * @param strokeThickness thickness of an outer stroke. The outline is inset by
 *   `strokeThickness / 2` so a stroked draw stays inside the layout bounds.
 *   Pure fills can leave this at `0.dp`.
 */
public class BalloonShape(
  public val cornerRadius: Dp,
  public val arrowWidth: Dp,
  public val arrowHeight: Dp,
  public val arrowOrientation: ArrowOrientation,
  public val arrowPositionRatio: Float = 0.5f,
  public val strokeThickness: Dp = 0.dp,
) : Shape {

  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val arrowWidthPx = with(density) { arrowWidth.toPx() }
    val arrowHeightPx = with(density) { arrowHeight.toPx() }
    val halfStrokePx = with(density) { strokeThickness.toPx() } / 2f

    val side = arrowOrientation.resolve(layoutDirection)

    val path = buildBalloonPath(
      size = size,
      cornerRadiusPx = cornerRadiusPx,
      arrowWidthPx = arrowWidthPx,
      arrowHeightPx = arrowHeightPx,
      side = side,
      ratioInRect = arrowPositionRatio,
      halfStrokePx = halfStrokePx,
    )
    return Outline.Generic(path)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BalloonShape) return false
    return cornerRadius == other.cornerRadius &&
      arrowWidth == other.arrowWidth &&
      arrowHeight == other.arrowHeight &&
      arrowOrientation == other.arrowOrientation &&
      arrowPositionRatio == other.arrowPositionRatio &&
      strokeThickness == other.strokeThickness
  }

  override fun hashCode(): Int {
    var result = cornerRadius.hashCode()
    result = 31 * result + arrowWidth.hashCode()
    result = 31 * result + arrowHeight.hashCode()
    result = 31 * result + arrowOrientation.hashCode()
    result = 31 * result + arrowPositionRatio.hashCode()
    result = 31 * result + strokeThickness.hashCode()
    return result
  }

  override fun toString(): String =
    "BalloonShape(cornerRadius=$cornerRadius, arrowWidth=$arrowWidth, " +
      "arrowHeight=$arrowHeight, arrowOrientation=$arrowOrientation, " +
      "arrowPositionRatio=$arrowPositionRatio, strokeThickness=$strokeThickness)"
}
