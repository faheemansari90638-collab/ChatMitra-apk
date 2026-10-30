package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Real-time Firebase Firestore service for ChatMitra messaging.
 * Integrates /users/{userId}, /chats/{chatId}, and /chats/{chatId}/messages/{messageId}
 * adhering to zero-trust Firestore security rules and schemas.
 */
object FirestoreChatService {
  private const val TAG = "FirestoreChatService"
  private var appContext: Context? = null
  private var cachedFirestore: FirebaseFirestore? = null

  fun init(context: Context) {
    appContext = context.applicationContext
  }

  fun getFirestore(): FirebaseFirestore? {
    if (cachedFirestore != null) return cachedFirestore
    return try {
      val ctx = appContext
      val resId = ctx?.resources?.getIdentifier("firestore_database_id", "string", ctx.packageName) ?: 0
      val dbId = if (resId != 0) ctx?.getString(resId) else null
      val firestore = if (!dbId.isNullOrBlank()) {
        try {
          FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
        } catch (_: Exception) {
          FirebaseFirestore.getInstance(dbId)
        }
      } else {
        Firebase.firestore
      }
      cachedFirestore = firestore
      firestore
    } catch (e: Exception) {
      Log.w(TAG, "Error initializing Firestore: ${e.message}")
      null
    }
  }

  /**
   * Listen to real-time chat messages from Firestore: /chats/{chatId}/messages
   */
  fun listenToChatMessages(
    chatId: String,
    onMessagesUpdated: (List<Message>) -> Unit
  ): ListenerRegistration? {
    val firestore = getFirestore() ?: return null
    return try {
      firestore.collection("chats")
        .document(chatId)
        .collection("messages")
        .orderBy("timestamp")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Snapshot error for $chatId: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null) {
            val list = snapshot.documents.mapNotNull { doc ->
              try {
                val statusStr = doc.getString("status") ?: MessageStatus.SENT.name
                val typeStr = doc.getString("type") ?: MessageType.TEXT.name
                val status = try { MessageStatus.valueOf(statusStr) } catch (_: Exception) { MessageStatus.SENT }
                val type = try { MessageType.valueOf(typeStr) } catch (_: Exception) { MessageType.TEXT }
                Message(
                  id = doc.id,
                  chatId = chatId,
                  senderId = doc.getString("senderId") ?: "",
                  senderName = doc.getString("senderName") ?: "User",
                  text = doc.getString("text") ?: "",
                  timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                  status = status,
                  type = type,
                  mediaUri = doc.getString("mediaUri"),
                  mediaFileName = doc.getString("mediaFileName"),
                  mediaFileSize = doc.getString("mediaFileSize"),
                  mediaDurationSec = doc.getLong("mediaDurationSec")?.toInt() ?: 0,
                  replyToId = doc.getString("replyToId"),
                  replyToText = doc.getString("replyToText"),
                  replyToSender = doc.getString("replyToSender"),
                  isStarred = doc.getBoolean("isStarred") ?: false,
                  isRead = doc.getBoolean("isRead") ?: (status == MessageStatus.READ),
                  readAt = doc.getLong("readAt"),
                  expiresAt = doc.getLong("expiresAt")
                )
              } catch (e: Exception) {
                Log.w(TAG, "Failed to parse message ${doc.id}: ${e.message}")
                null
              }
            }
            if (list.isNotEmpty()) {
              onMessagesUpdated(list)
            }
          }
        }
    } catch (e: Exception) {
      Log.w(TAG, "Error attaching listener: ${e.message}")
      null
    }
  }

  /**
   * Writes message to /chats/{chatId}/messages/{messageId} and updates parent /chats/{chatId}
   */
  suspend fun sendMessage(chatId: String, message: Message): Boolean {
    val firestore = getFirestore() ?: return false
    return try {
      val messagePayload = hashMapOf<String, Any?>(
        "id" to message.id,
        "chatId" to chatId,
        "senderId" to message.senderId,
        "senderName" to message.senderName,
        "text" to message.text,
        "timestamp" to message.timestamp,
        "status" to message.status.name,
        "type" to message.type.name,
        "mediaUri" to message.mediaUri,
        "mediaFileName" to message.mediaFileName,
        "mediaFileSize" to message.mediaFileSize,
        "mediaDurationSec" to message.mediaDurationSec,
        "replyToId" to message.replyToId,
        "replyToText" to message.replyToText,
        "replyToSender" to message.replyToSender,
        "expiresAt" to message.expiresAt,
        "createdAt" to FieldValue.serverTimestamp()
      )

      firestore.collection("chats")
        .document(chatId)
        .collection("messages")
        .document(message.id)
        .set(messagePayload)
        .await()

      // Update parent chat document
      val chatPayload = hashMapOf<String, Any?>(
        "id" to chatId,
        "lastMessageText" to message.text,
        "lastMessageTimestamp" to message.timestamp,
        "lastSenderId" to message.senderId,
        "participantIds" to FieldValue.arrayUnion(message.senderId),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      firestore.collection("chats")
        .document(chatId)
        .set(chatPayload, SetOptions.merge())
        .await()

      true
    } catch (e: Exception) {
      Log.w(TAG, "Failed to write message to Firestore: ${e.message}")
      false
    }
  }

  /**
   * Update delivery or read receipt in Firestore: /chats/{chatId}/messages/{messageId}
   */
  suspend fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus): Boolean {
    val firestore = getFirestore() ?: return false
    return try {
      val updates = hashMapOf<String, Any>(
        "status" to status.name
      )
      if (status == MessageStatus.READ) {
        updates["isRead"] = true
        updates["readAt"] = System.currentTimeMillis()
      }
      firestore.collection("chats")
        .document(chatId)
        .collection("messages")
        .document(messageId)
        .update(updates)
        .await()
      true
    } catch (e: Exception) {
      Log.w(TAG, "Failed to update status for $messageId: ${e.message}")
      false
    }
  }

  /**
   * Sync user profile to Firestore: /users/{userId}
   */
  suspend fun syncUserProfile(user: UserProfile): Boolean {
    val firestore = getFirestore() ?: return false
    val currentAuthUid = FirebaseAuth.getInstance().currentUser?.uid ?: user.id
    return try {
      val profilePayload = hashMapOf<String, Any?>(
        "userId" to currentAuthUid,
        "displayName" to user.name,
        "about" to user.about,
        "phone" to user.phone,
        "photoUrl" to user.avatarEmoji,
        "isOnline" to user.isOnline,
        "lastSeen" to user.lastSeen,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      firestore.collection("users")
        .document(currentAuthUid)
        .set(profilePayload, SetOptions.merge())
        .await()
      true
    } catch (e: Exception) {
      Log.w(TAG, "Failed to sync profile: ${e.message}")
      false
    }
  }
}
