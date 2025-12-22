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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.skydoves.balloon.Balloon
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21])
class BalloonStateTest {

  private lateinit var context: Context
  private lateinit var lifecycleOwner: LifecycleOwner
  private lateinit var builder: Balloon.Builder

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    lifecycleOwner = mockk(relaxed = true) {
      every { lifecycle } returns LifecycleRegistry(this@mockk).apply {
        currentState = Lifecycle.State.RESUMED
      }
    }
    builder = Balloon.Builder(context)
  }

  @Test
  fun `BalloonState should not be attached initially`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.isAttached).isFalse()
  }

  @Test
  fun `BalloonState isShowing should return false when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.isShowing).isFalse()
  }

  @Test
  fun `BalloonState shouldShowUp should return false when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.shouldShowUp()).isFalse()
  }

  @Test
  fun `BalloonState getMeasuredWidth should return 0 when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.getMeasuredWidth()).isEqualTo(0)
  }

  @Test
  fun `BalloonState getMeasuredHeight should return 0 when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.getMeasuredHeight()).isEqualTo(0)
  }

  @Test
  fun `BalloonState dismissWithDelay should return false when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.dismissWithDelay(1000L)).isFalse()
  }

  @Test
  fun `BalloonState dispose should clear internal references`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    state.dispose()

    assertThat(state._anchorView).isNull()
    assertThat(state._balloonWindow).isNull()
    assertThat(state.isAttached).isFalse()
  }

  @Test
  fun `BalloonState should hold builder reference`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    assertThat(state.builder).isEqualTo(builder)
  }

  @Test
  fun `BalloonState dismiss should not throw when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    // Should not throw
    state.dismiss()
  }

  @Test
  fun `BalloonState show methods should not throw when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    // None of these should throw when not attached
    state.showAlignTop()
    state.showAlignBottom()
    state.showAlignStart()
    state.showAlignEnd()
    state.showAsDropDown()
  }

  @Test
  fun `BalloonState update methods should not throw when not attached`() {
    val state = BalloonState(context, builder, lifecycleOwner)

    // None of these should throw when not attached
    state.updateAlignTop()
    state.updateAlignBottom()
    state.updateAlignStart()
    state.updateAlignEnd()
    state.update()
  }
}
