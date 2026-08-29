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

package com.skydoves.balloon.sample.labs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonOverlayAnimation
import com.skydoves.balloon.BalloonOverlayShape
import com.skydoves.balloon.BalloonRotateAnimation
import com.skydoves.balloon.BalloonRotateDirection
import com.skydoves.balloon.BalloonStyle
import kotlin.math.roundToInt

/**
 * Which `Balloon.Builder` inset overload a padding / margin group is driven through.
 *
 * The builder exposes three shapes of the same knob and they are not interchangeable in the
 * UI: switching mode has to switch *which setters run*, so each mode keeps its own values in
 * [LabInsets] instead of trying to project one representation onto another.
 */
internal enum class LabInsetMode(val label: String) {
  UNIFORM("Uniform"),
  AXIS("H / V"),
  PER_SIDE("Per side"),
}

/**
 * Every inset value the three [LabInsetMode]s need, kept side by side so flipping mode never
 * destroys what the previous mode was set to.
 */
@Immutable
internal data class LabInsets(
  val uniform: Dp = 0.dp,
  val horizontal: Dp = 0.dp,
  val vertical: Dp = 0.dp,
  val start: Dp = 0.dp,
  val top: Dp = 0.dp,
  val end: Dp = 0.dp,
  val bottom: Dp = 0.dp,
)

/** Builds a [LabInsets] whose every field is [all], for defaults that start out uniform. */
internal fun labInsets(all: Dp): LabInsets =
  LabInsets(all, all, all, all, all, all, all)

/** Which `BalloonState` show method the stage calls. */
internal enum class LabPlacement(val label: String) {
  ALIGN_TOP("Align top"),
  ALIGN_BOTTOM("Align bottom"),
  ALIGN_START("Align start"),
  ALIGN_END("Align end"),
  DROP_DOWN("As drop down"),
  CENTER_OVERLAY("Center overlay"),
  AT_CENTER("At center"),
}

/**
 * The [BalloonOverlayShape] variants, flattened into one pickable list.
 *
 * `Circle` appears twice on purpose: with no radius it falls back to half the anchor's longer
 * side, and with an explicit radius it does not, and those two behaviours are worth seeing
 * next to each other.
 */
internal enum class LabOverlayShapeKind(val label: String) {
  EMPTY("Empty"),
  RECT("Rect"),
  OVAL("Oval"),
  CIRCLE_AUTO("Circle (auto)"),
  CIRCLE_RADIUS("Circle (radius)"),
  ROUND_RECT("RoundRect"),
  ROUND_RECT_PER_CORNER("RoundRect per corner"),
}

/** Which composable the balloon renders, to show that the body is caller supplied. */
internal enum class LabBodyPreset(val label: String) {
  TEXT("Plain text"),
  TEXT_WITH_ICON("Text + icon"),
  RICH_CARD("Custom layout"),
}

/** Arrow geometry, colour and the two rules that decide where it ends up. */
@Immutable
internal data class LabArrowConfig(
  val isVisible: Boolean = true,
  val linkSize: Boolean = true,
  val width: Dp = 12.dp,
  val height: Dp = 12.dp,
  val color: Color = Color.Unspecified,
  val orientation: ArrowOrientation? = null,
  val orientationRules: ArrowOrientationRules = ArrowOrientationRules.ALIGN_ANCHOR,
  val position: Float = 0.5f,
  val positionRules: ArrowPositionRules = ArrowPositionRules.ALIGN_BALLOON,
  val alignAnchorPadding: Dp = 0.dp,
  val alignAnchorPaddingRatio: Float = 2.5f,
)

/** Everything painted on the balloon card itself, plus its inner padding and outer margin. */
@Immutable
internal data class LabBodyConfig(
  val backgroundColor: Color = Color(0xFF2196F3),
  val cornerRadius: Dp = 12.dp,
  val borderColor: Color = Color.Unspecified,
  val borderThickness: Dp = 0.dp,
  val elevation: Dp = 2.dp,
  val alpha: Float = 1f,
  val paddingMode: LabInsetMode = LabInsetMode.UNIFORM,
  val padding: LabInsets = LabInsets(
    uniform = 14.dp,
    horizontal = 16.dp,
    vertical = 10.dp,
    start = 16.dp,
    top = 10.dp,
    end = 16.dp,
    bottom = 10.dp,
  ),
  val marginMode: LabInsetMode = LabInsetMode.UNIFORM,
  val margin: LabInsets = LabInsets(),
)

/**
 * The seven width specs and the one height spec, each with its own on / off switch.
 *
 * They are deliberately independent rather than a single "mode" picker: the library resolves
 * them by precedence, and the only way to see that is to be able to set several at once.
 * See [widthPrecedenceHint].
 */
@Immutable
internal data class LabSizingConfig(
  val useWidth: Boolean = false,
  val width: Dp = 240.dp,
  val widthRatio: Float = 0f,
  val useMinWidth: Boolean = false,
  val minWidth: Dp = 140.dp,
  val useMaxWidth: Boolean = false,
  val maxWidth: Dp = 280.dp,
  val minWidthRatio: Float = 0f,
  val maxWidthRatio: Float = 0f,
  val useHeight: Boolean = false,
  val height: Dp = 120.dp,
)

/** Which show call is used, and the manual offset handed to it. */
@Immutable
internal data class LabPlacementConfig(
  val placement: LabPlacement = LabPlacement.ALIGN_BOTTOM,
  val centerAlign: BalloonCenterAlign = BalloonCenterAlign.TOP,
  val xOffset: Dp = 0.dp,
  val yOffset: Dp = 0.dp,
)

/** Enter / exit transition, the looping highlight, and the rotation the highlight can use. */
@Immutable
internal data class LabAnimationConfig(
  val animation: BalloonAnimation = BalloonAnimation.FADE,
  val circularDurationMillis: Long = 500L,
  val highlight: BalloonHighlightAnimation = BalloonHighlightAnimation.NONE,
  val highlightStartDelayMillis: Long = 0L,
  val overlayAnimation: BalloonOverlayAnimation = BalloonOverlayAnimation.FADE,
  val rotateDirection: BalloonRotateDirection = BalloonRotateDirection.RIGHT,
  val rotateTurns: Int = 1,
  val rotateLoopsInfinite: Boolean = true,
  val rotateLoops: Int = 2,
  val rotateSpeedMillis: Int = 2500,
  val rotateDegreeX: Int = 0,
  val rotateDegreeZ: Int = 0,
)

/** The dimming scrim and the shape of the hole it leaves over the anchor. */
@Immutable
internal data class LabOverlayConfig(
  val isVisible: Boolean = false,
  val color: Color = Color(0xBF000000),
  val shape: LabOverlayShapeKind = LabOverlayShapeKind.OVAL,
  val circleRadius: Dp = 56.dp,
  val roundRectRadiusX: Dp = 12.dp,
  val roundRectRadiusY: Dp = 12.dp,
  val cornerTopStart: Dp = 20.dp,
  val cornerTopEnd: Dp = 4.dp,
  val cornerBottomEnd: Dp = 20.dp,
  val cornerBottomStart: Dp = 4.dp,
  val paddingMode: LabInsetMode = LabInsetMode.UNIFORM,
  val padding: LabInsets = labInsets(8.dp),
  val paddingColor: Color = Color.Unspecified,
)

/**
 * The dismiss rules, focus, and auto dismiss.
 *
 * `dismissWhenTouchOutside` and `focusable` default to `false` here rather than to the
 * library's `true`: a focusable, touch-modal balloon eats the first tap on every control,
 * which would make the lab need two taps per slider. Turn either on to feel the difference.
 */
@Immutable
internal data class LabBehaviourConfig(
  val dismissWhenClicked: Boolean = false,
  val dismissWhenTouchOutside: Boolean = false,
  val dismissWhenTouchMargin: Boolean = true,
  val dismissWhenBackPressed: Boolean = true,
  val dismissWhenShowAgain: Boolean = false,
  val dismissWhenOverlayClicked: Boolean = true,
  val focusable: Boolean = false,
  val autoDismissMillis: Long = 0L,
)

/**
 * The whole lab in one value.
 *
 * Being a single value-equal object is what makes the screen's "re-show on any change" rule a
 * one-liner: `LaunchedEffect(config)` restarts whenever any knob anywhere moves, so the user
 * never has to re-open the balloon to see what they just changed.
 */
@Immutable
internal data class LabsConfig(
  val arrow: LabArrowConfig = LabArrowConfig(),
  val body: LabBodyConfig = LabBodyConfig(),
  val sizing: LabSizingConfig = LabSizingConfig(),
  val placement: LabPlacementConfig = LabPlacementConfig(),
  val animation: LabAnimationConfig = LabAnimationConfig(),
  val overlay: LabOverlayConfig = LabOverlayConfig(),
  val behaviour: LabBehaviourConfig = LabBehaviourConfig(),
  val bodyPreset: LabBodyPreset = LabBodyPreset.TEXT,
)

/**
 * Projects the lab state onto a [BalloonStyle] through the fluent [Balloon.Builder], so the
 * screen exercises the same migration API a porting user would type rather than constructing
 * [BalloonStyle] directly.
 */
internal fun LabsConfig.toBalloonStyle(): BalloonStyle = Balloon.Builder().apply {
  applyArrow(arrow)
  applyBody(body)
  applySizing(sizing)
  applyAnimation(animation)
  applyOverlay(overlay)
  // Behaviour goes last on purpose: `setBalloonAnimation(CIRCULAR)` and
  // `setDismissWhenTouchOutside(false)` both flip `focusable` off as a documented side
  // effect, so the focus switch is only authoritative if it runs after them.
  applyBehaviour(behaviour)
}.build()

private fun Balloon.Builder.applyArrow(arrow: LabArrowConfig) {
  setIsVisibleArrow(arrow.isVisible)
  if (arrow.linkSize) {
    // The single argument overload sets both dimensions, which is what most call sites use.
    setArrowSize(arrow.width)
  } else {
    setArrowWidth(arrow.width)
    setArrowHeight(arrow.height)
  }
  setArrowColor(arrow.color)
  // There is no setter that resets the orientation back to null, so "Auto" simply skips it
  // and lets the alignment derive the edge.
  arrow.orientation?.let { setArrowOrientation(it) }
  setArrowOrientationRules(arrow.orientationRules)
  setArrowPosition(arrow.position)
  setArrowPositionRules(arrow.positionRules)
  setArrowAlignAnchorPadding(arrow.alignAnchorPadding)
  setArrowAlignAnchorPaddingRatio(arrow.alignAnchorPaddingRatio)
}

private fun Balloon.Builder.applyBody(body: LabBodyConfig) {
  setBackgroundColor(body.backgroundColor)
  setCornerRadius(body.cornerRadius)
  setBorder(body.borderColor, body.borderThickness)
  setElevation(body.elevation)
  setAlpha(body.alpha)
  when (body.paddingMode) {
    LabInsetMode.UNIFORM -> setPadding(body.padding.uniform)
    LabInsetMode.AXIS -> {
      setPaddingHorizontal(body.padding.horizontal)
      setPaddingVertical(body.padding.vertical)
    }
    LabInsetMode.PER_SIDE -> {
      setPaddingStart(body.padding.start)
      setPaddingTop(body.padding.top)
      setPaddingEnd(body.padding.end)
      setPaddingBottom(body.padding.bottom)
    }
  }
  when (body.marginMode) {
    LabInsetMode.UNIFORM -> setMargin(body.margin.uniform)
    LabInsetMode.AXIS -> {
      setMarginHorizontal(body.margin.horizontal)
      setMarginVertical(body.margin.vertical)
    }
    LabInsetMode.PER_SIDE -> {
      setMarginStart(body.margin.start)
      setMarginTop(body.margin.top)
      setMarginEnd(body.margin.end)
      setMarginBottom(body.margin.bottom)
    }
  }
}

private fun Balloon.Builder.applySizing(sizing: LabSizingConfig) {
  if (sizing.useWidth) setWidth(sizing.width)
  setWidthRatio(sizing.widthRatio)
  setMinWidthRatio(sizing.minWidthRatio)
  setMaxWidthRatio(sizing.maxWidthRatio)
  if (sizing.useMinWidth) setMinWidth(sizing.minWidth)
  if (sizing.useMaxWidth) setMaxWidth(sizing.maxWidth)
  if (sizing.useHeight) setHeight(sizing.height)
}

private fun Balloon.Builder.applyAnimation(animation: LabAnimationConfig) {
  setBalloonAnimation(animation.animation)
  setCircularDuration(animation.circularDurationMillis)
  setBalloonHighlightAnimation(animation.highlight, animation.highlightStartDelayMillis)
  setBalloonRotationAnimation(
    BalloonRotateAnimation(
      direction = animation.rotateDirection,
      turns = animation.rotateTurns,
      loops = if (animation.rotateLoopsInfinite) {
        BalloonRotateAnimation.INFINITE
      } else {
        animation.rotateLoops
      },
      speedMillis = animation.rotateSpeedMillis,
      degreeX = animation.rotateDegreeX,
      degreeZ = animation.rotateDegreeZ,
    ),
  )
  setBalloonOverlayAnimation(animation.overlayAnimation)
}

private fun Balloon.Builder.applyOverlay(overlay: LabOverlayConfig) {
  setIsVisibleOverlay(overlay.isVisible)
  setOverlayColor(overlay.color)
  setOverlayShape(overlay.toOverlayShape())
  if (overlay.paddingMode == LabInsetMode.PER_SIDE) {
    setOverlayPadding(
      start = overlay.padding.start,
      top = overlay.padding.top,
      end = overlay.padding.end,
      bottom = overlay.padding.bottom,
    )
  } else {
    setOverlayPadding(overlay.padding.uniform)
  }
  setOverlayPaddingColor(overlay.paddingColor)
}

private fun Balloon.Builder.applyBehaviour(behaviour: LabBehaviourConfig) {
  setDismissWhenClicked(behaviour.dismissWhenClicked)
  setDismissWhenTouchMargin(behaviour.dismissWhenTouchMargin)
  setDismissWhenShowAgain(behaviour.dismissWhenShowAgain)
  setDismissWhenOverlayClicked(behaviour.dismissWhenOverlayClicked)
  setDismissWhenBackPressed(behaviour.dismissWhenBackPressed)
  setAutoDismissDuration(behaviour.autoDismissMillis)
  setDismissWhenTouchOutside(behaviour.dismissWhenTouchOutside)
  setFocusable(behaviour.focusable)
}

/** Maps the flattened [LabOverlayShapeKind] picker back onto the sealed library type. */
internal fun LabOverlayConfig.toOverlayShape(): BalloonOverlayShape = when (shape) {
  LabOverlayShapeKind.EMPTY -> BalloonOverlayShape.Empty
  LabOverlayShapeKind.RECT -> BalloonOverlayShape.Rect
  LabOverlayShapeKind.OVAL -> BalloonOverlayShape.Oval
  LabOverlayShapeKind.CIRCLE_AUTO -> BalloonOverlayShape.Circle()
  LabOverlayShapeKind.CIRCLE_RADIUS -> BalloonOverlayShape.Circle(circleRadius)
  LabOverlayShapeKind.ROUND_RECT -> BalloonOverlayShape.RoundRect(
    radiusX = roundRectRadiusX,
    radiusY = roundRectRadiusY,
  )
  LabOverlayShapeKind.ROUND_RECT_PER_CORNER -> BalloonOverlayShape.RoundRectPerCorner(
    topStart = cornerTopStart,
    topEnd = cornerTopEnd,
    bottomEnd = cornerBottomEnd,
    bottomStart = cornerBottomStart,
  )
}

/**
 * Names the width spec that actually wins, in the library's own resolution order.
 *
 * Mirrors the `when` in `BalloonContent`: an exact `widthRatio` first, then the min / max
 * ratio pair, then an exact `width`, then a wrap bounded by `minWidth` / `maxWidth`. Without
 * this readout a user who sets three specs at once has no way to tell which one is being
 * honoured, because a losing spec looks exactly like a broken one.
 */
internal fun LabSizingConfig.widthPrecedenceHint(): String = when {
  widthRatio > 0f ->
    "widthRatio wins: exactly ${(widthRatio * 100).roundToInt()}% of the window"
  minWidthRatio > 0f || maxWidthRatio > 0f -> {
    val max = if (maxWidthRatio > 0f) maxWidthRatio else 1f
    "min / max width ratio wins: wraps between " +
      "${(minWidthRatio * 100).roundToInt()}% and ${(max * 100).roundToInt()}% of the window"
  }
  useWidth -> "width wins: exactly ${width.value.roundToInt()}dp, capped at the window"
  useMinWidth || useMaxWidth -> {
    val min = if (useMinWidth) "${minWidth.value.roundToInt()}dp" else "0dp"
    val max = if (useMaxWidth) "${maxWidth.value.roundToInt()}dp" else "the window"
    "wraps content between $min and $max"
  }
  else -> "wraps content, bounded only by the window"
}
