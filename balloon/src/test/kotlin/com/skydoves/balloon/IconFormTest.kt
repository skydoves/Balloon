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
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class IconFormTest {

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun `iconForm builder should create IconForm with default values`() {
    val iconForm = IconForm.Builder(context).build()

    assertThat(iconForm.drawable).isNull()
    assertThat(iconForm.drawableRes).isNull()
    assertThat(iconForm.iconGravity).isEqualTo(IconGravity.START)
    assertThat(iconForm.iconColor).isEqualTo(Color.WHITE)
    assertThat(iconForm.iconContentDescription.toString()).isEmpty()
  }

  @Test
  fun `iconForm builder should set drawable correctly`() {
    val drawable = ColorDrawable(Color.RED)
    val iconForm = IconForm.Builder(context)
      .setDrawable(drawable)
      .build()

    assertThat(iconForm.drawable).isEqualTo(drawable)
  }

  @Test
  fun `iconForm builder should set icon gravity correctly`() {
    val iconForm = IconForm.Builder(context)
      .setDrawableGravity(IconGravity.END)
      .build()

    assertThat(iconForm.iconGravity).isEqualTo(IconGravity.END)
  }

  @Test
  fun `iconForm builder should set icon width correctly`() {
    val expectedWidth = 100
    val iconForm = IconForm.Builder(context)
      .setIconWidth(expectedWidth)
      .build()

    assertThat(iconForm.iconWidth).isEqualTo(expectedWidth)
  }

  @Test
  fun `iconForm builder should set icon height correctly`() {
    val expectedHeight = 100
    val iconForm = IconForm.Builder(context)
      .setIconHeight(expectedHeight)
      .build()

    assertThat(iconForm.iconHeight).isEqualTo(expectedHeight)
  }

  @Test
  fun `iconForm builder should set icon size correctly`() {
    val expectedSize = 50
    val iconForm = IconForm.Builder(context)
      .setIconSize(expectedSize)
      .build()

    assertThat(iconForm.iconWidth).isEqualTo(expectedSize)
    assertThat(iconForm.iconHeight).isEqualTo(expectedSize)
  }

  @Test
  fun `iconForm builder should set icon space correctly`() {
    val expectedSpace = 16
    val iconForm = IconForm.Builder(context)
      .setIconSpace(expectedSpace)
      .build()

    assertThat(iconForm.iconSpace).isEqualTo(expectedSpace)
  }

  @Test
  fun `iconForm builder should set icon color correctly`() {
    val expectedColor = Color.BLUE
    val iconForm = IconForm.Builder(context)
      .setIconColor(expectedColor)
      .build()

    assertThat(iconForm.iconColor).isEqualTo(expectedColor)
  }

  @Test
  fun `iconForm builder should set icon content description correctly`() {
    val expectedDescription = "Test Icon"
    val iconForm = IconForm.Builder(context)
      .setIconContentDescription(expectedDescription)
      .build()

    assertThat(iconForm.iconContentDescription).isEqualTo(expectedDescription)
  }

  @Test
  fun `iconForm DSL function should create IconForm correctly`() {
    val drawable = ColorDrawable(Color.GREEN)
    val iconForm = iconForm(context) {
      this.drawable = drawable
      iconGravity = IconGravity.TOP
      iconColor = Color.YELLOW
      iconWidth = 64
      iconHeight = 64
    }

    assertThat(iconForm.drawable).isEqualTo(drawable)
    assertThat(iconForm.iconGravity).isEqualTo(IconGravity.TOP)
    assertThat(iconForm.iconColor).isEqualTo(Color.YELLOW)
    assertThat(iconForm.iconWidth).isEqualTo(64)
    assertThat(iconForm.iconHeight).isEqualTo(64)
  }

  @Test
  fun `iconGravity enum should have correct values`() {
    assertThat(IconGravity.values()).hasLength(4)
    assertThat(IconGravity.START).isNotNull()
    assertThat(IconGravity.END).isNotNull()
    assertThat(IconGravity.TOP).isNotNull()
    assertThat(IconGravity.BOTTOM).isNotNull()
  }
}
