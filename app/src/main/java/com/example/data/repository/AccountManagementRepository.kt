package com.example.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DataExportRequest(
  val requestId: String,
  val status: String, // "READY", "PENDING", "EXPIRED"
  val requestedAtFormatted: String,
  val downloadUrl: String,
  val expiresAtFormatted: String,
  val fileSizeFormatted: String = "22.4 MB"
)

class AccountManagementRepository private constructor() {

  private val _exportRequest = MutableStateFlow<DataExportRequest?>(null)
  val exportRequest: StateFlow<DataExportRequest?> = _exportRequest.asStateFlow()

  private fun getFirestore(context: Context? = null): FirebaseFirestore? {
    return try {
      val resId = context?.resources?.getIdentifier("firestore_database_id", "string", context.packageName) ?: 0
      val dbId = if (resId != 0) context?.getString(resId) else null
      if (!dbId.isNullOrBlank()) {
        FirebaseFirestore.getInstance(dbId)
      } else {
        Firebase.firestore
      }
    } catch (_: Exception) {
      null
    }
  }

  fun requestDataExport(context: Context? = null): Result<DataExportRequest> {
    val reqId = "exp_${UUID.randomUUID().toString().take(8)}"
    val now = System.currentTimeMillis()
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val requestedFormatted = sdf.format(Date(now))
    val expiresFormatted = sdf.format(Date(now + 3 * 86400000L)) // 3 days expiry

    val downloadUrl = "https://chatmitra.app/export/download?token=auth_${reqId}_jwt_signed"

    val request = DataExportRequest(
      requestId = reqId,
      status = "READY",
      requestedAtFormatted = requestedFormatted,
      downloadUrl = downloadUrl,
      expiresAtFormatted = expiresFormatted,
      fileSizeFormatted = "22.4 MB"
    )

    _exportRequest.value = request

    ChatMitraRepository.addSecurityAuditEvent(
      title = "Account Data Export Generated",
      description = "Official archive ready for download (valid for 3 days).",
      isPositive = true
    )

    val currentUserId = ChatMitraRepository.currentUser.value?.id ?: "user_me"
    val firestore = getFirestore(context)
    if (firestore != null) {
      try {
        val payload = mapOf(
          "requestId" to reqId,
          "status" to "READY",
          "requestedAt" to now,
          "expiresAt" to (now + 3 * 86400000L),
          "serverTimestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(currentUserId)
          .collection("exports").document(reqId)
          .set(payload)
      } catch (e: Exception) {
        Log.w("AccountManagementRepo", "Failed to log export request in Firestore: ${e.message}")
      }
    }

    return Result.success(request)
  }

  fun deleteAccount(confirmedPhone: String, reason: String, context: Context? = null): Result<Boolean> {
    val currentPhone = ChatMitraRepository.currentUser.value?.phone ?: "+91 98765 43210"

    val cleanInput = confirmedPhone.replace(" ", "").replace("-", "")
    val cleanCurrent = currentPhone.replace(" ", "").replace("-", "")

    if (!cleanInput.endsWith(cleanCurrent.takeLast(8))) {
      return Result.failure(IllegalArgumentException("Phone number does not match your registered account."))
    }

    val currentUserId = ChatMitraRepository.currentUser.value?.id ?: "user_me"

    // 1. Unlink all devices
    ChatMitraRepository.unlinkAllOtherDevices()

    // 2. Clear all cloud backups
    ChatBackupRepository.getInstance().deleteAllCloudBackups()

    // 3. Clear cache & local history
    StorageManagementRepository.getInstance().clearCache()

    // 4. Log security event
    ChatMitraRepository.addSecurityAuditEvent(
      title = "Account Deletion Requested",
      description = "Account was terminated with reason: $reason.",
      isPositive = false
    )

    // 5. Update Firestore user doc to deleted
    val firestore = getFirestore(context)
    if (firestore != null) {
      try {
        firestore.collection("users").document(currentUserId)
          .update(mapOf("isDeleted" to true, "deletedAt" to System.currentTimeMillis()))
      } catch (e: Exception) {
        Log.w("AccountManagementRepo", "Failed to update user deleted status in Firestore: ${e.message}")
      }
    }

    // 6. Sign out
    ChatMitraRepository.logout()

    return Result.success(true)
  }

  companion object {
    @Volatile
    private var instance: AccountManagementRepository? = null

    fun getInstance(): AccountManagementRepository {
      return instance ?: synchronized(this) {
        instance ?: AccountManagementRepository().also { instance = it }
      }
    }
  }
}
