package com.skydoves.balloon.benchmark

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

@ExperimentalBaselineProfilesApi
class BaselineProfileGenerator {
  @get:Rule
  val baselineProfileRule = BaselineProfileRule()

  @Test
  fun startup() =
    baselineProfileRule.collectBaselineProfile(
      packageName = "com.skydoves.balloon.benchmark.app"
    ) {
      pressHome()
      // This block defines the app's critical user journey. Here we are interested in
      // optimizing for app startup. But you can also navigate and scroll
      // through your most important UI.
      startActivityAndWait()
      device.waitForIdle()
    }
}
