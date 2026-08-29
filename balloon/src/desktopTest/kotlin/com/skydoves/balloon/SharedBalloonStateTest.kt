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

package com.skydoves.balloon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * One [BalloonState] driving two anchors must still settle.
 *
 * The position provider writes the resolved arrow placement from the layout pass, and the
 * popup reads it while composing. While that placement lived on `BalloonState`, two popups
 * sharing one state wrote conflicting values into the same holder and invalidated each other
 * on every frame: composition never went idle and the app hung. `BalloonArrowPlacement` is
 * per popup layer for exactly this reason.
 *
 * The two anchors below are placed so the resolutions genuinely conflict: the top-left one
 * has room below and keeps its arrow on TOP, the bottom-right one has to flip. Anchors that
 * happen to resolve alike settle either way and would not catch the regression.
 *
 * JVM-only because it needs a watchdog thread: a regression here hangs rather than fails.
 */
class SharedBalloonStateTest {

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun twoAnchorsSharingOneStateSettle() {
    val done = CountDownLatch(1)
    val watchdog = thread(isDaemon = true) {
      if (!done.await(60, TimeUnit.SECONDS)) {
        // The test thread is wedged in composition, so failing from here is the only way to
        // report it; `halt` skips shutdown hooks that would otherwise never run either.
        System.err.println("twoAnchorsSharingOneStateSettle: composition never went idle")
        Runtime.getRuntime().halt(1)
      }
    }
    try {
      runComposeUiTest {
        lateinit var state: BalloonState
        setContent {
          BalloonHost(modifier = Modifier.fillMaxSize().testTag("host")) {
            state = rememberBalloonState(BalloonStyle(animation = BalloonAnimation.NONE))
            Box(Modifier.fillMaxSize()) {
              Box(Modifier.align(Alignment.TopStart)) {
                Balloon(
                  state = state,
                  balloonContent = { Box(Modifier.size(40.dp)) },
                ) { Box(Modifier.size(20.dp).testTag("a1")) }
              }
              Box(Modifier.align(Alignment.BottomEnd)) {
                Balloon(
                  state = state,
                  balloonContent = { Box(Modifier.size(40.dp)) },
                ) { Box(Modifier.size(20.dp).testTag("a2")) }
              }
            }
          }
        }
        runOnUiThread { state.showAlignBottom() }
        waitForIdle()
        assertTrue(state.isVisible)
      }
    } finally {
      done.countDown()
      watchdog.interrupt()
    }
  }
}
