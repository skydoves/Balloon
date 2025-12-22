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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BalloonEnumsTest {

  @Test
  fun `ArrowOrientation should have all expected values`() {
    val values = ArrowOrientation.values()

    assertThat(values).hasLength(4)
    assertThat(values).asList().containsExactly(
      ArrowOrientation.TOP,
      ArrowOrientation.BOTTOM,
      ArrowOrientation.START,
      ArrowOrientation.END,
    )
  }

  @Test
  fun `ArrowPositionRules should have all expected values`() {
    val values = ArrowPositionRules.values()

    assertThat(values).hasLength(2)
    assertThat(values).asList().containsExactly(
      ArrowPositionRules.ALIGN_BALLOON,
      ArrowPositionRules.ALIGN_ANCHOR,
    )
  }

  @Test
  fun `ArrowOrientationRules should have all expected values`() {
    val values = ArrowOrientationRules.values()

    assertThat(values).hasLength(2)
    assertThat(values).asList().containsExactly(
      ArrowOrientationRules.ALIGN_ANCHOR,
      ArrowOrientationRules.ALIGN_FIXED,
    )
  }

  @Test
  fun `BalloonAlign should have all expected values`() {
    val values = BalloonAlign.values()

    assertThat(values).hasLength(4)
    assertThat(values).asList().containsExactly(
      BalloonAlign.TOP,
      BalloonAlign.BOTTOM,
      BalloonAlign.START,
      BalloonAlign.END,
    )
  }

  @Test
  fun `BalloonCenterAlign should have all expected values`() {
    val values = BalloonCenterAlign.values()

    assertThat(values).hasLength(4)
    assertThat(values).asList().containsExactly(
      BalloonCenterAlign.TOP,
      BalloonCenterAlign.BOTTOM,
      BalloonCenterAlign.START,
      BalloonCenterAlign.END,
    )
  }

  @Test
  fun `BalloonAnimation should have all expected values`() {
    val values = BalloonAnimation.values()

    assertThat(values).hasLength(5)
    assertThat(values).asList().containsExactly(
      BalloonAnimation.NONE,
      BalloonAnimation.ELASTIC,
      BalloonAnimation.FADE,
      BalloonAnimation.CIRCULAR,
      BalloonAnimation.OVERSHOOT,
    )
  }

  @Test
  fun `BalloonHighlightAnimation should have all expected values`() {
    val values = BalloonHighlightAnimation.values()

    assertThat(values).hasLength(5)
    assertThat(values).asList().containsExactly(
      BalloonHighlightAnimation.NONE,
      BalloonHighlightAnimation.HEARTBEAT,
      BalloonHighlightAnimation.SHAKE,
      BalloonHighlightAnimation.BREATH,
      BalloonHighlightAnimation.ROTATE,
    )
  }

  @Test
  fun `IconGravity should have all expected values`() {
    val values = IconGravity.values()

    assertThat(values).hasLength(4)
    assertThat(values).asList().containsExactly(
      IconGravity.START,
      IconGravity.END,
      IconGravity.TOP,
      IconGravity.BOTTOM,
    )
  }
}
