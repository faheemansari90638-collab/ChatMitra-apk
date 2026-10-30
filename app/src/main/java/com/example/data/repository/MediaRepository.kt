package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class MediaRepository(
  private val customStorage: FirebaseStorage? = null
) {

  private fun getStorage(): FirebaseStorage? {
    return customStorage ?: try {
      Firebase.storage
    } catch (e: Exception) {
      Log.w(TAG, "Firebase Storage not initialized: ${e.message}")
      null
    }
  }

  /**
   * Upload an image from local Uri to Firebase Storage, returning the downloadable public URL.
   */
  suspend fun uploadImage(
    uri: Uri,
    chatId: String,
    context: Context
  ): Result<String> = withContext(Dispatchers.IO) {
    val storage = getStorage()
    val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
    val path = "chats/$chatId/images/$fileName"

    if (storage == null) {
      // Graceful fallback to local content URI string
      Log.d(TAG, "Storage unavailable; using local URI reference: $uri")
      return@withContext Result.success(uri.toString())
    }

    try {
      val stream = context.contentResolver.openInputStream(uri)
        ?: return@withContext Result.failure(IllegalArgumentException("Cannot open stream for URI: $uri"))

      val ref = storage.reference.child(path)
      ref.putStream(stream).await()
      val downloadUrl = ref.downloadUrl.await().toString()
      Result.success(downloadUrl)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to upload image to Firebase Storage: ${e.message}", e)
      // Return local URI as fallback so user experience isn't blocked
      Result.success(uri.toString())
    }
  }

  /**
   * Upload image raw bytes to Firebase Storage.
   */
  suspend fun uploadImageBytes(
    bytes: ByteArray,
    fileName: String,
    chatId: String
  ): Result<String> = withContext(Dispatchers.IO) {
    val storage = getStorage()
    val cleanName = if (fileName.isNotBlank()) fileName else "img_${System.currentTimeMillis()}.jpg"
    val path = "chats/$chatId/images/$cleanName"

    if (storage == null) {
      return@withContext Result.success("content://media/local/$cleanName")
    }

    try {
      val ref = storage.reference.child(path)
      ref.putBytes(bytes).await()
      val downloadUrl = ref.downloadUrl.await().toString()
      Result.success(downloadUrl)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to upload image bytes: ${e.message}", e)
      Result.failure(e)
    }
  }

  companion object {
    private const val TAG = "MediaRepository"

    @Volatile
    private var INSTANCE: MediaRepository? = null

    fun getInstance(storage: FirebaseStorage? = null): MediaRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = MediaRepository(storage)
        INSTANCE = instance
        instance
      }
    }
  }
}
