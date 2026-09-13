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

package com.skydoves.balloon.sample.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.rememberBalloonBuilder
import com.skydoves.balloon.rememberBalloonState
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.liquidGlass
import com.skydoves.cloudy.sky

internal val Backdrop = Color(0xFF0E1013)
private val GlassShape = RoundedCornerShape(20.dp)
private val BarShape = RoundedCornerShape(16.dp)
private val White = Color.White

/** One tile in the grid, and the detail the tooltip shows for it. */
internal data class GalleryItem(
  val title: String,
  val meta: String,
  val detail: String,
  val start: Color,
  val end: Color,
)

internal val GalleryItems: List<GalleryItem> = listOf(
  GalleryItem(
    "Aurora",
    "4032 x 3024  ·  6.1 MB",
    "Shot at ISO 800, f/1.8, 1/60s",
    Color(0xFFFF5F6D),
    Color(0xFFFFC371),
  ),
  GalleryItem(
    "Harbour",
    "3024 x 4032  ·  4.4 MB",
    "Shot at ISO 200, f/2.8, 1/250s",
    Color(0xFF2E3192),
    Color(0xFF1BFFFF),
  ),
  GalleryItem(
    "Dune",
    "4032 x 3024  ·  5.2 MB",
    "Shot at ISO 100, f/8.0, 1/500s",
    Color(0xFFF7971E),
    Color(0xFFFFD200),
  ),
  GalleryItem(
    "Fern",
    "3024 x 3024  ·  3.8 MB",
    "Shot at ISO 400, f/2.0, 1/125s",
    Color(0xFF11998E),
    Color(0xFF38EF7D),
  ),
  GalleryItem(
    "Neon",
    "4032 x 2268  ·  7.9 MB",
    "Shot at ISO 1600, f/1.4, 1/30s",
    Color(0xFFB621FE),
    Color(0xFF1FD1F9),
  ),
  GalleryItem(
    "Tide",
    "3024 x 4032  ·  4.9 MB",
    "Shot at ISO 320, f/4.0, 1/200s",
    Color(0xFFEE0979),
    Color(0xFFFF6A00),
  ),
  GalleryItem(
    "Basalt",
    "4032 x 3024  ·  6.7 MB",
    "Shot at ISO 250, f/5.6, 1/160s",
    Color(0xFF373B44),
    Color(0xFF4286F4),
  ),
  GalleryItem(
    "Pollen",
    "3024 x 3024  ·  3.1 MB",
    "Shot at ISO 125, f/3.2, 1/400s",
    Color(0xFFF12711),
    Color(0xFFF5AF19),
  ),
)

@Composable
internal fun GlassTopBar(onBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 14.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = 14.dp,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(White.copy(alpha = 0.10f))
        .clickable(onClick = onBack),
      contentAlignment = Alignment.Center,
    ) { Text("<", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    Spacer(Modifier.width(12.dp))
    Column {
      Text("Glass tooltips", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
      Text(
        text = "Tap a tile. The tooltip refracts the grid instead of hiding it.",
        color = White.copy(alpha = 0.55f),
        fontSize = 12.sp,
      )
    }
  }
}

/**
 * A grid tile that owns its own balloon.
 *
 * The anchor is the tile, so the tooltip lands over its neighbours, which is exactly where an
 * opaque tooltip would cost you the comparison you were making.
 */
@Composable
internal fun GalleryCard(
  item: GalleryItem,
  sky: Sky,
  blurRadius: Int,
  lensEnabled: Boolean,
) {
  val style = rememberBalloonBuilder(lensEnabled to blurRadius) {
    // Transparent body: every visible pixel of the panel comes from the glass, not from a
    // fill. The arrow keeps a faint white so it still reads as the same surface.
    setBackgroundColor(White.copy(alpha = 0.13f))
    setArrowSize(width = 18.dp, height = 10.dp)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setCornerRadius(20.dp)
    setPadding(0.dp)
    setMarginHorizontal(12.dp)
    setBalloonAnimation(BalloonAnimation.FADE)
    setDismissWhenTouchOutside(true)
  }
  val state = rememberBalloonState(style)

  Balloon(
    state = state,
    balloonContent = {
      GlassPanel(item = item, sky = sky, blurRadius = blurRadius, lensEnabled = lensEnabled)
    },
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .clip(RoundedCornerShape(18.dp))
        .background(Brush.linearGradient(listOf(item.start, item.end)))
        .clickable { state.toggle() },
      contentAlignment = Alignment.BottomStart,
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(item.title, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(item.meta, color = White.copy(alpha = 0.8f), fontSize = 11.sp)
      }
    }
  }
}

/**
 * The tooltip body.
 *
 * `cloudy(sky = ...)` draws the captured grid back, blurred and clipped to this panel.
 * `liquidGlass` then bends that drawing near the edges, which is what separates a lens from a
 * translucent rectangle. The border is a plain gradient stroke standing in for the bright rim
 * a real piece of glass catches.
 */
@Composable
private fun GlassPanel(
  item: GalleryItem,
  sky: Sky,
  blurRadius: Int,
  lensEnabled: Boolean,
) {
  val width = 250.dp
  val height = 132.dp
  val density = LocalDensity.current
  val lensSize = with(density) { Size(width.toPx(), height.toPx()) }
  val lensCenter = Offset(lensSize.width / 2f, lensSize.height / 2f)

  Box(
    modifier = Modifier
      .size(width, height)
      .cloudy(
        sky = sky,
        radius = blurRadius,
        shape = GlassShape,
        // A tooltip has to stay readable over whatever it lands on, and this grid runs from
        // near white to saturated red. A slight dark tint keeps the white text legible
        // without turning the panel back into an opaque box.
        tint = Backdrop.copy(alpha = 0.34f),
      )
      .then(
        if (lensEnabled) {
          Modifier.liquidGlass(
            lensCenter = lensCenter,
            lensSize = lensSize,
            cornerRadius = with(density) { 20.dp.toPx() },
            refraction = 0.22f,
            curve = 0.30f,
            dispersion = 0.04f,
            saturation = 1.15f,
            contrast = 1.05f,
            edge = 0.25f,
          )
        } else {
          Modifier
        },
      )
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(White.copy(alpha = 0.45f), White.copy(alpha = 0.10f)),
        ),
        shape = GlassShape,
      )
      .padding(16.dp),
  ) {
    Column {
      Text(item.title, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
      Spacer(Modifier.height(4.dp))
      Text(item.meta, color = White.copy(alpha = 0.75f), fontSize = 12.sp)
      Spacer(Modifier.height(2.dp))
      Text(item.detail, color = White.copy(alpha = 0.75f), fontSize = 12.sp)
      Spacer(Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassChip("Open")
        GlassChip("Share")
      }
    }
  }
}

@Composable
private fun GlassChip(label: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(White.copy(alpha = 0.18f))
      .padding(horizontal = 12.dp, vertical = 5.dp),
  ) { Text(label, color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
}

/** Enough of a control surface to see what each parameter is actually doing. */
@Composable
internal fun GlassControls(
  sky: Sky,
  lensEnabled: Boolean,
  onLensChange: (Boolean) -> Unit,
  blurRadius: Int,
  onBlurChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp,
        bottom = 16.dp +
          WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
      )
      .clip(BarShape)
      // The same backdrop API the tooltip uses, doing the ordinary job it is usually hired
      // for. Without it this bar sits unreadable on top of the tiles.
      .cloudy(sky = sky, radius = 32, shape = BarShape, tint = Color.Black.copy(alpha = 0.35f))
      .border(1.dp, White.copy(alpha = 0.16f), BarShape)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Toggle("Lens", lensEnabled) { onLensChange(!lensEnabled) }
    Spacer(Modifier.width(4.dp))
    Text("Blur", color = White.copy(alpha = 0.7f), fontSize = 12.sp)
    listOf(0, 14, 28, 44).forEach { r ->
      Toggle(if (r == 0) "off" else "$r", blurRadius == r) { onBlurChange(r) }
    }
  }
}

@Composable
private fun Toggle(label: String, selected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(9.dp))
      .background(if (selected) White.copy(alpha = 0.26f) else White.copy(alpha = 0.07f))
      .clickable(onClick = onClick)
      .padding(horizontal = 11.dp, vertical = 6.dp),
  ) {
    Text(
      text = label,
      color = if (selected) White else White.copy(alpha = 0.65f),
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
    )
  }
}
