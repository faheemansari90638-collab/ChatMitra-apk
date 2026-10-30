package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BackupDestination
import com.example.data.model.BackupRecord
import com.example.data.model.BackupSchedule
import com.example.data.model.BackupStatus
import com.example.data.repository.ChatBackupRepository
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBackupScreen(
  onBackClick: () -> Unit,
  onNavigateRestore: () -> Unit
) {
  val backupRepo = remember { ChatBackupRepository.getInstance() }
  val settings by backupRepo.backupSettings.collectAsState()
  val records by backupRepo.backupRecords.collectAsState()
  val status by backupRepo.backupStatus.collectAsState()
  val progress by backupRepo.backupProgress.collectAsState()
  val statusMessage by backupRepo.backupStatusMessage.collectAsState()

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var showScheduleDialog by remember { mutableStateOf(false) }
  var showDestinationDialog by remember { mutableStateOf(false) }
  var showEncryptedSetupDialog by remember { mutableStateOf(false) }
  var showDeleteAllConfirm by remember { mutableStateOf(false) }
  var recordToDelete by remember { mutableStateOf<BackupRecord?>(null) }

  // Encryption setup fields
  var backupPasswordInput by remember { mutableStateOf("") }
  var confirmPasswordInput by remember { mutableStateOf("") }
  var encryptionError by remember { mutableStateOf<String?>(null) }
  var generatedKeyDialogText by remember { mutableStateOf<String?>(null) }

  val isBackingUp = status == BackupStatus.PREPARING ||
    status == BackupStatus.ENCRYPTING ||
    status == BackupStatus.UPLOADING

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Chat Backup", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("chat_backup_back_button")) {
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Last Backup Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(TealPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (status == BackupStatus.COMPLETED) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                  contentDescription = null,
                  tint = TealPrimary,
                  modifier = Modifier.size(28.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Last Backup",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = settings.lastBackupFormatted,
                  style = MaterialTheme.typography.bodyMedium,
                  color = CheckmarkGrey
                )
                Text(
                  text = "Size: %.1f MB • %d messages".format(settings.totalBackupSizeBytes / 1_000_000.0, settings.totalMessagesCount),
                  style = MaterialTheme.typography.bodySmall,
                  color = TealPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            // Progress Bar if in progress
            if (isBackingUp) {
              Spacer(modifier = Modifier.height(14.dp))
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
                modifier = Modifier.padding(top = 6.dp)
              )
            } else if (status == BackupStatus.FAILED) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  backupRepo.performBackup(isManual = true, context = context)
                },
                enabled = !isBackingUp,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
                  .testTag("backup_now_button")
              ) {
                if (isBackingUp) {
                  CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Back Up Now", fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = onNavigateRestore,
                enabled = !isBackingUp,
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
                  .testTag("restore_backup_button")
              ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // End-to-End Encrypted Backup Section
      item {
        Text(
          text = "Security & Encryption",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          headlineContent = { Text("End-to-end encrypted backup", fontWeight = FontWeight.SemiBold) },
          supportingContent = {
            Text(
              if (settings.isEndToEndEncrypted)
                "On (Protected with private password & 64-digit recovery key)"
              else
                "Off (Standard cloud security with Google account authentication)",
              color = if (settings.isEndToEndEncrypted) CallGreen else CheckmarkGrey
            )
          },
          leadingContent = {
            Icon(
              imageVector = if (settings.isEndToEndEncrypted) Icons.Default.Shield else Icons.Default.Lock,
              contentDescription = null,
              tint = if (settings.isEndToEndEncrypted) CallGreen else TealPrimary
            )
          },
          trailingContent = {
            Switch(
              checked = settings.isEndToEndEncrypted,
              onCheckedChange = { enable ->
                if (enable) {
                  showEncryptedSetupDialog = true
                } else {
                  backupRepo.disableEndToEndEncryptedBackup()
                  scope.launch { snackbarHostState.showSnackbar("Encrypted backup turned off.") }
                }
              },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary),
              modifier = Modifier.testTag("encrypted_backup_switch")
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      if (settings.isEndToEndEncrypted && settings.recoveryKey64.isNotBlank()) {
        item {
          ListItem(
            headlineContent = { Text("64-digit recovery key", fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(settings.recoveryKey64, fontSize = 11.sp, color = CheckmarkGrey) },
            leadingContent = { Icon(Icons.Default.Key, contentDescription = null, tint = TealPrimary) }
          )
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
      }

      // Backup Settings Section
      item {
        Text(
          text = "Backup Settings",
          style = MaterialTheme.typography.labelLarge,
          color = TealPrimary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
        )
      }

      item {
        ListItem(
          modifier = Modifier
            .clickable { showScheduleDialog = true }
            .testTag("backup_schedule_row"),
          headlineContent = { Text("Auto-backup schedule", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.autoBackupSchedule.label, color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null, tint = TealPrimary) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          modifier = Modifier
            .clickable { showDestinationDialog = true }
            .testTag("backup_destination_row"),
          headlineContent = { Text("Backup destination", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text(settings.destination.label, color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Folder, contentDescription = null, tint = TealPrimary) }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Back up with media", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Include photos, voice notes, and documents in backup", color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Storage, contentDescription = null, tint = TealPrimary) },
          trailingContent = {
            Switch(
              checked = settings.backupWithMedia,
              onCheckedChange = { backupRepo.updateSettings(backupWithMedia = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Include videos in backup", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Videos require additional cloud storage quota", color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = settings.includeVideos,
              onCheckedChange = { backupRepo.updateSettings(includeVideos = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Back up over cellular data", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("If disabled, backups only run when connected to Wi-Fi", color = CheckmarkGrey) },
          trailingContent = {
            Switch(
              checked = settings.backupOverCellular,
              onCheckedChange = { backupRepo.updateSettings(backupOverCellular = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      item {
        ListItem(
          headlineContent = { Text("Email notification on backup", fontWeight = FontWeight.SemiBold) },
          supportingContent = { Text("Receive email confirmation with backup time & approximate size", color = CheckmarkGrey) },
          leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = TealPrimary) },
          trailingContent = {
            Switch(
              checked = settings.emailNotificationOnBackup,
              onCheckedChange = { backupRepo.updateSettings(emailNotification = it) },
              colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
            )
          }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }

      // Cloud Backup History Section
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Cloud Backup History",
            style = MaterialTheme.typography.labelLarge,
            color = TealPrimary,
            fontWeight = FontWeight.Bold
          )
          if (records.isNotEmpty()) {
            TextButton(onClick = { showDeleteAllConfirm = true }) {
              Text("Delete All", color = CallRed, fontSize = 12.sp)
            }
          }
        }
      }

      if (records.isEmpty()) {
        item {
          Text(
            text = "No saved backups yet. Tap 'Back Up Now' to create one.",
            style = MaterialTheme.typography.bodySmall,
            color = CheckmarkGrey,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
          )
        }
      } else {
        items(records, key = { it.id }) { record ->
          ListItem(
            headlineContent = {
              Text(
                text = record.timestampFormatted,
                fontWeight = FontWeight.SemiBold
              )
            },
            supportingContent = {
              Text(
                text = "${record.formattedSize} • ${record.messagesCount} msgs • ${if (record.isEncrypted) "🔒 Encrypted" else "Standard"}",
                color = CheckmarkGrey,
                style = MaterialTheme.typography.bodySmall
              )
            },
            leadingContent = {
              Icon(Icons.Default.CloudDone, contentDescription = null, tint = if (record.isEncrypted) CallGreen else TealPrimary)
            },
            trailingContent = {
              IconButton(onClick = { recordToDelete = record }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete backup", tint = CheckmarkGrey)
              }
            }
          )
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
      }

      item {
        Spacer(modifier = Modifier.height(36.dp))
      }
    }

    // Auto-Backup Schedule Dialog
    if (showScheduleDialog) {
      AlertDialog(
        onDismissRequest = { showScheduleDialog = false },
        title = { Text("Auto-backup schedule", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            BackupSchedule.values().forEach { schedule ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    backupRepo.updateSettings(schedule = schedule)
                    showScheduleDialog = false
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = settings.autoBackupSchedule == schedule,
                  onClick = null,
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(schedule.label, modifier = Modifier.padding(start = 12.dp))
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showScheduleDialog = false }) { Text("Done") }
        }
      )
    }

    // Backup Destination Dialog
    if (showDestinationDialog) {
      AlertDialog(
        onDismissRequest = { showDestinationDialog = false },
        title = { Text("Backup Destination", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            BackupDestination.values().forEach { dest ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    backupRepo.updateSettings(destination = dest)
                    showDestinationDialog = false
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = settings.destination == dest,
                  onClick = null,
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(dest.label, modifier = Modifier.padding(start = 12.dp))
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showDestinationDialog = false }) { Text("Done") }
        }
      )
    }

    // Setup End-to-End Encrypted Backup Dialog
    if (showEncryptedSetupDialog) {
      AlertDialog(
        onDismissRequest = { showEncryptedSetupDialog = false },
        title = { Text("Set Up Encrypted Backup", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text(
              text = "Create a password to protect your cloud backup. Neither ChatMitra nor Google can read your chats or reset this password if lost.",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
              value = backupPasswordInput,
              onValueChange = {
                backupPasswordInput = it
                encryptionError = null
              },
              label = { Text("Backup Password (min 6 chars)") },
              visualTransformation = PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = confirmPasswordInput,
              onValueChange = {
                confirmPasswordInput = it
                encryptionError = null
              },
              label = { Text("Confirm Backup Password") },
              visualTransformation = PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )

            encryptionError?.let { err ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (backupPasswordInput.length < 6) {
                encryptionError = "Password must be at least 6 characters."
                return@Button
              }
              if (backupPasswordInput != confirmPasswordInput) {
                encryptionError = "Passwords do not match."
                return@Button
              }

              val res = backupRepo.enableEndToEndEncryptedBackup(backupPasswordInput)
              res.fold(
                onSuccess = { key ->
                  showEncryptedSetupDialog = false
                  generatedKeyDialogText = key
                },
                onFailure = { ex ->
                  encryptionError = ex.message
                }
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Enable Encryption")
          }
        },
        dismissButton = {
          TextButton(onClick = { showEncryptedSetupDialog = false }) { Text("Cancel") }
        }
      )
    }

    // Generated 64-Digit Recovery Key Display Dialog
    generatedKeyDialogText?.let { key ->
      AlertDialog(
        onDismissRequest = { generatedKeyDialogText = null },
        title = { Text("Your 64-Digit Recovery Key", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text(
              text = "Save this key in a secure location. You can use it to restore your chats if you forget your backup password.",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = key,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TealPrimary,
                modifier = Modifier.padding(12.dp)
              )
            }
          }
        },
        confirmButton = {
          Button(
            onClick = { generatedKeyDialogText = null },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("I Saved My Recovery Key")
          }
        }
      )
    }

    // Delete Single Record Confirmation
    recordToDelete?.let { record ->
      AlertDialog(
        onDismissRequest = { recordToDelete = null },
        title = { Text("Delete this backup?") },
        text = { Text("This backup (${record.formattedSize} from ${record.timestampFormatted}) will be permanently deleted from your cloud storage.") },
        confirmButton = {
          TextButton(
            onClick = {
              backupRepo.deleteBackupRecord(record.id)
              recordToDelete = null
              scope.launch { snackbarHostState.showSnackbar("Backup deleted.") }
            }
          ) {
            Text("Delete", color = CallRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { recordToDelete = null }) { Text("Cancel") }
        }
      )
    }

    // Delete All Backups Confirmation
    if (showDeleteAllConfirm) {
      AlertDialog(
        onDismissRequest = { showDeleteAllConfirm = false },
        title = { Text("Delete all cloud backups?") },
        text = { Text("All backup records and message archives stored in ${settings.destination.label} will be deleted. You won't be able to restore them later.") },
        confirmButton = {
          TextButton(
            onClick = {
              backupRepo.deleteAllCloudBackups()
              showDeleteAllConfirm = false
              scope.launch { snackbarHostState.showSnackbar("All cloud backups wiped.") }
            }
          ) {
            Text("Delete All", color = CallRed, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancel") }
        }
      )
    }
  }
}
