package com.example.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.repository.CallNotificationRepository
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsSettingsScreen(
  onNavigateBack: () -> Unit,
  onNavigateCallHistory: () -> Unit
) {
  val context = LocalContext.current
  val settings by CallNotificationRepository.settings.collectAsState()
  val privacySettings by ChatMitraRepository.privacySettings.collectAsState()
  val callLogs by ChatMitraRepository.callLogs.collectAsState()

  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { result ->
    hasMicPermission = result[Manifest.permission.RECORD_AUDIO] ?: hasMicPermission
    hasCameraPermission = result[Manifest.permission.CAMERA] ?: hasCameraPermission
  }

  var fullScreenAlertsEnabled by remember { mutableStateOf(true) }
  var callWaitingEnabled by remember { mutableStateOf(true) }
  var outgoingRingbackEnabled by remember { mutableStateOf(true) }
  var autoSpeakerForVideo by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Calls", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("calls_settings_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("calls_settings_screen")
    ) {
      // 1. INCOMING CALLS
      item {
        Text(
          text = "INCOMING CALLS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("Full-Screen Call Alerts", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Show heads-up and full-screen incoming call UI", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = fullScreenAlertsEnabled,
              onCheckedChange = { fullScreenAlertsEnabled = it },
              modifier = Modifier.testTag("full_screen_alerts_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Silence Unknown Callers", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Calls from unknown numbers are silenced and saved to history", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.Security, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = privacySettings.silenceUnknownCallers,
              onCheckedChange = { checked ->
                ChatMitraRepository.updatePrivacySettings(silenceUnknownCallers = checked)
              },
              modifier = Modifier.testTag("silence_unknown_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Call Waiting", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Notify me of another incoming call while currently on a call", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = callWaitingEnabled,
              onCheckedChange = { callWaitingEnabled = it },
              modifier = Modifier.testTag("call_waiting_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 2. OUTGOING CALLS
      item {
        Text(
          text = "OUTGOING CALLS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("Ringback Tone", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Play outgoing ringback tone while waiting for answer", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.Call, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = outgoingRingbackEnabled,
              onCheckedChange = { outgoingRingbackEnabled = it },
              modifier = Modifier.testTag("ringback_tone_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Default to Speaker for Video", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Automatically route audio to speakerphone during video calls", color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = autoSpeakerForVideo,
              onCheckedChange = { autoSpeakerForVideo = it },
              modifier = Modifier.testTag("auto_speaker_video_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 3. CALL HISTORY STATS
      item {
        Text(
          text = "CALL HISTORY & LOGS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier
            .clickable { onNavigateCallHistory() }
            .testTag("view_call_history_item"),
          headlineContent = { Text("View Call History", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("${callLogs.size} recorded calls • Tap to inspect or clear", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.History, contentDescription = null, tint = TealPrimary)
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 4. DATA USAGE
      item {
        Text(
          text = "DATA USAGE",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("Use Less Data for Calls", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Reduces the data consumed during audio and video calls", color = CheckmarkGrey) },
          leadingContent = {
            Icon(Icons.Default.DataUsage, contentDescription = null, tint = TealPrimary)
          },
          trailingContent = {
            Switch(
              checked = settings.lowDataUsageForCalls,
              onCheckedChange = { checked ->
                CallNotificationRepository.setLowDataUsageForCalls(checked)
              },
              modifier = Modifier.testTag("low_data_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // 5. CAMERA & MICROPHONE PERMISSIONS CARD
      item {
        Text(
          text = "CAMERA & MICROPHONE PERMISSIONS",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = TealPrimary,
          modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp)
        )
      }

      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("permissions_status_card"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Device Hardware Access",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "ChatMitra requires microphone access for audio calls and camera access for video calls. Audio and video streams are encrypted end-to-end and never stored on any server.",
              fontSize = 12.sp,
              color = CheckmarkGrey
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Microphone Status Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = if (hasMicPermission) CallGreen else CallRed)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Microphone", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                  Text(
                    text = if (hasMicPermission) "Permission Granted" else "Permission Required",
                    fontSize = 11.sp,
                    color = if (hasMicPermission) CallGreen else CallRed
                  )
                }
              }
              Icon(
                imageVector = if (hasMicPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasMicPermission) CallGreen else CallRed,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Camera Status Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = if (hasCameraPermission) CallGreen else CallRed)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Camera", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                  Text(
                    text = if (hasCameraPermission) "Permission Granted" else "Permission Required",
                    fontSize = 11.sp,
                    color = if (hasCameraPermission) CallGreen else CallRed
                  )
                }
              }
              Icon(
                imageVector = if (hasCameraPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasCameraPermission) CallGreen else CallRed,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasMicPermission || !hasCameraPermission) {
              FilledTonalButton(
                onClick = {
                  permissionLauncher.launch(
                    arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                  )
                },
                modifier = Modifier.fillMaxWidth().testTag("request_permissions_button"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TealPrimary, contentColor = Color.White)
              ) {
                Text("Grant Hardware Permissions", fontWeight = FontWeight.Bold)
              }
            } else {
              FilledTonalButton(
                onClick = {
                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                  }
                  context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().testTag("open_app_settings_button")
              ) {
                Text("Manage in System Settings", fontSize = 13.sp)
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }
  }
}
