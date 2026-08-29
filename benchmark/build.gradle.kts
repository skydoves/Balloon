/*
 * Designed and developed by 2019 skydoves (Jaewoong Eum)
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
import com.skydoves.balloon.Configuration

plugins {
  id("com.android.test")
  id("org.jetbrains.kotlin.android")
  id(libs.plugins.baseline.profile.get().pluginId)
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon.benchmark"

  defaultConfig {
    minSdk = 28
    targetSdk = Configuration.targetSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  // The KMP demo is the only app that drives the library, so it is what the journey runs
  // against. Rules are filtered down to `com.skydoves.balloon.**` afterwards.
  targetProjectPath = ":androidApp"

  // Generation needs root, which a Play-Store emulator image never has. An AOSP managed
  // device gives CI a reproducible one; locally, any `userdebug` AVD works through
  // `useConnectedDevices`.
  testOptions.managedDevices.localDevices {
    create("aospApi35") {
      device = "Pixel 6"
      apiLevel = 35
      systemImageSource = "aosp"
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

baselineProfile {
  managedDevices += "aospApi35"
  // Off by default so `generateBaselineProfile` is reproducible: it always runs on the
  // managed device above. Pass `-Pballoon.connectedBaselineProfile` to use an attached
  // `userdebug` device instead (a Play-Store emulator image will not work - it cannot root).
  useConnectedDevices = providers.gradleProperty("balloon.connectedBaselineProfile").isPresent
}

dependencies {
  implementation(libs.androidx.test.runner)
  implementation(libs.androidx.test.uiautomator)
  implementation(libs.androidx.benchmark.macro)
  implementation(libs.androidx.profileinstaller)
}
