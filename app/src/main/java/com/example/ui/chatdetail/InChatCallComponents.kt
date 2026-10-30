package com.example.ui.chatdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.webrtc.PeerConnectionState
import com.example.data.webrtc.SignalingState
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

/**
 * InChatCallBanner: Rendered at the top of the chat message list when call is minimized.
 * Allows continuous messaging while audio/video call is ongoing.
 */
@Composable
fun InChatCallBanner(
  contact: UserProfile,
  signalingState: SignalingState,
  onExpandClick: () -> Unit,
  onToggleMute: () -> Unit,
  onEndCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val minutes = signalingState.callDurationSec / 60
  val seconds = signalingState.callDurationSec % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 4.dp),
    shape = RoundedCornerShape(16.dp),
    color = TealDark,
    shadowElevation = 6.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      UserAvatar(
        name = contact.name,
        colorHex = contact.avatarColorHex,
        emoji = contact.avatarEmoji,
        size = 38.dp
      )

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(CallGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (signalingState.isVideo) "Video Call • $timeFormatted" else "Voice Call • $timeFormatted",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
        Text(
          text = "WebRTC P2P • ${signalingState.metrics.roundTripTimeMs}ms • Opus HD",
          style = MaterialTheme.typography.labelSmall,
          color = AmberAccent
        )
      }

      // Quick Mute button
      IconButton(
        onClick = onToggleMute,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = if (signalingState.isAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Mute",
          tint = if (signalingState.isAudioMuted) CallRed else Color.White
        )
      }

      // Expand to full view
      IconButton(
        onClick = onExpandClick,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.OpenInFull,
          contentDescription = "Expand Call",
          tint = Color.White
        )
      }

      // End Call
      IconButton(
        onClick = onEndCall,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(CallRed)
      ) {
        Icon(
          imageVector = Icons.Default.CallEnd,
          contentDescription = "End Call",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

/**
 * InChatCallOverlay: High-quality full WebRTC video/voice call panel displayed inside the chat screen.
 */
@Composable
fun InChatCallOverlay(
  contact: UserProfile,
  signalingState: SignalingState,
  onMinimize: () -> Unit,
  onToggleMute: () -> Unit,
  onToggleVideo: () -> Unit,
  onToggleSpeaker: () -> Unit,
  onSwitchCamera: () -> Unit,
  onEndCall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val minutes = signalingState.callDurationSec / 60
  val seconds = signalingState.callDurationSec % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF071B14),
            Color(0xFF0F382A),
            Color(0xFF071A13)
          )
        )
      )
  ) {
    // Top Bar: Minimize button, encryption pill, and quality badge
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onMinimize,
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.15f))
      ) {
        Icon(
          Icons.Default.CloseFullscreen,
          contentDescription = "Minimize to Chat",
          tint = Color.White
        )
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.45f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Default.Shield,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "WebRTC DTLS-SRTP E2EE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.45f)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(if (signalingState.peerConnectionState == PeerConnectionState.CONNECTED) CallGreen else AmberAccent)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "${signalingState.metrics.roundTripTimeMs}ms",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Main Center Section
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center)
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (signalingState.isVideo && !signalingState.isVideoMuted) {
        // Video Call View with PIP
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
            .border(2.dp, TealPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
          // Remote Video stream simulation
          Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            UserAvatar(
              name = contact.name,
              colorHex = contact.avatarColorHex,
              emoji = contact.avatarEmoji,
              size = 110.dp,
              fontSize = 44
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = contact.name,
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Peer Stream • 1080p 60fps • VP8",
              style = MaterialTheme.typography.labelMedium,
              color = TealPrimary
            )
          }

          // Local PIP video camera box
          Surface(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(14.dp)
              .size(width = 96.dp, height = 130.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E2922),
            shadowElevation = 8.dp
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("You", style = MaterialTheme.typography.labelSmall, color = Color.White)
                Text(if (signalingState.isFrontCamera) "Front" else "Rear", style = MaterialTheme.typography.labelSmall, color = AmberAccent)
              }
            }
          }
        }
      } else {
        // Audio Call View with Pulsating Wave radar
        Box(
          modifier = Modifier.size(190.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(180.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(TealPrimary.copy(alpha = 0.2f))
          )
          Box(
            modifier = Modifier
              .size(145.dp)
              .scale(pulseScale * 0.95f)
              .clip(CircleShape)
              .background(TealPrimary.copy(alpha = 0.35f))
          )
          UserAvatar(
            name = contact.name,
            colorHex = contact.avatarColorHex,
            emoji = contact.avatarEmoji,
            size = 100.dp,
            fontSize = 42
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = contact.name,
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = when (signalingState.peerConnectionState) {
            PeerConnectionState.NEW, PeerConnectionState.CHECKING -> "Connecting WebRTC peer..."
            PeerConnectionState.CONNECTING -> "Exchanging ICE candidates..."
            PeerConnectionState.CONNECTED -> timeFormatted
            PeerConnectionState.DISCONNECTED -> "Reconnecting..."
            PeerConnectionState.FAILED -> "Call Failed"
            PeerConnectionState.CLOSED -> "Call Ended"
          },
          style = MaterialTheme.typography.titleMedium,
          color = if (signalingState.peerConnectionState == PeerConnectionState.CONNECTED) CallGreen else AmberAccent,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Opus HD 48kHz • STUN P2P • AES-128-GCM",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.7f)
        )
      }
    }

    // Bottom Controls Row
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .padding(16.dp),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF092118).copy(alpha = 0.92f))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Speakerphone
        IconButton(
          onClick = onToggleSpeaker,
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(if (signalingState.isSpeakerphoneOn) TealPrimary else Color.White.copy(alpha = 0.15f))
        ) {
          Icon(
            if (signalingState.isSpeakerphoneOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
            contentDescription = "Speaker",
            tint = Color.White
          )
        }

        // Camera Toggle (for video calls)
        if (signalingState.isVideo) {
          IconButton(
            onClick = onToggleVideo,
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(if (signalingState.isVideoMuted) CallRed else Color.White.copy(alpha = 0.15f))
          ) {
            Icon(
              if (signalingState.isVideoMuted) Icons.Default.VideocamOff else Icons.Default.Videocam,
              contentDescription = "Video",
              tint = Color.White
            )
          }

          IconButton(
            onClick = onSwitchCamera,
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.15f))
          ) {
            Icon(
              Icons.Default.Cameraswitch,
              contentDescription = "Flip Camera",
              tint = Color.White
            )
          }
        }

        // Mic Mute
        IconButton(
          onClick = onToggleMute,
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(if (signalingState.isAudioMuted) CallRed else Color.White.copy(alpha = 0.15f))
        ) {
          Icon(
            if (signalingState.isAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Mic",
            tint = Color.White
          )
        }

        // End Call
        IconButton(
          onClick = onEndCall,
          modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(CallRed)
            .testTag("end_call_button")
        ) {
          Icon(
            Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }
  }
}
