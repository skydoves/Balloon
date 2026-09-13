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

@file:OptIn(ExperimentalWasmDsl::class)

import com.skydoves.balloon.Configuration
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  id(libs.plugins.jetbrains.compose.get().pluginId)
  id(libs.plugins.compose.compiler.get().pluginId)
}

// `:samples-shared` is intentionally NOT published — it bundles the demo
// composable consumed by `:androidApp`, `:desktopApp`, `:wasmApp`, and `:iosApp`.
val artifactVersion = Configuration.versionName
group = Configuration.artifactGroup
version = artifactVersion

@OptIn(ExperimentalWasmDsl::class)
kotlin {
  androidTarget()
  jvm("desktop")

  // No `iosX64` here, unlike `:balloon`, because the glass demo depends on `cloudy`, which
  // does not publish an Intel simulator artifact. The library itself still targets it.
  listOf(
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach {
    it.binaries.framework {
      baseName = "shared"
      isStatic = true
      // Re-export the library so Swift sees `Balloon`, `BalloonState`, etc.
      // alongside `MainViewController` from the same `shared` framework.
      export(project(":balloon"))
    }
  }

  wasmJs {
    browser()
    binaries.library()
  }

  @Suppress("OPT_IN_USAGE")
  applyHierarchyTemplate {
    common {
      group("jvm") {
        withAndroidTarget()
        withJvm()
      }
      group("skia") {
        withJvm()
        group("apple") {
          group("ios") {
            withIosArm64()
            withIosSimulatorArm64()
          }
        }
        withWasmJs()
      }
    }
  }

  tasks.register("testClasses")

  sourceSets {
    val commonMain by getting {
      dependencies {
        // `api` so demo modules consuming `:samples-shared` transitively see
        // the library's public Compose APIs without re-declaring the dep.
        api(project(":balloon"))

        implementation(libs.compose.multiplatform.runtime)
        implementation(libs.compose.multiplatform.foundation)
        implementation(libs.compose.multiplatform.ui)
        implementation(libs.compose.multiplatform.material)
        // Ships the demo's profile picture to every target.
        implementation(libs.compose.multiplatform.resources)
        // Blur and liquid glass, used by the glass tooltip demo.
        implementation(libs.compose.cloudy)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}

// Pin the generated `Res` class so the shared demo can import it from common code.
compose.resources {
  publicResClass = false
  packageOfResClass = "com.skydoves.balloon.sample.resources"
  generateResClass = auto
}

android {
  compileSdk = Configuration.compileSdkDemo
  namespace = "com.skydoves.balloon.sample"

  defaultConfig {
    minSdk = Configuration.minSdk
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  lint {
    abortOnError = false
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
  // Match the JVM target of `:balloon` so consumers running on JVM 11 don't hit
  // `UnsupportedClassVersionError`.
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
  }
}

tasks.withType<JavaCompile>().configureEach {
  this.targetCompatibility = libs.versions.jvmTarget.get()
  this.sourceCompatibility = libs.versions.jvmTarget.get()
}
