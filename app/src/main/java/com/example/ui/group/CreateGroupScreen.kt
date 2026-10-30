package com.example.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChatMitraRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
  onBackClick: () -> Unit,
  onGroupCreated: () -> Unit
) {
  val contacts by ChatMitraRepository.contacts.collectAsState()

  var groupName by remember { mutableStateOf("") }
  var groupDescription by remember { mutableStateOf("") }
  val selectedContactIds = remember { mutableStateListOf<String>() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("New Group", fontWeight = FontWeight.Bold)
            Text(
              text = "${selectedContactIds.size} of ${contacts.size} selected",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = TealDark,
          titleContentColor = Color.White
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Group Details Header
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .background(TealPrimary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            placeholder = { Text("Group Subject / Title") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("group_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              unfocusedBorderColor = Color.Transparent
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = groupDescription,
          onValueChange = { groupDescription = it },
          placeholder = { Text("Provide group description & guidelines...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("group_desc_input"),
          maxLines = 2,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = Color.Transparent
          )
        )
      }

      Text(
        text = "Select Participants",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
      )

      LazyColumn(modifier = Modifier.weight(1f)) {
        items(contacts, key = { it.id }) { contact ->
          val isSelected = selectedContactIds.contains(contact.id)
          ListItem(
            modifier = Modifier
              .clickable {
                if (isSelected) {
                  selectedContactIds.remove(contact.id)
                } else {
                  selectedContactIds.add(contact.id)
                }
              }
              .testTag("select_contact_${contact.id}"),
            headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(contact.about, style = MaterialTheme.typography.bodySmall, color = CheckmarkGrey) },
            leadingContent = {
              UserAvatar(
                name = contact.name,
                colorHex = contact.avatarColorHex,
                emoji = contact.avatarEmoji,
                size = 46.dp
              )
            },
            trailingContent = {
              Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                  if (checked) selectedContactIds.add(contact.id) else selectedContactIds.remove(contact.id)
                },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
              )
            }
          )
          HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
        }
      }

      // Finish Button
      Box(modifier = Modifier.padding(16.dp)) {
        Button(
          onClick = {
            if (groupName.isNotBlank() && selectedContactIds.isNotEmpty()) {
              ChatMitraRepository.createGroup(groupName.trim(), groupDescription.trim(), selectedContactIds.toList())
              onGroupCreated()
            }
          },
          enabled = groupName.isNotBlank() && selectedContactIds.isNotEmpty(),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("create_group_submit_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Create Group (${selectedContactIds.size})", fontWeight = FontWeight.Bold, color = Color.White)
        }
      }
    }
  }
}
