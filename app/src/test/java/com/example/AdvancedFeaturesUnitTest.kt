package com.example

import com.example.data.model.BackupStatus
import com.example.data.model.RestoreStatus
import com.example.data.repository.AccountManagementRepository
import com.example.data.repository.ChatBackupRepository
import com.example.data.repository.ChatMitraRepository
import com.example.data.repository.EmailAccountRepository
import com.example.data.repository.StorageManagementRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdvancedFeaturesUnitTest {

  @Before
  fun setUp() {
    ChatMitraRepository.clearBlockedContactsForTesting()
    ChatMitraRepository.resetCooldownForTesting()
    EmailAccountRepository.getInstance().resetForTesting()
  }

  @After
  fun tearDown() {
    ChatMitraRepository.clearBlockedContactsForTesting()
    ChatMitraRepository.resetCooldownForTesting()
    EmailAccountRepository.getInstance().resetForTesting()
  }

  @Test
  fun testEmailLinkingAndOtpVerificationWithRateLimiting() {
    val emailRepo = EmailAccountRepository.getInstance()

    // 1. Request verification for email
    val linkResult = emailRepo.requestEmailVerification("engineer@chatmitra.com")
    assertTrue("Email link request should succeed", linkResult.isSuccess)
    val generatedOtp = linkResult.getOrNull()
    assertNotNull("Generated OTP should be present", generatedOtp)
    assertEquals("Generated OTP should be 6 digits", 6, generatedOtp?.length)

    // 2. Cooldown check - rapid request within cooldown should fail
    val rapidRequest = emailRepo.requestEmailVerification("other@chatmitra.com")
    assertFalse("Rapid request during cooldown should be blocked", rapidRequest.isSuccess)

    // 3. Invalid OTP should fail verification
    val wrongOtpResult = emailRepo.verifyEmailOtp("000000")
    assertFalse("Wrong OTP should fail", wrongOtpResult.isSuccess)

    // 4. Correct OTP verification
    val verifyResult = emailRepo.verifyEmailOtp(generatedOtp ?: "")
    assertTrue("Correct OTP verification should succeed", verifyResult.isSuccess)
    assertTrue("Email should now be verified", emailRepo.emailState.value.isVerified)
    assertEquals("engineer@chatmitra.com", emailRepo.emailState.value.email)

    // 5. Change email with reauthentication PIN
    val changeResult = emailRepo.changeEmailWithReauth(
      newEmail = "updated@chatmitra.com",
      accountPin = "1234"
    )
    assertTrue("Change email with correct account PIN should succeed", changeResult.isSuccess)
  }

  @Test
  fun testClientSideEncryptedBackupAndRestore() {
    val backupRepo = ChatBackupRepository.getInstance()

    // 1. Enable end-to-end encrypted backup with password
    val setupResult = backupRepo.enableEndToEndEncryptedBackup("MasterBackupPassword2026!")
    assertTrue("Enabling E2EE backup should succeed", setupResult.isSuccess)
    val recoveryKey = setupResult.getOrNull()
    assertNotNull(recoveryKey)
    assertTrue("Recovery key should be non-empty", recoveryKey?.isNotBlank() == true)

    // 2. Perform encrypted backup
    val backupJob = backupRepo.performBackup(isManual = true)
    assertNotNull(backupJob)

    val backupRecords = backupRepo.backupRecords.value
    assertTrue("Backup records should exist", backupRecords.isNotEmpty())
    val latestRecord = backupRecords.first()
    assertTrue("Checksum HMAC should be present", latestRecord.checksumHmac.isNotBlank())

    // 3. Perform restore with correct credentials
    var restoreSucceeded = false
    val restoreJob = backupRepo.performRestore(
      backupId = latestRecord.id,
      passwordOrKey = "MasterBackupPassword2026!",
      onComplete = { result ->
        restoreSucceeded = result.isSuccess
      }
    )
    assertNotNull(restoreJob)

    // 4. Restore with incorrect password
    var wrongPasswordFailed = false
    val failedJob = backupRepo.performRestore(
      backupId = latestRecord.id,
      passwordOrKey = "WrongPassword999!",
      onComplete = { result ->
        wrongPasswordFailed = result.isFailure
      }
    )
    assertNotNull(failedJob)
  }

  @Test
  fun testStorageOverviewAndCleanup() {
    val storageRepo = StorageManagementRepository.getInstance()

    // 1. Overview metrics
    val overview = storageRepo.storageOverview.value
    assertTrue("Total app storage should be positive", overview.totalAppStorageBytes > 0)
    assertTrue("Chat storage usages should be populated", storageRepo.chatStorageUsages.value.isNotEmpty())

    // 2. Delete specific media category
    val freedBytes = storageRepo.deleteChatMediaItems(
      chatId = "chat_priya",
      deletePhotos = true,
      deleteAudio = false,
      deleteVideos = false,
      deleteDocs = false
    )
    assertTrue("Deleting media should return non-negative freed bytes", freedBytes >= 0L)

    // 3. Clear cache
    val cacheFreed = storageRepo.clearCache()
    assertTrue("Clearing cache should succeed", cacheFreed >= 0L)
  }

  @Test
  fun testAccountDataExportAndDeletion() {
    val accountRepo = AccountManagementRepository.getInstance()

    // 1. Request official ZIP data archive
    val exportResult = accountRepo.requestDataExport()
    assertTrue("Export request should succeed", exportResult.isSuccess)
    val exportReq = exportResult.getOrNull()
    assertNotNull(exportReq)
    assertTrue("Export URL should contain authenticated token", exportReq?.downloadUrl?.contains("token=") == true)

    // 2. Delete account with mismatched phone should fail
    val failDelete = accountRepo.deleteAccount(confirmedPhone = "+1 0000000000", reason = "Testing")
    assertFalse("Deletion with mismatched phone must be rejected", failDelete.isSuccess)

    // 3. Delete account with correct registered phone
    val currentPhone = ChatMitraRepository.currentUser.value?.phone ?: "+91 98765 43210"
    val successDelete = accountRepo.deleteAccount(confirmedPhone = currentPhone, reason = "Device Upgrade")
    assertTrue("Deletion with matching phone must succeed", successDelete.isSuccess)
  }

  @Test
  fun testChatFoldersAndPollsAndScheduling() {
    // 1. Chat folders
    ChatMitraRepository.createChatFolder("VIP Clients", setOf("chat_priya", "chat_rahul"))
    val folders = ChatMitraRepository.chatFolders.value
    assertTrue("Custom folder should be added", folders.any { it.name == "VIP Clients" })

    // 2. Scheduled message
    val schedTime = System.currentTimeMillis() + 3600000L
    ChatMitraRepository.scheduleMessage("chat_priya", "Happy Launch Day!", schedTime)
    val scheduled = ChatMitraRepository.scheduledMessages.value
    assertTrue("Scheduled message should be present", scheduled.any { it.text == "Happy Launch Day!" })

    val schedItem = scheduled.first { it.text == "Happy Launch Day!" }
    ChatMitraRepository.cancelScheduledMessage(schedItem.id)
    assertFalse("Cancelled scheduled message should be removed", ChatMitraRepository.scheduledMessages.value.any { it.id == schedItem.id })

    // 3. Poll creation and voting
    ChatMitraRepository.createPoll("chat_priya", "Next Release Feature?", listOf("Dark Mode Wallpapers", "Multi-Account"), allowMultiple = false)
    val polls = ChatMitraRepository.polls.value["chat_priya"]
    assertNotNull(polls)
    assertTrue("Poll should be created", polls?.isNotEmpty() == true)

    val poll = polls?.firstOrNull()
    assertNotNull(poll)
    val opt1 = poll?.options?.firstOrNull()
    assertNotNull(opt1)

    ChatMitraRepository.votePoll("chat_priya", poll!!.id, opt1!!.id)
    val updatedPolls = ChatMitraRepository.polls.value["chat_priya"]
    val updatedOpt1 = updatedPolls?.first()?.options?.first()
    assertEquals("Vote count should increment to 1", 1, updatedOpt1?.votesCount)
  }
}
