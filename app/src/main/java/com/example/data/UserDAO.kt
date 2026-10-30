package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {

  // Create
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: User)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<User>)

  // Read
  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  fun getUserById(userId: String): Flow<User?>

  @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
  fun getUserByPhone(phone: String): Flow<User?>

  @Query("SELECT * FROM users ORDER BY name ASC")
  fun getAllUsers(): Flow<List<User>>

  @Query("SELECT * FROM users WHERE isOnline = 1")
  fun getOnlineUsers(): Flow<List<User>>

  // Update
  @Update
  suspend fun updateUser(user: User)

  @Query("UPDATE users SET name = :name, about = :about WHERE id = :userId")
  suspend fun updateProfileInfo(userId: String, name: String, about: String)

  @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
  suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastSeen: String)

  // Delete
  @Delete
  suspend fun deleteUser(user: User)

  @Query("DELETE FROM users WHERE id = :userId")
  suspend fun deleteUserById(userId: String)

  @Query("DELETE FROM users")
  suspend fun deleteAllUsers()

  @Query("DELETE FROM users")
  suspend fun clearAllUsers()
}

typealias UserDao = UserDAO
