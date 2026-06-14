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

package com.skydoves.balloon.compose.multiplatform.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point that hosts [BalloonDemoScreen] inside a [UIViewController]
 * so the iosApp Xcode project can embed it via `UIViewControllerRepresentable`.
 *
 * The Kotlin/Native compiler exposes this as `MainViewControllerKt.MainViewController()`
 * to Swift.
 */
public fun MainViewController(): UIViewController = ComposeUIViewController { BalloonDemoScreen() }
