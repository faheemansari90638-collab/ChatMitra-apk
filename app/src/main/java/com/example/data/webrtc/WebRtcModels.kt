package com.example.data.webrtc

enum class SdpType {
  OFFER,
  ANSWER,
  PRANSWER,
  ROLLBACK
}

enum class PeerConnectionState {
  NEW,
  CHECKING,
  CONNECTING,
  CONNECTED,
  DISCONNECTED,
  FAILED,
  CLOSED
}

enum class IceGatheringState {
  NEW,
  GATHERING,
  COMPLETE
}

data class WebRtcIceCandidate(
  val sdpMid: String = "0",
  val sdpMLineIndex: Int = 0,
  val candidate: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

data class WebRtcSessionDescription(
  val type: SdpType,
  val sdp: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class CallQualityMetrics(
  val roundTripTimeMs: Long = 28L,
  val audioBitrateKbps: Int = 128,
  val videoBitrateKbps: Int = 2400,
  val videoResolution: String = "1080p 60fps",
  val packetLossPercent: Double = 0.0,
  val audioCodec: String = "Opus HD (48kHz 2-channel)",
  val videoCodec: String = "VP9 / H.264 High Profile",
  val dtlsCipherSuite: String = "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"
)

data class SignalingState(
  val callId: String = "",
  val isVideo: Boolean = false,
  val peerConnectionState: PeerConnectionState = PeerConnectionState.NEW,
  val iceGatheringState: IceGatheringState = IceGatheringState.NEW,
  val localDescription: WebRtcSessionDescription? = null,
  val remoteDescription: WebRtcSessionDescription? = null,
  val iceCandidates: List<WebRtcIceCandidate> = emptyList(),
  val metrics: CallQualityMetrics = CallQualityMetrics(),
  val isAudioMuted: Boolean = false,
  val isVideoMuted: Boolean = false,
  val isSpeakerphoneOn: Boolean = true,
  val isFrontCamera: Boolean = true,
  val isMinimized: Boolean = false,
  val callDurationSec: Int = 0
)
