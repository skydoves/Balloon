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
import com.skydoves.balloondemo.databinding.ItemSampleBinding

class SampleAdapter(
  private val delegate: SampleViewHolder.Delegate
) : RecyclerView.Adapter<SampleAdapter.SampleViewHolder>() {

  private val sampleItems = mutableListOf<SampleItem>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleViewHolder {
    val binding = ItemSampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return SampleViewHolder(binding)
  }

  override fun onBindViewHolder(holder: SampleViewHolder, position: Int) {
    val sampleItem = this.sampleItems[position]
    holder.binding.run {
      sample0Avatar.setImageDrawable(sampleItem.image)
      sample0Name.text = sampleItem.name
      sample0Content.text = sampleItem.content
      root.setOnClickListener { delegate.onItemClick(sampleItem, root) }
    }
  }

  fun addItems(sampleItems: List<SampleItem>) {
    this.sampleItems.addAll(sampleItems)
    notifyDataSetChanged()
  }

  override fun getItemCount() = this.sampleItems.size

  class SampleViewHolder(val binding: ItemSampleBinding) : RecyclerView.ViewHolder(binding.root) {

    interface Delegate {
      fun onItemClick(sampleItem: SampleItem, view: View)
    }
  }
}
