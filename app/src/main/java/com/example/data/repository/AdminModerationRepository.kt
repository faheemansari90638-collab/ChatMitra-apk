package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages admin access, user report resolution, account suspension, appeals, service health, and audit logs.
 * Privacy Invariant: Private message contents are NEVER exposed to administrators unless explicitly
 * reported and consented as evidence by the reporter.
 */
object AdminModerationRepository {

  private const val ADMIN_PIN_DEFAULT = "admin2026"

  private val _isAdminAuthenticated = MutableStateFlow(false)
  val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

  // User Reports Queue
  private val _reports = MutableStateFlow<List<UserReport>>(
    listOf(
      UserReport(
        id = "rep_101",
        reportedUserId = "c_spammer",
        reportedUserName = "Crypto Bot 🤖",
        reporterUserId = "user_me",
        reporterUserName = "Hamza Ansari",
        reason = ReportCategory.SPAM,
        notes = "Sent unsolicited promotional links in rapid succession",
        timestamp = System.currentTimeMillis() - 3600000L * 2,
        status = ReportStatus.PENDING
      ),
      UserReport(
        id = "rep_102",
        reportedUserId = "c_harasser",
        reportedUserName = "Aggressive User",
        reporterUserId = "c_priya",
        reporterUserName = "Priya Sharma",
        reason = ReportCategory.HARASSMENT,
        notes = "Repeated unwanted voice calls and abusive texts",
        timestamp = System.currentTimeMillis() - 3600000L * 5,
        status = ReportStatus.PENDING
      )
    )
  )
  val reports: StateFlow<List<UserReport>> = _reports.asStateFlow()

  // Suspended or Banned Users
  private val _suspendedUserIds = MutableStateFlow<Map<String, String>>(
    mapOf("c_spammer" to "Suspended for automated spamming (7 days)")
  )
  val suspendedUserIds: StateFlow<Map<String, String>> = _suspendedUserIds.asStateFlow()

  // User Appeals Queue
  private val _appeals = MutableStateFlow<List<UserAppeal>>(
    listOf(
      UserAppeal(
        id = "app_1",
        userId = "c_spammer",
        userName = "Crypto Bot",
        userPhone = "+91 99999 88888",
        reasonForSuspension = "Automated spamming",
        appealExplanation = "My account was compromised. I have regained control and set 2-step verification.",
        timestamp = System.currentTimeMillis() - 1800000L,
        status = AppealStatus.PENDING
      )
    )
  )
  val appeals: StateFlow<List<UserAppeal>> = _appeals.asStateFlow()

  // Audit Logs
  private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(
    listOf(
      AdminAuditLog(
        id = "log_1",
        adminId = "admin_super",
        action = "SUSPEND_USER",
        targetId = "c_spammer",
        targetType = "USER",
        timestamp = System.currentTimeMillis() - 3600000L * 4,
        details = "Temporary 7-day suspension issued for SPAM report rep_101"
      ),
      AdminAuditLog(
        id = "log_2",
        adminId = "admin_super",
        action = "RATE_LIMIT_UPDATE",
        targetId = "system_gateway",
        targetType = "SYSTEM",
        timestamp = System.currentTimeMillis() - 86400000L,
        details = "Updated outbound message rate limit to 30 msgs/min per client"
      )
    )
  )
  val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

  // Service Health Metrics
  private val _serviceHealth = MutableStateFlow(
    ServiceHealthMetrics(
      databaseLatencyMs = 18L,
      firestoreStatus = "Operational (Healthy)",
      webrtcSignalingLatencyMs = 14L,
      signalingStatus = "Connected (coturn-prod-cluster)",
      activeUsersCount = 1420,
      activeCallsCount = 38,
      serverUptimePercent = 99.98,
      errorRatePercent = 0.02
    )
  )
  val serviceHealth: StateFlow<ServiceHealthMetrics> = _serviceHealth.asStateFlow()

  /**
   * Verify administrator key / PIN.
   */
  fun authenticateAdmin(pin: String): Boolean {
    if (pin.trim() == ADMIN_PIN_DEFAULT) {
      _isAdminAuthenticated.value = true
      logAuditAction(
        adminId = "admin_session",
        action = "ADMIN_LOGIN_SUCCESS",
        targetId = "auth",
        targetType = "SECURITY",
        details = "Administrator authenticated successfully via secure PIN"
      )
      return true
    }
    return false
  }

  fun logoutAdmin() {
    _isAdminAuthenticated.value = false
  }

  /**
   * Resolve a user report (dismiss or take action).
   */
  fun resolveReport(reportId: String, actionTaken: Boolean, resolutionNotes: String, adminId: String = "admin") {
    _reports.value = _reports.value.map { r ->
      if (r.id == reportId) {
        r.copy(
          status = if (actionTaken) ReportStatus.RESOLVED_ACTION_TAKEN else ReportStatus.RESOLVED_DISMISSED,
          resolvedBy = adminId,
          resolutionNotes = resolutionNotes
        )
      } else r
    }

    logAuditAction(
      adminId = adminId,
      action = if (actionTaken) "RESOLVE_REPORT_ACTION" else "DISMISS_REPORT",
      targetId = reportId,
      targetType = "REPORT",
      details = "Report $reportId marked as ${if (actionTaken) "ACTION TAKEN" else "DISMISSED"}. Note: $resolutionNotes"
    )
  }

  /**
   * Suspend user account.
   */
  fun suspendUser(userId: String, reason: String, adminId: String = "admin") {
    _suspendedUserIds.value = _suspendedUserIds.value + (userId to reason)
    logAuditAction(
      adminId = adminId,
      action = "SUSPEND_USER",
      targetId = userId,
      targetType = "USER",
      details = "User $userId suspended. Reason: $reason"
    )
  }

  /**
   * Unsuspend or reinstate a user account.
   */
  fun unsuspendUser(userId: String, adminId: String = "admin") {
    _suspendedUserIds.value = _suspendedUserIds.value - userId
    logAuditAction(
      adminId = adminId,
      action = "UNSUSPEND_USER",
      targetId = userId,
      targetType = "USER",
      details = "User $userId account unsuspended and restored to active standing"
    )
  }

  /**
   * Review user appeal (approve or reject).
   */
  fun reviewAppeal(appealId: String, approved: Boolean, reviewNotes: String, adminId: String = "admin") {
    val targetAppeal = _appeals.value.find { it.id == appealId } ?: return
    _appeals.value = _appeals.value.map { a ->
      if (a.id == appealId) {
        a.copy(
          status = if (approved) AppealStatus.APPROVED else AppealStatus.REJECTED,
          reviewNotes = reviewNotes
        )
      } else a
    }

    if (approved) {
      unsuspendUser(targetAppeal.userId, adminId)
    }

    logAuditAction(
      adminId = adminId,
      action = if (approved) "APPROVE_APPEAL" else "REJECT_APPEAL",
      targetId = appealId,
      targetType = "APPEAL",
      details = "Appeal $appealId for ${targetAppeal.userName} ${if (approved) "APPROVED" else "REJECTED"}. Notes: $reviewNotes"
    )
  }

  /**
   * Submit an appeal on behalf of a user.
   */
  fun submitAppeal(userId: String, userName: String, phone: String, reason: String, explanation: String): String {
    val appealId = "app_${UUID.randomUUID().toString().take(6)}"
    val appeal = UserAppeal(
      id = appealId,
      userId = userId,
      userName = userName,
      userPhone = phone,
      reasonForSuspension = reason,
      appealExplanation = explanation,
      timestamp = System.currentTimeMillis(),
      status = AppealStatus.PENDING
    )
    _appeals.value = listOf(appeal) + _appeals.value
    return appealId
  }

  /**
   * Record immutable audit entry.
   */
  private fun logAuditAction(
    adminId: String,
    action: String,
    targetId: String,
    targetType: String,
    details: String
  ) {
    val log = AdminAuditLog(
      id = "log_${UUID.randomUUID().toString().take(8)}",
      adminId = adminId,
      action = action,
      targetId = targetId,
      targetType = targetType,
      timestamp = System.currentTimeMillis(),
      details = details
    )
    _auditLogs.value = listOf(log) + _auditLogs.value
  }
}
