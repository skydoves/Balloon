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
import kotlinx.android.synthetic.main.item_custom.view.*

class CustomViewHolder(
  private val delegate: Delegate,
  view: View
) : BaseViewHolder(view) {

  interface Delegate {
    fun onCustomItemClick(customItem: CustomItem)
  }

  private lateinit var customItem: CustomItem

  override fun bindData(data: Any) {
    if (data is CustomItem) {
      this.customItem = data
      drawItemUI()
    }
  }

  private fun drawItemUI() {
    itemView.run {
      item_custom_icon.setImageDrawable(customItem.icon)
      item_custom_title.text = customItem.title
    }
  }

  override fun onClick(p0: View?) {
    delegate.onCustomItemClick(customItem)
  }

  override fun onLongClick(p0: View?) = false
}
