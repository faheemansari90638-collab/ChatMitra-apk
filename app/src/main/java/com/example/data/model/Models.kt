package com.example.data.model

enum class MessageStatus {
  SENDING,
  SENT,
  DELIVERED,
  READ
}

enum class MessageType {
  TEXT,
  IMAGE,
  VIDEO,
  AUDIO,
  DOCUMENT,
  VOICE_NOTE
}

enum class CallDirection {
  INCOMING,
  OUTGOING,
  MISSED
}

enum class VisibilityOption(val label: String) {
  EVERYONE("Everyone"),
  CONTACTS("My Contacts"),
  CONTACTS_EXCEPT("My Contacts Except..."),
  NOBODY("Nobody")
}

enum class OnlineStatusOption(val label: String) {
  EVERYONE("Everyone"),
  SAME_AS_LAST_SEEN("Same as Last Seen")
}

enum class AppLockTimeout(val label: String, val millis: Long) {
  IMMEDIATELY("Immediately", 0L),
  AFTER_1_MIN("After 1 minute", 60_000L),
  AFTER_5_MIN("After 5 minutes", 300_000L),
  AFTER_30_MIN("After 30 minutes", 1_800_000L)
}

enum class DisappearingDuration(val label: String, val durationMillis: Long) {
  OFF("Off", 0L),
  HOURS_24("24 hours", 86_400_000L),
  DAYS_7("7 days", 604_800_000L),
  DAYS_90("90 days", 7_776_000_000L)
}

enum class NotificationPrivacy(val label: String) {
  SHOW_CONTENT("Show sender & message"),
  SENDER_ONLY("Show sender only"),
  NO_PREVIEW("No preview (Private)")
}

enum class ReportCategory(val label: String) {
  SPAM("Spam or Advertising"),
  HARASSMENT("Harassment or Bullying"),
  IMPERSONATION("Fake account or Impersonation"),
  ABUSIVE_CONTENT("Abusive, Violence or Hate Speech"),
  OTHER("Other reason")
}

enum class CallState(val label: String) {
  IDLE("Idle"),
  OUTGOING_CALLING("Calling..."),
  OUTGOING_RINGING("Ringing..."),
  CONNECTING("Connecting..."),
  CONNECTED("Connected"),
  RECONNECTING("Reconnecting..."),
  BUSY("Busy"),
  DECLINED("Call Declined"),
  UNAVAILABLE("Unavailable"),
  FAILED("Call Failed"),
  INCOMING_RINGING("Incoming Call..."),
  ENDED("Call Ended")
}

enum class VibrationPattern(val label: String) {
  OFF("Off"),
  DEFAULT("Default"),
  SHORT("Short vibration"),
  LONG("Long vibration"),
  RING_AND_VIBRATE("Ring and vibrate"),
  VIBRATE_FIRST_THEN_RING("Vibrate first, then ring")
}

enum class SilentModeScope(val label: String) {
  CALLS_ONLY("Calls only"),
  MESSAGES_ONLY("Messages only"),
  CALLS_AND_MESSAGES("Calls and messages")
}

enum class DndDuration(val label: String) {
  OFF("Off"),
  ONE_HOUR("For 1 hour"),
  EIGHT_HOURS("For 8 hours"),
  MANUAL("Until manually disabled"),
  CUSTOM_SCHEDULE("Custom schedule")
}

enum class AudioRoute(val label: String) {
  EARPIECE("Earpiece"),
  SPEAKER("Speakerphone"),
  BLUETOOTH("Bluetooth Audio")
}

enum class CallNetworkQuality(val label: String, val latencyMs: Int) {
  EXCELLENT("HD Voice • 14ms", 14),
  GOOD("Good • 38ms", 38),
  FAIR("Fair • 85ms", 85),
  POOR("Poor • 290ms", 290)
}

enum class AccountStatus {
  ACTIVE,
  SUSPENDED,
  BANNED
}

data class UserProfile(
  val id: String,
  val name: String,
  val phone: String = "",
  val about: String = "Hey there! I am using ChatMitra.",
  val avatarColorHex: Long = 0xFF00A884,
  val avatarEmoji: String = "👤",
  val isOnline: Boolean = false,
  val lastSeen: String = "recently",
  val publicKeyFingerprint: String = "4819 2038 9128 4471 9023 1184 7291 0029",
  val isVerified: Boolean = false,
  val username: String = "",
  val accountStatus: AccountStatus = AccountStatus.ACTIVE
)

data class Message(
  val id: String,
  val chatId: String,
  val senderId: String,
  val senderName: String,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val status: MessageStatus = MessageStatus.SENT,
  val type: MessageType = MessageType.TEXT,
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
  val readAt: Long? = null,
  val reactions: Map<String, Int> = emptyMap(),
  val expiresAt: Long? = null,
  val translatedText: String? = null,
  val translatedLanguage: String? = null,
  val isTranslating: Boolean = false,
  val showOriginal: Boolean = false
)

data class Chat(
  val id: String,
  val name: String,
  val isGroup: Boolean = false,
  val participants: List<UserProfile> = emptyList(),
  val lastMessage: Message? = null,
  val unreadCount: Int = 0,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val isMuted: Boolean = false,
  val isLocked: Boolean = false,
  val disappearingDuration: DisappearingDuration = DisappearingDuration.OFF,
  val draft: String = "",
  val groupDescription: String? = null,
  val groupAdmins: List<String> = emptyList(),
  val inviteLink: String? = null,
  val avatarColorHex: Long = 0xFF008069,
  val avatarEmoji: String = "💬"
)

data class StatusStory(
  val id: String,
  val userId: String,
  val userName: String,
  val userAvatarEmoji: String,
  val userAvatarColorHex: Long,
  val text: String,
  val bgGradientIndex: Int = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val expiresAt: Long = System.currentTimeMillis() + 86400000L, // 24 hours
  val viewers: List<UserProfile> = emptyList(),
  val isViewed: Boolean = false
)

data class CallLog(
  val id: String,
  val contactId: String,
  val contactName: String,
  val contactAvatarEmoji: String,
  val contactAvatarColorHex: Long,
  val isVideo: Boolean,
  val direction: CallDirection,
  val timestamp: Long,
  val durationFormatted: String = "00:00"
)

data class LinkedDevice(
  val id: String,
  val name: String,
  val platform: String,
  val location: String,
  val lastActiveFormatted: String,
  val isCurrentDevice: Boolean = false
)

data class SecurityAuditEvent(
  val id: String,
  val title: String,
  val description: String,
  val timestampFormatted: String,
  val isPositive: Boolean = true
)

data class PrivacySettings(
  val lastSeenVisibility: VisibilityOption = VisibilityOption.EVERYONE,
  val onlineStatusVisibility: OnlineStatusOption = OnlineStatusOption.EVERYONE,
  val profilePhotoVisibility: VisibilityOption = VisibilityOption.EVERYONE,
  val aboutVisibility: VisibilityOption = VisibilityOption.EVERYONE,
  val readReceipts: Boolean = true,
  val statusPrivacy: VisibilityOption = VisibilityOption.CONTACTS,
  val groupsAddPermission: VisibilityOption = VisibilityOption.EVERYONE,
  val silenceUnknownCallers: Boolean = false,
  val appLockEnabled: Boolean = false,
  val appLockTimeout: AppLockTimeout = AppLockTimeout.IMMEDIATELY,
  val appLockPin: String = "1234",
  val useBiometrics: Boolean = true,
  val disappearingMessagesDefault: DisappearingDuration = DisappearingDuration.OFF,
  val securityNotificationsEnabled: Boolean = true,
  val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.SHOW_CONTENT,
  val twoStepVerificationEnabled: Boolean = false,
  val twoStepVerificationPin: String = "",
  val recoveryEmail: String = "",
  val autoDownloadPhotosMobile: Boolean = true,
  val autoDownloadAudioMobile: Boolean = false,
  val autoDownloadVideoMobile: Boolean = false,
  val autoDownloadDocsMobile: Boolean = false,
  val autoDownloadWifi: Boolean = true,
  val saveMediaToGallery: Boolean = true
)

enum class VideoCallQuality(val label: String, val resolutionLabel: String, val bitrateKbps: Int) {
  AUTO("Auto (Adaptive)", "Auto HD", 1500),
  STANDARD_480P("Standard (SD)", "480p • 30fps", 800),
  HD_720P("High Definition (HD)", "720p • 30fps", 1800),
  FULL_HD_1080P("Full HD (FHD)", "1080p • 60fps", 3500)
}

data class ActiveCallSession(
  val callState: CallState = CallState.IDLE,
  val contact: UserProfile? = null,
  val isVideo: Boolean = false,
  val isMuted: Boolean = false,
  val isSpeakerOn: Boolean = false,
  val audioRoute: AudioRoute = AudioRoute.EARPIECE,
  val isVideoCameraOff: Boolean = false,
  val isFrontCamera: Boolean = true,
  val durationSeconds: Int = 0,
  val signalingState: String = "WebRTC STUN/TURN P2P Connected",
  val networkQuality: CallNetworkQuality = CallNetworkQuality.EXCELLENT,
  val isMinimized: Boolean = false,
  val callWaitingContact: UserProfile? = null,
  val isCallWaitingVideo: Boolean = false,
  val videoQuality: VideoCallQuality = VideoCallQuality.HD_720P,
  val localZoomRatio: Float = 1.0f,
  val remoteZoomRatio: Float = 1.0f,
  val isRemoteZoomActive: Boolean = false,
  val supportedMaxZoom: Float = 5.0f
)

data class CallNotificationSettings(
  val callRingtone: String = "ChatMitra Chime",
  val videoCallRingtone: String = "Marimba Pulse",
  val vibrationPattern: VibrationPattern = VibrationPattern.RING_AND_VIBRATE,
  val messageNotificationSound: String = "Gentle Pop",
  val groupNotificationSound: String = "Marimba Chime",
  val backupNotificationSound: String = "Crystal Ping",
  val isSilentMode: Boolean = false,
  val silentModeScope: SilentModeScope = SilentModeScope.CALLS_AND_MESSAGES,
  val silentModeMuteVibration: Boolean = false,
  val dndDuration: DndDuration = DndDuration.OFF,
  val dndExpiresAt: Long? = null,
  val dndMuteCalls: Boolean = true,
  val dndMuteVibration: Boolean = true,
  val dndMuteMessages: Boolean = true,
  val dndAllowStarredContacts: Boolean = true,
  val dndAllowRepeatCallers: Boolean = true,
  val notificationPreviewLockscreen: Boolean = true,
  val lowDataUsageForCalls: Boolean = false,
  val contactRingtones: Map<String, String> = emptyMap(),
  val mutedChats: Set<String> = emptySet()
)

data class EmailAccountState(
  val email: String = "hamzaansari6929@gmail.com",
  val isVerified: Boolean = true,
  val pendingVerificationEmail: String? = null,
  val verificationOtp: String = "481920",
  val otpCooldownUntil: Long = 0L,
  val emailVerificationAttempts: Int = 0,
  val lastVerificationTimeFormatted: String = "Yesterday at 14:32"
)

enum class BackupSchedule(val label: String) {
  OFF("Never"),
  DAILY("Daily"),
  WEEKLY("Weekly"),
  MONTHLY("Monthly")
}

enum class BackupDestination(val label: String) {
  GOOGLE_DRIVE("Google Drive (Cloud)"),
  LOCAL_STORAGE("Device Storage & Private Cloud")
}

enum class BackupStatus {
  IDLE,
  PREPARING,
  ENCRYPTING,
  UPLOADING,
  COMPLETED,
  FAILED
}

enum class RestoreStatus {
  IDLE,
  DOWNLOADING,
  DECRYPTING,
  MERGING,
  COMPLETED,
  FAILED
}

data class BackupRecord(
  val id: String,
  val timestamp: Long,
  val timestampFormatted: String,
  val totalSizeBytes: Long,
  val formattedSize: String,
  val messagesCount: Int,
  val mediaFilesCount: Int,
  val isEncrypted: Boolean,
  val destination: BackupDestination = BackupDestination.GOOGLE_DRIVE,
  val checksumHmac: String = "hmac_sha256_verified_94a73b",
  val isCloudOnly: Boolean = false
)

data class ChatBackupSettings(
  val autoBackupSchedule: BackupSchedule = BackupSchedule.DAILY,
  val destination: BackupDestination = BackupDestination.GOOGLE_DRIVE,
  val backupOverCellular: Boolean = false,
  val includeVideos: Boolean = true,
  val backupWithMedia: Boolean = true,
  val emailNotificationOnBackup: Boolean = true,
  val notifyOnBackupFailure: Boolean = true,
  val notifyOnRestoreOnNewDevice: Boolean = true,
  val isEndToEndEncrypted: Boolean = false,
  val hasPasswordProtection: Boolean = false,
  val backupPasswordHash: String = "",
  val recoveryKey64: String = "",
  val lastBackupTimestamp: Long = System.currentTimeMillis() - 86400000L,
  val lastBackupFormatted: String = "Yesterday at 02:00 AM",
  val totalBackupSizeBytes: Long = 18_420_000L,
  val totalMessagesCount: Int = 412,
  val totalMediaFilesCount: Int = 28
)

data class ChatStorageUsage(
  val chatId: String,
  val chatName: String,
  val avatarColorHex: Long,
  val avatarEmoji: String,
  val isGroup: Boolean,
  val totalSizeBytes: Long,
  val formattedSize: String,
  val messagesCount: Int,
  val photosCount: Int,
  val photosSizeBytes: Long,
  val voiceNotesCount: Int,
  val voiceNotesSizeBytes: Long,
  val videosCount: Int,
  val videosSizeBytes: Long,
  val docsCount: Int,
  val docsSizeBytes: Long
)

data class MediaItemStorage(
  val id: String,
  val name: String,
  val type: MessageType,
  val sizeBytes: Long,
  val formattedSize: String,
  val chatId: String,
  val chatName: String,
  val timestampFormatted: String
)

data class StorageOverview(
  val totalAppStorageBytes: Long = 68_500_000L,
  val chatAndMediaBytes: Long = 52_100_000L,
  val cacheAndTempBytes: Long = 16_400_000L,
  val deviceFreeStorageBytes: Long = 64_000_000_000L,
  val largestFiles: List<MediaItemStorage> = emptyList()
)

data class ScheduledMessage(
  val id: String,
  val chatId: String,
  val text: String,
  val scheduledTimeMillis: Long,
  val scheduledTimeFormatted: String
)

data class PollOption(
  val id: String,
  val text: String,
  val votesCount: Int = 0,
  val voterUserIds: List<String> = emptyList()
)

data class Poll(
  val id: String,
  val chatId: String,
  val question: String,
  val options: List<PollOption>,
  val allowMultipleAnswers: Boolean = false,
  val creatorId: String,
  val creatorName: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class ChatFolder(
  val id: String,
  val name: String,
  val isCustom: Boolean = false,
  val chatIds: Set<String> = emptySet()
)

enum class AppThemeMode(val label: String) {
  LIGHT("Light Theme"),
  DARK("Dark Theme"),
  SYSTEM("System Default")
}

enum class AppAccentColor(val label: String, val primaryHex: Long, val darkHex: Long) {
  TEAL_EMERALD("Emerald Teal", 0xFF00A884, 0xFF008069),
  SAPPHIRE_BLUE("Sapphire Blue", 0xFF2563EB, 0xFF1D4ED8),
  AMETHYST_PURPLE("Amethyst Purple", 0xFF9333EA, 0xFF7E22CE),
  CRIMSON_CORAL("Crimson Coral", 0xFFE11D48, 0xFFBE123C),
  SUNSET_AMBER("Sunset Amber", 0xFFF59E0B, 0xFFD97706)
}

enum class ChatWallpaper(val label: String, val bgHex: Long, val isDark: Boolean = false) {
  DEFAULT_DOODLE("Default Doodle Pattern", 0xFFEFEAE2, false),
  DARK_SLATE("Dark Slate", 0xFF0B141B, true),
  WARM_GRADIENT("Warm Peach", 0xFFFFF3E0, false),
  MINT_FRESH("Mint Fresh", 0xFFE8F5E9, false),
  MIDNIGHT_DEEP("Midnight Deep", 0xFF030712, true)
}

enum class BubbleStyle(val label: String, val cornerRadiusDp: Int) {
  ROUNDED("Rounded (Modern)", 18),
  COMPACT("Compact (Classic)", 8)
}

enum class MessageSpacing(val label: String, val spacingDp: Int) {
  COMPACT("Compact", 4),
  NORMAL("Normal", 8),
  SPACIOUS("Spacious", 14)
}

enum class FontSizePreset(val label: String, val messageSp: Int, val listSp: Int, val composerSp: Int) {
  SMALL("Small", 13, 14, 13),
  MEDIUM("Medium (Default)", 15, 16, 15),
  LARGE("Large", 17, 18, 17),
  EXTRA_LARGE("Extra Large", 19, 20, 19)
}

enum class FontFamilyChoice(val label: String) {
  SYSTEM_DEFAULT("System Default"),
  SANS_SERIF("Sans-Serif (Clean)"),
  SERIF("Serif (Classic)"),
  MONOSPACE("Monospace (Tech)")
}

data class AppearanceSettings(
  val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  val accentColor: AppAccentColor = AppAccentColor.TEAL_EMERALD,
  val chatWallpaper: ChatWallpaper = ChatWallpaper.DEFAULT_DOODLE,
  val bubbleStyle: BubbleStyle = BubbleStyle.ROUNDED,
  val messageSpacing: MessageSpacing = MessageSpacing.NORMAL,
  val isHighContrast: Boolean = false,
  val isReducedMotion: Boolean = false,
  val isCompactDensity: Boolean = false
)

data class FontSettings(
  val preset: FontSizePreset = FontSizePreset.MEDIUM,
  val chatMessageSizeSp: Int = 15,
  val chatListSizeSp: Int = 16,
  val composerSizeSp: Int = 15,
  val fontFamily: FontFamilyChoice = FontFamilyChoice.SYSTEM_DEFAULT,
  val isBoldText: Boolean = false
)

data class HamzaAiMessage(
  val id: String,
  val text: String,
  val isFromUser: Boolean,
  val timestamp: Long = System.currentTimeMillis(),
  val isStreaming: Boolean = false,
  val error: String? = null
)

data class TranslationLanguage(
  val code: String,
  val name: String,
  val nativeName: String,
  val isRtl: Boolean = false
)

// ==================== Contact & Friend Request Models ====================
enum class FriendRequestStatus {
  PENDING,
  ACCEPTED,
  REJECTED
}

data class FriendRequest(
  val id: String,
  val senderId: String,
  val senderName: String,
  val senderUsername: String,
  val senderEmoji: String = "👤",
  val senderColorHex: Long = 0xFF00A884,
  val receiverId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val status: FriendRequestStatus = FriendRequestStatus.PENDING
)

// ==================== Group Video Calling Models ====================
enum class ParticipantRole {
  HOST,
  CO_HOST,
  MEMBER
}

data class CallParticipant(
  val user: UserProfile,
  val isAudioMuted: Boolean = false,
  val isVideoMuted: Boolean = false,
  val isSpeaking: Boolean = false,
  val role: ParticipantRole = ParticipantRole.MEMBER,
  val networkQuality: CallNetworkQuality = CallNetworkQuality.EXCELLENT
)

data class GroupCallSession(
  val callId: String,
  val title: String,
  val isVideo: Boolean,
  val hostId: String,
  val participants: List<CallParticipant>,
  val maxParticipants: Int = 8, // Full-mesh WebRTC max capacity limit
  val activeSpeakerId: String? = null,
  val callQualityMode: String = "HD", // HD, SD, LOW_DATA
  val startTime: Long = System.currentTimeMillis()
)

// ==================== Admin & Moderation Models ====================
enum class ReportStatus {
  PENDING,
  INVESTIGATING,
  RESOLVED_DISMISSED,
  RESOLVED_ACTION_TAKEN
}

enum class AppealStatus {
  PENDING,
  APPROVED,
  REJECTED
}

data class UserReport(
  val id: String,
  val reportedUserId: String,
  val reportedUserName: String,
  val reporterUserId: String,
  val reporterUserName: String,
  val reason: ReportCategory,
  val notes: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val status: ReportStatus = ReportStatus.PENDING,
  val resolvedBy: String? = null,
  val resolutionNotes: String? = null
)

data class UserAppeal(
  val id: String,
  val userId: String,
  val userName: String,
  val userPhone: String,
  val reasonForSuspension: String,
  val appealExplanation: String,
  val timestamp: Long = System.currentTimeMillis(),
  val status: AppealStatus = AppealStatus.PENDING,
  val reviewNotes: String? = null
)

data class AdminAuditLog(
  val id: String,
  val adminId: String,
  val action: String,
  val targetId: String,
  val targetType: String,
  val timestamp: Long = System.currentTimeMillis(),
  val details: String
)

data class ServiceHealthMetrics(
  val databaseLatencyMs: Long = 18L,
  val firestoreStatus: String = "Operational (Healthy)",
  val webrtcSignalingLatencyMs: Long = 14L,
  val signalingStatus: String = "Connected (coturn-prod)",
  val activeUsersCount: Int = 1420,
  val activeCallsCount: Int = 38,
  val serverUptimePercent: Double = 99.98,
  val errorRatePercent: Double = 0.02
)

// ==================== Low Data Mode Models ====================
enum class AutoDownloadOption(val label: String) {
  NEVER("Never"),
  WIFI_ONLY("Wi-Fi Only"),
  WIFI_AND_CELLULAR("Wi-Fi and Cellular")
}

data class LowDataSettings(
  val isLowDataModeEnabled: Boolean = false,
  val autoDownloadPhotos: AutoDownloadOption = AutoDownloadOption.WIFI_ONLY,
  val autoDownloadAudio: AutoDownloadOption = AutoDownloadOption.WIFI_ONLY,
  val autoDownloadVideo: AutoDownloadOption = AutoDownloadOption.WIFI_ONLY,
  val autoDownloadDocuments: AutoDownloadOption = AutoDownloadOption.WIFI_ONLY,
  val compressMediaBeforeUpload: Boolean = true,
  val adaptiveCallBitrateEnabled: Boolean = true,
  val limitBackgroundData: Boolean = false
)

// ==================== Localization Models ====================
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
  ENGLISH("en", "English", "English", false),
  HINDI("hi", "Hindi", "हिन्दी", false),
  URDU("ur", "Urdu", "اردو", true),
  ARABIC("ar", "Arabic", "العربية", true)
}

// ==================== Reliability & Offline Queue Models ====================
data class QueuedMessage(
  val id: String,
  val clientMessageId: String,
  val chatId: String,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val retryCount: Int = 0,
  val lastError: String? = null
)

data class SanitizedCrashLog(
  val id: String,
  val timestamp: Long = System.currentTimeMillis(),
  val tag: String,
  val message: String,
  val category: String,
  val isFatal: Boolean = false
)
