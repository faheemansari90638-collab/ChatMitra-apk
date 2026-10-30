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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDashboardScreen(
  onBackClick: () -> Unit,
  onNavigateToPrivacyCheckup: () -> Unit,
  onNavigateToTwoStep: () -> Unit,
  onNavigateToLinkedDevices: () -> Unit,
  onNavigateToBlockedContacts: () -> Unit,
  onNavigateToPrivacy: () -> Unit
) {
  val settings by ChatMitraRepository.privacySettings.collectAsState()
  val linkedDevices by ChatMitraRepository.linkedDevices.collectAsState()
  val blockedContacts by ChatMitraRepository.blockedContactIds.collectAsState()
  val auditEvents by ChatMitraRepository.securityAuditEvents.collectAsState()

  var showExportDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Security Dashboard", fontWeight = FontWeight.Bold) },
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
      // 1. Privacy Checkup Hero Banner
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPrivacyCheckup() }
            .testTag("privacy_checkup_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = TealDark)
        ) {
          Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(AmberAccent.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Privacy Checkup",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                "Choose the right privacy settings for you across 4 simple steps.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
          }
        }
      }

      // 2. Core Security Status Grid
      item {
        Text("Account Protection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }

      item {
        // Two-Step Verification Card
        SecurityStatusCard(
          title = "Two-Step Verification",
          subtitle = if (settings.twoStepVerificationEnabled) "Active • 6-digit PIN enabled" else "Disabled • Recommend turning on",
          isActive = settings.twoStepVerificationEnabled,
          icon = Icons.Default.Key,
          onClick = onNavigateToTwoStep
        )
      }

      item {
        // App Lock Card
        SecurityStatusCard(
          title = "App Lock & Biometrics",
          subtitle = if (settings.appLockEnabled) "Enabled • ${settings.appLockTimeout.label}" else "Disabled",
          isActive = settings.appLockEnabled,
          icon = Icons.Default.Fingerprint,
          onClick = onNavigateToPrivacy
        )
      }

      item {
        // Linked Devices Card
        SecurityStatusCard(
          title = "Linked Devices",
          subtitle = "${linkedDevices.size} active sessions • End-to-end synced",
          isActive = true,
          icon = Icons.Default.Devices,
          onClick = onNavigateToLinkedDevices
        )
      }

      item {
        // Blocked Contacts Card
        SecurityStatusCard(
          title = "Blocked Contacts",
          subtitle = "${blockedContacts.size} contacts blocked from messaging and calls",
          isActive = blockedContacts.isNotEmpty(),
          icon = Icons.Default.Block,
          onClick = onNavigateToBlockedContacts
        )
      }

      // 3. Recent Security Events Log
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Recent Security Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }

      items(auditEvents, key = { it.id }) { event ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (event.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (event.isPositive) CallGreen else AmberAccent,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(event.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
              Text(event.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(event.timestampFormatted, style = MaterialTheme.typography.labelSmall, color = CheckmarkGrey)
          }
        }
      }

      // 4. Data & Account Controls
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Text("Data & Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }

      item {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedButton(
            onClick = { showExportDialog = true },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export Data")
          }

          Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = CallRed),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete Account")
          }
        }
      }
    }
  }

  // Export Data Confirmation Dialog
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Account Information") },
      text = {
        Text("Your account details, chat logs, and privacy preferences will be compiled into an encrypted JSON export. (Locked chats are excluded from plain export for security).")
      },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.addSecurityAuditEvent("Data Exported", "Encrypted archive created.", true)
            showExportDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Download Archive")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
      }
    )
  }

  // Delete Account Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Account?", color = CallRed, fontWeight = FontWeight.Bold) },
      text = {
        Text("Deleting your account will erase your message history, delete you from all groups, and revoke all linked device sessions. This action cannot be undone.")
      },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.deleteAccount()
            showDeleteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CallRed)
        ) {
          Text("Delete Forever")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun SecurityStatusCard(
  title: String,
  subtitle: String,
  isActive: Boolean,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isActive) TealPrimary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isActive) TealPrimary else Color.Gray,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey)
    }
  }
}
