package com.example.data.repository

import com.example.data.model.ChatStorageUsage
import com.example.data.model.MediaItemStorage
import com.example.data.model.MessageType
import com.example.data.model.StorageOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StorageManagementRepository private constructor() {

  private val _storageOverview = MutableStateFlow(
    StorageOverview(
      totalAppStorageBytes = 68_500_000L, // 68.5 MB
      chatAndMediaBytes = 52_100_000L,   // 52.1 MB
      cacheAndTempBytes = 16_400_000L,   // 16.4 MB
      deviceFreeStorageBytes = 64_000_000_000L, // 64 GB
      largestFiles = listOf(
        MediaItemStorage(
          id = "file_1",
          name = "Product_Demo_Walkthrough.mp4",
          type = MessageType.VIDEO,
          sizeBytes = 14_200_000L,
          formattedSize = "14.2 MB",
          chatId = "chat_group_tech",
          chatName = "Tech Innovators",
          timestampFormatted = "Yesterday, 3:15 PM"
        ),
        MediaItemStorage(
          id = "file_2",
          name = "Architecture_Specification.pdf",
          type = MessageType.DOCUMENT,
          sizeBytes = 8_900_000L,
          formattedSize = "8.9 MB",
          chatId = "chat_priya",
          chatName = "Priya Sharma",
          timestampFormatted = "2 days ago"
        ),
        MediaItemStorage(
          id = "file_3",
          name = "High_Res_Presentation_Deck.pdf",
          type = MessageType.DOCUMENT,
          sizeBytes = 7_400_000L,
          formattedSize = "7.4 MB",
          chatId = "chat_group_tech",
          chatName = "Tech Innovators",
          timestampFormatted = "3 days ago"
        ),
        MediaItemStorage(
          id = "file_4",
          name = "Team_Celebration_Photo.png",
          type = MessageType.IMAGE,
          sizeBytes = 5_800_000L,
          formattedSize = "5.8 MB",
          chatId = "chat_rahul",
          chatName = "Rahul Verma",
          timestampFormatted = "4 days ago"
        )
      )
    )
  )
  val storageOverview: StateFlow<StorageOverview> = _storageOverview.asStateFlow()

  private val _chatStorageUsages = MutableStateFlow<List<ChatStorageUsage>>(
    listOf(
      ChatStorageUsage(
        chatId = "chat_group_tech",
        chatName = "Tech Innovators",
        avatarColorHex = 0xFF008069,
        avatarEmoji = "⚡",
        isGroup = true,
        totalSizeBytes = 24_800_000L,
        formattedSize = "24.8 MB",
        messagesCount = 184,
        photosCount = 12,
        photosSizeBytes = 6_200_000L,
        voiceNotesCount = 8,
        voiceNotesSizeBytes = 3_100_000L,
        videosCount = 2,
        videosSizeBytes = 14_200_000L,
        docsCount = 3,
        docsSizeBytes = 1_300_000L
      ),
      ChatStorageUsage(
        chatId = "chat_priya",
        chatName = "Priya Sharma",
        avatarColorHex = 0xFFE91E63,
        avatarEmoji = "🌸",
        isGroup = false,
        totalSizeBytes = 15_400_000L,
        formattedSize = "15.4 MB",
        messagesCount = 142,
        photosCount = 6,
        photosSizeBytes = 4_100_000L,
        voiceNotesCount = 14,
        voiceNotesSizeBytes = 2_400_000L,
        videosCount = 0,
        videosSizeBytes = 0L,
        docsCount = 1,
        docsSizeBytes = 8_900_000L
      ),
      ChatStorageUsage(
        chatId = "chat_rahul",
        chatName = "Rahul Verma",
        avatarColorHex = 0xFF3F51B5,
        avatarEmoji = "🚀",
        isGroup = false,
        totalSizeBytes = 8_200_000L,
        formattedSize = "8.2 MB",
        messagesCount = 64,
        photosCount = 3,
        photosSizeBytes = 5_800_000L,
        voiceNotesCount = 6,
        voiceNotesSizeBytes = 1_800_000L,
        videosCount = 0,
        videosSizeBytes = 0L,
        docsCount = 1,
        docsSizeBytes = 600_000L
      ),
      ChatStorageUsage(
        chatId = "chat_ananya",
        chatName = "Ananya Patel",
        avatarColorHex = 0xFFFF9800,
        avatarEmoji = "🎨",
        isGroup = false,
        totalSizeBytes = 3_700_000L,
        formattedSize = "3.7 MB",
        messagesCount = 22,
        photosCount = 2,
        photosSizeBytes = 2_400_000L,
        voiceNotesCount = 4,
        voiceNotesSizeBytes = 1_300_000L,
        videosCount = 0,
        videosSizeBytes = 0L,
        docsCount = 0,
        docsSizeBytes = 0L
      )
    )
  )
  val chatStorageUsages: StateFlow<List<ChatStorageUsage>> = _chatStorageUsages.asStateFlow()

  fun clearCache(): Long {
    val freedBytes = _storageOverview.value.cacheAndTempBytes
    val current = _storageOverview.value
    _storageOverview.value = current.copy(
      cacheAndTempBytes = 0L,
      totalAppStorageBytes = (current.totalAppStorageBytes - freedBytes).coerceAtLeast(0L)
    )
    ChatMitraRepository.addSecurityAuditEvent(
      title = "Cache Cleared",
      description = "Freed %.1f MB of temporary thumbnails and cached files.".format(freedBytes / 1_000_000.0),
      isPositive = true
    )
    return freedBytes
  }

  fun deleteLargestFile(fileId: String) {
    val file = _storageOverview.value.largestFiles.find { it.id == fileId } ?: return
    val current = _storageOverview.value
    _storageOverview.value = current.copy(
      largestFiles = current.largestFiles.filterNot { it.id == fileId },
      chatAndMediaBytes = (current.chatAndMediaBytes - file.sizeBytes).coerceAtLeast(0L),
      totalAppStorageBytes = (current.totalAppStorageBytes - file.sizeBytes).coerceAtLeast(0L)
    )
  }

  fun deleteChatMediaItems(
    chatId: String,
    deletePhotos: Boolean,
    deleteAudio: Boolean,
    deleteVideos: Boolean,
    deleteDocs: Boolean
  ): Long {
    var totalFreed = 0L
    _chatStorageUsages.value = _chatStorageUsages.value.map { item ->
      if (item.chatId == chatId) {
        var newPhotosSize = item.photosSizeBytes
        var newPhotosCount = item.photosCount
        if (deletePhotos) {
          totalFreed += newPhotosSize
          newPhotosSize = 0L
          newPhotosCount = 0
        }

        var newAudioSize = item.voiceNotesSizeBytes
        var newAudioCount = item.voiceNotesCount
        if (deleteAudio) {
          totalFreed += newAudioSize
          newAudioSize = 0L
          newAudioCount = 0
        }

        var newVideosSize = item.videosSizeBytes
        var newVideosCount = item.videosCount
        if (deleteVideos) {
          totalFreed += newVideosSize
          newVideosSize = 0L
          newVideosCount = 0
        }

        var newDocsSize = item.docsSizeBytes
        var newDocsCount = item.docsCount
        if (deleteDocs) {
          totalFreed += newDocsSize
          newDocsSize = 0L
          newDocsCount = 0
        }

        val updatedTotal = newPhotosSize + newAudioSize + newVideosSize + newDocsSize
        item.copy(
          totalSizeBytes = updatedTotal,
          formattedSize = "%.1f MB".format(updatedTotal / 1_000_000.0),
          photosCount = newPhotosCount,
          photosSizeBytes = newPhotosSize,
          voiceNotesCount = newAudioCount,
          voiceNotesSizeBytes = newAudioSize,
          videosCount = newVideosCount,
          videosSizeBytes = newVideosSize,
          docsCount = newDocsCount,
          docsSizeBytes = newDocsSize
        )
      } else {
        item
      }
    }

    val currentOverview = _storageOverview.value
    _storageOverview.value = currentOverview.copy(
      chatAndMediaBytes = (currentOverview.chatAndMediaBytes - totalFreed).coerceAtLeast(0L),
      totalAppStorageBytes = (currentOverview.totalAppStorageBytes - totalFreed).coerceAtLeast(0L)
    )

    return totalFreed
  }

  companion object {
    @Volatile
    private var instance: StorageManagementRepository? = null

    fun getInstance(): StorageManagementRepository {
      return instance ?: synchronized(this) {
        instance ?: StorageManagementRepository().also { instance = it }
      }
    }
  }
}
