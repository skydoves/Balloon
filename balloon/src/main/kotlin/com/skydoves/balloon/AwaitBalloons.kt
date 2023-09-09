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

package com.skydoves.balloon

import android.view.View

/**
 * A DSL for showing multiple balloons at the same time.
 */
public interface AwaitBalloonsDsl {
  /**
   *  Set true to dismiss balloons sequentially. False by default.
   */
  public var dismissSequentially: Boolean

  /**
   * Look at [Balloon.showAtCenter]
   */
  public fun Balloon.atCenter(
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  )

  /**
   * Look at [Balloon.showAsDropDown]
   */
  public fun Balloon.asDropDown(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [Balloon.showAlignTop]
   */
  public fun Balloon.alignTop(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [Balloon.showAlignStart]
   */
  public fun Balloon.alignStart(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [Balloon.showAlignEnd]
   */
  public fun Balloon.alignEnd(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [Balloon.showAlignBottom]
   */
  public fun Balloon.alignBottom(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Extension for [Balloon.awaitAtCenter]
   */
  public fun View.atCenter(
    balloon: Balloon,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  ) {
    balloon.atCenter(this, xOff, yOff, centerAlign)
  }

  /**
   * Extension for [Balloon.awaitAsDropDown]
   */
  public fun View.asDropDown(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
    balloon.asDropDown(this, xOff, yOff)
  }

  /**
   * Extension for [Balloon.awaitAlignTop]
   */
  public fun View.alignTop(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignTop(this, xOff, yOff)
  }

  /**
   * Extension for [Balloon.awaitAlignStart]
   */
  public fun View.alignStart(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignStart(this, xOff, yOff)
  }

  /**
   * Extension for [Balloon.awaitAlignEnd]
   */
  public fun View.alignEnd(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignEnd(this, xOff, yOff)
  }

  /**
   * Extension for [Balloon.awaitAlignBottom]
   */
  public fun View.alignBottom(balloon: Balloon, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignBottom(this, xOff, yOff)
  }
}

private class AwaitBalloonsDslImpl : AwaitBalloonsDsl {
  override var dismissSequentially: Boolean = false
  private val _balloons = mutableListOf<DeferredBalloon>()

  override fun Balloon.atCenter(
    anchor: View,
    xOff: Int,
    yOff: Int,
    centerAlign: BalloonCenterAlign,
  ) {
    _balloons.add(
      DeferredBalloon(
        balloon = this,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = centerAlign.toAlign(),
          type = PlacementType.CENTER,
        ),
      ),
    )
  }

  override fun Balloon.asDropDown(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          type = PlacementType.DROPDOWN,
        ),
      ),
    )
  }

  override fun Balloon.alignTop(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.TOP,
        ),
      ),
    )
  }

  override fun Balloon.alignStart(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.START,
        ),
      ),
    )
  }

  override fun Balloon.alignEnd(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.END,
        ),
      ),
    )
  }

  override fun Balloon.alignBottom(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.BOTTOM,
        ),
      ),
    )
  }

  fun build() = DeferredBalloonGroup(_balloons, dismissSequentially)
}

/**
 * Awaits for multiple balloons to be shown at the same time.
 */
public suspend fun awaitBalloons(block: AwaitBalloonsDsl.() -> Unit) {
  val balloons = AwaitBalloonsDslImpl().run {
    block()
    build()
  }
  Balloon.initConsumerIfNeeded()
  Balloon.channel.send(balloons)
}
