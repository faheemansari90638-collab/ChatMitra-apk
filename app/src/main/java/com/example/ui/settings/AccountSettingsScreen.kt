package com.example.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.EmailAccountRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
  onBackClick: () -> Unit,
  onNavigateEmail: () -> Unit,
  onNavigateTwoStep: () -> Unit,
  onNavigateLinkedDevices: () -> Unit,
  onNavigateExportData: () -> Unit,
  onNavigateDeleteAccount: () -> Unit,
  modifier: Modifier = Modifier
) {
  val emailState by EmailAccountRepository.getInstance().emailState.collectAsState()
  val privacySettings by ChatMitraRepository.privacySettings.collectAsState()
  val linkedDevices by ChatMitraRepository.linkedDevices.collectAsState()
  var securityAlertsEnabled by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Account", fontWeight = FontWeight.Bold, color = Color.White) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("account_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = TealDark)
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Security Score Card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Security,
              contentDescription = null,
              tint = TealPrimary,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Account Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              val isEmailVerified = emailState.isVerified
              val isTwoStepOn = privacySettings.twoStepVerificationEnabled
              val score = (if (isEmailVerified) 50 else 20) + (if (isTwoStepOn) 50 else 20)
              Text(
                text = "Health Score: $score/100 • ${if (score >= 80) "Strong Protection" else "Enhancement Recommended"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (score >= 80) TealPrimary else AmberAccent,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Email Address
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateEmail() }
            .testTag("account_email_item"),
          headlineContent = { Text("Email Address", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            if (emailState.email.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emailState.email, color = CheckmarkGrey)
                if (emailState.isVerified) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            } else {
              Text("Add email for account recovery & alerts", color = CheckmarkGrey)
            }
          },
          leadingContent = {
            Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = CheckmarkGrey,
              modifier = Modifier.size(18.dp)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Two-Step Verification
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateTwoStep() }
            .testTag("account_two_step_item"),
          headlineContent = { Text("Two-Step Verification", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(
              if (privacySettings.twoStepVerificationEnabled) "Enabled (6-digit PIN protected)" else "Turn on for enhanced sign-in security",
              color = if (privacySettings.twoStepVerificationEnabled) TealPrimary else CheckmarkGrey
            )
          },
          leadingContent = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = CheckmarkGrey,
              modifier = Modifier.size(18.dp)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Security Alerts Toggle
      item {
        ListItem(
          headlineContent = { Text("Security Notifications", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(
              "Receive alerts when new devices link or security settings change",
              color = CheckmarkGrey
            )
          },
          leadingContent = {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = securityAlertsEnabled,
              onCheckedChange = { securityAlertsEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Linked Devices
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateLinkedDevices() }
            .testTag("account_linked_devices_item"),
          headlineContent = { Text("Linked Devices", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("${linkedDevices.size} active sessions • Remote logout", color = CheckmarkGrey)
          },
          leadingContent = {
            Icon(Icons.Default.Devices, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = CheckmarkGrey,
              modifier = Modifier.size(18.dp)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Export My Data
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateExportData() }
            .testTag("account_export_data_item"),
          headlineContent = { Text("Export My Data", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Request an official ZIP archive of your account and chats", color = CheckmarkGrey)
          },
          leadingContent = {
            Icon(Icons.Default.Download, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = CheckmarkGrey,
              modifier = Modifier.size(18.dp)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Delete My Account
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateDeleteAccount() }
            .testTag("account_delete_account_item"),
          headlineContent = {
            Text("Delete My Account", fontWeight = FontWeight.SemiBold, color = Color.Red)
          },
          supportingContent = {
            Text("Permanently erase account, messages, and cloud backups", color = Color.Red.copy(alpha = 0.8f))
          },
          leadingContent = {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color.Red.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        )
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }
  }
}
