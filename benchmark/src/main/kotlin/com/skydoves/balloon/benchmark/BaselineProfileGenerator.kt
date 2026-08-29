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

package com.skydoves.balloon.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Records the classes and methods Balloon touches while a balloon is actually shown.
 *
 * A startup-only journey would miss almost all of the library: the popup, the shape builder,
 * the position provider and the animations only run once something is shown. So the journey
 * below opens one balloon of every kind the demo offers - each entry animation, each highlight
 * animation, and both overlay shapes - and dismisses it again.
 *
 * Run against a rooted or `userdebug` device:
 * `./gradlew :benchmark:generateBaselineProfile`.
 */
class BaselineProfileGenerator {

  @get:Rule
  val baselineProfileRule = BaselineProfileRule()

  @Test
  fun generate() = baselineProfileRule.collect(
    packageName = PACKAGE,
    includeInStartupProfile = true,
  ) {
    pressHome()
    startActivityAndWait()
    device.waitForIdle()

    BUTTONS.forEach { label ->
      val button = device.wait(Until.findObject(By.text(label)), TIMEOUT) ?: return@forEach
      button.click()
      device.waitForIdle()
      // Dismiss by tapping well away from the balloon, which also exercises the
      // outside-touch path rather than only the show path.
      device.click(device.displayWidth / 2, device.displayHeight - 1)
      device.waitForIdle()
    }
  }

  private companion object {
    const val PACKAGE = "com.skydoves.balloon.kmpdemo"
    const val TIMEOUT = 5_000L
    val BUTTONS = listOf(
      "Elastic",
      "Fade",
      "Overshoot",
      "Heartbeat",
      "Shake",
      "Breath",
      "Oval Overlay",
      "RoundRect Overlay",
      "Edit Profile",
    )
  }
}
