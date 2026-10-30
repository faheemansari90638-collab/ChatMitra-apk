package com.example.data.repository

import com.example.data.calling.CallAudioAndVibrationManager
import com.example.data.model.CallNotificationSettings
import com.example.data.model.DndDuration
import com.example.data.model.SilentModeScope
import com.example.data.model.UserProfile
import com.example.data.model.VibrationPattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user preferences for Ringtone, Vibration, Silent Mode, Do Not Disturb,
 * and message notifications.
 */
object CallNotificationRepository {

  private val defaultSettings = CallNotificationSettings()

  private val _settings = MutableStateFlow(defaultSettings)
  val settings: StateFlow<CallNotificationSettings> = _settings.asStateFlow()

  // Track recent callers for Do Not Disturb repeat caller rules (within 15 minutes)
  private val recentCallTimestamps = mutableMapOf<String, MutableList<Long>>()

  fun setCallRingtone(ringtone: String, isVideo: Boolean = false) {
    _settings.value = if (isVideo) {
      _settings.value.copy(videoCallRingtone = ringtone)
    } else {
      _settings.value.copy(callRingtone = ringtone)
    }
  }

  fun setVibrationPattern(pattern: VibrationPattern) {
    _settings.value = _settings.value.copy(vibrationPattern = pattern)
  }

  fun setMessageNotificationSound(sound: String, isGroup: Boolean = false) {
    _settings.value = if (isVideoGroup(isGroup)) {
      _settings.value.copy(groupNotificationSound = sound)
    } else {
      _settings.value.copy(messageNotificationSound = sound)
    }
  }

  private fun isVideoGroup(isGroup: Boolean) = isGroup

  fun setBackupNotificationSound(sound: String) {
    _settings.value = _settings.value.copy(backupNotificationSound = sound)
  }

  fun toggleSilentMode(
    enabled: Boolean,
    scope: SilentModeScope? = null,
    muteVibration: Boolean? = null
  ) {
    _settings.value = _settings.value.copy(
      isSilentMode = enabled,
      silentModeScope = scope ?: _settings.value.silentModeScope,
      silentModeMuteVibration = muteVibration ?: _settings.value.silentModeMuteVibration
    )
  }

  fun setDndMode(duration: DndDuration, customHours: Int = 1) {
    val expiresAt = when (duration) {
      DndDuration.OFF -> null
      DndDuration.ONE_HOUR -> System.currentTimeMillis() + 3600_000L
      DndDuration.EIGHT_HOURS -> System.currentTimeMillis() + (8 * 3600_000L)
      DndDuration.MANUAL -> Long.MAX_VALUE
      DndDuration.CUSTOM_SCHEDULE -> System.currentTimeMillis() + (customHours * 3600_000L)
    }
    _settings.value = _settings.value.copy(
      dndDuration = duration,
      dndExpiresAt = expiresAt
    )
  }

  fun updateDndPreferences(
    muteCalls: Boolean? = null,
    muteVibration: Boolean? = null,
    muteMessages: Boolean? = null,
    allowStarred: Boolean? = null,
    allowRepeatCallers: Boolean? = null
  ) {
    val current = _settings.value
    _settings.value = current.copy(
      dndMuteCalls = muteCalls ?: current.dndMuteCalls,
      dndMuteVibration = muteVibration ?: current.dndMuteVibration,
      dndMuteMessages = muteMessages ?: current.dndMuteMessages,
      dndAllowStarredContacts = allowStarred ?: current.dndAllowStarredContacts,
      dndAllowRepeatCallers = allowRepeatCallers ?: current.dndAllowRepeatCallers
    )
  }

  fun setNotificationPreviewLockscreen(enabled: Boolean) {
    _settings.value = _settings.value.copy(notificationPreviewLockscreen = enabled)
  }

  fun setLowDataUsageForCalls(enabled: Boolean) {
    _settings.value = _settings.value.copy(lowDataUsageForCalls = enabled)
  }

  fun setContactCustomRingtone(contactId: String, ringtone: String) {
    val updated = _settings.value.contactRingtones.toMutableMap()
    if (ringtone.isBlank() || ringtone == "Default") {
      updated.remove(contactId)
    } else {
      updated[contactId] = ringtone
    }
    _settings.value = _settings.value.copy(contactRingtones = updated)
  }

  fun getEffectiveRingtone(contactId: String?, isVideo: Boolean): String {
    val current = _settings.value
    if (contactId != null && current.contactRingtones.containsKey(contactId)) {
      return current.contactRingtones[contactId]!!
    }
    return if (isVideo) current.videoCallRingtone else current.callRingtone
  }

  fun toggleChatMute(chatId: String) {
    val currentMuted = _settings.value.mutedChats
    val updated = if (currentMuted.contains(chatId)) {
      currentMuted - chatId
    } else {
      currentMuted + chatId
    }
    _settings.value = _settings.value.copy(mutedChats = updated)
  }

  fun isChatMuted(chatId: String): Boolean {
    return _settings.value.mutedChats.contains(chatId)
  }

  /**
   * Determines whether DND is currently active based on duration and expiry.
   */
  fun isDndActive(): Boolean {
    val current = _settings.value
    if (current.dndDuration == DndDuration.OFF) return false
    val expires = current.dndExpiresAt ?: return false
    return System.currentTimeMillis() < expires
  }

  /**
   * Records a call attempt to evaluate the repeat-caller rule (within 15 minutes).
   */
  fun recordCallAttempt(contactId: String) {
    val now = System.currentTimeMillis()
    val list = recentCallTimestamps.getOrPut(contactId) { mutableListOf() }
    list.removeAll { now - it > 15 * 60_000L } // purge older than 15 min
    list.add(now)
  }

  private fun isRepeatCaller(contactId: String): Boolean {
    val now = System.currentTimeMillis()
    val list = recentCallTimestamps[contactId] ?: return false
    val recentCount = list.count { now - it <= 15 * 60_000L }
    return recentCount >= 2
  }

  /**
   * Evaluates Silent Mode and DND policies to determine if the device should ring and vibrate.
   * Returns Pair(shouldRing, shouldVibrate).
   */
  fun evaluateCallAlertPolicy(caller: UserProfile): Pair<Boolean, Boolean> {
    recordCallAttempt(caller.id)
    val current = _settings.value

    var shouldRing = true
    var shouldVibrate = current.vibrationPattern != VibrationPattern.OFF

    // 1. Silent Mode Evaluation
    if (current.isSilentMode) {
      if (current.silentModeScope == SilentModeScope.CALLS_ONLY ||
        current.silentModeScope == SilentModeScope.CALLS_AND_MESSAGES
      ) {
        shouldRing = false
        if (current.silentModeMuteVibration) {
          shouldVibrate = false
        }
      }
    }

    // 2. Do Not Disturb Evaluation
    if (isDndActive()) {
      val isStarred = caller.isVerified // starred or verified
      val isRepeat = isRepeatCaller(caller.id)

      val isExempt = (current.dndAllowStarredContacts && isStarred) ||
        (current.dndAllowRepeatCallers && isRepeat)

      if (!isExempt) {
        if (current.dndMuteCalls) {
          shouldRing = false
        }
        if (current.dndMuteVibration) {
          shouldVibrate = false
        }
      }
    }

    return Pair(shouldRing, shouldVibrate)
  }

  /**
   * Evaluates whether a message notification sound should play.
   */
  fun shouldPlayMessageSound(chatId: String): Boolean {
    val current = _settings.value

    // Per-chat mute
    if (current.mutedChats.contains(chatId)) return false

    // Silent Mode
    if (current.isSilentMode && (
      current.silentModeScope == SilentModeScope.MESSAGES_ONLY ||
        current.silentModeScope == SilentModeScope.CALLS_AND_MESSAGES
    )) {
      return false
    }

    // DND
    if (isDndActive() && current.dndMuteMessages) {
      return false
    }

    return true
  }

  fun restoreDefaults() {
    _settings.value = defaultSettings
  }

  fun resetForTesting() {
    _settings.value = defaultSettings
    recentCallTimestamps.clear()
    CallAudioAndVibrationManager.stopIncomingCallAlert()
    CallAudioAndVibrationManager.stopOutgoingRingbackTone()
    CallAudioAndVibrationManager.stopTonePreview()
  }
}
