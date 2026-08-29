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

import androidx.compose.runtime.Composable
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonCenterAlign
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonOverlayAnimation
import com.skydoves.balloon.BalloonRotateDirection

/** Every arrow knob: geometry, colour, visibility and the two positioning rules. */
@Composable
internal fun LabArrowControls(arrow: LabArrowConfig, onChange: (LabArrowConfig) -> Unit) {
  LabSwitchRow(
    label = "isVisibleArrow",
    checked = arrow.isVisible,
    onCheckedChange = { onChange(arrow.copy(isVisible = it)) },
    hint = "Hiding the arrow also releases the space it reserved, so the body sits flush.",
  )
  LabSwitchRow(
    label = "Link width and height",
    checked = arrow.linkSize,
    onCheckedChange = { onChange(arrow.copy(linkSize = it, height = arrow.width)) },
    hint = "On uses setArrowSize(value); off uses setArrowWidth and setArrowHeight.",
  )
  LabDpSlider(
    label = "arrowWidth",
    value = arrow.width,
    range = 0..48,
    onValueChange = {
      onChange(arrow.copy(width = it, height = if (arrow.linkSize) it else arrow.height))
    },
    enabled = arrow.isVisible,
  )
  LabDpSlider(
    label = "arrowHeight",
    value = arrow.height,
    range = 0..48,
    onValueChange = { onChange(arrow.copy(height = it)) },
    enabled = arrow.isVisible && !arrow.linkSize,
  )
  LabColorRow(
    label = "arrowColor",
    options = LabArrowColors,
    selected = arrow.color,
    onSelect = { onChange(arrow.copy(color = it)) },
  )
  LabGroupLabel("Orientation")
  LabOptionPicker(
    label = "arrowOrientation",
    options = listOf<ArrowOrientation?>(null) + ArrowOrientation.entries,
    selected = arrow.orientation,
    onSelect = { onChange(arrow.copy(orientation = it)) },
    optionLabel = { it?.name ?: "AUTO" },
  )
  LabHint(
    "AUTO leaves the orientation unset, so the edge is derived from the alignment the " +
      "balloon is shown with. There is no setter that puts it back to unset.",
  )
  LabOptionPicker(
    label = "arrowOrientationRules",
    options = ArrowOrientationRules.entries,
    selected = arrow.orientationRules,
    onSelect = { onChange(arrow.copy(orientationRules = it)) },
    optionLabel = { it.name },
  )
  LabHint("ALIGN_FIXED pins the arrow to the named edge even when the balloon flips.")
  LabGroupLabel("Position")
  LabFloatSlider(
    label = "arrowPosition",
    value = arrow.position,
    range = 0f..1f,
    onValueChange = { onChange(arrow.copy(position = it)) },
  )
  LabOptionPicker(
    label = "arrowPositionRules",
    options = ArrowPositionRules.entries,
    selected = arrow.positionRules,
    onSelect = { onChange(arrow.copy(positionRules = it)) },
    optionLabel = { it.name },
  )
  val anchorRule = arrow.positionRules == ArrowPositionRules.ALIGN_ANCHOR
  LabDpSlider(
    label = "arrowAlignAnchorPadding",
    value = arrow.alignAnchorPadding,
    range = 0..48,
    onValueChange = { onChange(arrow.copy(alignAnchorPadding = it)) },
    enabled = anchorRule,
  )
  LabFloatSlider(
    label = "arrowAlignAnchorPaddingRatio",
    value = arrow.alignAnchorPaddingRatio,
    range = 0f..6f,
    onValueChange = { onChange(arrow.copy(alignAnchorPaddingRatio = it)) },
    enabled = anchorRule,
  )
  LabHint(
    "Both only bite under ALIGN_ANCHOR: the arrow is kept " +
      "arrowSize * ratio + padding clear of the balloon's ends. Drag the offsets in " +
      "Placement to push the anchor off centre and watch the clamp take over.",
  )
}

/** The card itself: fill, corners, border, elevation, opacity, padding and margin. */
@Composable
internal fun LabBodyControls(body: LabBodyConfig, onChange: (LabBodyConfig) -> Unit) {
  LabColorRow(
    label = "backgroundColor",
    options = LabFillColors,
    selected = body.backgroundColor,
    onSelect = { onChange(body.copy(backgroundColor = it)) },
  )
  LabDpSlider(
    label = "cornerRadius",
    value = body.cornerRadius,
    range = 0..40,
    onValueChange = { onChange(body.copy(cornerRadius = it)) },
  )
  LabColorRow(
    label = "borderColor",
    options = LabBorderColors,
    selected = body.borderColor,
    onSelect = { onChange(body.copy(borderColor = it)) },
  )
  LabDpSlider(
    label = "borderThickness",
    value = body.borderThickness,
    range = 0..12,
    onValueChange = { onChange(body.copy(borderThickness = it)) },
  )
  LabHint("The border needs both a colour and a non-zero thickness to draw.")
  LabDpSlider(
    label = "elevation",
    value = body.elevation,
    range = 0..32,
    onValueChange = { onChange(body.copy(elevation = it)) },
  )
  LabHint(
    "Elevation is the inset reserved on the axis across from the arrow, and every width " +
      "spec is measured against the popup box that inset belongs to.",
  )
  LabFloatSlider(
    label = "alpha",
    value = body.alpha,
    range = 0f..1f,
    onValueChange = { onChange(body.copy(alpha = it)) },
  )
  LabGroupLabel("Padding")
  LabInsetControls(
    mode = body.paddingMode,
    modes = LabInsetMode.entries,
    insets = body.padding,
    range = 0..48,
    onModeChange = { onChange(body.copy(paddingMode = it)) },
    onInsetsChange = { onChange(body.copy(padding = it)) },
  )
  LabGroupLabel("Margin")
  LabInsetControls(
    mode = body.marginMode,
    modes = LabInsetMode.entries,
    insets = body.margin,
    range = 0..64,
    onModeChange = { onChange(body.copy(marginMode = it)) },
    onInsetsChange = { onChange(body.copy(margin = it)) },
  )
  LabHint("Margin keeps the balloon off the window edges and shrinks the body with it.")
}

/** Width specs, their bounds and the fixed height, plus a readout of which one wins. */
@Composable
internal fun LabSizingControls(sizing: LabSizingConfig, onChange: (LabSizingConfig) -> Unit) {
  LabHint(sizing.widthPrecedenceHint())
  LabFlowRow {
    LabChip(
      text = "Wrap content",
      selected = !sizing.useWidth && sizing.widthRatio == 0f &&
        sizing.minWidthRatio == 0f && sizing.maxWidthRatio == 0f &&
        !sizing.useMinWidth && !sizing.useMaxWidth,
      onClick = {
        onChange(
          sizing.copy(
            useWidth = false,
            widthRatio = 0f,
            minWidthRatio = 0f,
            maxWidthRatio = 0f,
            useMinWidth = false,
            useMaxWidth = false,
          ),
        )
      },
    )
  }
  LabGroupLabel("Exact width")
  LabSwitchRow(
    label = "setWidth",
    checked = sizing.useWidth,
    onCheckedChange = { onChange(sizing.copy(useWidth = it)) },
  )
  LabDpSlider(
    label = "width",
    value = sizing.width,
    range = 40..420,
    onValueChange = { onChange(sizing.copy(width = it)) },
    enabled = sizing.useWidth,
  )
  LabFloatSlider(
    label = "widthRatio",
    value = sizing.widthRatio,
    range = 0f..1f,
    onValueChange = { onChange(sizing.copy(widthRatio = it)) },
  )
  LabHint("0.00 disables widthRatio. Anything above it outranks every other width spec.")
  LabGroupLabel("Ratio bounds")
  LabFloatSlider(
    label = "minWidthRatio",
    value = sizing.minWidthRatio,
    range = 0f..1f,
    onValueChange = { onChange(sizing.copy(minWidthRatio = it)) },
  )
  LabFloatSlider(
    label = "maxWidthRatio",
    value = sizing.maxWidthRatio,
    range = 0f..1f,
    onValueChange = { onChange(sizing.copy(maxWidthRatio = it)) },
  )
  LabHint("Either ratio bound outranks setWidth; an unset max behaves as 1.00.")
  LabGroupLabel("Dp bounds")
  LabSwitchRow(
    label = "setMinWidth",
    checked = sizing.useMinWidth,
    onCheckedChange = { onChange(sizing.copy(useMinWidth = it)) },
  )
  LabDpSlider(
    label = "minWidth",
    value = sizing.minWidth,
    range = 0..420,
    onValueChange = { onChange(sizing.copy(minWidth = it)) },
    enabled = sizing.useMinWidth,
  )
  LabSwitchRow(
    label = "setMaxWidth",
    checked = sizing.useMaxWidth,
    onCheckedChange = { onChange(sizing.copy(useMaxWidth = it)) },
  )
  LabDpSlider(
    label = "maxWidth",
    value = sizing.maxWidth,
    range = 40..420,
    onValueChange = { onChange(sizing.copy(maxWidth = it)) },
    enabled = sizing.useMaxWidth,
  )
  LabGroupLabel("Height")
  LabSwitchRow(
    label = "setHeight",
    checked = sizing.useHeight,
    onCheckedChange = { onChange(sizing.copy(useHeight = it)) },
    hint = "There is no height ratio, matching the original API.",
  )
  LabDpSlider(
    label = "height",
    value = sizing.height,
    range = 40..320,
    onValueChange = { onChange(sizing.copy(height = it)) },
    enabled = sizing.useHeight,
  )
}

/** Which show call presents the balloon, and the manual offsets handed to it. */
@Composable
internal fun LabPlacementControls(
  placement: LabPlacementConfig,
  onChange: (LabPlacementConfig) -> Unit,
) {
  LabOptionPicker(
    label = "Show method",
    options = LabPlacement.entries,
    selected = placement.placement,
    onSelect = { onChange(placement.copy(placement = it)) },
    optionLabel = { it.label },
  )
  LabHint(
    "As drop down aligns the leading edges instead of centring. Center overlay is " +
      "show(BalloonAlign.CENTER), which sits on top of the anchor and has no arrow edge " +
      "to point at.",
  )
  LabOptionPicker(
    label = "BalloonCenterAlign",
    options = BalloonCenterAlign.entries,
    selected = placement.centerAlign,
    onSelect = { onChange(placement.copy(centerAlign = it)) },
    optionLabel = { it.name },
    enabled = placement.placement == LabPlacement.AT_CENTER,
  )
  LabHint("Only used by showAtCenter, which places the balloon against the anchor's centre.")
  LabDpSlider(
    label = "xOffset",
    value = placement.xOffset,
    range = -160..160,
    onValueChange = { onChange(placement.copy(xOffset = it)) },
  )
  LabDpSlider(
    label = "yOffset",
    value = placement.yOffset,
    range = -160..160,
    onValueChange = { onChange(placement.copy(yOffset = it)) },
  )
}

/** Enter and exit transitions, the looping highlight, and the rotation it can run. */
@Composable
internal fun LabAnimationControls(
  animation: LabAnimationConfig,
  onChange: (LabAnimationConfig) -> Unit,
) {
  LabOptionPicker(
    label = "BalloonAnimation",
    options = BalloonAnimation.entries,
    selected = animation.animation,
    onSelect = { onChange(animation.copy(animation = it)) },
    optionLabel = { it.name },
  )
  LabIntSlider(
    label = "circularDuration",
    value = animation.circularDurationMillis.toInt(),
    range = 0..2000,
    onValueChange = { onChange(animation.copy(circularDurationMillis = it.toLong())) },
    unit = "ms",
    enabled = animation.animation == BalloonAnimation.CIRCULAR,
  )
  LabHint("Picking CIRCULAR also turns focusable off, exactly as the original setter does.")
  LabGroupLabel("Highlight")
  LabOptionPicker(
    label = "BalloonHighlightAnimation",
    options = BalloonHighlightAnimation.entries,
    selected = animation.highlight,
    onSelect = { onChange(animation.copy(highlight = it)) },
    optionLabel = { it.name },
  )
  LabIntSlider(
    label = "highlightAnimationStartDelay",
    value = animation.highlightStartDelayMillis.toInt(),
    range = 0..3000,
    onValueChange = { onChange(animation.copy(highlightStartDelayMillis = it.toLong())) },
    unit = "ms",
    enabled = animation.highlight != BalloonHighlightAnimation.NONE,
  )
  LabGroupLabel("Rotation")
  val rotating = animation.highlight == BalloonHighlightAnimation.ROTATE
  LabHint("These drive BalloonRotateAnimation, which only runs under the ROTATE highlight.")
  LabOptionPicker(
    label = "direction",
    options = BalloonRotateDirection.entries,
    selected = animation.rotateDirection,
    onSelect = { onChange(animation.copy(rotateDirection = it)) },
    optionLabel = { it.name },
    enabled = rotating,
  )
  LabIntSlider(
    label = "turns",
    value = animation.rotateTurns,
    range = 1..5,
    onValueChange = { onChange(animation.copy(rotateTurns = it)) },
    enabled = rotating,
  )
  LabSwitchRow(
    label = "loops = INFINITE",
    checked = animation.rotateLoopsInfinite,
    onCheckedChange = { onChange(animation.copy(rotateLoopsInfinite = it)) },
  )
  LabIntSlider(
    label = "loops",
    value = animation.rotateLoops,
    range = 1..8,
    onValueChange = { onChange(animation.copy(rotateLoops = it)) },
    enabled = rotating && !animation.rotateLoopsInfinite,
  )
  LabIntSlider(
    label = "speed",
    value = animation.rotateSpeedMillis,
    range = 300..6000,
    onValueChange = { onChange(animation.copy(rotateSpeedMillis = it)) },
    unit = "ms",
    enabled = rotating,
  )
  LabIntSlider(
    label = "degreeX",
    value = animation.rotateDegreeX,
    range = 0..360,
    onValueChange = { onChange(animation.copy(rotateDegreeX = it)) },
    unit = "deg",
    enabled = rotating,
  )
  LabIntSlider(
    label = "degreeZ",
    value = animation.rotateDegreeZ,
    range = 0..360,
    onValueChange = { onChange(animation.copy(rotateDegreeZ = it)) },
    unit = "deg",
    enabled = rotating,
  )
  LabGroupLabel("Overlay")
  LabOptionPicker(
    label = "BalloonOverlayAnimation",
    options = BalloonOverlayAnimation.entries,
    selected = animation.overlayAnimation,
    onSelect = { onChange(animation.copy(overlayAnimation = it)) },
    optionLabel = { it.name },
  )
}

/** The scrim and the cut-out it leaves over the anchor. */
@Composable
internal fun LabOverlayControls(overlay: LabOverlayConfig, onChange: (LabOverlayConfig) -> Unit) {
  LabSwitchRow(
    label = "isVisibleOverlay",
    checked = overlay.isVisible,
    onCheckedChange = { onChange(overlay.copy(isVisible = it)) },
    hint = "The scrim covers the BalloonHost, which here is the stage above the controls.",
  )
  LabColorRow(
    label = "overlayColor",
    options = LabOverlayColors,
    selected = overlay.color,
    onSelect = { onChange(overlay.copy(color = it)) },
  )
  LabOptionPicker(
    label = "BalloonOverlayShape",
    options = LabOverlayShapeKind.entries,
    selected = overlay.shape,
    onSelect = { onChange(overlay.copy(shape = it)) },
    optionLabel = { it.label },
  )
  LabHint(
    "Empty draws no hole at all. Circle without a radius falls back to half the anchor's " +
      "longer side; with one it does not.",
  )
  LabDpSlider(
    label = "Circle radius",
    value = overlay.circleRadius,
    range = 0..160,
    onValueChange = { onChange(overlay.copy(circleRadius = it)) },
    enabled = overlay.shape == LabOverlayShapeKind.CIRCLE_RADIUS,
  )
  val roundRect = overlay.shape == LabOverlayShapeKind.ROUND_RECT
  LabDpSlider(
    label = "RoundRect radiusX",
    value = overlay.roundRectRadiusX,
    range = 0..80,
    onValueChange = { onChange(overlay.copy(roundRectRadiusX = it)) },
    enabled = roundRect,
  )
  LabDpSlider(
    label = "RoundRect radiusY",
    value = overlay.roundRectRadiusY,
    range = 0..80,
    onValueChange = { onChange(overlay.copy(roundRectRadiusY = it)) },
    enabled = roundRect,
  )
  val perCorner = overlay.shape == LabOverlayShapeKind.ROUND_RECT_PER_CORNER
  LabDpSlider(
    label = "topStart",
    value = overlay.cornerTopStart,
    range = 0..80,
    onValueChange = { onChange(overlay.copy(cornerTopStart = it)) },
    enabled = perCorner,
  )
  LabDpSlider(
    label = "topEnd",
    value = overlay.cornerTopEnd,
    range = 0..80,
    onValueChange = { onChange(overlay.copy(cornerTopEnd = it)) },
    enabled = perCorner,
  )
  LabDpSlider(
    label = "bottomEnd",
    value = overlay.cornerBottomEnd,
    range = 0..80,
    onValueChange = { onChange(overlay.copy(cornerBottomEnd = it)) },
    enabled = perCorner,
  )
  LabDpSlider(
    label = "bottomStart",
    value = overlay.cornerBottomStart,
    range = 0..80,
    onValueChange = { onChange(overlay.copy(cornerBottomStart = it)) },
    enabled = perCorner,
  )
  LabGroupLabel("Overlay padding")
  LabInsetControls(
    // The overlay only ships a uniform and a four-argument setter, so no H / V mode here.
    mode = overlay.paddingMode,
    modes = listOf(LabInsetMode.UNIFORM, LabInsetMode.PER_SIDE),
    insets = overlay.padding,
    range = 0..64,
    onModeChange = { onChange(overlay.copy(paddingMode = it)) },
    onInsetsChange = { onChange(overlay.copy(padding = it)) },
  )
  LabColorRow(
    label = "overlayPaddingColor",
    options = LabOverlayPaddingColors,
    selected = overlay.paddingColor,
    onSelect = { onChange(overlay.copy(paddingColor = it)) },
  )
  LabHint("The padding colour fills the band the padding opens around the anchor.")
}

/** Dismiss rules, focus, and the auto dismiss timer. */
@Composable
internal fun LabBehaviourControls(
  behaviour: LabBehaviourConfig,
  onChange: (LabBehaviourConfig) -> Unit,
) {
  LabSwitchRow(
    label = "dismissWhenClicked",
    checked = behaviour.dismissWhenClicked,
    onCheckedChange = { onChange(behaviour.copy(dismissWhenClicked = it)) },
    hint = "Tapping the balloon body closes it.",
  )
  LabSwitchRow(
    label = "dismissWhenTouchOutside",
    checked = behaviour.dismissWhenTouchOutside,
    onCheckedChange = { onChange(behaviour.copy(dismissWhenTouchOutside = it)) },
    hint = "Off in this lab by default so the controls stay one tap away.",
  )
  LabSwitchRow(
    label = "dismissWhenTouchMargin",
    checked = behaviour.dismissWhenTouchMargin,
    onCheckedChange = { onChange(behaviour.copy(dismissWhenTouchMargin = it)) },
    hint = "Taps in the margin band only close the balloon while touch outside is on.",
  )
  LabSwitchRow(
    label = "dismissWhenBackPressed",
    checked = behaviour.dismissWhenBackPressed,
    onCheckedChange = { onChange(behaviour.copy(dismissWhenBackPressed = it)) },
    hint = "Back on Android, Escape on desktop.",
  )
  LabSwitchRow(
    label = "dismissWhenShowAgain",
    checked = behaviour.dismissWhenShowAgain,
    onCheckedChange = { onChange(behaviour.copy(dismissWhenShowAgain = it)) },
    hint = "Showing an already visible balloon closes it instead of re-showing it.",
  )
  LabSwitchRow(
    label = "dismissWhenOverlayClicked",
    checked = behaviour.dismissWhenOverlayClicked,
    onCheckedChange = { onChange(behaviour.copy(dismissWhenOverlayClicked = it)) },
    hint = "Needs the overlay switched on to have anything to click.",
  )
  LabSwitchRow(
    label = "focusable",
    checked = behaviour.focusable,
    onCheckedChange = { onChange(behaviour.copy(focusable = it)) },
    hint = "A focusable popup is touch modal, so it swallows the next tap on the controls.",
  )
  LabIntSlider(
    label = "autoDismissDuration",
    value = behaviour.autoDismissMillis.toInt(),
    range = 0..8000,
    onValueChange = { onChange(behaviour.copy(autoDismissMillis = it.toLong())) },
    unit = "ms",
  )
  LabHint("0ms disables auto dismiss. Any change here re-shows the balloon and restarts it.")
}

/** Which composable the balloon renders, proving the body is entirely caller supplied. */
@Composable
internal fun LabContentControls(preset: LabBodyPreset, onChange: (LabBodyPreset) -> Unit) {
  LabOptionPicker(
    label = "Balloon body",
    options = LabBodyPreset.entries,
    selected = preset,
    onSelect = onChange,
    optionLabel = { it.label },
  )
  LabHint(
    "The body is an ordinary composable slot, so a preset can be a single Text, a row " +
      "with an icon, or a whole card with its own button.",
  )
}

/**
 * The mode picker plus the sliders that mode needs.
 *
 * Shared by padding, margin and overlay padding because all three expose the same shape of
 * setter; [modes] narrows the picker for the overlay, which has no horizontal / vertical
 * overload.
 */
@Composable
private fun LabInsetControls(
  mode: LabInsetMode,
  modes: List<LabInsetMode>,
  insets: LabInsets,
  range: IntRange,
  onModeChange: (LabInsetMode) -> Unit,
  onInsetsChange: (LabInsets) -> Unit,
) {
  LabOptionPicker(
    label = "Mode",
    options = modes,
    selected = mode,
    onSelect = onModeChange,
    optionLabel = { it.label },
  )
  when (mode) {
    LabInsetMode.UNIFORM -> LabDpSlider(
      label = "all sides",
      value = insets.uniform,
      range = range,
      onValueChange = { onInsetsChange(insets.copy(uniform = it)) },
    )
    LabInsetMode.AXIS -> {
      LabDpSlider(
        label = "horizontal",
        value = insets.horizontal,
        range = range,
        onValueChange = { onInsetsChange(insets.copy(horizontal = it)) },
      )
      LabDpSlider(
        label = "vertical",
        value = insets.vertical,
        range = range,
        onValueChange = { onInsetsChange(insets.copy(vertical = it)) },
      )
    }
    LabInsetMode.PER_SIDE -> {
      LabDpSlider(
        label = "start",
        value = insets.start,
        range = range,
        onValueChange = { onInsetsChange(insets.copy(start = it)) },
      )
      LabDpSlider(
        label = "top",
        value = insets.top,
        range = range,
        onValueChange = { onInsetsChange(insets.copy(top = it)) },
      )
      LabDpSlider(
        label = "end",
        value = insets.end,
        range = range,
        onValueChange = { onInsetsChange(insets.copy(end = it)) },
      )
      LabDpSlider(
        label = "bottom",
        value = insets.bottom,
        range = range,
        onValueChange = { onInsetsChange(insets.copy(bottom = it)) },
      )
    }
  }
}
