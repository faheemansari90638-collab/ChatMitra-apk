package com.example.ui.group

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
  chatId: String,
  onBackClick: () -> Unit
) {
  val chats by ChatMitraRepository.chats.collectAsState()
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val clipboardManager: ClipboardManager = LocalClipboardManager.current

  val chat = chats.find { it.id == chatId }
  var showSafetyDialog by remember { mutableStateOf(false) }
  var copiedToast by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (chat?.isGroup == true) "Group Info" else "Contact Info") },
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
    if (chat == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Chat not found")
      }
      return@Scaffold
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Header Card with Avatar
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          UserAvatar(
            name = chat.name,
            colorHex = chat.avatarColorHex,
            emoji = chat.avatarEmoji,
            size = 90.dp,
            fontSize = 40
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = chat.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (chat.isGroup) "Group • ${chat.participants.size + 1} participants" else (chat.participants.firstOrNull()?.phone ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = CheckmarkGrey
          )
        }
      }

      // Description / About
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = if (chat.isGroup) "Description" else "About",
              style = MaterialTheme.typography.labelMedium,
              color = TealPrimary,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = chat.groupDescription ?: (chat.participants.firstOrNull()?.about ?: "Hey there! I am using ChatMitra."),
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }

      // Group Invite Link (if Group)
      if (chat.isGroup && chat.inviteLink != null) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "Invite via Link", fontWeight = FontWeight.SemiBold)
                  Text(text = chat.inviteLink ?: "", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = {
                    clipboardManager.setText(AnnotatedString(chat.inviteLink ?: ""))
                    copiedToast = true
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                  Text(if (copiedToast) "Copied!" else "Copy Link")
                }

                TextButton(
                  onClick = {
                    ChatMitraRepository.resetGroupInviteLink(chat.id)
                  }
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, tint = AmberAccent)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reset Link", color = AmberAccent)
                }
              }
            }
          }
        }
      }

      // Encryption Safety Verification
      item {
        ListItem(
          modifier = Modifier
            .clickable { showSafetyDialog = true }
            .testTag("verify_encryption_row"),
          headlineContent = { Text("Encryption", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(
              "Messages and calls are end-to-end encrypted. Tap to verify security code.",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey
            )
          },
          leadingContent = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = AmberAccent)
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Notifications Mute
      item {
        ListItem(
          headlineContent = { Text("Mute Notifications", fontWeight = FontWeight.SemiBold) },
          trailingContent = {
            Switch(
              checked = chat.isMuted,
              onCheckedChange = { ChatMitraRepository.toggleMuteChat(chat.id) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          },
          leadingContent = {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary)
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Participants (if group)
      if (chat.isGroup) {
        item {
          Text(
            text = "${chat.participants.size + 1} Participants",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
          )
        }

        // Current User Item
        item {
          ListItem(
            headlineContent = { Text("You (${currentUser?.name})", fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text("Group Admin", style = MaterialTheme.typography.bodySmall, color = TealPrimary) },
            leadingContent = {
              UserAvatar(
                name = currentUser?.name ?: "Me",
                colorHex = currentUser?.avatarColorHex ?: 0xFF00A884,
                emoji = currentUser?.avatarEmoji ?: "✨",
                size = 44.dp
              )
            }
          )
        }

        items(chat.participants) { participant ->
          val isAdmin = chat.groupAdmins.contains(participant.id)
          ListItem(
            headlineContent = { Text(participant.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = {
              Text(
                if (isAdmin) "Group Admin" else participant.about,
                style = MaterialTheme.typography.bodySmall,
                color = if (isAdmin) TealPrimary else CheckmarkGrey
              )
            },
            leadingContent = {
              UserAvatar(
                name = participant.name,
                colorHex = participant.avatarColorHex,
                emoji = participant.avatarEmoji,
                size = 44.dp,
                isOnline = participant.isOnline
              )
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(40.dp))
      }
    }

    if (showSafetyDialog) {
      SafetyNumberDialog(
        contactName = chat.name,
        fingerprint = chat.participants.firstOrNull()?.publicKeyFingerprint ?: "4819 2038 9128 4471 9023 1184 7291 0029",
        onDismiss = { showSafetyDialog = false }
      )
    }
  }
}
