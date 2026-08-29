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

import androidx.compose.ui.window.PopupProperties

/**
 * Builds the [PopupProperties] used for the balloon's `Popup`.
 *
 * [BalloonPopupPositionProvider] computes absolute window coordinates from the anchor's
 * `boundsInWindow()`. On the Skia targets (iOS / Desktop / Wasm) the `actual` therefore
 * sets `usePlatformInsets = false`: otherwise skiko's `Popup` runs position providers in an
 * inset-excluded coordinate space and re-adds the system-bar insets afterwards, which would
 * shift every balloon down by the status-bar height (and right by a notch/nav-bar inset) on
 * iOS. Android's `Popup` performs no such translation, so its `actual` is a plain
 * [PopupProperties].
 */
internal expect fun balloonPopupProperties(
  focusable: Boolean,
  dismissOnBackPress: Boolean,
  dismissOnClickOutside: Boolean,
): PopupProperties
