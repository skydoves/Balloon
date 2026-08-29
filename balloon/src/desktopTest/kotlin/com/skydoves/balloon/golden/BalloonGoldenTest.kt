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

import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonOverlayAnimation
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Renders every [GoldenCase] in [GOLDEN_CASES] and compares it against its stored golden.
 *
 * ## Why one test rather than one per case
 *
 * `kotlin.test` has no parameterised runner, and a test function per case would have to be
 * written out by hand for all of them - a list that goes stale the moment someone adds a case
 * and forgets the matching function. Driving the list directly is the only arrangement where
 * the matrix and the suite cannot drift apart.
 *
 * The cost of that choice is that a plain `for` loop would stop at the first mismatch, and a
 * one-line change to the shape builder fails hundreds of cases at once: seeing only the
 * alphabetically-first one tells you nothing about the blast radius. So every case is
 * attempted, its failure recorded, and the whole set reported together at the end - by name,
 * because the name is also the golden's filename and therefore the thing you need in order to
 * go and look at the image.
 */
class BalloonGoldenTest {

  @Test
  fun everyCaseNameIsUniqueAndFileSafe() {
    val duplicates = GOLDEN_CASES
      .groupingBy { it.name }
      .eachCount()
      .filterValues { it > 1 }
      .keys
    assertTrue(
      duplicates.isEmpty(),
      "golden case names are golden filenames and must be unique, but these repeat: " +
        duplicates.joinToString(),
    )

    val malformed = GOLDEN_CASES.map { it.name }.filterNot(NAME_PATTERN::matches)
    assertTrue(
      malformed.isEmpty(),
      "golden case names must match ${NAME_PATTERN.pattern} so they are safe as filenames " +
        "on every platform, but these do not: " + malformed.joinToString(),
    )
  }

  /**
   * A frame captured while something is still moving is a golden nothing can reproduce.
   * Every case is expected to render a settled balloon, so the animation knobs are pinned
   * here rather than trusted to whoever adds the next case.
   */
  @Test
  fun everyCaseRendersASettledFrame() {
    val animated = GOLDEN_CASES.filter { case ->
      val style = case.style
      style.animation != BalloonAnimation.NONE ||
        style.highlightAnimation != BalloonHighlightAnimation.NONE ||
        style.autoDismissMillis != 0L ||
        (style.isVisibleOverlay && style.overlayAnimation != BalloonOverlayAnimation.NONE)
    }
    assertTrue(
      animated.isEmpty(),
      "golden cases must render a settled frame, but these animate: " +
        animated.joinToString { it.name },
    )
  }

  @Test
  fun everyCaseMatchesItsGolden() {
    val failures = LinkedHashMap<String, Throwable>()
    for (case in GOLDEN_CASES) {
      try {
        assertGolden(case)
      } catch (failure: Throwable) {
        // A dead VM is not a golden mismatch and must not be swallowed into the report.
        if (failure is VirtualMachineError) throw failure
        failures[case.name] = failure
      }
    }
    if (failures.isEmpty()) return

    val (firstName, firstFailure) = failures.entries.first().toPair()
    fail(
      buildString {
        append(failures.size)
        append(" of ")
        append(GOLDEN_CASES.size)
        appendLine(" golden cases failed:")
        failures.forEach { (name, failure) ->
          append("  - ")
          append(name)
          append(": ")
          appendLine(failure.summary())
        }
        appendLine()
        append("First failure in full ('")
        append(firstName)
        appendLine("'):")
        append(firstFailure.message ?: firstFailure.toString())
      },
    )
  }

  private companion object {
    /** Lowercase, digits and single hyphens: safe as a filename on every platform. */
    val NAME_PATTERN = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

    /** One line per failure, so a suite-wide regression stays readable. */
    fun Throwable.summary(): String =
      (message ?: this::class.simpleName ?: "failed")
        .lineSequence()
        .firstOrNull { it.isNotBlank() }
        ?.trim()
        ?: toString()
  }
}
