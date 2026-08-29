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

package com.skydoves.balloon.kmpdemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.skydoves.balloon.sample.BalloonDemoScreen

/**
 * Hosts the shared [BalloonDemoScreen] composable inside an Android activity.
 *
 * This module deliberately lives alongside the existing `:app` (which uses the
 * AndroidX Compose stack) so the Compose-Multiplatform classpath stays isolated
 * from the legacy demo and there is no plugin / runtime conflict.
 *
 * The window setup mirrors `ComposeActivity` in `:app` — same `enableEdgeToEdge()`, same
 * `Toast` feedback — so the two demos can be screenshotted and compared pixel for pixel.
 */
class KmpBalloonActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    setContent {
      BalloonDemoScreen(
        onMessage = { message ->
          Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        },
      )
    }
  }
}
