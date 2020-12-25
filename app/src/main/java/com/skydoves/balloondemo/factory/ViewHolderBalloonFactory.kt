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
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.IconGravity
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.textForm
import com.skydoves.balloondemo.BalloonUtils.isRtlLayout
import com.skydoves.balloondemo.R

class ViewHolderBalloonFactory : Balloon.Factory() {

  override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
    val textForm = textForm(context) {
      setText("This is your new content!!!")
      setTextSize(15f)
      setTextColorResource(R.color.white_87)
    }

    return createBalloon(context) {
      setWidth(BalloonSizeSpec.WRAP)
      setHeight(BalloonSizeSpec.WRAP)
      setArrowSize(10)
      setPadding(10)
      setCornerRadius(8f)
      setElevation(4)
      setTextForm(textForm)
      isRtlSupport(isRtlLayout())
      setArrowPosition(0.5f)
      setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
      setArrowOrientation(ArrowOrientation.TOP)
      setIconDrawableResource(R.drawable.ic_edit)
      setBackgroundColorResource(R.color.yellow)
      setOnBalloonDismissListener {
        Toast.makeText(context, "dismissed", Toast.LENGTH_SHORT).show()
      }
      setIconGravity(IconGravity.RIGHT)
      setDismissWhenClicked(true)
      setDismissWhenShowAgain(true)
      setBalloonAnimation(BalloonAnimation.ELASTIC)
      setLifecycleOwner(lifecycle)
      build()
    }
  }
}
