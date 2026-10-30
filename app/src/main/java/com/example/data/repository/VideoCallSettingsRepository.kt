package com.example.data.repository

import com.example.data.model.VideoCallQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages HD Video Calling quality presets, camera zoom levels (local preview and remote video),
 * and zoom boundary clamps.
 */
object VideoCallSettingsRepository {

  private val _videoQuality = MutableStateFlow(VideoCallQuality.HD_720P)
  val videoQuality: StateFlow<VideoCallQuality> = _videoQuality.asStateFlow()

  // Local camera hardware zoom (1.0x to 5.0x)
  private val _localZoom = MutableStateFlow(1.0f)
  val localZoom: StateFlow<Float> = _localZoom.asStateFlow()

  // Remote video viewing zoom (1.0x to 4.0x)
  private val _remoteZoom = MutableStateFlow(1.0f)
  val remoteZoom: StateFlow<Float> = _remoteZoom.asStateFlow()

  private val _isRemoteZoomActive = MutableStateFlow(false)
  val isRemoteZoomActive: StateFlow<Boolean> = _isRemoteZoomActive.asStateFlow()

  fun setVideoQuality(quality: VideoCallQuality) {
    _videoQuality.value = quality
    // Sync into active call session if active
    ChatMitraRepository.updateVideoQuality(quality)
  }

  fun zoomInLocal() {
    val newZoom = (_localZoom.value + 0.5f).coerceAtMost(5.0f)
    _localZoom.value = newZoom
    ChatMitraRepository.updateLocalZoom(newZoom)
  }

  fun zoomOutLocal() {
    val newZoom = (_localZoom.value - 0.5f).coerceAtLeast(1.0f)
    _localZoom.value = newZoom
    ChatMitraRepository.updateLocalZoom(newZoom)
  }

  fun setLocalZoom(zoom: Float) {
    val clamped = zoom.coerceIn(1.0f, 5.0f)
    _localZoom.value = clamped
    ChatMitraRepository.updateLocalZoom(clamped)
  }

  fun resetLocalZoom() {
    _localZoom.value = 1.0f
    ChatMitraRepository.updateLocalZoom(1.0f)
  }

  fun toggleRemoteZoomMode() {
    _isRemoteZoomActive.value = !_isRemoteZoomActive.value
  }

  fun zoomInRemote() {
    val newZoom = (_remoteZoom.value + 0.5f).coerceAtMost(4.0f)
    _remoteZoom.value = newZoom
  }

  fun zoomOutRemote() {
    val newZoom = (_remoteZoom.value - 0.5f).coerceAtLeast(1.0f)
    _remoteZoom.value = newZoom
  }

  fun resetRemoteZoom() {
    _remoteZoom.value = 1.0f
  }
}
