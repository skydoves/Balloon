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

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}
dependencyResolutionManagement {
  // PREFER_PROJECT (instead of FAIL_ON_PROJECT_REPOS) so that the
  // Kotlin/Wasm tasks can register the nodejs distribution repo on demand —
  // FAIL_ON_PROJECT_REPOS makes `:kotlinWasmNodeJsSetup` fail with
  // "repository 'Distributions at https://nodejs.org/dist' was added by unknown code".
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    google()
    mavenCentral()
  }
}

include(":app")
include(":balloon")
include(":benchmark")
include(":benchmark-app")
include(":balloon-compose")
include(":balloon-compose-multiplatform")
include(":samples-shared")
include(":wasmApp")
include(":desktopApp")
include(":androidApp")
