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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks down which way the highlight animations move.
 *
 * These two mappings are easy to get backwards, because the View implementation selects its
 * animation resource by the name of the side OPPOSITE the arrow, while the values inside that
 * resource are expressed relative to the arrow side. Reading the selector alone gives you the
 * wrong answer; you have to open the XML it points at.
 *
 * The expectations below are transcribed from `res/anim/balloon_{heartbeat,shake}_*.xml` at
 * tag `1.7.6`, paired with the selector in `Balloon.getBalloonHighlightAnimation()`.
 */
class BalloonHighlightAnimationTest {

  // ------------------------------------------------------------------ heartbeat

  @Test
  fun heartbeat_pivotsOnTheArrowEdge() {
    // arrow TOP -> `balloon_heartbeat_bottom` -> pivotX 50%, pivotY 0%
    assertEquals(
      TransformOrigin(0.5f, 0f),
      heartbeatOrigin(ResolvedArrowSide.TOP, isArrowVisible = true),
    )
    // arrow BOTTOM -> `balloon_heartbeat_top` -> pivotX 50%, pivotY 100%
    assertEquals(
      TransformOrigin(0.5f, 1f),
      heartbeatOrigin(ResolvedArrowSide.BOTTOM, isArrowVisible = true),
    )
    // arrow LEFT -> `balloon_heartbeat_right` -> pivotX 0%, pivotY 50%
    assertEquals(
      TransformOrigin(0f, 0.5f),
      heartbeatOrigin(ResolvedArrowSide.LEFT, isArrowVisible = true),
    )
    // arrow RIGHT -> `balloon_heartbeat_left` -> pivotX 100%, pivotY 50%
    assertEquals(
      TransformOrigin(1f, 0.5f),
      heartbeatOrigin(ResolvedArrowSide.RIGHT, isArrowVisible = true),
    )
  }

  @Test
  fun heartbeat_pivotsOnTheCentreWhenTheArrowIsHidden() {
    ResolvedArrowSide.entries.forEach { side ->
      assertEquals(
        TransformOrigin.Center,
        heartbeatOrigin(side, isArrowVisible = false),
        "hidden arrow on $side should use balloon_heartbeat_center",
      )
    }
  }

  // ---------------------------------------------------------------------- shake

  @Test
  fun shake_slidesAwayFromTheArrow() {
    // arrow TOP -> `balloon_shake_bottom` -> toYDelta +13%, i.e. downward
    assertEquals(Offset(0f, 1f), shakeDirection(ResolvedArrowSide.TOP))
    // arrow BOTTOM -> `balloon_shake_top` -> toYDelta -13%, i.e. upward
    assertEquals(Offset(0f, -1f), shakeDirection(ResolvedArrowSide.BOTTOM))
    // arrow LEFT -> `balloon_shake_right` -> toXDelta +13%, i.e. rightward
    assertEquals(Offset(1f, 0f), shakeDirection(ResolvedArrowSide.LEFT))
    // arrow RIGHT -> `balloon_shake_left` -> toXDelta -13%, i.e. leftward
    assertEquals(Offset(-1f, 0f), shakeDirection(ResolvedArrowSide.RIGHT))
  }

  @Test
  fun shake_movesOnExactlyOneAxis() {
    ResolvedArrowSide.entries.forEach { side ->
      val d = shakeDirection(side)
      assertEquals(
        1f,
        kotlin.math.abs(d.x) + kotlin.math.abs(d.y),
        "shake on $side should be a unit step on one axis, was $d",
      )
    }
  }

  @Test
  fun shakeAndHeartbeat_agreeOnWhichEdgeTheArrowIsOn() {
    // The two animations read the same geometry from opposite ends: heartbeat pins the arrow
    // edge, shake runs away from it. If one is ever flipped without the other, this fails.
    mapOf(
      ResolvedArrowSide.TOP to Pair(TransformOrigin(0.5f, 0f), Offset(0f, 1f)),
      ResolvedArrowSide.BOTTOM to Pair(TransformOrigin(0.5f, 1f), Offset(0f, -1f)),
      ResolvedArrowSide.LEFT to Pair(TransformOrigin(0f, 0.5f), Offset(1f, 0f)),
      ResolvedArrowSide.RIGHT to Pair(TransformOrigin(1f, 0.5f), Offset(-1f, 0f)),
    ).forEach { (side, expected) ->
      assertEquals(expected.first, heartbeatOrigin(side, isArrowVisible = true))
      assertEquals(expected.second, shakeDirection(side))
    }
  }
}
