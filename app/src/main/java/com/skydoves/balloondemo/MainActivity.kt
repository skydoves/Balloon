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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.balloon
import com.skydoves.balloon.showAlignTop
import com.skydoves.balloondemo.factory.ViewHolderBalloonFactory
import com.skydoves.balloondemo.recycler.ItemUtils
import com.skydoves.balloondemo.recycler.SampleAdapter
import com.skydoves.balloondemo.recycler.SampleItem
import com.skydoves.balloondemo.recycler.SampleViewHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SampleViewHolder.Delegate, OnBalloonClickListener {

  private val adapter by lazy { SampleAdapter(this) }
  private val profileBalloon by lazy { BalloonUtils.getProfileBalloon(this, this) }
  private val navigationBalloon by lazy { BalloonUtils.getNavigationBalloon(this, this, this) }
  private val viewHolderBalloon by balloon(ViewHolderBalloonFactory::class)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    tabLayout.addTab(tabLayout.newTab().setText("Timeline"))
    tabLayout.addTab(tabLayout.newTab().setText("Contents"))

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    adapter.addItems(ItemUtils.getSamples(this))

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

  override fun onBalloonClick(view: View) {
    navigationBalloon.dismiss()
    Toast.makeText(baseContext, "dismissed", Toast.LENGTH_SHORT).show()
  }

  override fun onItemClick(sampleItem: SampleItem, view: View) {
    this.viewHolderBalloon.showAlignBottom(view)
  }
}
