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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.balloon
import com.skydoves.balloon.showAlignTop
import com.skydoves.balloondemo.databinding.ActivityMainBinding
import com.skydoves.balloondemo.factory.ViewHolderBalloonFactory
import com.skydoves.balloondemo.recycler.ItemUtils
import com.skydoves.balloondemo.recycler.SampleAdapter
import com.skydoves.balloondemo.recycler.SampleItem

class MainActivity :
  AppCompatActivity(),
  SampleAdapter.SampleViewHolder.Delegate,
  OnBalloonClickListener {

  private val adapter by lazy { SampleAdapter(this) }
  private val profileBalloon by lazy { BalloonUtils.getEditBalloon(this, this) }
  private val navigationBalloon by lazy { BalloonUtils.getNavigationBalloon(this, this, this) }
  private val viewHolderBalloon by balloon<ViewHolderBalloonFactory>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    with(binding) {
      tabLayout.addTab(tabLayout.newTab().setText("Timeline"))
      tabLayout.addTab(tabLayout.newTab().setText("Contents"))

      recyclerView.adapter = adapter
      adapter.addItems(ItemUtils.getSamples(this@MainActivity))

      button.showAlignTop(profileBalloon)
      button.setOnClickListener {
        if (profileBalloon.isShowing) {
          profileBalloon.dismiss()
        } else {
          profileBalloon.showAlignTop(it)
        }
      }

      bottomNavigationView.setOnNavigationItemSelectedListener {
        if (navigationBalloon.isShowing) {
          navigationBalloon.dismiss()
        } else {
          navigationBalloon.showAlignTop(bottomNavigationView)
        }
        true
      }
    }
  }

  override fun onBalloonClick(view: View) {
    navigationBalloon.dismiss()
    Toast.makeText(applicationContext, "dismissed", Toast.LENGTH_SHORT).show()
  }

  override fun onItemClick(sampleItem: SampleItem, view: View) {
    this.viewHolderBalloon.showAlignBottom(view)
  }
}
