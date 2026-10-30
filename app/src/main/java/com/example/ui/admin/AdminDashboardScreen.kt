package com.example.ui.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminAuditLog
import com.example.data.model.AppealStatus
import com.example.data.model.ReportStatus
import com.example.data.model.ServiceHealthMetrics
import com.example.data.model.UserAppeal
import com.example.data.model.UserReport
import com.example.data.repository.AdminModerationRepository
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CallRed
import com.example.ui.theme.CheckmarkGrey
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
  onBackClick: () -> Unit
) {
  val context = LocalContext.current
  val isAuthenticated by AdminModerationRepository.isAdminAuthenticated.collectAsState()
  val reports by AdminModerationRepository.reports.collectAsState()
  val appeals by AdminModerationRepository.appeals.collectAsState()
  val auditLogs by AdminModerationRepository.auditLogs.collectAsState()
  val serviceHealth by AdminModerationRepository.serviceHealth.collectAsState()
  val suspendedUserIds by AdminModerationRepository.suspendedUserIds.collectAsState()

  var adminPinInput by remember { mutableStateOf("") }
  var pinError by remember { mutableStateOf(false) }

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Health", "Reports", "Appeals", "Suspensions", "Audit Logs")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AmberAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Admin Moderation Console", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          if (isAuthenticated) {
            TextButton(
              onClick = {
                AdminModerationRepository.logoutAdmin()
                onBackClick()
              }
            ) {
              Text("Logout", color = Color.White)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFF0F172A),
          titleContentColor = Color.White
        )
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (!isAuthenticated) {
        // Admin PIN Gate
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .background(TealPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Security,
              contentDescription = null,
              tint = TealPrimary,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(24.dp))
          Text("Restricted Admin Access", fontWeight = FontWeight.Bold, fontSize = 20.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Authorized personnel only. Audit logs record all administrative actions.",
            textAlign = TextAlign.Center,
            color = CheckmarkGrey,
            fontSize = 13.sp
          )

          Spacer(modifier = Modifier.height(24.dp))

          OutlinedTextField(
            value = adminPinInput,
            onValueChange = {
              adminPinInput = it
              pinError = false
            },
            placeholder = { Text("Enter Admin Secret Key") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = pinError,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_pin_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              focusedLabelColor = TealPrimary
            )
          )

          if (pinError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Invalid administrator credentials", color = CallRed, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val ok = AdminModerationRepository.authenticateAdmin(adminPinInput)
              if (ok) {
                adminPinInput = ""
                pinError = false
              } else {
                pinError = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("admin_login_submit"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Verify Credentials")
          }
        }
      } else {
        // Authenticated Admin Dashboard
        Column(modifier = Modifier.fillMaxSize()) {
          // Zero-Trust Privacy Notice Bar
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFF1E293B))
              .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            Text(
              "Zero-Trust Privacy: Administrator view is strictly limited to submitted abuse evidence. End-to-end encrypted private messages are inaccessible.",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp,
              lineHeight = 14.sp
            )
          }

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
                  val badge = when (index) {
                    1 -> reports.count { it.status == ReportStatus.PENDING }.toString()
                    2 -> appeals.count { it.status == AppealStatus.PENDING }.toString()
                    else -> null
                  }
                  Text(
                    text = if (badge != null && badge != "0") "$title ($badge)" else title,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                  )
                }
              )
            }
          }

          when (selectedTab) {
            0 -> HealthMetricsTab(serviceHealth)
            1 -> ReportsQueueTab(
              reports = reports,
              onResolve = { reportId, actionTaken, notes ->
                AdminModerationRepository.resolveReport(reportId, actionTaken, notes)
                Toast.makeText(context, if (actionTaken) "Action taken on report" else "Report dismissed", Toast.LENGTH_SHORT).show()
              },
              onSuspendUser = { userId, reason ->
                AdminModerationRepository.suspendUser(userId, reason)
                Toast.makeText(context, "User $userId suspended", Toast.LENGTH_SHORT).show()
              }
            )
            2 -> AppealsReviewTab(
              appeals = appeals,
              onReview = { appealId, approved, decision ->
                AdminModerationRepository.reviewAppeal(appealId, approved, decision)
                Toast.makeText(context, if (approved) "Appeal approved" else "Appeal rejected", Toast.LENGTH_SHORT).show()
              }
            )
            3 -> SuspensionsTab(
              suspendedUserIds = suspendedUserIds,
              onUnsuspend = { userId ->
                AdminModerationRepository.unsuspendUser(userId)
                Toast.makeText(context, "User $userId reinstated", Toast.LENGTH_SHORT).show()
              }
            )
            4 -> AuditLogsTab(auditLogs)
          }
        }
      }
    }
  }
}

@Composable
private fun HealthMetricsTab(metrics: ServiceHealthMetrics) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Service Health & Operational Infrastructure", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Text("Live status of signaling servers, Firestore replication, and STUN/TURN relays.", color = CheckmarkGrey, fontSize = 12.sp)
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Signaling & Relay Status", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
              Spacer(modifier = Modifier.width(6.dp))
              Text("OPERATIONAL", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricPill("Service Uptime", "${metrics.serverUptimePercent}%")
            MetricPill("Active Calls", "${metrics.activeCallsCount}")
            MetricPill("Active Users", "${metrics.activeUsersCount}")
          }

          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricPill("DB Latency", "${metrics.databaseLatencyMs} ms")
            MetricPill("WebRTC Latency", "${metrics.webrtcSignalingLatencyMs} ms")
            MetricPill("Error Rate", "${metrics.errorRatePercent}%")
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Production Capacity & Limits", fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "• WebRTC Group Calls: Strictly capped at 8 participants per room in peer-to-peer full-mesh.\n" +
            "• Firestore Rules: Enforced zero-trust RBAC at database level.\n" +
            "• Relay Failover: TURN (coturn) configured over TLS on port 5349.\n" +
            "• Status: ${metrics.firestoreStatus} • ${metrics.signalingStatus}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun MetricPill(title: String, value: String) {
  Column {
    Text(title, color = CheckmarkGrey, fontSize = 11.sp)
    Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimary)
  }
}

@Composable
private fun ReportsQueueTab(
  reports: List<UserReport>,
  onResolve: (String, Boolean, String) -> Unit,
  onSuspendUser: (String, String) -> Unit
) {
  val pendingReports = reports.filter { it.status == ReportStatus.PENDING }
  if (pendingReports.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("No pending user reports.", color = CheckmarkGrey, fontSize = 14.sp)
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(pendingReports, key = { it.id }) { report ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Report #${report.id.takeLast(6).uppercase()}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              report.reason.name,
              color = AmberAccent,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text("Reported User: ${report.reportedUserName} (${report.reportedUserId})", fontSize = 12.sp, color = CheckmarkGrey)
          Text("Reporter: ${report.reporterUserName} (${report.reporterUserId})", fontSize = 12.sp, color = CheckmarkGrey)

          Spacer(modifier = Modifier.height(8.dp))
          Text("Category: ${report.reason.name}", fontWeight = FontWeight.Medium, fontSize = 13.sp)

          if (report.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x33000000), RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Text(
                "Reporter Notes: \"${report.notes}\"",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = { onResolve(report.id, false, "Dismissed as false positive") }
            ) {
              Text("Dismiss")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                onResolve(report.id, true, "User suspended for violation")
                onSuspendUser(report.reportedUserId, "Violated Community Guidelines (${report.reason.name})")
              },
              colors = ButtonDefaults.buttonColors(containerColor = CallRed)
            ) {
              Text("Suspend User")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AppealsReviewTab(
  appeals: List<UserAppeal>,
  onReview: (String, Boolean, String) -> Unit
) {
  val pendingAppeals = appeals.filter { it.status == AppealStatus.PENDING }
  if (pendingAppeals.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("No pending appeals to review.", color = CheckmarkGrey, fontSize = 14.sp)
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(pendingAppeals, key = { it.id }) { appeal ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Appeal from User: ${appeal.userName} (${appeal.userId})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Reason for Suspension: ${appeal.reasonForSuspension}", fontSize = 12.sp, color = CheckmarkGrey)
          Spacer(modifier = Modifier.height(4.dp))
          Text("User Explanation: ${appeal.appealExplanation}", fontSize = 13.sp)

          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = { onReview(appeal.id, false, "Appeal rejected by admin") }
            ) {
              Text("Reject", color = CallRed)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = { onReview(appeal.id, true, "Appeal approved; account reinstated") },
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Approve & Reinstate")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SuspensionsTab(
  suspendedUserIds: Map<String, String>,
  onUnsuspend: (String) -> Unit
) {
  if (suspendedUserIds.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("No accounts are currently suspended.", color = CheckmarkGrey, fontSize = 14.sp)
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(suspendedUserIds.entries.toList(), key = { it.key }) { (userId, reason) ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(userId, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Reason: $reason", color = CallRed, fontSize = 12.sp)
          }

          Button(
            onClick = { onUnsuspend(userId) },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Lift Ban")
          }
        }
      }
    }
  }
}

@Composable
private fun AuditLogsTab(logs: List<AdminAuditLog>) {
  if (logs.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Text("No audit log entries recorded.", color = CheckmarkGrey, fontSize = 14.sp)
    }
    return
  }

  val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(logs.reversed(), key = { it.id }) { log ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TealPrimary)
            Text(sdf.format(Date(log.timestamp)), color = CheckmarkGrey, fontSize = 11.sp)
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text("Target: ${log.targetId} (${log.targetType})", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          Text("Details: ${log.details}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}
