package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Message as DomainMessage
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType

@Entity(
  tableName = "messages",
  indices = [
    Index(value = ["chatId"]),
    Index(value = ["timestamp"]),
    Index(value = ["isRead"])
  ]
)
data class Message(
  @PrimaryKey val id: String,
  val chatId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val status: String = MessageStatus.SENT.name,
  val type: String = MessageType.TEXT.name,
  val mediaUri: String? = null,
  val mediaFileName: String? = null,
  val mediaFileSize: String? = null,
  val mediaDurationSec: Int = 0,
  val replyToId: String? = null,
  val replyToText: String? = null,
  val replyToSender: String? = null,
  val isEdited: Boolean = false,
  val isDeletedForEveryone: Boolean = false,
  val isStarred: Boolean = false,
  val isRead: Boolean = false,
  val readAt: Long? = null
) {
  fun toDomain(): DomainMessage {
    return DomainMessage(
      id = id,
      chatId = chatId,
      senderId = senderId,
      senderName = senderName,
      text = text,
      timestamp = timestamp,
      status = if (isRead) {
        MessageStatus.READ
      } else {
        try {
          MessageStatus.valueOf(status)
        } catch (_: Exception) {
          MessageStatus.SENT
        }
      },
      type = try {
        MessageType.valueOf(type)
      } catch (_: Exception) {
        MessageType.TEXT
      },
      mediaUri = mediaUri,
      mediaFileName = mediaFileName,
      mediaFileSize = mediaFileSize,
      mediaDurationSec = mediaDurationSec,
      replyToId = replyToId,
      replyToText = replyToText,
      replyToSender = replyToSender,
      isEdited = isEdited,
      isDeletedForEveryone = isDeletedForEveryone,
      isStarred = isStarred,
      isRead = isRead || status == MessageStatus.READ.name,
      readAt = readAt
    )
  }

  companion object {
    fun fromDomain(msg: DomainMessage): Message {
      return Message(
        id = msg.id,
        chatId = msg.chatId,
        senderId = msg.senderId,
        senderName = msg.senderName,
        text = msg.text,
        timestamp = msg.timestamp,
        status = if (msg.isRead) MessageStatus.READ.name else msg.status.name,
        type = msg.type.name,
        mediaUri = msg.mediaUri,
        mediaFileName = msg.mediaFileName,
        mediaFileSize = msg.mediaFileSize,
        mediaDurationSec = msg.mediaDurationSec,
        replyToId = msg.replyToId,
        replyToText = msg.replyToText,
        replyToSender = msg.replyToSender,
        isEdited = msg.isEdited,
        isDeletedForEveryone = msg.isDeletedForEveryone,
        isStarred = msg.isStarred,
        isRead = msg.isRead || msg.status == MessageStatus.READ,
        readAt = msg.readAt
      )
    }
  }
}
