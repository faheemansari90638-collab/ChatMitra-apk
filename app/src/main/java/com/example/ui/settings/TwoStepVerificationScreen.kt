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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoStepVerificationScreen(
  onBackClick: () -> Unit
) {
  val settings by ChatMitraRepository.privacySettings.collectAsState()
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var isEditing by remember { mutableStateOf(!settings.twoStepVerificationEnabled) }
  var pin by remember { mutableStateOf("") }
  var confirmPin by remember { mutableStateOf("") }
  var recoveryEmail by remember { mutableStateOf(settings.recoveryEmail) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Two-Step Verification", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("two_step_back_button")) {
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
          .size(80.dp)
          .clip(CircleShape)
          .background(if (settings.twoStepVerificationEnabled) TealPrimary.copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          if (settings.twoStepVerificationEnabled) Icons.Default.CheckCircle else Icons.Default.Security,
          contentDescription = null,
          tint = if (settings.twoStepVerificationEnabled) TealPrimary else Color(0xFFFF9800),
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = if (settings.twoStepVerificationEnabled && !isEditing) "Two-Step Verification is ON" else "Set Up Two-Step Verification",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "For added security, two-step verification requires a 6-digit PIN when registering your phone number with ChatMitra again, plus recovery email protection.",
        style = MaterialTheme.typography.bodyMedium,
        color = CheckmarkGrey,
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      if (settings.twoStepVerificationEnabled && !isEditing) {
        // Status Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Pin, contentDescription = null, tint = TealPrimary)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text("Verification PIN", fontWeight = FontWeight.SemiBold)
                Text("••••••", color = CheckmarkGrey, letterSpacing = 4.sp)
              }
            }

            if (settings.recoveryEmail.isNotBlank()) {
              Spacer(modifier = Modifier.height(16.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Recovery Email", fontWeight = FontWeight.SemiBold)
                  Text(settings.recoveryEmail, color = CheckmarkGrey, style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
          onClick = {
            isEditing = true
            pin = ""
            confirmPin = ""
            recoveryEmail = settings.recoveryEmail
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("change_pin_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Change PIN or Email", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = {
            ChatMitraRepository.disableTwoStepVerification()
            scope.launch {
              snackbarHostState.showSnackbar("Two-step verification has been turned off.")
            }
            isEditing = true
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("disable_2fa_button"),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Turn Off Two-Step Verification")
        }
      } else {
        // Setup / Edit Form
        OutlinedTextField(
          value = pin,
          onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
              pin = it
              errorMessage = null
            }
          },
          label = { Text("6-Digit PIN") },
          placeholder = { Text("Enter 6 numbers") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("two_step_pin_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = confirmPin,
          onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
              confirmPin = it
              errorMessage = null
            }
          },
          label = { Text("Confirm 6-Digit PIN") },
          placeholder = { Text("Re-enter your PIN") },
          leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("two_step_confirm_pin_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = recoveryEmail,
          onValueChange = { recoveryEmail = it },
          label = { Text("Recovery Email (Optional)") },
          placeholder = { Text("name@example.com") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("two_step_email_input")
        )

        errorMessage?.let { error ->
          Spacer(modifier = Modifier.height(10.dp))
          Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
          onClick = {
            when {
              pin.length != 6 -> {
                errorMessage = "PIN must be exactly 6 digits."
              }
              pin != confirmPin -> {
                errorMessage = "PINs do not match. Please re-enter."
              }
              else -> {
                ChatMitraRepository.enableTwoStepVerification(pin, recoveryEmail.trim())
                isEditing = false
                scope.launch {
                  snackbarHostState.showSnackbar("Two-step verification enabled successfully!")
                }
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("two_step_save_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text(if (settings.twoStepVerificationEnabled) "Update PIN & Settings" else "Enable Two-Step Verification", fontWeight = FontWeight.Bold)
        }

        if (settings.twoStepVerificationEnabled) {
          Spacer(modifier = Modifier.height(12.dp))
          TextButton(
            onClick = { isEditing = false },
            modifier = Modifier.testTag("two_step_cancel_edit_button")
          ) {
            Text("Cancel", color = TealPrimary)
          }
        }
      }
    }
  }
}
