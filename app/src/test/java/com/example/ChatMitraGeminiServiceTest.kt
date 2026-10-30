package com.example

import com.example.data.service.ChatMitraGeminiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMitraGeminiServiceTest {

  @Test
  fun testSingletonInstanceNotNull() {
    val service = ChatMitraGeminiService.getInstance()
    assertNotNull("ChatMitraGeminiService instance should not be null", service)
    val secondInstance = ChatMitraGeminiService.getInstance()
    assertEquals("Service should be a singleton", service, secondInstance)
  }

  @Test
  fun testBlankPromptValidation() = runTest {
    val service = ChatMitraGeminiService.getInstance()
    val blankResult = service.completeText("   ")
    assertTrue("Blank prompt should fail validation", blankResult.isFailure)
    assertTrue(
      "Exception should be IllegalArgumentException",
      blankResult.exceptionOrNull() is IllegalArgumentException
    )
  }

  @Test
  fun testEmptyMessagesSummarizationValidation() = runTest {
    val service = ChatMitraGeminiService.getInstance()
    val emptyResult = service.summarizeMessages(emptyList())
    assertTrue("Empty messages list should fail validation", emptyResult.isFailure)
    assertTrue(
      "Exception should be IllegalArgumentException",
      emptyResult.exceptionOrNull() is IllegalArgumentException
    )
  }

  @Test
  fun testBlankTranslationValidation() = runTest {
    val service = ChatMitraGeminiService.getInstance()
    val blankResult = service.translateMessage("", "Hindi")
    assertTrue("Blank translation input should fail validation", blankResult.isFailure)
  }

  @Test
  fun testBlankRepliesContextValidation() = runTest {
    val service = ChatMitraGeminiService.getInstance()
    val blankResult = service.suggestPoliteReplies("")
    assertTrue("Blank context should fail validation", blankResult.isFailure)
  }
}
