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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.balloondemo.R
import kotlinx.android.synthetic.main.item_sample.view.sample0_avatar
import kotlinx.android.synthetic.main.item_sample.view.sample0_content
import kotlinx.android.synthetic.main.item_sample.view.sample0_name

@Suppress("PrivatePropertyName")
class SampleAdapter(
  private val delegate: SampleViewHolder.Delegate
) : RecyclerView.Adapter<SampleAdapter.SampleViewHolder>() {

  private val sampleItems = mutableListOf<SampleItem>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return SampleViewHolder(inflater.inflate(R.layout.item_sample, parent, false))
  }

  override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
    val sampleItem = this.sampleItems[position]
    holder.itemView.run {
      sample0_avatar.setImageDrawable(sampleItem.image)
      sample0_name.text = sampleItem.name
      sample0_content.text = sampleItem.content
      setOnClickListener { delegate.onItemClick(sampleItem, this) }
    }
  }

  fun addItems(sampleItems: List<SampleItem>) {
    this.sampleItems.addAll(sampleItems)
    notifyDataSetChanged()
  }

  override fun getItemCount() = this.sampleItems.size

  class SampleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    interface Delegate {
      fun onItemClick(sampleItem: SampleItem, view: View)
    }
  }
}
