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
  const val compileSdk = 32
  const val targetSdk = 32
  const val minSdk = 17
  const val majorVersion = 1
  const val minorVersion = 4
  const val patchVersion = 7
  const val versionName = "$majorVersion.$minorVersion.$patchVersion"
  const val versionCode = 48
  const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
  const val artifactGroup = "com.github.skydoves"
}
