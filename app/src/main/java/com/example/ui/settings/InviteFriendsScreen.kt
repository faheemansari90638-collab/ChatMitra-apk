package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendsScreen(
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val officialDownloadUrl = "https://chatmitra.app/download"
  val shareMessageText = """
    Hey! Join me on ChatMitra — a modern, secure, and fast instant messaging app with HD audio/video calls, Hamza AI assistance, and end-to-end encryption.
    
    Download here: $officialDownloadUrl
  """.trimIndent()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Invite Friends", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("invite_friends_back_button")) {
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
        .padding(horizontal = 20.dp)
        .testTag("invite_friends_screen"),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        Spacer(modifier = Modifier.height(16.dp))

        // App Logo & Identity Header
        Box(
          modifier = Modifier
            .size(80.dp)
            .background(TealPrimary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.chatmitra_logo),
            contentDescription = "ChatMitra Logo",
            modifier = Modifier.size(54.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "ChatMitra",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Version 1.0.0 • Supports Android 7.0+ (API 24+)",
          fontSize = 12.sp,
          color = CheckmarkGrey
        )

        Spacer(modifier = Modifier.height(24.dp))
      }

      // QR Code Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth().testTag("qr_invite_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Color.White,
              modifier = Modifier
                .size(180.dp)
                .border(2.dp, TealPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
              shadowElevation = 4.dp
            ) {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.QrCode2,
                  contentDescription = "Scan to Download",
                  tint = Color(0xFF0F172A),
                  modifier = Modifier.size(150.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "Scan to download ChatMitra",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )

            Text(
              text = "Friends can scan this with any phone camera to get the verified installer",
              fontSize = 12.sp,
              color = CheckmarkGrey,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Share Link Action Buttons
      item {
        Button(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, shareMessageText)
              type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share ChatMitra via")
            context.startActivity(shareIntent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("share_app_button")
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Share ChatMitra App", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ChatMitra Download Link", officialDownloadUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Official download link copied!", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("copy_download_link_button")
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Copy Download Link", fontWeight = FontWeight.SemiBold, color = TealPrimary, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Security & Google Play Publication Note
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Official Distribution Channels",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = TealPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "ChatMitra distribution links always use HTTPS encryption. Release packages are cryptographically signed to guarantee authenticity and prevent tampering.",
              fontSize = 11.sp,
              color = CheckmarkGrey,
              lineHeight = 16.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}
