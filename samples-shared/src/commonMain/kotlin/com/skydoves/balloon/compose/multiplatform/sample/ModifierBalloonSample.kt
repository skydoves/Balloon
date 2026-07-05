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

package com.skydoves.balloon.compose.multiplatform.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.compose.multiplatform.ArrowPositionRules
import com.skydoves.balloon.compose.multiplatform.BalloonAnimation
import com.skydoves.balloon.compose.multiplatform.BalloonHost
import com.skydoves.balloon.compose.multiplatform.balloon
import com.skydoves.balloon.compose.multiplatform.rememberBalloonBuilder
import com.skydoves.balloon.compose.multiplatform.rememberBalloonState

/**
 * Demonstrates the modifier-based API — `Modifier.balloon` + [BalloonHost] — the Compose
 * Multiplatform counterpart of the Android `balloon-compose` `Modifier.balloon`.
 *
 * A single [BalloonHost] wraps the subtree; each anchor decorates itself in place with
 * `Modifier.balloon(state) { body }` and triggers the balloon via its [state]. Because the
 * modifier only registers the anchor (the host emits the popup), the buttons never shift
 * even though they live in a `spacedBy` [Column].
 */
@Composable
fun ModifierBalloonSample(modifier: Modifier = Modifier) {
  BalloonHost(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      val aboveStyle = rememberBalloonBuilder {
        setBackgroundColor(Color(0xFF1E88E5))
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setBalloonAnimation(BalloonAnimation.ELASTIC)
      }
      val aboveState = rememberBalloonState(aboveStyle)
      Button(
        onClick = { aboveState.showAlignTop() },
        modifier = Modifier.balloon(aboveState) {
          Text(
            text = "Shown above via Modifier.balloon",
            color = Color.White,
            modifier = Modifier.padding(4.dp),
          )
        },
      ) {
        Text("Show above")
      }

      val belowStyle = rememberBalloonBuilder {
        setBackgroundColor(Color(0xFFC51162))
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setAutoDismissDuration(2_000L)
      }
      val belowState = rememberBalloonState(belowStyle)
      Button(
        onClick = { belowState.showAlignBottom() },
        modifier = Modifier.balloon(belowState) {
          Text(
            text = "Auto-dismisses in 2s",
            color = Color.White,
            modifier = Modifier.padding(4.dp),
          )
        },
      ) {
        Text("Show below (auto-dismiss)")
      }
    }
  }
}
