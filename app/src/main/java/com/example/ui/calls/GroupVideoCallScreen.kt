package com.example.ui.calls

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallParticipant
import com.example.data.model.ParticipantRole
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.GroupCallingRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupVideoCallScreen(
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val activeSession by GroupCallingRepository.activeGroupCall.collectAsState()
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val allContacts by ChatMitraRepository.contacts.collectAsState()

  var showAddParticipantSheet by remember { mutableStateOf(false) }
  var showQualityMenu by remember { mutableStateOf(false) }
  var selectedParticipantForManage by remember { mutableStateOf<CallParticipant?>(null) }

  val session = activeSession
  if (session == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0F172A)),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Call Ended", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(56.dp)
            .background(TealPrimary, CircleShape)
        ) {
          Icon(Icons.Default.CallEnd, contentDescription = "Close", tint = Color.White)
        }
      }
    }
    return
  }

  val isHost = session.hostId == currentUser?.id
  val myParticipant = session.participants.find { it.user.id == currentUser?.id }
  val isFull = session.participants.size >= session.maxParticipants

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B141A))
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Status Bar: Call Title, Participant Count (x/8), Quality Badge
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0x80000000))
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = session.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF22C55E), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${session.participants.size}/${session.maxParticipants} participants (Max 8)",
              color = if (isFull) AmberAccent else Color(0xFFCBD5E1),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Quality Mode Selector
          Box {
            Surface(
              onClick = { showQualityMenu = true },
              shape = RoundedCornerShape(12.dp),
              color = Color(0x33FFFFFF),
              modifier = Modifier.testTag("call_quality_selector")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.Speed,
                  contentDescription = "Quality",
                  tint = AmberAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = when (session.callQualityMode) {
                    "HD" -> "HD (720p)"
                    "SD" -> "SD (480p)"
                    "LOW_DATA" -> "Low Data (240p)"
                    else -> session.callQualityMode
                  },
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            DropdownMenu(
              expanded = showQualityMenu,
              onDismissRequest = { showQualityMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("HD Quality (720p • 1.5 Mbps)") },
                onClick = {
                  GroupCallingRepository.setCallQualityMode("HD")
                  showQualityMenu = false
                }
              )
              DropdownMenuItem(
                text = { Text("Standard Quality (480p • 500 Kbps)") },
                onClick = {
                  GroupCallingRepository.setCallQualityMode("SD")
                  showQualityMenu = false
                }
              )
              DropdownMenuItem(
                text = { Text("Low Data Mode (240p • 150 Kbps)") },
                onClick = {
                  GroupCallingRepository.setCallQualityMode("LOW_DATA")
                  showQualityMenu = false
                }
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Add participant button (if not full)
          if (!isFull) {
            IconButton(
              onClick = { showAddParticipantSheet = true },
              modifier = Modifier
                .size(36.dp)
                .background(TealPrimary, CircleShape)
                .testTag("add_participant_btn")
            ) {
              Icon(
                Icons.Default.PersonAdd,
                contentDescription = "Add Participant",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      // Participant Video Grid
      val participants = session.participants
      val gridColumns = if (participants.size <= 2) 1 else 2

      LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        modifier = Modifier
          .weight(1f)
          .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(participants, key = { it.user.id }) { participant ->
          val isActiveSpeaker = session.activeSpeakerId == participant.user.id
          val isSelf = participant.user.id == currentUser?.id

          ParticipantVideoTile(
            participant = participant,
            isActiveSpeaker = isActiveSpeaker,
            isSelf = isSelf,
            onTileClick = {
              if (isHost && !isSelf) {
                selectedParticipantForManage = participant
              } else {
                GroupCallingRepository.setActiveSpeaker(participant.user.id)
              }
            }
          )
        }
      }

      // Bottom Call Controls Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xCC111827))
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Toggle Audio Mic
        IconButton(
          onClick = {
            currentUser?.id?.let { GroupCallingRepository.toggleAudioMute(it) }
          },
          modifier = Modifier
            .size(52.dp)
            .background(
              if (myParticipant?.isAudioMuted == true) Color(0xFFEF4444) else Color(0x33FFFFFF),
              CircleShape
            )
            .testTag("toggle_mic_btn")
        ) {
          Icon(
            imageVector = if (myParticipant?.isAudioMuted == true) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Toggle Mic",
            tint = Color.White
          )
        }

        // Toggle Video Camera
        IconButton(
          onClick = {
            currentUser?.id?.let { GroupCallingRepository.toggleVideoMute(it) }
          },
          modifier = Modifier
            .size(52.dp)
            .background(
              if (myParticipant?.isVideoMuted == true) Color(0xFFEF4444) else Color(0x33FFFFFF),
              CircleShape
            )
            .testTag("toggle_camera_btn")
        ) {
          Icon(
            imageVector = if (myParticipant?.isVideoMuted == true) Icons.Default.VideocamOff else Icons.Default.Videocam,
            contentDescription = "Toggle Video",
            tint = Color.White
          )
        }

        // End / Leave Call Button
        IconButton(
          onClick = {
            GroupCallingRepository.endGroupCall()
            onDismiss()
          },
          modifier = Modifier
            .size(56.dp)
            .background(CallRed, CircleShape)
            .testTag("end_group_call_btn")
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }

    // Modal Sheet: Add Participant to Call
    if (showAddParticipantSheet) {
      ModalBottomSheet(
        onDismissRequest = { showAddParticipantSheet = false }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Text(
            "Add Participant (${session.participants.size}/8)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Peer-to-peer capacity limited to 8 devices for maximum stability.",
            color = CheckmarkGrey,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(16.dp))

          val eligibleContacts = allContacts.filter { c ->
            session.participants.none { p -> p.user.id == c.id }
          }

          if (eligibleContacts.isEmpty()) {
            Text(
              "No more contacts available to invite.",
              color = CheckmarkGrey,
              fontSize = 14.sp,
              modifier = Modifier.padding(vertical = 16.dp)
            )
          } else {
            eligibleContacts.forEach { contact ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    val added = GroupCallingRepository.addParticipant(contact)
                    if (added) {
                      Toast.makeText(context, "${contact.name} added to call", Toast.LENGTH_SHORT).show()
                      showAddParticipantSheet = false
                    } else {
                      Toast.makeText(context, "Group call capacity full (Max 8)", Toast.LENGTH_SHORT).show()
                    }
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                UserAvatar(name = contact.name, colorHex = contact.avatarColorHex, emoji = contact.avatarEmoji, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(contact.name, fontWeight = FontWeight.SemiBold)
                  if (contact.username.isNotBlank()) {
                    Text("@${contact.username}", color = TealPrimary, fontSize = 12.sp)
                  }
                }
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TealPrimary)
              }
            }
          }
        }
      }
    }

    // Modal Sheet: Host Management for Participant
    selectedParticipantForManage?.let { participant ->
      ModalBottomSheet(
        onDismissRequest = { selectedParticipantForManage = null }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
              name = participant.user.name,
              colorHex = participant.user.avatarColorHex,
              emoji = participant.user.avatarEmoji,
              size = 48.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(participant.user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text("Participant Management", color = CheckmarkGrey, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Mute Audio
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                GroupCallingRepository.toggleAudioMute(participant.user.id)
                selectedParticipantForManage = null
              }
              .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              if (participant.isAudioMuted) Icons.Default.Mic else Icons.Default.MicOff,
              contentDescription = null,
              tint = TealPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(if (participant.isAudioMuted) "Unmute Audio" else "Mute Participant Audio")
          }

          // Kick from Call
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                GroupCallingRepository.removeParticipant(participant.user.id)
                Toast.makeText(context, "${participant.user.name} removed from call", Toast.LENGTH_SHORT).show()
                selectedParticipantForManage = null
              }
              .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.PersonRemove, contentDescription = null, tint = CallRed)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Remove From Call", color = CallRed, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
private fun ParticipantVideoTile(
  participant: CallParticipant,
  isActiveSpeaker: Boolean,
  isSelf: Boolean,
  onTileClick: () -> Unit
) {
  val borderModifier = if (isActiveSpeaker) {
    Modifier.border(2.dp, Color(0xFF22C55E), RoundedCornerShape(16.dp))
  } else {
    Modifier
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1.2f)
      .then(borderModifier)
      .clickable(onClick = onTileClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (participant.isVideoMuted) Color(0xFF1E293B) else Color(0xFF0F172A)
    )
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      if (participant.isVideoMuted) {
        // Avatar placeholder when video is off
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          UserAvatar(
            name = participant.user.name,
            colorHex = participant.user.avatarColorHex,
            emoji = participant.user.avatarEmoji,
            size = 56.dp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "Camera Off",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
          )
        }
      } else {
        // Video Stream simulation background with subtle gradient
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color(0xFF1E3A8A).copy(alpha = 0.6f), Color(0xFF0F172A))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          UserAvatar(
            name = participant.user.name,
            colorHex = participant.user.avatarColorHex,
            emoji = participant.user.avatarEmoji,
            size = 48.dp
          )
        }
      }

      // Name & Status Overlay at Bottom
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .background(Color(0x80000000))
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (isSelf) "${participant.user.name} (You)" else participant.user.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (participant.role == ParticipantRole.HOST) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "HOST",
              color = AmberAccent,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (participant.isAudioMuted) {
            Icon(
              Icons.Default.MicOff,
              contentDescription = "Muted",
              tint = CallRed,
              modifier = Modifier.size(14.dp)
            )
          }
          if (isActiveSpeaker) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF22C55E), CircleShape)
            )
          }
        }
      }
    }
  }
}
