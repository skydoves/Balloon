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

package com.skydoves.balloon.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.skydoves.balloon.sample.glass.GlassTooltipScreen
import com.skydoves.balloon.sample.labs.BalloonLabsScreen

/**
 * The screens [BalloonSampleApp] can show.
 *
 * Deliberately tiny: the demo apps do not depend on a navigation library, so a single
 * enum plus a [remember] is all the state this sample needs.
 */
private enum class SampleScreen {
  Demo,
  Labs,
  Glass,
}

/**
 * Single entry point shared by every demo app (Android, iOS, Desktop, Wasm).
 *
 * It owns the sample's navigation state and swaps between [BalloonDemoScreen],
 * [BalloonLabsScreen], and [GlassTooltipScreen]. Platform entry points should call this instead of rendering a
 * screen directly, so every target reaches the same set of screens.
 *
 * The state is a plain [remember], not `rememberSaveable`: the sample intentionally
 * starts back on the demo screen after a configuration change or a page reload.
 *
 * @param onMessage invoked with a human-readable message when a demo action is triggered
 *   (the KMP stand-in for the original demo's `Toast`s).
 */
@Composable
public fun BalloonSampleApp(onMessage: (String) -> Unit = {}) {
  var screen by remember { mutableStateOf(SampleScreen.Demo) }

  when (screen) {
    SampleScreen.Demo -> BalloonDemoScreen(
      onMessage = onMessage,
      onOpenLabs = { screen = SampleScreen.Labs },
      onOpenGlass = { screen = SampleScreen.Glass },
    )

    SampleScreen.Labs -> BalloonLabsScreen(
      onBack = { screen = SampleScreen.Demo },
    )

    SampleScreen.Glass -> GlassTooltipScreen(
      onBack = { screen = SampleScreen.Demo },
    )
  }
}
