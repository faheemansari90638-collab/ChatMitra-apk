package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.repository.LocalizationRepository
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
  onBackClick: () -> Unit
) {
  val currentLanguage by LocalizationRepository.currentLanguage.collectAsState()
  val supportedLanguages = AppLanguage.entries

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            LocalizationRepository.t("settings_language", "App Language"),
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Translate, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                "Choose Your Language",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                "ChatMitra fully supports English, Hindi (हिन्दी), Urdu (اردو), and Arabic (العربية) with proper Right-to-Left (RTL) formatting.",
                color = CheckmarkGrey,
                fontSize = 12.sp
              )
            }
          }
        }
      }

      items(supportedLanguages, key = { it.code }) { lang ->
        val isSelected = currentLanguage == lang

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { LocalizationRepository.setLanguage(lang) }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("language_option_${lang.code}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(
            selected = isSelected,
            onClick = { LocalizationRepository.setLanguage(lang) },
            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = lang.nativeName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
              )
              if (lang.isRtl) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                  Text(
                    "RTL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = lang.displayName,
              fontSize = 13.sp,
              color = CheckmarkGrey
            )
          }

          if (isSelected) {
            Icon(
              Icons.Default.Check,
              contentDescription = "Selected",
              tint = TealPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }
    }
  }
}
