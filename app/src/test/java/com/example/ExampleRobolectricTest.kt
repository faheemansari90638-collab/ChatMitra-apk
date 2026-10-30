package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Message
import com.example.data.MessageDAO
import com.example.data.User
import com.example.data.UserDAO
import com.example.data.model.CallParticipant
import com.example.data.model.CallState
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.ParticipantRole
import com.example.data.model.ReportStatus
import com.example.data.model.UserProfile
import com.example.data.repository.AdminModerationRepository
import com.example.data.repository.AuthenticationRepository
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.FriendContactRepository
import com.example.data.repository.GroupCallingRepository
import com.example.data.repository.LocalizationRepository
import com.example.data.repository.LowDataRepository
import com.example.data.repository.MediaRepository
import com.example.data.repository.NetworkReliabilityManager
import com.example.data.repository.UserBlockRepository
import com.example.data.repository.UserSearchRepository
import com.example.data.repository.UserStatusRepository
import com.example.data.webrtc.PeerConnectionState
import com.example.data.webrtc.SdpType
import com.example.data.webrtc.WebRtcSignalingManager
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    ChatMitraRepository.clearBlockedContactsForTesting()
    ChatMitraRepository.resetCooldownForTesting()
  }

  @After
  fun tearDown() {
    database.close()
    ChatMitraRepository.clearBlockedContactsForTesting()
    ChatMitraRepository.resetCooldownForTesting()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ChatMitra", appName)
  }

  @Test
  fun `repository initial state verification`() {
    val user = ChatMitraRepository.currentUser.value
    assertNotNull(user)
    assertEquals("Hamza Ansari", user?.name)

    val chats = ChatMitraRepository.chats.value
    assertTrue("Should have initial seeded conversations", chats.isNotEmpty())

    val contacts = ChatMitraRepository.contacts.value
    assertTrue("Should have contacts list", contacts.isNotEmpty())
  }

  @Test
  fun `room database user and message persistence`() = runBlocking {
    val userDao: UserDAO = database.userDAO()
    val messageDao: MessageDAO = database.messageDAO()

    // 1. Test User persistence
    val testUser = User(
      id = "u_test_1",
      name = "Aarav Sharma",
      phone = "+91 99887 76655",
      about = "Testing local SQLite cache",
      avatarColorHex = 0xFF00A884,
      avatarEmoji = "✨",
      isOnline = true
    )
    userDao.insertUser(testUser)

    val retrievedUser = userDao.getUserById("u_test_1").first()
    assertNotNull(retrievedUser)
    assertEquals("Aarav Sharma", retrievedUser?.name)
    assertEquals("+91 99887 76655", retrievedUser?.phone)

    // 2. Test Message persistence and chat query
    val testMessage = Message(
      id = "msg_test_1",
      chatId = "chat_aarav",
      senderId = "u_test_1",
      senderName = "Aarav Sharma",
      text = "Hello from Room database!",
      status = MessageStatus.SENT.name,
      type = MessageType.TEXT.name
    )
    messageDao.insertMessage(testMessage)

    val chatMessages = messageDao.getMessagesForChat("chat_aarav").first()
    assertEquals(1, chatMessages.size)
    assertEquals("Hello from Room database!", chatMessages.first().text)

    // 3. Test Message read status update and readAt timestamp
    val readTimestamp = System.currentTimeMillis()
    messageDao.markMessageAsRead("msg_test_1", readTimestamp)
    val updatedMessage = messageDao.getMessagesForChat("chat_aarav").first().first()
    assertEquals(MessageStatus.READ.name, updatedMessage.status)
    assertTrue(updatedMessage.isRead)
    assertEquals(readTimestamp, updatedMessage.readAt)
  }

  @Test
  fun `send message flow and message addition`() {
    val testChatId = "chat_priya"
    val initialMsgCount = ChatMitraRepository.messages.value[testChatId]?.size ?: 0

    ChatMitraRepository.sendMessage(
      chatId = testChatId,
      text = "Unit test message verification",
      type = MessageType.TEXT
    )

    val updatedMsgs = ChatMitraRepository.messages.value[testChatId]
    assertNotNull(updatedMsgs)
    assertEquals(initialMsgCount + 1, updatedMsgs?.size)
    assertEquals("Unit test message verification", updatedMsgs?.last()?.text)
  }

  @Test
  fun `post story and group creation`() {
    val initialStories = ChatMitraRepository.myStories.value.size
    ChatMitraRepository.postStory("Test Story text", 0)
    assertEquals(initialStories + 1, ChatMitraRepository.myStories.value.size)

    val initialChats = ChatMitraRepository.chats.value.size
    ChatMitraRepository.createGroup("Sprint Team", "Testing description", listOf("c_priya", "c_rahul"))
    assertEquals(initialChats + 1, ChatMitraRepository.chats.value.size)
  }

  @Test
  fun `webrtc call state machine`() {
    val contact = ChatMitraRepository.contacts.value.first()
    ChatMitraRepository.startCall(contact, isVideo = true)

    val activeSession = ChatMitraRepository.activeCall.value
    assertNotNull(activeSession)
    assertEquals(contact.id, activeSession?.contact?.id)
    assertTrue(activeSession?.isVideo == true)

    ChatMitraRepository.endCall()
    assertEquals(null, ChatMitraRepository.activeCall.value)
  }

  @Test
  fun `authentication repository initialization and state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepo = AuthenticationRepository.getInstance()
    assertNotNull(authRepo)
    val clientId = authRepo.resolveServerClientId(context)
    assertNotNull(clientId)
    assertTrue(clientId!!.contains("googleusercontent.com"))
  }

  @Test
  fun `user search repository query and fallback`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val searchRepo = UserSearchRepository.getInstance(userDAO = database.userDAO())
    val searchResult = searchRepo.searchUsers("Priya", context).getOrNull()
    assertNotNull(searchResult)
    assertTrue("Should find matching contact Priya", searchResult!!.any { it.name.contains("Priya") })
  }

  @Test
  fun `user status repository set status and local sync`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val statusRepo = UserStatusRepository.getInstance()
    val result = statusRepo.setUserStatus("user_me", "Hamza", "Working on new AI Studio features 💻", 1, context)
    assertTrue(result.isSuccess)
    val myStories = ChatMitraRepository.myStories.value
    assertTrue("Status should be synced to local stories", myStories.any { it.text.contains("features") })
  }

  @Test
  fun `media repository upload fallback`() = runBlocking {
    val mediaRepo = MediaRepository.getInstance()
    val testBytes = "mock_image_bytes".toByteArray()
    val result = mediaRepo.uploadImageBytes(testBytes, "sample.jpg", "chat_test")
    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull())
  }

  @Test
  fun `chat viewmodel message flow`() = runBlocking {
    val authRepo = AuthenticationRepository.getInstance()
    val mediaRepo = MediaRepository.getInstance()
    val chatViewModel = ChatViewModel(
      messageDAO = database.messageDAO(),
      authRepository = authRepo,
      mediaRepository = mediaRepo
    )

    chatViewModel.loadChat("chat_unit_test")
    val job = chatViewModel.sendTextMessage("chat_unit_test", "Hello from ChatViewModel")
    job.join()

    val messages = database.messageDAO().getMessagesForChat("chat_unit_test").first()
    assertEquals(1, messages.size)
    assertEquals("Hello from ChatViewModel", messages.first().text)
  }

  @Test
  fun `webrtc signaling manager initiate and state transitions`() {
    val signaling = WebRtcSignalingManager.getInstance()
    signaling.initiateCall("call_test_123", isVideo = true)

    val state = signaling.signalingState.value
    assertEquals("call_test_123", state.callId)
    assertTrue(state.isVideo)
    assertNotNull(state.localDescription)
    assertEquals(SdpType.OFFER, state.localDescription?.type)

    signaling.toggleAudioMute()
    assertTrue(signaling.signalingState.value.isAudioMuted)

    signaling.toggleMinimize()
    assertTrue(signaling.signalingState.value.isMinimized)

    signaling.endCall()
    assertEquals(PeerConnectionState.CLOSED, signaling.signalingState.value.peerConnectionState)
  }

  @Test
  fun `user block repository blocks user and rejects communication`() = runBlocking {
    val blockRepo = UserBlockRepository()
    val rahulChat = ChatMitraRepository.chats.value.first { it.id == "chat_rahul" }
    val rahulContact = rahulChat.participants.first()

    org.junit.Assert.assertFalse("User should not be blocked initially", blockRepo.isUserBlocked(rahulContact.id))

    blockRepo.blockUser("user_me", rahulContact.id)
    try {
      assertTrue("User should be reported as blocked", blockRepo.isUserBlocked(rahulContact.id))

      val authRepo = AuthenticationRepository.getInstance()
      val mediaRepo = MediaRepository.getInstance()
      val chatViewModel = ChatViewModel(
        messageDAO = database.messageDAO(),
        authRepository = authRepo,
        mediaRepository = mediaRepo,
        userBlockRepository = blockRepo
      )

      // Attempting to send message to blocked contact sets error in UI state
      chatViewModel.sendTextMessage("chat_rahul", "Blocked message test")
      assertEquals("Cannot send message: Contact is blocked", chatViewModel.uiState.value.error)
    } finally {
      blockRepo.unblockUser("user_me", rahulContact.id)
      ChatMitraRepository.clearBlockedContactsForTesting()
    }
  }

  @Test
  fun `locked chats authentication and rate limiting lockout`() {
    try {
      ChatMitraRepository.relockChats()

      // 1. Correct PIN unlocks
      val correctResult = ChatMitraRepository.authenticateLockedChats("1234")
      assertTrue("Correct PIN unlocks locked chats", correctResult.isSuccess)
      assertTrue(ChatMitraRepository.isLockedChatsAuthenticated.value)

      ChatMitraRepository.relockChats()
      org.junit.Assert.assertFalse(ChatMitraRepository.isLockedChatsAuthenticated.value)

      // 2. 5 wrong attempts trigger lockout
      for (i in 1..4) {
        val failResult = ChatMitraRepository.authenticateLockedChats("wrong_$i")
        org.junit.Assert.assertFalse("Wrong attempt should fail", failResult.isSuccess)
      }

      val fifthResult = ChatMitraRepository.authenticateLockedChats("wrong_5")
      org.junit.Assert.assertFalse("5th wrong attempt should fail", fifthResult.isSuccess)
      assertTrue("Cooldown lockout should be activated", ChatMitraRepository.cooldownUntilMillis.value > System.currentTimeMillis())

      // Locked during cooldown
      val lockedResult = ChatMitraRepository.authenticateLockedChats("1234")
      org.junit.Assert.assertFalse("Even correct PIN should be rejected during cooldown lockout", lockedResult.isSuccess)
    } finally {
      ChatMitraRepository.resetCooldownForTesting()
    }
  }

  @Test
  fun `disappearing messages duration assignment and expiration timestamp`() {
    val chatId = "chat_ananya"
    ChatMitraRepository.setChatDisappearingDuration(chatId, com.example.data.model.DisappearingDuration.HOURS_24)

    try {
      ChatMitraRepository.sendMessage(chatId, "This message will self-destruct in 24 hours")

      val sentMessages = ChatMitraRepository.messages.value[chatId]
      assertNotNull(sentMessages)
      val lastMsg = sentMessages?.lastOrNull()
      assertNotNull(lastMsg)
      assertNotNull(lastMsg?.expiresAt)
      assertTrue("Message expiresAt should be in the future", (lastMsg?.expiresAt ?: 0L) > (lastMsg?.timestamp ?: 0L))
    } finally {
      ChatMitraRepository.setChatDisappearingDuration(chatId, com.example.data.model.DisappearingDuration.OFF)
    }
  }

  @Test
  fun `two step verification enable and audit logging`() {
    ChatMitraRepository.enableTwoStepVerification("987654", "recovery@chatmitra.com")
    val settings = ChatMitraRepository.privacySettings.value
    assertTrue("Two-step verification should be enabled", settings.twoStepVerificationEnabled)
    assertEquals("987654", settings.twoStepVerificationPin)
    assertEquals("recovery@chatmitra.com", settings.recoveryEmail)

    val auditEvents = ChatMitraRepository.securityAuditEvents.value
    assertTrue("Audit event should be recorded", auditEvents.any { it.title.contains("Two-Step") })

    ChatMitraRepository.disableTwoStepVerification()
    org.junit.Assert.assertFalse("Two-step verification should be disabled", ChatMitraRepository.privacySettings.value.twoStepVerificationEnabled)
  }

  @Test
  fun `friend request lifecycle and contact synchronization`() {
    val sender = UserProfile(id = "user_me", name = "Test Me", username = "testme")
    val recipient = UserProfile(id = "user_friend", name = "Friend", username = "friend123")

    val sent = FriendContactRepository.sendFriendRequest(targetUser = recipient, myUser = sender)
    assertTrue("Friend request should be sent successfully", sent)

    val requests = FriendContactRepository.friendRequests.value
    assertTrue("Friend request should exist", requests.any { it.senderId == sender.id && it.receiverId == recipient.id })

    val incomingReq = requests.firstOrNull { it.senderId == sender.id }
    if (incomingReq != null) {
      FriendContactRepository.acceptFriendRequest(incomingReq.id)
      assertTrue("Friend request should be marked accepted", FriendContactRepository.friendRequests.value.any { it.id == incomingReq.id && it.status == com.example.data.model.FriendRequestStatus.ACCEPTED })
    }
  }

  @Test
  fun `group video calling 8-participant full mesh capacity limit`() {
    val host = UserProfile(id = "host_1", name = "Host")
    GroupCallingRepository.startGroupCall(title = "Test Room", isVideo = true, host = host)

    val session = GroupCallingRepository.activeGroupCall.value
    assertNotNull(session)
    assertEquals(1, session?.participants?.size)

    // Add up to 8 participants
    for (i in 2..8) {
      val added = GroupCallingRepository.addParticipant(
        UserProfile(id = "p_$i", name = "Caller $i")
      )
      assertTrue("Participant $i should be admitted", added)
    }

    assertEquals(8, GroupCallingRepository.activeGroupCall.value?.participants?.size)

    // Attempt to add 9th participant - should be rejected due to mesh bandwidth limit
    val rejected = GroupCallingRepository.addParticipant(
      UserProfile(id = "p_9", name = "Caller 9")
    )
    assertFalse("9th participant must be rejected under 8-caller limit", rejected)

    GroupCallingRepository.endGroupCall()
    assertEquals(null, GroupCallingRepository.activeGroupCall.value)
  }

  @Test
  fun `low data mode bitrate and settings adaptation`() {
    LowDataRepository.setLowDataMode(false)
    assertEquals(1500, LowDataRepository.getTargetCallBitrateKbps(isLowDataActive = false, qualityMode = "HD"))
    assertEquals(500, LowDataRepository.getTargetCallBitrateKbps(isLowDataActive = false, qualityMode = "SD"))

    LowDataRepository.setLowDataMode(true)
    assertTrue(LowDataRepository.settings.value.isLowDataModeEnabled)
    assertEquals(150, LowDataRepository.getTargetCallBitrateKbps(isLowDataActive = true, qualityMode = "HD"))
    assertEquals(150, LowDataRepository.getTargetCallBitrateKbps(isLowDataActive = true, qualityMode = "LOW_DATA"))

    LowDataRepository.setLowDataMode(false)
  }

  @Test
  fun `admin moderation authentication, zero-trust report resolution and audit logging`() {
    val badAuth = AdminModerationRepository.authenticateAdmin("wrong_pass")
    assertFalse("Incorrect password should fail", badAuth)

    val goodAuth = AdminModerationRepository.authenticateAdmin("admin2026")
    assertTrue("admin2026 should authenticate", goodAuth)
    assertTrue(AdminModerationRepository.isAdminAuthenticated.value)

    val testReportId = "rep_101"
    AdminModerationRepository.resolveReport(testReportId, actionTaken = true, resolutionNotes = "Suspended for harassment")

    val logs = AdminModerationRepository.auditLogs.value
    assertTrue("Audit log must record action", logs.any { it.action.contains("RESOLVE_REPORT") })

    AdminModerationRepository.suspendUser("abusive_user_99", "Violation")
    assertTrue(AdminModerationRepository.suspendedUserIds.value.containsKey("abusive_user_99"))

    AdminModerationRepository.unsuspendUser("abusive_user_99")
    assertFalse(AdminModerationRepository.suspendedUserIds.value.containsKey("abusive_user_99"))

    AdminModerationRepository.logoutAdmin()
    assertFalse(AdminModerationRepository.isAdminAuthenticated.value)
  }

  @Test
  fun `network reliability message idempotency and retry queue`() {
    val clientMsgId = "msg_uuid_unique_123"
    val isFirst = NetworkReliabilityManager.registerClientMessageId(clientMsgId)
    assertTrue("First send of message ID should be allowed", isFirst)

    val isDuplicate = NetworkReliabilityManager.registerClientMessageId(clientMsgId)
    assertFalse("Duplicate message ID should be suppressed", isDuplicate)

    NetworkReliabilityManager.enqueueMessage("chat_test", "Offline queued content")
    val queue = NetworkReliabilityManager.queuedMessages.value
    assertTrue("Retry queue should contain queued message", queue.any { it.chatId == "chat_test" })

    var sentMessageText = ""
    NetworkReliabilityManager.flushRetryQueue { msg ->
      sentMessageText = msg.text
    }
    assertEquals("Offline queued content", sentMessageText)
    assertTrue("Queue should be empty after flush", NetworkReliabilityManager.queuedMessages.value.isEmpty())
  }

  @Test
  fun `hamza ai assistant pipeline and message history verification`() {
    val aiRepo = com.example.data.repository.HamzaAiRepository
    aiRepo.clearChatHistory()

    val initialMsgs = aiRepo.messages.value
    assertTrue("Initial messages should not be empty", initialMsgs.isNotEmpty())
    assertFalse("Initial message should be from assistant", initialMsgs.first().isFromUser)
    assertTrue("Suggested prompt chips should be available", aiRepo.suggestedPromptChips.isNotEmpty())

    // Send a message to Hamza AI assistant
    val context = ApplicationProvider.getApplicationContext<Context>()
    aiRepo.sendMessage("Hello Hamza AI!", context)
    val currentMsgs = aiRepo.messages.value
    assertTrue("Messages should now contain user message", currentMsgs.any { it.text == "Hello Hamza AI!" && it.isFromUser })

    // Cancel in-flight generation
    aiRepo.cancelGeneration()
    assertFalse("Generation state should be false after cancel", aiRepo.isGenerating.value)

    // Clear chat history
    aiRepo.clearChatHistory()
    assertEquals("History should reset back to initial greeting", 1, aiRepo.messages.value.size)
  }
}
