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
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id(libs.plugins.nexus.plugin.get().pluginId)
  id(libs.plugins.baseline.profile.get().pluginId)
  id(libs.plugins.dokka.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
  val artifactId = "balloon"
  coordinates(
    Configuration.artifactGroup,
    artifactId,
    rootProject.extra.get("libVersion").toString()
  )

  pom {
    name.set(artifactId)
    description.set("Modernized and sophisticated tooltips, fully customizable with an arrow and animations for Android.")
  }
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.balloon"

  defaultConfig {
    minSdk = Configuration.minSdk
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  resourcePrefix = "balloon"

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  lint {
    abortOnError = false
  }
}

baselineProfile {
  baselineProfileOutputDir = "."
  filter {
    include("com.skydoves.balloon.**")
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    freeCompilerArgs.addAll(
      "-Xexplicit-api=strict",
      "-opt-in=com.skydoves.balloon.annotations.InternalBalloonApi",
    )
  }

  target {
    compilations.configureEach {
      if (name.contains("Test")) {
        compileTaskProvider.configure {
          compilerOptions {
            freeCompilerArgs.set(listOf("-opt-in=com.skydoves.balloon.annotations.InternalBalloonApi"))
          }
        }
      }
    }
  }
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets {
    named("main") {
      moduleName.set("balloon")
      includes.from("README.md")
      sourceLink {
        remoteUrl.set(URL("https://github.com/skydoves/balloon"))
        remoteLineSuffix.set("#L")
      }
    }
  }
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

  testImplementation(libs.junit)
  testImplementation(libs.truth)
  testImplementation(libs.robolectric)
  testImplementation(libs.mockk)
  testImplementation(libs.androidx.test.core)

  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.truth)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)

  baselineProfile(project(":benchmark"))
  dokkaPlugin(libs.android.documentation.plugin)
}
