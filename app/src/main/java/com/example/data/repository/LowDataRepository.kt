package com.example.data.repository

import com.example.data.model.AutoDownloadOption
import com.example.data.model.LowDataSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Low Data Mode preferences, automatic media download rules, and adaptive media quality.
 */
object LowDataRepository {

  private val _settings = MutableStateFlow(LowDataSettings())
  val settings: StateFlow<LowDataSettings> = _settings.asStateFlow()

  fun setLowDataMode(enabled: Boolean) {
    _settings.value = _settings.value.copy(
      isLowDataModeEnabled = enabled,
      // When low data mode is turned on, restrict media auto-downloads
      autoDownloadPhotos = if (enabled) AutoDownloadOption.NEVER else AutoDownloadOption.WIFI_ONLY,
      autoDownloadAudio = if (enabled) AutoDownloadOption.NEVER else AutoDownloadOption.WIFI_ONLY,
      autoDownloadVideo = AutoDownloadOption.NEVER,
      autoDownloadDocuments = if (enabled) AutoDownloadOption.NEVER else AutoDownloadOption.WIFI_ONLY,
      compressMediaBeforeUpload = true,
      adaptiveCallBitrateEnabled = true,
      limitBackgroundData = enabled
    )
  }

  fun updateAutoDownloadPhotos(option: AutoDownloadOption) {
    _settings.value = _settings.value.copy(autoDownloadPhotos = option)
  }

  fun updateAutoDownloadAudio(option: AutoDownloadOption) {
    _settings.value = _settings.value.copy(autoDownloadAudio = option)
  }

  fun updateAutoDownloadVideo(option: AutoDownloadOption) {
    _settings.value = _settings.value.copy(autoDownloadVideo = option)
  }

  fun updateAutoDownloadDocuments(option: AutoDownloadOption) {
    _settings.value = _settings.value.copy(autoDownloadDocuments = option)
  }

  fun toggleCompressMedia(enabled: Boolean) {
    _settings.value = _settings.value.copy(compressMediaBeforeUpload = enabled)
  }

  fun toggleAdaptiveCallBitrate(enabled: Boolean) {
    _settings.value = _settings.value.copy(adaptiveCallBitrateEnabled = enabled)
  }

  fun toggleLimitBackgroundData(enabled: Boolean) {
    _settings.value = _settings.value.copy(limitBackgroundData = enabled)
  }

  /**
   * Calculates target video call bitrate based on low data mode and quality settings.
   */
  fun getTargetCallBitrateKbps(isLowDataActive: Boolean, qualityMode: String): Int {
    if (isLowDataActive || qualityMode == "LOW_DATA") {
      return 150 // 240p 15fps, ~150 kbps
    }
    return when (qualityMode) {
      "SD" -> 500  // 480p 24fps, ~500 kbps
      "HD" -> 1500 // 720p 30fps, ~1500 kbps
      else -> 1000
    }
  }
}
