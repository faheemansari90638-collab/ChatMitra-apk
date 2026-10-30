package com.example.data.local

import com.example.data.Message as RoomMessage
import com.example.data.MessageDAO
import com.example.data.User as RoomUser
import com.example.data.UserDAO
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalChatRepository(
  private val userDAO: UserDAO,
  private val messageDAO: MessageDAO
) {

  fun getMessagesForChat(chatId: String): Flow<List<Message>> {
    return messageDAO.getMessagesForChat(chatId).map { entities ->
      entities.map { it.toDomain() }
    }
  }

  fun getStarredMessages(): Flow<List<Message>> {
    return messageDAO.getStarredMessages().map { entities ->
      entities.map { it.toDomain() }
    }
  }

  suspend fun saveMessage(message: Message) {
    messageDAO.insertMessage(RoomMessage.fromDomain(message))
  }

  suspend fun saveMessages(messages: List<Message>) {
    messageDAO.insertMessages(messages.map { RoomMessage.fromDomain(it) })
  }

  suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
    messageDAO.updateMessageStatus(messageId, status.name)
  }

  suspend fun markMessageAsRead(messageId: String, readAt: Long = System.currentTimeMillis()) {
    messageDAO.markMessageAsRead(messageId, readAt)
  }

  suspend fun markChatMessagesAsRead(chatId: String, currentUserId: String, readAt: Long = System.currentTimeMillis()) {
    messageDAO.markChatMessagesAsRead(chatId, currentUserId, readAt)
  }

  suspend fun updateMessageText(messageId: String, newText: String) {
    messageDAO.updateMessageText(messageId, newText)
  }

  suspend fun markDeletedForEveryone(messageId: String) {
    messageDAO.markDeletedForEveryone(messageId)
  }

  suspend fun toggleStarMessage(messageId: String) {
    messageDAO.toggleStar(messageId)
  }

  suspend fun deleteMessage(messageId: String) {
    messageDAO.deleteMessageById(messageId)
  }

  suspend fun clearChatMessages(chatId: String) {
    messageDAO.clearMessagesForChat(chatId)
  }

  fun getUser(userId: String): Flow<UserProfile?> {
    return userDAO.getUserById(userId).map { it?.toProfile() }
  }

  fun getAllUsers(): Flow<List<UserProfile>> {
    return userDAO.getAllUsers().map { entities ->
      entities.map { it.toProfile() }
    }
  }

  suspend fun saveUser(user: UserProfile) {
    userDAO.insertUser(RoomUser.fromProfile(user))
  }

  suspend fun saveUsers(users: List<UserProfile>) {
    userDAO.insertUsers(users.map { RoomUser.fromProfile(it) })
  }
}
