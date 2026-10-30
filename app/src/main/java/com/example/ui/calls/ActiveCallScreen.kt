package com.example.ui.calls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.model.AudioRoute
import com.example.data.model.CallState
import com.example.data.model.VideoCallQuality
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.VideoCallSettingsRepository
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.TealPrimary

@Composable
fun ActiveCallScreen(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val session by ChatMitraRepository.activeCall.collectAsState()

  if (session == null || session?.callState == CallState.IDLE) {
    onDismiss()
    return
  }

  // If incoming call is ringing, display the full-screen IncomingCallOverlay
  if (session?.callState == CallState.INCOMING_RINGING) {
    IncomingCallOverlay(
      session = session!!,
      onDismiss = onDismiss
    )
    return
  }

  // If minimized, do not display full screen
  if (session?.isMinimized == true) {
    return
  }

  val currentSession = session!!
  val contact = currentSession.contact
  val isVideo = currentSession.isVideo
  val isConnected = currentSession.callState == CallState.CONNECTED
  val isOutgoingWaiting = currentSession.callState == CallState.OUTGOING_CALLING ||
    currentSession.callState == CallState.OUTGOING_RINGING ||
    currentSession.callState == CallState.CONNECTING

  var showAudioRouteDialog by remember { mutableStateOf(false) }
  var showVideoQualityDialog by remember { mutableStateOf(false) }

  val videoQuality by VideoCallSettingsRepository.videoQuality.collectAsState()
  val localZoom by VideoCallSettingsRepository.localZoom.collectAsState()
  val remoteZoom by VideoCallSettingsRepository.remoteZoom.collectAsState()
  val isRemoteZoomActive by VideoCallSettingsRepository.isRemoteZoomActive.collectAsState()

  val infiniteTransition = rememberInfiniteTransition(label = "calling_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Surface(
    modifier = Modifier.fillMaxSize().testTag("active_call_screen"),
    color = Color(0xFF0F172A)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      if (isVideo && isConnected && !currentSession.isVideoCameraOff) {
        // Video Call Background simulation with Remote Zoom
        Box(
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
              scaleX = if (isRemoteZoomActive) remoteZoom else 1f,
              scaleY = if (isRemoteZoomActive) remoteZoom else 1f
            )
            .background(
              Brush.radialGradient(
                colors = listOf(Color(0xFF1E293B), Color(0xFF0A0F1D))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(
              name = contact?.name ?: "Contact",
              colorHex = contact?.avatarColorHex ?: 0xFF00A884,
              emoji = contact?.avatarEmoji ?: "👤",
              size = 120.dp,
              fontSize = 54
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Live WebRTC Camera Stream Active",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.8f)
            )
            Text(
              text = "HD ${videoQuality.resolutionLabel} • ~${videoQuality.bitrateKbps} kbps",
              style = MaterialTheme.typography.labelSmall,
              color = TealPrimary,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          // Local Camera PIP Thumbnail with Hardware Zoom
          Card(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 72.dp, end = 20.dp)
              .size(width = 100.dp, height = 140.dp)
              .graphicsLayer(
                scaleX = if (!isRemoteZoomActive) localZoom else 1f,
                scaleY = if (!isRemoteZoomActive) localZoom else 1f
              )
              .border(2.dp, TealPrimary, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "You", color = Color.White, style = MaterialTheme.typography.labelSmall)
                Text(
                  text = if (currentSession.isFrontCamera) "Front Camera" else "Rear Camera",
                  color = TealPrimary,
                  fontSize = 11.sp
                )
                if (!isRemoteZoomActive && localZoom > 1.0f) {
                  Text(
                    text = String.format("%.1fx Zoom", localZoom),
                    color = AmberAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        // Floating Zoom Controller on Right Edge
        Card(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 12.dp)
            .testTag("floating_zoom_controls"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.88f))
        ) {
          Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Target Selector Toggle
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isRemoteZoomActive) AmberAccent.copy(alpha = 0.25f) else TealPrimary.copy(alpha = 0.25f),
              modifier = Modifier
                .clickable { VideoCallSettingsRepository.toggleRemoteZoomMode() }
                .testTag("toggle_zoom_target_btn")
            ) {
              Text(
                text = if (isRemoteZoomActive) "Remote" else "Camera",
                color = if (isRemoteZoomActive) AmberAccent else TealPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Zoom In (+)
            IconButton(
              onClick = {
                if (isRemoteZoomActive) VideoCallSettingsRepository.zoomInRemote()
                else VideoCallSettingsRepository.zoomInLocal()
              },
              modifier = Modifier.size(36.dp).testTag("zoom_in_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Current Zoom Ratio Pill
            val currentZoom = if (isRemoteZoomActive) remoteZoom else localZoom
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color.White.copy(alpha = 0.15f),
              modifier = Modifier.padding(vertical = 2.dp)
            ) {
              Text(
                text = String.format("%.1fx", currentZoom),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            // Zoom Out (-)
            IconButton(
              onClick = {
                if (isRemoteZoomActive) VideoCallSettingsRepository.zoomOutRemote()
                else VideoCallSettingsRepository.zoomOutLocal()
              },
              modifier = Modifier.size(36.dp).testTag("zoom_out_button")
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Reset Zoom Button (1.0x)
            Box(
              modifier = Modifier
                .size(28.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .clickable {
                  if (isRemoteZoomActive) VideoCallSettingsRepository.resetRemoteZoom()
                  else VideoCallSettingsRepository.resetLocalZoom()
                }
                .testTag("reset_zoom_button"),
              contentAlignment = Alignment.Center
            ) {
              Text("1x", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        // Audio Call Background
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xFF0B1E19), Color(0xFF081210))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(160.dp)
              .scale(if (isConnected) 1f else pulseScale)
              .background(TealPrimary.copy(alpha = 0.2f), CircleShape)
          )
        }
      }

      // Top App Bar with Minimize, Security Badge, HD Quality, and Network Badge
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 40.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { ChatMitraRepository.toggleMinimizeCall() },
          modifier = Modifier.testTag("minimize_call_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Minimize Call",
            tint = Color.White
          )
        }

        // Encryption Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "E2EE",
            tint = TealPrimary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "E2EE",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // HD Video Quality Selector Badge
        if (isVideo && isConnected) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
              .clickable { showVideoQualityDialog = true }
              .testTag("video_quality_badge")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                Icons.Default.HighQuality,
                contentDescription = "HD Quality",
                tint = TealPrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = videoQuality.resolutionLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Network Quality Badge
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = currentSession.networkQuality.label,
            color = AmberAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // Call Waiting Floating Alert
      currentSession.callWaitingContact?.let { waitingCaller ->
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 96.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Call Waiting: ${waitingCaller.name}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = "Incoming ${if (currentSession.isCallWaitingVideo) "Video" else "Audio"} Call",
                color = TealPrimary,
                fontSize = 11.sp
              )
            }
            Row {
              IconButton(
                onClick = { ChatMitraRepository.declineCallWaiting() },
                modifier = Modifier.size(34.dp).background(CallRed, CircleShape)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Decline Waiting", tint = Color.White, modifier = Modifier.size(16.dp))
              }
              Spacer(modifier = Modifier.width(8.dp))
              IconButton(
                onClick = { ChatMitraRepository.answerCallWaiting(context) },
                modifier = Modifier.size(34.dp).background(CallGreen, CircleShape)
              ) {
                Icon(Icons.Default.Call, contentDescription = "Answer Waiting", tint = Color.White, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // Main Call Body
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 110.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Contact Profile & Call State
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          if (!isVideo || !isConnected || currentSession.isVideoCameraOff) {
            UserAvatar(
              name = contact?.name ?: "Contact",
              colorHex = contact?.avatarColorHex ?: 0xFF00A884,
              emoji = contact?.avatarEmoji ?: "👤",
              size = 110.dp,
              fontSize = 48
            )
            Spacer(modifier = Modifier.height(20.dp))
          }

          Text(
            text = contact?.name ?: "ChatMitra Call",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = contact?.phone ?: "+91 98765 43210",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Call State Label / Timer
          val statusText = when (currentSession.callState) {
            CallState.OUTGOING_CALLING -> "Calling..."
            CallState.OUTGOING_RINGING -> "Ringing..."
            CallState.CONNECTING -> "Connecting..."
            CallState.RECONNECTING -> "Reconnecting..."
            CallState.BUSY -> "User is on another call"
            CallState.DECLINED -> "Call Declined"
            CallState.UNAVAILABLE -> "Unavailable"
            CallState.FAILED -> "Call Failed"
            CallState.ENDED -> "Call Ended"
            CallState.CONNECTED -> {
              val min = (currentSession.durationSeconds / 60)
              val sec = (currentSession.durationSeconds % 60)
              String.format("%02d:%02d", min, sec)
            }
            else -> "Connecting..."
          }

          Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            color = if (isConnected) CallGreen else TealPrimary,
            fontWeight = FontWeight.SemiBold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = currentSession.signalingState,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
          )
        }

        // Bottom Controls Bar
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (isOutgoingWaiting) {
            // Cancel Call Button during calling/ringing
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              IconButton(
                onClick = { ChatMitraRepository.cancelOutgoingCall(context) },
                modifier = Modifier
                  .size(68.dp)
                  .background(CallRed, CircleShape)
                  .testTag("cancel_call_button")
              ) {
                Icon(
                  Icons.Default.CallEnd,
                  contentDescription = "Cancel Call",
                  tint = Color.White,
                  modifier = Modifier.size(32.dp)
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text("Cancel", color = Color.White, fontSize = 13.sp)
            }
          } else {
            // Active In-Call Controls Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
              shape = RoundedCornerShape(24.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.95f))
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                // Secondary Controls Row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Mute Mic
                  IconButton(
                    onClick = { ChatMitraRepository.toggleMuteCall() },
                    modifier = Modifier
                      .size(50.dp)
                      .background(
                        if (currentSession.isMuted) CallRed else Color.White.copy(alpha = 0.15f),
                        CircleShape
                      )
                      .testTag("mute_call_button")
                  ) {
                    Icon(
                      imageVector = if (currentSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                      contentDescription = "Mute",
                      tint = Color.White
                    )
                  }

                  // Speaker / Audio Route
                  IconButton(
                    onClick = { showAudioRouteDialog = true },
                    modifier = Modifier
                      .size(50.dp)
                      .background(
                        if (currentSession.isSpeakerOn) TealPrimary else Color.White.copy(alpha = 0.15f),
                        CircleShape
                      )
                      .testTag("speaker_route_button")
                  ) {
                    val icon = when (currentSession.audioRoute) {
                      AudioRoute.SPEAKER -> Icons.Default.VolumeUp
                      AudioRoute.BLUETOOTH -> Icons.Default.Bluetooth
                      AudioRoute.EARPIECE -> Icons.Default.PhoneInTalk
                    }
                    Icon(
                      imageVector = icon,
                      contentDescription = "Audio Route",
                      tint = Color.White
                    )
                  }

                  // Video Camera Toggle
                  if (isVideo) {
                    IconButton(
                      onClick = { ChatMitraRepository.toggleVideoCamera() },
                      modifier = Modifier
                        .size(50.dp)
                        .background(
                          if (currentSession.isVideoCameraOff) CallRed else Color.White.copy(alpha = 0.15f),
                          CircleShape
                        )
                        .testTag("camera_toggle_button")
                    ) {
                      Icon(
                        imageVector = if (currentSession.isVideoCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Camera Toggle",
                        tint = Color.White
                      )
                    }

                    // Flip Camera
                    IconButton(
                      onClick = { ChatMitraRepository.switchCamera() },
                      modifier = Modifier
                        .size(50.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .testTag("flip_camera_button")
                    ) {
                      Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Flip Camera",
                        tint = Color.White
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // End Call Button
                IconButton(
                  onClick = { ChatMitraRepository.endCall(context) },
                  modifier = Modifier
                    .size(68.dp)
                    .background(CallRed, CircleShape)
                    .testTag("end_call_button")
                ) {
                  Icon(
                    Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Audio Route Selection Dialog
      if (showAudioRouteDialog) {
        AlertDialog(
          onDismissRequest = { showAudioRouteDialog = false },
          title = { Text("Select Audio Output", fontWeight = FontWeight.Bold) },
          text = {
            Column {
              AudioRoute.values().forEach { route ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      ChatMitraRepository.setAudioRoute(route, context)
                      showAudioRouteDialog = false
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = when (route) {
                      AudioRoute.EARPIECE -> Icons.Default.PhoneInTalk
                      AudioRoute.SPEAKER -> Icons.Default.VolumeUp
                      AudioRoute.BLUETOOTH -> Icons.Default.Bluetooth
                    },
                    contentDescription = null,
                    tint = if (currentSession.audioRoute == route) TealPrimary else Color.Gray
                  )
                  Spacer(modifier = Modifier.width(16.dp))
                  Text(
                    text = route.label,
                    fontWeight = if (currentSession.audioRoute == route) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentSession.audioRoute == route) TealPrimary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          },
          confirmButton = {
            TextButton(onClick = { showAudioRouteDialog = false }) {
              Text("Close")
            }
          }
        )
      }

      // HD Video Quality Selection Dialog
      if (showVideoQualityDialog) {
        AlertDialog(
          onDismissRequest = { showVideoQualityDialog = false },
          icon = { Icon(Icons.Default.HighQuality, contentDescription = null, tint = TealPrimary) },
          title = { Text("HD Video Call Quality", fontWeight = FontWeight.Bold) },
          text = {
            Column {
              Text(
                "Adaptive WebRTC video stream dynamically optimizes resolution and frame rate according to connection bandwidth.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(12.dp))
              VideoCallQuality.values().forEach { quality ->
                val isSelected = videoQuality == quality
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) TealPrimary.copy(alpha = 0.15f) else Color.Transparent,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      VideoCallSettingsRepository.setVideoQuality(quality)
                      showVideoQualityDialog = false
                    }
                    .padding(vertical = 4.dp)
                    .testTag("quality_option_${quality.name}")
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = quality.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = "${quality.resolutionLabel} • ~${quality.bitrateKbps} kbps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    if (isSelected) {
                      Icon(Icons.Default.Tune, contentDescription = "Active", tint = TealPrimary, modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          },
          confirmButton = {
            TextButton(onClick = { showVideoQualityDialog = false }) {
              Text("Done")
            }
          }
        )
      }
    }
  }
}
