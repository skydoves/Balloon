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
  id(libs.plugins.nexus.plugin.get().pluginId)
  id(libs.plugins.dokka.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

// NOTE on coordinates: the kotlin-multiplatform variant of vanniktech's maven
// publish plugin finalizes `groupId` earlier than the android-only variant
// (after reading `GROUP` from gradle.properties), so calling `coordinates(...)`
// here fails with "groupId$plugin is final and cannot be changed any further".
// We let the plugin auto-detect coordinates from `gradle.properties`:
//   - groupId  ← `GROUP=com.github.skydoves` (gradle.properties)
//   - artifactId ← project name = "balloon-compose-multiplatform"
//   - version  ← project.version (set below from `libVersion` extra)
// The pom name/description is the only thing we customize here.
project.version = rootProject.extra.get("libVersion").toString()
project.group = Configuration.artifactGroup

mavenPublishing {
  pom {
    name.set("balloon-compose-multiplatform")
    description.set("Compose Multiplatform tooltips for Android, iOS, Desktop, and Web (Wasm).")
  }
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
  androidTarget { publishLibraryVariants("release") }
  jvm("desktop")

  // No `binaries.framework` block — the library is a pure klib for KMP
  // consumers. iOS demo apps consume the framework via `:samples-shared`,
  // which re-exports this library.
  iosX64()
  iosArm64()
  iosSimulatorArm64()

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
            withIosX64()
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
        implementation(libs.compose.multiplatform.runtime)
        implementation(libs.compose.multiplatform.foundation)
        implementation(libs.compose.multiplatform.ui)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }

  explicitApi()
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon.compose.multiplatform"

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

// `kotlin { explicitApi() }` above already passes `-Xexplicit-api=strict` — no
// need to add it again here.

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
  // Match the JVM target of the rest of the repo (`:balloon`, `:balloon-compose`)
  // so consumers running on JVM 11 don't hit `UnsupportedClassVersionError`.
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
  }
}

tasks.withType<JavaCompile>().configureEach {
  this.targetCompatibility = libs.versions.jvmTarget.get()
  this.sourceCompatibility = libs.versions.jvmTarget.get()
}
