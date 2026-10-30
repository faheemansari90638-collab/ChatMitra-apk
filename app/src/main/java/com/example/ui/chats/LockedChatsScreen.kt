package com.example.ui.chats

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockedChatsScreen(
  onBackClick: () -> Unit,
  onChatClick: (String) -> Unit
) {
  val isAuthenticated by ChatMitraRepository.isLockedChatsAuthenticated.collectAsState()
  val lockedIds by ChatMitraRepository.lockedChatIds.collectAsState()
  val allChats by ChatMitraRepository.chats.collectAsState()
  val secretCode by ChatMitraRepository.secretCode.collectAsState()
  val secretFolderEnabled by ChatMitraRepository.secretLockedChatsEnabled.collectAsState()
  val wrongAttempts by ChatMitraRepository.wrongPinAttempts.collectAsState()
  val cooldownUntil by ChatMitraRepository.cooldownUntilMillis.collectAsState()

  val lockedChats = allChats.filter { lockedIds.contains(it.id) }

  var inputPin by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showSecretSettingsDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Locked Chats", fontWeight = FontWeight.Bold)
          }
        },
        navigationIcon = {
          IconButton(onClick = {
            ChatMitraRepository.relockChats()
            onBackClick()
          }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          if (isAuthenticated) {
            IconButton(onClick = { showSecretSettingsDialog = true }) {
              Icon(Icons.Default.Key, contentDescription = "Secret Code", tint = Color.White)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    }
  ) { paddingValues ->
    if (!isAuthenticated) {
      // Authentication Barrier Screen
      val isCoolingDown = System.currentTimeMillis() < cooldownUntil
      val remainingSeconds = ((cooldownUntil - System.currentTimeMillis()) / 1000).coerceAtLeast(0)

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(TealPrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Biometric Lock",
            tint = TealPrimary,
            modifier = Modifier.size(54.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "ChatMitra Locked Chats",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Enter your device PIN or biometric code to unlock your private conversations.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isCoolingDown) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = CallRed.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "⏳ Too many incorrect attempts. Locked for $remainingSeconds seconds.",
              color = CallRed,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(14.dp),
              textAlign = TextAlign.Center
            )
          }
        } else {
          OutlinedTextField(
            value = inputPin,
            onValueChange = {
              inputPin = it
              errorMessage = null
            },
            label = { Text("PIN or Secret Code") },
            placeholder = { Text("Default: 1234 or 7777") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("locked_chats_pin_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it, color = CallRed) } }
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val res = ChatMitraRepository.authenticateLockedChats(inputPin)
              if (res.isFailure) {
                errorMessage = res.exceptionOrNull()?.message ?: "Incorrect code"
              } else {
                inputPin = ""
                errorMessage = null
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("unlock_chats_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Unlock Conversations", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Biometric quick unlock simulation button
          OutlinedButton(
            onClick = {
              // Simulated biometric fingerprint match
              val res = ChatMitraRepository.authenticateLockedChats("1234")
              if (res.isSuccess) {
                inputPin = ""
                errorMessage = null
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use Biometric Unlock", color = TealPrimary)
          }
        }
      }
    } else {
      // Authenticated Content: Locked Chats List
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        // Privacy Banner
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Protected with Chat Lock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              Text("Notifications and message previews are hidden for these chats.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }

        if (lockedChats.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("No locked chats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "To lock a conversation, open its chat menu or contact profile and tap 'Lock Chat'.",
                style = MaterialTheme.typography.bodyMedium,
                color = CheckmarkGrey,
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(lockedChats, key = { it.id }) { chat ->
              ListItem(
                modifier = Modifier
                  .clickable { onChatClick(chat.id) }
                  .testTag("locked_chat_item_${chat.id}"),
                leadingContent = {
                  UserAvatar(
                    name = chat.name,
                    colorHex = chat.avatarColorHex,
                    emoji = chat.avatarEmoji,
                    size = 46.dp
                  )
                },
                headlineContent = {
                  Text(chat.name, fontWeight = FontWeight.SemiBold)
                },
                supportingContent = {
                  Text(
                    text = chat.lastMessage?.text ?: "No messages yet",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                },
                trailingContent = {
                  IconButton(onClick = { ChatMitraRepository.unlockChat(chat.id) }) {
                    Icon(
                      imageVector = Icons.Default.LockOpen,
                      contentDescription = "Unlock Chat",
                      tint = CheckmarkGrey
                    )
                  }
                }
              )
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
          }
        }
      }
    }
  }

  // Secret Code & Hidden Folder Settings Modal
  if (showSecretSettingsDialog) {
    var newSecretCode by remember { mutableStateOf(secretCode) }
    var hideFolderToggle by remember { mutableStateOf(secretFolderEnabled) }

    AlertDialog(
      onDismissRequest = { showSecretSettingsDialog = false },
      title = { Text("Secret Code for Locked Chats", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            "Use a secret code to hide your Locked Chats folder from the chat list. You can only find it by searching your secret code in the Chats search bar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = newSecretCode,
            onValueChange = { newSecretCode = it },
            label = { Text("Secret Code (Letters, Numbers or Emojis)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Hide Locked Chats", fontWeight = FontWeight.SemiBold)
              Text("Locked chats folder will not appear in the chats list.", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
            }
            Switch(
              checked = hideFolderToggle,
              onCheckedChange = { hideFolderToggle = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TealPrimary)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.setSecretCode(newSecretCode)
            ChatMitraRepository.setSecretLockedChatsEnabled(hideFolderToggle)
            showSecretSettingsDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSecretSettingsDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
