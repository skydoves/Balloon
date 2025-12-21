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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF2B292B)
private val Pink = Color(0xFFC51162)
private val SkyBlue = Color(0xFF57A8D8)
private val Purple = Color(0xFF9C27B0)
private val Teal = Color(0xFF009688)
private val White93 = Color(0xEDF8F8F8)
private val White56 = Color(0x8EFFFFFF)

class LauncherActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      LauncherScreen(
        onMainActivityClick = {
          startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
        },
        onCustomActivityClick = {
          startActivity(Intent(this@LauncherActivity, CustomActivity::class.java))
        },
        onComposeActivityClick = {
          startActivity(Intent(this@LauncherActivity, ComposeActivity::class.java))
        },
      )
    }
  }
}

@Composable
private fun LauncherScreen(
  onMainActivityClick: () -> Unit,
  onCustomActivityClick: () -> Unit,
  onComposeActivityClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Background)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    // Logo
    Box(
      modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(listOf(Pink, Purple)),
        ),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "B",
        color = Color.White,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Title
    Text(
      text = "Balloon",
      color = White93,
      fontSize = 32.sp,
      fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Subtitle
    Text(
      text = "Customizable tooltips & popups\nfor Android",
      color = White56,
      fontSize = 16.sp,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(48.dp))

    // Buttons
    LauncherButton(
      text = "Compose Demo",
      subtitle = "Jetpack Compose integration",
      icon = Icons.Default.Star,
      gradientColors = listOf(Pink, Purple),
      onClick = onComposeActivityClick,
    )

    Spacer(modifier = Modifier.height(16.dp))

    LauncherButton(
      text = "XML Demo",
      subtitle = "Traditional View-based demo",
      icon = Icons.Default.Build,
      gradientColors = listOf(SkyBlue, Teal),
      onClick = onMainActivityClick,
    )

    Spacer(modifier = Modifier.height(16.dp))

    LauncherButton(
      text = "Custom Layout Demo",
      subtitle = "Custom balloon layouts",
      icon = Icons.Default.Create,
      gradientColors = listOf(Purple, Pink),
      onClick = onCustomActivityClick,
    )

    Spacer(modifier = Modifier.height(48.dp))

    // Footer
    Text(
      text = "by skydoves",
      color = White56,
      fontSize = 14.sp,
    )
  }
}

@Composable
private fun LauncherButton(
  text: String,
  subtitle: String,
  icon: ImageVector,
  gradientColors: List<Color>,
  onClick: () -> Unit,
) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .height(72.dp),
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3A383A)),
    shape = RoundedCornerShape(16.dp),
    elevation = ButtonDefaults.elevation(
      defaultElevation = 0.dp,
      pressedElevation = 0.dp,
    ),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.linearGradient(gradientColors),
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(24.dp),
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = text,
          color = White93,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text = subtitle,
          color = White56,
          fontSize = 13.sp,
        )
      }
    }
  }
}
