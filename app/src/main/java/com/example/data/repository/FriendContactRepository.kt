package com.example.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.data.model.FriendRequest
import com.example.data.model.FriendRequestStatus
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages contacts, friend requests, username searches, contact sync, and QR profile sharing.
 */
object FriendContactRepository {

  // Seed searchable users for username discovery
  private val globalDirectory = listOf(
    UserProfile(
      id = "c_priya",
      name = "Priya Sharma",
      phone = "+91 98220 11223",
      username = "priya",
      about = "Always learning, always creating ✨",
      avatarColorHex = 0xFFE91E63,
      avatarEmoji = "🌸",
      isOnline = true,
      lastSeen = "Online"
    ),
    UserProfile(
      id = "c_rahul",
      name = "Rahul Verma",
      phone = "+91 98110 33445",
      username = "rahul_dev",
      about = "Coffee, code & music ☕🎧",
      avatarColorHex = 0xFF3F51B5,
      avatarEmoji = "🚀",
      isOnline = false,
      lastSeen = "Today at 08:42"
    ),
    UserProfile(
      id = "c_ananya",
      name = "Ananya Patel",
      phone = "+91 98990 77889",
      username = "ananya_ux",
      about = "Designing the future of human interface 🎨",
      avatarColorHex = 0xFF9C27B0,
      avatarEmoji = "🎨",
      isOnline = true,
      lastSeen = "Online"
    ),
    UserProfile(
      id = "c_rohan",
      name = "Rohan Gupta",
      phone = "+91 97120 55667",
      username = "rohan_g",
      about = "Available for badminton & tech talks 🏸",
      avatarColorHex = 0xFFFF9800,
      avatarEmoji = "⚡",
      isOnline = false,
      lastSeen = "Yesterday at 22:15"
    ),
    UserProfile(
      id = "c_zaid",
      name = "Zaid Khan",
      phone = "+91 99887 66554",
      username = "zaid_k",
      about = "Android & WebRTC Specialist 📱",
      avatarColorHex = 0xFF00A884,
      avatarEmoji = "🦁",
      isOnline = true,
      lastSeen = "Online"
    ),
    UserProfile(
      id = "c_sara",
      name = "Sara Ali",
      phone = "+971 50 123 4567",
      username = "sara_dubai",
      about = "Photography & traveling ✈️📸",
      avatarColorHex = 0xFF0284C7,
      avatarEmoji = "🌟",
      isOnline = true,
      lastSeen = "Online"
    )
  )

  // Friend Requests (Incoming and Outgoing)
  private val _friendRequests = MutableStateFlow<List<FriendRequest>>(
    listOf(
      FriendRequest(
        id = "req_1",
        senderId = "c_zaid",
        senderName = "Zaid Khan",
        senderUsername = "zaid_k",
        senderEmoji = "🦁",
        senderColorHex = 0xFF00A884,
        receiverId = "user_me",
        timestamp = System.currentTimeMillis() - 7200000L,
        status = FriendRequestStatus.PENDING
      ),
      FriendRequest(
        id = "req_2",
        senderId = "c_sara",
        senderName = "Sara Ali",
        senderUsername = "sara_dubai",
        senderEmoji = "🌟",
        senderColorHex = 0xFF0284C7,
        receiverId = "user_me",
        timestamp = System.currentTimeMillis() - 86400000L,
        status = FriendRequestStatus.PENDING
      )
    )
  )
  val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

  // Synced Device Contacts
  private val _syncedDeviceContacts = MutableStateFlow<List<UserProfile>>(emptyList())
  val syncedDeviceContacts: StateFlow<List<UserProfile>> = _syncedDeviceContacts.asStateFlow()

  private val _isSyncingContacts = MutableStateFlow(false)
  val isSyncingContacts: StateFlow<Boolean> = _isSyncingContacts.asStateFlow()

  /**
   * Search users by exact or partial username or name.
   */
  fun searchByUsername(query: String): List<UserProfile> {
    val clean = query.trim().removePrefix("@").lowercase()
    if (clean.isBlank()) return emptyList()
    return globalDirectory.filter {
      it.username.lowercase().contains(clean) ||
          it.name.lowercase().contains(clean) ||
          it.phone.contains(clean)
    }
  }

  /**
   * Send a friend request to a target user.
   */
  fun sendFriendRequest(targetUser: UserProfile, myUser: UserProfile): Boolean {
    val existing = _friendRequests.value.find {
      (it.senderId == myUser.id && it.receiverId == targetUser.id) ||
          (it.senderId == targetUser.id && it.receiverId == myUser.id)
    }
    if (existing != null) return false

    val request = FriendRequest(
      id = "req_${UUID.randomUUID().toString().take(8)}",
      senderId = myUser.id,
      senderName = myUser.name,
      senderUsername = myUser.username.ifBlank { "user_me" },
      senderEmoji = myUser.avatarEmoji,
      senderColorHex = myUser.avatarColorHex,
      receiverId = targetUser.id,
      timestamp = System.currentTimeMillis(),
      status = FriendRequestStatus.PENDING
    )
    _friendRequests.value = listOf(request) + _friendRequests.value
    return true
  }

  /**
   * Accept an incoming friend request and add user to contacts.
   */
  fun acceptFriendRequest(requestId: String) {
    val req = _friendRequests.value.find { it.id == requestId } ?: return
    _friendRequests.value = _friendRequests.value.map {
      if (it.id == requestId) it.copy(status = FriendRequestStatus.ACCEPTED) else it
    }

    val senderUser = globalDirectory.find { it.id == req.senderId } ?: UserProfile(
      id = req.senderId,
      name = req.senderName,
      phone = "+91 90000 00000",
      username = req.senderUsername,
      avatarEmoji = req.senderEmoji,
      avatarColorHex = req.senderColorHex,
      isOnline = true
    )
    ChatMitraRepository.addContact(senderUser)
  }

  /**
   * Reject a friend request.
   */
  fun rejectFriendRequest(requestId: String) {
    _friendRequests.value = _friendRequests.value.map {
      if (it.id == requestId) it.copy(status = FriendRequestStatus.REJECTED) else it
    }
  }

  /**
   * Synchronize system contacts from Android ContactsContract (or fallback gracefully).
   */
  fun syncContacts(context: Context) {
    _isSyncingContacts.value = true
    try {
      val resolver = context.contentResolver
      val cursor = resolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
          ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
          ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
      )

      val deviceList = mutableListOf<UserProfile>()
      cursor?.use { c ->
        val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (c.moveToNext()) {
          val name = if (nameIdx != -1) c.getString(nameIdx) else "Contact"
          val phone = if (numIdx != -1) c.getString(numIdx) else ""
          if (phone.isNotBlank()) {
            deviceList.add(
              UserProfile(
                id = "dev_${UUID.randomUUID().toString().take(6)}",
                name = name,
                phone = phone,
                username = name.lowercase().replace(" ", "_"),
                about = "Found via Contacts Sync",
                avatarColorHex = 0xFF00A884,
                avatarEmoji = "📱"
              )
            )
          }
        }
      }
      _syncedDeviceContacts.value = if (deviceList.isNotEmpty()) deviceList else globalDirectory.take(4)
    } catch (_: Exception) {
      // In case permission was not granted, use graceful fallback directory contacts
      _syncedDeviceContacts.value = globalDirectory.take(4)
    } finally {
      _isSyncingContacts.value = false
    }
  }

  /**
   * Returns standard QR Code deep-link URI for profile sharing.
   */
  fun getProfileDeepLink(user: UserProfile): String {
    val handle = user.username.ifBlank { user.id }
    return "chatmitra://profile/$handle"
  }
}
