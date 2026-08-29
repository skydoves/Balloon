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

package com.skydoves.balloon.golden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.BalloonStyle

/**
 * One rendered configuration in the golden screenshot suite.
 *
 * A case is a pure description: it names itself, describes the balloon and says where it is
 * shown, and says nothing about how it gets captured. `GoldenHarness` renders it and
 * `GoldenCases` enumerates them, so neither has to know about the other beyond this type.
 *
 * Cases must be deterministic. Anything that moves on its own - entry animations, highlight
 * animations, auto-dismiss - has no place here: a frame captured mid-animation is a flaky
 * golden, and those paths are covered by behavioural tests instead. Leave
 * [BalloonStyle.animation] at `NONE`.
 *
 * @param name file-safe, stable identifier. It is the golden's filename, so renaming a case
 *   orphans its golden. Group with a `-` prefix, e.g. `arrow-size-12`, `overlay-circle`.
 * @param style the balloon under test.
 * @param align which side of the anchor the balloon is shown on.
 * @param centerAlign set for the `showAtCenter` family; `null` for the plain aligns.
 * @param offset the x/y offset passed to the show call. Not cosmetic: the provider adds it
 *   before deciding whether to flip, so an offset can change which side the balloon lands on
 *   and not just where it sits.
 * @param layoutDirection RTL cases mirror START/END and the position provider's clamp.
 * @param anchorSize the anchor's size. A wide anchor is what pushes `ALIGN_ANCHOR` into its
 *   constraint band, so several cases vary this rather than the style.
 * @param anchorAlignment where the anchor sits in the window. Corner placements are how the
 *   flip and clamp paths get exercised.
 * @param windowSize the scene size to render into, fixed so goldens do not depend on the
 *   machine's display.
 * @param content the balloon body. Defaults to a plain block rather than text, so a geometry
 *   change shows up as geometry instead of as font-metric noise.
 */
internal data class GoldenCase(
  val name: String,
  val style: BalloonStyle,
  val align: BalloonAlign = BalloonAlign.BOTTOM,
  val centerAlign: BalloonCenterAlign? = null,
  val offset: DpOffset = DpOffset.Zero,
  val layoutDirection: LayoutDirection = LayoutDirection.Ltr,
  val anchorSize: DpSize = DpSize(64.dp, 32.dp),
  val anchorAlignment: Alignment = Alignment.Center,
  val windowSize: IntSize = IntSize(400, 700),
  val content: @Composable () -> Unit = { GoldenBody() },
)

/**
 * The default balloon body: a fixed-size block, not text.
 *
 * Text would drag font metrics into every golden, so a one-pixel difference in how a glyph is
 * laid out would fail cases that have nothing to do with text. Cases that specifically care
 * about content sizing pass their own body.
 */
@Composable
internal fun GoldenBody(
  width: Int = 96,
  height: Int = 36,
  color: Color = Color(0xFFFFEB3B),
) {
  Box(modifier = Modifier.size(width.dp, height.dp).background(color))
}
