package com.myopenclaw.data.auth

/**
 * Bridge for iOS Google Sign-In.
 *
 * The Swift app sets the sign-in handler at startup. When Kotlin needs Google Sign-In,
 * it calls the handler which delegates to the Swift GoogleSignInHelper.
 */
object GoogleSignInBridge {
    /**
     * Handler that performs Google Sign-In.
     * Set from Swift at app startup.
     */
    internal var signInHandler: GoogleSignInHandler? = null
}

/**
 * Interface for the Google Sign-In handler, implemented in Swift.
 */
interface GoogleSignInHandler {
    fun performSignIn(callback: GoogleSignInCallback)
}

/**
 * Callback interface for receiving Google Sign-In results in a Swift-friendly way.
 */
interface GoogleSignInCallback {
    fun onResult(idToken: String?, error: String?)
}

/**
 * Set the Google Sign-In handler from Swift.
 * Call this from AppDelegate after GoogleSignInHelper is configured.
 */
fun setGoogleSignInHandler(handler: GoogleSignInHandler) {
    GoogleSignInBridge.signInHandler = handler
}
