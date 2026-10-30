package com.example.ui.chatdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import com.example.data.repository.HamzaAiRepository
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisappearingDuration
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.Poll
import com.example.data.model.PollOption
import com.example.data.model.ReportCategory
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.UserBlockRepository
import androidx.compose.ui.platform.LocalContext
import com.example.data.webrtc.WebRtcSignalingManager
import com.example.ui.chatdetail.InChatCallBanner
import com.example.ui.chatdetail.InChatCallOverlay
import com.example.ui.components.E2EEBanner
import com.example.ui.components.MessageReceiptIndicator
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BubbleIncomingDark
import com.example.ui.theme.BubbleIncomingLight
import com.example.ui.theme.BubbleOutgoingDark
import com.example.ui.theme.BubbleOutgoingLight
import com.example.ui.theme.ChatBackgroundDark
import com.example.ui.theme.ChatBackgroundLight
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
  chatId: String,
  onBackClick: () -> Unit,
  onAudioCallClick: (UserProfile) -> Unit,
  onVideoCallClick: (UserProfile) -> Unit,
  onViewContactClick: (String) -> Unit
) {
  val chats by ChatMitraRepository.chats.collectAsState()
  val allMessages by ChatMitraRepository.messages.collectAsState()
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val typingStatus by ChatMitraRepository.typingStatus.collectAsState()
  val activeCall by ChatMitraRepository.activeCall.collectAsState()
  val webRtcSignaling = remember { WebRtcSignalingManager.getInstance() }
  val signalingState by webRtcSignaling.signalingState.collectAsState()
  val context = LocalContext.current

  val chat = chats.find { it.id == chatId }
  val messages = allMessages[chatId] ?: emptyList()
  val contact = chat?.participants?.firstOrNull()
  val isOtherTyping = typingStatus[chatId] == true

  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val clipboardManager: ClipboardManager = LocalClipboardManager.current

  var inputText by remember { mutableStateOf("") }
  var replyingToMessage by remember { mutableStateOf<Message?>(null) }
  var selectedMessageForAction by remember { mutableStateOf<Message?>(null) }
  var showAttachmentMenu by remember { mutableStateOf(false) }
  var showMenuOverflow by remember { mutableStateOf(false) }
  var showSafetyDialog by remember { mutableStateOf(false) }
  var showEditDialog by remember { mutableStateOf(false) }
  var editingMessageText by remember { mutableStateOf("") }
  var isRecordingVoice by remember { mutableStateOf(false) }
  var recordingSeconds by remember { mutableIntStateOf(0) }

  val userBlockRepository = remember { UserBlockRepository() }
  var showDisappearingDialog by remember { mutableStateOf(false) }
  var showBlockConfirmDialog by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }
  var reportCategory by remember { mutableStateOf(ReportCategory.SPAM) }
  var reportIncludeMessages by remember { mutableStateOf(true) }
  var reportDetails by remember { mutableStateOf("") }

  // Polls, Scheduled Messages, and Voice Speed
  val pollsMap by ChatMitraRepository.polls.collectAsState()
  val scheduledMessages by ChatMitraRepository.scheduledMessages.collectAsState()
  val chatPolls = pollsMap[chatId] ?: emptyList()
  val chatScheduled = scheduledMessages.filter { it.chatId == chatId }

  var voiceSpeed by remember { mutableStateOf("1x") }
  var showCreatePollDialog by remember { mutableStateOf(false) }
  var pollQuestionInput by remember { mutableStateOf("") }
  var pollOption1Input by remember { mutableStateOf("") }
  var pollOption2Input by remember { mutableStateOf("") }
  var pollOption3Input by remember { mutableStateOf("") }
  var pollAllowMultiple by remember { mutableStateOf(false) }

  var showScheduleMessageDialog by remember { mutableStateOf(false) }
  var scheduleTextInput by remember { mutableStateOf("") }
  var scheduleDelayMinutes by remember { mutableIntStateOf(60) }

  var showTranslateDialog by remember { mutableStateOf(false) }
  var messageToTranslate by remember { mutableStateOf<Message?>(null) }
  var showSummarizeDialog by remember { mutableStateOf(false) }
  var isSummarizing by remember { mutableStateOf(false) }
  var chatSummaryText by remember { mutableStateOf("") }
  var showSmartReplySheet by remember { mutableStateOf(false) }
  var smartRepliesList by remember { mutableStateOf<List<String>>(emptyList()) }
  var isLoadingSmartReplies by remember { mutableStateOf(false) }

  // Auto scroll to bottom when new messages arrive
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  // Voice recording timer
  LaunchedEffect(isRecordingVoice) {
    if (isRecordingVoice) {
      recordingSeconds = 0
      while (isRecordingVoice) {
        delay(1000)
        recordingSeconds++
      }
    }
  }

  // Mark chat as read and listen for real-time Firestore messages
  androidx.compose.runtime.DisposableEffect(chatId) {
    ChatMitraRepository.markChatAsRead(chatId)
    val listenerRegistration = com.example.data.service.FirestoreChatService.listenToChatMessages(chatId) { remoteMessages ->
      ChatMitraRepository.mergeFirestoreMessages(chatId, remoteMessages)
    }
    onDispose {
      listenerRegistration?.remove()
    }
  }

  val isDark = MaterialTheme.colorScheme.background == ChatBackgroundDark
  val bubbleOutColor = if (isDark) BubbleOutgoingDark else BubbleOutgoingLight
  val bubbleInColor = if (isDark) BubbleIncomingDark else BubbleIncomingLight
  val chatBg = if (isDark) ChatBackgroundDark else ChatBackgroundLight

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onViewContactClick(chatId) }
          ) {
            chat?.let {
              UserAvatar(
                name = it.name,
                colorHex = it.avatarColorHex,
                emoji = it.avatarEmoji,
                size = 40.dp,
                fontSize = 18,
                isOnline = contact?.isOnline ?: false
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = chat?.name ?: "Chat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = if (isOtherTyping) "typing..." else if (contact?.isOnline == true) "Online" else (contact?.lastSeen ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = if (isOtherTyping) AmberAccent else Color.White.copy(alpha = 0.85f),
                fontWeight = if (isOtherTyping) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          if (!chat?.isGroup!! && contact != null) {
            IconButton(onClick = {
              val callId = "call_${System.currentTimeMillis()}"
              webRtcSignaling.initiateCall(callId, isVideo = true, context)
              onVideoCallClick(contact)
            }) {
              Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
            }
            IconButton(onClick = {
              val callId = "call_${System.currentTimeMillis()}"
              webRtcSignaling.initiateCall(callId, isVideo = false, context)
              onAudioCallClick(contact)
            }) {
              Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
            }
          }
          IconButton(onClick = { showMenuOverflow = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
          }

          DropdownMenu(
            expanded = showMenuOverflow,
            onDismissRequest = { showMenuOverflow = false }
          ) {
            DropdownMenuItem(
              text = { Text("Encryption Info & QR") },
              onClick = {
                showMenuOverflow = false
                showSafetyDialog = true
              },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmberAccent) }
            )
            DropdownMenuItem(
              text = { Text(if (chat?.isLocked == true) "Unlock Chat" else "Lock Chat") },
              onClick = {
                showMenuOverflow = false
                if (chat?.isLocked == true) {
                  ChatMitraRepository.unlockChat(chatId)
                } else {
                  ChatMitraRepository.lockChat(chatId)
                }
              },
              leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )
            DropdownMenuItem(
              text = { Text("Disappearing Messages") },
              onClick = {
                showMenuOverflow = false
                showDisappearingDialog = true
              },
              leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
            )
            DropdownMenuItem(
              text = { Text("Summarize with Hamza AI ✨") },
              onClick = {
                showMenuOverflow = false
                showSummarizeDialog = true
                isSummarizing = true
                chatSummaryText = ""
                scope.launch {
                  val msgsText = messages.takeLast(25).map { "${it.senderName}: ${it.text}" }
                  val res = HamzaAiRepository.summarizeConversation(chat?.name ?: "Chat", msgsText, "English", context)
                  res.fold(
                    onSuccess = { summary ->
                      chatSummaryText = summary
                      isSummarizing = false
                    },
                    onFailure = { err ->
                      chatSummaryText = "Unable to summarize chat at this moment: ${err.message}"
                      isSummarizing = false
                    }
                  )
                }
              },
              leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAccent) }
            )
            DropdownMenuItem(
              text = { Text("Clear Chat") },
              onClick = {
                showMenuOverflow = false
                ChatMitraRepository.clearChat(chatId)
              },
              leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
            DropdownMenuItem(
              text = { Text("Mute Notifications") },
              onClick = {
                showMenuOverflow = false
                ChatMitraRepository.toggleMuteChat(chatId)
              }
            )
            if (contact != null && chat?.isGroup != true) {
              DropdownMenuItem(
                text = { Text("Block Contact", color = MaterialTheme.colorScheme.error) },
                onClick = {
                  showMenuOverflow = false
                  showBlockConfirmDialog = true
                },
                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
              )
            }
            DropdownMenuItem(
              text = { Text(if (chat?.isGroup == true) "Report Group" else "Report Contact") },
              onClick = {
                showMenuOverflow = false
                showReportDialog = true
              },
              leadingIcon = { Icon(Icons.Default.Report, contentDescription = null) }
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    },
    containerColor = chatBg
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // End-to-End Encryption Banner
        E2EEBanner(onClickVerify = { showSafetyDialog = true })

        // In-Chat WebRTC Call Banner (Shown when call is active and minimized for continuous chatting)
        if (activeCall != null && signalingState.isMinimized) {
          val callContact = activeCall?.contact ?: contact ?: UserProfile(id = "user_peer", name = "Friend", phone = "")
          InChatCallBanner(
            contact = callContact,
            signalingState = signalingState,
            onExpandClick = { webRtcSignaling.setMinimized(false) },
            onToggleMute = { webRtcSignaling.toggleAudioMute() },
            onEndCall = {
              webRtcSignaling.endCall(context)
              ChatMitraRepository.endCall()
            }
          )
        }

        // Scheduled Messages Banner
        chatScheduled.forEach { sched ->
          Card(
            colors = CardDefaults.cardColors(containerColor = AmberAccent.copy(alpha = 0.15f)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 4.dp)
              .testTag("scheduled_message_banner")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text("Scheduled: \"${sched.text}\"", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("Sending on ${sched.scheduledTimeFormatted}", style = MaterialTheme.typography.labelSmall, color = CheckmarkGrey)
              }
              TextButton(onClick = { ChatMitraRepository.cancelScheduledMessage(sched.id) }) {
                Text("Cancel", color = Color.Red, fontSize = 12.sp)
              }
            }
          }
        }

        // Messages List
        LazyColumn(
          state = listState,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
          contentPadding = PaddingValues(vertical = 8.dp)
        ) {
          items(messages, key = { it.id }) { message ->
            val isMe = message.senderId == (currentUser?.id ?: "")
            val messagePoll = chatPolls.find { message.text.contains(it.question) }
            MessageBubbleItem(
              message = message,
              isMe = isMe,
              bubbleColor = if (isMe) bubbleOutColor else bubbleInColor,
              voiceSpeed = voiceSpeed,
              onToggleVoiceSpeed = {
                voiceSpeed = when (voiceSpeed) {
                  "1x" -> "1.5x"
                  "1.5x" -> "2x"
                  else -> "1x"
                }
              },
              poll = messagePoll,
              onVotePoll = { optId ->
                if (messagePoll != null) {
                  ChatMitraRepository.votePoll(chatId, messagePoll.id, optId)
                }
              },
              currentUserId = currentUser?.id ?: "",
              onToggleShowOriginal = {
                ChatMitraRepository.toggleShowOriginal(chatId, message.id)
              },
              onLongClick = { selectedMessageForAction = message }
            )
            Spacer(modifier = Modifier.height(4.dp))
          }
        }

        // Reply Preview Banner
        AnimatedVisibility(visible = replyingToMessage != null) {
          replyingToMessage?.let { replyTarget ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .padding(horizontal = 16.dp, vertical = 6.dp)
                  .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(TealPrimary, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = replyTarget.senderName,
                    style = MaterialTheme.typography.labelMedium,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = replyTarget.text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                IconButton(onClick = { replyingToMessage = null }) {
                  Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }

        // Bottom Input Bar
        Surface(
          color = MaterialTheme.colorScheme.surface,
          shadowElevation = 8.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            // Attachment Sheet
            AnimatedVisibility(visible = showAttachmentMenu) {
              AttachmentOptionsRow(
                onSelectPhoto = {
                  showAttachmentMenu = false
                  ChatMitraRepository.sendMessage(
                    chatId = chatId,
                    text = "Shared a photo 📷",
                    type = MessageType.IMAGE
                  )
                },
                onSelectDocument = {
                  showAttachmentMenu = false
                  ChatMitraRepository.sendMessage(
                    chatId = chatId,
                    text = "Project_Requirements.pdf",
                    type = MessageType.DOCUMENT,
                    mediaFileName = "Project_Requirements.pdf",
                    mediaFileSize = "1.8 MB"
                  )
                },
                onSelectAudio = {
                  showAttachmentMenu = false
                  ChatMitraRepository.sendMessage(
                    chatId = chatId,
                    text = "Meeting_Recording.mp3",
                    type = MessageType.AUDIO,
                    mediaFileName = "Meeting_Recording.mp3",
                    mediaFileSize = "4.2 MB"
                  )
                },
                onSelectPoll = {
                  showAttachmentMenu = false
                  pollQuestionInput = ""
                  pollOption1Input = ""
                  pollOption2Input = ""
                  pollOption3Input = ""
                  pollAllowMultiple = false
                  showCreatePollDialog = true
                },
                onSelectSchedule = {
                  showAttachmentMenu = false
                  scheduleTextInput = inputText
                  showScheduleMessageDialog = true
                },
                onSelectLocation = {
                  showAttachmentMenu = false
                  ChatMitraRepository.sendMessage(
                    chatId = chatId,
                    text = "📍 Live Location: Current location shared (Updated 1m ago)",
                    type = MessageType.TEXT
                  )
                }
              )
            }

            Row(
              modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (isRecordingVoice) {
                // Voice Recording State
                Row(
                  modifier = Modifier
                    .weight(1f)
                    .background(TealPrimary.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Recording audio: ${recordingSeconds}s",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.weight(1f))
                  Text(
                    text = "Tap send to finish",
                    style = MaterialTheme.typography.labelSmall,
                    color = CheckmarkGrey
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                  onClick = {
                    isRecordingVoice = false
                    val duration = if (recordingSeconds > 0) recordingSeconds else 4
                    ChatMitraRepository.sendMessage(
                      chatId = chatId,
                      text = "Voice note (0:${String.format("%02d", duration)})",
                      type = MessageType.VOICE_NOTE,
                      mediaDurationSec = duration
                    )
                  },
                  modifier = Modifier
                    .background(TealPrimary, CircleShape)
                    .size(46.dp)
                ) {
                  Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Voice Note", tint = Color.White)
                }
              } else {
                // Normal Text & Attachment Input
                IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                  Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                IconButton(
                  onClick = {
                    showSmartReplySheet = true
                    if (inputText.isBlank()) {
                      isLoadingSmartReplies = true
                      scope.launch {
                        val lastOther = messages.lastOrNull { it.senderId != (currentUser?.id ?: "") }?.text ?: "Hello"
                        val res = HamzaAiRepository.suggestPoliteReplies(lastOther, context)
                        smartRepliesList = res.getOrDefault(
                          listOf(
                            "Sounds great! 👍",
                            "Thanks for updating me! ✨",
                            "I'll check this and reply shortly.",
                            "Let's sync up later today."
                          )
                        )
                        isLoadingSmartReplies = false
                      }
                    }
                  },
                  modifier = Modifier.testTag("hamza_ai_smart_reply_btn")
                ) {
                  Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Hamza AI Smart Replies",
                    tint = AmberAccent
                  )
                }

                OutlinedTextField(
                  value = inputText,
                  onValueChange = { inputText = it },
                  placeholder = { Text("Message...", fontSize = 15.sp) },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
                  maxLines = 4,
                  shape = RoundedCornerShape(24.dp),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                  )
                )

                Spacer(modifier = Modifier.width(6.dp))

                if (inputText.isNotBlank()) {
                  IconButton(
                    onClick = {
                      ChatMitraRepository.sendMessage(
                        chatId = chatId,
                        text = inputText.trim(),
                        replyTo = replyingToMessage
                      )
                      inputText = ""
                      replyingToMessage = null
                    },
                    modifier = Modifier
                      .background(TealPrimary, CircleShape)
                      .size(46.dp)
                      .testTag("send_message_button")
                  ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                  }
                } else {
                  IconButton(
                    onClick = { isRecordingVoice = true },
                    modifier = Modifier
                      .background(TealPrimary, CircleShape)
                      .size(46.dp)
                      .testTag("record_voice_button")
                  ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = Color.White)
                  }
                }
              }
            }
          }
        }
      }

      // In-Chat WebRTC Call Fullscreen Overlay (Shown when call is active and expanded within chat)
      if (activeCall != null && !signalingState.isMinimized) {
        val callContact = activeCall?.contact ?: contact ?: UserProfile(id = "user_peer", name = "Friend", phone = "")
        InChatCallOverlay(
          contact = callContact,
          signalingState = signalingState,
          onMinimize = { webRtcSignaling.setMinimized(true) },
          onToggleMute = { webRtcSignaling.toggleAudioMute() },
          onToggleVideo = { webRtcSignaling.toggleVideoMute() },
          onToggleSpeaker = { webRtcSignaling.toggleSpeakerphone() },
          onSwitchCamera = { webRtcSignaling.switchCamera() },
          onEndCall = {
            webRtcSignaling.endCall(context)
            ChatMitraRepository.endCall()
          }
        )
      }
    }

    // Contextual Action Dialog for selected message
    selectedMessageForAction?.let { msg ->
      val isMe = msg.senderId == (currentUser?.id ?: "")
      AlertDialog(
        onDismissRequest = { selectedMessageForAction = null },
        title = {
          Text(text = "Message Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Quick Emoji Reactions
            Text("React with emoji:", style = MaterialTheme.typography.labelMedium, color = CheckmarkGrey)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")
              emojis.forEach { emoji ->
                Text(
                  text = emoji,
                  fontSize = 26.sp,
                  modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                      ChatMitraRepository.addReaction(chatId, msg.id, emoji)
                      selectedMessageForAction = null
                    }
                    .padding(4.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Reply Action
            TextButton(
              onClick = {
                replyingToMessage = msg
                selectedMessageForAction = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Reply, contentDescription = null, tint = TealPrimary)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Reply to message", color = MaterialTheme.colorScheme.onSurface)
            }

            // Copy Action
            TextButton(
              onClick = {
                clipboardManager.setText(AnnotatedString(msg.text))
                selectedMessageForAction = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TealPrimary)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Copy text", color = MaterialTheme.colorScheme.onSurface)
            }

            // Translate with Hamza AI
            TextButton(
              onClick = {
                messageToTranslate = msg
                selectedMessageForAction = null
                showTranslateDialog = true
              },
              modifier = Modifier.fillMaxWidth().testTag("translate_message_action")
            ) {
              Icon(Icons.Default.Translate, contentDescription = null, tint = TealPrimary)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Translate with Hamza AI 🌐", color = MaterialTheme.colorScheme.onSurface)
            }

            // Star / Unstar
            TextButton(
              onClick = {
                ChatMitraRepository.toggleStarMessage(chatId, msg.id)
                selectedMessageForAction = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                if (msg.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = AmberAccent
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(if (msg.isStarred) "Unstar message" else "Star message", color = MaterialTheme.colorScheme.onSurface)
            }

            // Edit Action (own message only)
            if (isMe && !msg.isDeletedForEveryone) {
              TextButton(
                onClick = {
                  editingMessageText = msg.text
                  showEditDialog = true
                },
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Edit message", color = MaterialTheme.colorScheme.onSurface)
              }
            }

            // Delete Options
            TextButton(
              onClick = {
                ChatMitraRepository.deleteMessage(chatId, msg.id, forEveryone = false)
                selectedMessageForAction = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Delete for me", color = Color.Red)
            }

            if (isMe && !msg.isDeletedForEveryone) {
              TextButton(
                onClick = {
                  ChatMitraRepository.deleteMessage(chatId, msg.id, forEveryone = true)
                  selectedMessageForAction = null
                },
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Delete for everyone", color = Color.Red)
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { selectedMessageForAction = null }) {
            Text("Cancel")
          }
        }
      )
    }

    // Edit Message Dialog
    if (showEditDialog && selectedMessageForAction != null) {
      AlertDialog(
        onDismissRequest = { showEditDialog = false },
        title = { Text("Edit Message") },
        text = {
          OutlinedTextField(
            value = editingMessageText,
            onValueChange = { editingMessageText = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              selectedMessageForAction?.let {
                ChatMitraRepository.editMessage(chatId, it.id, editingMessageText)
              }
              showEditDialog = false
              selectedMessageForAction = null
            }
          ) {
            Text("Save")
          }
        },
        dismissButton = {
          TextButton(onClick = { showEditDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Safety Numbers Verification Dialog
    if (showSafetyDialog) {
      SafetyNumberDialog(
        contactName = chat?.name ?: "Contact",
        fingerprint = contact?.publicKeyFingerprint ?: "4819 2038 9128 4471 9023 1184 7291 0029",
        onDismiss = { showSafetyDialog = false }
      )
    }

    // Disappearing Messages Dialog
    if (showDisappearingDialog) {
      AlertDialog(
        onDismissRequest = { showDisappearingDialog = false },
        title = { Text("Disappearing Messages", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text(
              "When turned on, new messages sent in this chat will disappear after the selected duration. Recipients can still save, screenshot, or forward them.",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            DisappearingDuration.values().forEach { duration ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    ChatMitraRepository.setChatDisappearingDuration(chatId, duration)
                    showDisappearingDialog = false
                  }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = (chat?.disappearingDuration ?: DisappearingDuration.OFF) == duration,
                  onClick = null,
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(duration.label, modifier = Modifier.padding(start = 12.dp))
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showDisappearingDialog = false }) {
            Text("Done")
          }
        }
      )
    }

    // Block Confirmation Dialog
    if (showBlockConfirmDialog && contact != null) {
      AlertDialog(
        onDismissRequest = { showBlockConfirmDialog = false },
        title = { Text("Block ${contact.name}?") },
        text = {
          Text("Blocked contacts will no longer be able to call you or send you deliverable messages. Your status updates and online presence will be hidden.")
        },
        confirmButton = {
          TextButton(
            onClick = {
              scope.launch {
                userBlockRepository.blockUser(currentUser?.id ?: "", contact.id, context = context)
              }
              showBlockConfirmDialog = false
            }
          ) {
            Text("Block", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showBlockConfirmDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Report Dialog
    if (showReportDialog) {
      AlertDialog(
        onDismissRequest = { showReportDialog = false },
        title = { Text(if (chat?.isGroup == true) "Report Group" else "Report ${contact?.name ?: "Contact"}") },
        text = {
          Column {
            Text("Select an issue:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            ReportCategory.values().forEach { category ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { reportCategory = category }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = reportCategory == category,
                  onClick = null,
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(category.label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { reportIncludeMessages = !reportIncludeMessages }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = reportIncludeMessages,
                onCheckedChange = { reportIncludeMessages = it },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
              )
              Text("Include recent messages as evidence to report", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 6.dp))
            }
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              scope.launch {
                val reportedId = if (chat?.isGroup == true) chat.id else (contact?.id ?: "")
                userBlockRepository.reportUser(
                  reporterUserId = currentUser?.id ?: "",
                  reportedUserId = reportedId,
                  category = reportCategory,
                  details = reportDetails,
                  includeLast5Messages = reportIncludeMessages,
                  chatId = chatId,
                  context = context
                )
              }
              showReportDialog = false
            }
          ) {
            Text("Submit Report", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showReportDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Create Poll Dialog
    if (showCreatePollDialog) {
      AlertDialog(
        onDismissRequest = { showCreatePollDialog = false },
        icon = { Icon(Icons.Default.BarChart, contentDescription = null, tint = TealPrimary) },
        title = { Text("Create a Poll", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            OutlinedTextField(
              value = pollQuestionInput,
              onValueChange = { pollQuestionInput = it },
              label = { Text("Ask a question...") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("poll_question_input")
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = pollOption1Input,
              onValueChange = { pollOption1Input = it },
              label = { Text("Option 1") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("poll_option1_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = pollOption2Input,
              onValueChange = { pollOption2Input = it },
              label = { Text("Option 2") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("poll_option2_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = pollOption3Input,
              onValueChange = { pollOption3Input = it },
              label = { Text("Option 3 (Optional)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { pollAllowMultiple = !pollAllowMultiple }
            ) {
              Checkbox(
                checked = pollAllowMultiple,
                onCheckedChange = { pollAllowMultiple = it },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
              )
              Text("Allow multiple answers", style = MaterialTheme.typography.bodySmall)
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              val options = listOfNotNull(
                pollOption1Input.trim().takeIf { it.isNotBlank() },
                pollOption2Input.trim().takeIf { it.isNotBlank() },
                pollOption3Input.trim().takeIf { it.isNotBlank() }
              )
              if (pollQuestionInput.isNotBlank() && options.size >= 2) {
                ChatMitraRepository.createPoll(
                  chatId = chatId,
                  question = pollQuestionInput.trim(),
                  options = options,
                  allowMultiple = pollAllowMultiple
                )
                showCreatePollDialog = false
              }
            },
            enabled = pollQuestionInput.isNotBlank() && pollOption1Input.isNotBlank() && pollOption2Input.isNotBlank(),
            modifier = Modifier.testTag("create_poll_confirm_btn")
          ) {
            Text("Create Poll")
          }
        },
        dismissButton = {
          TextButton(onClick = { showCreatePollDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Schedule Message Dialog
    if (showScheduleMessageDialog) {
      AlertDialog(
        onDismissRequest = { showScheduleMessageDialog = false },
        icon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberAccent) },
        title = { Text("Schedule Message", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text("Message will be sent automatically at the scheduled time.")
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = scheduleTextInput,
              onValueChange = { scheduleTextInput = it },
              label = { Text("Message text") },
              minLines = 2,
              maxLines = 4,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("schedule_message_text_input")
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Send Time:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val delays = listOf(15 to "In 15 minutes", 60 to "In 1 hour", 720 to "Tomorrow morning")
            delays.forEach { (mins, label) ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { scheduleDelayMinutes = mins }
                  .padding(vertical = 2.dp)
              ) {
                RadioButton(
                  selected = scheduleDelayMinutes == mins,
                  onClick = { scheduleDelayMinutes = mins },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Text(label, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        },
        confirmButton = {
          Button(
            onClick = {
              if (scheduleTextInput.isNotBlank()) {
                val triggerTime = System.currentTimeMillis() + (scheduleDelayMinutes * 60000L)
                ChatMitraRepository.scheduleMessage(chatId, scheduleTextInput.trim(), triggerTime)
                if (inputText == scheduleTextInput) {
                  inputText = ""
                }
                showScheduleMessageDialog = false
              }
            },
            enabled = scheduleTextInput.isNotBlank(),
            modifier = Modifier.testTag("schedule_message_confirm_btn")
          ) {
            Text("Schedule")
          }
        },
        dismissButton = {
          TextButton(onClick = { showScheduleMessageDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Language Picker Dialog for Translation
    if (showTranslateDialog && messageToTranslate != null) {
      val targetMsg = messageToTranslate!!
      val languages = listOf(
        "Hindi (हिन्दी)" to "Hindi",
        "Urdu (اردو)" to "Urdu",
        "Arabic (العربية)" to "Arabic",
        "English" to "English",
        "Spanish (Español)" to "Spanish",
        "French (Français)" to "French",
        "German (Deutsch)" to "German",
        "Marathi (मराठी)" to "Marathi"
      )

      AlertDialog(
        onDismissRequest = {
          showTranslateDialog = false
          messageToTranslate = null
        },
        icon = { Icon(Icons.Default.Translate, contentDescription = null, tint = TealPrimary) },
        title = { Text("Translate with Hamza AI", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            Text(
              "Select target language to translate: \"${targetMsg.text.take(40)}...\"",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            languages.forEach { (label, langCode) ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp)
                  .clickable {
                    ChatMitraRepository.translateChatMessage(chatId, targetMsg.id, langCode, context)
                    showTranslateDialog = false
                    messageToTranslate = null
                  }
                  .testTag("translate_lang_$langCode")
              ) {
                Text(
                  text = label,
                  fontWeight = FontWeight.Medium,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = {
            showTranslateDialog = false
            messageToTranslate = null
          }) {
            Text("Cancel")
          }
        }
      )
    }

    // Conversation Summarizer Dialog
    if (showSummarizeDialog) {
      AlertDialog(
        onDismissRequest = { showSummarizeDialog = false },
        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAccent) },
        title = { Text("Hamza AI Conversation Summary", fontWeight = FontWeight.Bold) },
        text = {
          Column {
            if (isSummarizing) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                LinearProgressIndicator(color = AmberAccent, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                  "Hamza AI is reading recent messages and distilling key action points...",
                  style = MaterialTheme.typography.bodySmall,
                  color = CheckmarkGrey,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            } else {
              Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = chatSummaryText,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(14.dp),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        },
        confirmButton = {
          if (!isSummarizing && chatSummaryText.isNotBlank()) {
            Button(
              onClick = {
                clipboardManager.setText(AnnotatedString(chatSummaryText))
                showSummarizeDialog = false
              },
              colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Copy Summary")
            }
          }
        },
        dismissButton = {
          TextButton(onClick = { showSummarizeDialog = false }) {
            Text("Close")
          }
        }
      )
    }

    // Smart Reply and Message Polishing Dialog
    if (showSmartReplySheet) {
      AlertDialog(
        onDismissRequest = { showSmartReplySheet = false },
        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAccent) },
        title = {
          Text(
            if (inputText.isNotBlank()) "Polish with Hamza AI" else "Hamza AI Smart Replies",
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column {
            if (inputText.isNotBlank()) {
              Text("Your current draft:", style = MaterialTheme.typography.labelSmall, color = CheckmarkGrey)
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp)
              ) {
                Text(
                  inputText,
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.padding(8.dp)
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  scope.launch {
                    val res = HamzaAiRepository.translateMessage(
                      text = "Please rewrite the following message to sound polite, clear, and professional: \"$inputText\"",
                      targetLanguage = "English",
                      context = context
                    )
                    res.onSuccess { polished ->
                      inputText = polished.trim().removeSurrounding("\"")
                      showSmartReplySheet = false
                    }
                  }
                },
                modifier = Modifier.fillMaxWidth().testTag("polish_draft_btn"),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = TealPrimary)
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Make Polite & Professional")
              }
            } else {
              Text(
                "Tap a suggested reply to insert into composer:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(10.dp))
              if (isLoadingSmartReplies) {
                LinearProgressIndicator(color = AmberAccent, modifier = Modifier.fillMaxWidth())
              } else {
                smartRepliesList.forEach { suggestion ->
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                      .clickable {
                        inputText = suggestion
                        showSmartReplySheet = false
                      }
                      .testTag("smart_reply_chip")
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showSmartReplySheet = false }) {
            Text("Done")
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
  message: Message,
  isMe: Boolean,
  bubbleColor: Color,
  voiceSpeed: String = "1x",
  onToggleVoiceSpeed: () -> Unit = {},
  poll: Poll? = null,
  onVotePoll: (String) -> Unit = {},
  currentUserId: String = "",
  onToggleShowOriginal: () -> Unit = {},
  onLongClick: () -> Unit
) {
  val align = if (isMe) Alignment.End else Alignment.Start
  val shape = if (isMe) {
    RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
  } else {
    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalAlignment = align
  ) {
    Surface(
      shape = shape,
      color = bubbleColor,
      shadowElevation = 1.dp,
      modifier = Modifier
        .widthIn(max = 300.dp)
        .combinedClickable(
          onClick = {},
          onLongClick = onLongClick
        )
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        // Quoted message preview if replied
        message.replyToText?.let { replyText ->
          Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp)
          ) {
            Row(modifier = Modifier.padding(6.dp)) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(30.dp)
                  .background(TealPrimary, RoundedCornerShape(2.dp))
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = message.replyToSender ?: "Reply",
                  style = MaterialTheme.typography.labelSmall,
                  color = TealPrimary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = replyText,
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        // Message Content by Type
        when (message.type) {
          MessageType.VOICE_NOTE -> {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(TealPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${message.mediaDurationSec}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = TealPrimary.copy(alpha = 0.18f),
                modifier = Modifier
                  .clickable { onToggleVoiceSpeed() }
                  .testTag("voice_speed_button")
              ) {
                Text(
                  text = voiceSpeed,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = TealPrimary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          MessageType.DOCUMENT -> {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Icon(Icons.Default.Description, contentDescription = null, tint = TealPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = message.mediaFileName ?: "Document.pdf",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1
                )
                Text(
                  text = message.mediaFileSize ?: "1.2 MB",
                  style = MaterialTheme.typography.labelSmall,
                  color = CheckmarkGrey
                )
              }
            }
          }

          MessageType.IMAGE -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(36.dp))
                Text("📷 Photo Preview", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          else -> {
            if (poll != null) {
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.BarChart, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = poll.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
                Text(
                  text = if (poll.allowMultipleAnswers) "Select one or more" else "Select one option",
                  style = MaterialTheme.typography.labelSmall,
                  color = CheckmarkGrey,
                  modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                val totalVotes = poll.options.sumOf { it.votesCount }.coerceAtLeast(1)
                for (opt in poll.options) {
                  val isSelected = opt.voterUserIds.contains(currentUserId)
                  val votePct = (opt.votesCount.toFloat() / totalVotes.toFloat()).coerceIn(0f, 1f)
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) TealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp)
                      .clickable { onVotePoll(opt.id) }
                      .testTag("poll_option_${opt.id}")
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        if (poll.allowMultipleAnswers) {
                          Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onVotePoll(opt.id) },
                            colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
                            modifier = Modifier.size(20.dp)
                          )
                        } else {
                          RadioButton(
                            selected = isSelected,
                            onClick = { onVotePoll(opt.id) },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary),
                            modifier = Modifier.size(20.dp)
                          )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = opt.text,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          modifier = Modifier.weight(1f)
                        )
                        Text(
                          text = "${opt.votesCount}",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.SemiBold,
                          color = CheckmarkGrey
                        )
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      LinearProgressIndicator(
                        progress = { votePct },
                        color = TealPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(4.dp)
                          .clip(RoundedCornerShape(2.dp))
                      )
                    }
                  }
                }
              }
            } else {
              if (message.isTranslating) {
                Column {
                  Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .background(TealPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(
                      Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = AmberAccent,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Translating with Hamza AI...",
                      style = MaterialTheme.typography.labelSmall,
                      color = TealPrimary,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              } else if (message.translatedText != null && !message.showOriginal) {
                val isRtl = message.translatedLanguage?.lowercase()?.let { it.contains("urdu") || it.contains("arabic") } == true
                Column {
                  // Translation Header Badge
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                  ) {
                    Icon(
                      Icons.Default.Translate,
                      contentDescription = null,
                      tint = TealPrimary,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Translated to ${message.translatedLanguage ?: "Language"}",
                      style = MaterialTheme.typography.labelSmall,
                      color = TealPrimary,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                      text = "Show original",
                      style = MaterialTheme.typography.labelSmall,
                      color = CheckmarkGrey,
                      modifier = Modifier
                        .clickable { onToggleShowOriginal() }
                        .padding(horizontal = 4.dp)
                    )
                  }

                  Text(
                    text = message.translatedText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = if (isRtl) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              } else {
                Column {
                  Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  if (message.translatedText != null && message.showOriginal) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Show translation (${message.translatedLanguage ?: "AI"})",
                      style = MaterialTheme.typography.labelSmall,
                      color = TealPrimary,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier
                        .clickable { onToggleShowOriginal() }
                        .padding(vertical = 2.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Bottom timestamp, edited, starred, and ticks
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (message.isStarred) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
          }
          if (message.isEdited) {
            Text(text = "Edited", fontSize = 10.sp, color = CheckmarkGrey)
            Spacer(modifier = Modifier.width(4.dp))
          }
          val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
          Text(
            text = sdf.format(Date(message.timestamp)),
            fontSize = 10.sp,
            color = CheckmarkGrey
          )
          if (isMe) {
            Spacer(modifier = Modifier.width(4.dp))
            MessageReceiptIndicator(
              status = message.status,
              isRead = message.isRead,
              readAt = message.readAt
            )
          }
        }
      }
    }

    // Emoji reactions row
    if (message.reactions.isNotEmpty()) {
      Row(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        message.reactions.forEach { (emoji, count) ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
          ) {
            Text(
              text = "$emoji $count",
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun AttachmentOptionsRow(
  onSelectPhoto: () -> Unit,
  onSelectDocument: () -> Unit,
  onSelectAudio: () -> Unit,
  onSelectPoll: () -> Unit,
  onSelectSchedule: () -> Unit,
  onSelectLocation: () -> Unit
) {
  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(12.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(horizontal = 8.dp)
  ) {
    item { AttachmentIconBtn(icon = Icons.Default.Image, label = "Gallery", color = Color(0xFF9C27B0), onClick = onSelectPhoto) }
    item { AttachmentIconBtn(icon = Icons.Default.Description, label = "Document", color = Color(0xFF3F51B5), onClick = onSelectDocument) }
    item { AttachmentIconBtn(icon = Icons.Default.MusicNote, label = "Audio", color = Color(0xFFFF9800), onClick = onSelectAudio) }
    item { AttachmentIconBtn(icon = Icons.Default.BarChart, label = "Poll", color = Color(0xFF00897B), onClick = onSelectPoll) }
    item { AttachmentIconBtn(icon = Icons.Default.Schedule, label = "Schedule", color = Color(0xFF00ACC1), onClick = onSelectSchedule) }
    item { AttachmentIconBtn(icon = Icons.Default.LocationOn, label = "Location", color = Color(0xFF43A047), onClick = onSelectLocation) }
  }
}

@Composable
fun AttachmentIconBtn(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(color, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = label, tint = Color.White)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = label, style = MaterialTheme.typography.labelSmall)
  }
}
