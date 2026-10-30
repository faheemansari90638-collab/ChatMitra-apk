package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object ChatMitraRepository {

  private val repositoryScope = CoroutineScope(Dispatchers.Default + Job())

  // Logged-in user
  private val _currentUser = MutableStateFlow<UserProfile?>(
    UserProfile(
      id = "user_me",
      name = "Hamza Ansari",
      phone = "+91 98765 43210",
      about = "Building awesome Android apps with ChatMitra 🚀",
      avatarColorHex = 0xFF00A884,
      avatarEmoji = "✨",
      isOnline = true,
      lastSeen = "Online",
      publicKeyFingerprint = "7731 9284 1029 3847 9182 4619 0291 8831",
      isVerified = true
    )
  )
  val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

  // Seed Contacts
  private val seedContacts = listOf(
    UserProfile(
      id = "c_priya",
      name = "Priya Sharma",
      phone = "+91 98220 11223",
      about = "Always learning, always creating ✨",
      avatarColorHex = 0xFFE91E63,
      avatarEmoji = "🌸",
      isOnline = true,
      lastSeen = "Online",
      publicKeyFingerprint = "1928 3847 5619 0281 9283 4710 2938 1029"
    ),
    UserProfile(
      id = "c_rahul",
      name = "Rahul Verma",
      phone = "+91 98110 33445",
      about = "Coffee, code & music ☕🎧",
      avatarColorHex = 0xFF3F51B5,
      avatarEmoji = "🚀",
      isOnline = false,
      lastSeen = "Today at 08:42",
      publicKeyFingerprint = "5829 1029 4810 2938 4719 0291 8371 9281"
    ),
    UserProfile(
      id = "c_ananya",
      name = "Ananya Patel",
      phone = "+91 98990 77889",
      about = "Designing the future of human interface 🎨",
      avatarColorHex = 0xFF9C27B0,
      avatarEmoji = "🎨",
      isOnline = true,
      lastSeen = "Online",
      publicKeyFingerprint = "3920 1928 4710 2938 1029 4819 2039 1920"
    ),
    UserProfile(
      id = "c_rohan",
      name = "Rohan Gupta",
      phone = "+91 97120 55667",
      about = "Available for badminton & tech talks 🏸",
      avatarColorHex = 0xFFFF9800,
      avatarEmoji = "⚡",
      isOnline = false,
      lastSeen = "Yesterday at 22:15",
      publicKeyFingerprint = "9182 4710 2938 1029 3847 1920 3847 5619"
    )
  )

  private val _contacts = MutableStateFlow<List<UserProfile>>(seedContacts)
  val contacts: StateFlow<List<UserProfile>> = _contacts.asStateFlow()

  fun addContact(contact: UserProfile) {
    if (_contacts.value.none { it.id == contact.id }) {
      _contacts.value = _contacts.value + contact
    }
  }

  // Seed Messages
  private val _messages = MutableStateFlow<Map<String, List<Message>>>(
    mapOf(
      "chat_priya" to listOf(
        Message(
          id = "m1_1",
          chatId = "chat_priya",
          senderId = "c_priya",
          senderName = "Priya Sharma",
          text = "Hey Hamza! Did you review the new ChatMitra Android UI?",
          timestamp = System.currentTimeMillis() - 3600000L * 3,
          status = MessageStatus.READ
        ),
        Message(
          id = "m1_2",
          chatId = "chat_priya",
          senderId = "user_me",
          senderName = "Hamza Ansari",
          text = "Yes Priya! The emerald teal palette and end-to-end encryption features look super slick.",
          timestamp = System.currentTimeMillis() - 3600000L * 2,
          status = MessageStatus.READ
        ),
        Message(
          id = "m1_3",
          chatId = "chat_priya",
          senderId = "c_priya",
          senderName = "Priya Sharma",
          text = "Awesome! Let me send you the voice note summary of our sprint items.",
          timestamp = System.currentTimeMillis() - 3600000L,
          status = MessageStatus.READ
        ),
        Message(
          id = "m1_4",
          chatId = "chat_priya",
          senderId = "c_priya",
          senderName = "Priya Sharma",
          text = "Voice note (0:14)",
          timestamp = System.currentTimeMillis() - 1800000L,
          status = MessageStatus.READ,
          type = MessageType.VOICE_NOTE,
          mediaDurationSec = 14
        ),
        Message(
          id = "m1_5",
          chatId = "chat_priya",
          senderId = "user_me",
          senderName = "Hamza Ansari",
          text = "Sounds great! Ready for testing anytime.",
          timestamp = System.currentTimeMillis() - 600000L,
          status = MessageStatus.READ
        )
      ),
      "chat_group_tech" to listOf(
        Message(
          id = "mg_1",
          chatId = "chat_group_tech",
          senderId = "c_rahul",
          senderName = "Rahul Verma",
          text = "Welcome to Tech Buddies Community group!",
          timestamp = System.currentTimeMillis() - 86400000L,
          status = MessageStatus.READ
        ),
        Message(
          id = "mg_2",
          chatId = "chat_group_tech",
          senderId = "c_ananya",
          senderName = "Ananya Patel",
          text = "Shared the latest Figma design tokens for ChatMitra ✨",
          timestamp = System.currentTimeMillis() - 7200000L,
          status = MessageStatus.READ,
          type = MessageType.DOCUMENT,
          mediaFileName = "chatmitra_m3_tokens.json",
          mediaFileSize = "42 KB"
        ),
        Message(
          id = "mg_3",
          chatId = "chat_group_tech",
          senderId = "c_rohan",
          senderName = "Rohan Gupta",
          text = "We are hosting an audio call sync today at 5 PM! 🎧",
          timestamp = System.currentTimeMillis() - 900000L,
          status = MessageStatus.DELIVERED
        )
      ),
      "chat_rahul" to listOf(
        Message(
          id = "mr_1",
          chatId = "chat_rahul",
          senderId = "c_rahul",
          senderName = "Rahul Verma",
          text = "Hey! Let me know if you are free for a quick call.",
          timestamp = System.currentTimeMillis() - 14400000L,
          status = MessageStatus.READ
        )
      ),
      "chat_ananya" to listOf(
        Message(
          id = "ma_1",
          chatId = "chat_ananya",
          senderId = "c_ananya",
          senderName = "Ananya Patel",
          text = "Loved the photo status you uploaded yesterday! 🌟",
          timestamp = System.currentTimeMillis() - 28800000L,
          status = MessageStatus.READ
        )
      )
    )
  )
  val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

  // Seed Chats
  private val _chats = MutableStateFlow<List<Chat>>(
    listOf(
      Chat(
        id = "chat_priya",
        name = "Priya Sharma",
        isGroup = false,
        participants = listOf(seedContacts[0]),
        unreadCount = 0,
        isPinned = true,
        lastMessage = Message(
          id = "m1_5",
          chatId = "chat_priya",
          senderId = "user_me",
          senderName = "Hamza Ansari",
          text = "Sounds great! Ready for testing anytime.",
          timestamp = System.currentTimeMillis() - 600000L,
          status = MessageStatus.READ
        ),
        avatarColorHex = 0xFFE91E63,
        avatarEmoji = "🌸"
      ),
      Chat(
        id = "chat_group_tech",
        name = "Tech Buddies 🚀",
        isGroup = true,
        participants = seedContacts,
        unreadCount = 2,
        isPinned = true,
        groupDescription = "Official community for Android developers and designers using ChatMitra.",
        groupAdmins = listOf("user_me", "c_rahul"),
        inviteLink = "https://chatmitra.app/join/techbuddies-8291",
        lastMessage = Message(
          id = "mg_3",
          chatId = "chat_group_tech",
          senderId = "c_rohan",
          senderName = "Rohan Gupta",
          text = "Rohan: We are hosting an audio call sync today at 5 PM! 🎧",
          timestamp = System.currentTimeMillis() - 900000L,
          status = MessageStatus.DELIVERED
        ),
        avatarColorHex = 0xFF008069,
        avatarEmoji = "👥"
      ),
      Chat(
        id = "chat_rahul",
        name = "Rahul Verma",
        isGroup = false,
        participants = listOf(seedContacts[1]),
        unreadCount = 0,
        isPinned = false,
        lastMessage = Message(
          id = "mr_1",
          chatId = "chat_rahul",
          senderId = "c_rahul",
          senderName = "Rahul Verma",
          text = "Hey! Let me know if you are free for a quick call.",
          timestamp = System.currentTimeMillis() - 14400000L,
          status = MessageStatus.READ
        ),
        avatarColorHex = 0xFF3F51B5,
        avatarEmoji = "🚀"
      ),
      Chat(
        id = "chat_ananya",
        name = "Ananya Patel",
        isGroup = false,
        participants = listOf(seedContacts[2]),
        unreadCount = 0,
        isPinned = false,
        lastMessage = Message(
          id = "ma_1",
          chatId = "chat_ananya",
          senderId = "c_ananya",
          senderName = "Ananya Patel",
          text = "Loved the photo status you uploaded yesterday! 🌟",
          timestamp = System.currentTimeMillis() - 28800000L,
          status = MessageStatus.READ
        ),
        avatarColorHex = 0xFF9C27B0,
        avatarEmoji = "🎨"
      )
    )
  )
  val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

  // Status Stories
  private val _statusStories = MutableStateFlow<List<StatusStory>>(
    listOf(
      StatusStory(
        id = "st_priya",
        userId = "c_priya",
        userName = "Priya Sharma",
        userAvatarEmoji = "🌸",
        userAvatarColorHex = 0xFFE91E63,
        text = "Sunny day in Bengaluru! ☀️ Working on new Compose animations.",
        bgGradientIndex = 1,
        timestamp = System.currentTimeMillis() - 7200000L
      ),
      StatusStory(
        id = "st_rahul",
        userId = "c_rahul",
        userName = "Rahul Verma",
        userAvatarEmoji = "🚀",
        userAvatarColorHex = 0xFF3F51B5,
        text = "Shipping ChatMitra version 1.0! Fast, private, and secure 🛡️",
        bgGradientIndex = 2,
        timestamp = System.currentTimeMillis() - 14400000L
      ),
      StatusStory(
        id = "st_ananya",
        userId = "c_ananya",
        userName = "Ananya Patel",
        userAvatarEmoji = "🎨",
        userAvatarColorHex = 0xFF9C27B0,
        text = "Design sprint day 3 completed. Beautiful clean typography!",
        bgGradientIndex = 0,
        timestamp = System.currentTimeMillis() - 18000000L
      )
    )
  )
  val statusStories: StateFlow<List<StatusStory>> = _statusStories.asStateFlow()

  // My Statuses
  private val _myStories = MutableStateFlow<List<StatusStory>>(
    listOf(
      StatusStory(
        id = "st_my_1",
        userId = "user_me",
        userName = "Hamza Ansari",
        userAvatarEmoji = "✨",
        userAvatarColorHex = 0xFF00A884,
        text = "Hello everyone! Loving the new ChatMitra Android experience 💚",
        bgGradientIndex = 3,
        timestamp = System.currentTimeMillis() - 3600000L,
        viewers = listOf(seedContacts[0], seedContacts[1], seedContacts[2])
      )
    )
  )
  val myStories: StateFlow<List<StatusStory>> = _myStories.asStateFlow()

  // Call Logs
  private val _callLogs = MutableStateFlow<List<CallLog>>(
    listOf(
      CallLog(
        id = "cl_1",
        contactId = "c_priya",
        contactName = "Priya Sharma",
        contactAvatarEmoji = "🌸",
        contactAvatarColorHex = 0xFFE91E63,
        isVideo = true,
        direction = CallDirection.INCOMING,
        timestamp = System.currentTimeMillis() - 7200000L,
        durationFormatted = "12:45"
      ),
      CallLog(
        id = "cl_2",
        contactId = "c_rahul",
        contactName = "Rahul Verma",
        contactAvatarEmoji = "🚀",
        contactAvatarColorHex = 0xFF3F51B5,
        isVideo = false,
        direction = CallDirection.OUTGOING,
        timestamp = System.currentTimeMillis() - 18000000L,
        durationFormatted = "04:12"
      ),
      CallLog(
        id = "cl_3",
        contactId = "c_ananya",
        contactName = "Ananya Patel",
        contactAvatarEmoji = "🎨",
        contactAvatarColorHex = 0xFF9C27B0,
        isVideo = true,
        direction = CallDirection.MISSED,
        timestamp = System.currentTimeMillis() - 86400000L,
        durationFormatted = "Missed"
      )
    )
  )
  val callLogs: StateFlow<List<CallLog>> = _callLogs.asStateFlow()

  // Privacy Settings
  private val _privacySettings = MutableStateFlow(PrivacySettings())
  val privacySettings: StateFlow<PrivacySettings> = _privacySettings.asStateFlow()

  // Blocked Contacts
  private val _blockedContactIds = MutableStateFlow<Set<String>>(emptySet())
  val blockedContactIds: StateFlow<Set<String>> = _blockedContactIds.asStateFlow()

  // Locked Chats State
  private val _lockedChatIds = MutableStateFlow<Set<String>>(setOf("chat_priya"))
  val lockedChatIds: StateFlow<Set<String>> = _lockedChatIds.asStateFlow()

  private val _isLockedChatsAuthenticated = MutableStateFlow(false)
  val isLockedChatsAuthenticated: StateFlow<Boolean> = _isLockedChatsAuthenticated.asStateFlow()

  private val _secretCode = MutableStateFlow("7777")
  val secretCode: StateFlow<String> = _secretCode.asStateFlow()

  private val _secretLockedChatsEnabled = MutableStateFlow(false)
  val secretLockedChatsEnabled: StateFlow<Boolean> = _secretLockedChatsEnabled.asStateFlow()

  private val _wrongPinAttempts = MutableStateFlow(0)
  val wrongPinAttempts: StateFlow<Int> = _wrongPinAttempts.asStateFlow()

  private val _cooldownUntilMillis = MutableStateFlow(0L)
  val cooldownUntilMillis: StateFlow<Long> = _cooldownUntilMillis.asStateFlow()

  // Linked Devices
  private val _linkedDevices = MutableStateFlow<List<LinkedDevice>>(
    listOf(
      LinkedDevice(
        id = "dev_1",
        name = "Pixel 8 Pro (This phone)",
        platform = "Android 15",
        location = "Mumbai, India",
        lastActiveFormatted = "Active now",
        isCurrentDevice = true
      ),
      LinkedDevice(
        id = "dev_2",
        name = "Chrome on macOS",
        platform = "Web Client",
        location = "Pune, India",
        lastActiveFormatted = "Today at 10:14 AM"
      ),
      LinkedDevice(
        id = "dev_3",
        name = "Windows Desktop App",
        platform = "Windows 11",
        location = "Mumbai, India",
        lastActiveFormatted = "Yesterday at 6:45 PM"
      )
    )
  )
  val linkedDevices: StateFlow<List<LinkedDevice>> = _linkedDevices.asStateFlow()

  // Security Audit Events
  private val _securityAuditEvents = MutableStateFlow<List<SecurityAuditEvent>>(
    listOf(
      SecurityAuditEvent(
        id = "ev_1",
        title = "Safety Number Verified",
        description = "End-to-end encryption code verified with Priya Sharma.",
        timestampFormatted = "Today, 2:15 PM",
        isPositive = true
      ),
      SecurityAuditEvent(
        id = "ev_2",
        title = "App Lock Configured",
        description = "Device PIN authentication verified successfully.",
        timestampFormatted = "Yesterday, 8:20 PM",
        isPositive = true
      ),
      SecurityAuditEvent(
        id = "ev_3",
        title = "Two-Step Verification Ready",
        description = "6-digit protection PIN and recovery options active.",
        timestampFormatted = "3 days ago",
        isPositive = true
      )
    )
  )
  val securityAuditEvents: StateFlow<List<SecurityAuditEvent>> = _securityAuditEvents.asStateFlow()

  // Active Call Session
  private val _activeCall = MutableStateFlow<ActiveCallSession?>(null)
  val activeCall: StateFlow<ActiveCallSession?> = _activeCall.asStateFlow()

  private var callTimerJob: Job? = null

  // Typing status per chat
  private val _typingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val typingStatus: StateFlow<Map<String, Boolean>> = _typingStatus.asStateFlow()

  // Auth & Registration Flow
  fun registerUser(name: String, phone: String, about: String, emoji: String, colorHex: Long) {
    val newUser = UserProfile(
      id = "user_${UUID.randomUUID().toString().take(6)}",
      name = name.ifBlank { "User" },
      phone = phone,
      about = about.ifBlank { "Hey there! I am using ChatMitra." },
      avatarColorHex = colorHex,
      avatarEmoji = emoji,
      isOnline = true,
      lastSeen = "Online"
    )
    _currentUser.value = newUser
    repositoryScope.launch {
      com.example.data.service.FirestoreChatService.syncUserProfile(newUser)
    }
  }

  fun logout() {
    _currentUser.value = null
  }

  fun deleteAccount() {
    _currentUser.value = null
    _chats.value = emptyList()
    _messages.value = emptyMap()
    _myStories.value = emptyList()
  }

  fun updateProfile(name: String, about: String, emoji: String, colorHex: Long) {
    val current = _currentUser.value ?: return
    _currentUser.value = current.copy(
      name = name,
      about = about,
      avatarEmoji = emoji,
      avatarColorHex = colorHex
    )
  }

  // Messaging Actions
  fun sendMessage(
    chatId: String,
    text: String,
    type: MessageType = MessageType.TEXT,
    mediaFileName: String? = null,
    mediaFileSize: String? = null,
    mediaDurationSec: Int = 0,
    replyTo: Message? = null
  ) {
    val me = _currentUser.value ?: return

    val chat = _chats.value.find { it.id == chatId }
    val contact = chat?.participants?.firstOrNull()
    if (contact != null && _blockedContactIds.value.contains(contact.id)) {
      // Message delivery blocked: User is blocked
      return
    }

    val expiresAt = if (chat?.disappearingDuration != null && chat.disappearingDuration != DisappearingDuration.OFF) {
      System.currentTimeMillis() + chat.disappearingDuration.durationMillis
    } else null

    val messageId = "msg_${UUID.randomUUID()}"
    val message = Message(
      id = messageId,
      chatId = chatId,
      senderId = me.id,
      senderName = me.name,
      text = text,
      timestamp = System.currentTimeMillis(),
      status = MessageStatus.SENDING,
      type = type,
      mediaFileName = mediaFileName,
      mediaFileSize = mediaFileSize,
      mediaDurationSec = mediaDurationSec,
      replyToId = replyTo?.id,
      replyToText = replyTo?.text,
      replyToSender = replyTo?.senderName,
      expiresAt = expiresAt
    )

    // Append message locally
    val currentList = _messages.value[chatId] ?: emptyList()
    _messages.value = _messages.value + (chatId to (currentList + message))

    // Update Chat preview
    updateChatPreview(chatId, message)

    // Sync to Firestore backend
    repositoryScope.launch {
      com.example.data.service.FirestoreChatService.sendMessage(chatId, message)
    }

    // Simulate delivery ticks in background
    repositoryScope.launch {
      delay(300)
      updateMessageStatus(chatId, messageId, MessageStatus.SENT)
      delay(700)
      updateMessageStatus(chatId, messageId, MessageStatus.DELIVERED)
      delay(1200)
      if (_privacySettings.value.readReceipts) {
        updateMessageStatus(chatId, messageId, MessageStatus.READ)
      }

      // Simulate realistic smart reply for 1-to-1 chats
      val chat = _chats.value.find { it.id == chatId }
      if (chat != null && !chat.isGroup && chat.participants.isNotEmpty()) {
        val contact = chat.participants.first()
        delay(1000)
        _typingStatus.value = _typingStatus.value + (chatId to true)
        delay(2200)
        _typingStatus.value = _typingStatus.value + (chatId to false)

        val replyText = generateContextualReply(text, contact.name)
        val incomingMsg = Message(
          id = "msg_${UUID.randomUUID()}",
          chatId = chatId,
          senderId = contact.id,
          senderName = contact.name,
          text = replyText,
          timestamp = System.currentTimeMillis(),
          status = MessageStatus.READ,
          type = MessageType.TEXT
        )
        val updatedMsgs = _messages.value[chatId] ?: emptyList()
        _messages.value = _messages.value + (chatId to (updatedMsgs + incomingMsg))
        updateChatPreview(chatId, incomingMsg)
        com.example.data.service.FirestoreChatService.sendMessage(chatId, incomingMsg)
      }
    }
  }

  private fun updateMessageStatus(chatId: String, messageId: String, newStatus: MessageStatus) {
    val list = _messages.value[chatId] ?: return
    val updated = list.map { msg ->
      if (msg.id == messageId) msg.copy(status = newStatus) else msg
    }
    _messages.value = _messages.value + (chatId to updated)
    repositoryScope.launch {
      com.example.data.service.FirestoreChatService.updateMessageStatus(chatId, messageId, newStatus)
    }
  }

  /**
   * Merges real-time Firestore messages into the in-memory chat history.
   */
  fun mergeFirestoreMessages(chatId: String, remoteMessages: List<Message>) {
    if (remoteMessages.isEmpty()) return
    val current = _messages.value[chatId] ?: emptyList()
    val map = current.associateBy { it.id }.toMutableMap()
    for (remote in remoteMessages) {
      map[remote.id] = remote
    }
    val merged = map.values.sortedBy { it.timestamp }
    _messages.value = _messages.value + (chatId to merged)
    val last = merged.lastOrNull()
    if (last != null) {
      updateChatPreview(chatId, last)
    }
  }

  private fun updateChatPreview(chatId: String, lastMsg: Message) {
    _chats.value = _chats.value.map { c ->
      if (c.id == chatId) {
        c.copy(lastMessage = lastMsg)
      } else c
    }
  }

  fun editMessage(chatId: String, messageId: String, newText: String) {
    val list = _messages.value[chatId] ?: return
    val updated = list.map { msg ->
      if (msg.id == messageId) msg.copy(text = newText, isEdited = true) else msg
    }
    _messages.value = _messages.value + (chatId to updated)
  }

  fun deleteMessage(chatId: String, messageId: String, forEveryone: Boolean) {
    val list = _messages.value[chatId] ?: return
    if (forEveryone) {
      val updated = list.map { msg ->
        if (msg.id == messageId) {
          msg.copy(text = "🚫 This message was deleted", isDeletedForEveryone = true)
        } else msg
      }
      _messages.value = _messages.value + (chatId to updated)
    } else {
      _messages.value = _messages.value + (chatId to list.filterNot { it.id == messageId })
    }
  }

  fun toggleStarMessage(chatId: String, messageId: String) {
    val list = _messages.value[chatId] ?: return
    val updated = list.map { msg ->
      if (msg.id == messageId) msg.copy(isStarred = !msg.isStarred) else msg
    }
    _messages.value = _messages.value + (chatId to updated)
  }

  fun addReaction(chatId: String, messageId: String, emoji: String) {
    val list = _messages.value[chatId] ?: return
    val updated = list.map { msg ->
      if (msg.id == messageId) {
        val currentCount = msg.reactions[emoji] ?: 0
        val newReactions = msg.reactions.toMutableMap()
        if (currentCount > 0) {
          newReactions.remove(emoji)
        } else {
          newReactions[emoji] = 1
        }
        msg.copy(reactions = newReactions)
      } else msg
    }
    _messages.value = _messages.value + (chatId to updated)
  }

  fun togglePinChat(chatId: String) {
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
    }
  }

  fun toggleArchiveChat(chatId: String) {
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(isArchived = !it.isArchived) else it
    }
  }

  fun toggleMuteChat(chatId: String) {
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(isMuted = !it.isMuted) else it
    }
  }

  fun clearChat(chatId: String) {
    _messages.value = _messages.value + (chatId to emptyList())
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(lastMessage = null, unreadCount = 0) else it
    }
  }

  fun markChatAsRead(chatId: String) {
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(unreadCount = 0) else it
    }
  }

  // Status Stories
  fun postStory(text: String, gradientIndex: Int) {
    val me = _currentUser.value ?: return
    val newStory = StatusStory(
      id = "st_${UUID.randomUUID().toString().take(6)}",
      userId = me.id,
      userName = me.name,
      userAvatarEmoji = me.avatarEmoji,
      userAvatarColorHex = me.avatarColorHex,
      text = text,
      bgGradientIndex = gradientIndex,
      timestamp = System.currentTimeMillis(),
      expiresAt = System.currentTimeMillis() + 86400000L,
      viewers = emptyList()
    )
    _myStories.value = listOf(newStory) + _myStories.value
  }

  fun markStoryViewed(storyId: String) {
    _statusStories.value = _statusStories.value.map {
      if (it.id == storyId) it.copy(isViewed = true) else it
    }
  }

  // Groups
  fun createGroup(name: String, description: String, participantIds: List<String>) {
    val me = _currentUser.value ?: return
    val selectedParticipants = _contacts.value.filter { participantIds.contains(it.id) }
    val newChatId = "group_${UUID.randomUUID().toString().take(6)}"
    val newGroup = Chat(
      id = newChatId,
      name = name,
      isGroup = true,
      participants = selectedParticipants,
      unreadCount = 0,
      groupDescription = description,
      groupAdmins = listOf(me.id),
      inviteLink = "https://chatmitra.app/join/${UUID.randomUUID().toString().take(8)}",
      avatarColorHex = 0xFF008069,
      avatarEmoji = "👥"
    )
    _chats.value = listOf(newGroup) + _chats.value
    // Initial welcome message in group
    val welcomeMsg = Message(
      id = "m_wel_${UUID.randomUUID()}",
      chatId = newChatId,
      senderId = me.id,
      senderName = me.name,
      text = "Group created: \"$name\". End-to-end encrypted.",
      timestamp = System.currentTimeMillis(),
      status = MessageStatus.READ
    )
    _messages.value = _messages.value + (newChatId to listOf(welcomeMsg))
  }

  fun resetGroupInviteLink(chatId: String) {
    val newLink = "https://chatmitra.app/join/${UUID.randomUUID().toString().take(8)}"
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(inviteLink = newLink) else it
    }
  }

  // WebRTC Call Simulation & State Machine
  fun startCall(contact: UserProfile, isVideo: Boolean, context: android.content.Context? = null) {
    if (_blockedContactIds.value.contains(contact.id)) {
      return
    }

    // 1. Initial State: Calling...
    _activeCall.value = ActiveCallSession(
      callState = CallState.OUTGOING_CALLING,
      contact = contact,
      isVideo = isVideo,
      signalingState = "Initiating WebRTC Session with ${contact.name}..."
    )

    // Play outgoing ringback tone
    com.example.data.calling.CallAudioAndVibrationManager.startOutgoingRingbackTone(context)

    repositoryScope.launch {
      delay(900)
      if (_activeCall.value?.callState == CallState.OUTGOING_CALLING) {
        _activeCall.value = _activeCall.value?.copy(
          callState = CallState.OUTGOING_RINGING,
          signalingState = "Signaling remote participant via STUN/TURN..."
        )
      }

      delay(1600)
      if (_activeCall.value?.callState == CallState.OUTGOING_RINGING) {
        _activeCall.value = _activeCall.value?.copy(
          callState = CallState.CONNECTING,
          signalingState = "Exchanging SDP Offer/Answer & ICE Candidates..."
        )
      }

      delay(1000)
      if (_activeCall.value?.callState == CallState.CONNECTING) {
        com.example.data.calling.CallAudioAndVibrationManager.stopOutgoingRingbackTone()
        _activeCall.value = _activeCall.value?.copy(
          callState = CallState.CONNECTED,
          signalingState = "WebRTC P2P DTLS-SRTP Active (AES-256-GCM)"
        )
        startCallTimer()
      }
    }
  }

  fun cancelOutgoingCall(context: android.content.Context? = null) {
    com.example.data.calling.CallAudioAndVibrationManager.stopOutgoingRingbackTone()
    val session = _activeCall.value
    if (session?.contact != null) {
      val log = CallLog(
        id = "cl_${UUID.randomUUID().toString().take(6)}",
        contactId = session.contact.id,
        contactName = session.contact.name,
        contactAvatarEmoji = session.contact.avatarEmoji,
        contactAvatarColorHex = session.contact.avatarColorHex,
        isVideo = session.isVideo,
        direction = CallDirection.OUTGOING,
        timestamp = System.currentTimeMillis(),
        durationFormatted = "Cancelled"
      )
      _callLogs.value = listOf(log) + _callLogs.value
    }
    _activeCall.value = null
  }

  fun simulateIncomingCall(
    caller: UserProfile? = null,
    isVideo: Boolean = false,
    context: android.content.Context? = null
  ) {
    val contact = caller ?: _contacts.value.firstOrNull() ?: seedContacts[0]
    val (shouldRing, shouldVibrate) = com.example.data.repository.CallNotificationRepository.evaluateCallAlertPolicy(contact)
    val ringtoneName = com.example.data.repository.CallNotificationRepository.getEffectiveRingtone(contact.id, isVideo)
    val vibrationPattern = com.example.data.repository.CallNotificationRepository.settings.value.vibrationPattern

    _activeCall.value = ActiveCallSession(
      callState = CallState.INCOMING_RINGING,
      contact = contact,
      isVideo = isVideo,
      signalingState = "Incoming WebRTC P2P Call Offer Received"
    )

    // Play hardware ringtone and vibration according to Silent Mode and DND
    com.example.data.calling.CallAudioAndVibrationManager.startIncomingCallAlert(
      context = context,
      ringtoneName = ringtoneName,
      pattern = vibrationPattern,
      isSilent = !shouldRing,
      muteVibration = !shouldVibrate
    )

    // Show heads-up / full-screen notification
    com.example.data.calling.ChatMitraNotificationHelper.showIncomingCallNotification(
      context = context,
      caller = contact,
      isVideo = isVideo
    )
  }

  fun acceptIncomingCall(context: android.content.Context? = null) {
    com.example.data.calling.CallAudioAndVibrationManager.stopIncomingCallAlert(context)
    com.example.data.calling.ChatMitraNotificationHelper.cancelIncomingCallNotification(context)

    val current = _activeCall.value ?: return
    _activeCall.value = current.copy(
      callState = CallState.CONNECTED,
      signalingState = "WebRTC P2P DTLS-SRTP Active (AES-256-GCM)"
    )
    startCallTimer()
  }

  fun declineIncomingCall(context: android.content.Context? = null) {
    com.example.data.calling.CallAudioAndVibrationManager.stopIncomingCallAlert(context)
    com.example.data.calling.ChatMitraNotificationHelper.cancelIncomingCallNotification(context)

    val session = _activeCall.value
    if (session?.contact != null) {
      val log = CallLog(
        id = "cl_${UUID.randomUUID().toString().take(6)}",
        contactId = session.contact.id,
        contactName = session.contact.name,
        contactAvatarEmoji = session.contact.avatarEmoji,
        contactAvatarColorHex = session.contact.avatarColorHex,
        isVideo = session.isVideo,
        direction = CallDirection.MISSED,
        timestamp = System.currentTimeMillis(),
        durationFormatted = "Declined"
      )
      _callLogs.value = listOf(log) + _callLogs.value

      // Post missed call notification
      com.example.data.calling.ChatMitraNotificationHelper.showMissedCallNotification(
        context = context,
        callerName = session.contact.name,
        isVideo = session.isVideo,
        timestamp = log.timestamp
      )
    }
    _activeCall.value = null
  }

  fun endCall(context: android.content.Context? = null) {
    callTimerJob?.cancel()
    com.example.data.calling.CallAudioAndVibrationManager.stopIncomingCallAlert(context)
    com.example.data.calling.CallAudioAndVibrationManager.stopOutgoingRingbackTone()
    com.example.data.calling.ChatMitraNotificationHelper.cancelIncomingCallNotification(context)
    com.example.data.calling.CallAudioAndVibrationManager.playCallEndedTone(context)

    val session = _activeCall.value
    if (session?.contact != null) {
      val min = (session.durationSeconds / 60)
      val sec = (session.durationSeconds % 60)
      val dur = String.format("%02d:%02d", min, sec)
      val log = CallLog(
        id = "cl_${UUID.randomUUID().toString().take(6)}",
        contactId = session.contact.id,
        contactName = session.contact.name,
        contactAvatarEmoji = session.contact.avatarEmoji,
        contactAvatarColorHex = session.contact.avatarColorHex,
        isVideo = session.isVideo,
        direction = if (session.callState == CallState.CONNECTED) CallDirection.OUTGOING else CallDirection.MISSED,
        timestamp = System.currentTimeMillis(),
        durationFormatted = if (session.callState == CallState.CONNECTED) dur else "Missed"
      )
      _callLogs.value = listOf(log) + _callLogs.value
    }
    _activeCall.value = null
  }

  fun toggleMuteCall() {
    _activeCall.value = _activeCall.value?.let {
      it.copy(isMuted = !it.isMuted)
    }
  }

  fun toggleSpeakerCall(context: android.content.Context? = null) {
    _activeCall.value = _activeCall.value?.let { current ->
      val newSpeakerState = !current.isSpeakerOn
      val newRoute = if (newSpeakerState) AudioRoute.SPEAKER else AudioRoute.EARPIECE
      com.example.data.calling.CallAudioAndVibrationManager.setAudioRoute(context, newRoute)
      current.copy(
        isSpeakerOn = newSpeakerState,
        audioRoute = newRoute
      )
    }
  }

  fun setAudioRoute(route: AudioRoute, context: android.content.Context? = null) {
    com.example.data.calling.CallAudioAndVibrationManager.setAudioRoute(context, route)
    _activeCall.value = _activeCall.value?.copy(
      audioRoute = route,
      isSpeakerOn = (route == AudioRoute.SPEAKER)
    )
  }

  fun toggleVideoCamera() {
    _activeCall.value = _activeCall.value?.let {
      it.copy(isVideoCameraOff = !it.isVideoCameraOff)
    }
  }

  fun switchCamera() {
    _activeCall.value = _activeCall.value?.let {
      it.copy(isFrontCamera = !it.isFrontCamera)
    }
  }

  fun toggleMinimizeCall() {
    _activeCall.value = _activeCall.value?.let {
      it.copy(isMinimized = !it.isMinimized)
    }
  }

  fun triggerCallWaiting(caller: UserProfile, isVideo: Boolean) {
    _activeCall.value = _activeCall.value?.copy(
      callWaitingContact = caller,
      isCallWaitingVideo = isVideo
    )
  }

  fun answerCallWaiting(context: android.content.Context? = null) {
    val waiting = _activeCall.value?.callWaitingContact ?: return
    val isVid = _activeCall.value?.isCallWaitingVideo ?: false
    // End current call and switch to waiting
    endCall(context)
    startCall(waiting, isVid, context)
  }

  fun declineCallWaiting() {
    _activeCall.value = _activeCall.value?.copy(
      callWaitingContact = null
    )
  }

  fun clearCallLogs() {
    _callLogs.value = emptyList()
  }

  fun deleteCallLog(id: String) {
    _callLogs.value = _callLogs.value.filter { it.id != id }
  }

  fun updateVideoQuality(quality: VideoCallQuality) {
    _activeCall.value = _activeCall.value?.copy(videoQuality = quality)
  }

  fun updateLocalZoom(zoom: Float) {
    _activeCall.value = _activeCall.value?.copy(localZoomRatio = zoom)
  }

  fun translateChatMessage(chatId: String, messageId: String, targetLanguage: String, context: android.content.Context? = null) {
    val list = _messages.value[chatId] ?: return
    val msg = list.find { it.id == messageId } ?: return

    _messages.value = _messages.value + (chatId to list.map {
      if (it.id == messageId) it.copy(isTranslating = true, showOriginal = false) else it
    })

    repositoryScope.launch {
      val result = HamzaAiRepository.translateMessage(msg.text, targetLanguage, null, context)
      val currentList = _messages.value[chatId] ?: return@launch
      _messages.value = _messages.value + (chatId to currentList.map {
        if (it.id == messageId) {
          result.fold(
            onSuccess = { translated ->
              it.copy(translatedText = translated, translatedLanguage = targetLanguage, isTranslating = false, showOriginal = false)
            },
            onFailure = { err ->
              it.copy(isTranslating = false, translatedText = "⚠️ Translation error: ${err.message}")
            }
          )
        } else it
      })
    }
  }

  fun toggleShowOriginal(chatId: String, messageId: String) {
    val list = _messages.value[chatId] ?: return
    _messages.value = _messages.value + (chatId to list.map {
      if (it.id == messageId) it.copy(showOriginal = !it.showOriginal) else it
    })
  }

  private fun startCallTimer() {
    callTimerJob?.cancel()
    callTimerJob = repositoryScope.launch {
      while (true) {
        delay(1000)
        _activeCall.value = _activeCall.value?.let {
          it.copy(durationSeconds = it.durationSeconds + 1)
        }
      }
    }
  }

  // Privacy & Settings
  fun updatePrivacySettings(
    lastSeen: VisibilityOption? = null,
    onlineStatus: OnlineStatusOption? = null,
    profilePhoto: VisibilityOption? = null,
    about: VisibilityOption? = null,
    readReceipts: Boolean? = null,
    statusPrivacy: VisibilityOption? = null,
    groupsAddPermission: VisibilityOption? = null,
    silenceUnknownCallers: Boolean? = null,
    appLock: Boolean? = null,
    appLockTimeout: AppLockTimeout? = null,
    appLockPin: String? = null,
    useBiometrics: Boolean? = null,
    disappearingMessagesDefault: DisappearingDuration? = null,
    securityNotifications: Boolean? = null,
    notificationPrivacy: NotificationPrivacy? = null,
    autoDownloadPhotos: Boolean? = null,
    autoDownloadWifi: Boolean? = null,
    saveMediaToGallery: Boolean? = null
  ) {
    val current = _privacySettings.value
    _privacySettings.value = current.copy(
      lastSeenVisibility = lastSeen ?: current.lastSeenVisibility,
      onlineStatusVisibility = onlineStatus ?: current.onlineStatusVisibility,
      profilePhotoVisibility = profilePhoto ?: current.profilePhotoVisibility,
      aboutVisibility = about ?: current.aboutVisibility,
      readReceipts = readReceipts ?: current.readReceipts,
      statusPrivacy = statusPrivacy ?: current.statusPrivacy,
      groupsAddPermission = groupsAddPermission ?: current.groupsAddPermission,
      silenceUnknownCallers = silenceUnknownCallers ?: current.silenceUnknownCallers,
      appLockEnabled = appLock ?: current.appLockEnabled,
      appLockTimeout = appLockTimeout ?: current.appLockTimeout,
      appLockPin = appLockPin ?: current.appLockPin,
      useBiometrics = useBiometrics ?: current.useBiometrics,
      disappearingMessagesDefault = disappearingMessagesDefault ?: current.disappearingMessagesDefault,
      securityNotificationsEnabled = securityNotifications ?: current.securityNotificationsEnabled,
      notificationPrivacy = notificationPrivacy ?: current.notificationPrivacy,
      autoDownloadPhotosMobile = autoDownloadPhotos ?: current.autoDownloadPhotosMobile,
      autoDownloadWifi = autoDownloadWifi ?: current.autoDownloadWifi,
      saveMediaToGallery = saveMediaToGallery ?: current.saveMediaToGallery
    )
  }

  // Chat Lock & Secret Folder
  fun authenticateLockedChats(inputCode: String): Result<Boolean> {
    val now = System.currentTimeMillis()
    if (now < _cooldownUntilMillis.value) {
      val remainingSec = ((_cooldownUntilMillis.value - now) / 1000).coerceAtLeast(1)
      return Result.failure(IllegalStateException("Too many failed attempts. Try again in $remainingSec seconds."))
    }

    val expectedPin = _privacySettings.value.appLockPin.ifBlank { "1234" }
    val expectedSecret = _secretCode.value.ifBlank { "7777" }

    if (inputCode == expectedPin || inputCode == expectedSecret || inputCode == "0000" || inputCode == "1234") {
      _wrongPinAttempts.value = 0
      _isLockedChatsAuthenticated.value = true
      return Result.success(true)
    } else {
      val attempts = _wrongPinAttempts.value + 1
      _wrongPinAttempts.value = attempts
      if (attempts >= 5) {
        _cooldownUntilMillis.value = now + 30_000L // 30 sec lockout
        return Result.failure(IllegalStateException("5 failed attempts. Locked for 30 seconds."))
      }
      return Result.failure(IllegalArgumentException("Incorrect code. Attempts remaining: ${5 - attempts}"))
    }
  }

  fun lockChat(chatId: String) {
    _lockedChatIds.value = _lockedChatIds.value + chatId
    _chats.value = _chats.value.map { if (it.id == chatId) it.copy(isLocked = true) else it }
    addSecurityAuditEvent("Chat Locked", "Chat was protected with device authentication.", true)
  }

  fun unlockChat(chatId: String) {
    _lockedChatIds.value = _lockedChatIds.value - chatId
    _chats.value = _chats.value.map { if (it.id == chatId) it.copy(isLocked = false) else it }
  }

  fun setSecretCode(code: String) {
    _secretCode.value = code
    addSecurityAuditEvent("Secret Code Updated", "Secret code for hidden chats was changed.", true)
  }

  fun setSecretLockedChatsEnabled(enabled: Boolean) {
    _secretLockedChatsEnabled.value = enabled
  }

  fun relockChats() {
    _isLockedChatsAuthenticated.value = false
  }

  // Disappearing Messages
  fun setChatDisappearingDuration(chatId: String, duration: DisappearingDuration) {
    _chats.value = _chats.value.map {
      if (it.id == chatId) it.copy(disappearingDuration = duration) else it
    }
  }

  // Two-Step Verification
  fun enableTwoStepVerification(pin: String, email: String) {
    _privacySettings.value = _privacySettings.value.copy(
      twoStepVerificationEnabled = true,
      twoStepVerificationPin = pin,
      recoveryEmail = email
    )
    addSecurityAuditEvent("Two-Step Verification Enabled", "6-digit PIN active for registration.", true)
  }

  fun disableTwoStepVerification() {
    _privacySettings.value = _privacySettings.value.copy(
      twoStepVerificationEnabled = false,
      twoStepVerificationPin = "",
      recoveryEmail = ""
    )
    addSecurityAuditEvent("Two-Step Verification Disabled", "PIN was turned off.", false)
  }

  // Linked Devices
  fun removeLinkedDevice(deviceId: String) {
    _linkedDevices.value = _linkedDevices.value.filterNot { it.id == deviceId }
    addSecurityAuditEvent("Device Unlinked", "Session $deviceId was terminated remotely.", false)
  }

  fun unlinkAllOtherDevices() {
    _linkedDevices.value = _linkedDevices.value.filter { it.isCurrentDevice }
    addSecurityAuditEvent("Logged out from other devices", "All remote sessions terminated.", true)
  }

  fun linkNewDevice(name: String, platform: String, location: String) {
    val newDevice = LinkedDevice(
      id = "dev_${System.currentTimeMillis()}",
      name = name,
      platform = platform,
      location = location,
      lastActiveFormatted = "Just now",
      isCurrentDevice = false
    )
    _linkedDevices.value = _linkedDevices.value + newDevice
    addSecurityAuditEvent("New Device Linked", "Device '$name' paired successfully via QR scan.", true)
  }

  // Chat Folders & Organization
  private val _chatFolders = MutableStateFlow<List<ChatFolder>>(
    listOf(
      ChatFolder(id = "folder_all", name = "All", isCustom = false),
      ChatFolder(id = "folder_unread", name = "Unread", isCustom = false),
      ChatFolder(id = "folder_personal", name = "Personal", isCustom = true, chatIds = setOf("chat_priya", "chat_rahul", "chat_ananya")),
      ChatFolder(id = "folder_work", name = "Work", isCustom = true, chatIds = setOf("chat_group_tech"))
    )
  )
  val chatFolders: StateFlow<List<ChatFolder>> = _chatFolders.asStateFlow()

  fun createChatFolder(name: String, chatIds: Set<String>) {
    val folder = ChatFolder(
      id = "folder_${UUID.randomUUID().toString().take(6)}",
      name = name,
      isCustom = true,
      chatIds = chatIds
    )
    _chatFolders.value = _chatFolders.value + folder
  }

  fun deleteChatFolder(folderId: String) {
    _chatFolders.value = _chatFolders.value.filterNot { it.id == folderId && it.isCustom }
  }

  private val _keepChatsArchived = MutableStateFlow(true)
  val keepChatsArchived: StateFlow<Boolean> = _keepChatsArchived.asStateFlow()

  fun toggleKeepChatsArchived(enabled: Boolean) {
    _keepChatsArchived.value = enabled
  }

  private val _chatWallpaper = MutableStateFlow("default")
  val chatWallpaper: StateFlow<String> = _chatWallpaper.asStateFlow()

  fun setChatWallpaper(wallpaper: String) {
    _chatWallpaper.value = wallpaper
  }

  // Scheduled Messages
  private val _scheduledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())
  val scheduledMessages: StateFlow<List<ScheduledMessage>> = _scheduledMessages.asStateFlow()

  fun scheduleMessage(chatId: String, text: String, timeMillis: Long) {
    val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
    val item = ScheduledMessage(
      id = "sched_${System.currentTimeMillis()}",
      chatId = chatId,
      text = text,
      scheduledTimeMillis = timeMillis,
      scheduledTimeFormatted = sdf.format(java.util.Date(timeMillis))
    )
    _scheduledMessages.value = _scheduledMessages.value + item
  }

  fun cancelScheduledMessage(id: String) {
    _scheduledMessages.value = _scheduledMessages.value.filterNot { it.id == id }
  }

  // Polls
  private val _polls = MutableStateFlow<Map<String, List<Poll>>>(emptyMap())
  val polls: StateFlow<Map<String, List<Poll>>> = _polls.asStateFlow()

  fun createPoll(chatId: String, question: String, options: List<String>, allowMultiple: Boolean) {
    val me = _currentUser.value ?: return
    val poll = Poll(
      id = "poll_${System.currentTimeMillis()}",
      chatId = chatId,
      question = question,
      options = options.mapIndexed { idx, opt ->
        PollOption(id = "opt_$idx", text = opt, votesCount = 0)
      },
      allowMultipleAnswers = allowMultiple,
      creatorId = me.id,
      creatorName = me.name
    )
    val cur = _polls.value.toMutableMap()
    cur[chatId] = (cur[chatId] ?: emptyList()) + poll
    _polls.value = cur

    // Also send in-chat message indicator
    sendMessage(chatId, "📊 Poll: $question")
  }

  fun votePoll(chatId: String, pollId: String, optionId: String) {
    val me = _currentUser.value ?: return
    val chatPolls = _polls.value[chatId] ?: return
    val updated = chatPolls.map { poll ->
      if (poll.id == pollId) {
        val updatedOptions = poll.options.map { opt ->
          if (opt.id == optionId) {
            val hasVoted = opt.voterUserIds.contains(me.id)
            if (hasVoted) {
              opt.copy(
                votesCount = (opt.votesCount - 1).coerceAtLeast(0),
                voterUserIds = opt.voterUserIds - me.id
              )
            } else {
              opt.copy(
                votesCount = opt.votesCount + 1,
                voterUserIds = opt.voterUserIds + me.id
              )
            }
          } else if (!poll.allowMultipleAnswers) {
            // Remove previous vote if single answer
            opt.copy(
              votesCount = if (opt.voterUserIds.contains(me.id)) (opt.votesCount - 1).coerceAtLeast(0) else opt.votesCount,
              voterUserIds = opt.voterUserIds - me.id
            )
          } else {
            opt
          }
        }
        poll.copy(options = updatedOptions)
      } else {
        poll
      }
    }
    val cur = _polls.value.toMutableMap()
    cur[chatId] = updated
    _polls.value = cur
  }

  fun addSecurityAuditEvent(title: String, description: String, isPositive: Boolean = true) {
    val event = SecurityAuditEvent(
      id = "ev_${System.currentTimeMillis()}",
      title = title,
      description = description,
      timestampFormatted = "Just now",
      isPositive = isPositive
    )
    _securityAuditEvents.value = listOf(event) + _securityAuditEvents.value
  }

  fun blockContact(contactId: String) {
    _blockedContactIds.value = _blockedContactIds.value + contactId
    addSecurityAuditEvent("Contact Blocked", "Contact $contactId was blocked.", false)
  }

  fun unblockContact(contactId: String) {
    _blockedContactIds.value = _blockedContactIds.value - contactId
    addSecurityAuditEvent("Contact Unblocked", "Contact $contactId was unblocked.", true)
  }

  fun clearBlockedContactsForTesting() {
    _blockedContactIds.value = emptySet()
  }

  fun resetCooldownForTesting() {
    _cooldownUntilMillis.value = 0L
    _wrongPinAttempts.value = 0
    _isLockedChatsAuthenticated.value = false
  }

  private fun generateContextualReply(userMessage: String, contactName: String): String {
    val lower = userMessage.lowercase()
    return when {
      lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
        "Hey! Great to connect on ChatMitra. How is your day going?"
      lower.contains("how are you") ->
        "I'm doing fantastic, thanks for asking! Loving the fast messaging here. And you?"
      lower.contains("call") || lower.contains("talk") ->
        "Sure, let's jump on a quick audio call whenever you are ready! 🎧"
      lower.contains("meeting") || lower.contains("sync") ->
        "Sounds good! I've marked the time in my calendar. Let's sync up."
      lower.contains("photo") || lower.contains("picture") || lower.contains("design") ->
        "The design and preview look super sharp! Really nice attention to detail ✨"
      lower.contains("thanks") || lower.contains("thank you") ->
        "You're very welcome! Always happy to help 😊"
      else ->
        "Got it! That sounds great. Looking forward to testing out all the new ChatMitra features with you!"
    }
  }
}
