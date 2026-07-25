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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The handful of Material icons the demo needs, transcribed verbatim from
 * `androidx.compose.material.icons.filled` / `.automirrored.filled`.
 *
 * Compose Multiplatform stopped publishing `material-icons-extended` after 1.7.x, so the
 * shared demo cannot depend on it. The path data below was dumped straight out of the
 * androidx artifact the `:app` demo links against, which keeps the two demos rendering
 * identical glyphs — a prerequisite for the pixel-by-pixel comparison between them.
 */
internal object DemoIcons {

  val Home: ImageVector by lazy {
    ImageVector.Builder(
      name = "Home",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(10.0f, 20.0f)
        verticalLineToRelative(-6.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(6.0f)
        horizontalLineToRelative(5.0f)
        verticalLineToRelative(-8.0f)
        horizontalLineToRelative(3.0f)
        lineTo(12.0f, 3.0f)
        lineTo(2.0f, 12.0f)
        horizontalLineToRelative(3.0f)
        verticalLineToRelative(8.0f)
        close()
      }
    }.build()
  }

  val Person: ImageVector by lazy {
    ImageVector.Builder(
      name = "Person",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(12.0f, 12.0f)
        curveToRelative(2.21f, 0.0f, 4.0f, -1.79f, 4.0f, -4.0f)
        reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
        reflectiveCurveToRelative(-4.0f, 1.79f, -4.0f, 4.0f)
        reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
        close()
        moveTo(12.0f, 14.0f)
        curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(16.0f)
        verticalLineToRelative(-2.0f)
        curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
        close()
      }
    }.build()
  }

  val Settings: ImageVector by lazy {
    ImageVector.Builder(
      name = "Settings",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(19.14f, 12.94f)
        curveToRelative(0.04f, -0.3f, 0.06f, -0.61f, 0.06f, -0.94f)
        curveToRelative(0.0f, -0.32f, -0.02f, -0.64f, -0.07f, -0.94f)
        lineToRelative(2.03f, -1.58f)
        curveToRelative(0.18f, -0.14f, 0.23f, -0.41f, 0.12f, -0.61f)
        lineToRelative(-1.92f, -3.32f)
        curveToRelative(-0.12f, -0.22f, -0.37f, -0.29f, -0.59f, -0.22f)
        lineToRelative(-2.39f, 0.96f)
        curveToRelative(-0.5f, -0.38f, -1.03f, -0.7f, -1.62f, -0.94f)
        lineTo(14.4f, 2.81f)
        curveToRelative(-0.04f, -0.24f, -0.24f, -0.41f, -0.48f, -0.41f)
        horizontalLineToRelative(-3.84f)
        curveToRelative(-0.24f, 0.0f, -0.43f, 0.17f, -0.47f, 0.41f)
        lineTo(9.25f, 5.35f)
        curveTo(8.66f, 5.59f, 8.12f, 5.92f, 7.63f, 6.29f)
        lineTo(5.24f, 5.33f)
        curveToRelative(-0.22f, -0.08f, -0.47f, 0.0f, -0.59f, 0.22f)
        lineTo(2.74f, 8.87f)
        curveTo(2.62f, 9.08f, 2.66f, 9.34f, 2.86f, 9.48f)
        lineToRelative(2.03f, 1.58f)
        curveTo(4.84f, 11.36f, 4.8f, 11.69f, 4.8f, 12.0f)
        reflectiveCurveToRelative(0.02f, 0.64f, 0.07f, 0.94f)
        lineToRelative(-2.03f, 1.58f)
        curveToRelative(-0.18f, 0.14f, -0.23f, 0.41f, -0.12f, 0.61f)
        lineToRelative(1.92f, 3.32f)
        curveToRelative(0.12f, 0.22f, 0.37f, 0.29f, 0.59f, 0.22f)
        lineToRelative(2.39f, -0.96f)
        curveToRelative(0.5f, 0.38f, 1.03f, 0.7f, 1.62f, 0.94f)
        lineToRelative(0.36f, 2.54f)
        curveToRelative(0.05f, 0.24f, 0.24f, 0.41f, 0.48f, 0.41f)
        horizontalLineToRelative(3.84f)
        curveToRelative(0.24f, 0.0f, 0.44f, -0.17f, 0.47f, -0.41f)
        lineToRelative(0.36f, -2.54f)
        curveToRelative(0.59f, -0.24f, 1.13f, -0.56f, 1.62f, -0.94f)
        lineToRelative(2.39f, 0.96f)
        curveToRelative(0.22f, 0.08f, 0.47f, 0.0f, 0.59f, -0.22f)
        lineToRelative(1.92f, -3.32f)
        curveToRelative(0.12f, -0.22f, 0.07f, -0.47f, -0.12f, -0.61f)
        lineTo(19.14f, 12.94f)
        close()
        moveTo(12.0f, 15.6f)
        curveToRelative(-1.98f, 0.0f, -3.6f, -1.62f, -3.6f, -3.6f)
        reflectiveCurveToRelative(1.62f, -3.6f, 3.6f, -3.6f)
        reflectiveCurveToRelative(3.6f, 1.62f, 3.6f, 3.6f)
        reflectiveCurveTo(13.98f, 15.6f, 12.0f, 15.6f)
        close()
      }
    }.build()
  }

  val Edit: ImageVector by lazy {
    ImageVector.Builder(
      name = "Edit",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(3.0f, 17.25f)
        verticalLineTo(21.0f)
        horizontalLineToRelative(3.75f)
        lineTo(17.81f, 9.94f)
        lineToRelative(-3.75f, -3.75f)
        lineTo(3.0f, 17.25f)
        close()
        moveTo(20.71f, 7.04f)
        curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
        lineToRelative(-2.34f, -2.34f)
        curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
        lineToRelative(-1.83f, 1.83f)
        lineToRelative(3.75f, 3.75f)
        lineToRelative(1.83f, -1.83f)
        close()
      }
    }.build()
  }

  val AutoMirroredList: ImageVector by lazy {
    ImageVector.Builder(
      name = "AutoMirroredList",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
      autoMirror = true,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(3.0f, 13.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-2.0f)
        lineTo(3.0f, 11.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(3.0f, 17.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-2.0f)
        lineTo(3.0f, 15.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(3.0f, 9.0f)
        horizontalLineToRelative(2.0f)
        lineTo(5.0f, 7.0f)
        lineTo(3.0f, 7.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(7.0f, 13.0f)
        horizontalLineToRelative(14.0f)
        verticalLineToRelative(-2.0f)
        lineTo(7.0f, 11.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(7.0f, 17.0f)
        horizontalLineToRelative(14.0f)
        verticalLineToRelative(-2.0f)
        lineTo(7.0f, 15.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(7.0f, 7.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(14.0f)
        lineTo(21.0f, 7.0f)
        lineTo(7.0f, 7.0f)
        close()
      }
    }.build()
  }
}
