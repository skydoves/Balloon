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

package com.skydoves.balloon.radius

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.BalloonStroke.Companion.STROKE_THICKNESS_MULTIPLIER

/**
 * RadiusLayout clips four directions of inner layouts depending on the radius size.
 */
public class RadiusLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

  init {
    setWillNotDraw(false)
  }

  private val path = Path()
  private val fillPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
  private val strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.MITER // Ensure sharp corners for stroke
  }

  private val halfStroke: Float
    get() = if (strokePaint.strokeWidth > 0) strokePaint.strokeWidth / 2f else 0f

  public fun setFillColor(color: Int) {
    fillPaint.color = color
    invalidate()
  }

  public fun setStroke(thickness: Float, color: Int) {
    strokePaint.strokeWidth = thickness * STROKE_THICKNESS_MULTIPLIER
    strokePaint.color = color
    invalidate()
  }

  public var customShapeBackgroundDrawable: Drawable? = null
    set(value) {
      if (field != value) {
        field = value
        value?.setBounds(0, 0, width, height)
        invalidate()
      }
    }

  public var drawCustomShape: Boolean = false
    set(value) {
      if (field != value) {
        field = value
        setWillNotDraw(!value)
        invalidate()
      }
    }

  private var basePaddingLeft = 0
  private var basePaddingTop = 0
  private var basePaddingRight = 0
  private var basePaddingBottom = 0

  override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
    basePaddingLeft = left
    basePaddingTop = top
    basePaddingRight = right
    basePaddingBottom = bottom
    if (drawCustomShape) {
      updateEffectivePadding()
    } else {
      super.setPadding(left, top, right, bottom)
    }
  }

  public var radius: Float = 20f

  public var arrowHeight: Float = 40f
    set(value) {
      if (field != value) {
        field = value
        if (drawCustomShape) rebuildPath()
        if (drawCustomShape) updateEffectivePadding()
      }
    }

  public var arrowWidth: Float = 40f
    set(value) {
      if (field != value) {
        field = value
        if (drawCustomShape) rebuildPath()
      }
    }

  public var arrowPositionRatio: Float = 0.5f
    set(value) {
      if (field != value) {
        field = value
        if (drawCustomShape) rebuildPath()
      }
    }

  public var arrowOrientation: ArrowOrientation = ArrowOrientation.BOTTOM
    set(value) {
      if (field != value) {
        field = value
        if (drawCustomShape) rebuildPath()
        if (drawCustomShape) updateEffectivePadding()
      }
    }

  public fun updateEffectivePadding() {
    val effectiveLeft =
      basePaddingLeft + if (arrowOrientation == ArrowOrientation.START) {
        arrowHeight.toInt()
      } else {
        0
      }
    val effectiveTop =
      basePaddingTop + if (arrowOrientation == ArrowOrientation.TOP) {
        arrowHeight.toInt()
      } else {
        0
      }
    val effectiveRight =
      basePaddingRight + if (arrowOrientation == ArrowOrientation.END) {
        arrowHeight.toInt()
      } else {
        0
      }
    val effectiveBottom =
      basePaddingBottom + if (arrowOrientation == ArrowOrientation.BOTTOM) {
        arrowHeight.toInt()
      } else {
        0
      }

    super.setPadding(effectiveLeft, effectiveTop, effectiveRight, effectiveBottom)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (drawCustomShape) {
      rebuildPath()
      updateEffectivePadding()
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    if (drawCustomShape) {
      rebuildPath()
      updateEffectivePadding()
      customShapeBackgroundDrawable?.setBounds(0, 0, w, h)
    }
  }

  public fun rebuildPath() {
    path.reset()
    if (!drawCustomShape || width == 0 || height == 0) {
      return
    }

    /** The path accounts for halfStroke in its dimensions,
     * defining the center-line of the stroke.*/
    val rectLeft = if (arrowOrientation == ArrowOrientation.START) {
      arrowHeight + halfStroke
    } else {
      halfStroke
    }
    val rectTop = if (arrowOrientation == ArrowOrientation.TOP) {
      arrowHeight + halfStroke
    } else {
      halfStroke
    }
    val rectRight = if (arrowOrientation == ArrowOrientation.END) {
      width - arrowHeight - halfStroke
    } else {
      width - halfStroke
    }
    val rectBottom = if (arrowOrientation == ArrowOrientation.BOTTOM) {
      height - arrowHeight - halfStroke
    } else {
      height - halfStroke
    }

    val centerX = (width * arrowPositionRatio)
      .coerceIn(arrowWidth / 2f + halfStroke, width - arrowWidth / 2f - halfStroke)
    val centerY = (height * arrowPositionRatio)
      .coerceIn(arrowWidth / 2f + halfStroke, height - arrowWidth / 2f - halfStroke)

    val arrowProtrusion = arrowHeight * 0.5f // Factor for arrow sharpness.

    when (arrowOrientation) {
      ArrowOrientation.TOP -> {
        val tipY = rectTop - arrowProtrusion
        path.moveTo(radius + rectLeft, rectTop)
        path.lineTo(centerX - arrowWidth / 2f, rectTop)
        path.lineTo(centerX, tipY)
        path.lineTo(centerX + arrowWidth / 2f, rectTop)
        path.lineTo(rectRight - radius, rectTop)
        path.quadTo(rectRight, rectTop, rectRight, rectTop + radius)
        path.lineTo(rectRight, rectBottom - radius)
        path.quadTo(rectRight, rectBottom, rectRight - radius, rectBottom)
        path.lineTo(rectLeft + radius, rectBottom)
        path.quadTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
        path.lineTo(rectLeft, rectTop + radius)
        path.quadTo(rectLeft, rectTop, rectLeft + radius, rectTop)
      }

      ArrowOrientation.BOTTOM -> {
        val tipY = rectBottom + arrowProtrusion
        path.moveTo(radius + rectLeft, rectTop)
        path.lineTo(rectRight - radius, rectTop)
        path.quadTo(rectRight, rectTop, rectRight, rectTop + radius)
        path.lineTo(rectRight, rectBottom - radius)
        path.quadTo(rectRight, rectBottom, rectRight - radius, rectBottom)
        path.lineTo(centerX + arrowWidth / 2f, rectBottom)
        path.lineTo(centerX, tipY)
        path.lineTo(centerX - arrowWidth / 2f, rectBottom)
        path.lineTo(rectLeft + radius, rectBottom)
        path.quadTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
        path.lineTo(rectLeft, rectTop + radius)
        path.quadTo(rectLeft, rectTop, rectLeft + radius, rectTop)
      }

      ArrowOrientation.START -> {
        val tipX = rectLeft - arrowProtrusion
        path.moveTo(radius + rectLeft, rectTop)
        path.lineTo(rectRight - radius, rectTop)
        path.quadTo(rectRight, rectTop, rectRight, rectTop + radius)
        path.lineTo(rectRight, rectBottom - radius)
        path.quadTo(rectRight, rectBottom, rectRight - radius, rectBottom)
        path.lineTo(rectLeft + radius, rectBottom)
        path.quadTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
        path.lineTo(rectLeft, centerY + arrowWidth / 2f)
        path.lineTo(tipX, centerY)
        path.lineTo(rectLeft, centerY - arrowWidth / 2f)
        path.lineTo(rectLeft, rectTop + radius)
        path.quadTo(rectLeft, rectTop, rectLeft + radius, rectTop)
      }

      ArrowOrientation.END -> {
        val tipX = rectRight + arrowProtrusion
        path.moveTo(radius + halfStroke, rectTop)
        path.lineTo(rectRight - radius, rectTop)
        path.quadTo(rectRight, rectTop, rectRight, rectTop + radius)
        path.lineTo(rectRight, centerY - arrowWidth / 2f)
        path.lineTo(tipX, centerY)
        path.lineTo(rectRight, centerY + arrowWidth / 2f)
        path.lineTo(rectRight, rectBottom - radius)
        path.quadTo(rectRight, rectBottom, rectRight - radius, rectBottom)
        path.lineTo(rectLeft + radius, rectBottom)
        path.quadTo(rectLeft, rectBottom, rectLeft, rectBottom - radius)
        path.lineTo(rectLeft, rectTop + radius)
        path.quadTo(rectLeft, rectTop, rectLeft + radius, rectTop)
      }
    }
    path.close()
    invalidate()
  }

  override fun dispatchDraw(canvas: Canvas) {
    if (drawCustomShape && !path.isEmpty) {
      val save = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

      if (strokePaint.strokeWidth > 0) {
        canvas.drawPath(path, strokePaint)
      }
      canvas.clipPath(path)

      /**
       * Draw the fill.
       * If a customShapeBackgroundDrawable is provided, draw it.
       * It's expected that this drawable defines the fill (color/gradient)
       * and does NOT define its own stroke, as the RadiusLayout manages the stroke.
       * If no custom drawable, use the RadiusLayout's fillPaint for the fill.
       */
      if (customShapeBackgroundDrawable != null) {
        customShapeBackgroundDrawable?.draw(canvas)
      } else {
        canvas.drawPath(path, fillPaint)
      }
      super.dispatchDraw(canvas)

      canvas.restoreToCount(save)
    } else {
      super.dispatchDraw(canvas)
    }
  }
}
