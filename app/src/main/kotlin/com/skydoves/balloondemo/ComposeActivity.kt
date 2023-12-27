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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.skydoves.balloon.compose.BalloonWindow
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.overlay.BalloonOverlayRoundRect

class ComposeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setWidth(BalloonSizeSpec.WRAP)
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
            R.dimen.editBalloonOverlayRadius,
          ),
        )
        setDismissWhenClicked(true)
      }

      var balloonWindow1: BalloonWindow? by remember { mutableStateOf(null) }
      var balloonWindow2: BalloonWindow? by remember { mutableStateOf(null) }
      var balloonWindow3: BalloonWindow? by remember { mutableStateOf(null) }

      Box(modifier = Modifier.fillMaxSize()) {
        Balloon(
          modifier = Modifier
            .padding(20.dp)
            .align(Alignment.Center),
          builder = builder,
          onBalloonWindowInitialized = { balloonWindow1 = it },
          onComposedAnchor = { balloonWindow1?.showAlignTop() },
          balloonContent = {
            Text(
              text = "Now you can edit your profile1 profile2 profile3 profile4",
              textAlign = TextAlign.Center,
              color = Color.White,
            )
          },
        ) {
          Button(
            modifier = Modifier.size(160.dp, 60.dp),
            onClick = { balloonWindow1?.showAlignTop() },
          ) {
            Text(text = "showAlignTop")
          }
        }

        Balloon(
          modifier = Modifier
            .padding(20.dp)
            .align(Alignment.TopStart),
          builder = builder,
          onBalloonWindowInitialized = { balloonWindow2 = it },
          balloonContent = {
            Text(
              text = "Now you can edit your profile!",
              textAlign = TextAlign.Center,
              color = Color.White,
            )
          },
        ) { balloonWindow ->
          Button(
            modifier = Modifier.size(160.dp, 60.dp),
            onClick = { balloonWindow2?.showAlignTop() },
          ) {
            Text(text = "wrap balloon")
          }
        }

        Balloon(
          modifier = Modifier
            .padding(20.dp)
            .align(Alignment.TopEnd),
          builder = builder,
          onBalloonWindowInitialized = { balloonWindow3 = it },
          balloonContent = {
            Box(modifier = Modifier.fillMaxWidth()) {
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .align(Alignment.CenterStart)
                  .background(Color.Blue),
              )
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .align(Alignment.Center)
                  .background(Color.Blue),
              )
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .border(2.dp, Color.Red)
                  .align(Alignment.CenterEnd)
                  .background(Color.Blue),
              )
            }
          },
        ) { balloonWindow ->
          Button(
            modifier = Modifier.size(160.dp, 60.dp),
            onClick = { balloonWindow3?.showAlignBottom() },
          ) {
            Text(text = "alignments")
          }
        }
      }
    }
  }
}
