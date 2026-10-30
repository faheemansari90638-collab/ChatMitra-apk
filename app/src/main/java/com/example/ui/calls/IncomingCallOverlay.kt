package com.example.ui.calls

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveCallSession
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.TealPrimary

@Composable
fun IncomingCallOverlay(
  session: ActiveCallSession,
  onDismiss: () -> Unit = {}
) {
  val context = LocalContext.current
  val contact = session.contact
  val isVideo = session.isVideo

  val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Surface(
    modifier = Modifier.fillMaxSize().testTag("incoming_call_screen"),
    color = Color(0xFF071510)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF0A241C),
              Color(0xFF040E0A)
            )
          )
        )
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Top Header
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(top = 24.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(TealPrimary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
              contentDescription = null,
              tint = TealPrimary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
              text = if (isVideo) "Incoming Video Call" else "Incoming Audio Call",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = contact?.name ?: "ChatMitra Caller",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = contact?.phone ?: "+91 98765 43210",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
          )
        }

        // Center Pulsing Avatar
        Box(
          modifier = Modifier.size(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(190.dp)
              .scale(pulseScale)
              .background(TealPrimary.copy(alpha = 0.15f), CircleShape)
          )
          Box(
            modifier = Modifier
              .size(160.dp)
              .background(TealPrimary.copy(alpha = 0.3f), CircleShape)
          )
          UserAvatar(
            name = contact?.name ?: "User",
            colorHex = contact?.avatarColorHex ?: 0xFF00A884,
            emoji = contact?.avatarEmoji ?: "👤",
            size = 130.dp,
            fontSize = 58
          )
        }

        // Quick Message Reply Bar
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Message,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.7f),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              text = "Swipe up or tap to reply with quick message",
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 12.sp
            )
          }
        }

        // Bottom Accept / Decline Buttons
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Decline Call Button
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = {
                ChatMitraRepository.declineIncomingCall(context)
                onDismiss()
              },
              modifier = Modifier
                .size(72.dp)
                .background(CallRed, CircleShape)
                .testTag("decline_call_button")
            ) {
              Icon(
                Icons.Default.CallEnd,
                contentDescription = "Decline Call",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Decline",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }

          // Accept Call Button
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = {
                ChatMitraRepository.acceptIncomingCall(context)
              },
              modifier = Modifier
                .size(72.dp)
                .background(CallGreen, CircleShape)
                .testTag("accept_call_button")
            ) {
              Icon(
                imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Accept Call",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Accept",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}
