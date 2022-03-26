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
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.balloon
import com.skydoves.balloondemo.databinding.ActivityCustomBinding
import com.skydoves.balloondemo.factory.CustomListBalloonFactory
import com.skydoves.balloondemo.factory.EditBalloonFactory
import com.skydoves.balloondemo.factory.ProfileBalloonFactory
import com.skydoves.balloondemo.factory.TagBalloonFactory
import com.skydoves.balloondemo.recycler.CustomAdapter
import com.skydoves.balloondemo.recycler.CustomItem
import com.skydoves.balloondemo.recycler.ItemUtils
import com.skydoves.balloondemo.recycler.SampleAdapter

class CustomActivity : AppCompatActivity(), CustomAdapter.CustomViewHolder.Delegate {

  private val adapter by lazy { SampleAdapter() }
  private val customAdapter by lazy { CustomAdapter(this) }
  private val editBalloon by balloon<EditBalloonFactory>()
  private val customListBalloon by balloon<CustomListBalloonFactory>()
  private val customProfileBalloon by balloon<ProfileBalloonFactory>()
  private val customTagBalloon by balloon<TagBalloonFactory>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityCustomBinding.inflate(layoutInflater)
    setContentView(binding.root)

    with(binding) {
      tabLayout.addTab(tabLayout.newTab().setText("Timeline"))
      tabLayout.addTab(tabLayout.newTab().setText("Contents"))

      recyclerView.adapter = adapter
      adapter.addItems(ItemUtils.getSamples(this@CustomActivity))

      // gets customListBalloon's recyclerView.
      val listRecycler: RecyclerView =
        customListBalloon.getContentView().findViewById(R.id.list_recyclerView)
      listRecycler.adapter = customAdapter
      customAdapter.addCustomItem(ItemUtils.getCustomSamples(this@CustomActivity))

      toolbar.toolbarList.setOnClickListener {
        customListBalloon.showAlignBottom(it, 0, 36)
      }

      edit.setOnClickListener {
        editBalloon.showAlign(
          align = BalloonAlign.BOTTOM,
          mainAnchor = circleImageView,
          subAnchorList = listOf(it),
        )
      }

      circleImageView.setOnClickListener {
        customProfileBalloon.showAlignBottom(it)
      }

      val buttonEdit: Button = customProfileBalloon.getContentView().findViewById(R.id.button_edit)
      buttonEdit.setOnClickListener {
        customProfileBalloon.dismiss()
        Toast.makeText(applicationContext, "Edit", Toast.LENGTH_SHORT).show()
      }

      bottomNavigationView.setOnNavigationItemSelectedListener {
        customTagBalloon.showAlignTop(bottomNavigationView, 130, 0)
        true
      }
    }
  }

  override fun onCustomItemClick(customItem: CustomItem) {
    this.customListBalloon.dismiss()
    Toast.makeText(applicationContext, customItem.title, Toast.LENGTH_SHORT).show()
  }
}
