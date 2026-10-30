package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDAO {

  // Create
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: Message)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessages(messages: List<Message>)

  // Read
  @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
  fun getMessageById(messageId: String): Flow<Message?>

  @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
  fun getMessagesForChat(chatId: String): Flow<List<Message>>

  @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
  fun getLastMessageForChat(chatId: String): Flow<Message?>

  @Query("SELECT * FROM messages WHERE isStarred = 1 ORDER BY timestamp DESC")
  fun getStarredMessages(): Flow<List<Message>>

  @Query("SELECT * FROM messages WHERE chatId = :chatId AND text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
  fun searchMessagesInChat(chatId: String, query: String): Flow<List<Message>>

  @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND isRead = 0 AND status != 'READ'")
  fun getUnreadCountForChat(chatId: String): Flow<Int>

  @Query("SELECT * FROM messages WHERE chatId = :chatId AND isRead = 1 ORDER BY readAt DESC")
  fun getReadMessagesForChat(chatId: String): Flow<List<Message>>

  // Update
  @Update
  suspend fun updateMessage(message: Message)

  @Query("UPDATE messages SET status = :status WHERE id = :messageId")
  suspend fun updateMessageStatus(messageId: String, status: String)

  @Query("UPDATE messages SET isRead = 1, readAt = :readAt, status = 'READ' WHERE id = :messageId")
  suspend fun markMessageAsRead(messageId: String, readAt: Long = System.currentTimeMillis())

  @Query("UPDATE messages SET isRead = 1, readAt = :readAt, status = 'READ' WHERE chatId = :chatId AND senderId != :currentUserId AND isRead = 0")
  suspend fun markChatMessagesAsRead(chatId: String, currentUserId: String, readAt: Long = System.currentTimeMillis())

  @Query("UPDATE messages SET text = :newText, isEdited = 1 WHERE id = :messageId")
  suspend fun updateMessageText(messageId: String, newText: String)

  @Query("UPDATE messages SET text = '🚫 This message was deleted', isDeletedForEveryone = 1 WHERE id = :messageId")
  suspend fun markDeletedForEveryone(messageId: String)

  @Query("UPDATE messages SET isStarred = NOT isStarred WHERE id = :messageId")
  suspend fun toggleStar(messageId: String)

  // Delete
  @Delete
  suspend fun deleteMessage(message: Message)

  @Query("DELETE FROM messages WHERE id = :messageId")
  suspend fun deleteMessageById(messageId: String)

  @Query("DELETE FROM messages WHERE chatId = :chatId")
  suspend fun clearMessagesForChat(chatId: String)

  @Query("DELETE FROM messages")
  suspend fun deleteAllMessages()

  @Query("DELETE FROM messages")
  suspend fun clearAllMessages()
}

typealias MessageDao = MessageDAO
