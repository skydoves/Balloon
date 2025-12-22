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

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BalloonStrokeTest {

  @Test
  fun `BalloonStroke should create with color and default thickness`() {
    val stroke = BalloonStroke(Color.RED)

    assertThat(stroke.color).isEqualTo(Color.RED)
    assertThat(stroke.thickness).isEqualTo(1f)
  }

  @Test
  fun `BalloonStroke should create with color and custom thickness`() {
    val stroke = BalloonStroke(Color.BLUE, 2.5f)

    assertThat(stroke.color).isEqualTo(Color.BLUE)
    assertThat(stroke.thickness).isEqualTo(2.5f)
  }

  @Test
  fun `BalloonStroke should support data class equality`() {
    val stroke1 = BalloonStroke(Color.GREEN, 1.5f)
    val stroke2 = BalloonStroke(Color.GREEN, 1.5f)

    assertThat(stroke1).isEqualTo(stroke2)
  }

  @Test
  fun `BalloonStroke should have correct STROKE_THICKNESS_MULTIPLIER`() {
    assertThat(BalloonStroke.STROKE_THICKNESS_MULTIPLIER).isEqualTo(1.5f)
  }

  @Test
  fun `BalloonStroke should support data class copy`() {
    val original = BalloonStroke(Color.BLACK, 2f)
    val copied = original.copy(color = Color.WHITE)

    assertThat(copied.color).isEqualTo(Color.WHITE)
    assertThat(copied.thickness).isEqualTo(2f)
  }

  @Test
  fun `BalloonStroke should handle zero thickness`() {
    val stroke = BalloonStroke(Color.RED, 0f)

    assertThat(stroke.thickness).isEqualTo(0f)
  }
}
