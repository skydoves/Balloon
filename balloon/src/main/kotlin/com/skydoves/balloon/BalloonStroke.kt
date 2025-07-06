package com.skydoves.balloon

import androidx.annotation.ColorInt
import com.skydoves.balloon.annotations.Dp

public data class BalloonStroke(
  @ColorInt public val color: Int,
  @Dp public val thickness: Float = 1f
)