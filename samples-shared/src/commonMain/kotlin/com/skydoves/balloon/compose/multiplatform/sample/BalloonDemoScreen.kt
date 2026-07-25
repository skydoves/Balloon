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

package com.skydoves.balloon.compose.multiplatform.sample

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.balloon.compose.multiplatform.ArrowOrientation
import com.skydoves.balloon.compose.multiplatform.ArrowPositionRules
import com.skydoves.balloon.compose.multiplatform.BalloonAnimation
import com.skydoves.balloon.compose.multiplatform.BalloonHighlightAnimation
import com.skydoves.balloon.compose.multiplatform.BalloonHost
import com.skydoves.balloon.compose.multiplatform.BalloonOverlayShape
import com.skydoves.balloon.compose.multiplatform.BalloonStyle
import com.skydoves.balloon.compose.multiplatform.balloon
import com.skydoves.balloon.compose.multiplatform.rememberBalloonBuilder
import com.skydoves.balloon.compose.multiplatform.rememberBalloonState
import com.skydoves.balloon.compose.multiplatform.sample.resources.Res
import com.skydoves.balloon.compose.multiplatform.sample.resources.sample0
import org.jetbrains.compose.resources.painterResource

// Color definitions
private val Background = Color(0xFF2B292B)
private val SkyBlue = Color(0xFF57A8D8)
private val Pink = Color(0xFFC51162)
private val White93 = Color(0xEDF8F8F8)
private val White70 = Color(0xB2FFFFFF)
private val White56 = Color(0x8EFFFFFF)
private val Overlay = Color(0xBF000000)
private val Purple = Color(0xFF9C27B0)
private val Teal = Color(0xFF009688)
private val Orange = Color(0xFFFF5722)

/**
 * Demo screen exercising the balloon APIs across all Compose Multiplatform targets
 * (Android, iOS, Desktop, Wasm).
 *
 * This is a deliberate 1:1 port of `ComposeActivity` in the Android-only `:app` module —
 * same sections, strings, colours, sizes, icons and balloon configuration — so the two can
 * be screenshot side by side and compared pixel for pixel. Keep them in sync: if you change
 * something here, change it there too, or the comparison stops meaning anything.
 *
 * The only intentional differences, all forced by the platform:
 * - `Toast` becomes the [onMessage] callback.
 * - Material icons are drawn from [DemoIcons] (Compose Multiplatform stopped publishing
 *   `material-icons-extended` after 1.7.x); the path data is copied verbatim from androidx,
 *   so the glyphs are identical.
 * - `BalloonOverlayRoundRect(12f, 12f)` takes raw pixels in the original; the KMP API takes
 *   [androidx.compose.ui.unit.Dp], so the overlay cut-out corners here are 12dp rather than
 *   12px.
 *
 * **NOTE:** This composable is bundled inside `:samples-shared` purely so the demo apps in
 * this repository can share a single implementation. It is not part of the library's API.
 *
 * @param onMessage invoked with a human-readable message when a demo action is triggered
 *   (the KMP stand-in for the original demo's `Toast`s).
 */
@Composable
public fun BalloonDemoScreen(onMessage: (String) -> Unit = {}) {
  // `Modifier.balloon` registers anchors with the nearest host, which is what actually
  // emits the popups. One host wraps the whole screen.
  BalloonHost {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Background),
    ) {
      // Top App Bar with Menu Balloon
      TopAppBar(onMessage = onMessage)

      // Scrollable Content
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Section with Profile Balloon
        ProfileSection(onMessage = onMessage)

        Spacer(modifier = Modifier.height(24.dp))

        // Edit Profile Button with Overlay Balloon
        EditProfileSection()

        Spacer(modifier = Modifier.height(32.dp))

        // Demo Section Title
        Text(
          text = "Balloon Demos",
          color = White93,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Animation Demos
        AnimationDemos()

        Spacer(modifier = Modifier.height(24.dp))

        // Highlight Animation Demos
        HighlightAnimationDemos()

        Spacer(modifier = Modifier.height(24.dp))

        // Position Demos
        PositionDemos()

        Spacer(modifier = Modifier.height(24.dp))

        // Modifier API Demo (New!)
        ModifierDemo()

        Spacer(modifier = Modifier.height(24.dp))

        // LazyColumn Demo
        LazyColumnDemo(onMessage = onMessage)

        Spacer(modifier = Modifier.height(100.dp))
      }

      // Bottom Navigation with Tag Balloon
      BottomNavigation(onMessage = onMessage)
    }
  }
}

@Composable
private fun TopAppBar(onMessage: (String) -> Unit) {
  val menuBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.85f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
    setArrowOrientation(ArrowOrientation.TOP)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(White93)
    setBalloonAnimation(BalloonAnimation.FADE)
    setDismissWhenClicked(true)
  }

  val menuBalloonState = rememberBalloonState(menuBalloonBuilder)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Pink)
      .padding(
        top = 12.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = 12.dp,
        start = 8.dp,
        end = 8.dp,
      ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "Balloon Compose",
      color = White93,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 8.dp),
    )

    IconButton(
      onClick = { menuBalloonState.showAlignBottom() },
      modifier = Modifier.balloon(menuBalloonState) {
        Column(modifier = Modifier.padding(4.dp)) {
          MenuItem(icon = DemoIcons.Home, text = "Home") {
            menuBalloonState.dismiss()
            onMessage("Home clicked")
          }
          MenuItem(icon = DemoIcons.Person, text = "Profile") {
            menuBalloonState.dismiss()
            onMessage("Profile clicked")
          }
          MenuItem(icon = DemoIcons.Settings, text = "Settings") {
            menuBalloonState.dismiss()
            onMessage("Settings clicked")
          }
        }
      },
    ) {
      Icon(
        imageVector = DemoIcons.AutoMirroredList,
        contentDescription = "Menu",
        tint = White93,
      )
    }
  }
}

@Composable
private fun MenuItem(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Background,
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = text,
      color = Background,
      fontSize = 14.sp,
    )
  }
}

@Composable
private fun ProfileSection(onMessage: (String) -> Unit) {
  val profileBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setPadding(16.dp)
    setCornerRadius(12.dp)
    setBackgroundColor(SkyBlue)
    setBalloonAnimation(BalloonAnimation.CIRCULAR)
    setDismissWhenTouchOutside(true)
  }

  val profileBalloonState = rememberBalloonState(profileBalloonBuilder)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier,
  ) {
    Image(
      painter = painterResource(Res.drawable.sample0),
      contentDescription = "Profile",
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(85.dp)
        .clip(CircleShape)
        .border(3.dp, SkyBlue, CircleShape)
        .balloon(profileBalloonState) {
          Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "Welcome!",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Tap to view your profile details\nand customize your settings.",
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 14.sp,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                profileBalloonState.dismiss()
                onMessage("View Profile clicked")
              },
              colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
              shape = RoundedCornerShape(20.dp),
            ) {
              Text(text = "View Profile", color = SkyBlue, fontSize = 12.sp)
            }
          }
        }
        .clickable { profileBalloonState.showAlignBottom() },
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "skydoves",
      color = White93,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )

    Text(
      text = "Android Developer & Open Source Enthusiast",
      color = White56,
      fontSize = 14.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 32.dp),
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Stats Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      StatItem(count = "32", label = "Posts")
      StatItem(count = "2.3K", label = "Followers")
      StatItem(count = "21", label = "Following")
    }
  }
}

@Composable
private fun StatItem(count: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = count,
      color = White93,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = label,
      color = White56,
      fontSize = 12.sp,
    )
  }
}

@Composable
private fun EditProfileSection() {
  val editBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidthRatio(0.6f)
    setPadding(12.dp)
    setMarginHorizontal(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(SkyBlue)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setIsVisibleOverlay(true)
    setOverlayColor(Overlay)
    setOverlayPadding(8.dp)
    setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
    setOverlayShape(BalloonOverlayShape.RoundRect(12.dp, 12.dp))
    setDismissWhenClicked(true)
    setDismissWhenOverlayClicked(true)
  }

  val editBalloonState = rememberBalloonState(editBalloonBuilder)

  Button(
    onClick = { editBalloonState.showAlignTop() },
    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
    shape = RoundedCornerShape(20.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .border(1.dp, SkyBlue, RoundedCornerShape(20.dp))
      .balloon(editBalloonState) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(4.dp),
        ) {
          Icon(
            imageVector = DemoIcons.Edit,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Now you can edit your profile1 profile2 profile3 profile4 " +
              "really long text so we can test stuff Now you can edit your " +
              "profile1 profile2 profile3 profile4 really long text so we can test stuff",
            color = Color.White,
            fontSize = 14.sp,
          )
        }
      },
    elevation = ButtonDefaults.elevation(0.dp),
  ) {
    Text(text = "Edit Profile", color = SkyBlue)
  }
}

@Composable
private fun AnimationDemos() {
  Text(
    text = "Entry Animations",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    AnimationDemoButton(
      text = "Elastic",
      color = SkyBlue,
      animation = BalloonAnimation.ELASTIC,
      modifier = Modifier.weight(1f),
    )
    AnimationDemoButton(
      text = "Fade",
      color = Purple,
      animation = BalloonAnimation.FADE,
      modifier = Modifier.weight(1f),
    )
    AnimationDemoButton(
      text = "Overshoot",
      color = Teal,
      animation = BalloonAnimation.OVERSHOOT,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun AnimationDemoButton(
  text: String,
  color: Color,
  animation: BalloonAnimation,
  modifier: Modifier = Modifier,
) {
  val builder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(color)
    setBalloonAnimation(animation)
    setDismissWhenClicked(true)
  }

  val balloonState = rememberBalloonState(builder)

  Button(
    onClick = { balloonState.showAlignTop() },
    colors = ButtonDefaults.buttonColors(backgroundColor = color),
    shape = RoundedCornerShape(8.dp),
    modifier = modifier
      .fillMaxWidth()
      .balloon(balloonState) {
        Text(
          text = "$text Animation",
          color = Color.White,
          fontSize = 13.sp,
        )
      },
  ) {
    Text(text = text, color = Color.White, fontSize = 12.sp)
  }
}

@Composable
private fun HighlightAnimationDemos() {
  Text(
    text = "Highlight Animations",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    HighlightDemoButton(
      text = "Heartbeat",
      color = Pink,
      highlightAnimation = BalloonHighlightAnimation.HEARTBEAT,
      modifier = Modifier.weight(1f),
    )
    HighlightDemoButton(
      text = "Shake",
      color = Orange,
      highlightAnimation = BalloonHighlightAnimation.SHAKE,
      modifier = Modifier.weight(1f),
    )
    HighlightDemoButton(
      text = "Breath",
      color = Teal,
      highlightAnimation = BalloonHighlightAnimation.BREATH,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun HighlightDemoButton(
  text: String,
  color: Color,
  highlightAnimation: BalloonHighlightAnimation,
  modifier: Modifier = Modifier,
) {
  val builder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(color)
    setBalloonAnimation(BalloonAnimation.FADE)
    setBalloonHighlightAnimation(highlightAnimation)
    setDismissWhenClicked(true)
  }

  val balloonState = rememberBalloonState(builder)

  Button(
    onClick = { balloonState.showAlignTop() },
    colors = ButtonDefaults.buttonColors(backgroundColor = color),
    shape = RoundedCornerShape(8.dp),
    modifier = modifier
      .fillMaxWidth()
      .balloon(balloonState) {
        Text(
          text = "$text effect!",
          color = Color.White,
          fontSize = 13.sp,
        )
      },
  ) {
    Text(text = text, color = Color.White, fontSize = 12.sp)
  }
}

@Composable
private fun PositionDemos() {
  Text(
    text = "Positioning & Overlay",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  val overlayBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Purple)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setIsVisibleOverlay(true)
    setOverlayColor(Overlay)
    setOverlayPadding(12.dp)
    setOverlayShape(BalloonOverlayShape.Oval)
    setDismissWhenClicked(true)
    setDismissWhenOverlayClicked(true)
  }

  val overlayBalloonState = rememberBalloonState(overlayBuilder)

  // Round Rect Overlay Demo
  val roundRectBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Teal)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setIsVisibleOverlay(true)
    setOverlayColor(Overlay)
    setOverlayPadding(8.dp)
    setOverlayShape(BalloonOverlayShape.RoundRect(12.dp, 12.dp))
    setBalloonHighlightAnimation(BalloonHighlightAnimation.HEARTBEAT)
    setDismissWhenClicked(true)
    setDismissWhenOverlayClicked(true)
  }

  val roundRectBalloonState = rememberBalloonState(roundRectBuilder)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Oval Overlay Demo
    Box(
      modifier = Modifier
        .weight(1f)
        .height(60.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.horizontalGradient(listOf(Purple, Pink)),
        )
        .balloon(overlayBalloonState) {
          Text(
            text = "Oval overlay shape!",
            color = Color.White,
            fontSize = 13.sp,
          )
        }
        .clickable { overlayBalloonState.showAlignTop() },
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Oval Overlay",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
      )
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .height(60.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.horizontalGradient(listOf(Teal, SkyBlue)),
        )
        .balloon(roundRectBalloonState) {
          Text(
            text = "Rounded rectangle!",
            color = Color.White,
            fontSize = 13.sp,
          )
        }
        .clickable { roundRectBalloonState.showAlignTop() },
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "RoundRect Overlay",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
private fun ModifierDemo() {
  Text(
    text = "Modifier API (New!)",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  Text(
    text = "Use Modifier.balloon() instead of wrapping content",
    color = White56,
    fontSize = 12.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 12.dp),
  )

  // Create balloon builder
  val modifierBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.BOTTOM)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Orange)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setDismissWhenClicked(true)
  }

  // Use rememberBalloonState instead of storing the popup manually
  val balloonState = rememberBalloonState(modifierBalloonBuilder)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Example using the Modifier.balloon() API
    Box(
      modifier = Modifier
        .weight(1f)
        .height(60.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.horizontalGradient(listOf(Orange, Pink)),
        )
        .balloon(balloonState) {
          // Balloon content as trailing lambda
          Column(modifier = Modifier.padding(4.dp)) {
            Text(
              text = "Modifier API!",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "No wrapping needed",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 12.sp,
            )
          }
        }
        .clickable { balloonState.showAlignTop() },
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Modifier.balloon()",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
      )
    }

    // Show button for the balloon
    Button(
      onClick = { balloonState.showAlignTop() },
      modifier = Modifier
        .weight(1f)
        .height(60.dp),
      colors = ButtonDefaults.buttonColors(backgroundColor = Orange),
      shape = RoundedCornerShape(8.dp),
    ) {
      Text(
        text = "Show Balloon",
        color = Color.White,
        fontSize = 12.sp,
      )
    }
  }
}

@Composable
private fun LazyColumnDemo(onMessage: (String) -> Unit) {
  Text(
    text = "LazyColumn with Balloons",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  val items = listOf(
    "Compose Balloon" to "A modern tooltip library for Jetpack Compose",
    "Easy Integration" to "Simple API with powerful customization options",
    "Rich Animations" to "Supports elastic, fade, circular, and more",
    "Overlay Support" to "Highlight anchors with customizable shapes",
    "Arrow Positioning" to "Flexible arrow placement and orientation",
    "Lifecycle Aware" to "Automatically handles lifecycle events",
    "Compose Support" to "Native Jetpack Compose integration",
    "Custom Content" to "Support for custom composable content",
    "Persistence" to "Show once or count-based display options",
    "Accessibility" to "Full accessibility support built-in",
    "RTL Support" to "Right-to-left layout support included",
  )

  val itemBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setPadding(12.dp)
    setMarginHorizontal(16.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(SkyBlue)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setDismissWhenClicked(true)
    setDismissWhenTouchOutside(false)
  }

  val headerBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setPadding(16.dp)
    setCornerRadius(12.dp)
    setBackgroundColor(Purple)
    setBalloonAnimation(BalloonAnimation.CIRCULAR)
    setDismissWhenTouchOutside(true)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxWidth()
      .height(500.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF3A383A)),
  ) {
    // Header section - similar to main layout's profile section
    item {
      LazyColumnHeader(style = headerBalloonBuilder)
    }

    // Divider
    item {
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(Color(0xFF4A484A)),
      )
    }

    // Section title
    item {
      Text(
        text = "Features",
        color = White93,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .fillMaxWidth()
          .background(Background)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      )
    }

    // List items
    itemsIndexed(items) { index, (title, description) ->
      ListItemWithBalloon(
        index = index,
        title = title,
        description = description,
        style = itemBalloonBuilder,
        onMessage = onMessage,
      )
      if (index < items.lastIndex) {
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF3A383A)),
        )
      }
    }
  }
}

@Composable
private fun LazyColumnHeader(style: BalloonStyle) {
  val balloonState = rememberBalloonState(style)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(Background)
      .balloon(balloonState) {
        Column(
          modifier = Modifier.padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "Balloon Library",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Tap items below to see\ntooltips in action!",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
          )
        }
      }
      .clickable { balloonState.showAlignBottom() }
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Profile-like header
    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(listOf(Purple, Pink)),
        ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = DemoIcons.Settings,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(32.dp),
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "Balloon Demo",
      color = White93,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
    )

    Text(
      text = "Tap to learn more",
      color = White56,
      fontSize = 14.sp,
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Stats row similar to main layout
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      LazyColumnStatItem(count = "11", label = "Features")
      LazyColumnStatItem(count = "5+", label = "Animations")
      LazyColumnStatItem(count = "100%", label = "Compose")
    }
  }
}

@Composable
private fun LazyColumnStatItem(count: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = count,
      color = White93,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = label,
      color = White56,
      fontSize = 12.sp,
    )
  }
}

@Composable
private fun ListItemWithBalloon(
  index: Int,
  title: String,
  description: String,
  style: BalloonStyle,
  onMessage: (String) -> Unit,
) {
  val balloonState = rememberBalloonState(style)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Background)
      .balloon(balloonState) {
        Column(modifier = Modifier.padding(4.dp)) {
          Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = description,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
          )
        }
      }
      .clickable {
        balloonState.showAlignBottom()
        onMessage("Item ${index + 1}: $title")
      }
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          Brush.linearGradient(
            listOf(SkyBlue, Purple),
          ),
        ),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "${index + 1}",
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        color = White93,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
      )
      Text(
        text = "Tap to see details",
        color = White56,
        fontSize = 12.sp,
      )
    }
    Icon(
      imageVector = DemoIcons.Settings,
      contentDescription = null,
      tint = White56,
      modifier = Modifier.size(20.dp),
    )
  }
}

@Composable
private fun BottomNavigation(onMessage: (String) -> Unit) {
  val tagBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowOrientation(ArrowOrientation.BOTTOM)
    setPadding(8.dp)
    setCornerRadius(4.dp)
    setBackgroundColor(White93)
    setBalloonAnimation(BalloonAnimation.FADE)
    setBalloonHighlightAnimation(BalloonHighlightAnimation.HEARTBEAT)
    setDismissWhenClicked(true)
    setAutoDismissDuration(2000L)
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Pink)
      .padding(
        top = 8.dp,
        bottom = 8.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        start = 8.dp,
        end = 8.dp,
      ),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    BottomNavItem(
      icon = DemoIcons.Home,
      label = "Home",
      style = tagBalloonBuilder,
      tagText = "Home",
      onMessage = onMessage,
    )
    BottomNavItem(
      icon = DemoIcons.Person,
      label = "Profile",
      style = tagBalloonBuilder,
      tagText = "Profile",
      onMessage = onMessage,
    )
    BottomNavItem(
      icon = DemoIcons.Settings,
      label = "Settings",
      style = tagBalloonBuilder,
      tagText = "Settings",
      onMessage = onMessage,
    )
  }
}

@Composable
private fun BottomNavItem(
  icon: ImageVector,
  label: String,
  style: BalloonStyle,
  tagText: String,
  onMessage: (String) -> Unit,
) {
  val balloonState = rememberBalloonState(style)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .balloon(balloonState) {
        Text(
          text = tagText,
          color = Background,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
        )
      }
      .clickable {
        balloonState.showAlignTop()
        onMessage("$label clicked")
      }
      .padding(horizontal = 16.dp, vertical = 4.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = White93,
      modifier = Modifier.size(24.dp),
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      color = White93,
      fontSize = 10.sp,
    )
  }
}
