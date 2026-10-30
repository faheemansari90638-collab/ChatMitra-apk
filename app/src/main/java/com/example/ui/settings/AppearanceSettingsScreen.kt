package com.example.ui.settings

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppAccentColor
import com.example.data.model.AppThemeMode
import com.example.data.model.BubbleStyle
import com.example.data.model.ChatWallpaper
import com.example.data.model.MessageSpacing
import com.example.data.repository.AppearanceRepository
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
  onNavigateBack: () -> Unit,
  onNavigateFontSettings: () -> Unit
) {
  val appearance by AppearanceRepository.appearanceSettings.collectAsState()
  var showResetDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Appearance", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("appearance_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showResetDialog = true }, modifier = Modifier.testTag("reset_appearance_button")) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset to Default", tint = TealPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("appearance_settings_screen")
    ) {
      // 1. LIVE PREVIEW CARD
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("appearance_preview_card"),
          colors = CardDefaults.cardColors(containerColor = Color(appearance.chatWallpaper.bgHex)),
          shape = RoundedCornerShape(16.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Live Chat Preview",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (appearance.chatWallpaper.isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Incoming bubble
            Row {
              Box(
                modifier = Modifier
                  .background(
                    if (appearance.chatWallpaper.isDark) Color(0xFF1E293B) else Color.White,
                    RoundedCornerShape(appearance.bubbleStyle.cornerRadiusDp.dp)
                  )
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Text(
                  text = "Hey! How does the new ChatMitra look?",
                  fontSize = 13.sp,
                  color = if (appearance.chatWallpaper.isDark) Color.White else Color.Black
                )
              }
            }

            Spacer(modifier = Modifier.height(appearance.messageSpacing.spacingDp.dp))

            // Outgoing bubble
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
              Box(
                modifier = Modifier
                  .background(
                    Color(appearance.accentColor.primaryHex),
                    RoundedCornerShape(appearance.bubbleStyle.cornerRadiusDp.dp)
                  )
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Text(
                  text = "Clean, responsive, and gorgeous! ✨",
                  fontSize = 13.sp,
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // 2. THEME MODE
      item {
        Text(
          text = "THEME",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 6.dp)
        )
      }

      items(AppThemeMode.values()) { mode ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { AppearanceRepository.setThemeMode(mode) }
            .padding(horizontal = 24.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = appearance.themeMode == mode,
            onClick = { AppearanceRepository.setThemeMode(mode) },
            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(text = mode.label, fontSize = 14.sp)
        }
      }

      item {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }

      // 3. ACCENT COLOR SELECTION
      item {
        Text(
          text = "ACCENT COLOR",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 6.dp)
        )
      }

      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(AppAccentColor.values()) { accent ->
            val isSelected = appearance.accentColor == accent
            Box(
              modifier = Modifier
                .size(46.dp)
                .background(Color(accent.primaryHex), CircleShape)
                .border(
                  width = if (isSelected) 3.dp else 1.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = CircleShape
                )
                .clickable { AppearanceRepository.setAccentColor(accent) }
                .testTag("accent_color_${accent.name}"),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
            }
          }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }

      // 4. CHAT WALLPAPER
      item {
        Text(
          text = "CHAT WALLPAPER",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 6.dp)
        )
      }

      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(ChatWallpaper.values()) { wall ->
            val isSelected = appearance.chatWallpaper == wall
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.clickable { AppearanceRepository.setChatWallpaper(wall) }
            ) {
              Box(
                modifier = Modifier
                  .size(width = 64.dp, height = 90.dp)
                  .background(Color(wall.bgHex), RoundedCornerShape(12.dp))
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = wall.label.take(12), fontSize = 11.sp, color = CheckmarkGrey)
            }
          }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }

      // 5. FONT SETTINGS NAVIGATION
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateFontSettings() }
            .testTag("navigate_font_settings_item"),
          headlineContent = { Text("Font & Text Size", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Adjust message, list & composer text sizes, family & bold", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.FormatSize, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 6. BUBBLE STYLE & SPACING
      item {
        Text(
          text = "MESSAGE BUBBLES & ACCESSIBILITY",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("Modern Rounded Bubbles", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(appearance.bubbleStyle.label, color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = appearance.bubbleStyle == BubbleStyle.ROUNDED,
              onCheckedChange = { checked ->
                AppearanceRepository.setBubbleStyle(if (checked) BubbleStyle.ROUNDED else BubbleStyle.COMPACT)
              },
              modifier = Modifier.testTag("bubble_style_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("High Contrast Mode", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Enhance contrast for text readability and borders", color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = appearance.isHighContrast,
              onCheckedChange = { AppearanceRepository.setHighContrast(it) },
              modifier = Modifier.testTag("high_contrast_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Reduced Motion", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Minimize decorative animations to save battery and reduce flicker", color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = appearance.isReducedMotion,
              onCheckedChange = { AppearanceRepository.setReducedMotion(it) },
              modifier = Modifier.testTag("reduced_motion_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }

    // Reset Defaults Dialog
    if (showResetDialog) {
      AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Reset Appearance?") },
        text = { Text("Do you want to restore the default theme, emerald teal accent, and standard message bubble style?") },
        confirmButton = {
          TextButton(
            onClick = {
              AppearanceRepository.resetAppearanceToDefault()
              showResetDialog = false
            }
          ) {
            Text("Reset", color = TealPrimary, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}
