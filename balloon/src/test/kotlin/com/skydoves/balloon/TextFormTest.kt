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

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class TextFormTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun `textForm builder should create TextForm with default values`() {
    val textForm = TextForm.Builder(context).build()

    assertThat(textForm.text.toString()).isEmpty()
    assertThat(textForm.textSize).isEqualTo(12f)
    assertThat(textForm.textColor).isEqualTo(Color.WHITE)
    assertThat(textForm.textStyle).isEqualTo(Typeface.NORMAL)
    assertThat(textForm.textGravity).isEqualTo(Gravity.CENTER)
    assertThat(textForm.textIsHtml).isFalse()
    assertThat(textForm.enableAutoSized).isTrue()
    assertThat(textForm.includeFontPadding).isTrue()
  }

  @Test
  fun `textForm builder should set text correctly`() {
    val expectedText = "Hello Balloon"
    val textForm = TextForm.Builder(context)
      .setText(expectedText)
      .build()

    assertThat(textForm.text).isEqualTo(expectedText)
  }

  @Test
  fun `textForm builder should set text size correctly`() {
    val expectedSize = 16f
    val textForm = TextForm.Builder(context)
      .setTextSize(expectedSize)
      .build()

    assertThat(textForm.textSize).isEqualTo(expectedSize)
  }

  @Test
  fun `textForm builder should set text color correctly`() {
    val expectedColor = Color.RED
    val textForm = TextForm.Builder(context)
      .setTextColor(expectedColor)
      .build()

    assertThat(textForm.textColor).isEqualTo(expectedColor)
  }

  @Test
  fun `textForm builder should set text typeface correctly`() {
    val expectedTypeface = Typeface.BOLD
    val textForm = TextForm.Builder(context)
      .setTextTypeface(expectedTypeface)
      .build()

    assertThat(textForm.textStyle).isEqualTo(expectedTypeface)
  }

  @Test
  fun `textForm builder should set text gravity correctly`() {
    val expectedGravity = Gravity.START
    val textForm = TextForm.Builder(context)
      .setTextGravity(expectedGravity)
      .build()

    assertThat(textForm.textGravity).isEqualTo(expectedGravity)
  }

  @Test
  fun `textForm builder should set textIsHtml correctly`() {
    val textForm = TextForm.Builder(context)
      .setTextIsHtml(true)
      .build()

    assertThat(textForm.textIsHtml).isTrue()
  }

  @Test
  fun `textForm builder should set enableAutoSized correctly`() {
    val textForm = TextForm.Builder(context)
      .setEnableAutoSized(false)
      .build()

    assertThat(textForm.enableAutoSized).isFalse()
  }

  @Test
  fun `textForm builder should set auto size text bounds correctly`() {
    val minSize = 10f
    val maxSize = 20f
    val textForm = TextForm.Builder(context)
      .setMinAutoSizeTextSize(minSize)
      .setMaxAutoSizeTextSize(maxSize)
      .build()

    assertThat(textForm.minAutoSizeTextSize).isEqualTo(minSize)
    assertThat(textForm.maxAutoSizeTextSize).isEqualTo(maxSize)
  }

  @Test
  fun `textForm builder should set line spacing correctly`() {
    val lineSpacing = 1.5f
    val textForm = TextForm.Builder(context)
      .setTextLineSpacing(lineSpacing)
      .build()

    assertThat(textForm.textLineSpacing).isEqualTo(lineSpacing)
  }

  @Test
  fun `textForm builder should set letter spacing correctly`() {
    val letterSpacing = 0.1f
    val textForm = TextForm.Builder(context)
      .setTextLetterSpacing(letterSpacing)
      .build()

    assertThat(textForm.textLetterSpacing).isEqualTo(letterSpacing)
  }

  @Test
  fun `textForm builder should set includeFontPadding correctly`() {
    val textForm = TextForm.Builder(context)
      .setIncludeFontPadding(false)
      .build()

    assertThat(textForm.includeFontPadding).isFalse()
  }

  @Test
  fun `textForm DSL function should create TextForm correctly`() {
    val textForm = textForm(context) {
      text = "DSL Test"
      textSize = 14f
      textColor = Color.BLUE
    }

    assertThat(textForm.text).isEqualTo("DSL Test")
    assertThat(textForm.textSize).isEqualTo(14f)
    assertThat(textForm.textColor).isEqualTo(Color.BLUE)
  }
}
