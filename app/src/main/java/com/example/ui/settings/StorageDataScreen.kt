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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoDownloadOption
import com.example.data.model.ChatStorageUsage
import com.example.data.model.MediaItemStorage
import com.example.data.model.MessageType
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.LowDataRepository
import com.example.data.repository.StorageManagementRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDataScreen(
  onBackClick: () -> Unit
) {
  val storageRepo = remember { StorageManagementRepository.getInstance() }
  val overview by storageRepo.storageOverview.collectAsState()
  val usages by storageRepo.chatStorageUsages.collectAsState()
  val privacySettings by ChatMitraRepository.privacySettings.collectAsState()
  val lowDataSettings by LowDataRepository.settings.collectAsState()

  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var selectedChatForManage by remember { mutableStateOf<ChatStorageUsage?>(null) }
  var showClearCacheConfirm by remember { mutableStateOf(false) }
  var fileToDelete by remember { mutableStateOf<MediaItemStorage?>(null) }

  // Manage individual chat media checkboxes
  var delPhotos by remember { mutableStateOf(false) }
  var delAudio by remember { mutableStateOf(false) }
  var delVideos by remember { mutableStateOf(false) }
  var delDocs by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Storage and Data", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("storage_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Storage Overview Bar Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Total App Storage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "%.1f MB".format(overview.totalAppStorageBytes / 1_000_000.0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment storage bar representation
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
            ) {
              // Chat & Media
              Box(
                modifier = Modifier
                  .weight(0.70f)
                  .fillMaxSize()
                  .background(TealPrimary)
              )
              // Cache & Temp
              Box(
                modifier = Modifier
                  .weight(0.20f)
                  .fillMaxSize()
                  .background(AmberAccent)
              )
              // Free app headroom
              Box(
                modifier = Modifier
                  .weight(0.10f)
                  .fillMaxSize()
                  .background(MaterialTheme.colorScheme.outlineVariant)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TealPrimary))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chats & Media: %.1f MB".format(overview.chatAndMediaBytes / 1_000_000.0), fontSize = 12.sp, color = CheckmarkGrey)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AmberAccent))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cache: %.1f MB".format(overview.cacheAndTempBytes / 1_000_000.0), fontSize = 12.sp, color = CheckmarkGrey)
              }
            }
          }
        }
      }

      // Quick Cache Cleaner Button
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, tint = AmberAccent)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Clear Cached Thumbnails", fontWeight = FontWeight.SemiBold)
                Text(
                  text = "Frees %.1f MB temporary files without deleting messages".format(overview.cacheAndTempBytes / 1_000_000.0),
                  style = MaterialTheme.typography.bodySmall,
                  color = CheckmarkGrey
                )
              }
            }
            TextButton(
              onClick = { showClearCacheConfirm = true },
              enabled = overview.cacheAndTempBytes > 0,
              modifier = Modifier.testTag("clear_cache_button")
            ) {
              Text("Clear")
            }
          }
        }
      }

      // Largest Files Section
      if (overview.largestFiles.isNotEmpty()) {
        item {
          Text(
            text = "Review and Delete Items (> 5 MB)",
            style = MaterialTheme.typography.labelLarge,
            color = TealPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
          )
        }

        items(overview.largestFiles, key = { it.id }) { file ->
          ListItem(
            headlineContent = { Text(file.name, fontWeight = FontWeight.SemiBold, maxLines = 1) },
            supportingContent = { Text("${file.formattedSize} • in ${file.chatName}", color = CheckmarkGrey, style = MaterialTheme.typography.bodySmall) },
            leadingContent = {
              val icon = when (file.type) {
                MessageType.VIDEO -> Icons.Default.Videocam
                MessageType.IMAGE -> Icons.Default.Image
                else -> Icons.Default.Description
              }
              Icon(icon, contentDescription = null, tint = TealPrimary)
            },
            trailingContent = {
              IconButton(onClick = { fileToDelete = file }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete file", tint = CheckmarkGrey)
              }
            }
          )
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
      }

      // Storage per chat conversation
      item {
        Text(
          text = "Storage by Conversation",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      items(usages, key = { it.chatId }) { chatUsage ->
        ListItem(
          modifier = Modifier
            .clickable {
              selectedChatForManage = chatUsage
              delPhotos = false
              delAudio = false
              delVideos = false
              delDocs = false
            }
            .testTag("storage_chat_${chatUsage.chatId}"),
          leadingContent = {
            UserAvatar(
              name = chatUsage.chatName,
              colorHex = chatUsage.avatarColorHex,
              emoji = chatUsage.avatarEmoji,
              size = 46.dp
            )
          },
          headlineContent = { Text(chatUsage.chatName, fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("${chatUsage.messagesCount} msgs • ${chatUsage.photosCount} photos • ${chatUsage.voiceNotesCount} audio", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          },
          trailingContent = {
            Text(chatUsage.formattedSize, fontWeight = FontWeight.Bold, color = TealPrimary)
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Network Usage Summary
      item {
        Text(
          text = "Network Usage",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          leadingContent = { Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = TealPrimary) },
          headlineContent = { Text("Network data sent & received", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Calls: 22.1 MB • Media: 44.8 MB • Messages: 1.2 MB", color = CheckmarkGrey) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Low Data Mode Section
      item {
        Text(
          text = "Low Data Mode & Network Adaptation",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("Low Data Mode", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(
              "Reduces call bitrate to 150 kbps, compresses outbound uploads, and disables auto-downloads.",
              color = CheckmarkGrey,
              fontSize = 12.sp
            )
          },
          trailingContent = {
            Switch(
              checked = lowDataSettings.isLowDataModeEnabled,
              onCheckedChange = { LowDataRepository.setLowDataMode(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Compress Uploaded Media", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Reduces image and voice note upload footprint", color = CheckmarkGrey, fontSize = 12.sp) },
          trailingContent = {
            Switch(
              checked = lowDataSettings.compressMediaBeforeUpload,
              onCheckedChange = { LowDataRepository.toggleCompressMedia(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Auto-Download Photos", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Setting: ${lowDataSettings.autoDownloadPhotos.label}", color = CheckmarkGrey, fontSize = 12.sp)
          },
          trailingContent = {
            TextButton(
              onClick = {
                val next = when (lowDataSettings.autoDownloadPhotos) {
                  AutoDownloadOption.NEVER -> AutoDownloadOption.WIFI_ONLY
                  AutoDownloadOption.WIFI_ONLY -> AutoDownloadOption.WIFI_AND_CELLULAR
                  AutoDownloadOption.WIFI_AND_CELLULAR -> AutoDownloadOption.NEVER
                }
                LowDataRepository.updateAutoDownloadPhotos(next)
              }
            ) {
              Text(
                when (lowDataSettings.autoDownloadPhotos) {
                  AutoDownloadOption.NEVER -> "Never"
                  AutoDownloadOption.WIFI_ONLY -> "Wi-Fi Only"
                  AutoDownloadOption.WIFI_AND_CELLULAR -> "Wi-Fi & Cell"
                },
                color = TealPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Auto-Download Videos", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text("Setting: ${lowDataSettings.autoDownloadVideo.label}", color = CheckmarkGrey, fontSize = 12.sp)
          },
          trailingContent = {
            TextButton(
              onClick = {
                val next = when (lowDataSettings.autoDownloadVideo) {
                  AutoDownloadOption.NEVER -> AutoDownloadOption.WIFI_ONLY
                  AutoDownloadOption.WIFI_ONLY -> AutoDownloadOption.WIFI_AND_CELLULAR
                  AutoDownloadOption.WIFI_AND_CELLULAR -> AutoDownloadOption.NEVER
                }
                LowDataRepository.updateAutoDownloadVideo(next)
              }
            ) {
              Text(
                when (lowDataSettings.autoDownloadVideo) {
                  AutoDownloadOption.NEVER -> "Never"
                  AutoDownloadOption.WIFI_ONLY -> "Wi-Fi Only"
                  AutoDownloadOption.WIFI_AND_CELLULAR -> "Wi-Fi & Cell"
                },
                color = TealPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        )
        Spacer(modifier = Modifier.height(36.dp))
      }
    }

    // Modal Bottom Sheet: Manage Specific Conversation Storage
    selectedChatForManage?.let { chat ->
      ModalBottomSheet(
        onDismissRequest = { selectedChatForManage = null }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(name = chat.chatName, colorHex = chat.avatarColorHex, emoji = chat.avatarEmoji, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(chat.chatName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
              Text("Total: ${chat.formattedSize}", style = MaterialTheme.typography.bodySmall, color = TealPrimary)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text("Select media types to delete from this conversation:", fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(8.dp))

          // Photos
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { delPhotos = !delPhotos }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(checked = delPhotos, onCheckedChange = { delPhotos = it }, colors = CheckboxDefaults.colors(checkedColor = TealPrimary))
            Icon(Icons.Default.Image, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Photos (${chat.photosCount} files, %.1f MB)".format(chat.photosSizeBytes / 1_000_000.0), modifier = Modifier.weight(1f))
          }

          // Voice Notes / Audio
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { delAudio = !delAudio }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(checked = delAudio, onCheckedChange = { delAudio = it }, colors = CheckboxDefaults.colors(checkedColor = TealPrimary))
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Voice Notes & Audio (${chat.voiceNotesCount} files, %.1f MB)".format(chat.voiceNotesSizeBytes / 1_000_000.0), modifier = Modifier.weight(1f))
          }

          // Videos
          if (chat.videosCount > 0) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { delVideos = !delVideos }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(checked = delVideos, onCheckedChange = { delVideos = it }, colors = CheckboxDefaults.colors(checkedColor = TealPrimary))
              Icon(Icons.Default.Videocam, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Videos (${chat.videosCount} files, %.1f MB)".format(chat.videosSizeBytes / 1_000_000.0), modifier = Modifier.weight(1f))
            }
          }

          // Documents
          if (chat.docsCount > 0) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { delDocs = !delDocs }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(checked = delDocs, onCheckedChange = { delDocs = it }, colors = CheckboxDefaults.colors(checkedColor = TealPrimary))
              Icon(Icons.Default.Description, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Documents (${chat.docsCount} files, %.1f MB)".format(chat.docsSizeBytes / 1_000_000.0), modifier = Modifier.weight(1f))
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val freed = storageRepo.deleteChatMediaItems(
                chatId = chat.chatId,
                deletePhotos = delPhotos,
                deleteAudio = delAudio,
                deleteVideos = delVideos,
                deleteDocs = delDocs
              )
              selectedChatForManage = null
              scope.launch {
                snackbarHostState.showSnackbar("Freed %.1f MB from ${chat.chatName}.".format(freed / 1_000_000.0))
              }
            },
            enabled = delPhotos || delAudio || delVideos || delDocs,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CallRed)
          ) {
            Text("Delete Selected Media", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(12.dp))
        }
      }
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheConfirm) {
      AlertDialog(
        onDismissRequest = { showClearCacheConfirm = false },
        title = { Text("Clear cached files?") },
        text = {
          Text("This will delete cached photo previews and temporary files (%.1f MB). No chat messages or downloaded documents will be deleted.".format(overview.cacheAndTempBytes / 1_000_000.0))
        },
        confirmButton = {
          TextButton(
            onClick = {
              val freed = storageRepo.clearCache()
              showClearCacheConfirm = false
              scope.launch { snackbarHostState.showSnackbar("Cleared %.1f MB of cache.".format(freed / 1_000_000.0)) }
            }
          ) {
            Text("Clear", color = AmberAccent, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showClearCacheConfirm = false }) { Text("Cancel") }
        }
      )
    }

    // Delete Single Large File Confirmation
    fileToDelete?.let { file ->
      AlertDialog(
        onDismissRequest = { fileToDelete = null },
        title = { Text("Delete ${file.name}?") },
        text = { Text("This file (${file.formattedSize}) will be removed from your local storage.") },
        confirmButton = {
          TextButton(
            onClick = {
              storageRepo.deleteLargestFile(file.id)
              fileToDelete = null
              scope.launch { snackbarHostState.showSnackbar("Deleted ${file.name}.") }
            }
          ) {
            Text("Delete", color = CallRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { fileToDelete = null }) { Text("Cancel") }
        }
      )
    }
  }
}
