// Designed and developed by 2019 skydoves (Jaewoong Eum)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.skydoves.balloon.Configuration


plugins {
  id(libs.plugins.android.application.get().pluginId)
  id(libs.plugins.compose.compiler.get().pluginId)
  id(libs.plugins.baseline.profile.get().pluginId)
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon.benchmark.app"

  defaultConfig {
    applicationId = "com.skydoves.balloon.benchmark.app"
    minSdk = Configuration.minSdk
    targetSdk = Configuration.targetSdk
    versionCode = Configuration.versionCode
    versionName = Configuration.versionName
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    viewBinding = true
    compose = true
  }

  lint {
    abortOnError = false
  }

  buildTypes {
    create("benchmark") {
      isDebuggable = true
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

dependencies {
  implementation(project(":balloon"))
  implementation(project(":balloon-compose"))

  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.lifecycle)

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.runtime)

  baselineProfile(project(":benchmark"))
}
