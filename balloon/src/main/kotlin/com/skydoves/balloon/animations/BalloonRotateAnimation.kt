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

@file:Suppress("unused", "RedundantVisibilityModifier")

package com.skydoves.balloon.animations

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * BalloonRotateAnimation gives rotation animation to the balloon.
 * This class can be created by [BalloonRotateAnimation.Builder].
 */
public class BalloonRotateAnimation private constructor(
  builder: Builder
) : Animation() {

  private val degreeX: Float
  private val degreeY: Float
  private val degreeZ: Float
  private var width: Float = 0f
  private var height: Float = 0f

  init {
    degreeX = builder.degreeX.toFloat()
    degreeY = (360 * builder.turns).toFloat() * builder.direction.value
    degreeZ = builder.degreeZ.toFloat()
    duration = builder.speeds.toLong()
    repeatCount = if (builder.loops == INFINITE) {
      INFINITE
    } else {
      builder.loops - 1
    }
  }

  override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
    super.initialize(width, height, parentWidth, parentHeight)
    this.width = width * 0.5f
    this.height = height * 0.5f
  }

  override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
    val xDegrees = degreeX * interpolatedTime
    val yDegrees = degreeY * interpolatedTime
    val zDegrees = degreeZ * interpolatedTime
    val matrix = transformation.matrix
    Camera().apply {
      save()
      rotateX(xDegrees)
      rotateY(yDegrees)
      rotateZ(zDegrees)
      getMatrix(matrix)
      restore()
    }
    matrix.preTranslate(-width, -height)
    matrix.postTranslate(width, height)
  }

  /** Builder class to create [BalloonRotateAnimation]. */
  public class Builder {
    @JvmField
    @set:JvmSynthetic
    public var direction: BalloonRotateDirection = BalloonRotateDirection.RIGHT

    @JvmField
    @set:JvmSynthetic
    public var turns: Int = 1

    @JvmField
    @set:JvmSynthetic
    public var loops: Int = INFINITE

    @JvmField
    @set:JvmSynthetic
    public var speeds: Int = 2500

    @JvmField
    @set:JvmSynthetic
    public var degreeX: Int = 0

    @JvmField
    @set:JvmSynthetic
    public var degreeZ: Int = 0

    /** sets the direction of the rotation animation. */
    public fun setDirection(rotateDirection: BalloonRotateDirection): Builder = apply {
      this.direction = rotateDirection
    }

    /** sets the turning count of the rotation animation. */
    public fun setTurns(turn: Int): Builder = apply { this.turns = turn }

    /** sets the iteration of the rotation animation. */
    public fun setLoops(loop: Int): Builder = apply { this.loops = loop }

    /** sets the speed of the rotation animation. */
    public fun setSpeeds(speed: Int): Builder = apply { this.speeds = speed }

    /** sets the degree X of the rotation animation. */
    public fun setDegreeX(degreeX: Int): Builder = apply { this.degreeX = degreeX }

    /** sets the degree Y of the rotation animation. */
    public fun setDegreeZ(degreeZ: Int): Builder = apply { this.degreeZ = degreeZ }

    /** Build the [BalloonRotateAnimation]. */
    public fun build(): BalloonRotateAnimation = BalloonRotateAnimation(this)
  }
}
