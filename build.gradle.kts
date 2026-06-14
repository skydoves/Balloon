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


plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.jetbrains.compose) apply false
  alias(libs.plugins.baseline.profile) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.nexus.plugin)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.binary.compatibility)
  alias(libs.plugins.dokka)
}

apiValidation {
  ignoredProjects.addAll(
    listOf(
      "app",
      "benchmark",
      "benchmark-app",
      "wasmApp",
      "desktopApp",
      "androidApp",
      // The demo composable lives in this non-published module; nothing to validate.
      "samples-shared",
    ),
  )
  // Dot-separated FQN (not slash): the KLib ABI validator rejects slash-form
  // package names as "illegal characters". Nothing public currently lives here
  // (the `:balloon` viewBinding classes are non-public), so this is defensive.
  ignoredPackages.add("com.skydoves.balloon.databinding")
  nonPublicMarkers.add("kotlin.PublishedApi")

  // `balloon-compose-multiplatform` publishes klibs for the native/wasm targets
  // in addition to the JVM/Android jars, so enable KLib ABI validation to baseline
  // its full multi-target public surface (run `:balloon-compose-multiplatform:apiDump`).
  @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
  klib {
    enabled = true
  }
}

subprojects {
  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)

  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("$buildDir/**/*.kt")
      ktlint().editorConfigOverride(
        mapOf(
          "indent_size" to "2",
          "continuation_indent_size" to "2"
        )
      )
      licenseHeaderFile(rootProject.file("spotless/spotless.license.kt"))
      trimTrailingWhitespace()
      endWithNewline()
    }
    format("xml") {
      target("**/*.xml")
      targetExclude("**/build/**/*.xml")
      // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
      licenseHeaderFile(rootProject.file("spotless/spotless.license.xml"), "(<[^!?])")
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
}