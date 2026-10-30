package com.example.ui.calls

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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallDirection
import com.example.data.model.CallLog
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsScreen(
  onStartCall: (UserProfile, Boolean) -> Unit,
  onNewCallClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val callLogs by ChatMitraRepository.callLogs.collectAsState()
  val contacts by ChatMitraRepository.contacts.collectAsState()

  var selectedFilter by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }
  var showClearHistoryDialog by remember { mutableStateOf(false) }
  var showIncomingTestDialog by remember { mutableStateOf(false) }

  val filteredLogs = callLogs.filter { log ->
    val matchesFilter = when (selectedFilter) {
      "Missed" -> log.direction == CallDirection.MISSED
      else -> true
    }
    val matchesSearch = searchQuery.isBlank() || log.contactName.contains(searchQuery, ignoreCase = true)
    matchesFilter && matchesSearch
  }

  val missedCount = callLogs.count { it.direction == CallDirection.MISSED }

  Box(modifier = modifier.fillMaxSize().testTag("calls_screen")) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      // Top Controls Header: Filters, Clear History, and Test Incoming Call
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          // Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search calls...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CheckmarkGrey) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("calls_search_field"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Filter Chips & Clear Action Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = selectedFilter == "All",
                onClick = { selectedFilter = "All" },
                label = { Text("All (${callLogs.size})") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                  selectedLabelColor = TealPrimary
                ),
                modifier = Modifier.testTag("filter_all_calls")
              )

              FilterChip(
                selected = selectedFilter == "Missed",
                onClick = { selectedFilter = "Missed" },
                label = { Text("Missed ($missedCount)") },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CallRed.copy(alpha = 0.15f),
                  selectedLabelColor = CallRed
                ),
                modifier = Modifier.testTag("filter_missed_calls")
              )
            }

            Row {
              // Test Incoming Call Trigger
              IconButton(
                onClick = { showIncomingTestDialog = true },
                modifier = Modifier.testTag("test_incoming_call_button")
              ) {
                Icon(
                  Icons.Default.NotificationsActive,
                  contentDescription = "Test Incoming Call",
                  tint = AmberAccent
                )
              }

              // Clear History Button
              if (callLogs.isNotEmpty()) {
                IconButton(
                  onClick = { showClearHistoryDialog = true },
                  modifier = Modifier.testTag("clear_call_history_button")
                ) {
                  Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear Call History",
                    tint = CheckmarkGrey
                  )
                }
              }
            }
          }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      }

      // Create Call Link Item
      item {
        ListItem(
          modifier = Modifier
            .clickable { onNewCallClick() }
            .testTag("create_call_link_card"),
          headlineContent = {
            Text(
              text = "Create call link",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          },
          supportingContent = {
            Text(
              text = "Share a secure WebRTC link for your ChatMitra call",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey
            )
          },
          leadingContent = {
            Box(
              modifier = Modifier
                .size(46.dp)
                .background(TealPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Link, contentDescription = "Call Link", tint = Color.White)
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      }

      // Recent Calls Header
      item {
        Text(
          text = if (selectedFilter == "Missed") "Missed Calls" else "Recent Calls",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
      }

      if (filteredLogs.isEmpty()) {
        item {
          Box(
            modifier = Modifier.fillMaxWidth().padding(48.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (searchQuery.isNotBlank()) "No calls match \"$searchQuery\"" else "No calls in history",
              color = CheckmarkGrey,
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }

      // Call Log Items
      items(filteredLogs, key = { it.id }) { log ->
        val contact = contacts.find { it.id == log.contactId }

        ListItem(
          modifier = Modifier.testTag("call_log_item_${log.id}"),
          headlineContent = {
            Text(
              text = log.contactName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = if (log.direction == CallDirection.MISSED) CallRed else MaterialTheme.colorScheme.onSurface
            )
          },
          supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              val directionIcon = when (log.direction) {
                CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed
              }
              val iconTint = when (log.direction) {
                CallDirection.MISSED -> CallRed
                CallDirection.INCOMING -> CallGreen
                CallDirection.OUTGOING -> TealPrimary
              }

              Icon(
                imageVector = directionIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              val sdf = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
              Text(
                text = "${sdf.format(Date(log.timestamp))} • ${log.durationFormatted}",
                style = MaterialTheme.typography.bodySmall,
                color = CheckmarkGrey
              )
            }
          },
          leadingContent = {
            UserAvatar(
              name = log.contactName,
              colorHex = log.contactAvatarColorHex,
              emoji = log.contactAvatarEmoji,
              size = 48.dp,
              fontSize = 20
            )
          },
          trailingContent = {
            Row {
              // Quick Audio Call Back
              IconButton(
                onClick = {
                  val target = contact ?: UserProfile(
                    id = log.contactId,
                    name = log.contactName,
                    phone = "+91 98765 43210",
                    avatarColorHex = log.contactAvatarColorHex,
                    avatarEmoji = log.contactAvatarEmoji
                  )
                  onStartCall(target, false)
                },
                modifier = Modifier.testTag("call_back_audio_${log.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Call,
                  contentDescription = "Audio Call Back",
                  tint = TealPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }

              // Quick Video Call Back
              IconButton(
                onClick = {
                  val target = contact ?: UserProfile(
                    id = log.contactId,
                    name = log.contactName,
                    phone = "+91 98765 43210",
                    avatarColorHex = log.contactAvatarColorHex,
                    avatarEmoji = log.contactAvatarEmoji
                  )
                  onStartCall(target, true)
                },
                modifier = Modifier.testTag("call_back_video_${log.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Videocam,
                  contentDescription = "Video Call Back",
                  tint = TealPrimary,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 72.dp))
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
      AlertDialog(
        onDismissRequest = { showClearHistoryDialog = false },
        title = { Text("Clear call history?") },
        text = { Text("Do you want to clear your entire call log? This action cannot be undone.") },
        confirmButton = {
          TextButton(
            onClick = {
              ChatMitraRepository.clearCallLogs()
              showClearHistoryDialog = false
            },
            modifier = Modifier.testTag("confirm_clear_history")
          ) {
            Text("Clear", color = CallRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showClearHistoryDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Test Incoming Call Trigger Dialog
    if (showIncomingTestDialog) {
      AlertDialog(
        onDismissRequest = { showIncomingTestDialog = false },
        title = { Text("Simulate Incoming Call") },
        text = {
          Column {
            Text(
              "Select a call type to simulate an incoming call with your configured ringtone, vibration pattern, Silent Mode, and Do Not Disturb settings.",
              fontSize = 13.sp,
              color = CheckmarkGrey
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier.fillMaxWidth().clickable {
                showIncomingTestDialog = false
                ChatMitraRepository.simulateIncomingCall(
                  caller = contacts.firstOrNull(),
                  isVideo = false,
                  context = context
                )
              }
            ) {
              Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Call, contentDescription = null, tint = CallGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Incoming Audio Call", fontWeight = FontWeight.SemiBold)
                  Text("From ${contacts.firstOrNull()?.name ?: "Priya Sharma"}", fontSize = 12.sp, color = CheckmarkGrey)
                }
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier.fillMaxWidth().clickable {
                showIncomingTestDialog = false
                ChatMitraRepository.simulateIncomingCall(
                  caller = contacts.firstOrNull(),
                  isVideo = true,
                  context = context
                )
              }
            ) {
              Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Incoming Video Call", fontWeight = FontWeight.SemiBold)
                  Text("From ${contacts.firstOrNull()?.name ?: "Priya Sharma"}", fontSize = 12.sp, color = CheckmarkGrey)
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showIncomingTestDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}
