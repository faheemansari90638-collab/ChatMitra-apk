package com.example.ui.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.TealDark

@Composable
fun MinimizedCallBanner(
  session: ActiveCallSession,
  onMaximize: () -> Unit
) {
  val context = LocalContext.current
  val contact = session.contact
  val min = session.durationSeconds / 60
  val sec = session.durationSeconds % 60
  val durationFormatted = String.format("%02d:%02d", min, sec)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .clickable { onMaximize() }
      .testTag("minimized_call_banner"),
    colors = CardDefaults.cardColors(containerColor = TealDark),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(CallGreen, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (session.isVideo) Icons.Default.Videocam else Icons.Default.Call,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = contact?.name ?: "ChatMitra Call",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Text(
          text = "$durationFormatted • Tap to return to call",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 12.sp
        )
      }

      // Mute Toggle
      IconButton(
        onClick = { ChatMitraRepository.toggleMuteCall() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = if (session.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Mute",
          tint = if (session.isMuted) CallRed else Color.White,
          modifier = Modifier.size(20.dp)
        )
      }

      // End Call
      IconButton(
        onClick = { ChatMitraRepository.endCall(context) },
        modifier = Modifier
          .size(36.dp)
          .background(CallRed, CircleShape)
          .testTag("minimized_end_call_button")
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
