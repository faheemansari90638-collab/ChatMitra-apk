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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCheckupScreen(
  onBackClick: () -> Unit,
  onNavigateToPrivacy: () -> Unit,
  onNavigateToTwoStep: () -> Unit
) {
  val settings by ChatMitraRepository.privacySettings.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Privacy Checkup", fontWeight = FontWeight.Bold) },
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
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text("Your privacy matters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Review recommendations and strengthen your security settings across ChatMitra.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Step 1: Who can contact you
      item {
        CheckupStepCard(
          stepNumber = "1",
          title = "Choose who can contact you",
          description = "Control groups and unknown callers. Currently: Groups (${settings.groupsAddPermission.label}), Silence unknown (${if (settings.silenceUnknownCallers) "On" else "Off"}).",
          icon = Icons.Default.Group,
          onClick = onNavigateToPrivacy
        )
      }

      // Step 2: Control your personal info
      item {
        CheckupStepCard(
          stepNumber = "2",
          title = "Control your personal info",
          description = "Manage Last Seen (${settings.lastSeenVisibility.label}), Profile photo (${settings.profilePhotoVisibility.label}), and Read receipts (${if (settings.readReceipts) "On" else "Off"}).",
          icon = Icons.Default.Person,
          onClick = onNavigateToPrivacy
        )
      }

      // Step 3: Add more privacy to your chats
      item {
        CheckupStepCard(
          stepNumber = "3",
          title = "Add more privacy to your chats",
          description = "Default message timer is ${settings.disappearingMessagesDefault.label}. Lock individual conversations for extra privacy.",
          icon = Icons.Default.Chat,
          onClick = onNavigateToPrivacy
        )
      }

      // Step 4: Add more protection to your account
      item {
        CheckupStepCard(
          stepNumber = "4",
          title = "Add more protection to your account",
          description = "Two-Step Verification is ${if (settings.twoStepVerificationEnabled) "Active" else "Disabled"}. App Lock is ${if (settings.appLockEnabled) "Enabled" else "Off"}.",
          icon = Icons.Default.Lock,
          onClick = onNavigateToTwoStep
        )
      }

      item {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
          onClick = onBackClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Done", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun CheckupStepCard(
  stepNumber: String,
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
  ) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(TealPrimary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text("STEP $stepNumber", style = MaterialTheme.typography.labelSmall, color = TealPrimary, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey)
    }
  }
}
