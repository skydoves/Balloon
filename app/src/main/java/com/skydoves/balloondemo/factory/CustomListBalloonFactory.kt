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

package com.skydoves.balloondemo.factory

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloondemo.R

class CustomListBalloonFactory : Balloon.Factory() {

  override fun create(context: Context, lifecycleOwner: LifecycleOwner): Balloon {
    return Balloon.Builder(context)
      .setLayout(R.layout.layout_custom_list)
      .setArrowSize(10)
      .setArrowOrientation(ArrowOrientation.TOP)
      .setArrowPosition(0.85f)
      .setWidth(170)
      .setHeight(200)
      .setCornerRadius(4f)
      .setBackgroundColor(ContextCompat.getColor(context, R.color.background800))
      .setBalloonAnimation(BalloonAnimation.FADE)
      .setLifecycleOwner(lifecycleOwner)
      .build()
  }
}
