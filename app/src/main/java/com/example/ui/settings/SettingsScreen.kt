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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary

@Composable
fun SettingsScreen(
  onNavigatePrivacy: () -> Unit,
  onLogout: () -> Unit,
  onNavigateAccount: () -> Unit = {},
  onNavigateChatBackup: () -> Unit = {},
  onNavigateStorageData: () -> Unit = {},
  onNavigateDeleteAccount: () -> Unit = {},
  onNavigateNotifications: () -> Unit = {},
  onNavigateCallsSettings: () -> Unit = {},
  onNavigateHamzaAi: () -> Unit = {},
  onNavigateAppearance: () -> Unit = {},
  onNavigateInviteFriends: () -> Unit = {},
  onNavigateAppUpdates: () -> Unit = {},
  onNavigateLanguage: () -> Unit = {},
  onNavigateContactsFriends: () -> Unit = {},
  onNavigateAdminDashboard: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val callNotificationSettings by com.example.data.repository.CallNotificationRepository.settings.collectAsState()

  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showLogoutConfirm by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showQrDialog by remember { mutableStateOf(false) }

  // Profile Edit State
  var editName by remember { mutableStateOf(currentUser?.name ?: "") }
  var editAbout by remember { mutableStateOf(currentUser?.about ?: "") }
  var editEmoji by remember { mutableStateOf(currentUser?.avatarEmoji ?: "✨") }
  var editColorHex by remember { mutableLongStateOf(currentUser?.avatarColorHex ?: 0xFF00A884) }

  val emojiOptions = listOf("✨", "🚀", "🌸", "🎨", "⚡", "🌟", "🦁", "☕", "💻", "🔥")
  val colorOptions = listOf(0xFF00A884, 0xFF008069, 0xFF3F51B5, 0xFFE91E63, 0xFF9C27B0, 0xFFFF9800)

  LazyColumn(modifier = modifier.fillMaxSize()) {
    // User Profile Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .clickable {
            editName = currentUser?.name ?: ""
            editAbout = currentUser?.about ?: ""
            editEmoji = currentUser?.avatarEmoji ?: "✨"
            editColorHex = currentUser?.avatarColorHex ?: 0xFF00A884
            showEditProfileDialog = true
          }
          .testTag("user_profile_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          UserAvatar(
            name = currentUser?.name ?: "User",
            colorHex = currentUser?.avatarColorHex ?: 0xFF00A884,
            emoji = currentUser?.avatarEmoji ?: "✨",
            size = 64.dp,
            fontSize = 28
          )
          Spacer(modifier = Modifier.width(16.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = currentUser?.name ?: "ChatMitra User",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = currentUser?.about ?: "Available",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              maxLines = 1
            )
            Text(
              text = currentUser?.phone ?: "",
              style = MaterialTheme.typography.labelSmall,
              color = TealPrimary,
              fontWeight = FontWeight.SemiBold
            )
          }
          IconButton(onClick = { showQrDialog = true }) {
            Icon(Icons.Default.QrCode, contentDescription = "My QR Code", tint = TealPrimary)
          }
        }
      }
    }

    // Settings Menu Items
    // 0. Hamza AI Assistant
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateHamzaAi() }
          .testTag("settings_hamza_ai_item"),
        headlineContent = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hamza AI Assistant", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF6366F1).copy(alpha = 0.2f)
            ) {
              Text(
                "AI",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6366F1),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
              )
            }
          }
        },
        supportingContent = { Text("Ask questions, smart replies, translate & summarize chats", color = CheckmarkGrey) },
        leadingContent = {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          }
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 1. Account Hub (Email, 2FA, Export, Delete)
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateAccount() }
          .testTag("settings_account_item"),
        headlineContent = { Text("Account", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Email linking, two-step verification, export & delete", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.Key, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 2. Privacy
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigatePrivacy() }
          .testTag("settings_privacy_item"),
        headlineContent = { Text("Privacy", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Block contacts, disappearing messages, last seen", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 3. Appearance & Display (Themes, Wallpapers, Font Settings)
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateAppearance() }
          .testTag("settings_appearance_item"),
        headlineContent = { Text("Appearance & Display", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Theme mode, emerald/sapphire accent, chat wallpapers & fonts", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.Palette, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 4. Chats & Media (Chat Backup)
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateChatBackup() }
          .testTag("settings_chat_backup_item"),
        headlineContent = { Text("Chats & Backup", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("End-to-end encrypted backup, Google Drive, restore", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 5. Calls & Ringtone Settings
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateCallsSettings() }
          .testTag("settings_calls_item"),
        headlineContent = { Text("Calls & Ringtones", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Ringtones, vibration, call waiting, silent mode & HD calling", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.Call, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 6. Notifications
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateNotifications() }
          .testTag("settings_notifications_item"),
        headlineContent = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Message, group & call tones, vibration & silent mode", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 7. Storage and Data
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateStorageData() }
          .testTag("settings_storage_item"),
        headlineContent = { Text("Storage and Data", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Manage storage, large files, network usage & cache", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.DataUsage, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 8. Invite Friends
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateInviteFriends() }
          .testTag("settings_invite_friends_item"),
        headlineContent = { Text("Invite Friends", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Share official ChatMitra link and QR code to another phone", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AmberAccent)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 9. App Download & Installation Guide
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateAppUpdates() }
          .testTag("settings_app_updates_item"),
        headlineContent = { Text("App Updates & Installation", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("APK fingerprint verification, debug vs release, installer guide", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 10. App Language & Localization
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateLanguage() }
          .testTag("settings_language_item"),
        headlineContent = { Text("App Language", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("English, Hindi (हिन्दी), Urdu (اردو), Arabic (العربية)", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.Language, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 11. Contacts & Friends Hub
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateContactsFriends() }
          .testTag("settings_contacts_friends_item"),
        headlineContent = { Text("Contacts & Friends", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Sync phone contacts, @username search, QR code profile", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.People, contentDescription = null, tint = TealPrimary)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 12. Admin Moderation Console
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateAdminDashboard() }
          .testTag("settings_admin_dashboard_item"),
        headlineContent = { Text("Admin Moderation Console", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Reports queue, suspension controls, appeals, service health", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AmberAccent)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(18.dp))
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 10. About
    item {
      ListItem(
        modifier = Modifier.clickable { showAboutDialog = true },
        headlineContent = { Text("About ChatMitra", fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("Version 2.0.0 • Hamza AI • HD Calling • E2EE", color = CheckmarkGrey) },
        leadingContent = {
          Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = AmberAccent)
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 7. Log Out
    item {
      ListItem(
        modifier = Modifier
          .clickable { showLogoutConfirm = true }
          .testTag("logout_button"),
        headlineContent = { Text("Log Out", fontWeight = FontWeight.SemiBold, color = AmberAccent) },
        leadingContent = {
          Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AmberAccent)
        }
      )
      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }

    // 8. Delete Account
    item {
      ListItem(
        modifier = Modifier
          .clickable { onNavigateDeleteAccount() }
          .testTag("delete_account_button"),
        headlineContent = { Text("Delete Account", fontWeight = FontWeight.SemiBold, color = Color.Red) },
        supportingContent = { Text("Permanently delete your account and all data", color = Color.Red.copy(alpha = 0.7f)) },
        leadingContent = {
          Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
        },
        trailingContent = {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(60.dp))
    }
  }

  // Edit Profile Dialog
  if (showEditProfileDialog) {
    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Avatar Picker
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(editColorHex))
                .border(2.dp, TealPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(text = editEmoji, fontSize = 32.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(emojiOptions) { emoji ->
              Surface(
                shape = CircleShape,
                color = if (editEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(36.dp)
                  .clickable { editEmoji = emoji }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(text = emoji, fontSize = 18.sp)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = editAbout,
            onValueChange = { editAbout = it },
            label = { Text("About Status") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            ChatMitraRepository.updateProfile(
              name = editName.ifBlank { "User" },
              about = editAbout.ifBlank { "Available" },
              emoji = editEmoji,
              colorHex = editColorHex
            )
            showEditProfileDialog = false
          }
        ) {
          Text("Save", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Logout Confirm Dialog
  if (showLogoutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirm = false },
      title = { Text("Log out of ChatMitra?") },
      text = { Text("You can log back in anytime with your phone number and OTP.") },
      confirmButton = {
        TextButton(
          onClick = {
            showLogoutConfirm = false
            ChatMitraRepository.logout()
            onLogout()
          }
        ) {
          Text("Log Out", color = AmberAccent, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }


  // About ChatMitra Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      icon = {
        Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(36.dp))
      },
      title = { Text("ChatMitra 1.0.0", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("ChatMitra is a modern, privacy-focused instant messenger built for Android with Jetpack Compose.")
          Spacer(modifier = Modifier.height(8.dp))
          Text("• 256-bit End-to-End Encryption")
          Text("• Real-time one-to-one & group chats")
          Text("• WebRTC audio & video calling with STUN/TURN")
          Text("• Status stories with 24-hour expiration")
          Text("• Voice notes & high-speed media sharing")
        }
      },
      confirmButton = {
        TextButton(onClick = { showAboutDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // My QR Code Dialog
  if (showQrDialog) {
    SafetyNumberDialog(
      contactName = currentUser?.name ?: "You",
      fingerprint = currentUser?.publicKeyFingerprint ?: "7731 9284 1029 3847 9182 4619 0291 8831",
      onDismiss = { showQrDialog = false }
    )
  }
}
