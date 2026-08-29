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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The lab's own dark palette.
 *
 * Kept separate from the Material colours so the control chrome never competes with whatever
 * colours the user picks for the balloon.
 */
internal object LabPalette {
  val Background: Color = Color(0xFF141418)
  val Surface: Color = Color(0xFF1E1E25)
  val SurfaceHigh: Color = Color(0xFF2A2A33)
  val Accent: Color = Color(0xFF57A8D8)
  val OnSurface: Color = Color(0xFFE9E9F0)
  val OnSurfaceMuted: Color = Color(0xFF9494A6)
  val Outline: Color = Color(0xFF3A3A46)
  val Anchor: Color = Color(0xFFC51162)
  val Stage: Color = Color(0xFF0E0E12)
}

/** A named colour offered by [LabColorRow]. */
internal data class LabColorOption(val name: String, val color: Color)

/** Body / arrow fills, ordered light to dark so the row reads as a ramp. */
internal val LabFillColors: List<LabColorOption> = listOf(
  LabColorOption("Black", Color(0xFF000000)),
  LabColorOption("Slate", Color(0xFF2B292B)),
  LabColorOption("Blue", Color(0xFF2196F3)),
  LabColorOption("Sky", Color(0xFF57A8D8)),
  LabColorOption("Pink", Color(0xFFC51162)),
  LabColorOption("Purple", Color(0xFF9C27B0)),
  LabColorOption("Teal", Color(0xFF009688)),
  LabColorOption("Orange", Color(0xFFFF5722)),
  LabColorOption("Amber", Color(0xFFFFC107)),
  LabColorOption("White", Color(0xFFF8F8F8)),
)

/** Fills plus the "inherit the background" sentinel the arrow colour understands. */
internal val LabArrowColors: List<LabColorOption> =
  listOf(LabColorOption("Inherit", Color.Unspecified)) + LabFillColors

/** Fills plus the sentinel that disables the border regardless of its thickness. */
internal val LabBorderColors: List<LabColorOption> =
  listOf(LabColorOption("None", Color.Unspecified)) + LabFillColors

/** Scrim colours; the alpha steps are the point, so they are spelled out. */
internal val LabOverlayColors: List<LabColorOption> = listOf(
  LabColorOption("Clear", Color.Transparent),
  LabColorOption("Black 25", Color(0x40000000)),
  LabColorOption("Black 50", Color(0x80000000)),
  LabColorOption("Black 75", Color(0xBF000000)),
  LabColorOption("Black 95", Color(0xF2000000)),
  LabColorOption("Navy 70", Color(0xB3001B3D)),
  LabColorOption("Plum 70", Color(0xB33A0A3A)),
)

/** Overlay padding band fills, including the transparent default. */
internal val LabOverlayPaddingColors: List<LabColorOption> = listOf(
  LabColorOption("None", Color.Unspecified),
  LabColorOption("White", Color(0xFFFFFFFF)),
  LabColorOption("White 40", Color(0x66FFFFFF)),
  LabColorOption("Sky", Color(0xFF57A8D8)),
  LabColorOption("Amber", Color(0xFFFFC107)),
  LabColorOption("Pink", Color(0xFFC51162)),
)

/**
 * A collapsible group of controls.
 *
 * The lab exposes more knobs than fit on a phone screen at once, so every group starts
 * collapsed except the one the caller opens; the caller owns [expanded] so that state
 * survives the section being scrolled out of view.
 */
@Composable
internal fun LabSection(
  title: String,
  summary: String,
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  content: @Composable ColumnScope.() -> Unit,
) {
  val rotation by animateFloatAsState(if (expanded) 0f else -90f, label = "labSectionChevron")
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(LabPalette.Surface)
      .border(1.dp, LabPalette.Outline, RoundedCornerShape(14.dp)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onExpandedChange(!expanded) }
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Canvas(modifier = Modifier.size(12.dp).rotate(rotation)) {
        val chevron = Path().apply {
          moveTo(0f, size.height * 0.3f)
          lineTo(size.width, size.height * 0.3f)
          lineTo(size.width / 2f, size.height * 0.78f)
          close()
        }
        drawPath(chevron, LabPalette.Accent)
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = LabPalette.OnSurface,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Text(text = summary, color = LabPalette.OnSurfaceMuted, fontSize = 11.sp)
      }
    }
    if (expanded) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content,
      )
    }
  }
}

/** A muted explanatory line, used where a control's effect is not self evident. */
@Composable
internal fun LabHint(text: String) {
  Text(
    text = text,
    color = LabPalette.OnSurfaceMuted,
    fontSize = 11.sp,
    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
  )
}

/** A small heading that groups related controls inside a section. */
@Composable
internal fun LabGroupLabel(text: String) {
  Text(
    text = text.uppercase(),
    color = LabPalette.Accent,
    fontSize = 10.sp,
    fontWeight = FontWeight.SemiBold,
    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
  )
}

/** A boolean knob: label on the left, switch on the right, optional explanation below. */
@Composable
internal fun LabSwitchRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  hint: String? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = label, color = LabPalette.OnSurface, fontSize = 13.sp)
      if (hint != null) {
        Text(text = hint, color = LabPalette.OnSurfaceMuted, fontSize = 11.sp)
      }
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

/**
 * The slider every typed slider below delegates to: a label, the current value rendered by
 * the caller, and a continuous track.
 *
 * The track is deliberately continuous (`steps = 0`) even for integer knobs, because a
 * stepped Material slider draws one tick per step and a 0..3000ms range would paint three
 * thousand of them. Quantisation happens in the caller's `onValueChange` instead.
 */
@Composable
internal fun LabSlider(
  label: String,
  valueText: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  enabled: Boolean = true,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = label,
        color = if (enabled) LabPalette.OnSurface else LabPalette.OnSurfaceMuted,
        fontSize = 13.sp,
        modifier = Modifier.weight(1f),
      )
      Text(
        text = valueText,
        color = if (enabled) LabPalette.Accent else LabPalette.OnSurfaceMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
      )
    }
    Slider(
      value = value.coerceIn(valueRange.start, valueRange.endInclusive),
      onValueChange = onValueChange,
      valueRange = valueRange,
      enabled = enabled,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

/** A [Dp] knob snapped to whole density independent pixels. */
@Composable
internal fun LabDpSlider(
  label: String,
  value: Dp,
  range: IntRange,
  onValueChange: (Dp) -> Unit,
  enabled: Boolean = true,
) {
  LabSlider(
    label = label,
    valueText = "${value.value.roundToInt()}dp",
    value = value.value,
    valueRange = range.first.toFloat()..range.last.toFloat(),
    onValueChange = { onValueChange(it.roundToInt().dp) },
    enabled = enabled,
  )
}

/** An [Int] knob, with an optional unit suffix such as `ms` or `deg`. */
@Composable
internal fun LabIntSlider(
  label: String,
  value: Int,
  range: IntRange,
  onValueChange: (Int) -> Unit,
  unit: String = "",
  enabled: Boolean = true,
) {
  LabSlider(
    label = label,
    valueText = "$value$unit",
    value = value.toFloat(),
    valueRange = range.first.toFloat()..range.last.toFloat(),
    onValueChange = { onValueChange(it.roundToInt()) },
    enabled = enabled,
  )
}

/** A fractional knob rendered with two decimals, for the `0f..1f` style ratios. */
@Composable
internal fun LabFloatSlider(
  label: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  enabled: Boolean = true,
) {
  LabSlider(
    label = label,
    valueText = value.formatFraction(),
    value = value,
    valueRange = range,
    // Snapped to hundredths so the readout and the value can never disagree.
    onValueChange = { onValueChange((it * 100f).roundToInt() / 100f) },
    enabled = enabled,
  )
}

/** A wrapping row of chips, one per option, with the selected one filled. */
@Composable
internal fun <T> LabOptionPicker(
  label: String,
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  optionLabel: (T) -> String,
  enabled: Boolean = true,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Text(text = label, color = LabPalette.OnSurface, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(6.dp))
    LabFlowRow {
      options.forEach { option ->
        LabChip(
          text = optionLabel(option),
          selected = option == selected,
          enabled = enabled,
          onClick = { onSelect(option) },
        )
      }
    }
  }
}

/** A wrapping row of colour swatches; the selected one carries a ring. */
@Composable
internal fun LabColorRow(
  label: String,
  options: List<LabColorOption>,
  selected: Color,
  onSelect: (Color) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Text(text = label, color = LabPalette.OnSurface, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(6.dp))
    LabFlowRow {
      options.forEach { option ->
        LabColorSwatch(
          option = option,
          selected = option.color == selected,
          onClick = { onSelect(option.color) },
        )
      }
    }
  }
}

/** A tappable chip, also used on its own for the stage's actions. */
@Composable
internal fun LabChip(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  enabled: Boolean = true,
) {
  val shape = RoundedCornerShape(9.dp)
  val outline = when {
    !enabled -> LabPalette.Outline
    selected -> LabPalette.Accent
    else -> LabPalette.Outline
  }
  Box(
    modifier = Modifier
      .clip(shape)
      .background(if (selected) LabPalette.Accent else LabPalette.SurfaceHigh)
      .border(1.dp, outline, shape)
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 7.dp),
  ) {
    Text(
      text = text,
      color = when {
        !enabled -> LabPalette.OnSurfaceMuted
        selected -> Color(0xFF10151A)
        else -> LabPalette.OnSurface
      },
      fontSize = 12.sp,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}

/**
 * One colour swatch.
 *
 * The swatch is painted over a light-to-dark gradient rather than over the section
 * background, so a half transparent scrim colour reads as half transparent instead of just
 * looking like a darker solid.
 */
@Composable
private fun LabColorSwatch(
  option: LabColorOption,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val shape = RoundedCornerShape(8.dp)
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(46.dp).clickable(onClick = onClick),
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(shape)
        .background(Brush.linearGradient(listOf(Color(0xFFDDDDDD), Color(0xFF33333B))))
        .background(if (option.color.isSpecified) option.color else Color.Transparent)
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) LabPalette.Accent else LabPalette.Outline,
          shape = shape,
        ),
    ) {
      if (!option.color.isSpecified) {
        // No fill to show, so mark the sentinel with a slash instead of an empty box.
        Canvas(modifier = Modifier.size(34.dp).padding(9.dp)) {
          drawLine(
            color = Color(0xFFE45C5C),
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
          )
        }
      }
    }
    Text(
      text = option.name,
      color = if (selected) LabPalette.Accent else LabPalette.OnSurfaceMuted,
      fontSize = 9.sp,
    )
  }
}

/**
 * A minimal wrapping row.
 *
 * `FlowRow` is still experimental in the foundation version this sample builds against, and
 * a horizontally scrolling row would hide options on a phone, so the lab measures its own:
 * every child is placed left to right until the next one would overflow, then a new line
 * starts. That is all the pickers need, and it behaves the same on a 360dp phone and a
 * 1600dp desktop window.
 */
@Composable
internal fun LabFlowRow(
  modifier: Modifier = Modifier,
  horizontalSpacing: Dp = 6.dp,
  verticalSpacing: Dp = 6.dp,
  content: @Composable () -> Unit,
) {
  Layout(modifier = modifier, content = content) { measurables, constraints ->
    val hGap = horizontalSpacing.roundToPx()
    val vGap = verticalSpacing.roundToPx()
    val lineLimit = constraints.maxWidth
    val itemConstraints = Constraints(maxWidth = lineLimit)

    val lines = mutableListOf<MutableList<Placeable>>()
    var line = mutableListOf<Placeable>()
    var lineWidth = 0
    measurables.forEach { measurable ->
      val placeable = measurable.measure(itemConstraints)
      if (line.isNotEmpty() && lineWidth + hGap + placeable.width > lineLimit) {
        lines += line
        line = mutableListOf()
        lineWidth = 0
      }
      lineWidth += (if (line.isEmpty()) 0 else hGap) + placeable.width
      line += placeable
    }
    if (line.isNotEmpty()) lines += line

    val lineHeights = lines.map { row -> row.maxOf { it.height } }
    val height = lineHeights.sum() + vGap * (lines.size - 1).coerceAtLeast(0)
    val width = if (constraints.hasBoundedWidth) {
      lineLimit
    } else {
      lines.maxOfOrNull { row -> row.sumOf { it.width } + hGap * (row.size - 1) } ?: 0
    }

    layout(width, height.coerceAtMost(constraints.maxHeight)) {
      var y = 0
      lines.forEachIndexed { index, row ->
        var x = 0
        row.forEach { placeable ->
          placeable.placeRelative(x, y)
          x += placeable.width + hGap
        }
        y += lineHeights[index] + vGap
      }
    }
  }
}

/** The one icon the lab draws itself, transcribed from `Icons.AutoMirrored.Filled.ArrowBack`. */
internal object LabIcons {
  val ArrowBack: ImageVector by lazy {
    ImageVector.Builder(
      name = "ArrowBack",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
      autoMirror = true,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(20.0f, 11.0f)
        horizontalLineTo(7.83f)
        lineToRelative(5.59f, -5.59f)
        lineTo(12.0f, 4.0f)
        lineToRelative(-8.0f, 8.0f)
        lineToRelative(8.0f, 8.0f)
        lineToRelative(1.41f, -1.41f)
        lineTo(7.83f, 13.0f)
        horizontalLineTo(20.0f)
        verticalLineToRelative(-2.0f)
        close()
      }
    }.build()
  }
}

/**
 * Renders a non-negative fraction with two decimals.
 *
 * `String.format` is a `java.*` API and is therefore unavailable in common code, so the
 * rounding is done by hand.
 */
internal fun Float.formatFraction(): String {
  val hundredths = (abs(this) * 100f).roundToInt()
  val whole = hundredths / 100
  val rest = hundredths % 100
  val restText = if (rest < 10) "0$rest" else "$rest"
  return "$whole.$restText"
}
