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
import com.skydoves.balloon.annotations.Dp
import com.skydoves.balloon.extensions.dp2Px

/**
 * BalloonAnchorOverlayView is an overlay view for highlighting an anchor
 * by overlaying specific shapes on the anchor.
 */
class BalloonAnchorOverlayView @JvmOverloads constructor(
  context: Context,
  attr: AttributeSet? = null,
  defStyle: Int = 0
) : View(context, attr, defStyle) {

  /** target view for highlighting. */
  private var _anchorView: View? = null
  var anchorView: View?
    get() = _anchorView
    set(value) {
      _anchorView = value
      invalidate()
    }

  /** background color of the overlay. */
  @ColorInt private var _overlayColor: Int = Color.TRANSPARENT
  var overlayColor: Int
    @ColorInt get() = _overlayColor
    set(@ColorInt value) {
      _overlayColor = value
      invalidate()
    }

  /** padding value of the internal overlay shape. */
  @Px private var _overlayPadding: Float = 0f
  var overlayPadding: Float
    @Px get() = _overlayPadding
    set(@Dp value) {
      _overlayPadding = context.dp2Px(value)
      invalidate()
    }

  /** specific position of the overlay shape. */
  private var _overlayPosition: Point? = null
  var overlayPosition: Point?
    get() = _overlayPosition
    set(value) {
      _overlayPosition = value
      invalidate()
    }

  /** shape of the overlay over the anchor view. */
  private var _balloonOverlayShape: BalloonOverlayShape = BalloonOverlayOval
  var balloonOverlayShape: BalloonOverlayShape
    get() = _balloonOverlayShape
    set(value) {
      _balloonOverlayShape = value
      invalidate()
    }

  private var bitmap: Bitmap? = null
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var invalidated: Boolean = false

  init {
    paint.apply {
      isAntiAlias = true
      isFilterBitmap = true
      isDither = true
    }
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

    anchorView?.let { anchor ->
      val anchorRect = overlayPosition?.let { position ->
        RectF(
          position.x - overlayPadding,
          position.y - overlayPadding + getStatusBarHeight(),
          position.x + anchor.width + overlayPadding,
          position.y + anchor.height + overlayPadding + getStatusBarHeight()
        )
      } ?: RectF(
        anchor.x - overlayPadding,
        anchor.y - overlayPadding + getStatusBarHeight(),
        anchor.x + anchor.width + overlayPadding,
        anchor.y + anchor.height + overlayPadding + getStatusBarHeight()
      )

      when (val overlay = balloonOverlayShape) {
        is BalloonOverlayRect -> canvas.drawRect(anchorRect, paint)
        is BalloonOverlayOval -> canvas.drawOval(anchorRect, paint)
        is BalloonOverlayCircle ->
          canvas.drawCircle(anchorRect.centerX(), anchorRect.centerY(), overlay.radius, paint)
        is BalloonOverlayRoundRect ->
          canvas.drawRoundRect(anchorRect, overlay.radiusX, overlay.radiusY, paint)
      }
    }

    invalidated = false
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
