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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.balloon.compose.multiplatform.ArrowOrientation
import com.skydoves.balloon.compose.multiplatform.ArrowPositionRules
import com.skydoves.balloon.compose.multiplatform.Balloon
import com.skydoves.balloon.compose.multiplatform.BalloonAnimation
import com.skydoves.balloon.compose.multiplatform.BalloonCenterAlign
import com.skydoves.balloon.compose.multiplatform.BalloonStyle
import com.skydoves.balloon.compose.multiplatform.rememberBalloonBuilder
import com.skydoves.balloon.compose.multiplatform.rememberBalloonState

// Color definitions — mirrors the palette of the original ComposeActivity demo.
private val Background = Color(0xFF2B292B)
private val SkyBlue = Color(0xFF57A8D8)
private val Pink = Color(0xFFC51162)
private val White93 = Color(0xEDF8F8F8)
private val White70 = Color(0xB2FFFFFF)
private val White56 = Color(0x8EFFFFFF)
private val Purple = Color(0xFF9C27B0)
private val Teal = Color(0xFF009688)
private val Orange = Color(0xFFFF5722)

/**
 * Demo screen exercising the [Balloon] APIs across all Compose Multiplatform
 * targets (Android, iOS, Desktop, Wasm). It mirrors the structure of the pure
 * Android Compose sample (`ComposeActivity` in the `:app` module): a top app bar
 * with a menu balloon, a profile section, animation / styling / positioning
 * demos, a LazyColumn with per-item balloons and a bottom navigation bar with
 * auto-dismissing tag balloons.
 *
 * Android-only features of the original demo are substituted with their KMP
 * counterparts: `Toast` becomes the [onMessage] callback, drawable resources
 * become gradient placeholders, and the overlay / highlight-animation demos
 * become border, arrow-color and auto-dismiss styling demos.
 *
 * **NOTE:** This composable is bundled inside `:samples-shared` purely so the
 * demo apps in this repository can share a single implementation. It is not
 * part of the library's stable API.
 *
 * @param onMessage invoked with a human-readable message when a demo action is
 *   triggered (the KMP stand-in for the original demo's `Toast`s).
 */
@Composable
public fun BalloonDemoScreen(onMessage: (String) -> Unit = {}) {
  MaterialTheme {
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

        // Edit Profile Button with a width-constrained Balloon
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

        // Styling Demos (border / arrow color / auto dismiss)
        StylingDemos()

        Spacer(modifier = Modifier.height(24.dp))

        // Position Demos
        PositionDemos()

        Spacer(modifier = Modifier.height(24.dp))

        // Anchor Composable API Demo
        AnchorApiDemo()

        Spacer(modifier = Modifier.height(24.dp))

        // LazyColumn Demo
        LazyColumnDemo(onMessage = onMessage)

        Spacer(modifier = Modifier.height(100.dp))
      }

      // Bottom Navigation with Tag Balloon
      BottomNavigationBar(onMessage = onMessage)
    }
  }
}

@Composable
private fun TopAppBar(onMessage: (String) -> Unit) {
  val menuBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.85f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
    setArrowOrientation(ArrowOrientation.TOP)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(White93)
    setBalloonAnimation(BalloonAnimation.FADE)
  }

  val menuBalloonState = rememberBalloonState(menuBalloonStyle)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Pink)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(vertical = 12.dp, horizontal = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "Balloon Multiplatform",
      color = White93,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 8.dp),
    )

    Balloon(
      state = menuBalloonState,
      balloonContent = {
        Column(modifier = Modifier.padding(4.dp)) {
          MenuItem(text = "Home") {
            menuBalloonState.dismiss()
            onMessage("Home clicked")
          }
          MenuItem(text = "Profile") {
            menuBalloonState.dismiss()
            onMessage("Profile clicked")
          }
          MenuItem(text = "Settings") {
            menuBalloonState.dismiss()
            onMessage("Settings clicked")
          }
        }
      },
    ) {
      // Hamburger menu icon drawn with plain boxes so it renders identically on
      // every target without a material-icons dependency.
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable { menuBalloonState.showAlignBottom() }
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        repeat(3) {
          Box(
            modifier = Modifier
              .width(18.dp)
              .height(2.dp)
              .clip(RoundedCornerShape(1.dp))
              .background(White93),
          )
        }
      }
    }
  }
}

@Composable
private fun MenuItem(
  text: String,
  onClick: () -> Unit,
) {
  Text(
    text = text,
    color = Background,
    fontSize = 14.sp,
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
  )
}

@Composable
private fun ProfileSection(onMessage: (String) -> Unit) {
  val profileBalloonStyle = rememberBalloonBuilder {
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

  val profileBalloonState = rememberBalloonState(profileBalloonStyle)

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Balloon(
      state = profileBalloonState,
      balloonContent = {
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
      },
    ) {
      // Gradient placeholder avatar (the original demo uses a drawable resource,
      // which isn't available from common multiplatform code).
      Box(
        modifier = Modifier
          .size(85.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(SkyBlue, Purple)))
          .border(3.dp, SkyBlue, CircleShape)
          .clickable { profileBalloonState.showAlignBottom() },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "B",
          color = Color.White,
          fontSize = 36.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }

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
  val editBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setMaxWidth(280.dp)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(SkyBlue)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
  }

  val editBalloonState = rememberBalloonState(editBalloonStyle)

  Balloon(
    state = editBalloonState,
    modifier = Modifier.fillMaxWidth(),
    balloonContent = {
      Text(
        text = "Now you can edit your profile1 profile2 profile3 profile4 " +
          "really long text so we can test stuff Now you can edit your " +
          "profile1 profile2 profile3 profile4 really long text so we can test stuff",
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.padding(4.dp),
      )
    },
  ) {
    Button(
      onClick = { editBalloonState.showAlignTop() },
      colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .border(1.dp, SkyBlue, RoundedCornerShape(20.dp)),
      elevation = ButtonDefaults.elevation(0.dp),
    ) {
      Text(text = "Edit Profile", color = SkyBlue)
    }
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
  val style = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(color)
    setBalloonAnimation(animation)
  }

  val balloonState = rememberBalloonState(style)

  Balloon(
    state = balloonState,
    modifier = modifier,
    balloonContent = {
      Text(
        text = "$text Animation",
        color = Color.White,
        fontSize = 13.sp,
        modifier = Modifier.clickable { balloonState.dismiss() },
      )
    },
  ) {
    Button(
      onClick = { balloonState.showAlignTop() },
      colors = ButtonDefaults.buttonColors(backgroundColor = color),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = text, color = Color.White, fontSize = 12.sp)
    }
  }
}

@Composable
private fun StylingDemos() {
  Text(
    text = "Styling & Behavior",
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
    StylingDemoButton(
      text = "Border",
      color = Pink,
      balloonText = "Bordered balloon!",
      modifier = Modifier.weight(1f),
    ) {
      setBalloonStroke(White93, 2.dp)
    }
    StylingDemoButton(
      text = "Arrow",
      color = Orange,
      balloonText = "Custom arrow color!",
      modifier = Modifier.weight(1f),
    ) {
      setArrowColor(Pink)
    }
    StylingDemoButton(
      text = "Auto",
      color = Teal,
      balloonText = "Dismisses in 2s",
      modifier = Modifier.weight(1f),
    ) {
      setAutoDismissDuration(2000L)
    }
  }
}

@Composable
private fun StylingDemoButton(
  text: String,
  color: Color,
  balloonText: String,
  modifier: Modifier = Modifier,
  styleBlock: Balloon.Builder.() -> Unit,
) {
  val style = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(color)
    setBalloonAnimation(BalloonAnimation.FADE)
    styleBlock()
  }

  val balloonState = rememberBalloonState(style)

  Balloon(
    state = balloonState,
    modifier = modifier,
    balloonContent = {
      Text(
        text = balloonText,
        color = Color.White,
        fontSize = 13.sp,
        modifier = Modifier.clickable { balloonState.dismiss() },
      )
    },
  ) {
    Button(
      onClick = { balloonState.showAlignTop() },
      colors = ButtonDefaults.buttonColors(backgroundColor = color),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = text, color = Color.White, fontSize = 12.sp)
    }
  }
}

@Composable
private fun PositionDemos() {
  Text(
    text = "Positioning",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  val endBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Purple)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
  }

  val endBalloonState = rememberBalloonState(endBalloonStyle)

  val centerBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Teal)
    setBalloonAnimation(BalloonAnimation.CIRCULAR)
  }

  val centerBalloonState = rememberBalloonState(centerBalloonStyle)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Align End Demo — flips to the opposite side automatically when there is
    // not enough room on the requested side.
    Balloon(
      state = endBalloonState,
      modifier = Modifier.weight(1f),
      balloonContent = {
        Text(
          text = "End aligned!",
          color = Color.White,
          fontSize = 13.sp,
        )
      },
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Brush.horizontalGradient(listOf(Purple, Pink)))
          .clickable { endBalloonState.showAlignEnd() },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Align End",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
        )
      }
    }

    // showAtCenter Demo — places the balloon adjacent to the anchor's center.
    Balloon(
      state = centerBalloonState,
      modifier = Modifier.weight(1f),
      balloonContent = {
        Text(
          text = "Shown at center!",
          color = Color.White,
          fontSize = 13.sp,
        )
      },
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Brush.horizontalGradient(listOf(Teal, SkyBlue)))
          .clickable { centerBalloonState.showAtCenter(BalloonCenterAlign.TOP) },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Show At Center",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
        )
      }
    }
  }
}

@Composable
private fun AnchorApiDemo() {
  Text(
    text = "Anchor Composable API",
    color = White70,
    fontSize = 14.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
  )

  Text(
    text = "Wrap any anchor with Balloon(state) { ... }",
    color = White56,
    fontSize = 12.sp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 12.dp),
  )

  val anchorBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.BOTTOM)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(Orange)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
  }

  val balloonState = rememberBalloonState(anchorBalloonStyle)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // The anchor wrapped by the Balloon composable.
    Balloon(
      state = balloonState,
      modifier = Modifier.weight(1f),
      balloonContent = {
        Column(modifier = Modifier.padding(4.dp)) {
          Text(
            text = "Balloon anchor!",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Layout-shift free popup",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
          )
        }
      },
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Brush.horizontalGradient(listOf(Orange, Pink)))
          .clickable { balloonState.showAlignTop() },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Balloon { ... }",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
        )
      }
    }

    // A second trigger sharing the same state — showing the balloon from
    // anywhere while it stays anchored to the wrapped composable above.
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
    "Compose Multiplatform" to "Runs on Android, iOS, Desktop and Wasm",
    "Easy Integration" to "Simple API with powerful customization options",
    "Rich Animations" to "Supports elastic, fade, circular, and more",
    "Smart Positioning" to "Flips to the opposite side when out of room",
    "Arrow Positioning" to "Flexible arrow placement and orientation",
    "State Driven" to "Fully Compose-state-driven visibility",
    "Custom Content" to "Support for custom composable content",
    "Auto Dismiss" to "Time-based dismissal built-in",
    "Await Support" to "Suspend until the balloon is dismissed",
    "RTL Support" to "Right-to-left layout support included",
  )

  val itemBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
    setArrowOrientation(ArrowOrientation.TOP)
    setPadding(12.dp)
    setCornerRadius(8.dp)
    setBackgroundColor(SkyBlue)
    setBalloonAnimation(BalloonAnimation.ELASTIC)
    setDismissWhenTouchOutside(false)
  }

  val headerBalloonStyle = rememberBalloonBuilder {
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
      LazyColumnHeader(style = headerBalloonStyle)
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
        style = itemBalloonStyle,
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

  Balloon(
    state = balloonState,
    modifier = Modifier.fillMaxWidth(),
    balloonContent = {
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
    },
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Background)
        .clickable { balloonState.showAlignBottom() }
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Profile-like header
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(Purple, Pink))),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "B",
          color = Color.White,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
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
        StatItem(count = "10", label = "Features")
        StatItem(count = "4+", label = "Animations")
        StatItem(count = "100%", label = "Multiplatform")
      }
    }
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

  Balloon(
    state = balloonState,
    modifier = Modifier.fillMaxWidth(),
    balloonContent = {
      // dismissWhenTouchOutside is disabled for list items, so the balloon
      // content itself is tappable to dismiss (the original demo's
      // setDismissWhenClicked equivalent).
      Column(
        modifier = Modifier
          .clickable { balloonState.dismiss() }
          .padding(4.dp),
      ) {
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
    },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Background)
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
          .background(Brush.linearGradient(listOf(SkyBlue, Purple))),
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
      Text(
        text = "›",
        color = White56,
        fontSize = 20.sp,
      )
    }
  }
}

@Composable
private fun BottomNavigationBar(onMessage: (String) -> Unit) {
  val tagBalloonStyle = rememberBalloonBuilder {
    setArrowSize(10.dp)
    setArrowPosition(0.5f)
    setArrowOrientation(ArrowOrientation.BOTTOM)
    setPadding(8.dp)
    setCornerRadius(4.dp)
    setBackgroundColor(White93)
    setBalloonAnimation(BalloonAnimation.FADE)
    setAutoDismissDuration(2000L)
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(Pink)
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(vertical = 8.dp, horizontal = 8.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    BottomNavItem(
      label = "Home",
      style = tagBalloonStyle,
      tagText = "Home",
      onMessage = onMessage,
    )
    BottomNavItem(
      label = "Profile",
      style = tagBalloonStyle,
      tagText = "Profile",
      onMessage = onMessage,
    )
    BottomNavItem(
      label = "Settings",
      style = tagBalloonStyle,
      tagText = "Settings",
      onMessage = onMessage,
    )
  }
}

@Composable
private fun BottomNavItem(
  label: String,
  style: BalloonStyle,
  tagText: String,
  onMessage: (String) -> Unit,
) {
  val balloonState = rememberBalloonState(style)

  Balloon(
    state = balloonState,
    balloonContent = {
      Text(
        text = tagText,
        color = Background,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
      )
    },
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .clickable {
          balloonState.showAlignTop()
          onMessage("$label clicked")
        }
        .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
      // Simple shape icon so the demo has no material-icons dependency.
      Box(
        modifier = Modifier
          .size(20.dp)
          .border(2.dp, White93, CircleShape),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        color = White93,
        fontSize = 10.sp,
      )
    }
  }
}
