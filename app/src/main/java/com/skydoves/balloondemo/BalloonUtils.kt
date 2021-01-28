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

import android.content.Context
import android.widget.Toast
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.OnBalloonClickListener
import java.util.Locale

object BalloonUtils {

  fun getEditBalloon(context: Context, lifecycleOwner: LifecycleOwner): Balloon {
    return Balloon.Builder(context)
      .setText("You can edit your profile now!")
      .setArrowSize(10)
      .setWidthRatio(1.0f)
      .setHeight(BalloonSizeSpec.WRAP)
      .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
      .setArrowPosition(0.5f)
      .setPadding(12)
      .setMarginRight(12)
      .setMarginLeft(12)
      .setTextSize(15f)
      .isRtlSupport(isRtlLayout())
      .setCornerRadius(8f)
      .setTextColorResource(R.color.white_87)
      .setIconDrawableResource(R.drawable.ic_edit)
      .setBackgroundColorResource(R.color.skyBlue)
      .setOnBalloonDismissListener {
        Toast.makeText(context.applicationContext, "dismissed", Toast.LENGTH_SHORT).show()
      }
      .setBalloonAnimation(BalloonAnimation.ELASTIC)
      .setLifecycleOwner(lifecycleOwner)
      .build()
  }

  fun getNavigationBalloon(
    context: Context,
    onBalloonClickListener: OnBalloonClickListener,
    lifecycleOwner: LifecycleOwner
  ): Balloon {
    return Balloon.Builder(context)
      .setText("You can access your profile from on now.")
      .setArrowSize(10)
      .setArrowPosition(0.62f)
      .setWidthRatio(1.0f)
      .setHeight(BalloonSizeSpec.WRAP)
      .setTextSize(15f)
      .isRtlSupport(isRtlLayout())
      .setPadding(10)
      .setMarginRight(12)
      .setMarginLeft(12)
      .setCornerRadius(4f)
      .setAlpha(0.9f)
      .setTextColorResource(R.color.white_93)
      .setIconDrawableResource(R.drawable.ic_profile)
      .setBackgroundColorResource(R.color.colorPrimary)
      .setOnBalloonClickListener(onBalloonClickListener)
      .setBalloonAnimation(BalloonAnimation.FADE)
      .setLifecycleOwner(lifecycleOwner)
      .build()
  }

  fun isRtlLayout(): Boolean {
    return TextUtilsCompat.getLayoutDirectionFromLocale(
      Locale.getDefault()
    ) == ViewCompat.LAYOUT_DIRECTION_RTL
  }
}
