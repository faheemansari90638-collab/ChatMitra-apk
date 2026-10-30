package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserProfile

@Entity(tableName = "users")
data class User(
  @PrimaryKey val id: String,
  val name: String,
  val phone: String,
  val about: String = "Hey there! I am using ChatMitra.",
  val avatarColorHex: Long = 0xFF00A884,
  val avatarEmoji: String = "👤",
  val isOnline: Boolean = false,
  val lastSeen: String = "recently",
  val publicKeyFingerprint: String = "",
  val isVerified: Boolean = false
) {
  fun toProfile(): UserProfile {
    return UserProfile(
      id = id,
      name = name,
      phone = phone,
      about = about,
      avatarColorHex = avatarColorHex,
      avatarEmoji = avatarEmoji,
      isOnline = isOnline,
      lastSeen = lastSeen,
      publicKeyFingerprint = publicKeyFingerprint,
      isVerified = isVerified
    )
  }

  companion object {
    fun fromProfile(profile: UserProfile): User {
      return User(
        id = profile.id,
        name = profile.name,
        phone = profile.phone,
        about = profile.about,
        avatarColorHex = profile.avatarColorHex,
        avatarEmoji = profile.avatarEmoji,
        isOnline = profile.isOnline,
        lastSeen = profile.lastSeen,
        publicKeyFingerprint = profile.publicKeyFingerprint,
        isVerified = profile.isVerified
      )
    }
  }
}
