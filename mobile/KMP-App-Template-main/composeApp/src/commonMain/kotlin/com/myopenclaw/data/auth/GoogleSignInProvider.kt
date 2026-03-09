package com.myopenclaw.data.auth

/**
 * Result of a Google Sign-In attempt
 */
data class GoogleSignInResult(
    val idToken: String?,
    val error: String? = null
) {
    val isSuccess: Boolean get() = idToken != null && error == null
}

/**
 * Result of an Apple Sign-In attempt
 */
data class AppleSignInResult(
    val idToken: String?,
    val nonce: String?,
    val error: String? = null
) {
    val isSuccess: Boolean get() = idToken != null && nonce != null && error == null
}

/**
 * Platform-specific Google Sign-In provider
 * Implemented differently on Android (Credential Manager) and iOS (native SDK)
 */
expect class GoogleSignInProvider {
    /**
     * Initiates Google Sign-In flow
     * @return GoogleSignInResult with ID token or error
     */
    suspend fun signIn(): GoogleSignInResult
}

/**
 * Platform-specific Apple Sign-In provider
 * Only available on iOS, returns error on Android
 */
expect class AppleSignInProvider {
    /**
     * Initiates Apple Sign-In flow
     * @return AppleSignInResult with ID token, nonce, or error
     */
    suspend fun signIn(): AppleSignInResult
}
