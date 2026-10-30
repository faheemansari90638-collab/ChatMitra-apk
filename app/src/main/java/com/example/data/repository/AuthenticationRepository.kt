package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * AuthenticationRepository abstracts Firebase Authentication and Jetpack Credential Manager
 * to provide a clean, modern interface for user authentication.
 */
class AuthenticationRepository(
  private val customAuth: FirebaseAuth? = null
) {

  private val auth: FirebaseAuth?
    get() = customAuth ?: try {
      Firebase.auth
    } catch (_: Exception) {
      null
    }

  val currentUser: FirebaseUser?
    get() = auth?.currentUser

  val isAuthenticated: Boolean
    get() = auth?.currentUser != null

  /**
   * Observe Firebase auth state changes as a cold Kotlin Flow.
   */
  fun observeAuthState(): Flow<FirebaseUser?> {
    val firebaseAuth = auth ?: return flowOf(null)
    return callbackFlow {
      val listener = FirebaseAuth.AuthStateListener { fa ->
        trySend(fa.currentUser)
      }
      firebaseAuth.addAuthStateListener(listener)
      awaitClose {
        firebaseAuth.removeAuthStateListener(listener)
      }
    }
  }

  /**
   * Interactive Sign-In with Google using Jetpack Credential Manager.
   * Prompts the user with the Google Sign-In bottom sheet.
   */
  suspend fun signInWithGoogle(
    activity: Activity,
    customServerClientId: String? = null
  ): Result<FirebaseUser> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase Auth is not initialized"))

    val credentialManager = CredentialManager.create(activity)
    val serverClientId = customServerClientId ?: resolveServerClientId(activity)
      ?: return Result.failure(
        IllegalStateException("Google Sign-In configuration missing: default_web_client_id resource not found")
      )

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId)
      .build()

    val request = GetCredentialRequest.Builder()
      .addCredentialOption(signInOption)
      .build()

    return try {
      val result = credentialManager.getCredential(activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = firebaseAuth.signInWithCredential(authCredential).await()
        val user = authResult.user
        if (user != null) {
          Result.success(user)
        } else {
          Result.failure(IllegalStateException("Firebase Auth succeeded but returned null user"))
        }
      } else {
        Result.failure(IllegalArgumentException("Unexpected credential type returned by CredentialManager"))
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w(TAG, "Google Sign-In was cancelled by user: ${e.message}")
      Result.failure(e)
    } catch (e: Exception) {
      Log.e(TAG, "Google Sign-In failed", e)
      Result.failure(e)
    }
  }

  /**
   * Silent background auto-sign-in for previously authorized Google accounts.
   */
  suspend fun attemptAutoSignIn(
    context: Context,
    customServerClientId: String? = null
  ): Result<FirebaseUser> {
    val firebaseAuth = auth
      ?: return Result.failure(IllegalStateException("Firebase Auth is not initialized"))

    if (firebaseAuth.currentUser != null) {
      return Result.success(firebaseAuth.currentUser!!)
    }

    val credentialManager = CredentialManager.create(context)
    val serverClientId = customServerClientId ?: resolveServerClientId(context)
      ?: return Result.failure(
        IllegalStateException("default_web_client_id not found")
      )

    val googleIdOption = GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(serverClientId)
      .setAutoSelectEnabled(true)
      .build()

    val request = GetCredentialRequest.Builder()
      .addCredentialOption(googleIdOption)
      .build()

    return try {
      val result = credentialManager.getCredential(context, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = firebaseAuth.signInWithCredential(authCredential).await()
        val user = authResult.user
        if (user != null) {
          Result.success(user)
        } else {
          Result.failure(IllegalStateException("Auto sign-in user is null"))
        }
      } else {
        Result.failure(NoCredentialException("No matching Google ID credential found"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Signs the user out from both Firebase Authentication and Credential Manager state.
   */
  suspend fun signOut(context: Context): Result<Unit> {
    return try {
      auth?.signOut()
      val credentialManager = CredentialManager.create(context)
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Sign out error", e)
      Result.failure(e)
    }
  }

  /**
   * Deletes the currently authenticated user's account and clears credentials.
   */
  suspend fun deleteAccount(context: Context): Result<Unit> {
    val user = auth?.currentUser
      ?: return Result.failure(IllegalStateException("No authenticated user to delete"))

    return try {
      user.delete().await()
      val credentialManager = CredentialManager.create(context)
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Account deletion error", e)
      Result.failure(e)
    }
  }

  fun resolveServerClientId(context: Context): String? {
    return try {
      val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
      if (resId != 0) context.getString(resId) else null
    } catch (_: Exception) {
      null
    }
  }

  companion object {
    private const val TAG = "AuthenticationRepository"

    @Volatile
    private var INSTANCE: AuthenticationRepository? = null

    fun getInstance(auth: FirebaseAuth? = null): AuthenticationRepository {
      return INSTANCE ?: synchronized(this) {
        val instance = AuthenticationRepository(auth)
        INSTANCE = instance
        instance
      }
    }
  }
}
