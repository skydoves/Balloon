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
import kotlinx.android.synthetic.main.item_custom.view.item_custom_icon
import kotlinx.android.synthetic.main.item_custom.view.item_custom_title

class CustomAdapter(
  private val delegate: CustomViewHolder.Delegate
) : RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {

  private val customItems = mutableListOf<CustomItem>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return CustomViewHolder(inflater.inflate(R.layout.item_custom, parent, false))
  }

  override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
    val customItem = this.customItems[position]
    holder.itemView.run {
      item_custom_icon.setImageDrawable(customItem.icon)
      item_custom_title.text = customItem.title
      setOnClickListener { delegate.onCustomItemClick(customItem) }
    }
  }

  fun addCustomItem(customList: List<CustomItem>) {
    this.customItems.addAll(customList)
    notifyDataSetChanged()
  }

  override fun getItemCount() = this.customItems.size

  class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    interface Delegate {
      fun onCustomItemClick(customItem: CustomItem)
    }
  }
}
