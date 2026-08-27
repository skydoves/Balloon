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

package com.skydoves.balloondemo.parity

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.overlay.BalloonOverlayCircle
import com.skydoves.balloon.overlay.BalloonOverlayEmpty
import com.skydoves.balloon.overlay.BalloonOverlayOval
import com.skydoves.balloon.overlay.BalloonOverlayRect
import com.skydoves.balloon.overlay.BalloonOverlayRoundRect

/**
 * Renders one [ParityCase] with the View implementation so it can be screenshotted and
 * compared against the Compose Multiplatform rendering of the same case.
 *
 * Launch with `am start -n com.skydoves.balloondemo/.parity.ParityActivity --es case <id>`.
 *
 * The activity is deliberately bare: an opaque white window, a single fixed-position anchor
 * and a fixed-size balloon body, all painted in sentinel colors so the driver can recover the
 * exact rectangles from the PNG. Animations are forced off — a screenshot has to be taken of
 * a settled frame, not a frame mid-transition.
 */
class ParityActivity : ComponentActivity() {

  private var balloon: Balloon? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val case = intent.getStringExtra(EXTRA_CASE)?.let(::parityCase)
      ?: PARITY_CASES.first()

    val root = FrameLayout(this).apply {
      setBackgroundColor(PARITY_BACKGROUND_COLOR)
      fitsSystemWindows = false
    }
    val anchor = View(this).apply {
      setBackgroundColor(PARITY_ANCHOR_COLOR)
      layoutParams = FrameLayout.LayoutParams(
        case.anchorWidthDp.dpToPx(),
        case.anchorHeightDp.dpToPx(),
      ).apply {
        leftMargin = case.anchorLeftDp.dpToPx()
        topMargin = case.anchorTopDp.dpToPx()
      }
    }
    root.addView(anchor)
    setContentView(root)

    anchor.post {
      balloon = buildBalloon(case).also { it.show(case, anchor) }
    }
  }

  override fun onDestroy() {
    balloon?.dismiss()
    super.onDestroy()
  }

  private fun buildBalloon(case: ParityCase): Balloon {
    val body = View(this).apply {
      setBackgroundColor(PARITY_CONTENT_COLOR)
      layoutParams = FrameLayout.LayoutParams(
        case.contentWidthDp.dpToPx(),
        case.contentHeightDp.dpToPx(),
      )
    }
    return Balloon.Builder(this)
      .setLayout(body)
      .setBackgroundColor(PARITY_BODY_COLOR)
      .apply { if (case.distinctArrowColor) setArrowColor(PARITY_ARROW_COLOR) }
      .setCornerRadius(case.cornerRadiusDp)
      .setArrowWidth(case.arrowWidthDp)
      .setArrowHeight(case.arrowHeightDp)
      .setIsVisibleArrow(case.arrowVisible && case.arrowWidthDp > 0 && case.arrowHeightDp > 0)
      .setArrowPosition(case.arrowPosition)
      .setArrowPositionRules(ArrowPositionRules.valueOf(case.arrowPositionRules))
      .setArrowOrientationRules(ArrowOrientationRules.valueOf(case.arrowOrientationRules))
      .setArrowAlignAnchorPadding(case.arrowAlignAnchorPaddingDp)
      .setArrowAlignAnchorPaddingRatio(case.arrowAlignAnchorPaddingRatio)
      .apply { case.arrowOrientation?.let { setArrowOrientation(ArrowOrientation.valueOf(it)) } }
      .setPaddingLeft(case.padLeftDp)
      .setPaddingTop(case.padTopDp)
      .setPaddingRight(case.padRightDp)
      .setPaddingBottom(case.padBottomDp)
      .setMarginLeft(case.marginLeftDp)
      .setMarginTop(case.marginTopDp)
      .setMarginRight(case.marginRightDp)
      .setMarginBottom(case.marginBottomDp)
      .setWidth(if (case.widthDp >= 0) case.widthDp else BalloonSizeSpec.WRAP)
      .apply {
        if (case.widthRatio > 0f) setWidthRatio(case.widthRatio)
        if (case.minWidthRatio > 0f) setMinWidthRatio(case.minWidthRatio)
        if (case.maxWidthRatio > 0f) setMaxWidthRatio(case.maxWidthRatio)
        if (case.minWidthDp >= 0) setMinWidth(case.minWidthDp)
        if (case.maxWidthDp >= 0) setMaxWidth(case.maxWidthDp)
      }
      .setHeight(if (case.heightDp >= 0) case.heightDp else BalloonSizeSpec.WRAP)
      .setElevation(case.elevationDp)
      .apply {
        if (case.borderThicknessDp > 0f) {
          setBalloonStroke(PARITY_BORDER_COLOR, case.borderThicknessDp)
        }
      }
      .setAlpha(case.alpha)
      .setBalloonAnimation(BalloonAnimation.NONE)
      .apply {
        case.overlayShape?.let { shape ->
          setIsVisibleOverlay(true)
          setOverlayColor(PARITY_OVERLAY_COLOR)
          if (case.overlayPadStartDp >= 0f) {
            setOverlayPadding(
              left = case.overlayPadStartDp,
              top = case.overlayPadTopDp,
              right = case.overlayPadEndDp,
              bottom = case.overlayPadBottomDp,
            )
          } else {
            setOverlayPadding(case.overlayPaddingDp)
          }
          setOverlayShape(
            when (shape) {
              "EMPTY" -> BalloonOverlayEmpty
              "RECT" -> BalloonOverlayRect
              "CIRCLE" -> BalloonOverlayCircle(radius = case.overlayRadiusDp.dpToPxF())
              "ROUNDRECT" -> BalloonOverlayRoundRect(
                case.overlayRadiusDp.dpToPxF(),
                case.overlayRadiusDp.dpToPxF(),
              )
              else -> BalloonOverlayOval
            },
          )
        }
      }
      // The harness screenshots a settled frame; a focusable popup would also swallow the
      // driver's `am start` for the next case on some API levels.
      .setDismissWhenTouchOutside(false)
      .setFocusable(false)
      .build()
  }

  private fun Balloon.show(case: ParityCase, anchor: View) {
    val x = case.xOffDp.dpToPx()
    val y = case.yOffDp.dpToPx()
    when (case.align) {
      "TOP" -> showAlignTop(anchor, x, y)
      "BOTTOM" -> showAlignBottom(anchor, x, y)
      "START" -> showAlignStart(anchor, x, y)
      "END" -> showAlignEnd(anchor, x, y)
      "CENTER_TOP" -> showAtCenter(anchor, x, y, BalloonCenterAlign.TOP)
      "CENTER_BOTTOM" -> showAtCenter(anchor, x, y, BalloonCenterAlign.BOTTOM)
      "CENTER_START" -> showAtCenter(anchor, x, y, BalloonCenterAlign.START)
      "CENTER_END" -> showAtCenter(anchor, x, y, BalloonCenterAlign.END)
      "DROP_DOWN" -> showAsDropDown(anchor, x, y)
      else -> showAlignBottom(anchor, x, y)
    }
  }

  private fun Int.dpToPx(): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    toFloat(),
    resources.displayMetrics,
  ).toInt()

  private fun Float.dpToPxF(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    resources.displayMetrics,
  )

  private companion object {
    const val EXTRA_CASE = "case"
  }
}
