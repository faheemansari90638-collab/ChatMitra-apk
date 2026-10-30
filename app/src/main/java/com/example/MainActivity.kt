package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.repository.LocalizationRepository
import com.example.ui.navigation.ChatMitraApp
import com.example.ui.theme.ChatMitraTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    com.example.data.calling.ChatMitraNotificationHelper.initializeNotificationChannels(this)
    com.example.data.service.FirestoreChatService.init(this)
    setContent {
      val currentLanguage by LocalizationRepository.currentLanguage.collectAsState()
      CompositionLocalProvider(
        LocalLayoutDirection provides if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
      ) {
        ChatMitraTheme {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            ChatMitraApp()
          }
        }
      }
    }
  }
}
