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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calling.CallAudioAndVibrationManager
import com.example.data.model.DndDuration
import com.example.data.model.SilentModeScope
import com.example.data.model.VibrationPattern
import com.example.data.repository.CallNotificationRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val settings by CallNotificationRepository.settings.collectAsState()

  var playingToneName by remember { mutableStateOf<String?>(null) }
  var showRingtoneDialog by remember { mutableStateOf(false) }
  var isVideoRingtonePicker by remember { mutableStateOf(false) }
  var showVibrationDialog by remember { mutableStateOf(false) }
  var showDndDurationDialog by remember { mutableStateOf(false) }
  var showMessageSoundDialog by remember { mutableStateOf(false) }
  var isGroupSoundPicker by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Notifications", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = {
              CallAudioAndVibrationManager.stopTonePreview()
              onNavigateBack()
            },
            modifier = Modifier.testTag("notifications_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier.testTag("restore_defaults_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Restore Defaults", tint = TealPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("notifications_settings_screen")
    ) {
      // 1. SILENT MODE MASTER CARD
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("silent_mode_card"),
          colors = CardDefaults.cardColors(
            containerColor = if (settings.isSilentMode) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant
          ),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(if (settings.isSilentMode) AmberAccent else TealPrimary, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (settings.isSilentMode) Icons.Default.VolumeOff else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Silent Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = if (settings.isSilentMode) "Calls and messages are muted" else "Sounds are active",
                    fontSize = 12.sp,
                    color = if (settings.isSilentMode) AmberAccent else CheckmarkGrey
                  )
                }
              }
              Switch(
                checked = settings.isSilentMode,
                onCheckedChange = { checked ->
                  CallNotificationRepository.toggleSilentMode(enabled = checked)
                  scope.launch {
                    snackbarHostState.showSnackbar(
                      if (checked) "Silent Mode enabled" else "Silent Mode disabled"
                    )
                  }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = AmberAccent),
                modifier = Modifier.testTag("silent_mode_switch")
              )
            }

            if (settings.isSilentMode) {
              Spacer(modifier = Modifier.height(14.dp))
              HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "Apply Silent Mode to:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White
              )

              // Scope Radio Options
              SilentModeScope.values().forEach { scopeOption ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      CallNotificationRepository.toggleSilentMode(
                        enabled = true,
                        scope = scopeOption
                      )
                    }
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = settings.silentModeScope == scopeOption,
                    onClick = {
                      CallNotificationRepository.toggleSilentMode(
                        enabled = true,
                        scope = scopeOption
                      )
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                  )
                  Text(
                    text = scopeOption.label,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                  )
                }
              }

              // Mute Vibration Checkbox
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    CallNotificationRepository.toggleSilentMode(
                      enabled = true,
                      muteVibration = !settings.silentModeMuteVibration
                    )
                  }
                  .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Checkbox(
                  checked = settings.silentModeMuteVibration,
                  onCheckedChange = { checked ->
                    CallNotificationRepository.toggleSilentMode(
                      enabled = true,
                      muteVibration = checked
                    )
                  },
                  colors = CheckboxDefaults.colors(checkedColor = AmberAccent)
                )
                Text(
                  text = "Also disable vibration in Silent Mode",
                  fontSize = 13.sp,
                  color = Color.White.copy(alpha = 0.9f)
                )
              }

              // Clarification Note
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Incoming calls will still appear on screen visually without ringing or declining.",
                  fontSize = 11.sp,
                  color = Color.White.copy(alpha = 0.8f)
                )
              }
            }
          }
        }
      }

      // 2. DO NOT DISTURB SECTION
      item {
        val isDndActive = CallNotificationRepository.isDndActive()
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("dnd_settings_card"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(if (isDndActive) CallRed else TealPrimary, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.DoNotDisturbOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Do Not Disturb",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = if (isDndActive) "Active: ${settings.dndDuration.label}" else "Off",
                    fontSize = 12.sp,
                    color = if (isDndActive) CallRed else CheckmarkGrey
                  )
                }
              }

              TextButton(
                onClick = { showDndDurationDialog = true },
                modifier = Modifier.testTag("dnd_duration_button")
              ) {
                Text(if (isDndActive) "Change" else "Turn On", fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DND Policy Checkboxes
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = settings.dndAllowStarredContacts,
                onCheckedChange = { checked ->
                  CallNotificationRepository.updateDndPreferences(allowStarred = checked)
                }
              )
              Text("Allow calls from Starred & Verified contacts", fontSize = 13.sp)
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = settings.dndAllowRepeatCallers,
                onCheckedChange = { checked ->
                  CallNotificationRepository.updateDndPreferences(allowRepeatCallers = checked)
                }
              )
              Text("Allow repeat callers (called twice within 15 mins)", fontSize = 13.sp)
            }

            // Note on System DND
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Info, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Android system Do Not Disturb overrides app settings when enabled globally.",
                fontSize = 11.sp,
                color = CheckmarkGrey
              )
            }
          }
        }
      }

      // 3. CALL RINGTONE SETTINGS
      item {
        Text(
          text = "CALL RINGTONES",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      // Audio Call Ringtone Item
      item {
        ListItem(
          modifier = Modifier
            .clickable {
              isVideoRingtonePicker = false
              showRingtoneDialog = true
            }
            .testTag("audio_call_ringtone_item"),
          headlineContent = { Text("Audio Call Ringtone", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.callRingtone, color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            IconButton(
              onClick = {
                if (playingToneName == settings.callRingtone) {
                  CallAudioAndVibrationManager.stopTonePreview()
                  playingToneName = null
                } else {
                  playingToneName = settings.callRingtone
                  CallAudioAndVibrationManager.playTonePreview(context, settings.callRingtone) {
                    playingToneName = null
                  }
                }
              },
              modifier = Modifier.testTag("preview_audio_ringtone_button")
            ) {
              Icon(
                imageVector = if (playingToneName == settings.callRingtone) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Preview Audio Ringtone",
                tint = TealPrimary
              )
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Video Call Ringtone Item
      item {
        ListItem(
          modifier = Modifier
            .clickable {
              isVideoRingtonePicker = true
              showRingtoneDialog = true
            }
            .testTag("video_call_ringtone_item"),
          headlineContent = { Text("Video Call Ringtone", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.videoCallRingtone, color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            IconButton(
              onClick = {
                if (playingToneName == settings.videoCallRingtone) {
                  CallAudioAndVibrationManager.stopTonePreview()
                  playingToneName = null
                } else {
                  playingToneName = settings.videoCallRingtone
                  CallAudioAndVibrationManager.playTonePreview(context, settings.videoCallRingtone) {
                    playingToneName = null
                  }
                }
              },
              modifier = Modifier.testTag("preview_video_ringtone_button")
            ) {
              Icon(
                imageVector = if (playingToneName == settings.videoCallRingtone) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Preview Video Ringtone",
                tint = TealPrimary
              )
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 4. VIBRATION SETTINGS
      item {
        Text(
          text = "VIBRATION",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier
            .clickable { showVibrationDialog = true }
            .testTag("vibration_pattern_item"),
          headlineContent = { Text("Call Vibration Pattern", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.vibrationPattern.label, color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.Vibration, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            TextButton(
              onClick = {
                CallAudioAndVibrationManager.triggerTestVibration(context, settings.vibrationPattern)
              },
              modifier = Modifier.testTag("test_vibration_button")
            ) {
              Text("Test", fontWeight = FontWeight.Bold)
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 5. MESSAGE NOTIFICATION SOUNDS
      item {
        Text(
          text = "MESSAGES & GROUPS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier
            .clickable {
              isGroupSoundPicker = false
              showMessageSoundDialog = true
            }
            .testTag("message_notification_sound_item"),
          headlineContent = { Text("Message Tone", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.messageNotificationSound, color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            IconButton(
              onClick = {
                CallAudioAndVibrationManager.playTonePreview(context, settings.messageNotificationSound)
              }
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = TealPrimary)
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier
            .clickable {
              isGroupSoundPicker = true
              showMessageSoundDialog = true
            }
            .testTag("group_notification_sound_item"),
          headlineContent = { Text("Group Notification Tone", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.groupNotificationSound, color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            IconButton(
              onClick = {
                CallAudioAndVibrationManager.playTonePreview(context, settings.groupNotificationSound)
              }
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = TealPrimary)
            }
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Lock Screen Notification Preview
      item {
        ListItem(
          headlineContent = { Text("Notification Previews", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Show message sender and preview text on lock screen", color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = settings.notificationPreviewLockscreen,
              onCheckedChange = { checked ->
                CallNotificationRepository.setNotificationPreviewLockscreen(checked)
              },
              modifier = Modifier.testTag("preview_lockscreen_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }

    // Ringtone Selection Dialog
    if (showRingtoneDialog) {
      val selectedCurrent = if (isVideoRingtonePicker) settings.videoCallRingtone else settings.callRingtone
      AlertDialog(
        onDismissRequest = {
          CallAudioAndVibrationManager.stopTonePreview()
          showRingtoneDialog = false
        },
        title = { Text(if (isVideoRingtonePicker) "Video Call Ringtone" else "Audio Call Ringtone", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            CallAudioAndVibrationManager.availableRingtones.forEach { tone ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    CallNotificationRepository.setCallRingtone(tone, isVideoRingtonePicker)
                    CallAudioAndVibrationManager.playTonePreview(context, tone)
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedCurrent == tone,
                  onClick = {
                    CallNotificationRepository.setCallRingtone(tone, isVideoRingtonePicker)
                    CallAudioAndVibrationManager.playTonePreview(context, tone)
                  },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = tone,
                  fontWeight = if (selectedCurrent == tone) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 14.sp
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              CallAudioAndVibrationManager.stopTonePreview()
              showRingtoneDialog = false
            }
          ) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    // Vibration Pattern Dialog
    if (showVibrationDialog) {
      AlertDialog(
        onDismissRequest = { showVibrationDialog = false },
        title = { Text("Vibration Pattern", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            VibrationPattern.values().forEach { pattern ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    CallNotificationRepository.setVibrationPattern(pattern)
                    CallAudioAndVibrationManager.triggerTestVibration(context, pattern)
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = settings.vibrationPattern == pattern,
                  onClick = {
                    CallNotificationRepository.setVibrationPattern(pattern)
                    CallAudioAndVibrationManager.triggerTestVibration(context, pattern)
                  },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = pattern.label,
                  fontWeight = if (settings.vibrationPattern == pattern) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 14.sp
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showVibrationDialog = false }) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    // Do Not Disturb Duration Dialog
    if (showDndDurationDialog) {
      AlertDialog(
        onDismissRequest = { showDndDurationDialog = false },
        title = { Text("Do Not Disturb Schedule", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            DndDuration.values().forEach { dndOption ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    CallNotificationRepository.setDndMode(dndOption)
                    showDndDurationDialog = false
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = settings.dndDuration == dndOption,
                  onClick = {
                    CallNotificationRepository.setDndMode(dndOption)
                    showDndDurationDialog = false
                  },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = dndOption.label,
                  fontWeight = if (settings.dndDuration == dndOption) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 14.sp
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showDndDurationDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Message / Group Sound Selection Dialog
    if (showMessageSoundDialog) {
      val toneList = if (isGroupSoundPicker) {
        CallAudioAndVibrationManager.availableGroupSounds
      } else {
        CallAudioAndVibrationManager.availableMessageSounds
      }
      val selectedCurrent = if (isGroupSoundPicker) settings.groupNotificationSound else settings.messageNotificationSound

      AlertDialog(
        onDismissRequest = {
          CallAudioAndVibrationManager.stopTonePreview()
          showMessageSoundDialog = false
        },
        title = { Text(if (isGroupSoundPicker) "Group Notification Sound" else "Message Notification Sound", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            toneList.forEach { sound ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    CallNotificationRepository.setMessageNotificationSound(sound, isGroupSoundPicker)
                    CallAudioAndVibrationManager.playTonePreview(context, sound)
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedCurrent == sound,
                  onClick = {
                    CallNotificationRepository.setMessageNotificationSound(sound, isGroupSoundPicker)
                    CallAudioAndVibrationManager.playTonePreview(context, sound)
                  },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = sound,
                  fontWeight = if (selectedCurrent == sound) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 14.sp
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              CallAudioAndVibrationManager.stopTonePreview()
              showMessageSoundDialog = false
            }
          ) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    // Restore Defaults Confirmation Dialog
    if (showResetConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showResetConfirmDialog = false },
        title = { Text("Restore notification defaults?") },
        text = { Text("Reset all ringtones, vibration patterns, silent mode, and notification sounds to default ChatMitra settings?") },
        confirmButton = {
          TextButton(
            onClick = {
              CallNotificationRepository.restoreDefaults()
              showResetConfirmDialog = false
              scope.launch {
                snackbarHostState.showSnackbar("All notification settings restored to defaults")
              }
            }
          ) {
            Text("Restore", color = TealPrimary, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetConfirmDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}
