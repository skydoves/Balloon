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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.compose.BalloonComposeView
import com.skydoves.balloon.compose.LayoutInfo
import java.util.UUID

class ComposeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      Box(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current

        val balloon = Balloon.Builder(context)
          .setArrowSize(10)
          .setWidthRatio(1.0f)
          .setHeight(BalloonSizeSpec.WRAP)
          .setArrowOrientation(ArrowOrientation.BOTTOM)
          .setArrowPosition(0.5f)
          .setPadding(12)
          .setMarginHorizontal(12)
          .setTextSize(15f)
          .setCornerRadius(8f)
          .setTextColorResource(R.color.white_87)
          .setIconDrawableResource(R.drawable.ic_edit)
          .setBackgroundColorResource(R.color.skyBlue)
          .setOnBalloonDismissListener {
            Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show()
          }
          .setBalloonAnimation(BalloonAnimation.ELASTIC)

        val view = LocalView.current
        val compositionContext = rememberCompositionContext()
        val balloonComposeView = remember {
          BalloonComposeView(
            anchorView = view,
            builder = balloon,
            balloonID = UUID.randomUUID()
          ).apply {
            setContent(compositionContext) {
              Text(
                modifier = Modifier
                  .padding(12.dp)
                  .onGloballyPositioned { coordinates ->
                    val layoutInfo = LayoutInfo(
                      x = coordinates.positionInWindow().x,
                      y = coordinates.positionInWindow().y,
                      width = coordinates.size.width,
                      height = coordinates.size.height
                    )
                  },
                text = "Helloooooooooo!",
                color = Color.White
              )
            }
          }
        }

        Button(
          modifier = Modifier
            .size(120.dp, 75.dp)
            .align(Alignment.Center),
          onClick = { balloonComposeView.showAtCenter() }
        ) {
          Text(text = "click")
        }
      }
    }
  }
}
