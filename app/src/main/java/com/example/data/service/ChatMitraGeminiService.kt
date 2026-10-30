package com.example.data.service

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * ChatMitraGeminiService integrates with the Firebase AI SDK to handle text completion,
 * message summarization, translation, and polite smart reply suggestions.
 *
 * Security Architecture:
 * All API access and rate limiting are managed via Firebase server-side environment
 * and App Check attestation. No API keys are embedded or stored inside the client APK.
 */
class ChatMitraGeminiService private constructor(private val context: Context? = null) {

  companion object {
    private const val TAG = "ChatMitraGeminiService"
    const val DEFAULT_MODEL_NAME = "gemini-3.8-flash"
    private const val TIMEOUT_MILLIS = 60_000L

    @Volatile
    private var instance: ChatMitraGeminiService? = null

    fun getInstance(context: Context? = null): ChatMitraGeminiService {
      return instance ?: synchronized(this) {
        instance ?: ChatMitraGeminiService(context?.applicationContext).also { instance = it }
      }
    }
  }

  /**
   * Lazily initializes the general-use Gemini model via Firebase AI Logic.
   * Evaluated upon first inference request rather than eagerly at construction.
   */
  val generativeModel: GenerativeModel by lazy {
    context?.let { ctx ->
      if (FirebaseApp.getApps(ctx).isEmpty()) {
        try {
          FirebaseApp.initializeApp(ctx)
        } catch (e: Exception) {
          Log.w(TAG, "FirebaseApp initialization: ${e.message}")
        }
      }
    }

    Firebase.ai.generativeModel(
      modelName = DEFAULT_MODEL_NAME,
      generationConfig = generationConfig {
        temperature = 0.7f
        topP = 0.95f
      }
    )
  }

  /**
   * Generates a text completion for a given user prompt.
   *
   * @param prompt The input query or text prompt.
   * @param systemInstruction Optional system instruction for persona or formatting.
   * @return Result containing generated response or exception.
   */
  suspend fun completeText(
    prompt: String,
    systemInstruction: String? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    if (prompt.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Prompt cannot be blank."))
    }

    try {
      val modelToUse = if (systemInstruction.isNullOrBlank()) {
        generativeModel
      } else {
        Firebase.ai.generativeModel(
          modelName = DEFAULT_MODEL_NAME,
          systemInstruction = content { text(systemInstruction) }
        )
      }

      val response: GenerateContentResponse = withTimeout(TIMEOUT_MILLIS) {
        modelToUse.generateContent(prompt)
      }

      val text = response.text
      if (!text.isNullOrBlank()) {
        Result.success(text)
      } else {
        Result.failure(IllegalStateException("Gemini returned an empty response."))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "completeText failed", e)
      Result.failure(e)
    }
  }

  /**
   * Streams text completion chunks for low-latency interactive UI rendering.
   *
   * @param prompt The user's query.
   * @return Flow of text chunks as they arrive from Firebase AI.
   */
  fun completeTextStream(prompt: String): Flow<String> = flow {
    withTimeout(TIMEOUT_MILLIS) {
      generativeModel.generateContentStream(prompt).collect { chunk ->
        val text = chunk.text
        if (!text.isNullOrEmpty()) {
          emit(text)
        }
      }
    }
  }.flowOn(Dispatchers.IO)

  /**
   * Summarizes a list of chat messages into a concise summary.
   *
   * @param messages List of formatted message strings from the chat.
   * @param language Output language for the summary (default English).
   * @return Result containing the summary text.
   */
  suspend fun summarizeMessages(
    messages: List<String>,
    language: String = "English"
  ): Result<String> = withContext(Dispatchers.IO) {
    if (messages.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Message list is empty."))
    }

    val conversationText = messages.joinToString("\n")
    val prompt = """
      Please provide a concise, structured summary of the following conversation in $language.
      Highlight key decisions, action items, and important dates if mentioned:
      
      ---
      $conversationText
      ---
    """.trimIndent()

    try {
      val response: GenerateContentResponse = withTimeout(TIMEOUT_MILLIS) {
        generativeModel.generateContent(prompt)
      }
      val text = response.text
      if (!text.isNullOrBlank()) {
        Result.success(text)
      } else {
        Result.failure(IllegalStateException("Failed to produce conversation summary."))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "summarizeMessages failed", e)
      Result.failure(e)
    }
  }

  /**
   * Translates a message into a specified target language.
   *
   * @param text The message text to translate.
   * @param targetLanguage Target language (e.g. "Hindi", "Urdu", "Arabic", "English").
   * @param sourceLanguage Optional source language if known.
   * @return Result containing translated text.
   */
  suspend fun translateMessage(
    text: String,
    targetLanguage: String,
    sourceLanguage: String? = null
  ): Result<String> = withContext(Dispatchers.IO) {
    if (text.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Text to translate cannot be blank."))
    }

    val sourceContext = if (!sourceLanguage.isNullOrBlank()) " from $sourceLanguage" else ""
    val prompt = """
      You are a high-accuracy multilingual translator for ChatMitra messaging app.
      Translate the following message$sourceContext into $targetLanguage.
      Preserve conversational tone, emojis, and slang naturally.
      Return ONLY the translated text without additional commentary, greetings, or quotation marks:
      
      $text
    """.trimIndent()

    try {
      val response: GenerateContentResponse = withTimeout(TIMEOUT_MILLIS) {
        generativeModel.generateContent(prompt)
      }
      val translated = response.text?.trim()
      if (!translated.isNullOrBlank()) {
        Result.success(translated)
      } else {
        Result.failure(IllegalStateException("Model returned empty translation."))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "translateMessage failed", e)
      Result.failure(e)
    }
  }

  /**
   * Generates polite, natural reply suggestions based on recent conversation context.
   *
   * @param recentContext The last message or conversation snippet.
   * @return Result containing a list of 2-4 suggested replies.
   */
  suspend fun suggestPoliteReplies(
    recentContext: String
  ): Result<List<String>> = withContext(Dispatchers.IO) {
    if (recentContext.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("Context cannot be blank."))
    }

    val prompt = """
      Given the following incoming message in a chat conversation:
      "$recentContext"
      
      Generate 3 distinct, polite, and natural reply suggestions that the user could choose to send.
      Provide each suggestion on a separate line, prefixed with a hyphen (-).
      Do not include numbering or extra preamble.
    """.trimIndent()

    try {
      val response: GenerateContentResponse = withTimeout(TIMEOUT_MILLIS) {
        generativeModel.generateContent(prompt)
      }
      val text = response.text.orEmpty()
      val replies = text.lines()
        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
        .filter { it.isNotBlank() }

      if (replies.isNotEmpty()) {
        Result.success(replies)
      } else {
        Result.success(listOf("Thank you!", "Sounds good, will do.", "I'll get back to you shortly."))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "suggestPoliteReplies failed", e)
      Result.failure(e)
    }
  }
}
