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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.EmailAccountRepository
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailAddressScreen(
  onBackClick: () -> Unit
) {
  val emailRepo = remember { EmailAccountRepository.getInstance() }
  val emailState by emailRepo.emailState.collectAsState()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var isEditing by remember { mutableStateOf(false) }
  var newEmailInput by remember { mutableStateOf("") }
  var accountPinInput by remember { mutableStateOf("") }
  var otpInput by remember { mutableStateOf("") }
  var isAwaitingOtp by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var infoMessage by remember { mutableStateOf<String?>(null) }
  var cooldownSeconds by remember { mutableIntStateOf(0) }

  // Cooldown timer ticker
  LaunchedEffect(emailState.otpCooldownUntil) {
    while (true) {
      val remaining = ((emailState.otpCooldownUntil - System.currentTimeMillis()) / 1000).toInt()
      cooldownSeconds = remaining.coerceAtLeast(0)
      if (cooldownSeconds <= 0) break
      delay(1000)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Email Address", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick, modifier = Modifier.testTag("email_back_button")) {
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
      // Header Icon
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(TealPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Email,
          contentDescription = null,
          tint = TealPrimary,
          modifier = Modifier.size(42.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Account Email Linking",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "Your linked email secures account recovery and delivers instant security alerts for cloud backups and new device sessions.",
        style = MaterialTheme.typography.bodyMedium,
        color = CheckmarkGrey,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
      )

      // Verified Status Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("current_email_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Linked Email",
              style = MaterialTheme.typography.labelMedium,
              color = CheckmarkGrey
            )
            if (emailState.isVerified) {
              Surface(
                color = CallGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = CallGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Verified",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CallGreen
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = emailState.email.ifBlank { "No email linked" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          if (emailState.isVerified) {
            Text(
              text = "Last verified: ${emailState.lastVerificationTimeFormatted}",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Security Notice Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary)
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Email alone cannot bypass Two-Step Verification PIN. Verification codes expire after 10 minutes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Area: Change or Verify Email
      if (!isEditing && !isAwaitingOtp) {
        Button(
          onClick = {
            isEditing = true
            errorMessage = null
            newEmailInput = ""
            accountPinInput = ""
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("change_email_button"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Icon(Icons.Default.MarkEmailRead, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Change Linked Email Address", fontWeight = FontWeight.SemiBold)
        }
      } else if (isEditing && !isAwaitingOtp) {
        // Step 1: Input new email & reauthenticate with Account PIN
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Step 1: Enter New Email & Confirm PIN",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = TealPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = newEmailInput,
              onValueChange = {
                newEmailInput = it
                errorMessage = null
              },
              label = { Text("New Email Address") },
              placeholder = { Text("example@domain.com") },
              leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = accountPinInput,
              onValueChange = {
                accountPinInput = it
                errorMessage = null
              },
              label = { Text("Account PIN / Passcode") },
              placeholder = { Text("Enter your 4-digit PIN") },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
              visualTransformation = PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("reauth_pin_input")
            )

            errorMessage?.let { err ->
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { isEditing = false },
                modifier = Modifier.weight(1f)
              ) {
                Text("Cancel")
              }

              Button(
                onClick = {
                  if (newEmailInput.isBlank()) {
                    errorMessage = "Please enter your new email address."
                    return@Button
                  }
                  if (accountPinInput.isBlank()) {
                    errorMessage = "Please enter your Account PIN to reauthenticate."
                    return@Button
                  }

                  val result = emailRepo.changeEmailWithReauth(newEmailInput, accountPinInput, context)
                  result.fold(
                    onSuccess = { generatedOtp ->
                      isAwaitingOtp = true
                      infoMessage = "Verification code generated for $newEmailInput: $generatedOtp"
                      scope.launch {
                        snackbarHostState.showSnackbar("OTP Sent to $newEmailInput (Code: $generatedOtp)")
                      }
                    },
                    onFailure = { ex ->
                      errorMessage = ex.message ?: "Authentication error"
                    }
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("send_email_otp_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                enabled = cooldownSeconds <= 0
              ) {
                Text(if (cooldownSeconds > 0) "Wait (${cooldownSeconds}s)" else "Send OTP")
              }
            }
          }
        }
      } else if (isAwaitingOtp) {
        // Step 2: Enter 6-digit OTP
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Step 2: Enter 6-Digit OTP",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = TealPrimary
            )

            Text(
              text = "We sent a 6-digit verification code to ${emailState.pendingVerificationEmail ?: newEmailInput}.",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            OutlinedTextField(
              value = otpInput,
              onValueChange = {
                if (it.length <= 6) {
                  otpInput = it
                  errorMessage = null
                }
              },
              label = { Text("6-Digit Verification Code") },
              placeholder = { Text("e.g. 481920") },
              leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("email_otp_input")
            )

            errorMessage?.let { err ->
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            infoMessage?.let { info ->
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = info, color = TealPrimary, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = {
                if (otpInput.length != 6) {
                  errorMessage = "Please enter the complete 6-digit verification code."
                  return@Button
                }

                val verifyResult = emailRepo.verifyEmailOtp(otpInput, context)
                verifyResult.fold(
                  onSuccess = {
                    isAwaitingOtp = false
                    isEditing = false
                    otpInput = ""
                    scope.launch {
                      snackbarHostState.showSnackbar("Email linked and verified successfully!")
                    }
                  },
                  onFailure = { ex ->
                    errorMessage = ex.message ?: "Verification failed."
                  }
                )
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_email_otp_button"),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Verify & Link Email", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(
                onClick = {
                  isAwaitingOtp = false
                  isEditing = true
                }
              ) {
                Text("Change Email")
              }

              TextButton(
                onClick = {
                  val target = emailState.pendingVerificationEmail ?: newEmailInput
                  val res = emailRepo.requestEmailVerification(target, context)
                  res.fold(
                    onSuccess = { newOtp ->
                      infoMessage = "New OTP sent: $newOtp"
                      scope.launch {
                        snackbarHostState.showSnackbar("New verification OTP sent: $newOtp")
                      }
                    },
                    onFailure = { ex ->
                      errorMessage = ex.message
                    }
                  )
                },
                enabled = cooldownSeconds <= 0
              ) {
                Text(if (cooldownSeconds > 0) "Resend in ${cooldownSeconds}s" else "Resend Code")
              }
            }
          }
        }
      }
    }
  }
}
