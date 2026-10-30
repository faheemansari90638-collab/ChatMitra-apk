package com.example.data.calling

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.UserProfile

/**
 * Handles Android Notification Channels, Incoming Call heads-up / full-screen alerts,
 * and Missed Call notifications.
 */
object ChatMitraNotificationHelper {
  const val CHANNEL_CALLS = "chatmitra_channel_calls"
  const val CHANNEL_MISSED_CALLS = "chatmitra_channel_missed_calls"
  const val CHANNEL_MESSAGES = "chatmitra_channel_messages"
  const val CHANNEL_GROUPS = "chatmitra_channel_groups"
  const val CHANNEL_BACKUP = "chatmitra_channel_backup"

  private const val NOTIFICATION_ID_INCOMING_CALL = 8801
  private const val NOTIFICATION_ID_MISSED_BASE = 9900

  fun initializeNotificationChannels(context: Context?) {
    if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    try {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        ?: return

      // 1. Calls Channel (High Importance for incoming call banners)
      val callsChannel = NotificationChannel(
        CHANNEL_CALLS,
        "Incoming Calls",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Full-screen and heads-up notifications for audio and video calls"
        enableVibration(true)
        setShowBadge(true)
        setBypassDnd(false)
      }

      // 2. Missed Calls Channel
      val missedCallsChannel = NotificationChannel(
        CHANNEL_MISSED_CALLS,
        "Missed Calls",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Alerts for missed audio and video calls"
        enableVibration(true)
        setShowBadge(true)
      }

      // 3. Messages Channel
      val messagesChannel = NotificationChannel(
        CHANNEL_MESSAGES,
        "Direct Messages",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "One-on-one incoming message notifications"
        enableVibration(true)
        setShowBadge(true)
      }

      // 4. Groups Channel
      val groupsChannel = NotificationChannel(
        CHANNEL_GROUPS,
        "Group Messages",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Community and group notifications"
        enableVibration(true)
        setShowBadge(true)
      }

      // 5. Backup Channel
      val backupChannel = NotificationChannel(
        CHANNEL_BACKUP,
        "Chat Backup & Cloud Sync",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Progress notifications during chat backups"
        enableVibration(false)
        setShowBadge(false)
      }

      notificationManager.createNotificationChannels(
        listOf(callsChannel, missedCallsChannel, messagesChannel, groupsChannel, backupChannel)
      )
    } catch (_: Exception) {}
  }

  fun showIncomingCallNotification(
    context: Context?,
    caller: UserProfile,
    isVideo: Boolean
  ) {
    if (context == null) return
    try {
      val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("incoming_call_id", caller.id)
        putExtra("is_video", isVideo)
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        fullScreenIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val callType = if (isVideo) "Incoming Video Call" else "Incoming Audio Call"
      val builder = NotificationCompat.Builder(context, CHANNEL_CALLS)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(caller.name)
        .setContentText("$callType • ChatMitra")
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setFullScreenIntent(pendingIntent, true)
        .setAutoCancel(true)
        .setOngoing(true)

      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(NOTIFICATION_ID_INCOMING_CALL, builder.build())
    } catch (_: Exception) {}
  }

  fun cancelIncomingCallNotification(context: Context?) {
    if (context == null) return
    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.cancel(NOTIFICATION_ID_INCOMING_CALL)
    } catch (_: Exception) {}
  }

  fun showMissedCallNotification(
    context: Context?,
    callerName: String,
    isVideo: Boolean,
    timestamp: Long
  ) {
    if (context == null) return
    try {
      val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("navigate_to", "calls")
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        (timestamp % 10000).toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val callType = if (isVideo) "Missed video call" else "Missed audio call"
      val builder = NotificationCompat.Builder(context, CHANNEL_MISSED_CALLS)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(callerName)
        .setContentText("$callType • Tap to view call history")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

      val notificationManager = NotificationManagerCompat.from(context)
      val notifId = NOTIFICATION_ID_MISSED_BASE + (timestamp % 50).toInt()
      notificationManager.notify(notifId, builder.build())
    } catch (_: Exception) {}
  }
}
