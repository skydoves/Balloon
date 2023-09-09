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

package com.skydoves.balloondemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.awaitAlignBottom
import com.skydoves.balloon.awaitAlignEnd
import com.skydoves.balloon.awaitAlignStart
import com.skydoves.balloon.awaitAlignTop
import com.skydoves.balloon.awaitAtCenter
import com.skydoves.balloon.awaitBalloons
import com.skydoves.balloon.showAlignBottom
import com.skydoves.balloon.showAlignEnd
import com.skydoves.balloon.showAlignStart
import com.skydoves.balloon.showAlignTop
import com.skydoves.balloon.showAtCenter
import com.skydoves.balloondemo.databinding.ActivityAlignmentBinding
import kotlinx.coroutines.launch

class AlignmentActivity : AppCompatActivity() {
  private lateinit var binding: ActivityAlignmentBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityAlignmentBinding.inflate(layoutInflater)
    setContentView(binding.root)

    showBalloons()
  }

  private fun showBalloons() {
    binding.textView.showAlignTop(balloon("Align Top"))
    binding.textView.showAlignStart(balloon("Align Start"))
    binding.textView.showAlignEnd(balloon("Align End"))
    binding.textView.showAlignBottom(balloon("Align Bottom"))

    binding.textView.showAtCenter(balloon("Center Top"), centerAlign = BalloonCenterAlign.TOP)
    binding.textView.showAtCenter(balloon("Center Start"), centerAlign = BalloonCenterAlign.START)
    binding.textView.showAtCenter(balloon("Center End"), centerAlign = BalloonCenterAlign.END)
    binding.textView.showAtCenter(balloon("Center Bottom"), centerAlign = BalloonCenterAlign.BOTTOM)
  }

  private fun awaitBalloonsSequential() {
    lifecycleScope.launch {
      binding.textView.awaitAlignTop(balloon("Align Top"))
      binding.textView.awaitAlignStart(balloon("Align Start"))
      binding.textView.awaitAlignEnd(balloon("Align End"))
      binding.textView.awaitAlignBottom(balloon("Align Bottom"))

      binding.textView.awaitAtCenter(balloon("Center Top"), centerAlign = BalloonCenterAlign.TOP)
      binding.textView.awaitAtCenter(
        balloon("Center Start"),
        centerAlign = BalloonCenterAlign.START,
      )
      binding.textView.awaitAtCenter(balloon("Center End"), centerAlign = BalloonCenterAlign.END)
      binding.textView.awaitAtCenter(
        balloon("Center Bottom"),
        centerAlign = BalloonCenterAlign.BOTTOM,
      )
    }
  }

  private fun awaitBalloonsParallel() {
    lifecycleScope.launch {
      awaitBalloons {
        binding.textView.alignTop(balloon("Align Top"))
        binding.textView.alignStart(balloon("Align Start"))
        binding.textView.alignEnd(balloon("Align End"))
        binding.textView.alignBottom(balloon("Align Bottom"))
      }

      awaitBalloons {
        dismissSequentially = true

        binding.textView.atCenter(balloon("Center Top"), centerAlign = BalloonCenterAlign.TOP)
        binding.textView.atCenter(
          balloon("Center Start"),
          centerAlign = BalloonCenterAlign.START,
        )
        binding.textView.atCenter(balloon("Center End"), centerAlign = BalloonCenterAlign.END)
        binding.textView.atCenter(
          balloon("Center Bottom"),
          centerAlign = BalloonCenterAlign.BOTTOM,
        )
      }
    }
  }

  private fun relayBalloons() {
    val tooltip = balloon("Align Top")
    tooltip.relayShowAlignStart(balloon("Align Start"), binding.textView)
      .relayShowAlignEnd(balloon("Align End"), binding.textView)
      .relayShowAlignBottom(balloon("Align Bottom"), binding.textView)
      .relayShowAtCenter(
        balloon("Center Top"),
        binding.textView,
        centerAlign = BalloonCenterAlign.TOP,
      )
      .relayShowAtCenter(
        balloon("Center Start"),
        binding.textView,
        centerAlign = BalloonCenterAlign.START,
      )
      .relayShowAtCenter(
        balloon("Center End"),
        binding.textView,
        centerAlign = BalloonCenterAlign.END,
      )
      .relayShowAtCenter(
        balloon("Center Bottom"),
        binding.textView,
        centerAlign = BalloonCenterAlign.BOTTOM,
      )
    binding.textView.showAlignTop(tooltip)
  }

  private fun balloon(text: String) = Balloon.Builder(this)
    .setText(text)
    .setLifecycleOwner(this)
    .build()
}
