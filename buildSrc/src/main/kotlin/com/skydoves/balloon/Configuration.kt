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

package com.skydoves.balloon

object Configuration {
  const val compileSdk = 36

  /**
   * Compile SDK for the demo modules only.
   *
   * `cloudy`, which the glass tooltip demo uses, declares `minCompileSdk=37`. The published
   * library stays on 36 on purpose: its own `minCompileSdk` is part of what it asks of every
   * consumer, and a demo dependency is no reason to raise that.
   */
  const val compileSdkDemo = 37
  const val targetSdk = 36
  const val minSdk = 23
  const val minSdkBenchmark = 23
  const val majorVersion = 2
  const val minorVersion = 0
  const val patchVersion = 0
  const val versionName = "$majorVersion.$minorVersion.$patchVersion"
  const val versionCode = 76
  const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
  const val artifactGroup = "com.github.skydoves"
}
