package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageStatus
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkBlue
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@Composable
fun UserAvatar(
  name: String,
  colorHex: Long,
  emoji: String,
  size: Dp = 48.dp,
  fontSize: Int = 22,
  isOnline: Boolean = false,
  hasStatusRing: Boolean = false,
  isStatusViewed: Boolean = false,
  onClick: (() -> Unit)? = null
) {
  val ringModifier = if (hasStatusRing) {
    Modifier
      .size(size + 6.dp)
      .border(
        width = 2.5.dp,
        color = if (isStatusViewed) CheckmarkGrey else TealPrimary,
        shape = CircleShape
      )
      .padding(3.dp)
  } else {
    Modifier.size(size)
  }

  Box(
    modifier = ringModifier.then(
      if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(colorHex),
              Color(colorHex).copy(alpha = 0.8f)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = emoji,
        fontSize = fontSize.sp,
        textAlign = TextAlign.Center
      )
    }

    if (isOnline) {
      Box(
        modifier = Modifier
          .size(size / 3.5f)
          .align(Alignment.BottomEnd)
          .offset(x = 1.dp, y = 1.dp)
          .clip(CircleShape)
          .background(Color(0xFF22C55E))
          .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
      )
    }
  }
}

@Composable
fun MessageReceiptIndicator(
  status: MessageStatus,
  isRead: Boolean = false,
  readAt: Long? = null,
  modifier: Modifier = Modifier
) {
  val effectiveStatus = if (isRead) MessageStatus.READ else status
  when (effectiveStatus) {
    MessageStatus.SENDING -> {
      Icon(
        imageVector = Icons.Default.AccessTime,
        contentDescription = "Sending",
        tint = CheckmarkGrey,
        modifier = modifier.size(14.dp)
      )
    }
    MessageStatus.SENT -> {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Sent",
        tint = CheckmarkGrey,
        modifier = modifier.size(15.dp)
      )
    }
    MessageStatus.DELIVERED -> {
      Icon(
        imageVector = Icons.Default.DoneAll,
        contentDescription = "Delivered",
        tint = CheckmarkGrey,
        modifier = modifier.size(16.dp)
      )
    }
    MessageStatus.READ -> {
      Icon(
        imageVector = Icons.Default.DoneAll,
        contentDescription = if (readAt != null) "Read at ${java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(readAt))}" else "Read (Double Blue Checkmarks)",
        tint = CheckmarkBlue,
        modifier = modifier.size(16.dp)
      )
    }
  }
}

@Composable
fun E2EEBanner(onClickVerify: () -> Unit, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 8.dp)
      .clickable { onClickVerify() },
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Encrypted",
        tint = AmberAccent,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Messages and calls are end-to-end encrypted. No one outside of this chat can read or listen to them. Tap to verify.",
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 15.sp,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun SafetyNumberDialog(
  contactName: String,
  fingerprint: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = "Security Shield",
        tint = TealPrimary,
        modifier = Modifier.size(36.dp)
      )
    },
    title = {
      Text(
        text = "Verify Security Code",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "To verify end-to-end encryption with $contactName, compare these 60 numbers or scan the QR code on their device.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Visual QR Representation
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .size(140.dp)
            .border(2.dp, TealPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.QrCode,
              contentDescription = "Safety QR Code",
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(110.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = fingerprint,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "✓ 256-bit AES & RSA encryption keys verified",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF16A34A),
          fontWeight = FontWeight.Medium
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Done")
      }
    }
  )
}
