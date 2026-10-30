package com.example.data.calling

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.model.AudioRoute
import com.example.data.model.VibrationPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages real hardware audio routing, ringtone playback, ringback tones,
 * vibration patterns, and preview sounds for ChatMitra calling.
 */
object CallAudioAndVibrationManager {
  private const val TAG = "CallAudioVibration"

  private val managerScope = CoroutineScope(Dispatchers.Default)

  private var activeRingtone: Ringtone? = null
  private var previewRingtone: Ringtone? = null
  private var ringbackToneGenerator: ToneGenerator? = null
  private var previewToneGenerator: ToneGenerator? = null

  private var vibrationJob: Job? = null
  private var ringbackJob: Job? = null
  private var previewJob: Job? = null

  val availableRingtones = listOf(
    "ChatMitra Chime",
    "Marimba Pulse",
    "Zen Garden",
    "Acoustic Horizon",
    "Cosmic Melody",
    "Classic Digital",
    "Default System Ringtone"
  )

  val availableMessageSounds = listOf(
    "Gentle Pop",
    "Crystal Bell",
    "Subtle Tap",
    "Cosmic Tone",
    "Bright Ping",
    "Default Notification Sound"
  )

  val availableGroupSounds = listOf(
    "Marimba Chime",
    "Double Tap",
    "Crystal Bell",
    "Bright Ping",
    "Default Notification Sound"
  )

  /**
   * Starts ringing and vibrating for an incoming call according to the active settings.
   */
  fun startIncomingCallAlert(
    context: Context?,
    ringtoneName: String,
    pattern: VibrationPattern,
    isSilent: Boolean,
    muteVibration: Boolean
  ) {
    stopIncomingCallAlert(context)

    // 1. Vibration
    if (!muteVibration && pattern != VibrationPattern.OFF) {
      startVibration(context, pattern)
    }

    // 2. Ringtone
    if (!isSilent) {
      if (pattern == VibrationPattern.VIBRATE_FIRST_THEN_RING) {
        managerScope.launch {
          delay(2000)
          playRingtoneInternal(context, ringtoneName)
        }
      } else {
        playRingtoneInternal(context, ringtoneName)
      }
    }
  }

  /**
   * Stops all incoming call ringtone playback and vibration loops immediately.
   */
  fun stopIncomingCallAlert(context: Context? = null) {
    try {
      activeRingtone?.stop()
      activeRingtone = null
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping ringtone", e)
    }

    vibrationJob?.cancel()
    vibrationJob = null

    try {
      context?.let { ctx ->
        val vibrator = getVibrator(ctx)
        vibrator?.cancel()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error cancelling vibrator", e)
    }
  }

  /**
   * Starts the outgoing ringback tone (standard telecom signaling tone) while waiting for the remote party.
   */
  fun startOutgoingRingbackTone(context: Context?) {
    stopOutgoingRingbackTone()
    ringbackJob = managerScope.launch {
      try {
        ringbackToneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 75)
        while (isActive) {
          ringbackToneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1500)
          delay(3500)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Outgoing ringback tone exception: ${e.message}")
      }
    }
  }

  /**
   * Stops the outgoing ringback tone.
   */
  fun stopOutgoingRingbackTone() {
    ringbackJob?.cancel()
    ringbackJob = null
    try {
      ringbackToneGenerator?.stopTone()
      ringbackToneGenerator?.release()
      ringbackToneGenerator = null
    } catch (e: Exception) {
      Log.w(TAG, "Error releasing ringback tone generator", e)
    }
  }

  /**
   * Plays a brief call-ended beep.
   */
  fun playCallEndedTone(context: Context?) {
    managerScope.launch {
      try {
        val tg = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 60)
        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        delay(250)
        tg.release()
      } catch (e: Exception) {
        Log.w(TAG, "Call ended tone exception: ${e.message}")
      }
    }
  }

  /**
   * Previews a selected ringtone or notification sound.
   */
  fun playTonePreview(context: Context?, toneName: String, onFinished: (() -> Unit)? = null) {
    stopTonePreview()

    previewJob = managerScope.launch {
      try {
        if (context != null) {
          val uri: Uri? = if (toneName == "Default System Ringtone") {
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
          } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
          }

          if (uri != null) {
            previewRingtone = RingtoneManager.getRingtone(context, uri)
            previewRingtone?.play()
            delay(4000)
            previewRingtone?.stop()
            previewRingtone = null
          }
        } else {
          // JVM fallback
          delay(2000)
        }
      } catch (e: Exception) {
        // Fallback to ToneGenerator for preview
        try {
          previewToneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
          val toneType = when (toneName) {
            "Crystal Bell", "Bright Ping" -> ToneGenerator.TONE_PROP_BEEP2
            "Marimba Pulse", "Marimba Chime" -> ToneGenerator.TONE_SUP_CONFIRM
            "Gentle Pop" -> ToneGenerator.TONE_PROP_BEEP
            else -> ToneGenerator.TONE_PROP_ACK
          }
          previewToneGenerator?.startTone(toneType, 800)
          delay(1000)
          previewToneGenerator?.release()
          previewToneGenerator = null
        } catch (_: Exception) {}
      } finally {
        onFinished?.invoke()
      }
    }
  }

  /**
   * Stops any currently playing preview sound.
   */
  fun stopTonePreview() {
    previewJob?.cancel()
    previewJob = null
    try {
      previewRingtone?.stop()
      previewRingtone = null
    } catch (_: Exception) {}

    try {
      previewToneGenerator?.stopTone()
      previewToneGenerator?.release()
      previewToneGenerator = null
    } catch (_: Exception) {}
  }

  /**
   * Triggers a one-shot sample vibration for settings verification.
   */
  fun triggerTestVibration(context: Context?, pattern: VibrationPattern) {
    if (context == null || pattern == VibrationPattern.OFF) return
    try {
      val vibrator = getVibrator(context) ?: return
      when (pattern) {
        VibrationPattern.SHORT -> {
          vibrateDirect(vibrator, 200L)
        }
        VibrationPattern.LONG -> {
          vibrateDirect(vibrator, 800L)
        }
        VibrationPattern.DEFAULT, VibrationPattern.RING_AND_VIBRATE -> {
          vibratePattern(vibrator, longArrayOf(0, 300, 200, 300), repeatIndex = -1)
        }
        VibrationPattern.VIBRATE_FIRST_THEN_RING -> {
          vibrateDirect(vibrator, 1500L)
        }
        VibrationPattern.OFF -> {}
      }
    } catch (e: Exception) {
      Log.w(TAG, "Test vibration error", e)
    }
  }

  /**
   * Configures audio output routing (Speaker, Earpiece, or Bluetooth).
   */
  fun setAudioRoute(context: Context?, route: AudioRoute) {
    if (context == null) return
    try {
      val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
      am.mode = AudioManager.MODE_IN_COMMUNICATION
      when (route) {
        AudioRoute.SPEAKER -> {
          am.isSpeakerphoneOn = true
          am.isBluetoothScoOn = false
        }
        AudioRoute.EARPIECE -> {
          am.isSpeakerphoneOn = false
          am.isBluetoothScoOn = false
        }
        AudioRoute.BLUETOOTH -> {
          am.isSpeakerphoneOn = false
          am.startBluetoothSco()
          am.isBluetoothScoOn = true
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Audio routing error", e)
    }
  }

  private fun playRingtoneInternal(context: Context?, ringtoneName: String) {
    if (context == null) return
    try {
      val uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      activeRingtone = RingtoneManager.getRingtone(context, uri)
      activeRingtone?.audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
      activeRingtone?.play()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to play incoming ringtone", e)
    }
  }

  private fun startVibration(context: Context?, pattern: VibrationPattern) {
    if (context == null) return
    val vibrator = getVibrator(context) ?: return

    vibrationJob?.cancel()
    vibrationJob = managerScope.launch {
      val (timings, amplitudes) = when (pattern) {
        VibrationPattern.SHORT -> Pair(longArrayOf(0, 200, 300, 200), intArrayOf(0, 180, 0, 180))
        VibrationPattern.LONG -> Pair(longArrayOf(0, 800, 600, 800), intArrayOf(0, 255, 0, 255))
        VibrationPattern.VIBRATE_FIRST_THEN_RING -> Pair(longArrayOf(0, 1800, 500), intArrayOf(0, 255, 0))
        else -> Pair(longArrayOf(0, 400, 400, 400), intArrayOf(0, 220, 0, 220))
      }

      while (isActive) {
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
          } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
          }
        } catch (_: Exception) {}

        val totalDuration = timings.sum() + 400
        delay(totalDuration)
      }
    }
  }

  private fun vibrateDirect(vibrator: Vibrator, duration: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(duration)
    }
  }

  private fun vibratePattern(vibrator: Vibrator, pattern: LongArray, repeatIndex: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeatIndex))
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(pattern, repeatIndex)
    }
  }

  private fun getVibrator(context: Context): Vibrator? {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }
    } catch (_: Exception) {
      null
    }
  }
}
