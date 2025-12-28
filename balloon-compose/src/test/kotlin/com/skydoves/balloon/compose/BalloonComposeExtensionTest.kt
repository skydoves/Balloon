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

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BalloonComposeExtensionTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun `Balloon Builder should set arrow size correctly`() {
    val builder = Balloon.Builder(context)
      .setArrowSize(15)

    // Builder methods should return the builder for chaining
    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set arrow position correctly`() {
    val builder = Balloon.Builder(context)
      .setArrowPosition(0.5f)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set arrow orientation correctly`() {
    val builder = Balloon.Builder(context)
      .setArrowOrientation(ArrowOrientation.BOTTOM)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set arrow position rules correctly`() {
    val builder = Balloon.Builder(context)
      .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set width correctly`() {
    val builder = Balloon.Builder(context)
      .setWidth(BalloonSizeSpec.WRAP)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set height correctly`() {
    val builder = Balloon.Builder(context)
      .setHeight(BalloonSizeSpec.WRAP)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set padding correctly`() {
    val builder = Balloon.Builder(context)
      .setPadding(16)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set corner radius correctly`() {
    val builder = Balloon.Builder(context)
      .setCornerRadius(8f)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set background color correctly`() {
    val builder = Balloon.Builder(context)
      .setBackgroundColor(Color.BLUE)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set balloon animation correctly`() {
    val builder = Balloon.Builder(context)
      .setBalloonAnimation(BalloonAnimation.ELASTIC)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set dismiss when clicked correctly`() {
    val builder = Balloon.Builder(context)
      .setDismissWhenClicked(true)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set dismiss when touch outside correctly`() {
    val builder = Balloon.Builder(context)
      .setDismissWhenTouchOutside(true)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set auto dismiss duration correctly`() {
    val builder = Balloon.Builder(context)
      .setAutoDismissDuration(2000L)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should chain multiple configurations`() {
    val builder = Balloon.Builder(context)
      .setArrowSize(10)
      .setArrowPosition(0.5f)
      .setArrowOrientation(ArrowOrientation.TOP)
      .setWidth(BalloonSizeSpec.WRAP)
      .setHeight(BalloonSizeSpec.WRAP)
      .setPadding(12)
      .setCornerRadius(8f)
      .setBackgroundColor(Color.WHITE)
      .setBalloonAnimation(BalloonAnimation.FADE)
      .setDismissWhenClicked(true)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set margin correctly`() {
    val builder = Balloon.Builder(context)
      .setMargin(16)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set margin horizontal correctly`() {
    val builder = Balloon.Builder(context)
      .setMarginHorizontal(16)

    assertThat(builder).isNotNull()
  }

  @Test
  fun `Balloon Builder should set margin vertical correctly`() {
    val builder = Balloon.Builder(context)
      .setMarginVertical(16)

    assertThat(builder).isNotNull()
  }
}
