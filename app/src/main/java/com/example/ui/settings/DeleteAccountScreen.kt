package com.example.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AccountManagementRepository
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
  onBackClick: () -> Unit,
  onAccountDeleted: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val currentUser by ChatMitraRepository.currentUser.collectAsState()

  var phoneInput by remember { mutableStateOf("") }
  var selectedReason by remember { mutableStateOf("I am changing my device") }
  var showReasonDropdown by remember { mutableStateOf(false) }
  var feedbackInput by remember { mutableStateOf("") }
  var showConfirmDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isDeleting by remember { mutableStateOf(false) }

  val reasons = listOf(
    "I am changing my device",
    "I am changing my phone number",
    "I am deleting my account temporarily",
    "ChatMitra is missing a feature",
    "ChatMitra is not working properly",
    "Other"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Delete My Account", fontWeight = FontWeight.Bold, color = Color.White)
        },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("delete_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB71C1C))
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Warning Card
      Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            Icons.Default.Warning,
            contentDescription = "Warning",
            tint = Color(0xFFC62828),
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Deleting your account is permanent:",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFB71C1C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "• Your profile info and photo will be deleted.\n" +
                  "• You will be removed from all ChatMitra groups.\n" +
                  "• Message history on this device and cloud backups will be wiped.\n" +
                  "• All active linked device sessions will be immediately revoked.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF424242),
              lineHeight = 20.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Confirm Registered Phone Number",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Enter your phone number (${currentUser?.phone ?: "registered number"}) to confirm ownership before proceeding.",
        style = MaterialTheme.typography.bodySmall,
        color = CheckmarkGrey,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
      )

      OutlinedTextField(
        value = phoneInput,
        onValueChange = {
          phoneInput = it
          errorMessage = null
        },
        label = { Text("Phone Number") },
        placeholder = { Text(currentUser?.phone ?: "+91 98765 43210") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        isError = errorMessage != null,
        supportingText = {
          if (errorMessage != null) {
            Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
          }
        },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("delete_phone_input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Reason for Leaving (Optional)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))

      Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
          onClick = { showReasonDropdown = true },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("delete_reason_dropdown_btn")
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = selectedReason,
              modifier = Modifier.weight(1f),
              color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select reason")
          }
        }

        DropdownMenu(
          expanded = showReasonDropdown,
          onDismissRequest = { showReasonDropdown = false }
        ) {
          reasons.forEach { reason ->
            DropdownMenuItem(
              text = { Text(reason) },
              onClick = {
                selectedReason = reason
                showReasonDropdown = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = feedbackInput,
        onValueChange = { feedbackInput = it },
        label = { Text("How can we improve? (Optional)") },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = {
          if (phoneInput.isBlank()) {
            errorMessage = "Please enter your registered phone number."
            return@Button
          }
          showConfirmDialog = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        shape = RoundedCornerShape(10.dp),
        enabled = !isDeleting,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("delete_account_confirm_button")
      ) {
        Text("Delete My Account", fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Final Confirmation Dialog
  if (showConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showConfirmDialog = false },
      icon = {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828))
      },
      title = { Text("Delete Account Permanently?", fontWeight = FontWeight.Bold) },
      text = {
        Text("This action is immediate and cannot be undone. You will lose all your chats, media, and cloud backups forever.")
      },
      confirmButton = {
        Button(
          onClick = {
            showConfirmDialog = false
            isDeleting = true
            val result = AccountManagementRepository.getInstance().deleteAccount(
              confirmedPhone = phoneInput,
              reason = selectedReason,
              context = context
            )
            isDeleting = false
            if (result.isSuccess) {
              onAccountDeleted()
            } else {
              errorMessage = result.exceptionOrNull()?.message ?: "Account deletion failed."
              scope.launch {
                snackbarHostState.showSnackbar(errorMessage ?: "Failed to delete account.")
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
          modifier = Modifier.testTag("delete_account_final_confirm")
        ) {
          Text("Delete Forever", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
