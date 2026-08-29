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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.skydoves.balloon.BalloonHost
import com.skydoves.balloon.BalloonState
import com.skydoves.balloon.balloon
import com.skydoves.balloon.rememberBalloonState
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.test.fail

/**
 * Renders [case] and either compares it against its stored golden or re-records it.
 *
 * This is the only entry point the case suite needs: everything about scene setup, capture,
 * file layout and diffing lives behind it, so adding a case never means touching the harness.
 *
 * Compare mode (the default) fails when the golden is missing, when the rendered size differs,
 * or when any pixel differs by more than [CHANNEL_TOLERANCE] on any channel. Every failure
 * leaves `expected` / `actual` / `diff` PNGs in the report directory, because a screenshot
 * failure is unreadable as text and a human has to look at it.
 *
 * Update mode (`-Pballoon.updateGolden`) overwrites the golden and passes. Re-record
 * deliberately: an unreviewed `-Pballoon.updateGolden` turns a regression into a new baseline.
 *
 * The case must be quiescent. [waitForIdle][androidx.compose.ui.test.ComposeUiTest.waitForIdle]
 * spins until the scene stops invalidating, so a looping highlight animation would hang here
 * rather than fail; see the `GoldenCase` KDoc for why animated cases do not belong.
 */
internal fun assertGolden(case: GoldenCase) {
  val actual = render(case)
  val golden = File(goldenDir, "${case.name}.png")

  if (isUpdateMode) {
    goldenDir.mkdirs()
    ImageIO.write(actual, PNG_FORMAT, golden)
    return
  }

  if (!golden.isFile) {
    val written = writeReport(case.name, ACTUAL_SUFFIX, actual)
    fail(
      "Golden '${case.name}' has never been recorded: ${golden.absolutePath}\n" +
        "The render is at ${written.absolutePath} (${actual.width}x${actual.height}).\n" +
        RE_RECORD_HINT,
    )
  }

  val expected = ImageIO.read(golden) ?: fail(
    "Golden '${case.name}' is not a readable PNG: ${golden.absolutePath}\n" + RE_RECORD_HINT,
  )
  val difference = difference(expected, actual) ?: return

  val expectedFile = writeReport(case.name, EXPECTED_SUFFIX, expected)
  val actualFile = writeReport(case.name, ACTUAL_SUFFIX, actual)
  val diffFile = writeReport(case.name, DIFF_SUFFIX, difference.image)
  val sizeNote = if (expected.width != actual.width || expected.height != actual.height) {
    "Size changed: golden is ${expected.width}x${expected.height}, " +
      "render is ${actual.width}x${actual.height}.\n"
  } else {
    ""
  }
  fail(
    "Golden mismatch for '${case.name}': ${difference.differingPixels} of " +
      "${difference.comparedPixels} pixels differ by more than $CHANNEL_TOLERANCE " +
      "(worst channel delta ${difference.maxChannelDelta}).\n" +
      sizeNote +
      "Difference bounds: x=[${difference.left}..${difference.right}] " +
      "y=[${difference.top}..${difference.bottom}] " +
      "(${difference.right - difference.left + 1}x${difference.bottom - difference.top + 1}).\n" +
      "  expected: ${expectedFile.absolutePath}\n" +
      "  actual:   ${actualFile.absolutePath}\n" +
      "  diff:     ${diffFile.absolutePath} (magenta marks the differing pixels)\n" +
      RE_RECORD_HINT,
  )
}

// --------------------------------------------------------------------------------- rendering

/**
 * Composes [case] into a fixed-size, fixed-density offscreen scene and snapshots it.
 *
 * `runSkikoComposeUiTest` rather than `runComposeUiTest`: on Skiko the latter is a thin
 * delegate to the former with the scene left at its 1024x768 default, and only this overload
 * takes `size` and `density`. Pinning both is what makes a golden reproducible - a golden
 * recorded at the host machine's display density would fail on every other machine.
 *
 * Density is fixed at 1, so one dp is one pixel and a case's dp dimensions read directly as
 * pixel coordinates in the golden.
 *
 * The scene is painted opaque ([SCENE_BACKGROUND]). The test surface is cleared to
 * transparent, and premultiplied-alpha pixels do not survive a PNG round trip byte for byte,
 * so a transparent backdrop would make goldens differ from themselves.
 */
@OptIn(ExperimentalTestApi::class)
private fun render(case: GoldenCase): BufferedImage {
  lateinit var captured: ImageBitmap
  runSkikoComposeUiTest(
    size = Size(case.windowSize.width.toFloat(), case.windowSize.height.toFloat()),
    density = Density(GOLDEN_DENSITY),
  ) {
    lateinit var state: BalloonState
    setContent {
      // Provided above the host so the host, the anchor and the popup all agree. RTL cases
      // depend on this reaching the popup's position provider, which reads the direction
      // that was in scope where the `Popup` was emitted - that is, inside `BalloonHost`.
      CompositionLocalProvider(LocalLayoutDirection provides case.layoutDirection) {
        BalloonHost(modifier = Modifier.fillMaxSize().background(SCENE_BACKGROUND)) {
          state = rememberBalloonState(case.style)
          Box(modifier = Modifier.fillMaxSize()) {
            Box(
              modifier = Modifier
                .align(case.anchorAlignment)
                .size(case.anchorSize)
                .background(ANCHOR_COLOR)
                .balloon(state) { case.content() },
            )
          }
        }
      }
    }
    runOnUiThread { state.show(case) }
    waitForIdle()
    // The no-argument `SkikoComposeUiTest.captureToImage` snapshots the whole surface. The
    // node-scoped overload would crop to a semantics node's bounds instead, which is not
    // what a full-scene golden wants.
    captured = captureToImage()
  }
  return captured.toArgbImage()
}

/**
 * Shows the balloon the way [case] asks for.
 *
 * `centerAlign` wins when set, matching `BalloonState`: `showAtCenter` overwrites `align` with
 * `CENTER` anyway, so a case that sets both is asking for the center-align family.
 *
 * Everything else goes through `show(align)`, which is exactly what `showAlignTop` and the
 * rest delegate to. Routing through it keeps the mapping total - a new `BalloonAlign` entry
 * cannot fall through a `when` here and silently render the wrong side.
 */
private fun BalloonState.show(case: GoldenCase) {
  val centerAlign = case.centerAlign
  val offset = case.offset
  if (centerAlign != null) {
    showAtCenter(centerAlign, xOffset = offset.x, yOffset = offset.y)
  } else {
    show(case.align, xOffset = offset.x, yOffset = offset.y)
  }
}

/**
 * Normalises a capture to [BufferedImage.TYPE_INT_ARGB].
 *
 * The concrete type `toAwtImage` hands back is an implementation detail, and a PNG read back
 * from disk comes back as yet another type. Pinning both sides means `getRGB` is comparing
 * like for like and never silently un-premultiplies one side only.
 */
private fun ImageBitmap.toArgbImage(): BufferedImage {
  val source = toAwtImage()
  val normalised = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_ARGB)
  val graphics = normalised.createGraphics()
  try {
    graphics.drawImage(source, 0, 0, null)
  } finally {
    graphics.dispose()
  }
  return normalised
}

// -------------------------------------------------------------------------------- comparison

/** A failed comparison: how much differs, where, and a picture of it. */
private class Difference(
  val differingPixels: Int,
  val comparedPixels: Int,
  val maxChannelDelta: Int,
  val left: Int,
  val top: Int,
  val right: Int,
  val bottom: Int,
  val image: BufferedImage,
)

/**
 * Compares two renders, returning `null` when they match.
 *
 * The comparison walks the union of both sizes so a size change produces a diff image that
 * shows the change instead of an error with nothing to look at.
 *
 * [CHANNEL_TOLERANCE] absorbs anti-aliasing jitter without absorbing a real change: a shifted
 * edge repaints pixels from background to balloon, which is a delta of well over a hundred,
 * while a differently rounded AA coverage moves a channel by one or two.
 */
private fun difference(expected: BufferedImage, actual: BufferedImage): Difference? {
  val width = max(expected.width, actual.width)
  val height = max(expected.height, actual.height)
  val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

  var differingPixels = 0
  var maxChannelDelta = 0
  var left = Int.MAX_VALUE
  var top = Int.MAX_VALUE
  var right = Int.MIN_VALUE
  var bottom = Int.MIN_VALUE

  for (y in 0 until height) {
    for (x in 0 until width) {
      val inExpected = x < expected.width && y < expected.height
      val inActual = x < actual.width && y < actual.height
      val expectedPixel = if (inExpected) expected.getRGB(x, y) else TRANSPARENT
      val actualPixel = if (inActual) actual.getRGB(x, y) else TRANSPARENT
      // A pixel only one side has is a total difference: neither image has an opinion to
      // compare, and the size change itself is the regression.
      val delta = if (inExpected && inActual) {
        channelDelta(expectedPixel, actualPixel)
      } else {
        MAX_CHANNEL
      }

      if (delta > CHANNEL_TOLERANCE) {
        differingPixels++
        maxChannelDelta = max(maxChannelDelta, delta)
        left = min(left, x)
        top = min(top, y)
        right = max(right, x)
        bottom = max(bottom, y)
        image.setRGB(x, y, DIFF_MARK)
      } else {
        image.setRGB(x, y, washedOut(expectedPixel))
      }
    }
  }

  if (differingPixels == 0) return null
  return Difference(
    differingPixels = differingPixels,
    comparedPixels = width * height,
    maxChannelDelta = maxChannelDelta,
    left = left,
    top = top,
    right = right,
    bottom = bottom,
    image = image,
  )
}

/** The largest single-channel distance between two ARGB pixels, alpha included. */
private fun channelDelta(first: Int, second: Int): Int {
  var delta = 0
  var shift = 0
  while (shift <= 24) {
    val channel = abs(((first shr shift) and MAX_CHANNEL) - ((second shr shift) and MAX_CHANNEL))
    delta = max(delta, channel)
    shift += 8
  }
  return delta
}

/**
 * Fades a pixel toward white so the magenta diff marks are the only thing that draws the eye,
 * while the balloon is still recognisable enough to say *where* the change is.
 */
private fun washedOut(argb: Int): Int {
  val red = (argb shr 16) and MAX_CHANNEL
  val green = (argb shr 8) and MAX_CHANNEL
  val blue = argb and MAX_CHANNEL
  // Rec. 601 luma, integer-only: the exact weights do not matter for a diff backdrop.
  val luma = (red * 77 + green * 151 + blue * 28) shr 8
  val faded = MAX_CHANNEL - (MAX_CHANNEL - luma) / FADE_DIVISOR
  return OPAQUE_ALPHA or (faded shl 16) or (faded shl 8) or faded
}

// ------------------------------------------------------------------------------------- files

/**
 * Where the goldens live, resolved against the SOURCE tree rather than the classpath.
 *
 * The classpath copy under `build/` is a build output: writing there would record goldens
 * that vanish on the next `clean` and are never committed. `balloon.goldenDir` is set by the
 * `desktopTest` task (see `balloon/build.gradle.kts`) and is the path that is actually used.
 */
private val goldenDir: File by lazy {
  System.getProperty(GOLDEN_DIR_PROPERTY)?.let(::File) ?: File(moduleDir, GOLDEN_RELATIVE_PATH)
}

/** Where failure artefacts are written. A build output, so it lives under `build/`. */
private val reportDir: File by lazy {
  System.getProperty(REPORT_DIR_PROPERTY)?.let(::File) ?: File(moduleDir, REPORT_RELATIVE_PATH)
}

/**
 * Fallback location of the `balloon` module, for runs that bypass the Gradle task (an IDE
 * gutter run, say) and therefore set neither system property.
 *
 * Walks up from the working directory looking for the directory this file itself lives in.
 * That marker exists before any golden has been recorded, which the golden directory does
 * not, so a first-ever recording still resolves. Both the module directory and the repository
 * root are checked at each level, since those are the two working directories in practice.
 */
private val moduleDir: File by lazy {
  var candidate: File? = File(System.getProperty("user.dir")).absoluteFile
  while (candidate != null) {
    if (File(candidate, HARNESS_MARKER).isDirectory) return@lazy candidate
    val nested = File(candidate, MODULE_NAME)
    if (File(nested, HARNESS_MARKER).isDirectory) return@lazy nested
    candidate = candidate.parentFile
  }
  error(
    "Cannot locate the 'balloon' module from ${System.getProperty("user.dir")}. Run the " +
      "goldens through Gradle, which sets '$GOLDEN_DIR_PROPERTY' explicitly.",
  )
}

/** Writes one failure artefact and returns it, so the failure message can name the path. */
private fun writeReport(name: String, suffix: String, image: BufferedImage): File {
  reportDir.mkdirs()
  val file = File(reportDir, "$name$suffix.png")
  ImageIO.write(image, PNG_FORMAT, file)
  return file
}

/**
 * Whether this run re-records instead of comparing.
 *
 * `-Pballoon.updateGolden` carries no value, so mere presence means on; `=false` and `=0` are
 * honoured so a `gradle.properties` entry can be switched off without being deleted.
 */
private val isUpdateMode: Boolean
  get() = when (System.getProperty(UPDATE_PROPERTY)?.lowercase()) {
    null -> false
    "false", "0", "no" -> false
    else -> true
  }

// --------------------------------------------------------------------------------- constants

/** Set by the `desktopTest` task to the absolute path of `src/desktopTest/resources/golden`. */
private const val GOLDEN_DIR_PROPERTY = "balloon.goldenDir"

/** Set by the `desktopTest` task to the absolute path of `build/reports/golden`. */
private const val REPORT_DIR_PROPERTY = "balloon.goldenReportDir"

/** Presence switches the harness from comparing to re-recording. */
private const val UPDATE_PROPERTY = "balloon.updateGolden"

private const val MODULE_NAME = "balloon"
private const val HARNESS_MARKER = "src/desktopTest/kotlin/com/skydoves/balloon/golden"
private const val GOLDEN_RELATIVE_PATH = "src/desktopTest/resources/golden"
private const val REPORT_RELATIVE_PATH = "build/reports/golden"

private const val PNG_FORMAT = "png"
private const val EXPECTED_SUFFIX = "-expected"
private const val ACTUAL_SUFFIX = "-actual"
private const val DIFF_SUFFIX = "-diff"

private const val RE_RECORD_HINT =
  "Re-record with: ./gradlew :balloon:desktopTest -Pballoon.updateGolden"

/** One dp is one pixel, so a case's dp dimensions are the golden's pixel coordinates. */
private const val GOLDEN_DENSITY = 1f

/**
 * Per-channel slack, in 0..255. Large enough for anti-aliasing rounding, far too small for a
 * moved edge or a changed colour.
 */
private const val CHANNEL_TOLERANCE = 4

private const val MAX_CHANNEL = 0xFF
private const val OPAQUE_ALPHA = 0xFF shl 24
private const val TRANSPARENT = 0
private const val DIFF_MARK = 0xFFFF00FF.toInt()

/** How far `washedOut` pulls a pixel toward white: keep a quarter of its contrast. */
private const val FADE_DIVISOR = 4

/** Opaque, and not a colour the library draws by default, so the scene backdrop is obvious. */
private val SCENE_BACKGROUND = Color.White

/** Distinct from the backdrop, the default black balloon and the default yellow body. */
private val ANCHOR_COLOR = Color(0xFF90A4AE)
