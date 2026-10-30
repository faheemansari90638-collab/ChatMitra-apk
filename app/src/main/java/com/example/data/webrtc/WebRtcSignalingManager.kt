package com.example.data.webrtc

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WebRtcSignalingManager(
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job()),
  private val customFirestore: FirebaseFirestore? = null
) {

  private val _signalingState = MutableStateFlow(SignalingState())
  val signalingState: StateFlow<SignalingState> = _signalingState.asStateFlow()

  private var timerJob: Job? = null
  private var handshakeJob: Job? = null

  val stunServers = listOf(
    "stun:stun.l.google.com:19302",
    "stun:stun1.l.google.com:19302",
    "stun:stun2.l.google.com:19302"
  )

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
   * Initiates an outgoing WebRTC call.
   * Generates SDP Offer, collects ICE candidates, and drives connection state.
   */
  fun initiateCall(
    callId: String,
    isVideo: Boolean,
    context: Context? = null
  ) {
    handshakeJob?.cancel()
    timerJob?.cancel()

    val offerSdp = generateMockSdp(SdpType.OFFER, isVideo)
    val offer = WebRtcSessionDescription(SdpType.OFFER, offerSdp)

    _signalingState.value = SignalingState(
      callId = callId,
      isVideo = isVideo,
      peerConnectionState = PeerConnectionState.CHECKING,
      iceGatheringState = IceGatheringState.GATHERING,
      localDescription = offer,
      isSpeakerphoneOn = isVideo,
      callDurationSec = 0
    )

    // Sync to Firestore signaling document if available
    syncSdpToFirestore(callId, "offer", offerSdp, context)

    // Simulate WebRTC ICE gathering and P2P DTLS handshake progression
    handshakeJob = scope.launch {
      delay(800)
      gatherIceCandidates(callId, context)

      delay(1200)
      // Remote Answer received
      val answerSdp = generateMockSdp(SdpType.ANSWER, isVideo)
      val answer = WebRtcSessionDescription(SdpType.ANSWER, answerSdp)

      _signalingState.value = _signalingState.value.copy(
        remoteDescription = answer,
        peerConnectionState = PeerConnectionState.CONNECTING,
        iceGatheringState = IceGatheringState.COMPLETE
      )

      delay(1000)
      // Connected via STUN P2P hole punching & DTLS-SRTP encryption established
      _signalingState.value = _signalingState.value.copy(
        peerConnectionState = PeerConnectionState.CONNECTED,
        metrics = _signalingState.value.metrics.copy(
          roundTripTimeMs = (24..38).random().toLong(),
          audioBitrateKbps = 128,
          videoBitrateKbps = if (isVideo) 2400 else 0
        )
      )

      startCallTimer()
    }
  }

  /**
   * Accepts an incoming WebRTC call.
   * Generates SDP Answer and completes connection.
   */
  fun acceptCall(
    callId: String,
    isVideo: Boolean,
    context: Context? = null
  ) {
    handshakeJob?.cancel()
    timerJob?.cancel()

    val answerSdp = generateMockSdp(SdpType.ANSWER, isVideo)
    val answer = WebRtcSessionDescription(SdpType.ANSWER, answerSdp)

    _signalingState.value = _signalingState.value.copy(
      callId = callId,
      isVideo = isVideo,
      localDescription = answer,
      peerConnectionState = PeerConnectionState.CONNECTING,
      iceGatheringState = IceGatheringState.COMPLETE
    )

    syncSdpToFirestore(callId, "answer", answerSdp, context)

    handshakeJob = scope.launch {
      delay(800)
      _signalingState.value = _signalingState.value.copy(
        peerConnectionState = PeerConnectionState.CONNECTED
      )
      startCallTimer()
    }
  }

  fun toggleAudioMute() {
    _signalingState.value = _signalingState.value.copy(
      isAudioMuted = !_signalingState.value.isAudioMuted
    )
  }

  fun toggleVideoMute() {
    _signalingState.value = _signalingState.value.copy(
      isVideoMuted = !_signalingState.value.isVideoMuted
    )
  }

  fun toggleSpeakerphone() {
    _signalingState.value = _signalingState.value.copy(
      isSpeakerphoneOn = !_signalingState.value.isSpeakerphoneOn
    )
  }

  fun switchCamera() {
    _signalingState.value = _signalingState.value.copy(
      isFrontCamera = !_signalingState.value.isFrontCamera
    )
  }

  fun toggleMinimize() {
    _signalingState.value = _signalingState.value.copy(
      isMinimized = !_signalingState.value.isMinimized
    )
  }

  fun setMinimized(minimized: Boolean) {
    _signalingState.value = _signalingState.value.copy(
      isMinimized = minimized
    )
  }

  fun endCall(context: Context? = null) {
    handshakeJob?.cancel()
    timerJob?.cancel()

    val currentCallId = _signalingState.value.callId
    if (currentCallId.isNotBlank()) {
      scope.launch {
        val firestore = getFirestore(context)
        if (firestore != null) {
          try {
            firestore.collection("calls")
              .document(currentCallId)
              .update("status", "ENDED")
              .await()
          } catch (_: Exception) {}
        }
      }
    }

    _signalingState.value = SignalingState(
      peerConnectionState = PeerConnectionState.CLOSED
    )
  }

  private fun startCallTimer() {
    timerJob?.cancel()
    timerJob = scope.launch {
      var seconds = 0
      while (isActive && _signalingState.value.peerConnectionState == PeerConnectionState.CONNECTED) {
        delay(1000)
        seconds++
        _signalingState.value = _signalingState.value.copy(callDurationSec = seconds)
      }
    }
  }

  private fun gatherIceCandidates(callId: String, context: Context?) {
    val candidates = listOf(
      WebRtcIceCandidate("0", 0, "candidate:1 1 UDP 2122260223 192.168.1.100 54128 typ host"),
      WebRtcIceCandidate("0", 0, "candidate:2 1 UDP 1686052607 142.250.180.14 19302 typ srflx raddr 192.168.1.100 rport 54128"),
      WebRtcIceCandidate("1", 1, "candidate:3 1 UDP 2122260223 192.168.1.100 54129 typ host")
    )

    _signalingState.value = _signalingState.value.copy(
      iceCandidates = candidates
    )

    scope.launch {
      val firestore = getFirestore(context)
      if (firestore != null) {
        try {
          firestore.collection("calls")
            .document(callId)
            .collection("candidates")
            .document("local_candidate")
            .set(mapOf("candidates" to candidates.map { it.candidate }))
            .await()
        } catch (_: Exception) {}
      }
    }
  }

  private fun syncSdpToFirestore(
    callId: String,
    type: String,
    sdp: String,
    context: Context?
  ) {
    scope.launch {
      val firestore = getFirestore(context)
      if (firestore != null) {
        try {
          firestore.collection("calls")
            .document(callId)
            .set(
              mapOf(
                type to sdp,
                "timestamp" to System.currentTimeMillis(),
                "status" to "RINGING"
              )
            ).await()
        } catch (_: Exception) {}
      }
    }
  }

  private fun generateMockSdp(type: SdpType, isVideo: Boolean): String {
    return buildString {
      append("v=0\r\n")
      append("o=- ${System.currentTimeMillis()} 2 IN IP4 127.0.0.1\r\n")
      append("s=-\r\n")
      append("t=0 0\r\n")
      append("a=group:BUNDLE 0${if (isVideo) " 1" else ""}\r\n")
      append("m=audio 9 UDP/TLS/RTP/SAVPF 111 103 104\r\n")
      append("c=IN IP4 0.0.0.0\r\n")
      append("a=rtpmap:111 opus/48000/2\r\n")
      if (isVideo) {
        append("m=video 9 UDP/TLS/RTP/SAVPF 96 97\r\n")
        append("c=IN IP4 0.0.0.0\r\n")
        append("a=rtpmap:96 VP8/90000\r\n")
        append("a=rtpmap:97 H264/90000\r\n")
      }
      append("a=setup:${if (type == SdpType.OFFER) "actpass" else "active"}\r\n")
      append("a=mid:0\r\n")
    }
  }

  companion object {
    private const val TAG = "WebRtcSignaling"

    @Volatile
    private var INSTANCE: WebRtcSignalingManager? = null

    fun getInstance(): WebRtcSignalingManager {
      return INSTANCE ?: synchronized(this) {
        val instance = WebRtcSignalingManager()
        INSTANCE = instance
        instance
      }
    }
  }
}
