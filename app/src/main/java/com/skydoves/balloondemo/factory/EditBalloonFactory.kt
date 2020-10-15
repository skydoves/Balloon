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
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowConstraints
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.overlay.BalloonOverlayRoundRect
import com.skydoves.balloondemo.BalloonUtils
import com.skydoves.balloondemo.R

class EditBalloonFactory : Balloon.Factory() {

  override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
    return Balloon.Builder(context)
      .setText("You can edit your profile now!")
      .setArrowSize(10)
      .setWidthRatio(1.0f)
      .setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
      .setArrowPosition(0.5f)
      .setPadding(12)
      .setMarginRight(12)
      .setMarginLeft(12)
      .setTextSize(15f)
      .isRtlSupport(BalloonUtils.isRtlLayout())
      .setCornerRadius(8f)
      .setTextColorResource(R.color.white_87)
      .setIconDrawableResource(R.drawable.ic_edit)
      .setBackgroundColorResource(R.color.skyBlue)
      .setBalloonAnimation(BalloonAnimation.ELASTIC)
      .setIsVisibleOverlay(true)
      .setOverlayColorResource(R.color.overlay)
      .setOverlayPadding(6f)
      .setOverlayShape(BalloonOverlayRoundRect(12f, 12f))
      .setLifecycleOwner(lifecycle)
      .setDismissWhenClicked(true)
      .setOnBalloonDismissListener {
        Toast.makeText(context.applicationContext, "dismissed", Toast.LENGTH_SHORT).show()
      }.build()
  }
}
