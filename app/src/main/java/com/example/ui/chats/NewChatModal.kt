package com.example.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatModal(
  onDismiss: () -> Unit,
  onSelectContact: (UserProfile) -> Unit,
  onCreateGroupClick: () -> Unit
) {
  val contacts by ChatMitraRepository.contacts.collectAsState()
  val sheetState = rememberModalBottomSheetState()
  var searchQuery by remember { mutableStateOf("") }

  val filteredContacts = contacts.filter {
    it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
    ) {
      Text(
        text = "Select Contact",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
      )

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search name or number...", fontSize = 14.sp) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary)
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("contact_picker_search"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = TealPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
      )

      // Shortcut: New Group
      ListItem(
        modifier = Modifier
          .clickable {
            onDismiss()
            onCreateGroupClick()
          }
          .testTag("new_group_shortcut"),
        headlineContent = { Text("New Group", fontWeight = FontWeight.SemiBold) },
        leadingContent = {
          Icon(Icons.Default.GroupAdd, contentDescription = null, tint = TealPrimary)
        }
      )

      HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

      Text(
        text = "Contacts on ChatMitra (${filteredContacts.size})",
        style = MaterialTheme.typography.labelMedium,
        color = TealPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
      )

      LazyColumn {
        items(filteredContacts, key = { it.id }) { contact ->
          ListItem(
            modifier = Modifier
              .clickable {
                onSelectContact(contact)
                onDismiss()
              }
              .testTag("contact_picker_item_${contact.id}"),
            headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(contact.about, style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey) },
            leadingContent = {
              UserAvatar(
                name = contact.name,
                colorHex = contact.avatarColorHex,
                emoji = contact.avatarEmoji,
                size = 46.dp,
                isOnline = contact.isOnline
              )
            }
          )
        }
      }
    }
  }
}
