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
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowOrientation
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
        setArrowPosition(0.2f)
        setArrowOrientation(ArrowOrientation.BOTTOM)
        setPadding(12)
        setMarginHorizontal(12)
        setTextSize(15f)
        setCornerRadius(8f)
        setBackgroundColorResource(R.color.skyBlue)
        setBalloonAnimation(BalloonAnimation.ELASTIC)
        setDismissWhenTouchOutside(false)
        setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
        setOverlayShape(
          BalloonOverlayRoundRect(
            R.dimen.editBalloonOverlayRadius,
            R.dimen.editBalloonOverlayRadius,
          ),
        )
        setDismissWhenClicked(true)
      }

      Box(modifier = Modifier.fillMaxSize()) {

        val items = List(20) { it.toString() }
        val view = LocalView.current

        LazyColumn {
          items(items = items, key = { it }) {
            var balloonWindow1: BalloonWindow? by remember { mutableStateOf(null) }
            var anchorView: View? by remember { mutableStateOf(null) }

            Balloon(
              modifier = Modifier
                .padding(20.dp)
                .align(Alignment.Center)
                .onGloballyPositioned {
                  val position = it.positionInWindow()
                  if (balloonWindow1?.balloon?.isShowing == true) {
                    val calculatedY =
                      position.y.toInt() - (balloonWindow1!!.getMeasuredHeight() + anchorView!!.measuredHeight / 2)
                    balloonWindow1!!.balloon.update(view, 0, calculatedY)
                  }
                },
              builder = builder,
              onComposedAnchor = { anchorView = it },
              onBalloonWindowInitialized = { balloonWindow1 = it },
              balloonContent = {
                Text(
                  text = "Now you can edit your profile1 profile2 profile3 profile4",
                  textAlign = TextAlign.Center,
                  color = Color.White,
                )
              },
            ) {
              Button(
                modifier = Modifier
                  .size(160.dp, 60.dp),
                onClick = { balloonWindow1?.showAlignTop() },
              ) {
                Text(text = "showAlignTop")
              }
            }
          }
        }
      }
    }
  }
}
