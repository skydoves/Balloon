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
 * On the Skia backend, `usePlatformInsets = false` keeps the `Popup` in raw window
 * coordinates so it lines up with the absolute position our
 * [BalloonPopupPositionProvider] computes from `boundsInWindow()` — see the KDoc on the
 * `expect` declaration for why the default (`true`) would displace balloons by the
 * system-bar insets on iOS.
 */
internal actual fun balloonPopupProperties(
  focusable: Boolean,
  dismissOnBackPress: Boolean,
  dismissOnClickOutside: Boolean,
): PopupProperties = PopupProperties(
  focusable = focusable,
  dismissOnBackPress = dismissOnBackPress,
  dismissOnClickOutside = dismissOnClickOutside,
  usePlatformInsets = false,
)
