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
@Deprecated(
  message = "Use BalloonState with Modifier.balloon() instead.",
  level = DeprecationLevel.WARNING,
)
@Suppress("DEPRECATION")
public interface AwaitBalloonWindowsDsl {
  /**
   *  Set true to dismiss balloons sequentially. False by default.
   */
  public var dismissSequentially: Boolean

  /**
   * Look at [BalloonWindow.showAtCenter]
   */
  public fun BalloonWindow.atCenter(
    xOff: Int = 0,
    yOff: Int = 0,
    centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  )

  /**
   * Look at [BalloonWindow.showAsDropDown]
   */
  public fun BalloonWindow.asDropDown(xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignTop]
   */
  public fun BalloonWindow.alignTop(xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignStart]
   */
  public fun BalloonWindow.alignStart(xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignEnd]
   */
  public fun BalloonWindow.alignEnd(xOff: Int = 0, yOff: Int = 0)

  /**
   * Look at [BalloonWindow.showAlignBottom]
   */
  public fun BalloonWindow.alignBottom(xOff: Int = 0, yOff: Int = 0)
}

@Suppress("DEPRECATION")
private class AwaitBalloonWindowsDslImpl : AwaitBalloonWindowsDsl {
  override var dismissSequentially: Boolean = false
  private val _balloons = mutableListOf<DeferredBalloon>()

  override fun BalloonWindow.atCenter(
    xOff: Int,
    yOff: Int,
    centerAlign: BalloonCenterAlign,
  ) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchorView,
          xOff = xOff,
          yOff = yOff,
          align = centerAlign.toAlign(),
          type = PlacementType.CENTER,
        ),
      ),
    )
  }

  override fun BalloonWindow.asDropDown(xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchorView,
          xOff = xOff,
          yOff = yOff,
          type = PlacementType.DROPDOWN,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignTop(xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchorView,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.TOP,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignStart(xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchorView,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.START,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignEnd(xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchorView,
          xOff = xOff,
          yOff = yOff,
          align = BalloonAlign.END,
        ),
      ),
    )
  }

  override fun BalloonWindow.alignBottom(xOff: Int, yOff: Int) {
    _balloons.add(
      DeferredBalloon(
        balloon = this.balloon,
        placement = BalloonPlacement(
          anchor = anchorView,
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
@Deprecated(
  message = "Use BalloonState with Modifier.balloon() instead.",
  level = DeprecationLevel.WARNING,
)
@Suppress("DEPRECATION")
public suspend fun awaitBalloonWindows(block: AwaitBalloonWindowsDsl.() -> Unit) {
  val balloons = AwaitBalloonWindowsDslImpl().run {
    block()
    build()
  }
  Balloon.initConsumerIfNeeded()
  Balloon.channel.send(balloons)
}
