package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.R
import com.example.data.model.CallParticipant
import com.example.data.model.ParticipantRole
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.FriendContactRepository
import com.example.data.repository.GroupCallingRepository
import com.example.data.repository.LocalizationRepository
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.calls.ActiveCallScreen
import com.example.ui.calls.CallsScreen
import com.example.ui.calls.GroupVideoCallScreen
import com.example.ui.chatdetail.ChatDetailScreen
import com.example.ui.chats.ChatListScreen
import com.example.ui.chats.NewChatModal
import com.example.ui.chats.LockedChatsScreen
import com.example.ui.components.NetworkStatusBanner
import com.example.ui.friends.ContactsFriendsScreen
import com.example.ui.group.CreateGroupScreen
import com.example.ui.group.GroupInfoScreen
import com.example.ui.ai.HamzaAiScreen
import com.example.ui.settings.AccountSettingsScreen
import com.example.ui.settings.AppDownloadUpdatesScreen
import com.example.ui.settings.AppearanceSettingsScreen
import com.example.ui.settings.BlockedContactsScreen
import com.example.ui.settings.CallsSettingsScreen
import com.example.ui.settings.ChatBackupScreen
import com.example.ui.settings.DeleteAccountScreen
import com.example.ui.settings.EmailAddressScreen
import com.example.ui.settings.ExportDataScreen
import com.example.ui.settings.FontSettingsScreen
import com.example.ui.settings.InviteFriendsScreen
import com.example.ui.settings.LanguageSettingsScreen
import com.example.ui.settings.LinkedDevicesScreen
import com.example.ui.settings.NotificationsSettingsScreen
import com.example.ui.settings.PrivacyCheckupScreen
import com.example.ui.settings.PrivacySecurityScreen
import com.example.ui.settings.RestoreBackupScreen
import com.example.ui.settings.SecurityDashboardScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.StorageDataScreen
import com.example.ui.settings.TwoStepVerificationScreen
import com.example.ui.status.StatusScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

enum class MainTab(val title: String) {
  CHATS("Chats"),
  UPDATES("Updates"),
  CALLS("Calls"),
  SETTINGS("Settings")
}

sealed class AppDestination {
  object Main : AppDestination()
  data class ChatDetail(val chatId: String) : AppDestination()
  object CreateGroup : AppDestination()
  data class GroupInfo(val chatId: String) : AppDestination()
  object PrivacySettings : AppDestination()
  object SecurityDashboard : AppDestination()
  object BlockedContacts : AppDestination()
  object LinkedDevices : AppDestination()
  object TwoStepVerification : AppDestination()
  object PrivacyCheckup : AppDestination()
  object LockedChats : AppDestination()
  object AccountSettings : AppDestination()
  object EmailAddress : AppDestination()
  object ChatBackup : AppDestination()
  object RestoreBackup : AppDestination()
  object StorageData : AppDestination()
  object ExportData : AppDestination()
  object DeleteAccount : AppDestination()
  object HamzaAi : AppDestination()
  object AppearanceSettings : AppDestination()
  object FontSettings : AppDestination()
  object InviteFriends : AppDestination()
  object AppDownloadUpdates : AppDestination()
  object CallsSettings : AppDestination()
  object NotificationsSettings : AppDestination()
  object ContactsFriends : AppDestination()
  object GroupVideoCall : AppDestination()
  object AdminDashboard : AppDestination()
  object LanguageSettings : AppDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMitraApp() {
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val chats by ChatMitraRepository.chats.collectAsState()
  val activeCall by ChatMitraRepository.activeCall.collectAsState()
  val currentLanguage by LocalizationRepository.currentLanguage.collectAsState()
  val groupCallSession by GroupCallingRepository.activeGroupCall.collectAsState()

  var currentTab by remember { mutableStateOf(MainTab.CHATS) }
  var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Main) }
  var showNewChatModal by remember { mutableStateOf(false) }
  var showMainOverflowMenu by remember { mutableStateOf(false) }

  // If not logged in, show Auth
  if (currentUser == null) {
    AuthScreen(
      onLoginSuccess = {
        currentDestination = AppDestination.Main
        currentTab = MainTab.CHATS
      }
    )
    return
  }

  // Active Call Screen Overlay (Shown when outside ChatDetail screen)
  if (activeCall != null && currentDestination !is AppDestination.ChatDetail) {
    ActiveCallScreen(onDismiss = {})
    return
  }

  // Sub-screen BackHandler & Routing
  when (val destination = currentDestination) {
    is AppDestination.ChatDetail -> {
      BackHandler { currentDestination = AppDestination.Main }
      ChatDetailScreen(
        chatId = destination.chatId,
        onBackClick = { currentDestination = AppDestination.Main },
        onAudioCallClick = { contact ->
          ChatMitraRepository.startCall(contact, isVideo = false)
        },
        onVideoCallClick = { contact ->
          ChatMitraRepository.startCall(contact, isVideo = true)
        },
        onViewContactClick = { id ->
          currentDestination = AppDestination.GroupInfo(id)
        }
      )
      return
    }

    is AppDestination.CreateGroup -> {
      BackHandler { currentDestination = AppDestination.Main }
      CreateGroupScreen(
        onBackClick = { currentDestination = AppDestination.Main },
        onGroupCreated = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.GroupInfo -> {
      BackHandler { currentDestination = AppDestination.Main }
      GroupInfoScreen(
        chatId = destination.chatId,
        onBackClick = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.PrivacySettings -> {
      BackHandler { currentDestination = AppDestination.Main }
      PrivacySecurityScreen(
        onBackClick = { currentDestination = AppDestination.Main },
        onNavigateSecurityDashboard = { currentDestination = AppDestination.SecurityDashboard },
        onNavigateBlockedContacts = { currentDestination = AppDestination.BlockedContacts },
        onNavigateTwoStep = { currentDestination = AppDestination.TwoStepVerification },
        onNavigatePrivacyCheckup = { currentDestination = AppDestination.PrivacyCheckup }
      )
      return
    }

    is AppDestination.SecurityDashboard -> {
      BackHandler { currentDestination = AppDestination.PrivacySettings }
      SecurityDashboardScreen(
        onBackClick = { currentDestination = AppDestination.PrivacySettings },
        onNavigateToPrivacyCheckup = { currentDestination = AppDestination.PrivacyCheckup },
        onNavigateToTwoStep = { currentDestination = AppDestination.TwoStepVerification },
        onNavigateToLinkedDevices = { currentDestination = AppDestination.LinkedDevices },
        onNavigateToBlockedContacts = { currentDestination = AppDestination.BlockedContacts },
        onNavigateToPrivacy = { currentDestination = AppDestination.PrivacySettings }
      )
      return
    }

    is AppDestination.BlockedContacts -> {
      BackHandler { currentDestination = AppDestination.PrivacySettings }
      BlockedContactsScreen(
        onBackClick = { currentDestination = AppDestination.PrivacySettings }
      )
      return
    }

    is AppDestination.LinkedDevices -> {
      BackHandler { currentDestination = AppDestination.SecurityDashboard }
      LinkedDevicesScreen(
        onBackClick = { currentDestination = AppDestination.SecurityDashboard }
      )
      return
    }

    is AppDestination.TwoStepVerification -> {
      BackHandler { currentDestination = AppDestination.PrivacySettings }
      TwoStepVerificationScreen(
        onBackClick = { currentDestination = AppDestination.PrivacySettings }
      )
      return
    }

    is AppDestination.PrivacyCheckup -> {
      BackHandler { currentDestination = AppDestination.PrivacySettings }
      PrivacyCheckupScreen(
        onBackClick = { currentDestination = AppDestination.PrivacySettings },
        onNavigateToPrivacy = { currentDestination = AppDestination.PrivacySettings },
        onNavigateToTwoStep = { currentDestination = AppDestination.TwoStepVerification }
      )
      return
    }

    is AppDestination.LockedChats -> {
      BackHandler { currentDestination = AppDestination.Main }
      LockedChatsScreen(
        onBackClick = { currentDestination = AppDestination.Main },
        onChatClick = { chatId ->
          currentDestination = AppDestination.ChatDetail(chatId)
        }
      )
      return
    }

    is AppDestination.AccountSettings -> {
      BackHandler { currentDestination = AppDestination.Main }
      AccountSettingsScreen(
        onBackClick = { currentDestination = AppDestination.Main },
        onNavigateEmail = { currentDestination = AppDestination.EmailAddress },
        onNavigateTwoStep = { currentDestination = AppDestination.TwoStepVerification },
        onNavigateLinkedDevices = { currentDestination = AppDestination.LinkedDevices },
        onNavigateExportData = { currentDestination = AppDestination.ExportData },
        onNavigateDeleteAccount = { currentDestination = AppDestination.DeleteAccount }
      )
      return
    }

    is AppDestination.EmailAddress -> {
      BackHandler { currentDestination = AppDestination.AccountSettings }
      EmailAddressScreen(
        onBackClick = { currentDestination = AppDestination.AccountSettings }
      )
      return
    }

    is AppDestination.ChatBackup -> {
      BackHandler { currentDestination = AppDestination.Main }
      ChatBackupScreen(
        onBackClick = { currentDestination = AppDestination.Main },
        onNavigateRestore = { currentDestination = AppDestination.RestoreBackup }
      )
      return
    }

    is AppDestination.RestoreBackup -> {
      BackHandler { currentDestination = AppDestination.ChatBackup }
      RestoreBackupScreen(
        onBackClick = { currentDestination = AppDestination.ChatBackup }
      )
      return
    }

    is AppDestination.StorageData -> {
      BackHandler { currentDestination = AppDestination.Main }
      StorageDataScreen(
        onBackClick = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.ExportData -> {
      BackHandler { currentDestination = AppDestination.AccountSettings }
      ExportDataScreen(
        onBackClick = { currentDestination = AppDestination.AccountSettings }
      )
      return
    }

    is AppDestination.DeleteAccount -> {
      BackHandler { currentDestination = AppDestination.AccountSettings }
      DeleteAccountScreen(
        onBackClick = { currentDestination = AppDestination.AccountSettings },
        onAccountDeleted = {
          currentDestination = AppDestination.Main
          currentTab = MainTab.CHATS
        }
      )
      return
    }

    is AppDestination.HamzaAi -> {
      BackHandler { currentDestination = AppDestination.Main }
      HamzaAiScreen(
        onNavigateBack = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.AppearanceSettings -> {
      BackHandler { currentDestination = AppDestination.Main }
      AppearanceSettingsScreen(
        onNavigateBack = { currentDestination = AppDestination.Main },
        onNavigateFontSettings = { currentDestination = AppDestination.FontSettings }
      )
      return
    }

    is AppDestination.FontSettings -> {
      BackHandler { currentDestination = AppDestination.AppearanceSettings }
      FontSettingsScreen(
        onNavigateBack = { currentDestination = AppDestination.AppearanceSettings }
      )
      return
    }

    is AppDestination.InviteFriends -> {
      BackHandler { currentDestination = AppDestination.Main }
      InviteFriendsScreen(
        onNavigateBack = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.AppDownloadUpdates -> {
      BackHandler { currentDestination = AppDestination.Main }
      AppDownloadUpdatesScreen(
        onNavigateBack = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.CallsSettings -> {
      BackHandler { currentDestination = AppDestination.Main }
      CallsSettingsScreen(
        onNavigateBack = { currentDestination = AppDestination.Main },
        onNavigateCallHistory = {
          currentDestination = AppDestination.Main
          currentTab = MainTab.CALLS
        }
      )
      return
    }

    is AppDestination.NotificationsSettings -> {
      BackHandler { currentDestination = AppDestination.Main }
      NotificationsSettingsScreen(
        onNavigateBack = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.ContactsFriends -> {
      BackHandler { currentDestination = AppDestination.Main }
      ContactsFriendsScreen(
        onBackClick = { currentDestination = AppDestination.Main },
        onStartChat = { contact ->
          val existingChat = chats.find { !it.isGroup && it.participants.any { p -> p.id == contact.id } }
          if (existingChat != null) {
            currentDestination = AppDestination.ChatDetail(existingChat.id)
          } else {
            val newChatId = "chat_${contact.id}"
            ChatMitraRepository.sendMessage(newChatId, "Hey ${contact.name}! Connected on ChatMitra 👋")
            currentDestination = AppDestination.ChatDetail(newChatId)
          }
        }
      )
      return
    }

    is AppDestination.GroupVideoCall -> {
      BackHandler { currentDestination = AppDestination.Main }
      GroupVideoCallScreen(
        onDismiss = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.AdminDashboard -> {
      BackHandler { currentDestination = AppDestination.Main }
      AdminDashboardScreen(
        onBackClick = { currentDestination = AppDestination.Main }
      )
      return
    }

    is AppDestination.LanguageSettings -> {
      BackHandler { currentDestination = AppDestination.Main }
      LanguageSettingsScreen(
        onBackClick = { currentDestination = AppDestination.Main }
      )
      return
    }

    AppDestination.Main -> {
      // Main scaffold with TopAppBar and Bottom Navigation
      val totalUnread = chats.sumOf { it.unreadCount }

      Scaffold(
        topBar = {
          androidx.compose.foundation.layout.Column {
            TopAppBar(
              title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Image(
                    painter = painterResource(id = R.drawable.chatmitra_logo),
                    contentDescription = "ChatMitra Logo",
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "ChatMitra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                  )
                }
              },
              actions = {
                IconButton(onClick = { /* camera shortcut */ }) {
                  Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White)
                }
                IconButton(onClick = { showMainOverflowMenu = true }) {
                  Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }

                DropdownMenu(
                  expanded = showMainOverflowMenu,
                  onDismissRequest = { showMainOverflowMenu = false }
                ) {
                  DropdownMenuItem(
                    text = { Text("New Group") },
                    onClick = {
                      showMainOverflowMenu = false
                      currentDestination = AppDestination.CreateGroup
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Contacts & Friends") },
                    onClick = {
                      showMainOverflowMenu = false
                      currentDestination = AppDestination.ContactsFriends
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Group Video Call (Max 8)") },
                    onClick = {
                      showMainOverflowMenu = false
                      currentUser?.let { me ->
                        GroupCallingRepository.startGroupCall(
                          title = "ChatMitra Lounge",
                          isVideo = true,
                          host = me
                        )
                      }
                      currentDestination = AppDestination.GroupVideoCall
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("App Language (भाषा / زبان)") },
                    onClick = {
                      showMainOverflowMenu = false
                      currentDestination = AppDestination.LanguageSettings
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Admin Console") },
                    onClick = {
                      showMainOverflowMenu = false
                      currentDestination = AppDestination.AdminDashboard
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                      showMainOverflowMenu = false
                      currentTab = MainTab.SETTINGS
                    }
                  )
                }
              },
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TealDark,
                titleContentColor = Color.White
              )
            )
            NetworkStatusBanner()
          }
        },
        bottomBar = {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.navigationBarsPadding()
          ) {
            // Chats Tab
            NavigationBarItem(
              selected = currentTab == MainTab.CHATS,
              onClick = { currentTab = MainTab.CHATS },
              icon = {
                if (totalUnread > 0) {
                  BadgedBox(
                    badge = {
                      Badge(
                        containerColor = TealPrimary,
                        contentColor = Color.White
                      ) {
                        Text("$totalUnread")
                      }
                    }
                  ) {
                    Icon(
                      if (currentTab == MainTab.CHATS) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                      contentDescription = "Chats"
                    )
                  }
                } else {
                  Icon(
                    if (currentTab == MainTab.CHATS) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Chats"
                  )
                }
              },
              label = { Text("Chats", fontWeight = if (currentTab == MainTab.CHATS) FontWeight.Bold else FontWeight.Normal) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ),
              modifier = Modifier.testTag("nav_chats_tab")
            )

            // Updates / Status Tab
            NavigationBarItem(
              selected = currentTab == MainTab.UPDATES,
              onClick = { currentTab = MainTab.UPDATES },
              icon = {
                Icon(
                  if (currentTab == MainTab.UPDATES) Icons.Default.Update else Icons.Outlined.Update,
                  contentDescription = "Updates"
                )
              },
              label = { Text("Updates", fontWeight = if (currentTab == MainTab.UPDATES) FontWeight.Bold else FontWeight.Normal) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ),
              modifier = Modifier.testTag("nav_updates_tab")
            )

            // Calls Tab
            NavigationBarItem(
              selected = currentTab == MainTab.CALLS,
              onClick = { currentTab = MainTab.CALLS },
              icon = {
                Icon(
                  if (currentTab == MainTab.CALLS) Icons.Default.Call else Icons.Outlined.Call,
                  contentDescription = "Calls"
                )
              },
              label = { Text("Calls", fontWeight = if (currentTab == MainTab.CALLS) FontWeight.Bold else FontWeight.Normal) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ),
              modifier = Modifier.testTag("nav_calls_tab")
            )

            // Settings Tab
            NavigationBarItem(
              selected = currentTab == MainTab.SETTINGS,
              onClick = { currentTab = MainTab.SETTINGS },
              icon = {
                Icon(
                  if (currentTab == MainTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                  contentDescription = "Settings"
                )
              },
              label = { Text("Settings", fontWeight = if (currentTab == MainTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ),
              modifier = Modifier.testTag("nav_settings_tab")
            )
          }
        }
      ) { innerPadding ->
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "tab_transition",
          modifier = Modifier.padding(innerPadding)
        ) { tab ->
          when (tab) {
            MainTab.CHATS -> {
              ChatListScreen(
                onChatClick = { chatId ->
                  currentDestination = AppDestination.ChatDetail(chatId)
                },
                onNewChatClick = { showNewChatModal = true },
                onCreateGroupClick = { currentDestination = AppDestination.CreateGroup },
                onLockedChatsClick = { currentDestination = AppDestination.LockedChats },
                onHamzaAiClick = { currentDestination = AppDestination.HamzaAi }
              )
            }

            MainTab.UPDATES -> {
              StatusScreen()
            }

            MainTab.CALLS -> {
              CallsScreen(
                onStartCall = { contact, isVideo ->
                  ChatMitraRepository.startCall(contact, isVideo)
                },
                onNewCallClick = { showNewChatModal = true }
              )
            }

            MainTab.SETTINGS -> {
              SettingsScreen(
                onNavigatePrivacy = { currentDestination = AppDestination.PrivacySettings },
                onNavigateAccount = { currentDestination = AppDestination.AccountSettings },
                onNavigateChatBackup = { currentDestination = AppDestination.ChatBackup },
                onNavigateStorageData = { currentDestination = AppDestination.StorageData },
                onNavigateDeleteAccount = { currentDestination = AppDestination.DeleteAccount },
                onNavigateNotifications = { currentDestination = AppDestination.NotificationsSettings },
                onNavigateCallsSettings = { currentDestination = AppDestination.CallsSettings },
                onNavigateHamzaAi = { currentDestination = AppDestination.HamzaAi },
                onNavigateAppearance = { currentDestination = AppDestination.AppearanceSettings },
                onNavigateInviteFriends = { currentDestination = AppDestination.InviteFriends },
                onNavigateAppUpdates = { currentDestination = AppDestination.AppDownloadUpdates },
                onNavigateLanguage = { currentDestination = AppDestination.LanguageSettings },
                onNavigateContactsFriends = { currentDestination = AppDestination.ContactsFriends },
                onNavigateAdminDashboard = { currentDestination = AppDestination.AdminDashboard },
                onLogout = {
                  currentDestination = AppDestination.Main
                  currentTab = MainTab.CHATS
                }
              )
            }
          }
        }
      }

      // New Chat Contact Picker Modal
      if (showNewChatModal) {
        NewChatModal(
          onDismiss = { showNewChatModal = false },
          onSelectContact = { contact ->
            // Open or create chat with contact
            val existingChat = chats.find { !it.isGroup && it.participants.any { p -> p.id == contact.id } }
            if (existingChat != null) {
              currentDestination = AppDestination.ChatDetail(existingChat.id)
            } else {
              val newChatId = "chat_${contact.id}"
              ChatMitraRepository.sendMessage(newChatId, "Hey ${contact.name}! Connected on ChatMitra 👋")
              currentDestination = AppDestination.ChatDetail(newChatId)
            }
          },
          onCreateGroupClick = {
            currentDestination = AppDestination.CreateGroup
          }
        )
      }
    }
  }
}
