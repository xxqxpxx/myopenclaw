package com.myopenclaw.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Global reference to the current Activity for Google Sign-In.
 * This is shared with RevenueCat's activity management.
 */
private var googleSignInActivity: Activity? = null

/**
 * Callback for legacy Google Sign-In result
 */
private var legacySignInCallback: ((GoogleSignInResult) -> Unit)? = null

/**
 * Sets the current activity for Google Sign-In.
 * Called from MainActivity.onResume()
 */
fun setGoogleSignInActivity(activity: Activity) {
    googleSignInActivity = activity
    // // println("GoogleSignIn: Activity set")
}

/**
 * Clears the activity reference.
 * Called from MainActivity.onPause()
 */
fun clearGoogleSignInActivity() {
    googleSignInActivity = null
    // // println("GoogleSignIn: Activity reference cleared")
}

/**
 * Handles the result from legacy Google Sign-In Intent.
 * Should be called from MainActivity.onActivityResult()
 */
fun handleGoogleSignInResult(data: Intent?) {
    // // println("GoogleSignIn: handleGoogleSignInResult called")
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken
        // // println("GoogleSignIn: Legacy sign-in SUCCESS, idToken=${idToken?.take(20)}...")
        if (idToken != null) {
            legacySignInCallback?.invoke(GoogleSignInResult(idToken = idToken))
        } else {
            legacySignInCallback?.invoke(GoogleSignInResult(
                idToken = null,
                error = "Failed to get ID token from Google account"
            ))
        }
    } catch (e: ApiException) {
        // // println("GoogleSignIn: Legacy sign-in FAILED, statusCode=${e.statusCode}, message=${e.message}")
        val errorMessage = when (e.statusCode) {
            12501 -> "Sign-in was cancelled"
            12500 -> "Google Sign-In failed. Please try again."
            7 -> "Network error. Please check your connection."
            10 -> "Developer error: Check your Google Cloud Console configuration."
            else -> "Google Sign-In failed: ${e.message}"
        }
        legacySignInCallback?.invoke(GoogleSignInResult(idToken = null, error = errorMessage))
    }
    legacySignInCallback = null
}

/**
 * Android implementation of Google Sign-In using Credential Manager API
 */
actual class GoogleSignInProvider(
    private val context: Context,
    private val webClientId: String
) {
    private val credentialManager = CredentialManager.create(context)

    actual suspend fun signIn(): GoogleSignInResult = withContext(Dispatchers.Main) {
        try {
            // // println("GoogleSignIn: Starting sign-in with webClientId: $webClientId")

            // Get the activity reference first
            val activity = googleSignInActivity
            if (activity == null) {
                // // println("GoogleSignIn: ERROR - Activity is null!")
                return@withContext GoogleSignInResult(
                    idToken = null,
                    error = "Activity not available. Please try again."
                )
            }

            // Try GetSignInWithGoogleOption first (uses Google's Sign-In button flow)
            // // println("GoogleSignIn: Trying GetSignInWithGoogleOption...")
            try {
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                // // println("GoogleSignIn: Activity available, starting credential request...")
                val response = credentialManager.getCredential(
                    request = request,
                    context = activity
                )
                // // println("GoogleSignIn: Got credential response: ${response.credential.type}")

                return@withContext handleSignInResponse(response)
            } catch (e: Exception) {
                // // println("GoogleSignIn: GetSignInWithGoogleOption failed: ${e.message}, trying GetGoogleIdOption...")
            }

            // Fallback to GetGoogleIdOption if GetSignInWithGoogleOption fails
            // This option requires a nonce for security
            try {
                val nonce = generateNonce()
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .setNonce(nonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // // println("GoogleSignIn: Trying GetGoogleIdOption...")
                val response = credentialManager.getCredential(
                    request = request,
                    context = activity
                )
                // // println("GoogleSignIn: Got credential response: ${response.credential.type}")

                return@withContext handleSignInResponse(response)
            } catch (e: Exception) {
                // // println("GoogleSignIn: GetGoogleIdOption failed: ${e.message}, trying legacy GoogleSignInClient...")
            }

            // Final fallback: Use legacy GoogleSignInClient
            // This is more reliable for new sign-ups and doesn't require SHA-1 fingerprint for basic sign-in
            // // println("GoogleSignIn: Trying legacy GoogleSignInClient...")
            return@withContext signInWithLegacyClient(activity)
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            // // println("GoogleSignIn: User cancelled - ${e.message}")
            GoogleSignInResult(idToken = null, error = "Sign-in was cancelled")
        } catch (e: Exception) {
            // // println("GoogleSignIn: Unexpected exception - ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            GoogleSignInResult(idToken = null, error = "An unexpected error occurred. Please try again.")
        }
    }

    /**
     * Legacy Google Sign-In using GoogleSignInClient
     * This is more reliable for sign-ups and doesn't require SHA-1 fingerprint for basic sign-in
     */
    private suspend fun signInWithLegacyClient(activity: Activity): GoogleSignInResult =
        suspendCancellableCoroutine { continuation ->
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(activity, gso)

            // Sign out first to ensure account picker is shown
            googleSignInClient.signOut().addOnCompleteListener {
                legacySignInCallback = { result ->
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                val signInIntent = googleSignInClient.signInIntent
                activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
            }

            continuation.invokeOnCancellation {
                legacySignInCallback = null
            }
        }

    companion object {
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 9001
    }

    private fun handleSignInResponse(response: GetCredentialResponse): GoogleSignInResult {
        val credential = response.credential
        // // println("GoogleSignIn: handleSignInResponse - credential type: ${credential.type}")

        return when (credential) {
            is CustomCredential -> {
                // // println("GoogleSignIn: CustomCredential received, type: ${credential.type}")
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        // // println("GoogleSignIn: Successfully extracted Google ID token")
                        // // println("GoogleSignIn: User ID: ${googleIdTokenCredential.id}")
                        // // println("GoogleSignIn: Display Name: ${googleIdTokenCredential.displayName}")
                        GoogleSignInResult(idToken = googleIdTokenCredential.idToken)
                    } catch (e: Exception) {
                        // // println("GoogleSignIn: Failed to parse GoogleIdTokenCredential - ${e.message}")
                        GoogleSignInResult(idToken = null, error = "Failed to process Google credentials")
                    }
                } else {
                    // // println("GoogleSignIn: Unexpected CustomCredential type: ${credential.type}")
                    GoogleSignInResult(idToken = null, error = "Unexpected credential type")
                }
            }
            else -> {
                // // println("GoogleSignIn: Unexpected credential class: ${credential::class.simpleName}")
                GoogleSignInResult(idToken = null, error = "Unexpected credential type")
            }
        }
    }

    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

/**
 * Android implementation of Apple Sign-In - Not available on Android
 */
actual class AppleSignInProvider {
    actual suspend fun signIn(): AppleSignInResult {
        return AppleSignInResult(
            idToken = null,
            nonce = null,
            error = "Apple Sign-In is not available on Android"
        )
    }
}
