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

package com.skydoves.balloon.compose

import android.view.View
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.BalloonPlacement
import com.skydoves.balloon.DeferredBalloon
import com.skydoves.balloon.DeferredBalloonGroup
import com.skydoves.balloon.PlacementType

/**
 * A DSL for showing multiple balloons at the same time.
 */
public interface AwaitBalloonWindowsDsl {
  /**
   *  Set true to dismiss balloons sequentially. False by default.
   */
  public var dismissSequentially: Boolean

  /**
   * Look at [BalloonWindow.showAtCenter]
   */
  public fun BalloonWindow.atCenter(
    anchor: View,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  )

  /**
   * Look at [BalloonWindow.showAsDropDown]
   */
  public fun BalloonWindow.asDropDown(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignTop]
   */
  public fun BalloonWindow.alignTop(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignStart]
   */
  public fun BalloonWindow.alignStart(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignEnd]
   */
  public fun BalloonWindow.alignEnd(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignBottom]
   */
  public fun BalloonWindow.alignBottom(anchor: View, xOff: Int = 0, yOff: Int = 0)

  /**
   * Extension for [BalloonWindow.awaitAtCenter]
   */
  public fun View.atCenter(
    balloon: BalloonWindow,
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  ) {
    balloon.atCenter(this, xOff, yOff, centerAlign)
  }

  /**
   * Extension for [BalloonWindow.awaitAsDropDown]
   */
  public fun View.asDropDown(balloon: BalloonWindow, xOff: Int = 0, yOff: Int = 0) {
    balloon.asDropDown(this, xOff, yOff)
  }

  /**
   * Extension for [BalloonWindow.awaitAlignTop]
   */
  public fun View.alignTop(balloon: BalloonWindow, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignTop(this, xOff, yOff)
  }

  /**
   * Extension for [BalloonWindow.awaitAlignStart]
   */
  public fun View.alignStart(balloon: BalloonWindow, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignStart(this, xOff, yOff)
  }

  /**
   * Extension for [BalloonWindow.awaitAlignEnd]
   */
  public fun View.alignEnd(balloon: BalloonWindow, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignEnd(this, xOff, yOff)
  }

  /**
   * Extension for [BalloonWindow.awaitAlignBottom]
   */
  public fun View.alignBottom(balloon: BalloonWindow, xOff: Int = 0, yOff: Int = 0) {
    balloon.alignBottom(this, xOff, yOff)
  }
}

private class AwaitBalloonWindowsDslImpl : AwaitBalloonWindowsDsl {
  override var dismissSequentially: Boolean = false
  private val _balloons = mutableListOf<DeferredBalloon>()

  override fun BalloonWindow.atCenter(
    anchor: View,
    xOff: Int,
    yOff: Int,
    centerAlign: BalloonCenterAlign,
  ) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
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

  override fun BalloonWindow.asDropDown(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          type = PlacementType.DROPDOWN,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignTop(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.TOP,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignStart(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.START,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignEnd(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchor,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.END,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignBottom(anchor: View, xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
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
public suspend fun awaitBalloonWindows(block: AwaitBalloonWindowsDsl.() -> Unit) {
  val balloons = AwaitBalloonWindowsDslImpl().run {
    block()
    build()
  }
  Balloon.initConsumerIfNeeded()
  Balloon.channel.send(balloons)
}
