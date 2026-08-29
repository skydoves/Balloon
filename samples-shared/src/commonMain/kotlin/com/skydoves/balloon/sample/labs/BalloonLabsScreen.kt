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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.balloon.BalloonAlign
import com.skydoves.balloon.BalloonHost
import com.skydoves.balloon.BalloonState
import com.skydoves.balloon.balloon
import com.skydoves.balloon.rememberBalloonState
import com.skydoves.balloon.sample.DemoIcons

/** The collapsible groups the controls pane is built from, in the order they are shown. */
internal enum class LabSectionId(val title: String, val summary: String) {
  ARROW("Arrow", "size, colour, orientation and the two positioning rules"),
  BODY("Body", "fill, corners, border, elevation, alpha, padding, margin"),
  SIZING("Sizing", "width specs, their bounds, height and who wins"),
  PLACEMENT("Placement", "which show call, and the manual offsets"),
  ANIMATION("Animation", "enter and exit, highlight loop, rotation, overlay fade"),
  OVERLAY("Overlay", "scrim colour, cut-out shape, padding band"),
  BEHAVIOUR("Behaviour", "dismiss rules, focus and auto dismiss"),
  CONTENT("Body content", "which composable the balloon renders"),
}

/** Width at which the stage moves beside the controls instead of sitting above them. */
private const val WIDE_LAYOUT_BREAKPOINT_DP = 720

/**
 * An interactive playground that drives every knob the library exposes.
 *
 * The screen is deliberately built as one value-equal [LabsConfig] plus a projection onto a
 * [com.skydoves.balloon.BalloonStyle]: that is what lets a single `LaunchedEffect(config)`
 * re-show the balloon on any change, so the anchor never has to be tapped twice to see the
 * effect of a slider.
 *
 * It is self contained on purpose (its own top bar, its own back affordance) so a host app
 * only has to route to it.
 *
 * @param onBack invoked by the top bar's back affordance.
 */
@Composable
public fun BalloonLabsScreen(onBack: () -> Unit = {}) {
  var config by remember { mutableStateOf(LabsConfig()) }
  var openSections by remember { mutableStateOf(setOf(LabSectionId.ARROW)) }
  var lastEvent by remember { mutableStateOf("Move any control to re-show the balloon") }

  val style = remember(config) { config.toBalloonStyle() }
  val balloonState = rememberBalloonState(style)

  // The re-show below has to dismiss first (see the effect), which would otherwise report a
  // dismissal the user never asked for. This flag lets the listener tell the two apart.
  val programmaticDismiss = remember { mutableStateOf(false) }

  LaunchedEffect(balloonState) {
    balloonState.onBalloonClick = { lastEvent = "onBalloonClick" }
    balloonState.onOverlayClick = { lastEvent = "onOverlayClick" }
    balloonState.onDismiss = { if (!programmaticDismiss.value) lastEvent = "onDismiss" }
  }

  LaunchedEffect(config) {
    // Dismissing first is what makes the re-show unconditional: with dismissWhenShowAgain on,
    // calling show() while visible would close the balloon instead. Both writes land in the
    // same snapshot, so the balloon is never actually seen to disappear.
    programmaticDismiss.value = true
    balloonState.dismiss()
    programmaticDismiss.value = false
    balloonState.showWith(config.placement)
  }

  MaterialTheme(colors = LabColors) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(LabPalette.Background),
    ) {
      LabTopBar(onBack = onBack, onReset = { config = LabsConfig() })
      BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
        val isWide = maxWidth >= WIDE_LAYOUT_BREAKPOINT_DP.dp
        val stage: @Composable (Modifier) -> Unit = { stageModifier ->
          LabStage(
            balloonState = balloonState,
            config = config,
            lastEvent = lastEvent,
            modifier = stageModifier,
          )
        }
        val controls: @Composable (Modifier) -> Unit = { controlsModifier ->
          LabControlsPane(
            config = config,
            onConfigChange = { config = it },
            openSections = openSections,
            onToggleSection = { id ->
              openSections = if (id in openSections) openSections - id else openSections + id
            },
            modifier = controlsModifier,
          )
        }
        if (isWide) {
          Row(modifier = Modifier.fillMaxSize()) {
            stage(Modifier.width(380.dp).fillMaxHeight())
            controls(Modifier.weight(1f).fillMaxHeight())
          }
        } else {
          Column(modifier = Modifier.fillMaxSize()) {
            stage(Modifier.fillMaxWidth().height(230.dp))
            controls(Modifier.weight(1f).fillMaxWidth())
          }
        }
      }
    }
  }
}

/** Material colours for the stock [androidx.compose.material.Slider] / Switch chrome. */
private val LabColors = darkColors(
  primary = LabPalette.Accent,
  primaryVariant = LabPalette.Accent,
  secondary = LabPalette.Accent,
  secondaryVariant = LabPalette.Accent,
  background = LabPalette.Background,
  surface = LabPalette.Surface,
  onPrimary = Color(0xFF0F1418),
  onSecondary = Color(0xFF0F1418),
  onBackground = LabPalette.OnSurface,
  onSurface = LabPalette.OnSurface,
)

@Composable
private fun LabTopBar(onBack: () -> Unit, onReset: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(LabPalette.Surface)
      .padding(
        top = 8.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = 8.dp,
        start = 4.dp,
        end = 14.dp,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(onClick = onBack) {
      Icon(
        imageVector = LabIcons.ArrowBack,
        contentDescription = "Back",
        tint = LabPalette.OnSurface,
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "Balloon Labs",
        color = LabPalette.OnSurface,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Drive every builder setter live",
        color = LabPalette.OnSurfaceMuted,
        fontSize = 11.sp,
      )
    }
    LabChip(text = "Reset", selected = false, onClick = onReset)
  }
}

/**
 * The anchor and the balloon actions.
 *
 * [BalloonHost] wraps only this pane rather than the whole screen on purpose: the overlay
 * scrim fills its host and swallows taps, so hosting the whole screen would put a modal
 * scrim over the very controls the user is trying to move.
 */
@Composable
private fun LabStage(
  balloonState: BalloonState,
  config: LabsConfig,
  lastEvent: String,
  modifier: Modifier,
) {
  Column(modifier = modifier.background(LabPalette.Stage)) {
    BalloonHost(modifier = Modifier.weight(1f).fillMaxWidth()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
          modifier = Modifier
            .size(width = 140.dp, height = 62.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LabPalette.Anchor)
            .balloon(balloonState) {
              LabBalloonBody(
                preset = config.bodyPreset,
                backgroundColor = config.body.backgroundColor,
                onDismissRequest = { balloonState.dismiss() },
              )
            }
            .clickable { balloonState.toggle() },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "ANCHOR",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(LabPalette.Surface)
        .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
      Text(
        text = if (balloonState.isShowing) "showing" else "hidden",
        color = if (balloonState.isShowing) LabPalette.Accent else LabPalette.OnSurfaceMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
      )
      Text(text = lastEvent, color = LabPalette.OnSurfaceMuted, fontSize = 11.sp)
      Spacer(modifier = Modifier.height(6.dp))
      LabFlowRow {
        LabChip(
          text = "Show",
          selected = false,
          onClick = { balloonState.showWith(config.placement) },
        )
        LabChip(text = "Dismiss", selected = false, onClick = { balloonState.dismiss() })
        LabChip(text = "Toggle", selected = false, onClick = { balloonState.toggle() })
      }
    }
  }
}

@Composable
private fun LabControlsPane(
  config: LabsConfig,
  onConfigChange: (LabsConfig) -> Unit,
  openSections: Set<LabSectionId>,
  onToggleSection: (LabSectionId) -> Unit,
  modifier: Modifier,
) {
  Box(modifier = modifier) {
    Column(
      modifier = Modifier
        // Capped so the sliders stay a usable length in a maximised desktop window.
        .align(Alignment.TopCenter)
        .widthIn(max = 700.dp)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      LabSectionId.entries.forEach { id ->
        LabSection(
          title = id.title,
          summary = id.summary,
          expanded = id in openSections,
          onExpandedChange = { onToggleSection(id) },
        ) {
          LabSectionContent(id = id, config = config, onConfigChange = onConfigChange)
        }
      }
      Spacer(
        modifier = Modifier.height(
          24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
      )
    }
  }
}

@Composable
private fun LabSectionContent(
  id: LabSectionId,
  config: LabsConfig,
  onConfigChange: (LabsConfig) -> Unit,
) {
  when (id) {
    LabSectionId.ARROW -> LabArrowControls(config.arrow) {
      onConfigChange(config.copy(arrow = it))
    }
    LabSectionId.BODY -> LabBodyControls(config.body) {
      onConfigChange(config.copy(body = it))
    }
    LabSectionId.SIZING -> LabSizingControls(config.sizing) {
      onConfigChange(config.copy(sizing = it))
    }
    LabSectionId.PLACEMENT -> LabPlacementControls(config.placement) {
      onConfigChange(config.copy(placement = it))
    }
    LabSectionId.ANIMATION -> LabAnimationControls(config.animation) {
      onConfigChange(config.copy(animation = it))
    }
    LabSectionId.OVERLAY -> LabOverlayControls(config.overlay) {
      onConfigChange(config.copy(overlay = it))
    }
    LabSectionId.BEHAVIOUR -> LabBehaviourControls(config.behaviour) {
      onConfigChange(config.copy(behaviour = it))
    }
    LabSectionId.CONTENT -> LabContentControls(config.bodyPreset) {
      onConfigChange(config.copy(bodyPreset = it))
    }
  }
}

/**
 * The balloon body itself.
 *
 * The content colour is derived from the chosen fill rather than fixed, so switching the
 * background to white does not leave white text on white.
 */
@Composable
private fun LabBalloonBody(
  preset: LabBodyPreset,
  backgroundColor: Color,
  onDismissRequest: () -> Unit,
) {
  val contentColor = if (backgroundColor.luminance() > 0.45f) {
    Color(0xFF14141A)
  } else {
    Color.White
  }
  when (preset) {
    LabBodyPreset.TEXT -> Text(
      text = "Every setter, wired to a control.",
      color = contentColor,
      fontSize = 14.sp,
    )

    LabBodyPreset.TEXT_WITH_ICON -> Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = DemoIcons.Edit,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = "Edit your profile", color = contentColor, fontSize = 14.sp)
    }

    LabBodyPreset.RICH_CARD -> Column(modifier = Modifier.widthIn(max = 260.dp)) {
      Text(
        text = "Custom layout",
        color = contentColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "The body is a plain composable slot, so it can hold anything, " +
          "including its own button.",
        color = contentColor.copy(alpha = 0.85f),
        fontSize = 13.sp,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = onDismissRequest,
        colors = ButtonDefaults.buttonColors(
          backgroundColor = contentColor,
          contentColor = backgroundColor,
        ),
      ) {
        Text(text = "Got it", fontSize = 13.sp)
      }
    }
  }
}

/** Routes the lab's placement choice onto the matching `BalloonState` show call. */
private fun BalloonState.showWith(placement: LabPlacementConfig) {
  val x = placement.xOffset
  val y = placement.yOffset
  when (placement.placement) {
    LabPlacement.ALIGN_TOP -> showAlignTop(x, y)
    LabPlacement.ALIGN_BOTTOM -> showAlignBottom(x, y)
    LabPlacement.ALIGN_START -> showAlignStart(x, y)
    LabPlacement.ALIGN_END -> showAlignEnd(x, y)
    LabPlacement.DROP_DOWN -> showAsDropDown(x, y)
    LabPlacement.CENTER_OVERLAY -> show(BalloonAlign.CENTER, x, y)
    LabPlacement.AT_CENTER -> showAtCenter(placement.centerAlign, x, y)
  }
}
