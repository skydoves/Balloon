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

package com.skydoves.balloondemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.compose.balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.compose.rememberBalloonState
import com.skydoves.balloon.overlay.BalloonOverlayOval
import com.skydoves.balloon.overlay.BalloonOverlayRoundRect

class ComposeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      ComposeActivityContent(
        onToast = { message ->
          Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        },
      )
    }
  }
}

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

@Composable
private fun ComposeActivityContent(
  onToast: (String) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Background),
  ) {
    // Top App Bar with Menu Balloon
    TopAppBar(onToast = onToast)

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
      ProfileSection(onToast = onToast)

      Spacer(modifier = Modifier.height(24.dp))

      // Edit Profile Button with Overlay Balloon
      EditProfileSection(onToast = onToast)

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
      AnimationDemos(onToast = onToast)

      Spacer(modifier = Modifier.height(24.dp))

      // Highlight Animation Demos
      HighlightAnimationDemos(onToast = onToast)

      Spacer(modifier = Modifier.height(24.dp))

      // Position Demos
      PositionDemos(onToast = onToast)

      Spacer(modifier = Modifier.height(24.dp))

      // Modifier API Demo (New!)
      ModifierDemo(onToast = onToast)

      Spacer(modifier = Modifier.height(24.dp))

      // LazyColumn Demo
      LazyColumnDemo(onToast = onToast)

      Spacer(modifier = Modifier.height(100.dp))
    }

    // Bottom Navigation with Tag Balloon
    BottomNavigation(onToast = onToast)
  }
}

@Composable
private fun TopAppBar(onToast: (String) -> Unit) {
  val menuBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.85f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
    setArrowOrientation(ArrowOrientation.TOP)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColor(White93.hashCode())
    setBalloonAnimation(BalloonAnimation.FADE)
    setDismissWhenClicked(true)
  }

  val menuBalloonState = rememberBalloonState(menuBalloonBuilder)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Pink)
      .padding(horizontal = 8.dp, vertical = 12.dp),
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
          MenuItem(icon = Icons.Default.Home, text = "Home") {
            menuBalloonState.dismiss()
            onToast("Home clicked")
          }
          MenuItem(icon = Icons.Default.Person, text = "Profile") {
            menuBalloonState.dismiss()
            onToast("Profile clicked")
          }
          MenuItem(icon = Icons.Default.Settings, text = "Settings") {
            menuBalloonState.dismiss()
            onToast("Settings clicked")
          }
        }
      },
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.List,
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
private fun ProfileSection(onToast: (String) -> Unit) {
  val profileBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(16)
    setCornerRadius(12f)
    setBackgroundColor(SkyBlue.hashCode())
    setBalloonAnimation(BalloonAnimation.CIRCULAR)
    setDismissWhenTouchOutside(true)
  }

  val profileBalloonState = rememberBalloonState(profileBalloonBuilder)

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier,
  ) {
    Image(
      painter = painterResource(id = R.drawable.sample0),
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
                onToast("View Profile clicked")
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
private fun EditProfileSection(onToast: (String) -> Unit) {
  val editBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidthRatio(0.6f)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setMarginHorizontal(12)
    setTextSize(15f)
    setCornerRadius(8f)
    setBackgroundColor(SkyBlue.hashCode())
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setIsVisibleOverlay(true)
    setOverlayColor(Overlay.hashCode())
    setOverlayPadding(8f)
    setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
    setOverlayShape(BalloonOverlayRoundRect(12f, 12f))
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
            imageVector = Icons.Default.Edit,
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
private fun AnimationDemos(onToast: (String) -> Unit) {
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
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColor(color.hashCode())
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
private fun HighlightAnimationDemos(onToast: (String) -> Unit) {
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
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColor(color.hashCode())
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
private fun PositionDemos(onToast: (String) -> Unit) {
  Text(
    text = "Positioning & Overlay",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  val overlayBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColor(Purple.hashCode())
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setIsVisibleOverlay(true)
    setOverlayColor(Overlay.hashCode())
    setOverlayPadding(12f)
    setOverlayShape(BalloonOverlayOval)
    setDismissWhenClicked(true)
    setDismissWhenOverlayClicked(true)
  }

  val overlayBalloonState = rememberBalloonState(overlayBuilder)

  // Round Rect Overlay Demo
  val roundRectBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColor(Teal.hashCode())
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setIsVisibleOverlay(true)
    setOverlayColor(Overlay.hashCode())
    setOverlayPadding(8f)
    setOverlayShape(BalloonOverlayRoundRect(12f, 12f))
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
private fun ModifierDemo(onToast: (String) -> Unit) {
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
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.BOTTOM)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setCornerRadius(8f)
    setBackgroundColor(Orange.hashCode())
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setDismissWhenClicked(true)
  }

  // Use rememberBalloonState instead of storing BalloonWindow manually
  val balloonState = rememberBalloonState(modifierBalloonBuilder)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Example using the new Modifier.balloon() API
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
private fun LazyColumnDemo(onToast: (String) -> Unit) {
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
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(12)
    setMarginHorizontal(16)
    setCornerRadius(8f)
    setBackgroundColor(SkyBlue.hashCode())
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setDismissWhenClicked(true)
    setDismissWhenTouchOutside(false)
  }

  val headerBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(16)
    setCornerRadius(12f)
    setBackgroundColor(Purple.hashCode())
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
      LazyColumnHeader(
        builder = headerBalloonBuilder,
        onToast = onToast,
      )
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
        builder = itemBalloonBuilder,
        onToast = onToast,
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
private fun LazyColumnHeader(
  builder: com.skydoves.balloon.Balloon.Builder,
  onToast: (String) -> Unit,
) {
  val balloonState = rememberBalloonState(builder)

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
        imageVector = Icons.Default.Settings,
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
  builder: com.skydoves.balloon.Balloon.Builder,
  onToast: (String) -> Unit,
) {
  val balloonState = rememberBalloonState(builder)

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
        onToast("Item ${index + 1}: $title")
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
      imageVector = Icons.Default.Settings,
      contentDescription = null,
      tint = White56,
      modifier = Modifier.size(20.dp),
    )
  }
}

@Composable
private fun BottomNavigation(onToast: (String) -> Unit) {
  val tagBalloonBuilder = rememberBalloonBuilder {
    setArrowSize(10)
    setArrowPosition(0.5f)
    setArrowOrientation(ArrowOrientation.BOTTOM)
    setWidth(BalloonSizeSpec.WRAP)
    setHeight(BalloonSizeSpec.WRAP)
    setPadding(8)
    setCornerRadius(4f)
    setBackgroundColor(White93.hashCode())
    setBalloonAnimation(BalloonAnimation.FADE)
    setBalloonHighlightAnimation(BalloonHighlightAnimation.HEARTBEAT)
    setDismissWhenClicked(true)
    setAutoDismissDuration(2000L)
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Pink)
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    BottomNavItem(
      icon = Icons.Default.Home,
      label = "Home",
      builder = tagBalloonBuilder,
      tagText = "Home",
      onToast = onToast,
    )
    BottomNavItem(
      icon = Icons.Default.Person,
      label = "Profile",
      builder = tagBalloonBuilder,
      tagText = "Profile",
      onToast = onToast,
    )
    BottomNavItem(
      icon = Icons.Default.Settings,
      label = "Settings",
      builder = tagBalloonBuilder,
      tagText = "Settings",
      onToast = onToast,
    )
  }
}

@Composable
private fun BottomNavItem(
  icon: ImageVector,
  label: String,
  builder: com.skydoves.balloon.Balloon.Builder,
  tagText: String,
  onToast: (String) -> Unit,
) {
  val balloonState = rememberBalloonState(builder)

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
        onToast("$label clicked")
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
