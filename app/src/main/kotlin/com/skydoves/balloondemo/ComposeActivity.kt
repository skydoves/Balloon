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

package com.skydoves.balloondemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.overlay.BalloonOverlayRoundRect

class ComposeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setWidthRatio(1.0f)
        setHeight(BalloonSizeSpec.WRAP)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setArrowPosition(0.5f)
        setPadding(12)
        setMarginHorizontal(12)
        setTextSize(15f)
        setCornerRadius(8f)
        setBackgroundColorResource(R.color.skyBlue)
        setBalloonAnimation(BalloonAnimation.ELASTIC)
        setIsVisibleOverlay(true)
        setOverlayColorResource(R.color.overlay)
        setOverlayPaddingResource(R.dimen.editBalloonOverlayPadding)
        setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
        setOverlayShape(
          BalloonOverlayRoundRect(
            R.dimen.editBalloonOverlayRadius,
            R.dimen.editBalloonOverlayRadius
          )
        )
        setDismissWhenClicked(true)
      }

      Box(modifier = Modifier.fillMaxSize()) {
        Balloon(
          modifier = Modifier.align(Alignment.Center),
          builder = builder,
          balloonContent = {
            Text(
              modifier = Modifier.fillMaxWidth(),
              text = "Helloooooooooo!\nNice to meet youuuuu\nskydoves",
              textAlign = TextAlign.Center,
              color = Color.White
            )
          }
        ) { balloonWindow ->
          Button(
            modifier = Modifier.size(160.dp, 60.dp),
            onClick = {
              balloonWindow.showAlignTop()
            }
          ) {
            Text(text = "showAlignTop")
          }
        }
      }
    }
  }
}
