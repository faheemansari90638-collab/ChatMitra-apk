package com.example.ui.friends

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FriendRequest
import com.example.data.model.FriendRequestStatus
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.FriendContactRepository
import com.example.data.repository.LocalizationRepository
import com.example.data.repository.UserBlockRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsFriendsScreen(
  onBackClick: () -> Unit,
  onStartChat: (UserProfile) -> Unit
) {
  val context = LocalContext.current
  val currentUser by ChatMitraRepository.currentUser.collectAsState()
  val contacts by ChatMitraRepository.contacts.collectAsState()
  val allFriendRequests by FriendContactRepository.friendRequests.collectAsState()
  val blockedUserIds by ChatMitraRepository.blockedContactIds.collectAsState()

  val incomingRequests = allFriendRequests.filter { it.receiverId == currentUser?.id && it.status == FriendRequestStatus.PENDING }
  val outgoingRequests = allFriendRequests.filter { it.senderId == currentUser?.id }

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf(
    LocalizationRepository.t("tab_contacts", "Contacts"),
    LocalizationRepository.t("tab_requests", "Requests"),
    LocalizationRepository.t("tab_discover", "Discover"),
    LocalizationRepository.t("tab_qr", "QR Share")
  )

  // Contacts Permission Launcher
  val contactPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      FriendContactRepository.syncContacts(context)
      Toast.makeText(context, "Contacts synchronized", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Contacts permission denied", Toast.LENGTH_SHORT).show()
    }
  }

  // Username Search State
  var searchQuery by remember { mutableStateOf("") }
  var searchResult by remember { mutableStateOf<UserProfile?>(null) }
  var searchAttempted by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            LocalizationRepository.t("contacts_and_friends", "Contacts & Friends"),
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(
            onClick = {
              contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            modifier = Modifier.testTag("sync_contacts_button")
          ) {
            Icon(Icons.Default.Sync, contentDescription = "Sync Contacts", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Tab Row
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = TealPrimary,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = TealPrimary
          )
        }
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = if (index == 1 && incomingRequests.isNotEmpty()) "$title (${incomingRequests.size})" else title,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }

      when (selectedTab) {
        0 -> ContactsListTab(
          contacts = contacts,
          blockedUserIds = blockedUserIds,
          onStartChat = onStartChat,
          onToggleBlock = { contactId ->
            if (blockedUserIds.contains(contactId)) {
              ChatMitraRepository.unblockContact(contactId)
              Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
            } else {
              ChatMitraRepository.blockContact(contactId)
              Toast.makeText(context, "User blocked", Toast.LENGTH_SHORT).show()
            }
          },
          onRequestSync = { contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
        )

        1 -> FriendRequestsTab(
          incoming = incomingRequests,
          outgoing = outgoingRequests,
          onAccept = { req ->
            FriendContactRepository.acceptFriendRequest(req.id)
            Toast.makeText(context, "Request accepted!", Toast.LENGTH_SHORT).show()
          },
          onReject = { req ->
            FriendContactRepository.rejectFriendRequest(req.id)
            Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show()
          }
        )

        2 -> DiscoverUsersTab(
          query = searchQuery,
          onQueryChange = {
            searchQuery = it
            searchAttempted = false
          },
          onSearch = {
            searchAttempted = true
            searchResult = FriendContactRepository.searchByUsername(searchQuery).firstOrNull()
          },
          searchResult = searchResult,
          searchAttempted = searchAttempted,
          currentUser = currentUser,
          onSendRequest = { user ->
            currentUser?.let { me ->
              val sent = FriendContactRepository.sendFriendRequest(targetUser = user, myUser = me)
              if (sent) {
                Toast.makeText(context, "Friend request sent to @${user.username}!", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "Request already pending or existing friend", Toast.LENGTH_SHORT).show()
              }
            }
          }
        )

        3 -> QrShareTab(
          user = currentUser,
          onShareLink = { link ->
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, "Connect with me on ChatMitra: $link")
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share ChatMitra Profile"))
          },
          onCopyLink = { link ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("ChatMitra Profile", link)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Profile link copied!", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }
  }
}

@Composable
private fun ContactsListTab(
  contacts: List<UserProfile>,
  blockedUserIds: Set<String>,
  onStartChat: (UserProfile) -> Unit,
  onToggleBlock: (String) -> Unit,
  onRequestSync: () -> Unit
) {
  if (contacts.isEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        Icons.Default.Sync,
        contentDescription = null,
        tint = TealPrimary,
        modifier = Modifier.size(56.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        "No contacts found",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "Sync phone contacts or search usernames to connect with friends.",
        textAlign = TextAlign.Center,
        color = CheckmarkGrey,
        fontSize = 14.sp
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = onRequestSync,
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Icon(Icons.Default.Sync, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sync Phone Contacts")
      }
    }
    return
  }

  LazyColumn(modifier = Modifier.fillMaxSize()) {
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "${contacts.size} Contact(s)",
          fontWeight = FontWeight.SemiBold,
          color = TealPrimary,
          fontSize = 13.sp
        )
        OutlinedButton(
          onClick = onRequestSync,
          shape = RoundedCornerShape(16.dp)
        ) {
          Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Sync", fontSize = 12.sp)
        }
      }
      HorizontalDivider()
    }

    items(contacts, key = { it.id }) { contact ->
      val isBlocked = blockedUserIds.contains(contact.id)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { if (!isBlocked) onStartChat(contact) }
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        UserAvatar(
          name = contact.name,
          colorHex = contact.avatarColorHex,
          emoji = contact.avatarEmoji,
          size = 48.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = contact.name,
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (isBlocked) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "(Blocked)",
                color = CallRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          if (contact.username.isNotBlank()) {
            Text(
              text = "@${contact.username}",
              color = TealPrimary,
              fontSize = 13.sp
            )
          }
          Text(
            text = contact.about,
            color = CheckmarkGrey,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        IconButton(
          onClick = { onToggleBlock(contact.id) },
          modifier = Modifier.testTag("toggle_block_${contact.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Block,
            contentDescription = if (isBlocked) "Unblock" else "Block",
            tint = if (isBlocked) CallRed else CheckmarkGrey
          )
        }

        if (!isBlocked) {
          IconButton(onClick = { onStartChat(contact) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Chat,
              contentDescription = "Chat",
              tint = TealPrimary
            )
          }
        }
      }
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    }
  }
}

@Composable
private fun FriendRequestsTab(
  incoming: List<FriendRequest>,
  outgoing: List<FriendRequest>,
  onAccept: (FriendRequest) -> Unit,
  onReject: (FriendRequest) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    item {
      Text(
        "Incoming Requests (${incoming.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TealPrimary
      )
      Spacer(modifier = Modifier.height(8.dp))
    }

    if (incoming.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Text(
            "No pending incoming friend requests.",
            color = CheckmarkGrey,
            fontSize = 13.sp,
            modifier = Modifier.padding(16.dp)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    } else {
      items(incoming, key = { it.id }) { req ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UserAvatar(
              name = req.senderName,
              colorHex = req.senderColorHex,
              emoji = req.senderEmoji,
              size = 44.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(req.senderName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("@${req.senderUsername}", color = TealPrimary, fontSize = 13.sp)
            }
            IconButton(
              onClick = { onAccept(req) },
              modifier = Modifier
                .size(36.dp)
                .background(TealPrimary, CircleShape)
            ) {
              Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
              onClick = { onReject(req) },
              modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(20.dp))
            }
          }
        }
      }
      item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    item {
      Text(
        "Outgoing Requests (${outgoing.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TealPrimary
      )
      Spacer(modifier = Modifier.height(8.dp))
    }

    if (outgoing.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Text(
            "No pending sent requests.",
            color = CheckmarkGrey,
            fontSize = 13.sp,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(outgoing, key = { it.id }) { req ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Request to user: ${req.receiverId}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
              Text("Status: ${req.status}", color = AmberAccent, fontSize = 12.sp)
            }
            Text("Pending", fontSize = 12.sp, color = AmberAccent, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun DiscoverUsersTab(
  query: String,
  onQueryChange: (String) -> Unit,
  onSearch: () -> Unit,
  searchResult: UserProfile?,
  searchAttempted: Boolean,
  currentUser: UserProfile?,
  onSendRequest: (UserProfile) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Text(
      "Search by @username",
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = TealPrimary
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      "Connect with friends anywhere by entering their unique ChatMitra username.",
      color = CheckmarkGrey,
      fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("e.g. rahul_dev or priya") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary) },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("username_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = TealPrimary,
          cursorColor = TealPrimary
        )
      )
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = onSearch,
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        modifier = Modifier.testTag("username_search_submit")
      ) {
        Text("Find")
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (searchAttempted) {
      if (searchResult != null) {
        val isSelf = currentUser?.id == searchResult.id
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(16.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UserAvatar(
              name = searchResult.name,
              colorHex = searchResult.avatarColorHex,
              emoji = searchResult.avatarEmoji,
              size = 56.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(searchResult.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text("@${searchResult.username}", color = TealPrimary, fontSize = 14.sp)
              Text(searchResult.about, color = CheckmarkGrey, fontSize = 12.sp)
            }

            if (!isSelf) {
              Button(
                onClick = { onSendRequest(searchResult) },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_friend_btn_${searchResult.id}")
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
              }
            } else {
              Text("You", color = TealPrimary, fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("No user found matching '$query'", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Make sure the handle is spelled correctly.", color = CheckmarkGrey, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun QrShareTab(
  user: UserProfile?,
  onShareLink: (String) -> Unit,
  onCopyLink: (String) -> Unit
) {
  val defaultUser = user ?: UserProfile(id = "user_me", name = "ChatMitra User", username = "me")
  val profileLink = FriendContactRepository.getProfileDeepLink(defaultUser)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        UserAvatar(
          name = defaultUser.name,
          colorHex = defaultUser.avatarColorHex,
          emoji = defaultUser.avatarEmoji,
          size = 64.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(defaultUser.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("@${defaultUser.username}", color = TealPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Decorative Vector QR Code Pattern Canvas
        Box(
          modifier = Modifier
            .size(200.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          QrCodeVectorPattern(text = profileLink)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
          "Scan or share to connect instantly without phone numbers.",
          textAlign = TextAlign.Center,
          color = CheckmarkGrey,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedButton(
        onClick = { onCopyLink(profileLink) },
        modifier = Modifier
          .weight(1f)
          .testTag("copy_profile_link_btn"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy Link")
      }

      Button(
        onClick = { onShareLink(profileLink) },
        modifier = Modifier
          .weight(1f)
          .testTag("share_profile_btn"),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Share QR")
      }
    }
  }
}

@Composable
private fun QrCodeVectorPattern(text: String) {
  val hash = text.hashCode()
  Canvas(modifier = Modifier.fillMaxSize()) {
    val gridSize = 15
    val cellSize = size.width / gridSize
    val darkColor = Color(0xFF1E293B)

    fun drawFinder(startX: Float, startY: Float) {
      drawRoundRect(
        color = darkColor,
        topLeft = Offset(startX, startY),
        size = Size(cellSize * 4, cellSize * 4),
        cornerRadius = CornerRadius(6f, 6f)
      )
      drawRoundRect(
        color = Color.White,
        topLeft = Offset(startX + cellSize * 0.75f, startY + cellSize * 0.75f),
        size = Size(cellSize * 2.5f, cellSize * 2.5f),
        cornerRadius = CornerRadius(4f, 4f)
      )
      drawRoundRect(
        color = darkColor,
        topLeft = Offset(startX + cellSize * 1.35f, startY + cellSize * 1.35f),
        size = Size(cellSize * 1.3f, cellSize * 1.3f),
        cornerRadius = CornerRadius(2f, 2f)
      )
    }

    drawFinder(0f, 0f)
    drawFinder(size.width - cellSize * 4, 0f)
    drawFinder(0f, size.height - cellSize * 4)

    var seed = hash.toLong()
    for (r in 0 until gridSize) {
      for (c in 0 until gridSize) {
        val inFinder1 = r < 5 && c < 5
        val inFinder2 = r < 5 && c >= gridSize - 5
        val inFinder3 = r >= gridSize - 5 && c < 5
        if (!inFinder1 && !inFinder2 && !inFinder3) {
          seed = (seed * 1103515245 + 12345) and 0x7fffffff
          if (seed % 3 == 0L || (r + c) % 2 == 0) {
            drawRoundRect(
              color = darkColor,
              topLeft = Offset(c * cellSize + cellSize * 0.1f, r * cellSize + cellSize * 0.1f),
              size = Size(cellSize * 0.8f, cellSize * 0.8f),
              cornerRadius = CornerRadius(2f, 2f)
            )
          }
        }
      }
    }
  }
}
