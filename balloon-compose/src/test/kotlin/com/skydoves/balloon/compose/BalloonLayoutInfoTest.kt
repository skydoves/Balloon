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

package com.skydoves.balloon.compose

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BalloonLayoutInfoTest {

  @Test
  fun `BalloonLayoutInfo should store coordinates correctly`() {
    val layoutInfo = BalloonLayoutInfo(
      x = 10f,
      y = 20f,
      width = 100,
      height = 50,
    )

    assertThat(layoutInfo.x).isEqualTo(10f)
    assertThat(layoutInfo.y).isEqualTo(20f)
    assertThat(layoutInfo.width).isEqualTo(100)
    assertThat(layoutInfo.height).isEqualTo(50)
  }

  @Test
  fun `BalloonLayoutInfo should support data class equality`() {
    val layoutInfo1 = BalloonLayoutInfo(
      x = 10f,
      y = 20f,
      width = 100,
      height = 50,
    )

    val layoutInfo2 = BalloonLayoutInfo(
      x = 10f,
      y = 20f,
      width = 100,
      height = 50,
    )

    assertThat(layoutInfo1).isEqualTo(layoutInfo2)
  }

  @Test
  fun `BalloonLayoutInfo should support data class copy`() {
    val original = BalloonLayoutInfo(
      x = 10f,
      y = 20f,
      width = 100,
      height = 50,
    )

    val copied = original.copy(x = 30f)

    assertThat(copied.x).isEqualTo(30f)
    assertThat(copied.y).isEqualTo(20f)
    assertThat(copied.width).isEqualTo(100)
    assertThat(copied.height).isEqualTo(50)
  }

  @Test
  fun `BalloonLayoutInfo should handle zero values`() {
    val layoutInfo = BalloonLayoutInfo(
      x = 0f,
      y = 0f,
      width = 0,
      height = 0,
    )

    assertThat(layoutInfo.x).isEqualTo(0f)
    assertThat(layoutInfo.y).isEqualTo(0f)
    assertThat(layoutInfo.width).isEqualTo(0)
    assertThat(layoutInfo.height).isEqualTo(0)
  }

  @Test
  fun `BalloonLayoutInfo should handle negative coordinates`() {
    val layoutInfo = BalloonLayoutInfo(
      x = -10f,
      y = -20f,
      width = 100,
      height = 50,
    )

    assertThat(layoutInfo.x).isEqualTo(-10f)
    assertThat(layoutInfo.y).isEqualTo(-20f)
  }
}
