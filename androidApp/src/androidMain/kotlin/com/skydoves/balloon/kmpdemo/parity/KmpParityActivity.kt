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

package com.skydoves.balloon.kmpdemo.parity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.compose.multiplatform.ArrowOrientation
import com.skydoves.balloon.compose.multiplatform.ArrowOrientationRules
import com.skydoves.balloon.compose.multiplatform.ArrowPositionRules
import com.skydoves.balloon.compose.multiplatform.Balloon
import com.skydoves.balloon.compose.multiplatform.BalloonAlign
import com.skydoves.balloon.compose.multiplatform.BalloonAnimation
import com.skydoves.balloon.compose.multiplatform.BalloonCenterAlign
import com.skydoves.balloon.compose.multiplatform.BalloonHost
import com.skydoves.balloon.compose.multiplatform.BalloonOverlayShape
import com.skydoves.balloon.compose.multiplatform.BalloonStyle
import com.skydoves.balloon.compose.multiplatform.rememberBalloonState

/**
 * The Compose Multiplatform twin of `com.skydoves.balloondemo.parity.ParityActivity`: renders
 * exactly one [ParityCase] with the KMP API so the two screenshots can be diffed.
 *
 * Launch with
 * `am start -n com.skydoves.balloon.kmpdemo/.parity.KmpParityActivity --es case <id>`.
 */
class KmpParityActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val case = intent.getStringExtra("case")?.let(::parityCase) ?: PARITY_CASES.first()

    setContent {
      BalloonHost(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(PARITY_BACKGROUND_COLOR)),
        ) {
          val state = rememberBalloonState(style = case.toStyle())
          LaunchedEffect(Unit) { state.show(case) }
          Balloon(
            modifier = Modifier.offset(case.anchorLeftDp.dp, case.anchorTopDp.dp),
            state = state,
            balloonContent = {
              Box(
                Modifier
                  .size(case.contentWidthDp.dp, case.contentHeightDp.dp)
                  .background(Color(PARITY_CONTENT_COLOR)),
              )
            },
          ) {
            Box(
              Modifier
                .size(case.anchorWidthDp.dp, case.anchorHeightDp.dp)
                .background(Color(PARITY_ANCHOR_COLOR)),
            )
          }
        }
      }
    }
  }
}

private fun com.skydoves.balloon.compose.multiplatform.BalloonState.show(case: ParityCase) {
  val x = case.xOffDp.dp
  val y = case.yOffDp.dp
  when (case.align) {
    "TOP" -> showAlignTop(x, y)
    "BOTTOM" -> showAlignBottom(x, y)
    "START" -> showAlignStart(x, y)
    "END" -> showAlignEnd(x, y)
    "CENTER" -> show(BalloonAlign.CENTER, x, y)
    "CENTER_TOP" -> showAtCenter(BalloonCenterAlign.TOP, x, y)
    "CENTER_BOTTOM" -> showAtCenter(BalloonCenterAlign.BOTTOM, x, y)
    "CENTER_START" -> showAtCenter(BalloonCenterAlign.START, x, y)
    "CENTER_END" -> showAtCenter(BalloonCenterAlign.END, x, y)
    "DROP_DOWN" -> showAsDropDown(x, y)
    else -> showAlignBottom(x, y)
  }
}

private fun ParityCase.toStyle(): BalloonStyle = Balloon.Builder()
  .setBackgroundColor(Color(PARITY_BODY_COLOR))
  .apply { if (distinctArrowColor) setArrowColor(Color(PARITY_ARROW_COLOR)) }
  .setCornerRadius(cornerRadiusDp.dp)
  .setArrowSize(arrowWidthDp.dp, arrowHeightDp.dp)
  .setIsVisibleArrow(arrowVisible && arrowWidthDp > 0 && arrowHeightDp > 0)
  .setArrowPosition(arrowPosition)
  .setArrowPositionRules(ArrowPositionRules.valueOf(arrowPositionRules))
  .apply { arrowOrientation?.let { setArrowOrientation(ArrowOrientation.valueOf(it)) } }
  .setArrowOrientationRules(ArrowOrientationRules.valueOf(arrowOrientationRules))
  .setArrowAlignAnchorPadding(arrowAlignAnchorPaddingDp.dp)
  .setArrowAlignAnchorPaddingRatio(arrowAlignAnchorPaddingRatio)
  .setPadding(padLeftDp.dp, padTopDp.dp, padRightDp.dp, padBottomDp.dp)
  .setMargin(marginLeftDp.dp, marginTopDp.dp, marginRightDp.dp, marginBottomDp.dp)
  .setWidth(if (widthDp >= 0) widthDp.dp else Dp.Unspecified)
  .setWidthRatio(widthRatio)
  .setMinWidthRatio(minWidthRatio)
  .setMaxWidthRatio(maxWidthRatio)
  .setMinWidth(if (minWidthDp >= 0) minWidthDp.dp else Dp.Unspecified)
  .setMaxWidth(if (maxWidthDp >= 0) maxWidthDp.dp else Dp.Unspecified)
  .setHeight(if (heightDp >= 0) heightDp.dp else Dp.Unspecified)
  .setElevation(elevationDp.dp)
  .apply {
    if (borderThicknessDp > 0f) setBorder(Color(PARITY_BORDER_COLOR), borderThicknessDp.dp)
  }
  .setAlpha(alpha)
  .setBalloonAnimation(BalloonAnimation.NONE)
  .apply {
    overlayShape?.let { shape ->
      setIsVisibleOverlay(true)
      setOverlayColor(Color(PARITY_OVERLAY_COLOR))
      if (overlayPadStartDp >= 0f) {
        setOverlayPadding(
          start = overlayPadStartDp.dp,
          top = overlayPadTopDp.dp,
          end = overlayPadEndDp.dp,
          bottom = overlayPadBottomDp.dp,
        )
      } else {
        setOverlayPadding(overlayPaddingDp.dp)
      }
      setOverlayShape(
        when (shape) {
          "EMPTY" -> BalloonOverlayShape.Empty
          "RECT" -> BalloonOverlayShape.Rect
          "CIRCLE" -> BalloonOverlayShape.Circle(overlayRadiusDp.dp)
          "ROUNDRECT" -> BalloonOverlayShape.RoundRect(overlayRadiusDp.dp, overlayRadiusDp.dp)
          else -> BalloonOverlayShape.Oval
        },
      )
    }
  }
  .setDismissWhenTouchOutside(false)
  .setFocusable(false)
  .build()
