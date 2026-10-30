package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HamzaAiMessage
import com.example.data.repository.HamzaAiRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

/**
 * Stateful ChatMitraAIContent composable connecting directly to HamzaAiRepository.
 * Displays message history, input composer with send button, and loading indicators.
 */
@Composable
fun ChatMitraAIContent(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val messages by HamzaAiRepository.messages.collectAsState()
  val isGenerating by HamzaAiRepository.isGenerating.collectAsState()

  ChatMitraAIContent(
    messages = messages,
    isGenerating = isGenerating,
    onSendMessage = { prompt ->
      HamzaAiRepository.sendMessage(prompt, context)
    },
    modifier = modifier,
    onCancelGeneration = {
      HamzaAiRepository.cancelGeneration()
    },
    suggestedPrompts = HamzaAiRepository.suggestedPromptChips,
    onPromptChipClick = { prompt ->
      HamzaAiRepository.sendMessage(prompt, context)
    }
  )
}

/**
 * Stateless ChatMitraAIContent composable for flexible composition and testing.
 *
 * @param messages The list of AI and user conversation messages to display in history.
 * @param isGenerating Whether the AI assistant is currently generating a response.
 * @param onSendMessage Callback invoked when the user taps Send or presses the action key.
 * @param modifier Composable modifier for layout constraints.
 * @param onCancelGeneration Optional callback to halt in-flight AI response generation.
 * @param suggestedPrompts Optional prompt chips for quick interaction.
 * @param onPromptChipClick Callback invoked when a prompt chip is tapped.
 */
@Composable
fun ChatMitraAIContent(
  messages: List<HamzaAiMessage>,
  isGenerating: Boolean,
  onSendMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
  onCancelGeneration: (() -> Unit)? = null,
  suggestedPrompts: List<String> = emptyList(),
  onPromptChipClick: ((String) -> Unit)? = null
) {
  val context = LocalContext.current
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto-scroll to the bottom whenever a new message arrives or generation finishes
  LaunchedEffect(messages.size, isGenerating) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("chat_mitra_ai_content")
  ) {
    // Privacy & Zero-Leak Security Header Strip
    Surface(
      color = TealPrimary.copy(alpha = 0.08f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Zero Leak Shield",
          tint = TealPrimary,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Zero-leak guarantee: Conversations with Hamza AI are encrypted. Keys stay server-side.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Suggested Prompt Chips
    if (suggestedPrompts.isNotEmpty()) {
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
          .testTag("suggested_prompt_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(suggestedPrompts) { chipText ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clickable(enabled = !isGenerating) {
                if (onPromptChipClick != null) {
                  onPromptChipClick(chipText)
                } else {
                  onSendMessage(chipText)
                }
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
    }

    // Message History List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .testTag("message_history_list")
        .testTag("chat_mitra_ai_message_list")
    ) {
      items(messages, key = { it.id }) { msg ->
        HamzaAiMessageItem(
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

      // Loading Indicator for in-flight generation
      if (isGenerating) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .padding(8.dp)
              .testTag("loading_indicator")
              .testTag("chat_mitra_ai_loading_indicator")
          ) {
            CircularProgressIndicator(
              modifier = Modifier
                .size(18.dp)
                .testTag("ai_loading_spinner"),
              strokeWidth = 2.dp,
              color = TealPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Hamza AI is thinking...",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    // Stop Generation Action Bar
    AnimatedVisibility(
      visible = isGenerating && onCancelGeneration != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        TextButton(
          onClick = { onCancelGeneration?.invoke() },
          modifier = Modifier
            .testTag("ai_stop_generation_button")
            .testTag("chat_mitra_ai_stop_button")
        ) {
          Icon(
            imageVector = Icons.Default.Stop,
            contentDescription = "Stop Generating",
            tint = AmberAccent,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Stop Generating",
            color = AmberAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // Message Input Field and Send Button
    Surface(
      tonalElevation = 2.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("message_input_container")
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
          placeholder = {
            Text(
              text = "Ask Hamza AI in English, Hindi, Urdu...",
              fontSize = 13.sp
            )
          },
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("message_input_field")
            .testTag("ai_input_text_field")
            .testTag("chat_mitra_ai_input_field"),
          maxLines = 4,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(
            onSend = {
              if (inputText.isNotBlank() && !isGenerating) {
                val toSend = inputText.trim()
                inputText = ""
                onSendMessage(toSend)
              }
            }
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
          )
        )

        Spacer(modifier = Modifier.width(8.dp))

        val canSend = inputText.isNotBlank() && !isGenerating
        IconButton(
          onClick = {
            if (canSend) {
              val toSend = inputText.trim()
              inputText = ""
              onSendMessage(toSend)
            }
          },
          enabled = canSend,
          modifier = Modifier
            .size(46.dp)
            .background(
              color = if (canSend) TealPrimary else MaterialTheme.colorScheme.surfaceVariant,
              shape = CircleShape
            )
            .testTag("send_button")
            .testTag("ai_send_button")
            .testTag("chat_mitra_ai_send_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (canSend) Color.White else Color.Gray,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

/**
 * Message bubble item for Hamza AI chat history.
 */
@Composable
fun HamzaAiMessageItem(
  message: HamzaAiMessage,
  onCopy: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isUser = message.isFromUser
  val bubbleColor = if (isUser) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .background(TealDark, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "Hamza AI Avatar",
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
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
              modifier = Modifier
                .size(24.dp)
                .testTag("copy_ai_bubble_${message.id}")
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
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
