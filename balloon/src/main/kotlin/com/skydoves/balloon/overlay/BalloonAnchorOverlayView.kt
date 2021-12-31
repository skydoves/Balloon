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
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.skydoves.balloon.extensions.dimen
import com.skydoves.balloon.internal.viewProperty

/**
 * BalloonAnchorOverlayView is an overlay view for highlighting an anchor
 * by overlaying specific shapes on the anchor.
 */
public class BalloonAnchorOverlayView @JvmOverloads constructor(
  context: Context,
  attr: AttributeSet? = null,
  defStyle: Int = 0
) : View(context, attr, defStyle) {

  /** target view for highlighting. */
  public var anchorView: View? by viewProperty(null)

  public var anchorViewList: List<View>? = null

  /** background color of the overlay. */
  @get:ColorInt
  public var overlayColor: Int by viewProperty(Color.TRANSPARENT)

  /** padding color of the overlay shape. */
  @get:ColorInt
  public var overlayPaddingColor: Int by viewProperty(Color.TRANSPARENT)

  /** padding value of the internal overlay shape. */
  @get:Px
  public var overlayPadding: Float by viewProperty(0f)

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

  override fun dispatchDraw(canvas: Canvas?) {
    if (invalidated || bitmap == null || bitmap?.isRecycled == true) {
      prepareBitmap()
    }

    val bitmap = this.bitmap
    if (bitmap != null && !bitmap.isRecycled) {
      canvas?.drawBitmap(bitmap, 0f, 0f, null)
    }
  }

  private fun prepareBitmap() {
    if (width == 0 || height == 0 || anchorView?.width == 0 || anchorView?.height == 0) return

    var localBitmap = bitmap
    if (localBitmap != null && !localBitmap.isRecycled) {
      localBitmap.recycle()
    }

    localBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
      strokeWidth = overlayPadding
    }

    if (anchorViewList.isNullOrEmpty()){
      addFocusViewInOverlay(anchorView,canvas)
    }else{
      anchorViewList?.forEach { view ->
        addFocusViewInOverlay(view,canvas)
      }
    }

    invalidated = false
  }

  private fun addFocusViewInOverlay(view: View?,canvas: Canvas) {
    view?.let { anchor ->
      val rect = Rect()
      anchor.getGlobalVisibleRect(rect)
      val anchorRect = overlayPosition?.let { position ->
        RectF(
          position.x - overlayPadding,
          position.y - overlayPadding + getStatusBarHeight(),
          position.x + anchor.width + overlayPadding,
          position.y + anchor.height + overlayPadding + getStatusBarHeight()
        )
      } ?: RectF(
        rect.left - overlayPadding,
        rect.top - overlayPadding,
        rect.right + overlayPadding,
        rect.bottom + overlayPadding
      )

      val halfOfOverlayPadding = overlayPadding / 2
      val anchorPaddingRect = RectF(anchorRect).apply {
        inset(halfOfOverlayPadding, halfOfOverlayPadding)
      }

      when (val overlay = balloonOverlayShape) {
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
              radius - halfOfOverlayPadding,
              paddingColorPaint
            )
          }
          overlay.radiusRes?.let { radiusRes ->
            canvas.drawCircle(
              anchorRect.centerX(), anchorRect.centerY(), context.dimen(radiusRes),
              paint
            )
            canvas.drawCircle(
              anchorPaddingRect.centerX(), anchorPaddingRect.centerY(),
              context.dimen(radiusRes) - halfOfOverlayPadding,
              paddingColorPaint
            )
          }
        }
        is BalloonOverlayRoundRect -> {
          overlay.radiusPair?.let { radiusPair ->
            canvas.drawRoundRect(anchorRect, radiusPair.first, radiusPair.second, paint)
            canvas.drawRoundRect(
              anchorPaddingRect,
              radiusPair.first - halfOfOverlayPadding,
              radiusPair.second - halfOfOverlayPadding,
              paddingColorPaint
            )
          }
          overlay.radiusResPair?.let { radiusResPair ->
            canvas.drawRoundRect(
              anchorRect, context.dimen(radiusResPair.first),
              context.dimen(radiusResPair.second), paint
            )
            canvas.drawRoundRect(
              anchorPaddingRect, context.dimen(radiusResPair.first) - halfOfOverlayPadding,
              context.dimen(radiusResPair.second) - halfOfOverlayPadding, paddingColorPaint
            )
          }
        }
      }
      }
  }

  private fun getStatusBarHeight(): Int {
    val rectangle = Rect()
    val context = context
    return if (context is Activity) {
      context.window.decorView.getWindowVisibleDisplayFrame(rectangle)
      rectangle.top
    } else 0
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    invalidated = true
  }
}
