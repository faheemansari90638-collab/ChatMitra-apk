package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class UserStatusUpdate(
  val userId: String = "",
  val userName: String = "",
  val statusText: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val bgGradientIndex: Int = 0
)

class UserStatusRepository(
  private val customFirestore: FirebaseFirestore? = null
) {

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
   * Sets or updates the user's text-based status in Firestore.
   */
  suspend fun setUserStatus(
    userId: String,
    userName: String,
    statusText: String,
    bgGradientIndex: Int = 0,
    context: Context? = null
  ): Result<Unit> {
    if (userId.isBlank() || statusText.isBlank()) {
      return Result.failure(IllegalArgumentException("userId and statusText must not be empty"))
    }

    val firestore = getFirestore(context)
    val statusPayload = mapOf(
      "userId" to userId,
      "userName" to userName,
      "statusText" to statusText.trim(),
      "bgGradientIndex" to bgGradientIndex,
      "timestamp" to System.currentTimeMillis(),
      "serverTimestamp" to FieldValue.serverTimestamp()
    )

    if (firestore != null) {
      try {
        firestore.collection("user_statuses")
          .document(userId)
          .set(statusPayload, SetOptions.merge())
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to persist status in Firestore: ${e.message}")
      }
    }

    // Sync locally with ChatMitra repository
    ChatMitraRepository.postStory(statusText.trim(), bgGradientIndex)

    return Result.success(Unit)
  }

  /**
   * Real-time listener for all friends' status updates.
   */
  fun observeStatuses(context: Context? = null): Flow<Map<String, UserStatusUpdate>> = callbackFlow {
    val firestore = getFirestore(context)
    if (firestore == null) {
      // Return local stories mapped by userId
      val localMap = ChatMitraRepository.statusStories.value.associate { story ->
        story.userId to UserStatusUpdate(
          userId = story.userId,
          userName = story.userName,
          statusText = story.text,
          timestamp = story.timestamp,
          bgGradientIndex = story.bgGradientIndex
        )
      }
      trySend(localMap)
      close()
      return@callbackFlow
    }

    val registration = firestore.collection("user_statuses")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Error observing statuses: ${error.message}")
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val map = mutableMapOf<String, UserStatusUpdate>()
          for (doc in snapshot.documents) {
            val uId = doc.getString("userId") ?: doc.id
            val uName = doc.getString("userName") ?: ""
            val text = doc.getString("statusText") ?: ""
            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
            val bg = doc.getLong("bgGradientIndex")?.toInt() ?: 0

            map[uId] = UserStatusUpdate(
              userId = uId,
              userName = uName,
              statusText = text,
              timestamp = time,
              bgGradientIndex = bg
            )
          }
          trySend(map)
        }
      }

    awaitClose {
      registration.remove()
    }
  }

  companion object {
    private const val TAG = "UserStatusRepository"

    @Volatile
    private var INSTANCE: UserStatusRepository? = null

    fun getInstance(firestore: FirebaseFirestore? = null): UserStatusRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = UserStatusRepository(firestore)
        INSTANCE = instance
        instance
      }
    }
  }
}
