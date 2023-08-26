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

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
}

rootProject.extra.apply {
  set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
  set("PUBLISH_ARTIFACT_ID", "balloon")
  set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from ="${rootDir}/scripts/publish-module.gradle")

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon"

  defaultConfig {
    minSdk = Configuration.minSdk
  }

  resourcePrefix = "balloon"

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = libs.versions.jvmTarget.get()
  }

  lint {
    abortOnError = false
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.freeCompilerArgs += listOf("-Xexplicit-api=strict")
}

tasks.withType(JavaCompile::class.java).configureEach {
  this.targetCompatibility = libs.versions.jvmTarget.get()
  this.sourceCompatibility = libs.versions.jvmTarget.get()
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.lifecycle)
  implementation(libs.androidx.annotation)
}
