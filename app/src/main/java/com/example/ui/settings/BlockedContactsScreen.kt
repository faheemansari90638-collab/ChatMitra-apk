package com.example.ui.settings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedContactsScreen(
  onBackClick: () -> Unit
) {
  val blockedIds by ChatMitraRepository.blockedContactIds.collectAsState()
  val allContacts by ChatMitraRepository.contacts.collectAsState()

  val blockedList = allContacts.filter { blockedIds.contains(it.id) }
  val unblockedCandidates = allContacts.filterNot { blockedIds.contains(it.id) }

  var showAddBlockDialog by remember { mutableStateOf(false) }
  var userToUnblock by remember { mutableStateOf<UserProfile?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Blocked Contacts (${blockedIds.size})", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(onClick = { showAddBlockDialog = true }) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Block Contact", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddBlockDialog = true },
        containerColor = TealPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.testTag("add_blocked_contact_fab")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Text(
        text = "Blocked contacts will no longer be able to call you or send you messages.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(16.dp)
      )

      if (blockedList.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Block, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No blocked contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Tap the add button above to block a contact from calling or messaging you.",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(modifier = Modifier.weight(1f)) {
          items(blockedList, key = { it.id }) { contact ->
            ListItem(
              leadingContent = {
                UserAvatar(
                  name = contact.name,
                  colorHex = contact.avatarColorHex,
                  emoji = contact.avatarEmoji,
                  size = 46.dp
                )
              },
              headlineContent = {
                Text(contact.name, fontWeight = FontWeight.SemiBold)
              },
              supportingContent = {
                Text(contact.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
              },
              trailingContent = {
                OutlinedButton(
                  onClick = { userToUnblock = contact },
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("Unblock")
                }
              }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          }
        }
      }
    }
  }

  // Unblock Confirmation Dialog
  userToUnblock?.let { target ->
    AlertDialog(
      onDismissRequest = { userToUnblock = null },
      title = { Text("Unblock ${target.name}?") },
      text = {
        Text("They will once again be able to send you messages and initiate voice/video calls.")
      },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.unblockContact(target.id)
            userToUnblock = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Unblock")
        }
      },
      dismissButton = {
        TextButton(onClick = { userToUnblock = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add Contact to Block Dialog
  if (showAddBlockDialog) {
    AlertDialog(
      onDismissRequest = { showAddBlockDialog = false },
      title = { Text("Select Contact to Block", fontWeight = FontWeight.Bold) },
      text = {
        if (unblockedCandidates.isEmpty()) {
          Text("All your current contacts are already blocked.")
        } else {
          LazyColumn(modifier = Modifier.height(280.dp)) {
            items(unblockedCandidates, key = { it.id }) { candidate ->
              ListItem(
                modifier = Modifier.clickable {
                  ChatMitraRepository.blockContact(candidate.id)
                  showAddBlockDialog = false
                },
                leadingContent = {
                  UserAvatar(
                    name = candidate.name,
                    colorHex = candidate.avatarColorHex,
                    emoji = candidate.avatarEmoji,
                    size = 40.dp
                  )
                },
                headlineContent = { Text(candidate.name, fontWeight = FontWeight.Medium) },
                supportingContent = { Text(candidate.phone, style = MaterialTheme.typography.bodySmall) }
              )
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showAddBlockDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
