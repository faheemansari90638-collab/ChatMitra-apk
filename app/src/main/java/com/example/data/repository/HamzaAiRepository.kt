package com.example.data.repository

import android.content.Context
import com.example.data.model.HamzaAiMessage
import com.example.data.service.ChatMitraGeminiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Repository for Hamza AI conversational assistant, summarization, translation,
 * and smart replies. Powered by ChatMitraGeminiService and Firebase AI Logic.
 *
 * Security: Private chats are NEVER sent to Hamza AI without explicit user confirmation.
 * API keys are never embedded in the client APK.
 */
object HamzaAiRepository {

  private val scope = CoroutineScope(Dispatchers.Main)
  private var activeGenerationJob: Job? = null

  private val initialMessages = listOf(
    HamzaAiMessage(
      id = "msg_intro_1",
      text = "Namaste & Hello! I am Hamza AI, your personal intelligent assistant in ChatMitra. 🤖✨\n\nI can answer questions, translate messages between English, Hindi, Urdu, and Arabic, summarize conversations, and help you draft polite replies.\n\n🛡️ Privacy Notice: Your private chats are end-to-end encrypted and are NEVER shared with me unless you explicitly choose to translate or summarize them.",
      isFromUser = false
    )
  )

  private val _messages = MutableStateFlow<List<HamzaAiMessage>>(initialMessages)
  val messages: StateFlow<List<HamzaAiMessage>> = _messages.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _streamingText = MutableStateFlow("")
  val streamingText: StateFlow<String> = _streamingText.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Preset quick prompt chips
  val suggestedPromptChips = listOf(
    "Translate to Hindi 🇮🇳",
    "Translate to Urdu 🇵🇰",
    "Explain WebRTC in simple terms",
    "Help me write a polite greeting",
    "Summarize key messaging features",
    "Draft a professional reply"
  )

  fun sendMessage(prompt: String, context: Context? = null) {
    if (prompt.isBlank() || _isGenerating.value) return

    val userMessage = HamzaAiMessage(
      id = "user_${UUID.randomUUID()}",
      text = prompt.trim(),
      isFromUser = true
    )

    _messages.value = _messages.value + userMessage
    _isGenerating.value = true
    _streamingText.value = ""
    _errorMessage.value = null

    val aiService = ChatMitraGeminiService.getInstance(context)

    activeGenerationJob = scope.launch {
      val accumulated = StringBuilder()
      val systemPrompt = """
        You are Hamza AI, an empathetic, helpful, and culturally respectful AI companion built into ChatMitra messenger.
        You support Hindi (हिन्दी), English, Urdu (اردو), and Arabic (العربية).
        Answer concisely and helpfully with friendly tone and appropriate emojis.
        Keep answers clear, accurate, and easy to read on mobile screens.
      """.trimIndent()

      try {
        val result = aiService.completeText(
          prompt = prompt,
          systemInstruction = systemPrompt
        )

        result.fold(
          onSuccess = { responseText ->
            val aiMessage = HamzaAiMessage(
              id = "ai_${UUID.randomUUID()}",
              text = responseText,
              isFromUser = false
            )
            _messages.value = _messages.value + aiMessage
            _isGenerating.value = false
            _streamingText.value = ""
          },
          onFailure = { err ->
            val failureMessage = HamzaAiMessage(
              id = "ai_err_${UUID.randomUUID()}",
              text = "⚠️ Hamza AI service is currently unavailable or configuring App Check. (${err.localizedMessage ?: "Unknown network error"}). Please retry in a moment.",
              isFromUser = false,
              error = err.localizedMessage
            )
            _messages.value = _messages.value + failureMessage
            _isGenerating.value = false
            _streamingText.value = ""
            _errorMessage.value = err.localizedMessage
          }
        )
      } catch (e: CancellationException) {
        val cancelledMsg = HamzaAiMessage(
          id = "ai_cancel_${UUID.randomUUID()}",
          text = "Generation was stopped.",
          isFromUser = false
        )
        _messages.value = _messages.value + cancelledMsg
        _isGenerating.value = false
        _streamingText.value = ""
      } catch (e: Exception) {
        _isGenerating.value = false
        _streamingText.value = ""
        _errorMessage.value = e.localizedMessage
      }
    }
  }

  fun cancelGeneration() {
    activeGenerationJob?.cancel()
    activeGenerationJob = null
    _isGenerating.value = false
    _streamingText.value = ""
  }

  fun clearChatHistory() {
    cancelGeneration()
    _messages.value = initialMessages
    _errorMessage.value = null
  }

  suspend fun translateMessage(
    text: String,
    targetLanguage: String,
    sourceLanguage: String? = null,
    context: Context? = null
  ): Result<String> {
    val service = ChatMitraGeminiService.getInstance(context)
    return service.translateMessage(text, targetLanguage, sourceLanguage)
  }

  suspend fun summarizeConversation(
    chatTitle: String,
    messages: List<String>,
    language: String = "English",
    context: Context? = null
  ): Result<String> {
    val service = ChatMitraGeminiService.getInstance(context)
    return service.summarizeMessages(messages, language)
  }

  suspend fun suggestPoliteReplies(
    lastMessageText: String,
    context: Context? = null
  ): Result<List<String>> {
    val service = ChatMitraGeminiService.getInstance(context)
    return service.suggestPoliteReplies(lastMessageText)
  }
}
