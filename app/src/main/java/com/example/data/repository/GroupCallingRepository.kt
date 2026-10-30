package com.example.data.repository

import com.example.data.model.CallNetworkQuality
import com.example.data.model.CallParticipant
import com.example.data.model.GroupCallSession
import com.example.data.model.ParticipantRole
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages multi-participant audio and video group calls.
 * Enforces participant capacity limit of 8 participants (maximum WebRTC full-mesh p2p bandwidth).
 * Provides adaptive video quality controls, participant mute/kick, and active speaker spotlighting.
 */
object GroupCallingRepository {

  const val MAX_PARTICIPANTS = 8

  private val _activeGroupCall = MutableStateFlow<GroupCallSession?>(null)
  val activeGroupCall: StateFlow<GroupCallSession?> = _activeGroupCall.asStateFlow()

  private val _callErrorMessage = MutableStateFlow<String?>(null)
  val callErrorMessage: StateFlow<String?> = _callErrorMessage.asStateFlow()

  fun clearError() {
    _callErrorMessage.value = null
  }

  /**
   * Start a group call session with a host and initial invited participants.
   */
  fun startGroupCall(
    title: String,
    isVideo: Boolean,
    host: UserProfile,
    initialParticipants: List<UserProfile> = emptyList()
  ): Boolean {
    val totalCount = 1 + initialParticipants.size
    if (totalCount > MAX_PARTICIPANTS) {
      _callErrorMessage.value = "Participant limit reached ($MAX_PARTICIPANTS max for full-mesh call)"
      return false
    }

    val hostParticipant = CallParticipant(
      user = host,
      isAudioMuted = false,
      isVideoMuted = false,
      isSpeaking = true,
      role = ParticipantRole.HOST,
      networkQuality = CallNetworkQuality.EXCELLENT
    )

    val memberParticipants = initialParticipants.map { user ->
      CallParticipant(
        user = user,
        isAudioMuted = false,
        isVideoMuted = false,
        isSpeaking = false,
        role = ParticipantRole.MEMBER,
        networkQuality = CallNetworkQuality.GOOD
      )
    }

    _activeGroupCall.value = GroupCallSession(
      callId = "grp_call_${UUID.randomUUID().toString().take(8)}",
      title = title.ifBlank { "Group Call" },
      isVideo = isVideo,
      hostId = host.id,
      participants = listOf(hostParticipant) + memberParticipants,
      maxParticipants = MAX_PARTICIPANTS,
      activeSpeakerId = host.id,
      callQualityMode = "HD"
    )
    return true
  }

  /**
   * Add a new participant to the ongoing group call, strictly enforcing the 8-participant limit.
   */
  fun addParticipant(user: UserProfile): Boolean {
    val current = _activeGroupCall.value ?: return false
    if (current.participants.any { it.user.id == user.id }) {
      return true
    }
    if (current.participants.size >= current.maxParticipants) {
      _callErrorMessage.value = "Cannot add ${user.name}: Maximum capacity of ${current.maxParticipants} participants reached."
      return false
    }

    val newParticipant = CallParticipant(
      user = user,
      isAudioMuted = false,
      isVideoMuted = false,
      isSpeaking = false,
      role = ParticipantRole.MEMBER,
      networkQuality = CallNetworkQuality.EXCELLENT
    )

    _activeGroupCall.value = current.copy(
      participants = current.participants + newParticipant
    )
    return true
  }

  /**
   * Host action to kick/remove a participant from the call.
   */
  fun removeParticipant(userId: String) {
    val current = _activeGroupCall.value ?: return
    val updated = current.participants.filterNot { it.user.id == userId }
    if (updated.isEmpty() || userId == current.hostId) {
      endGroupCall()
    } else {
      _activeGroupCall.value = current.copy(
        participants = updated,
        activeSpeakerId = if (current.activeSpeakerId == userId) updated.firstOrNull()?.user?.id else current.activeSpeakerId
      )
    }
  }

  /**
   * Toggle remote or local participant audio mute state.
   */
  fun toggleAudioMute(userId: String) {
    val current = _activeGroupCall.value ?: return
    val updated = current.participants.map {
      if (it.user.id == userId) it.copy(isAudioMuted = !it.isAudioMuted) else it
    }
    _activeGroupCall.value = current.copy(participants = updated)
  }

  /**
   * Toggle video mute state for a participant.
   */
  fun toggleVideoMute(userId: String) {
    val current = _activeGroupCall.value ?: return
    val updated = current.participants.map {
      if (it.user.id == userId) it.copy(isVideoMuted = !it.isVideoMuted) else it
    }
    _activeGroupCall.value = current.copy(participants = updated)
  }

  /**
   * Set active speaker spotlight.
   */
  fun setActiveSpeaker(userId: String) {
    val current = _activeGroupCall.value ?: return
    val updated = current.participants.map {
      it.copy(isSpeaking = it.user.id == userId)
    }
    _activeGroupCall.value = current.copy(
      participants = updated,
      activeSpeakerId = userId
    )
  }

  /**
   * Switch adaptive video quality (HD: 720p 30fps, SD: 480p 24fps, LOW_DATA: 240p 15fps).
   */
  fun setCallQualityMode(mode: String) {
    val current = _activeGroupCall.value ?: return
    _activeGroupCall.value = current.copy(callQualityMode = mode)
  }

  /**
   * Terminate the group call session.
   */
  fun endGroupCall() {
    _activeGroupCall.value = null
  }
}
