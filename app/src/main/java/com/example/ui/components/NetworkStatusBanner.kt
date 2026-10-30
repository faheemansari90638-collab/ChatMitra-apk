package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.NetworkReliabilityManager
import com.example.ui.theme.AmberAccent
import kotlinx.coroutines.launch

@Composable
fun NetworkStatusBanner(
  modifier: Modifier = Modifier
) {
  val isOnline by NetworkReliabilityManager.isOnline.collectAsState()
  val queuedMessages by NetworkReliabilityManager.queuedMessages.collectAsState()
  val scope = rememberCoroutineScope()

  val showBanner = !isOnline || queuedMessages.isNotEmpty()

  AnimatedVisibility(
    visible = showBanner,
    enter = expandVertically(),
    exit = shrinkVertically(),
    modifier = modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(if (!isOnline) Color(0xFFD32F2F) else AmberAccent)
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .testTag("network_status_banner")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = if (!isOnline) Icons.Default.CloudOff else Icons.Default.Sync,
            contentDescription = "Connection Status",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (!isOnline) {
              if (queuedMessages.isNotEmpty()) {
                "Offline • ${queuedMessages.size} message(s) queued"
              } else {
                "Waiting for connection..."
              }
            } else {
              "Online • Syncing ${queuedMessages.size} pending message(s)..."
            },
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        if (isOnline && queuedMessages.isNotEmpty()) {
          IconButton(
            onClick = {
              scope.launch {
                NetworkReliabilityManager.flushRetryQueue()
              }
            },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Retry Sync",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
