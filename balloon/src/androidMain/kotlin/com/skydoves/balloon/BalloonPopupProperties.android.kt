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
 * Android's `Popup` runs position providers directly in window coordinates (no platform-inset
 * translation), so a plain [PopupProperties] already matches the absolute coordinates our
 * position provider produces — with one exception, [PopupProperties.clippingEnabled].
 *
 * With clipping enabled (the framework default) `AndroidPopup` derives the `windowSize` it
 * hands to `calculatePosition` from `View.getWindowVisibleDisplayFrame()`, which **excludes**
 * the system bars, and clips the popup window to that same frame. Our anchor rectangles come
 * from `LayoutCoordinates.boundsInWindow()`, which under `enableEdgeToEdge()` is measured
 * from the physical top of the screen — so the two disagree by the status-bar height. The
 * observable symptoms are:
 *
 * - a balloon near the bottom flips above its anchor although there is room below, because
 *   `anchor.bottom + popupHeight > windowSize.height` fires a status-bar height too early;
 * - the final on-screen clamp makes the bottom strip of the window unreachable;
 * - the overlay scrim stops at the status bar instead of covering the whole screen, and its
 *   anchor cut-out is displaced downward by exactly the status-bar height.
 *
 * Disabling clipping switches the framework to the full window bounds (on API 30+,
 * `WindowMetrics.getBounds()`), which is the same coordinate space `boundsInWindow()` uses.
 * Nothing is lost by turning it off: [BalloonPopupPositionProvider] already clamps the
 * balloon inside the window itself, and honours [BalloonStyle.margin] while doing so.
 *
 * This is the Android counterpart of the Skia targets' `usePlatformInsets = false`.
 */
internal actual fun balloonPopupProperties(
  focusable: Boolean,
  dismissOnBackPress: Boolean,
  dismissOnClickOutside: Boolean,
): PopupProperties = PopupProperties(
  focusable = focusable,
  dismissOnBackPress = dismissOnBackPress,
  dismissOnClickOutside = dismissOnClickOutside,
  clippingEnabled = false,
)
