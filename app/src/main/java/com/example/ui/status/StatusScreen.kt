package com.example.ui.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StatusStory
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val statusGradients = listOf(
  listOf(Color(0xFF0F766E), Color(0xFF14B8A6)), // Teal
  listOf(Color(0xFF7C3AED), Color(0xFFA855F7)), // Purple
  listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)), // Blue
  listOf(Color(0xFFB45309), Color(0xFFF59E0B)), // Amber
  listOf(Color(0xFFBE185D), Color(0xFFF43F5E))  // Rose
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
  modifier: Modifier = Modifier
) {
  val myStories by ChatMitraRepository.myStories.collectAsState()
  val contactStories by ChatMitraRepository.statusStories.collectAsState()
  val currentUser by ChatMitraRepository.currentUser.collectAsState()

  var viewingStory by remember { mutableStateOf<StatusStory?>(null) }
  var showCreateStatusSheet by remember { mutableStateOf(false) }
  var showViewersSheetForStory by remember { mutableStateOf<StatusStory?>(null) }
  val sheetState = rememberModalBottomSheetState()

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      // My Status Section
      item {
        val latestMyStory = myStories.firstOrNull()
        ListItem(
          modifier = Modifier
            .clickable {
              if (latestMyStory != null) {
                viewingStory = latestMyStory
              } else {
                showCreateStatusSheet = true
              }
            }
            .testTag("my_status_item"),
          headlineContent = {
            Text(
              text = "My Status",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          },
          supportingContent = {
            if (latestMyStory != null) {
              val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
              Text(
                text = "${sdf.format(Date(latestMyStory.timestamp))} • ${latestMyStory.viewers.size} views",
                style = MaterialTheme.typography.bodySmall,
                color = CheckmarkGrey
              )
            } else {
              Text(
                text = "Tap to add status update",
                style = MaterialTheme.typography.bodySmall,
                color = CheckmarkGrey
              )
            }
          },
          leadingContent = {
            Box {
              UserAvatar(
                name = currentUser?.name ?: "Me",
                colorHex = currentUser?.avatarColorHex ?: 0xFF00A884,
                emoji = currentUser?.avatarEmoji ?: "✨",
                size = 52.dp,
                hasStatusRing = latestMyStory != null,
                isStatusViewed = false
              )
              if (latestMyStory == null) {
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(TealPrimary)
                    .align(Alignment.BottomEnd),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Add, contentDescription = "Add Status", tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }
            }
          },
          trailingContent = {
            if (latestMyStory != null) {
              IconButton(onClick = { showViewersSheetForStory = latestMyStory }) {
                Icon(Icons.Default.Visibility, contentDescription = "Viewers", tint = TealPrimary)
              }
            }
          }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      }

      // Recent Updates Header
      item {
        Text(
          text = "Recent updates",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
      }

      // Contact Stories
      items(contactStories, key = { it.id }) { story ->
        ListItem(
          modifier = Modifier
            .clickable {
              ChatMitraRepository.markStoryViewed(story.id)
              viewingStory = story
            }
            .testTag("status_story_${story.id}"),
          headlineContent = {
            Text(
              text = story.userName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          },
          supportingContent = {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            Text(
              text = "Today at ${sdf.format(Date(story.timestamp))} • Disappears in 24h",
              style = MaterialTheme.typography.bodySmall,
              color = CheckmarkGrey
            )
          },
          leadingContent = {
            UserAvatar(
              name = story.userName,
              colorHex = story.userAvatarColorHex,
              emoji = story.userAvatarEmoji,
              size = 52.dp,
              hasStatusRing = true,
              isStatusViewed = story.isViewed
            )
          }
        )
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }

    // Floating Action Buttons (Pencil & Camera)
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 16.dp, end = 16.dp),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      FloatingActionButton(
        onClick = { showCreateStatusSheet = true },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = TealPrimary,
        shape = CircleShape,
        modifier = Modifier
          .size(44.dp)
          .testTag("compose_status_pencil_fab")
      ) {
        Icon(Icons.Default.Edit, contentDescription = "Text Status", modifier = Modifier.size(20.dp))
      }

      FloatingActionButton(
        onClick = { showCreateStatusSheet = true },
        containerColor = TealPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(56.dp)
          .testTag("camera_status_fab")
      ) {
        Icon(Icons.Default.CameraAlt, contentDescription = "Camera Status")
      }
    }

    // Story Fullscreen Viewer Dialog
    viewingStory?.let { story ->
      StatusViewerModal(
        story = story,
        onDismiss = { viewingStory = null },
        onShowViewers = {
          val current = viewingStory
          viewingStory = null
          showViewersSheetForStory = current
        }
      )
    }

    // Create Status Composer Modal
    if (showCreateStatusSheet) {
      CreateStatusModal(
        onDismiss = { showCreateStatusSheet = false },
        onPost = { text, bgIndex ->
          ChatMitraRepository.postStory(text, bgIndex)
          showCreateStatusSheet = false
        }
      )
    }

    // Viewers Sheet
    showViewersSheetForStory?.let { story ->
      ModalBottomSheet(
        onDismissRequest = { showViewersSheetForStory = null },
        sheetState = sheetState
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "Viewed by ${story.viewers.size}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (story.viewers.isEmpty()) {
            Text(
              text = "No views yet. Your contacts will appear here once they see your status.",
              style = MaterialTheme.typography.bodyMedium,
              color = CheckmarkGrey
            )
          } else {
            story.viewers.forEach { viewer ->
              ListItem(
                headlineContent = { Text(viewer.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Today", style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey) },
                leadingContent = {
                  UserAvatar(name = viewer.name, colorHex = viewer.avatarColorHex, emoji = viewer.avatarEmoji, size = 40.dp)
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatusViewerModal(
  story: StatusStory,
  onDismiss: () -> Unit,
  onShowViewers: () -> Unit
) {
  var progress by remember { mutableFloatStateOf(0f) }
  var isPaused by remember { mutableStateOf(false) }

  val gradientColors = statusGradients.getOrElse(story.bgGradientIndex) { statusGradients[0] }

  LaunchedEffect(isPaused) {
    if (!isPaused) {
      while (progress < 1f) {
        delay(50)
        progress += 0.015f
      }
      onDismiss()
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isPaused = true
            tryAwaitRelease()
            isPaused = false
          },
          onTap = { offset ->
            if (offset.x < size.width / 2) {
              onDismiss()
            } else {
              onDismiss()
            }
          }
        )
      }
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(gradientColors))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
      ) {
        // Segmented Progress Bar
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = Color.White,
          trackColor = Color.White.copy(alpha = 0.3f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Author Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          UserAvatar(
            name = story.userName,
            colorHex = story.userAvatarColorHex,
            emoji = story.userAvatarEmoji,
            size = 42.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = story.userName,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium
            )
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            Text(
              text = "Today at ${sdf.format(Date(story.timestamp))}",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
          }
        }

        // Story Central Content
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = story.text,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
          )
        }

        // Bottom view counter (if own story) or reply box
        if (story.userId == "user_me") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onShowViewers() }
              .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${story.viewers.size} viewers (Tap to view)",
              color = Color.White,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
          }
        } else {
          Text(
            text = "Reply to status...",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
              .padding(vertical = 12.dp)
          )
        }
      }
    }
  }
}

@Composable
fun CreateStatusModal(
  onDismiss: () -> Unit,
  onPost: (String, Int) -> Unit
) {
  var statusText by remember { mutableStateOf("") }
  var selectedBgIndex by remember { mutableIntStateOf(0) }

  val gradientColors = statusGradients[selectedBgIndex]

  Surface(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(gradientColors))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel", tint = Color.White)
          }
          Text(
            text = "Compose Status",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          TextButton(
            onClick = {
              if (statusText.isNotBlank()) {
                onPost(statusText.trim(), selectedBgIndex)
              }
            }
          ) {
            Text("POST", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Color selector
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(statusGradients.indices.toList()) { index ->
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(statusGradients[index]))
                .clickable { selectedBgIndex = index }
            )
          }
        }

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
          value = statusText,
          onValueChange = { statusText = it },
          placeholder = {
            Text(
              "Type a status...",
              fontSize = 22.sp,
              color = Color.White.copy(alpha = 0.6f),
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          },
          textStyle = MaterialTheme.typography.headlineSmall.copy(
            color = Color.White,
            textAlign = TextAlign.Center
          ),
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .testTag("compose_status_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
          )
        )

        Text(
          text = "Status updates disappear after 24 hours automatically",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.8f),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
