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
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.balloondemo.recycler.CustomAdapter
import com.skydoves.balloondemo.recycler.CustomItem
import com.skydoves.balloondemo.recycler.CustomViewHolder
import com.skydoves.balloondemo.recycler.ItemUtils
import com.skydoves.balloondemo.recycler.SampleAdapter
import com.skydoves.balloondemo.recycler.SampleItem
import com.skydoves.balloondemo.recycler.SampleViewHolder
import kotlinx.android.synthetic.main.activity_custom.*
import kotlinx.android.synthetic.main.toolbar_custom.*

class CustomActivity : AppCompatActivity(),
  SampleViewHolder.Delegate,
  CustomViewHolder.Delegate {

  private val adapter by lazy { SampleAdapter(this, this) }
  private val customAdapter by lazy { CustomAdapter(this) }
  private val customListBalloon by lazy { BalloonUtils.getCustomListBalloon(this, this) }
  private val customProfileBalloon by lazy { BalloonUtils.getCustomProfileBalloon(this, this) }
  private val customTagBalloon by lazy { BalloonUtils.getCustomTagBalloon(this, this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_custom)

    tabLayout.addTab(tabLayout.newTab().setText("Timeline"))
    tabLayout.addTab(tabLayout.newTab().setText("Contents"))

    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    adapter.addItems(ItemUtils.getSamples(this))

    val listRecycler: RecyclerView = customListBalloon.getContentView().findViewById(R.id.list_recyclerView)
    listRecycler.adapter = customAdapter
    listRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    customAdapter.addCustomItem(ItemUtils.getCustomSamples(this))

    toolbar_list.setOnClickListener {
      if (customListBalloon.isShowing) {
        customListBalloon.dismiss()
      } else {
        customListBalloon.showAlignBottom(it)
      }
    }

    circleImageView.setOnClickListener {
      if (customProfileBalloon.isShowing) {
        customProfileBalloon.dismiss()
      } else {
        customProfileBalloon.showAlignBottom(it)
      }
    }

    val buttonEdit: Button = customProfileBalloon.getContentView().findViewById(R.id.button_edit)
    buttonEdit.setOnClickListener {
      customProfileBalloon.dismiss()
      Toast.makeText(baseContext, "Edit", Toast.LENGTH_SHORT).show()
    }

    bottomNavigationView.setOnNavigationItemSelectedListener {
      if (customTagBalloon.isShowing) {
        customTagBalloon.dismiss()
      } else {
        customTagBalloon.showAlignTop(bottomNavigationView, 130, 0)
      }
      true
    }
  }

  override fun onCustomItemClick(customItem: CustomItem) {
    customListBalloon.dismiss()
    Toast.makeText(baseContext, customItem.title, Toast.LENGTH_SHORT).show()
  }

  override fun onItemClick(sampleItem: SampleItem) = Unit
}
