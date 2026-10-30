package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.Message
import com.example.data.MessageDAO
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.repository.AuthenticationRepository
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.MediaRepository
import com.example.data.repository.UserBlockRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ChatUiState(
  val currentChatId: String = "",
  val messages: List<Message> = emptyList(),
  val isLoading: Boolean = false,
  val isUploadingMedia: Boolean = false,
  val error: String? = null
)

class ChatViewModel(
  private val messageDAO: MessageDAO,
  private val authRepository: AuthenticationRepository,
  private val mediaRepository: MediaRepository = MediaRepository.getInstance(),
  private val userBlockRepository: UserBlockRepository = UserBlockRepository.getInstance(),
  private val customFirestore: FirebaseFirestore? = null
) : ViewModel() {

  private val _uiState = MutableStateFlow(ChatUiState())
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  private var localObserverJob: Job? = null
  private var firestoreRegistration: ListenerRegistration? = null

  private fun getFirestore(context: Context? = null): FirebaseFirestore? {
    return customFirestore ?: try {
      val resId = context?.resources?.getIdentifier("firestore_database_id", "string", context.packageName) ?: 0
      val dbId = if (resId != 0) context?.getString(resId) else null
      if (!dbId.isNullOrBlank()) {
        FirebaseFirestore.getInstance(dbId)
      } else {
        Firebase.firestore
      }
    } catch (e: Exception) {
      Log.w(TAG, "Firestore not initialized: ${e.message}")
      null
    }
  }

  /**
   * Load chat history reactively from local MessageDAO and attach Firestore real-time listener.
   */
  fun loadChat(chatId: String, context: Context? = null) {
    if (_uiState.value.currentChatId == chatId && _uiState.value.messages.isNotEmpty()) {
      return
    }

    firestoreRegistration?.remove()
    localObserverJob?.cancel()

    _uiState.value = _uiState.value.copy(
      currentChatId = chatId,
      isLoading = true,
      error = null
    )

    // 1. Observe local Room Database
    localObserverJob = viewModelScope.launch {
      messageDAO.getMessagesForChat(chatId).collectLatest { roomMessages ->
        _uiState.value = _uiState.value.copy(
          messages = roomMessages,
          isLoading = false
        )
      }
    }

    // 2. Attach Firestore real-time snapshot listener
    val firestore = getFirestore(context)
    if (firestore != null) {
      firestoreRegistration = firestore.collection("chats")
        .document(chatId)
        .collection("messages")
        .orderBy("timestamp")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Firestore listener error for chat $chatId: ${error.message}")
            return@addSnapshotListener
          }

          if (snapshot != null && !snapshot.isEmpty) {
            viewModelScope.launch {
              val incomingMessages = snapshot.documents.mapNotNull { doc ->
                try {
                  val id = doc.id
                  val senderId = doc.getString("senderId") ?: ""
                  val senderName = doc.getString("senderName") ?: "User"
                  val text = doc.getString("text") ?: ""
                  val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
                  val status = doc.getString("status") ?: MessageStatus.DELIVERED.name
                  val type = doc.getString("type") ?: MessageType.TEXT.name
                  val mediaUri = doc.getString("mediaUri")
                  val mediaFileName = doc.getString("mediaFileName")
                  val mediaFileSize = doc.getString("mediaFileSize")
                  val mediaDurationSec = doc.getLong("mediaDurationSec")?.toInt() ?: 0
                  val isStarred = doc.getBoolean("isStarred") ?: false
                  val isRead = doc.getBoolean("isRead") ?: (status == MessageStatus.READ.name)
                  val readAt = doc.getLong("readAt")

                  Message(
                    id = id,
                    chatId = chatId,
                    senderId = senderId,
                    senderName = senderName,
                    text = text,
                    timestamp = time,
                    status = status,
                    type = type,
                    mediaUri = mediaUri,
                    mediaFileName = mediaFileName,
                    mediaFileSize = mediaFileSize,
                    mediaDurationSec = mediaDurationSec,
                    isStarred = isStarred,
                    isRead = isRead,
                    readAt = readAt
                  )
                } catch (e: Exception) {
                  null
                }
              }
              if (incomingMessages.isNotEmpty()) {
                messageDAO.insertMessages(incomingMessages)
              }
            }
          }
        }
    }
  }

  /**
   * Send text message with real-time Firestore sync and Room local storage.
   */
  fun sendTextMessage(
    chatId: String,
    text: String,
    replyToId: String? = null,
    replyToText: String? = null,
    replyToSender: String? = null,
    context: Context? = null
  ): Job {
    val cleanText = text.trim()
    if (cleanText.isBlank()) return Job().apply { complete() }

    val currentChat = ChatMitraRepository.chats.value.find { it.id == chatId }
    val recipient = currentChat?.participants?.firstOrNull()
    if (recipient != null && userBlockRepository.isUserBlocked(recipient.id)) {
      _uiState.value = _uiState.value.copy(error = "Cannot send message: Contact is blocked")
      return Job().apply { complete() }
    }

    val currentUser = authRepository.currentUser
    val senderId = currentUser?.uid ?: ChatMitraRepository.currentUser.value?.id ?: "user_me"
    val senderName = currentUser?.displayName ?: ChatMitraRepository.currentUser.value?.name ?: "You"

    val messageId = "msg_${UUID.randomUUID()}"
    val message = Message(
      id = messageId,
      chatId = chatId,
      senderId = senderId,
      senderName = senderName,
      text = cleanText,
      timestamp = System.currentTimeMillis(),
      status = MessageStatus.SENT.name,
      type = MessageType.TEXT.name,
      replyToId = replyToId,
      replyToText = replyToText,
      replyToSender = replyToSender
    )

    return viewModelScope.launch {
      // Save locally to Room
      messageDAO.insertMessage(message)

      // Also sync to ChatMitraRepository in-memory state
      ChatMitraRepository.sendMessage(
        chatId = chatId,
        text = cleanText
      )

      // Sync to Firestore backend
      val firestore = getFirestore(context)
      if (firestore != null && authRepository.isAuthenticated) {
        val payload = mapOf(
          "id" to messageId,
          "chatId" to chatId,
          "senderId" to senderId,
          "senderName" to senderName,
          "text" to cleanText,
          "timestamp" to message.timestamp,
          "status" to message.status,
          "type" to message.type,
          "replyToId" to replyToId,
          "replyToText" to replyToText,
          "replyToSender" to replyToSender,
          "serverTimestamp" to FieldValue.serverTimestamp()
        )

        try {
          kotlinx.coroutines.withTimeoutOrNull(2000L) {
            firestore.collection("chats")
              .document(chatId)
              .collection("messages")
              .document(messageId)
              .set(payload)
              .await()
          }
        } catch (e: Exception) {
          Log.w(TAG, "Failed to upload message to Firestore: ${e.message}")
        }
      }
    }
  }

  /**
   * Send image message using MediaRepository to upload to Firebase Storage first.
   */
  fun sendImageMessage(
    chatId: String,
    imageUri: Uri,
    context: Context
  ) {
    val currentChat = ChatMitraRepository.chats.value.find { it.id == chatId }
    val recipient = currentChat?.participants?.firstOrNull()
    if (recipient != null && userBlockRepository.isUserBlocked(recipient.id)) {
      _uiState.value = _uiState.value.copy(error = "Cannot send media: Contact is blocked")
      return
    }

    _uiState.value = _uiState.value.copy(isUploadingMedia = true)

    viewModelScope.launch {
      val uploadResult = mediaRepository.uploadImage(imageUri, chatId, context)
      _uiState.value = _uiState.value.copy(isUploadingMedia = false)

      val downloadUrl = uploadResult.getOrDefault(imageUri.toString())
      val currentUser = authRepository.currentUser
      val senderId = currentUser?.uid ?: ChatMitraRepository.currentUser.value?.id ?: "user_me"
      val senderName = currentUser?.displayName ?: ChatMitraRepository.currentUser.value?.name ?: "You"

      val messageId = "msg_${UUID.randomUUID()}"
      val message = Message(
        id = messageId,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        text = "📷 Photo",
        timestamp = System.currentTimeMillis(),
        status = MessageStatus.SENT.name,
        type = MessageType.IMAGE.name,
        mediaUri = downloadUrl
      )

      messageDAO.insertMessage(message)

      val firestore = getFirestore(context)
      if (firestore != null) {
        val payload = mapOf(
          "id" to messageId,
          "chatId" to chatId,
          "senderId" to senderId,
          "senderName" to senderName,
          "text" to "📷 Photo",
          "timestamp" to message.timestamp,
          "status" to message.status,
          "type" to message.type,
          "mediaUri" to downloadUrl,
          "serverTimestamp" to FieldValue.serverTimestamp()
        )
        try {
          firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .set(payload)
            .await()
        } catch (e: Exception) {
          Log.w(TAG, "Failed to sync image message to Firestore: ${e.message}")
        }
      }
    }
  }

  fun toggleStar(messageId: String) {
    viewModelScope.launch {
      messageDAO.toggleStar(messageId)
    }
  }

  fun deleteMessage(messageId: String, forEveryone: Boolean, chatId: String, context: Context? = null) {
    viewModelScope.launch {
      if (forEveryone) {
        messageDAO.markDeletedForEveryone(messageId)
        val firestore = getFirestore(context)
        if (firestore != null) {
          try {
            firestore.collection("chats")
              .document(chatId)
              .collection("messages")
              .document(messageId)
              .update(
                mapOf(
                  "text" to "🚫 This message was deleted",
                  "isDeletedForEveryone" to true
                )
              ).await()
          } catch (e: Exception) {
            Log.w(TAG, "Failed to mark deleted in Firestore: ${e.message}")
          }
        }
      } else {
        messageDAO.deleteMessageById(messageId)
      }
    }
  }

  fun markMessageAsRead(chatId: String, messageId: String, context: Context? = null) {
    val now = System.currentTimeMillis()
    viewModelScope.launch {
      messageDAO.markMessageAsRead(messageId, now)
      val firestore = getFirestore(context)
      if (firestore != null) {
        try {
          firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
              mapOf(
                "isRead" to true,
                "readAt" to now,
                "status" to MessageStatus.READ.name
              )
            ).await()
        } catch (e: Exception) {
          Log.w(TAG, "Failed to update read receipt in Firestore: ${e.message}")
        }
      }
    }
  }

  fun markChatAsRead(chatId: String, currentUserId: String, context: Context? = null) {
    val now = System.currentTimeMillis()
    viewModelScope.launch {
      messageDAO.markChatMessagesAsRead(chatId, currentUserId, now)
      val firestore = getFirestore(context)
      if (firestore != null) {
        try {
          val unreadDocs = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("isRead", false)
            .get()
            .await()

          for (doc in unreadDocs.documents) {
            val sender = doc.getString("senderId")
            if (sender != currentUserId) {
              doc.reference.update(
                mapOf(
                  "isRead" to true,
                  "readAt" to now,
                  "status" to MessageStatus.READ.name
                )
              )
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Failed to batch update read receipts in Firestore: ${e.message}")
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    firestoreRegistration?.remove()
    localObserverJob?.cancel()
  }

  companion object {
    private const val TAG = "ChatViewModel"
  }
}
