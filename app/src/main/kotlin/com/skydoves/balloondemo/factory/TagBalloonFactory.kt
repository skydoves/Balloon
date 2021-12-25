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
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.animations.BalloonRotateAnimation
import com.skydoves.balloon.createBalloon
import com.skydoves.balloondemo.R

class TagBalloonFactory : Balloon.Factory() {

  override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
    return createBalloon(context) {
      setWidth(BalloonSizeSpec.WRAP)
      setHeight(BalloonSizeSpec.WRAP)
      setLayout(R.layout.layout_custom_tag)
      setArrowSize(10)
      setArrowOrientation(ArrowOrientation.BOTTOM)
      setArrowPosition(0.5f)
      setPadding(4)
      setCornerRadius(4f)
      setBalloonAnimationStyle(R.style.Balloon_Fade_Anim)
      setBackgroundColorResource(R.color.white_93)
      setBalloonHighlightAnimation(BalloonHighlightAnimation.ROTATE)
      setBalloonRotationAnimation(BalloonRotateAnimation.Builder().build())
      setDismissWhenClicked(true)
      setDismissWhenShowAgain(true)
      setLifecycleOwner(lifecycle)
    }
  }
}
