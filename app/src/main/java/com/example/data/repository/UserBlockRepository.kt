package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ReportCategory
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class UserBlockRepository(
  private val customFirestore: FirebaseFirestore? = null
) {

  private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
  val blockedUserIds: Flow<Set<String>> = _blockedUserIds.asStateFlow()

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

  init {
    // Initial sync with in-memory repo
    _blockedUserIds.value = ChatMitraRepository.blockedContactIds.value
  }

  fun isUserBlocked(userId: String): Boolean {
    return _blockedUserIds.value.contains(userId) || ChatMitraRepository.blockedContactIds.value.contains(userId)
  }

  suspend fun blockUser(
    currentUserId: String,
    blockedUserId: String,
    reason: String = "",
    context: Context? = null
  ): Result<Unit> {
    if (blockedUserId.isBlank()) {
      return Result.failure(IllegalArgumentException("Target user ID cannot be blank"))
    }

    // 1. Update in-memory set immediately for instantaneous UI updates
    _blockedUserIds.value = _blockedUserIds.value + blockedUserId
    ChatMitraRepository.blockContact(blockedUserId)

    // 2. Sync to Firestore backend
    val firestore = getFirestore(context)
    if (firestore != null && currentUserId.isNotBlank()) {
      try {
        val payload = mapOf(
          "blockedUserId" to blockedUserId,
          "reason" to reason,
          "blockedAt" to System.currentTimeMillis(),
          "serverTimestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("users")
          .document(currentUserId)
          .collection("blocked_users")
          .document(blockedUserId)
          .set(payload)
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to persist blocked user in Firestore: ${e.message}")
      }
    }

    return Result.success(Unit)
  }

  suspend fun unblockUser(
    currentUserId: String,
    blockedUserId: String,
    context: Context? = null
  ): Result<Unit> {
    if (blockedUserId.isBlank()) {
      return Result.failure(IllegalArgumentException("Target user ID cannot be blank"))
    }

    _blockedUserIds.value = _blockedUserIds.value - blockedUserId
    ChatMitraRepository.unblockContact(blockedUserId)

    val firestore = getFirestore(context)
    if (firestore != null && currentUserId.isNotBlank()) {
      try {
        firestore.collection("users")
          .document(currentUserId)
          .collection("blocked_users")
          .document(blockedUserId)
          .delete()
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to delete blocked user in Firestore: ${e.message}")
      }
    }

    return Result.success(Unit)
  }

  suspend fun reportUser(
    reporterUserId: String,
    reportedUserId: String,
    category: ReportCategory,
    details: String,
    includeLast5Messages: Boolean,
    chatId: String? = null,
    blockAfterReport: Boolean = true,
    context: Context? = null
  ): Result<Unit> {
    val firestore = getFirestore(context)
    val reportId = "rep_${System.currentTimeMillis()}"

    val messagesPayload = if (includeLast5Messages && chatId != null) {
      ChatMitraRepository.messages.value[chatId]?.takeLast(5)?.map { msg ->
        mapOf(
          "senderId" to msg.senderId,
          "text" to msg.text,
          "timestamp" to msg.timestamp
        )
      } ?: emptyList()
    } else {
      emptyList()
    }

    val reportData = mapOf(
      "reportId" to reportId,
      "reporterUserId" to reporterUserId,
      "reportedUserId" to reportedUserId,
      "category" to category.name,
      "details" to details,
      "chatId" to (chatId ?: ""),
      "messagesSnapshot" to messagesPayload,
      "timestamp" to System.currentTimeMillis(),
      "serverTimestamp" to FieldValue.serverTimestamp()
    )

    if (firestore != null) {
      try {
        firestore.collection("reports")
          .document(reportId)
          .set(reportData)
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Firestore report upload error: ${e.message}")
      }
    }

    if (blockAfterReport) {
      blockUser(reporterUserId, reportedUserId, "Reported as ${category.label}", context)
    }

    return Result.success(Unit)
  }

  suspend fun reportGroup(
    reporterUserId: String,
    groupId: String,
    category: ReportCategory,
    details: String,
    context: Context? = null
  ): Result<Unit> {
    val firestore = getFirestore(context)
    val reportId = "grp_rep_${System.currentTimeMillis()}"

    val reportData = mapOf(
      "reportId" to reportId,
      "reporterUserId" to reporterUserId,
      "groupId" to groupId,
      "category" to category.name,
      "details" to details,
      "timestamp" to System.currentTimeMillis(),
      "serverTimestamp" to FieldValue.serverTimestamp()
    )

    if (firestore != null) {
      try {
        firestore.collection("group_reports")
          .document(reportId)
          .set(reportData)
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Firestore group report error: ${e.message}")
      }
    }

    return Result.success(Unit)
  }

  companion object {
    private const val TAG = "UserBlockRepository"

    @Volatile
    private var INSTANCE: UserBlockRepository? = null

    fun getInstance(firestore: FirebaseFirestore? = null): UserBlockRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = UserBlockRepository(firestore)
        INSTANCE = instance
        instance
      }
    }
  }
}
