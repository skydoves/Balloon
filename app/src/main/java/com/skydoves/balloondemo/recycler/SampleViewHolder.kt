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

package com.skydoves.balloondemo.recycler

import android.view.View
import com.skydoves.baserecyclerviewadapter.BaseViewHolder
import kotlinx.android.synthetic.main.item_sample.view.*

@Suppress("CanBeParameter")
class SampleViewHolder(
  private val view: View,
  private val delegate: Delegate
) : BaseViewHolder(view) {

  interface Delegate {
    fun onItemClick(sampleItem: SampleItem)
  }

  private lateinit var sampleItem: SampleItem

  override fun bindData(data: Any) {
    if (data is SampleItem) {
      sampleItem = data
      drawItem()
    }
  }

  private fun drawItem() {
    itemView.run {
      sample0_avatar.setImageDrawable(sampleItem.image)
      sample0_name.text = sampleItem.name
      sample0_content.text = sampleItem.content
    }
  }

  override fun onClick(v: View?) {
    delegate.onItemClick(this.sampleItem)
  }

  override fun onLongClick(v: View?) = false
}
