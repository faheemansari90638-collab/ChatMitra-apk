package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.UserDAO
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserSearchRepository(
  private val customFirestore: FirebaseFirestore? = null,
  private val userDAO: UserDAO? = null
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
      Log.w(TAG, "Firestore not initialized or unavailable: ${e.message}")
      null
    }
  }

  /**
   * Search users by name, username, or email.
   * Queries Firestore first, and gracefully combines or falls back to local UserDAO / contacts.
   */
  suspend fun searchUsers(
    query: String,
    context: Context? = null
  ): Result<List<UserProfile>> {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) {
      return Result.success(emptyList())
    }

    val firestore = getFirestore(context)
    val results = mutableListOf<UserProfile>()

    if (firestore != null) {
      try {
        // Query by exact or prefix name
        val nameSnapshot = firestore.collection("users")
          .whereGreaterThanOrEqualTo("name", cleanQuery)
          .whereLessThanOrEqualTo("name", cleanQuery + "\uf8ff")
          .limit(20)
          .get()
          .await()

        for (doc in nameSnapshot.documents) {
          val id = doc.id
          val name = doc.getString("name") ?: ""
          val phone = doc.getString("phone") ?: ""
          val about = doc.getString("about") ?: "Available on ChatMitra"
          val avatarColorHex = doc.getLong("avatarColorHex") ?: 0xFF00A884
          val avatarEmoji = doc.getString("avatarEmoji") ?: "👤"
          val isOnline = doc.getBoolean("isOnline") ?: false

          results.add(
            UserProfile(
              id = id,
              name = name,
              phone = phone,
              about = about,
              avatarColorHex = avatarColorHex,
              avatarEmoji = avatarEmoji,
              isOnline = isOnline
            )
          )
        }

        // Query by email if it looks like an email address
        if (cleanQuery.contains("@")) {
          val emailSnapshot = firestore.collection("users")
            .whereEqualTo("email", cleanQuery.lowercase())
            .limit(10)
            .get()
            .await()

          for (doc in emailSnapshot.documents) {
            val id = doc.id
            if (results.none { it.id == id }) {
              val name = doc.getString("name") ?: cleanQuery.substringBefore("@")
              val phone = doc.getString("phone") ?: ""
              val about = doc.getString("about") ?: "Available on ChatMitra"
              val avatarColorHex = doc.getLong("avatarColorHex") ?: 0xFF3F51B5
              val avatarEmoji = doc.getString("avatarEmoji") ?: "👤"
              val isOnline = doc.getBoolean("isOnline") ?: false

              results.add(
                UserProfile(
                  id = id,
                  name = name,
                  phone = phone,
                  about = about,
                  avatarColorHex = avatarColorHex,
                  avatarEmoji = avatarEmoji,
                  isOnline = isOnline
                )
              )
            }
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Firestore query failed: ${e.message}, falling back to local dataset")
      }
    }

    // Include local contacts if Firestore returned empty or failed
    if (results.isEmpty()) {
      val localContacts = ChatMitraRepository.contacts.value.filter {
        it.name.contains(cleanQuery, ignoreCase = true) ||
          it.phone.contains(cleanQuery, ignoreCase = true)
      }
      results.addAll(localContacts)
    }

    return Result.success(results.distinctBy { it.id })
  }

  fun observeUserSearch(query: String, context: Context? = null): Flow<List<UserProfile>> = flow {
    emit(searchUsers(query, context).getOrDefault(emptyList()))
  }

  companion object {
    private const val TAG = "UserSearchRepository"

    @Volatile
    private var INSTANCE: UserSearchRepository? = null

    fun getInstance(
      firestore: FirebaseFirestore? = null,
      userDAO: UserDAO? = null
    ): UserSearchRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = UserSearchRepository(firestore, userDAO)
        INSTANCE = instance
        instance
      }
    }
  }
}
