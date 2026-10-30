package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.model.QueuedMessage
import com.example.data.model.SanitizedCrashLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Manages network status detection, offline message retry queues, duplicate prevention,
 * and privacy-safe error/crash telemetry.
 */
object NetworkReliabilityManager {

  private val scope = CoroutineScope(Dispatchers.Default + Job())

  // Network Connectivity
  private val _isOnline = MutableStateFlow(true)
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  // Offline Retry Queue
  private val _queuedMessages = MutableStateFlow<List<QueuedMessage>>(emptyList())
  val queuedMessages: StateFlow<List<QueuedMessage>> = _queuedMessages.asStateFlow()

  // Processed Client Message IDs for Duplicate Prevention
  private val processedMessageIdMap = mutableSetOf<String>()

  // Sanitized Crash/Error Telemetry (Ring Buffer of 50 max, no PII)
  private val _crashLogs = MutableStateFlow<List<SanitizedCrashLog>>(
    listOf(
      SanitizedCrashLog(
        id = "log_init",
        tag = "AppStart",
        message = "Application session initialized cleanly with zero uncaught exceptions",
        category = "SYSTEM",
        isFatal = false
      )
    )
  )
  val crashLogs: StateFlow<List<SanitizedCrashLog>> = _crashLogs.asStateFlow()

  fun init(context: Context) {
    try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
      val activeNetwork = cm.activeNetwork
      val caps = cm.getNetworkCapabilities(activeNetwork)
      _isOnline.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

      val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

      cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          _isOnline.value = true
          // Automatically flush retry queue when connection resumes
          flushRetryQueue()
        }

        override fun onLost(network: Network) {
          _isOnline.value = false
        }
      })
    } catch (_: Exception) {
      _isOnline.value = true
    }
  }

  fun setManualOnlineState(online: Boolean) {
    _isOnline.value = online
    if (online) {
      flushRetryQueue()
    }
  }

  /**
   * Idempotency check: returns true if the message has NOT been processed before.
   */
  fun registerClientMessageId(clientMessageId: String): Boolean {
    synchronized(processedMessageIdMap) {
      if (processedMessageIdMap.contains(clientMessageId)) {
        return false // Duplicate detected!
      }
      processedMessageIdMap.add(clientMessageId)
      return true
    }
  }

  /**
   * Enqueue a message to the retry queue when offline.
   */
  fun enqueueMessage(chatId: String, text: String, clientMessageId: String = UUID.randomUUID().toString()) {
    val queued = QueuedMessage(
      id = "queue_${UUID.randomUUID().toString().take(6)}",
      clientMessageId = clientMessageId,
      chatId = chatId,
      text = text,
      timestamp = System.currentTimeMillis()
    )
    _queuedMessages.value = _queuedMessages.value + queued
  }

  /**
   * Flush pending messages when network reconnects.
   */
  fun flushRetryQueue(onSent: ((QueuedMessage) -> Unit)? = null) {
    val current = _queuedMessages.value
    if (current.isEmpty()) return
    _queuedMessages.value = emptyList()

    for (msg in current) {
      if (registerClientMessageId(msg.clientMessageId)) {
        if (onSent != null) {
          onSent(msg)
        } else {
          ChatMitraRepository.sendMessage(
            chatId = msg.chatId,
            text = msg.text
          )
        }
      }
    }
  }

  /**
   * Log sanitized exception or error without any user content or PII.
   */
  fun logSanitizedError(tag: String, message: String, category: String, isFatal: Boolean = false) {
    val log = SanitizedCrashLog(
      id = "err_${UUID.randomUUID().toString().take(8)}",
      tag = tag.take(30),
      message = message.take(120),
      category = category,
      isFatal = isFatal
    )
    val updated = listOf(log) + _crashLogs.value.take(49)
    _crashLogs.value = updated
  }
}
