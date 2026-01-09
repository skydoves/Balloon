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

package com.skydoves.balloon.overlay

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import com.skydoves.balloon.BalloonOverlayPadding
import com.skydoves.balloon.extensions.dimen
import com.skydoves.balloon.internals.viewProperty

/**
 * BalloonAnchorOverlayView is an overlay view for highlighting an anchor
 * by overlaying specific shapes on the anchor.
 */
public class BalloonAnchorOverlayView @JvmOverloads constructor(
  context: Context,
  attr: AttributeSet? = null,
  defStyle: Int = 0,
) : View(context, attr, defStyle) {

  /** target view for highlighting. */
  public var anchorView: View? by viewProperty(null)

  /** target views for highlighting. */
  public var anchorViewList: List<View>? by viewProperty(null)

  /** background color of the overlay. */
  @get:ColorInt
  public var overlayColor: Int by viewProperty(Color.TRANSPARENT)

  /** padding color of the overlay shape. */
  @get:ColorInt
  public var overlayPaddingColor: Int by viewProperty(Color.TRANSPARENT)

  /** shader of the overlay padding's painter. */
  public var overlayPaddingShader: Shader? by viewProperty(null)

  /** padding value of the internal overlay shape. */
  public var overlayPadding: BalloonOverlayPadding by viewProperty(BalloonOverlayPadding())

  /** specific position of the overlay shape. */
  public var overlayPosition: Point? by viewProperty(null)

  /** shape of the overlay over the anchor view. */
  public var balloonOverlayShape: BalloonOverlayShape by viewProperty(BalloonOverlayOval)

  private var bitmap: Bitmap? = null
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val paddingColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var invalidated: Boolean = false

  init {
    paint.apply {
      isAntiAlias = true
      isFilterBitmap = true
      isDither = true
    }
    paddingColorPaint.apply {
      isAntiAlias = true
      isFilterBitmap = true
      isDither = true
    }
  }

  public fun forceInvalidate() {
    invalidated = true
    invalidate()
  }

  override fun dispatchDraw(canvas: Canvas) {
    if (invalidated || bitmap == null || bitmap?.isRecycled == true) {
      prepareBitmap()
    }

    val bitmap = this.bitmap
    if (bitmap != null && !bitmap.isRecycled) {
      canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
  }

  private fun prepareBitmap() {
    if (width == 0 || height == 0 || anchorView?.width == 0 || anchorView?.height == 0) return

    var localBitmap = bitmap
    if (localBitmap != null && !localBitmap.isRecycled) {
      localBitmap.recycle()
    }

    localBitmap = createBitmap(width, height)
    bitmap = localBitmap

    val canvas = Canvas(localBitmap)

    paint.apply {
      xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
      color = overlayColor
    }

    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    paint.apply {
      xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
      color = Color.TRANSPARENT
    }
    paddingColorPaint.apply {
      color = overlayPaddingColor
      style = Paint.Style.STROKE
      strokeWidth = overlayPadding.top
      shader = overlayPaddingShader
    }

    if (anchorViewList.isNullOrEmpty()) {
      addFocusViewInOverlay(anchorView, canvas)
    } else {
      anchorViewList?.forEach { view ->
        addFocusViewInOverlay(view, canvas)
      }
    }

    invalidated = false
  }

  private fun addFocusViewInOverlay(view: View?, canvas: Canvas) {
    view?.let { anchor ->
      val rect = Rect()
      anchor.getGlobalVisibleRect(rect)
      rect.offset(anchor.translationX.toInt(), anchor.translationY.toInt())
      val anchorRect = overlayPosition?.let { position ->
        RectF(
          position.x - overlayPadding.left,
          position.y - overlayPadding.top + getStatusBarHeight(),
          position.x + anchor.width + overlayPadding.right,
          position.y + anchor.height + overlayPadding.bottom + getStatusBarHeight(),
        )
      } ?: RectF(
        rect.left - overlayPadding.left,
        rect.top - overlayPadding.top,
        rect.right + overlayPadding.right,
        rect.bottom + overlayPadding.bottom,
      )

      val halfOverlayPadding = BalloonOverlayPadding(
        top = overlayPadding.top / 2,
        bottom = overlayPadding.bottom / 2,
        left = overlayPadding.left / 2,
        right = overlayPadding.right / 2,
      )

      val anchorPaddingRect = RectF(anchorRect).apply {
        inset(halfOverlayPadding.left, halfOverlayPadding.top)
      }

      when (val overlay = balloonOverlayShape) {
        is BalloonOverlayEmpty -> Unit
        is BalloonOverlayRect -> {
          canvas.drawRect(anchorRect, paint)
          canvas.drawRect(anchorPaddingRect, paddingColorPaint)
        }

        is BalloonOverlayOval -> {
          canvas.drawOval(anchorRect, paint)
          canvas.drawOval(anchorPaddingRect, paddingColorPaint)
        }

        is BalloonOverlayCircle -> {
          overlay.radius?.let { radius ->
            canvas.drawCircle(anchorRect.centerX(), anchorRect.centerY(), radius, paint)
            canvas.drawCircle(
              anchorPaddingRect.centerX(),
              anchorPaddingRect.centerY(),
              radius - overlayPadding.top / 2,
              paddingColorPaint,
            )
          }
          overlay.radiusRes?.let { radiusRes ->
            val resolvedRadius = context.dimen(radiusRes)
            canvas.drawCircle(
              anchorRect.centerX(),
              anchorRect.centerY(),
              resolvedRadius,
              paint,
            )
            canvas.drawCircle(
              anchorPaddingRect.centerX(),
              anchorPaddingRect.centerY(),
              resolvedRadius - overlayPadding.top / 2,
              paddingColorPaint,
            )
          }
        }

        is BalloonOverlayRoundRect -> {
          // Handle individual corner radii
          overlay.cornerRadii?.let { radii ->
            drawRoundRectWithCorners(canvas, anchorRect, radii, paint)
            val paddingRadii = floatArrayOf(
              (radii[0] - overlayPadding.left / 2).coerceAtLeast(0f),
              (radii[1] - overlayPadding.right / 2).coerceAtLeast(0f),
              (radii[2] - overlayPadding.right / 2).coerceAtLeast(0f),
              (radii[3] - overlayPadding.left / 2).coerceAtLeast(0f),
            )
            drawRoundRectWithCorners(canvas, anchorPaddingRect, paddingRadii, paddingColorPaint)
            return@let
          }
          overlay.cornerRadiiRes?.let { radiiRes ->
            val radii = floatArrayOf(
              context.dimen(radiiRes[0]),
              context.dimen(radiiRes[1]),
              context.dimen(radiiRes[2]),
              context.dimen(radiiRes[3]),
            )
            drawRoundRectWithCorners(canvas, anchorRect, radii, paint)
            val paddingRadii = floatArrayOf(
              (radii[0] - overlayPadding.left / 2).coerceAtLeast(0f),
              (radii[1] - overlayPadding.right / 2).coerceAtLeast(0f),
              (radii[2] - overlayPadding.right / 2).coerceAtLeast(0f),
              (radii[3] - overlayPadding.left / 2).coerceAtLeast(0f),
            )
            drawRoundRectWithCorners(canvas, anchorPaddingRect, paddingRadii, paddingColorPaint)
            return@let
          }
          // Handle uniform corner radii (original behavior)
          overlay.radiusPair?.let { radiusPair ->
            canvas.drawRoundRect(anchorRect, radiusPair.first, radiusPair.second, paint)
            canvas.drawRoundRect(
              anchorPaddingRect,
              radiusPair.first - overlayPadding.left / 2,
              radiusPair.second - overlayPadding.top / 2,
              paddingColorPaint,
            )
          }
          overlay.radiusResPair?.let { radiusResPair ->
            val radiusX = context.dimen(radiusResPair.first)
            val radiusY = context.dimen(radiusResPair.second)
            canvas.drawRoundRect(
              anchorRect,
              radiusX,
              radiusY,
              paint,
            )
            canvas.drawRoundRect(
              anchorPaddingRect,
              radiusX - overlayPadding.left / 2,
              radiusY - overlayPadding.top / 2,
              paddingColorPaint,
            )
          }
        }
      }
    }
  }

  /**
   * Draws a rounded rectangle with individual corner radii.
   * @param canvas The canvas to draw on
   * @param rect The rectangle bounds
   * @param cornerRadii Array of 4 corner radii: [topStart, topEnd, bottomEnd, bottomStart]
   * @param paint The paint to use for drawing
   */
  private fun drawRoundRectWithCorners(
    canvas: Canvas,
    rect: RectF,
    cornerRadii: FloatArray,
    paint: Paint,
  ) {
    // Convert 4 corner radii to 8 radii (x, y pairs for each corner)
    // Order: [topLeft.x, topLeft.y, topRight.x, topRight.y,
    //         bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y]
    val radii = floatArrayOf(
      cornerRadii[0],
      cornerRadii[0], // top-left (topStart)
      cornerRadii[1],
      cornerRadii[1], // top-right (topEnd)
      cornerRadii[2],
      cornerRadii[2], // bottom-right (bottomEnd)
      cornerRadii[3],
      cornerRadii[3], // bottom-left (bottomStart)
    )
    val path = Path().apply {
      addRoundRect(rect, radii, Path.Direction.CW)
    }
    canvas.drawPath(path, paint)
  }

  private fun getStatusBarHeight(): Int {
    val rectangle = Rect()
    val context = context
    return if (context is Activity) {
      context.window.decorView.getWindowVisibleDisplayFrame(rectangle)
      rectangle.top
    } else {
      0
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    invalidated = true
  }
}
