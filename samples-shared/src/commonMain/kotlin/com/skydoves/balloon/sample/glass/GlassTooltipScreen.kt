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

package com.skydoves.balloon.sample.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.BalloonHost
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky

/**
 * A tooltip that is made of glass instead of paint.
 *
 * The usual reason a tooltip hurts is that it is opaque. It lands on top of the thing you just
 * tapped and hides the neighbours you were comparing it against, which is the worst moment to
 * take context away. A glass tooltip keeps the grid readable underneath while staying legible
 * itself, so the answer and the question stay on screen together.
 *
 * Three pieces make that work, and they belong to two libraries:
 *
 * - `Modifier.sky(sky)` marks the grid as the content to capture.
 * - `Modifier.cloudy(sky = sky, ...)` draws that capture back, blurred, inside the balloon.
 * - `Modifier.liquidGlass(...)` bends it at the edges so the panel reads as a lens rather
 *   than as a flat translucent rectangle.
 *
 * The part worth knowing is that the balloon body lives in a `Popup`, which is its own window
 * on Android and its own scene layer on the Skia targets. The backdrop is still sampled at the
 * right place, because `Sky` holds a shared capture rather than reading whatever happens to be
 * behind the current window.
 *
 * @param onBack invoked when the back affordance is pressed.
 */
@Composable
public fun GlassTooltipScreen(onBack: () -> Unit = {}) {
  val sky = rememberSky()
  var lensEnabled by remember { mutableStateOf(true) }
  var blurRadius by remember { mutableStateOf(28) }

  BalloonHost {
    Box(modifier = Modifier.fillMaxSize().background(Backdrop)) {
      // The capture source is the whole screen behind the glass, top bar included.
      //
      // Deliberately a plain scrolling Column rather than a `LazyVerticalGrid`. A lazy
      // container as the capture source draws nothing at all on Android: the items compose
      // and appear in the semantics tree, but no pixels reach the screen. The same code is
      // fine on Desktop, and a non lazy source is fine on both. Eight tiles do not need
      // recycling anyway.
      Column(
        modifier = Modifier
          .fillMaxSize()
          .sky(sky),
      ) {
        GlassTopBar(onBack = onBack)
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          GalleryItems.chunked(2).forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              row.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                  GalleryCard(
                    item = item,
                    sky = sky,
                    blurRadius = blurRadius,
                    lensEnabled = lensEnabled,
                  )
                }
              }
            }
          }
        }
      }
      GlassControls(
        sky = sky,
        lensEnabled = lensEnabled,
        onLensChange = { lensEnabled = it },
        blurRadius = blurRadius,
        onBlurChange = { blurRadius = it },
        modifier = Modifier.align(Alignment.BottomCenter),
      )
    }
  }
}
