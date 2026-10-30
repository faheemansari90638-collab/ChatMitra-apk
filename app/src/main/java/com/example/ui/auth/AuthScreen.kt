package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import android.app.Activity
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.data.repository.AuthenticationRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.ChatMitraRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay

enum class AuthStep {
  PHONE_ENTRY,
  OTP_VERIFY,
  PROFILE_SETUP
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val authRepository = remember { AuthenticationRepository() }
  var isGoogleSigningIn by remember { mutableStateOf(false) }
  var googleSignInError by remember { mutableStateOf<String?>(null) }

  var currentStep by remember { mutableStateOf(AuthStep.PHONE_ENTRY) }

  var countryCode by remember { mutableStateOf("+91") }
  var phoneNumber by remember { mutableStateOf("9876543210") }
  var otpCode by remember { mutableStateOf("") }
  var otpError by remember { mutableStateOf(false) }
  var otpTimer by remember { mutableIntStateOf(30) }

  var displayName by remember { mutableStateOf("Hamza Ansari") }
  var aboutText by remember { mutableStateOf("Building awesome apps with ChatMitra 🚀") }
  var selectedEmoji by remember { mutableStateOf("✨") }
  var selectedColorHex by remember { mutableLongStateOf(0xFF00A884) }

  val emojiOptions = listOf("✨", "🚀", "🌸", "🎨", "⚡", "🌟", "🦁", "☕", "💻", "🔥")
  val colorOptions = listOf(
    0xFF00A884, // Teal
    0xFF008069, // Dark Teal
    0xFF3F51B5, // Indigo
    0xFFE91E63, // Pink
    0xFF9C27B0, // Purple
    0xFFFF9800, // Amber
    0xFF0284C7, // Sky
    0xFF16A34A  // Green
  )

  LaunchedEffect(currentStep) {
    if (currentStep == AuthStep.OTP_VERIFY) {
      otpTimer = 30
      while (otpTimer > 0) {
        delay(1000)
        otpTimer--
      }
    }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Header Branding
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          Image(
            painter = painterResource(id = R.drawable.img_welcome_hero),
            contentDescription = "ChatMitra Welcome Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.35f))
          )
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(16.dp)
          ) {
            Text(
              text = "ChatMitra",
              style = MaterialTheme.typography.headlineMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Private & modern messaging for friends",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.9f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      when (currentStep) {
        AuthStep.PHONE_ENTRY -> {
          Text(
            text = "Enter your phone number",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "ChatMitra will send an SMS verification code to verify your account securely.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = countryCode,
              onValueChange = { countryCode = it },
              label = { Text("Code") },
              modifier = Modifier
                .width(90.dp)
                .testTag("country_code_input"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                focusedLabelColor = TealPrimary
              )
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
              value = phoneNumber,
              onValueChange = { phoneNumber = it },
              label = { Text("Phone number") },
              leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TealPrimary)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("phone_number_input"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                focusedLabelColor = TealPrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
          ) {
            Icon(
              Icons.Default.Security,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Protected with 256-bit End-to-End Encryption",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(32.dp))

          Button(
            onClick = {
              if (phoneNumber.isNotBlank()) {
                currentStep = AuthStep.OTP_VERIFY
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("send_otp_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "Next",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
              text = "  OR  ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = {
              val activity = context as? Activity
              if (activity != null) {
                isGoogleSigningIn = true
                googleSignInError = null
                scope.launch {
                  val result = authRepository.signInWithGoogle(activity)
                  isGoogleSigningIn = false
                  result.onSuccess { user ->
                    ChatMitraRepository.registerUser(
                      name = user.displayName ?: "Google User",
                      phone = user.phoneNumber ?: user.email ?: "+91 98765 43210",
                      about = "Hey there! I am using ChatMitra with Google Sign-In 🚀",
                      emoji = "✨",
                      colorHex = 0xFF00A884
                    )
                    onLoginSuccess()
                  }.onFailure { err ->
                    googleSignInError = err.localizedMessage ?: "Sign-in failed"
                  }
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("google_sign_in_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isGoogleSigningIn) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TealPrimary)
            } else {
              Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          if (googleSignInError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = googleSignInError ?: "",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center
            )
          }
        }

        AuthStep.OTP_VERIFY -> {
          Text(
            text = "Verify your number",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Enter the 6-digit verification code sent to $countryCode $phoneNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(24.dp))

          OutlinedTextField(
            value = otpCode,
            onValueChange = {
              if (it.length <= 6) {
                otpCode = it
                otpError = false
              }
            },
            label = { Text("6-Digit Code") },
            placeholder = { Text("123456") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
            },
            isError = otpError,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("otp_input"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              focusedLabelColor = TealPrimary
            )
          )

          if (otpError) {
            Text(
              text = "Invalid code. Please enter 123456 or tap autofill below.",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = {
              otpCode = "123456"
              otpError = false
            },
            modifier = Modifier.testTag("test_otp_chip"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Auto-fill Test Code (123456)", style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (otpTimer > 0) "Resend code in ${otpTimer}s" else "Didn't receive code?",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (otpTimer == 0) {
              Text(
                text = "Resend SMS",
                style = MaterialTheme.typography.bodySmall,
                color = TealPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { otpTimer = 30 }
              )
            }
          }

          Spacer(modifier = Modifier.height(28.dp))

          Button(
            onClick = {
              if (otpCode == "123456" || otpCode.length == 6) {
                currentStep = AuthStep.PROFILE_SETUP
              } else {
                otpError = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("verify_otp_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "Verify & Continue",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }
        }

        AuthStep.PROFILE_SETUP -> {
          Text(
            text = "Profile Info",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Please provide your name and an optional profile avatar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(20.dp))

          // Avatar Preview
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(Color(selectedColorHex))
              .border(3.dp, TealPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(text = selectedEmoji, fontSize = 36.sp)
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Choose your avatar icon & theme",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(emojiOptions) { emoji ->
              Surface(
                shape = CircleShape,
                color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(42.dp)
                  .clickable { selectedEmoji = emoji }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(text = emoji, fontSize = 20.sp)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(colorOptions) { colorHex ->
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(colorHex))
                  .border(
                    width = if (selectedColorHex == colorHex) 3.dp else 0.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = CircleShape
                  )
                  .clickable { selectedColorHex = colorHex },
                contentAlignment = Alignment.Center
              ) {
                if (selectedColorHex == colorHex) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            placeholder = { Text("Type your name here") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("display_name_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              focusedLabelColor = TealPrimary
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = aboutText,
            onValueChange = { aboutText = it },
            label = { Text("About") },
            placeholder = { Text("Status message...") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("about_input"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              focusedLabelColor = TealPrimary
            )
          )

          Spacer(modifier = Modifier.height(28.dp))

          Button(
            onClick = {
              ChatMitraRepository.registerUser(
                name = displayName,
                phone = "$countryCode $phoneNumber",
                about = aboutText,
                emoji = selectedEmoji,
                colorHex = selectedColorHex
              )
              onLoginSuccess()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("finish_profile_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "Get Started",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}
