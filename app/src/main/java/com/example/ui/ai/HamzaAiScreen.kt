package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HamzaAiMessage
import com.example.data.repository.HamzaAiRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HamzaAiScreen(
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val messages by HamzaAiRepository.messages.collectAsState()
  val isGenerating by HamzaAiRepository.isGenerating.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var showPrivacyInfoDialog by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .background(TealPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Hamza AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text(
                text = if (isGenerating) "Generating response..." else "Multilingual Assistant",
                fontSize = 11.sp,
                color = if (isGenerating) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("hamza_ai_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showPrivacyInfoDialog = true }, modifier = Modifier.testTag("ai_privacy_info_button")) {
            Icon(Icons.Default.Shield, contentDescription = "Privacy Policy", tint = TealPrimary)
          }
          IconButton(onClick = { showClearConfirmDialog = true }, modifier = Modifier.testTag("ai_clear_history_button")) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("hamza_ai_screen")
    ) {
      // Privacy Guarantee Strip
      Surface(
        color = TealPrimary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Zero-leak guarantee: No chat messages are shared without your consent. Keys stay server-side.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Quick Prompt Suggestion Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(HamzaAiRepository.suggestedPromptChips) { chipText ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clickable(enabled = !isGenerating) {
                HamzaAiRepository.sendMessage(chipText, context)
              }
              .testTag("prompt_chip_${chipText.take(10)}")
          ) {
            Text(
              text = chipText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = TealPrimary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Messages List
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 12.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          HamzaAiMessageBubble(
            message = msg,
            onCopy = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Hamza AI", msg.text)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        if (isGenerating) {
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(8.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = TealPrimary
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Hamza AI is thinking...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Generation Cancel Bar
      AnimatedVisibility(visible = isGenerating) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.Center
        ) {
          TextButton(
            onClick = { HamzaAiRepository.cancelGeneration() },
            modifier = Modifier.testTag("ai_stop_generation_button")
          ) {
            Icon(Icons.Default.Stop, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Stop Generating", color = AmberAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // Message Input Composer
      Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Ask Hamza AI in English, Hindi, or Urdu...", fontSize = 13.sp) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("ai_input_text_field"),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (inputText.isNotBlank() && !isGenerating) {
                val query = inputText
                inputText = ""
                HamzaAiRepository.sendMessage(query, context)
              }
            },
            enabled = inputText.isNotBlank() && !isGenerating,
            modifier = Modifier
              .size(46.dp)
              .background(
                if (inputText.isNotBlank() && !isGenerating) TealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
              )
              .testTag("ai_send_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = if (inputText.isNotBlank() && !isGenerating) Color.White else Color.Gray,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Clear History Dialog
    if (showClearConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showClearConfirmDialog = false },
        title = { Text("Clear AI Chat History?") },
        text = { Text("This will remove all messages from your Hamza AI conversation. This action cannot be undone.") },
        confirmButton = {
          TextButton(
            onClick = {
              HamzaAiRepository.clearChatHistory()
              showClearConfirmDialog = false
            },
            modifier = Modifier.testTag("confirm_clear_ai_history")
          ) {
            Text("Clear", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showClearConfirmDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Privacy & API Limits Info Dialog
    if (showPrivacyInfoDialog) {
      AlertDialog(
        onDismissRequest = { showPrivacyInfoDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hamza AI Security & Limits", fontWeight = FontWeight.Bold)
          }
        },
        text = {
          Column {
            Text(
              "• Zero-Trust Privacy: Your conversations in 1-on-1 and group chats are end-to-end encrypted. They are never sent to AI unless you explicitly press 'Translate' or 'Summarize'.",
              fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "• Server-Side Security: No API keys are embedded in this APK. All inferences route through Firebase AI Logic with App Check attestation.",
              fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "• Supported Languages: Full natural language comprehension for Hindi (हिन्दी), English, Urdu (اردو), and Arabic (العربية).",
              fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "• Fair Usage Policy: Free tier includes unlimited helpful responses. Rate limits apply to prevent abuse.",
              fontSize = 13.sp
            )
          }
        },
        confirmButton = {
          TextButton(onClick = { showPrivacyInfoDialog = false }) {
            Text("Got it", fontWeight = FontWeight.Bold)
          }
        }
      )
    }
  }
}

@Composable
fun HamzaAiMessageBubble(
  message: HamzaAiMessage,
  onCopy: () -> Unit
) {
  val isUser = message.isFromUser
  val bubbleColor = if (isUser) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .background(TealDark, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Card(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      colors = CardDefaults.cardColors(containerColor = bubbleColor),
      modifier = Modifier
        .widthIn(max = 300.dp)
        .testTag("ai_bubble_${message.id}")
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = message.text,
          color = textColor,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )

        if (!isUser) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onCopy,
              modifier = Modifier.size(24.dp).testTag("copy_ai_bubble_${message.id}")
            ) {
              Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy Response",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }
  }
}
