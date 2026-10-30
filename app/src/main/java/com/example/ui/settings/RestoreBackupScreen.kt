package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RestoreStatus
import com.example.data.repository.ChatBackupRepository
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupScreen(
  onBackClick: () -> Unit
) {
  val backupRepo = remember { ChatBackupRepository.getInstance() }
  val records by backupRepo.backupRecords.collectAsState()
  val restoreStatus by backupRepo.restoreStatus.collectAsState()
  val progress by backupRepo.restoreProgress.collectAsState()
  val statusMessage by backupRepo.restoreStatusMessage.collectAsState()
  val settings by backupRepo.backupSettings.collectAsState()
  val context = LocalContext.current

  val latestRecord = records.firstOrNull()

  var passwordOrKeyInput by remember { mutableStateOf("") }
  var restoreErrorMessage by remember { mutableStateOf<String?>(null) }
  var restoredCount by remember { mutableStateOf<Int?>(null) }

  val isRestoring = restoreStatus == RestoreStatus.DOWNLOADING ||
    restoreStatus == RestoreStatus.DECRYPTING ||
    restoreStatus == RestoreStatus.MERGING

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Restore Backup", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("restore_back_button")) {
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (latestRecord == null) {
        // No backup available
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(CheckmarkGrey.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CheckmarkGrey, modifier = Modifier.size(38.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "No Backups Found",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "There are no cloud or local backups found for your account. Perform a backup from Chat Backup settings first.",
          style = MaterialTheme.typography.bodyMedium,
          color = CheckmarkGrey,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
          onClick = onBackClick,
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Go Back")
        }
      } else if (restoreStatus == RestoreStatus.COMPLETED) {
        // Success Screen
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(CallGreen.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CallGreen, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Restore Completed!",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Successfully merged ${restoredCount ?: latestRecord.messagesCount} messages and attachments into your conversations.",
          style = MaterialTheme.typography.bodyMedium,
          color = CheckmarkGrey,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
          onClick = onBackClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("restore_done_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Return to Chats", fontWeight = FontWeight.Bold)
        }
      } else {
        // Backup Available Card
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(TealPrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(38.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Restore Chat History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Restore your messages and media from your cloud archive. Existing messages on your phone will be preserved.",
          style = MaterialTheme.typography.bodyMedium,
          color = CheckmarkGrey,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

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
              Text("Found Backup", fontWeight = FontWeight.Bold, color = TealPrimary)
              if (latestRecord.isEncrypted) {
                Surface(
                  color = CallGreen.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = CallGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Encrypted", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CallGreen)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Date: ${latestRecord.timestampFormatted}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Total Size: ${latestRecord.formattedSize}", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
            Text("Messages: ${latestRecord.messagesCount} • Files: ${latestRecord.mediaFilesCount}", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
            Text("Storage: ${latestRecord.destination.label}", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey)
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // If encrypted, prompt for password or recovery key
        if (latestRecord.isEncrypted) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Enter Backup Password or 64-Digit Key",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "This backup is end-to-end encrypted. Provide the password you created when setting up backup encryption.",
                style = MaterialTheme.typography.bodySmall,
                color = CheckmarkGrey,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
              )

              OutlinedTextField(
                value = passwordOrKeyInput,
                onValueChange = {
                  passwordOrKeyInput = it
                  restoreErrorMessage = null
                },
                label = { Text("Password or Recovery Key") },
                placeholder = { Text("Enter password or 64-char key") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("restore_password_input")
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Progress or Error state
        if (isRestoring) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = TealPrimary
          )
          Text(
            text = "$statusMessage (${(progress * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = CheckmarkGrey,
            modifier = Modifier.padding(top = 8.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
        } else if (restoreStatus == RestoreStatus.FAILED) {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = restoreErrorMessage ?: statusMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
          onClick = {
            restoreErrorMessage = null
            backupRepo.performRestore(
              backupId = latestRecord.id,
              passwordOrKey = passwordOrKeyInput,
              context = context,
              onComplete = { result ->
                result.fold(
                  onSuccess = { count ->
                    restoredCount = count
                  },
                  onFailure = { ex ->
                    restoreErrorMessage = ex.message ?: "Restore failed"
                  }
                )
              }
            )
          },
          enabled = !isRestoring,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("start_restore_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          if (isRestoring) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restoring...", fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.CloudDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (restoreStatus == RestoreStatus.FAILED) "Retry Restore" else "Restore Messages", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
