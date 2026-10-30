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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AccountManagementRepository
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataScreen(
  onBackClick: () -> Unit
) {
  val accountRepo = remember { AccountManagementRepository.getInstance() }
  val exportRequest by accountRepo.exportRequest.collectAsState()
  val context = LocalContext.current
  val clipboard: ClipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Export My Data", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("export_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(CircleShape)
          .background(TealPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.FolderZip, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(38.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Request Account Information",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "Create a downloadable ZIP report of your ChatMitra account information, profile settings, contacts, and message records.",
        style = MaterialTheme.typography.bodyMedium,
        color = CheckmarkGrey,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
      )

      // Report Status Card
      if (exportRequest != null) {
        val req = exportRequest!!
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Your Report is Ready", fontWeight = FontWeight.Bold, color = CallGreen)
              Surface(color = CallGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text(req.fileSizeFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CallGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Requested: ${req.requestedAtFormatted}", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
            Text("Expires on: ${req.expiresAtFormatted}", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = req.downloadUrl,
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
                  modifier = Modifier.weight(1f),
                  color = TealPrimary
                )
                IconButton(
                  onClick = {
                    clipboard.setText(AnnotatedString(req.downloadUrl))
                    scope.launch { snackbarHostState.showSnackbar("Download link copied to clipboard!") }
                  }
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = TealPrimary)
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                scope.launch { snackbarHostState.showSnackbar("Downloading ${req.fileSizeFormatted} archive...") }
              },
              modifier = Modifier.fillMaxWidth().height(46.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Icon(Icons.Default.Download, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Download ZIP Archive", fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Information Card: What is included
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("What's included in this archive:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(10.dp))

          ExportItemBullet("Profile details (display name, registered phone, verified email, about)")
          ExportItemBullet("Account settings and privacy configurations")
          ExportItemBullet("Contact lists and group participation data")
          ExportItemBullet("Message timestamps, receipts, and text records")
          ExportItemBullet("Security audit events and linked device history")

          HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

          Text("What cannot be exported:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
          Spacer(modifier = Modifier.height(10.dp))

          ExportItemBullet("Messages deleted before the export was requested")
          ExportItemBullet("Ephemeral WebRTC peer connection session tokens")
          ExportItemBullet("Private end-to-end encryption keys (for your security)")
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      if (exportRequest == null) {
        Button(
          onClick = {
            accountRepo.requestDataExport(context)
            scope.launch { snackbarHostState.showSnackbar("Archive generated and ready for download!") }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("request_export_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Icon(Icons.Default.FolderZip, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Request Data Archive (ZIP)", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun ExportItemBullet(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text("•", color = TealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
