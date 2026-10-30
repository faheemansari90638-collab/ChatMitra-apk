package com.example.ui.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.MessageType
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.MessageReceiptIndicator
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
  onChatClick: (String) -> Unit,
  onNewChatClick: () -> Unit,
  onCreateGroupClick: () -> Unit,
  onLockedChatsClick: () -> Unit = {},
  onHamzaAiClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val chats by ChatMitraRepository.chats.collectAsState()
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val typingStatus by ChatMitraRepository.typingStatus.collectAsState()
  val lockedChatIds by ChatMitraRepository.lockedChatIds.collectAsState()
  val secretLockedChatsEnabled by ChatMitraRepository.secretLockedChatsEnabled.collectAsState()
  val secretCode by ChatMitraRepository.secretCode.collectAsState()
  val chatFolders by ChatMitraRepository.chatFolders.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedFolderId by remember { mutableStateOf("folder_all") }
  var showAddFolderDialog by remember { mutableStateOf(false) }
  var newFolderName by remember { mutableStateOf("") }
  var selectedChatIdsForFolder by remember { mutableStateOf<Set<String>>(emptySet()) }
  var selectedChatForActions by remember { mutableStateOf<Chat?>(null) }
  val sheetState = rememberModalBottomSheetState()

  // Determine if locked chats header should be visible
  val showLockedChatsRow = when {
    !secretLockedChatsEnabled -> lockedChatIds.isNotEmpty()
    searchQuery.isNotBlank() && searchQuery.trim() == secretCode.trim() -> true
    else -> false
  }

  // Filtered chats (excluding locked chats from the regular list)
  val filteredChats = chats.filter { chat ->
    val isChatLocked = chat.isLocked || lockedChatIds.contains(chat.id)
    if (isChatLocked) return@filter false

    val matchesSearch = chat.name.contains(searchQuery, ignoreCase = true) ||
      (chat.lastMessage?.text?.contains(searchQuery, ignoreCase = true) == true)
    val selectedFolder = chatFolders.find { it.id == selectedFolderId }
    val matchesFilter = when {
      selectedFolderId == "folder_unread" -> chat.unreadCount > 0
      selectedFolder != null && selectedFolder.isCustom -> selectedFolder.chatIds.contains(chat.id)
      else -> !chat.isArchived
    }
    matchesSearch && matchesFilter
  }.sortedByDescending { it.isPinned }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            if (secretLockedChatsEnabled) "Search chats or enter secret code..." else "Search chats or messages...",
            fontSize = 14.sp
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = TealPrimary)
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          focusedBorderColor = TealPrimary,
          unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("chat_search_input")
      )

      // Folder Filter Chips
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
      ) {
        items(chatFolders) { folder ->
          FilterChip(
            selected = selectedFolderId == folder.id,
            onClick = { selectedFolderId = folder.id },
            label = { Text(folder.name, fontSize = 13.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = TealPrimary,
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("folder_chip_${folder.id}")
          )
        }

        item {
          FilterChip(
            selected = false,
            onClick = {
              newFolderName = ""
              selectedChatIdsForFolder = emptySet()
              showAddFolderDialog = true
            },
            leadingIcon = {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            label = { Text("New Folder", fontSize = 13.sp) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("add_chat_folder_btn")
          )
        }
      }

      LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Locked chats entry row (WhatsApp style)
        if (showLockedChatsRow) {
          item(key = "locked_chats_row") {
            ListItem(
              modifier = Modifier
                .clickable { onLockedChatsClick() }
                .testTag("locked_chats_entry_row"),
              leadingContent = {
                Box(
                  modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Lock, contentDescription = "Locked chats", tint = TealPrimary)
                }
              },
              headlineContent = {
                Text("Locked chats", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              },
              supportingContent = {
                Text(
                  "${lockedChatIds.size} protected conversation${if (lockedChatIds.size == 1) "" else "s"}",
                  color = CheckmarkGrey,
                  style = MaterialTheme.typography.bodySmall
                )
              },
              trailingContent = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
              }
            )
            HorizontalDivider(
              modifier = Modifier.padding(start = 76.dp, end = 16.dp),
              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
              thickness = 0.5.dp
            )
          }
        }

        // Hamza AI Assistant Entry Card
        item(key = "hamza_ai_entry_card") {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable { onHamzaAiClick() }
              .testTag("hamza_ai_chat_entry"),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Hamza AI", tint = Color.White, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Hamza AI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f)
                  ) {
                    Text(
                      "AI Assistant",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF6366F1),
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  "Ask questions, summarize chats & translate • Multilingual",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        if (filteredChats.isEmpty() && !showLockedChatsRow) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  Icons.Default.Chat,
                  contentDescription = null,
                  tint = CheckmarkGrey.copy(alpha = 0.5f),
                  modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = if (searchQuery.isNotBlank()) "No chats matching \"$searchQuery\"" else "No chats yet",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Tap the message button to start chatting with friends",
                  style = MaterialTheme.typography.bodySmall,
                  color = CheckmarkGrey,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        } else {
          items(filteredChats, key = { it.id }) { chat ->
            val isTyping = typingStatus[chat.id] == true
            ChatListItem(
              chat = chat,
              currentUserId = currentUser?.id ?: "",
              isTyping = isTyping,
              onClick = { onChatClick(chat.id) },
              onLongClick = { selectedChatForActions = chat }
            )
            HorizontalDivider(
              modifier = Modifier.padding(start = 76.dp, end = 16.dp),
              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
              thickness = 0.5.dp
            )
          }
        }
      }
    }

    // Action FABs
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 16.dp, end = 16.dp),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FloatingActionButton(
        onClick = onHamzaAiClick,
        containerColor = Color(0xFF6366F1),
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .testTag("hamza_ai_fab")
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = "Hamza AI", modifier = Modifier.size(20.dp))
      }

      FloatingActionButton(
        onClick = onCreateGroupClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = TealPrimary,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .testTag("new_group_fab")
      ) {
        Icon(Icons.Default.GroupAdd, contentDescription = "Create Group", modifier = Modifier.size(20.dp))
      }

      FloatingActionButton(
        onClick = onNewChatClick,
        containerColor = TealPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(56.dp)
          .testTag("new_chat_fab")
      ) {
        Icon(Icons.Default.Chat, contentDescription = "New Chat")
      }
    }

    // Contextual Actions Bottom Sheet
    selectedChatForActions?.let { chat ->
      ModalBottomSheet(
        onDismissRequest = { selectedChatForActions = null },
        sheetState = sheetState
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ) {
          Text(
            text = chat.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
          )

          HorizontalDivider()

          ActionSheetItem(
            icon = Icons.Default.Lock,
            title = if (chat.isLocked) "Unlock Chat" else "Lock Chat with PIN"
          ) {
            if (chat.isLocked) {
              ChatMitraRepository.unlockChat(chat.id)
            } else {
              ChatMitraRepository.lockChat(chat.id)
            }
            selectedChatForActions = null
          }

          ActionSheetItem(
            icon = Icons.Default.PushPin,
            title = if (chat.isPinned) "Unpin Chat" else "Pin Chat"
          ) {
            ChatMitraRepository.togglePinChat(chat.id)
            selectedChatForActions = null
          }

          ActionSheetItem(
            icon = Icons.Default.VolumeOff,
            title = if (chat.isMuted) "Unmute Notifications" else "Mute Notifications"
          ) {
            ChatMitraRepository.toggleMuteChat(chat.id)
            selectedChatForActions = null
          }

          ActionSheetItem(
            icon = Icons.Default.Archive,
            title = if (chat.isArchived) "Unarchive Chat" else "Archive Chat"
          ) {
            ChatMitraRepository.toggleArchiveChat(chat.id)
            selectedChatForActions = null
          }

          ActionSheetItem(
            icon = Icons.Default.ClearAll,
            title = "Clear Chat History"
          ) {
            ChatMitraRepository.clearChat(chat.id)
            selectedChatForActions = null
          }
        }
      }
    }

    // Add Custom Folder Dialog
    if (showAddFolderDialog) {
      AlertDialog(
        onDismissRequest = { showAddFolderDialog = false },
        icon = {
          Icon(Icons.Default.Folder, contentDescription = null, tint = TealPrimary)
        },
        title = { Text("Create Chat Folder", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text("Organize your chats into custom categories.")
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
              value = newFolderName,
              onValueChange = { newFolderName = it },
              label = { Text("Folder Name") },
              placeholder = { Text("e.g. Friends, Family, Clients") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_folder_name_input")
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Include Chats:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn(modifier = Modifier.height(180.dp)) {
              items(chats) { chatItem ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      selectedChatIdsForFolder = if (selectedChatIdsForFolder.contains(chatItem.id)) {
                        selectedChatIdsForFolder - chatItem.id
                      } else {
                        selectedChatIdsForFolder + chatItem.id
                      }
                    }
                    .padding(vertical = 4.dp)
                ) {
                  Checkbox(
                    checked = selectedChatIdsForFolder.contains(chatItem.id),
                    onCheckedChange = { checked ->
                      selectedChatIdsForFolder = if (checked) {
                        selectedChatIdsForFolder + chatItem.id
                      } else {
                        selectedChatIdsForFolder - chatItem.id
                      }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(chatItem.name, style = MaterialTheme.typography.bodyMedium)
                }
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (newFolderName.isNotBlank()) {
                ChatMitraRepository.createChatFolder(newFolderName.trim(), selectedChatIdsForFolder)
                showAddFolderDialog = false
              }
            },
            enabled = newFolderName.isNotBlank(),
            modifier = Modifier.testTag("save_folder_confirm_btn")
          ) {
            Text("Create Folder")
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddFolderDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
  chat: Chat,
  currentUserId: String,
  isTyping: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  val contact = chat.participants.firstOrNull()

  ListItem(
    modifier = Modifier
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("chat_item_${chat.id}"),
    headlineContent = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = chat.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        chat.lastMessage?.let { lastMsg ->
          Text(
            text = formatMessageTime(lastMsg.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = if (chat.unreadCount > 0) TealPrimary else CheckmarkGrey
          )
        }
      }
    },
    supportingContent = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (isTyping) {
          Text(
            text = "typing...",
            color = TealPrimary,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
          )
        } else {
          chat.lastMessage?.let { lastMsg ->
            if (lastMsg.senderId == currentUserId) {
              MessageReceiptIndicator(
                status = lastMsg.status,
                isRead = lastMsg.isRead,
                readAt = lastMsg.readAt,
                modifier = Modifier.padding(end = 4.dp)
              )
            }
            val previewText = when (lastMsg.type) {
              MessageType.VOICE_NOTE -> "🎤 Voice note (${lastMsg.mediaDurationSec}s)"
              MessageType.IMAGE -> "📷 Photo"
              MessageType.VIDEO -> "🎥 Video"
              MessageType.DOCUMENT -> "📄 Document"
              MessageType.AUDIO -> "🎵 Audio"
              MessageType.TEXT -> lastMsg.text
            }
            Text(
              text = previewText,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f)
            )
          } ?: run {
            Text(
              text = "Tap to start conversation",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Badges: Pin, Mute, Unread count
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (chat.isPinned) {
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Pinned",
              tint = CheckmarkGrey,
              modifier = Modifier.size(15.dp)
            )
          }
          if (chat.isMuted) {
            Icon(
              imageVector = Icons.Default.NotificationsOff,
              contentDescription = "Muted",
              tint = CheckmarkGrey,
              modifier = Modifier.size(15.dp)
            )
          }
          if (chat.unreadCount > 0) {
            Surface(
              color = TealPrimary,
              shape = CircleShape,
              modifier = Modifier.size(20.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "${chat.unreadCount}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    },
    leadingContent = {
      UserAvatar(
        name = chat.name,
        colorHex = chat.avatarColorHex,
        emoji = chat.avatarEmoji,
        size = 52.dp,
        isOnline = contact?.isOnline ?: false
      )
    },
    colors = ListItemDefaults.colors(
      containerColor = Color.Transparent
    )
  )
}

@Composable
fun ActionSheetItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  onClick: () -> Unit
) {
  ListItem(
    headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
    leadingContent = { Icon(icon, contentDescription = null, tint = TealPrimary) },
    modifier = Modifier.clickable(onClick = onClick)
  )
}

fun formatMessageTime(timestamp: Long): String {
  val now = System.currentTimeMillis()
  val diff = now - timestamp
  val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
  return when {
    diff < 86400000L -> sdfTime.format(Date(timestamp))
    diff < 172800000L -> "Yesterday"
    else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
  }
}
