package com.example.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.LinkedDevice
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedDevicesScreen(
  onBackClick: () -> Unit
) {
  val linkedDevices by ChatMitraRepository.linkedDevices.collectAsState()

  var deviceToUnlink by remember { mutableStateOf<LinkedDevice?>(null) }
  var showUnlinkAllDialog by remember { mutableStateOf(false) }
  var showPairingDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Linked Devices", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header Banner
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(TealPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Devices, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(38.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Use ChatMitra on Other Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "Your personal chats are end-to-end encrypted across all your phones, tablets, and computers.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { showPairingDialog = true },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Link a Device", fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Device Status", style = MaterialTheme.typography.titleSmall, color = TealPrimary, fontWeight = FontWeight.Bold)
          if (linkedDevices.size > 1) {
            TextButton(onClick = { showUnlinkAllDialog = true }) {
              Text("Log out of all devices", color = CallRed, style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }

      items(linkedDevices, key = { it.id }) { device ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          ListItem(
            leadingContent = {
              Icon(
                imageVector = if (device.platform.contains("Android", ignoreCase = true)) Icons.Default.PhoneAndroid else Icons.Default.Computer,
                contentDescription = null,
                tint = if (device.isCurrentDevice) CallGreen else TealPrimary,
                modifier = Modifier.size(32.dp)
              )
            },
            headlineContent = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(device.name, fontWeight = FontWeight.SemiBold)
                if (device.isCurrentDevice) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(shape = RoundedCornerShape(6.dp), color = CallGreen.copy(alpha = 0.15f)) {
                    Text("This device", color = CallGreen, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                  }
                }
              }
            },
            supportingContent = {
              Text("${device.location} • ${device.lastActiveFormatted}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingContent = {
              if (!device.isCurrentDevice) {
                OutlinedButton(
                  onClick = { deviceToUnlink = device },
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("Log out")
                }
              }
            }
          )
        }
      }
    }
  }

  // QR Pairing Modal
  if (showPairingDialog) {
    AlertDialog(
      onDismissRequest = { showPairingDialog = false },
      title = { Text("Link with QR Code", fontWeight = FontWeight.Bold) },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Text("Open chatmitra.app on your computer or browser and scan this QR code to authenticate with your end-to-end encryption key.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
          Spacer(modifier = Modifier.height(16.dp))
          Box(
            modifier = Modifier
              .size(160.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color.Black, modifier = Modifier.size(140.dp))
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text("Code: MITRA-7829-9182", fontWeight = FontWeight.Bold, color = TealPrimary)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.addSecurityAuditEvent("New Device Linked", "Web Client paired successfully via QR.", true)
            showPairingDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Done")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPairingDialog = false }) { Text("Cancel") }
      }
    )
  }

  // Unlink Single Device Dialog
  deviceToUnlink?.let { device ->
    AlertDialog(
      onDismissRequest = { deviceToUnlink = null },
      title = { Text("Log out from ${device.name}?") },
      text = { Text("Are you sure you want to log out from this session? Any sync operations on this device will stop immediately.") },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.removeLinkedDevice(device.id)
            deviceToUnlink = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = CallRed)
        ) {
          Text("Log out")
        }
      },
      dismissButton = {
        TextButton(onClick = { deviceToUnlink = null }) { Text("Cancel") }
      }
    )
  }

  // Unlink All Devices Dialog
  if (showUnlinkAllDialog) {
    AlertDialog(
      onDismissRequest = { showUnlinkAllDialog = false },
      title = { Text("Log out from all other devices?", color = CallRed) },
      text = { Text("All remote browser and desktop sessions will be immediately revoked. You will remain logged in on this phone.") },
      confirmButton = {
        Button(
          onClick = {
            ChatMitraRepository.unlinkAllOtherDevices()
            showUnlinkAllDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CallRed)
        ) {
          Text("Log out All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showUnlinkAllDialog = false }) { Text("Cancel") }
      }
    )
  }
}
