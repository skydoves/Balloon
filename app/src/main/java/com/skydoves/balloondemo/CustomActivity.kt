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
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.balloon.balloon
import com.skydoves.balloondemo.factory.CustomListBalloonFactory
import com.skydoves.balloondemo.factory.ProfileBalloonFactory
import com.skydoves.balloondemo.factory.TagBalloonFactory
import com.skydoves.balloondemo.factory.ViewHolderBalloonFactory
import com.skydoves.balloondemo.recycler.CustomAdapter
import com.skydoves.balloondemo.recycler.CustomItem
import com.skydoves.balloondemo.recycler.ItemUtils
import com.skydoves.balloondemo.recycler.SampleAdapter
import com.skydoves.balloondemo.recycler.SampleItem
import kotlinx.android.synthetic.main.activity_custom.bottomNavigationView
import kotlinx.android.synthetic.main.activity_custom.circleImageView
import kotlinx.android.synthetic.main.activity_custom.recyclerView
import kotlinx.android.synthetic.main.activity_custom.tabLayout
import kotlinx.android.synthetic.main.toolbar_custom.toolbar_list

class CustomActivity :
  AppCompatActivity(),
  SampleAdapter.SampleViewHolder.Delegate,
  CustomAdapter.CustomViewHolder.Delegate {

  private val adapter by lazy { SampleAdapter(this) }
  private val customAdapter by lazy { CustomAdapter(this) }
  private val customListBalloon by balloon(CustomListBalloonFactory::class)
  private val customProfileBalloon by balloon(ProfileBalloonFactory::class)
  private val viewHolderBalloon by balloon(ViewHolderBalloonFactory::class)
  private val customTagBalloon by balloon(TagBalloonFactory::class)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_custom)

    tabLayout.addTab(tabLayout.newTab().setText("Timeline"))
    tabLayout.addTab(tabLayout.newTab().setText("Contents"))

    recyclerView.adapter = adapter
    adapter.addItems(ItemUtils.getSamples(this))

    // gets customListBalloon's recyclerView.
    val listRecycler: RecyclerView =
      customListBalloon.getContentView().findViewById(R.id.list_recyclerView)
    listRecycler.adapter = customAdapter
    this.customAdapter.addCustomItem(ItemUtils.getCustomSamples(this))

    toolbar_list.setOnClickListener {
      this.customListBalloon.showAlignBottom(it)
    }

    circleImageView.setOnClickListener {
      this.customProfileBalloon.showAlignBottom(it)
    }

    val buttonEdit: Button = customProfileBalloon.getContentView().findViewById(R.id.button_edit)
    buttonEdit.setOnClickListener {
      this.customProfileBalloon.dismiss()
      Toast.makeText(applicationContext, "Edit", Toast.LENGTH_SHORT).show()
    }

    bottomNavigationView.setOnNavigationItemSelectedListener {
      this.customTagBalloon.showAlignTop(bottomNavigationView, 130, 0)
      true
    }
  }

  override fun onCustomItemClick(customItem: CustomItem) {
    this.customListBalloon.dismiss()
    Toast.makeText(applicationContext, customItem.title, Toast.LENGTH_SHORT).show()
  }

  override fun onItemClick(sampleItem: SampleItem, view: View) {
    this.viewHolderBalloon.showAlignBottom(view)
  }
}
