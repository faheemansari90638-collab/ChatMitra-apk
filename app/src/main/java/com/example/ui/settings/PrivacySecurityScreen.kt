package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.AppLockTimeout
import com.example.data.model.DisappearingDuration
import com.example.data.model.NotificationPrivacy
import com.example.data.model.OnlineStatusOption
import com.example.data.model.VisibilityOption
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

enum class PrivacyDialogTarget {
  LAST_SEEN,
  ONLINE_STATUS,
  PROFILE_PHOTO,
  ABOUT,
  STATUS,
  GROUPS,
  DEFAULT_TIMER,
  APP_LOCK_TIMEOUT,
  NOTIFICATION_PREVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
  onBackClick: () -> Unit,
  onNavigateSecurityDashboard: () -> Unit = {},
  onNavigateBlockedContacts: () -> Unit = {},
  onNavigateTwoStep: () -> Unit = {},
  onNavigatePrivacyCheckup: () -> Unit = {}
) {
  val settings by ChatMitraRepository.privacySettings.collectAsState()
  val blockedIds by ChatMitraRepository.blockedContactIds.collectAsState()

  var dialogTarget by remember { mutableStateOf<PrivacyDialogTarget?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Privacy", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("privacy_back_button")) {
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
    ) {
      // Privacy Checkup Banner
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onNavigatePrivacyCheckup() }
            .testTag("privacy_checkup_banner"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(TealPrimary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Privacy Checkup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text("Control your privacy and choose the right settings for you.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
          }
        }
      }

      // Who can see my personal info
      item {
        Text(
          text = "Who can see my personal info",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.LAST_SEEN }.testTag("privacy_last_seen_row"),
          headlineContent = { Text("Last seen", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.lastSeenVisibility.label, color = CheckmarkGrey) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.ONLINE_STATUS }.testTag("privacy_online_status_row"),
          headlineContent = { Text("Online status", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.onlineStatusVisibility.label, color = CheckmarkGrey) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.PROFILE_PHOTO }.testTag("privacy_profile_photo_row"),
          headlineContent = { Text("Profile photo", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.profilePhotoVisibility.label, color = CheckmarkGrey) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.ABOUT }.testTag("privacy_about_row"),
          headlineContent = { Text("About", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.aboutVisibility.label, color = CheckmarkGrey) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.STATUS }.testTag("privacy_status_row"),
          headlineContent = { Text("Status", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.statusPrivacy.label, color = CheckmarkGrey) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Read receipts", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(
              "If turned off, you won't send or receive read receipts (blue double checkmarks).",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey
            )
          },
          trailingContent = {
            Switch(
              checked = settings.readReceipts,
              onCheckedChange = { ChatMitraRepository.updatePrivacySettings(readReceipts = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary),
              modifier = Modifier.testTag("privacy_read_receipts_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Disappearing Messages
      item {
        Text(
          text = "Disappearing messages",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.DEFAULT_TIMER }.testTag("privacy_default_timer_row"),
          headlineContent = { Text("Default message timer", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Start new chats with disappearing messages: ${settings.disappearingMessagesDefault.label}", color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Timer, contentDescription = null, tint = TealPrimary) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Groups & Calls
      item {
        Text(
          text = "Groups & Calls",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.GROUPS }.testTag("privacy_groups_row"),
          headlineContent = { Text("Groups", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Who can add me: ${settings.groupsAddPermission.label}", color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Group, contentDescription = null, tint = TealPrimary) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Silence unknown callers", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Calls from unknown numbers will be silenced and shown in the Calls tab.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          },
          leadingContent = { Icon(Icons.Default.Call, contentDescription = null, tint = TealPrimary) },
          trailingContent = {
            Switch(
              checked = settings.silenceUnknownCallers,
              onCheckedChange = { ChatMitraRepository.updatePrivacySettings(silenceUnknownCallers = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary),
              modifier = Modifier.testTag("silence_unknown_callers_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { onNavigateBlockedContacts() }.testTag("privacy_blocked_contacts_row"),
          headlineContent = { Text("Blocked contacts", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("${blockedIds.size} contacts blocked", color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Block, contentDescription = null, tint = AmberAccent) },
          trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(14.dp)) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Security & App Lock
      item {
        Text(
          text = "Security & App Lock",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { onNavigateSecurityDashboard() }.testTag("privacy_security_dashboard_row"),
          headlineContent = { Text("Security Dashboard", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Manage 2FA, linked devices, audit logs & account data", color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Security, contentDescription = null, tint = TealPrimary) },
          trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(14.dp)) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { onNavigateTwoStep() }.testTag("privacy_two_step_row"),
          headlineContent = { Text("Two-step verification", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(if (settings.twoStepVerificationEnabled) "Enabled (6-digit PIN active)" else "Off", color = CheckmarkGrey)
          },
          leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary) },
          trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(14.dp)) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("App lock (Biometrics / PIN)", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Require biometric authentication or PIN to open ChatMitra.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          },
          leadingContent = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = TealPrimary) },
          trailingContent = {
            Switch(
              checked = settings.appLockEnabled,
              onCheckedChange = { ChatMitraRepository.updatePrivacySettings(appLock = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary),
              modifier = Modifier.testTag("app_lock_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      if (settings.appLockEnabled) {
        item {
          ListItem(
            modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.APP_LOCK_TIMEOUT }.testTag("app_lock_timeout_row"),
            headlineContent = { Text("Automatically lock", fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(settings.appLockTimeout.label, color = CheckmarkGrey) },
            leadingContent = { Icon(Icons.Default.HourglassTop, contentDescription = null, tint = TealPrimary) }
          )
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
      }

      item {
        ListItem(
          headlineContent = { Text("Show security notifications", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Get notified when a contact's encryption code changes.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          },
          leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary) },
          trailingContent = {
            Switch(
              checked = settings.securityNotificationsEnabled,
              onCheckedChange = { ChatMitraRepository.updatePrivacySettings(securityNotifications = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary),
              modifier = Modifier.testTag("security_notifications_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier.clickable { dialogTarget = PrivacyDialogTarget.NOTIFICATION_PREVIEW }.testTag("notification_preview_row"),
          headlineContent = { Text("Notification privacy", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.notificationPrivacy.label, color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Media & Download Privacy
      item {
        Text(
          text = "Media & Download Privacy",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("Save media to phone gallery", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Show newly downloaded media in your phone's gallery.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          },
          leadingContent = { Icon(Icons.Default.Download, contentDescription = null, tint = TealPrimary) },
          trailingContent = {
            Switch(
              checked = settings.saveMediaToGallery,
              onCheckedChange = { ChatMitraRepository.updatePrivacySettings(saveMediaToGallery = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Auto-download photos on mobile data", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Voice notes and documents are always downloaded on Wi-Fi only.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          },
          trailingContent = {
            Switch(
              checked = settings.autoDownloadPhotosMobile,
              onCheckedChange = { ChatMitraRepository.updatePrivacySettings(autoDownloadPhotos = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        Spacer(modifier = Modifier.height(32.dp))
      }
    }

    // Dialogs for Settings
    dialogTarget?.let { target ->
      when (target) {
        PrivacyDialogTarget.LAST_SEEN -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Who can see my last seen", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                VisibilityOption.values().forEach { option ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(lastSeen = option)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.lastSeenVisibility == option,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(option.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.ONLINE_STATUS -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Who can see when I'm online", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                OnlineStatusOption.values().forEach { option ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(onlineStatus = option)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.onlineStatusVisibility == option,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(option.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.PROFILE_PHOTO -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Who can see my profile photo", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                VisibilityOption.values().forEach { option ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(profilePhoto = option)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.profilePhotoVisibility == option,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(option.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.ABOUT -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Who can see my About", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                VisibilityOption.values().forEach { option ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(about = option)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.aboutVisibility == option,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(option.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.STATUS -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Who can see my status updates", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                VisibilityOption.values().forEach { option ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(statusPrivacy = option)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.statusPrivacy == option,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(option.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.GROUPS -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Who can add me to groups", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                VisibilityOption.values().forEach { option ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(groupsAddPermission = option)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.groupsAddPermission == option,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(option.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.DEFAULT_TIMER -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Default message timer", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                DisappearingDuration.values().forEach { duration ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(disappearingMessagesDefault = duration)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.disappearingMessagesDefault == duration,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(duration.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.APP_LOCK_TIMEOUT -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Automatically lock", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                AppLockTimeout.values().forEach { timeout ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(appLockTimeout = timeout)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.appLockTimeout == timeout,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(timeout.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }

        PrivacyDialogTarget.NOTIFICATION_PREVIEW -> {
          AlertDialog(
            onDismissRequest = { dialogTarget = null },
            title = { Text("Notification privacy", fontWeight = FontWeight.Bold) },
            text = {
              Column {
                NotificationPrivacy.values().forEach { privacy ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        ChatMitraRepository.updatePrivacySettings(notificationPrivacy = privacy)
                        dialogTarget = null
                      }
                      .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = settings.notificationPrivacy == privacy,
                      onClick = null,
                      colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                    )
                    Text(privacy.label, modifier = Modifier.padding(start = 12.dp))
                  }
                }
              }
            },
            confirmButton = {
              TextButton(onClick = { dialogTarget = null }) { Text("Done") }
            }
          )
        }
      }
    }
  }
}
