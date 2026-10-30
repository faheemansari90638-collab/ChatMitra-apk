package com.example.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FontFamilyChoice
import com.example.data.model.FontSizePreset
import com.example.data.repository.AppearanceRepository
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
  onNavigateBack: () -> Unit
) {
  val fontSettings by AppearanceRepository.fontSettings.collectAsState()
  var showResetDialog by remember { mutableStateOf(false) }

  val targetFontFamily = when (fontSettings.fontFamily) {
    FontFamilyChoice.SERIF -> FontFamily.Serif
    FontFamilyChoice.MONOSPACE -> FontFamily.Monospace
    FontFamilyChoice.SANS_SERIF -> FontFamily.SansSerif
    FontFamilyChoice.SYSTEM_DEFAULT -> FontFamily.Default
  }
  val targetFontWeight = if (fontSettings.isBoldText) FontWeight.Bold else FontWeight.Normal

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Font Settings", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("font_settings_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showResetDialog = true }, modifier = Modifier.testTag("reset_font_button")) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TealPrimary)
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
        .testTag("font_settings_screen")
    ) {
      // 1. LIVE MULTILINGUAL SCRIPT PREVIEW CARD
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("font_preview_card"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Live Script Preview",
              style = MaterialTheme.typography.labelSmall,
              color = TealPrimary,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // English sample
            Text(
              text = "English: ChatMitra is fast, secure, and encrypted.",
              fontSize = fontSettings.chatMessageSizeSp.sp,
              fontFamily = targetFontFamily,
              fontWeight = targetFontWeight,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Hindi sample
            Text(
              text = "हिन्दी: चैटमित्र आपको अपनों से जोड़ता है।",
              fontSize = fontSettings.chatMessageSizeSp.sp,
              fontFamily = targetFontFamily,
              fontWeight = targetFontWeight,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Urdu RTL sample
            Text(
              text = "اردو: چیٹ مترا آپ کو اپنے پیاروں سے جوڑتا ہے۔",
              fontSize = fontSettings.chatMessageSizeSp.sp,
              fontFamily = targetFontFamily,
              fontWeight = targetFontWeight,
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // 2. PRESETS ROW
      item {
        Text(
          text = "FONT SIZE PRESETS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 6.dp)
        )
      }

      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FontSizePreset.values().forEach { preset ->
            FilterChip(
              selected = fontSettings.preset == preset,
              onClick = { AppearanceRepository.setFontPreset(preset) },
              label = { Text(preset.label.take(7), fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                selectedLabelColor = TealPrimary
              ),
              modifier = Modifier.testTag("preset_${preset.name}")
            )
          }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }

      // 3. SLIDERS FOR GRANULAR TEXT SIZING
      item {
        Text(
          text = "GRANULAR SIZE CONTROLS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 6.dp)
        )
      }

      // Chat Message Size Slider
      item {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Chat Message Text Size", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("${fontSettings.chatMessageSizeSp} sp", color = TealPrimary, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = fontSettings.chatMessageSizeSp.toFloat(),
            onValueChange = { AppearanceRepository.setChatMessageFontSize(it.toInt()) },
            valueRange = 12f..22f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary),
            modifier = Modifier.testTag("chat_message_size_slider")
          )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Chat List Text Size Slider
      item {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Chat List Contact Name Size", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("${fontSettings.chatListSizeSp} sp", color = TealPrimary, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = fontSettings.chatListSizeSp.toFloat(),
            onValueChange = { AppearanceRepository.setChatListFontSize(it.toInt()) },
            valueRange = 12f..22f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary),
            modifier = Modifier.testTag("chat_list_size_slider")
          )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Message Composer Text Size Slider
      item {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Message Composer Input Size", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("${fontSettings.composerSizeSp} sp", color = TealPrimary, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = fontSettings.composerSizeSp.toFloat(),
            onValueChange = { AppearanceRepository.setComposerFontSize(it.toInt()) },
            valueRange = 12f..22f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary),
            modifier = Modifier.testTag("composer_size_slider")
          )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 4. FONT FAMILY
      item {
        Text(
          text = "FONT FAMILY",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
        )
      }

      items(FontFamilyChoice.values().toList()) { choice ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { AppearanceRepository.setFontFamily(choice) }
            .padding(horizontal = 24.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = fontSettings.fontFamily == choice,
            onClick = { AppearanceRepository.setFontFamily(choice) },
            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(text = choice.label, fontSize = 14.sp)
        }
      }

      item {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }

      // 5. BOLD TEXT TOGGLE
      item {
        ListItem(
          headlineContent = { Text("Bold Text", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Applies bold styling across headers and chat text", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.FormatBold, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = fontSettings.isBoldText,
              onCheckedChange = { AppearanceRepository.setBoldText(it) },
              modifier = Modifier.testTag("bold_text_switch")
            )
          }
        )
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }

    // Reset Defaults Dialog
    if (showResetDialog) {
      AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("Reset Font Settings?") },
        text = { Text("Restore default Medium text sizes and standard Android system font family?") },
        confirmButton = {
          TextButton(
            onClick = {
              AppearanceRepository.resetFontToDefault()
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
