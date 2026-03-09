package com.myopenclaw.data.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.*
import platform.Foundation.*
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UByteVar
import platform.posix.memcpy
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

/**
 * iOS implementation of Google Sign-In using the GoogleSignIn-iOS SDK.
 *
 * The actual sign-in UI is handled by the Swift GoogleSignInHelper.
 * Communication between Kotlin and Swift goes through GoogleSignInBridge.
 */
actual class GoogleSignInProvider {
    actual suspend fun signIn(): GoogleSignInResult {
        val handler = GoogleSignInBridge.signInHandler
            ?: return GoogleSignInResult(
                idToken = null,
                error = "Google Sign-In is not configured. Please restart the app."
            )

        return suspendCancellableCoroutine { continuation ->
            handler.performSignIn(object : GoogleSignInCallback {
                override fun onResult(idToken: String?, error: String?) {
                    if (continuation.isActive) {
                        continuation.resume(
                            GoogleSignInResult(
                                idToken = idToken,
                                error = error
                            )
                        )
                    }
                }
            })
        }
    }
}

/**
 * iOS implementation of Apple Sign-In using AuthenticationServices
 */
@OptIn(ExperimentalForeignApi::class)
actual class AppleSignInProvider {
    private var currentNonce: String? = null
    // Strong references to prevent garbage collection before ObjC callbacks fire
    private var currentDelegate: AppleSignInDelegate? = null
    private var currentPresentationProvider: ApplePresentationContextProvider? = null
    private var currentController: ASAuthorizationController? = null

    actual suspend fun signIn(): AppleSignInResult = suspendCancellableCoroutine { continuation ->
        val nonce = generateNonce()
        currentNonce = nonce
        val hashedNonce = sha256(nonce)

        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(
                ASAuthorizationScopeFullName,
                ASAuthorizationScopeEmail
            )
            this.nonce = hashedNonce
        }

        val controller = ASAuthorizationController(
            authorizationRequests = listOf(request)
        )

        val delegate = AppleSignInDelegate { result ->
            if (continuation.isActive) {
                when {
                    result.idToken != null -> {
                        continuation.resume(
                            AppleSignInResult(
                                idToken = result.idToken,
                                nonce = currentNonce,
                                error = null
                            )
                        )
                    }
                    else -> {
                        continuation.resume(
                            AppleSignInResult(
                                idToken = null,
                                nonce = null,
                                error = result.error ?: "Apple Sign-In failed"
                            )
                        )
                    }
                }
            }
            // Release strong references after callback
            currentDelegate = null
            currentPresentationProvider = null
            currentController = null
        }

        val presentationProvider = ApplePresentationContextProvider()

        // Store strong references so ObjC weak delegate/provider aren't garbage collected
        currentDelegate = delegate
        currentPresentationProvider = presentationProvider
        currentController = controller

        controller.delegate = delegate
        controller.presentationContextProvider = presentationProvider
        controller.performRequests()
    }

    private fun generateNonce(length: Int = 32): String {
        val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"
        val result = StringBuilder()
        val bytes = ByteArray(length)

        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, length.toULong(), pinned.addressOf(0))
        }

        bytes.forEach { byte ->
            val index = (byte.toInt() and 0xFF) % charset.length
            result.append(charset[index])
        }

        return result.toString()
    }

    private fun sha256(input: String): String {
        val data = input.encodeToByteArray()
        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)

        data.usePinned { dataPinned ->
            hash.usePinned { hashPinned ->
                CC_SHA256(
                    dataPinned.addressOf(0),
                    data.size.toUInt(),
                    hashPinned.addressOf(0).reinterpret<UByteVar>()
                )
            }
        }
        return hash.joinToString("") { byte ->
            val hex = (byte.toInt() and 0xFF).toString(16)
            if (hex.length == 1) "0$hex" else hex
        }
    }
}

private data class AppleSignInDelegateResult(
    val idToken: String?,
    val error: String?
)

@OptIn(ExperimentalForeignApi::class)
private class AppleSignInDelegate(
    private val onComplete: (AppleSignInDelegateResult) -> Unit
) : NSObject(), ASAuthorizationControllerDelegateProtocol {

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        val credential = didCompleteWithAuthorization.credential
        if (credential is ASAuthorizationAppleIDCredential) {
            val idTokenData = credential.identityToken
            if (idTokenData != null) {
                val idToken = NSString.create(idTokenData, NSUTF8StringEncoding) as? String
                onComplete(AppleSignInDelegateResult(idToken = idToken, error = null))
            } else {
                onComplete(AppleSignInDelegateResult(idToken = null, error = "No identity token"))
            }
        } else {
            onComplete(AppleSignInDelegateResult(idToken = null, error = "Unexpected credential type"))
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        val errorMessage = when (didCompleteWithError.code) {
            ASAuthorizationErrorCanceled -> "Sign-in cancelled"
            ASAuthorizationErrorFailed -> "Sign-in failed"
            ASAuthorizationErrorInvalidResponse -> "Invalid response"
            ASAuthorizationErrorNotHandled -> "Not handled"
            ASAuthorizationErrorUnknown -> "Unknown error"
            else -> didCompleteWithError.localizedDescription
        }
        onComplete(AppleSignInDelegateResult(idToken = null, error = errorMessage))
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ApplePresentationContextProvider : NSObject(), ASAuthorizationControllerPresentationContextProvidingProtocol {
    override fun presentationAnchorForAuthorizationController(
        controller: ASAuthorizationController
    ): ASPresentationAnchor {
        // Get the key window for presentation
        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScene = scenes.firstOrNull { scene ->
            scene is platform.UIKit.UIWindowScene
        } as? platform.UIKit.UIWindowScene

        return windowScene?.windows?.firstOrNull { window ->
            (window as? UIWindow)?.isKeyWindow() == true
        } as? UIWindow ?: UIWindow()
    }
}
