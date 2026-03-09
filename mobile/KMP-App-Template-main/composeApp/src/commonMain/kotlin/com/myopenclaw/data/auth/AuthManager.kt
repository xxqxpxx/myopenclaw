package com.myopenclaw.data.auth

import com.myopenclaw.domain.models.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

/**
 * AuthManager interface for Firebase Auth implementations
 */
interface AuthManager {
    suspend fun signUp(email: String, password: String, displayName: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithApple(idToken: String, nonce: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun getCurrentUser(): User?
    suspend fun getIdToken(forceRefresh: Boolean = false): String?
    fun isAuthenticated(): Boolean
    val authState: StateFlow<User?>
}

/**
 * Real Firebase Auth implementation using GitLive Firebase SDK
 */
class FirebaseAuthManager : AuthManager {
    private val auth = Firebase.auth
    private val _authState = MutableStateFlow<User?>(null)
    override val authState: StateFlow<User?> = _authState.asStateFlow()

    init {
        // Initialize with current user if exists
        auth.currentUser?.let { firebaseUser ->
            _authState.value = firebaseUser.toUser()
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        // println("FirebaseAuthManager: signUp called")
        // println("FirebaseAuthManager: email='$email', displayName='$displayName', password.length=${password.length}")
        return try {
            // println("FirebaseAuthManager: Calling Firebase createUserWithEmailAndPassword")
            val result = auth.createUserWithEmailAndPassword(email, password)
            // println("FirebaseAuthManager: createUserWithEmailAndPassword SUCCESS")

            val firebaseUser = result.user ?: throw Exception("User creation failed")
            // println("FirebaseAuthManager: Firebase user created, uid=${firebaseUser.uid}, email=${firebaseUser.email}")

            // Update display name
            // println("FirebaseAuthManager: Updating display name to '$displayName'")
            firebaseUser.updateProfile(displayName = displayName)
            // println("FirebaseAuthManager: Display name updated")

            // Send email verification
            // println("FirebaseAuthManager: Sending email verification")
            firebaseUser.sendEmailVerification()
            // println("FirebaseAuthManager: Email verification sent")

            val user = firebaseUser.toUser()
            _authState.value = user
            // println("FirebaseAuthManager: Sign up complete, user=${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            // println("FirebaseAuthManager: Sign up FAILED - ${e.message}")
            e.printStackTrace()
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        // println("FirebaseAuthManager: signIn called with email='$email'")
        return try {
            // println("FirebaseAuthManager: Calling Firebase signInWithEmailAndPassword")
            val result = auth.signInWithEmailAndPassword(email, password)
            // println("FirebaseAuthManager: signInWithEmailAndPassword SUCCESS")

            val firebaseUser = result.user ?: throw Exception("Sign in failed")
            // println("FirebaseAuthManager: Firebase user signed in, uid=${firebaseUser.uid}, email=${firebaseUser.email}")

            val user = firebaseUser.toUser()
            _authState.value = user
            // println("FirebaseAuthManager: Sign in complete, user=${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            // println("FirebaseAuthManager: Sign in FAILED - ${e.message}")
            // println("FirebaseAuthManager: Exception type: ${e::class.simpleName}")
            // println("FirebaseAuthManager: Exception class name: ${e::class.qualifiedName}")
            e.printStackTrace()
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val result = auth.signInWithCredential(credential)
            val firebaseUser = result.user ?: throw Exception("Google sign in failed")
            val user = firebaseUser.toUser()
            _authState.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<User> {
        return try {
            // Create OAuth credential for Apple Sign-In
            // The OAuthProvider.credential method creates a credential that can be used with signInWithCredential
            val credential = OAuthProvider.credential(
                providerId = "apple.com",
                idToken = idToken,
                rawNonce = nonce
            )

            // Sign in with the credential
            val result = auth.signInWithCredential(credential)
            val firebaseUser = result.user ?: throw Exception("Apple sign in failed")

            val user = firebaseUser.toUser()
            _authState.value = user
            // println("FirebaseAuthManager: Apple Sign-In SUCCESS - user=${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            // println("FirebaseAuthManager: Apple Sign-In FAILED - ${e.message}")
            e.printStackTrace()
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            _authState.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override fun getCurrentUser(): User? {
        return auth.currentUser?.toUser()
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return try {
            auth.currentUser?.getIdToken(forceRefresh)
        } catch (e: Exception) {
            null
        }
    }

    override fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoURL,
            emailVerified = isEmailVerified,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun mapFirebaseException(e: Exception): Exception {
        val errorMessage = e.message?.lowercase() ?: ""
        val exceptionClassName = (e::class.simpleName ?: "").lowercase()
        
        val message = when {
            // Check exception class name first (more reliable)
            exceptionClassName.contains("invalidcredentials") ||
            exceptionClassName.contains("invalidcredential") ||
            errorMessage.contains("incorrect") && (errorMessage.contains("credential") || errorMessage.contains("password")) ||
            errorMessage.contains("malformed") ||
            errorMessage.contains("expired") && errorMessage.contains("credential") ->
                "Incorrect email or password. Please check your credentials and try again."
            
            e.message?.contains("email-already-in-use") == true ->
                "This email is already registered. Please sign in instead."
            e.message?.contains("invalid-email") == true ->
                "Please enter a valid email address."
            e.message?.contains("weak-password") == true ->
                "Password is too weak. Please use at least 6 characters."
            e.message?.contains("user-not-found") == true ->
                "No account found with this email. Please sign up."
            e.message?.contains("wrong-password") == true ->
                "Incorrect password. Please try again."
            e.message?.contains("too-many-requests") == true ->
                "Too many failed attempts. Please try again later."
            e.message?.contains("network-request-failed") == true ->
                "Network error. Please check your connection."
            // Apple Sign-In specific errors
            e.message?.contains("invalid-credential") == true ->
                "Invalid credentials. Please try signing in again."
            e.message?.contains("account-exists-with-different-credential") == true ->
                "An account already exists with this email using a different sign-in method."
            e.message?.contains("credential-already-in-use") == true ->
                "This credential is already associated with another account."
            e.message?.contains("operation-not-allowed") == true ->
                "This sign-in method is not enabled. Please contact support."
            // Password reset specific errors
            e.message?.contains("invalid-recipient-email") == true ||
            e.message?.contains("invalid-sender") == true ||
            e.message?.contains("invalid-message-payload") == true ->
                "Unable to send reset email. Please contact support if this issue persists."
            else -> e.message ?: "An unexpected error occurred"
        }
        return Exception(message)
    }
}

/**
 * Mock implementation for development/testing
 */
class MockAuthManager : AuthManager {
    private var currentUser: User? = null
    private var mockToken: String? = null
    private val _authState = MutableStateFlow<User?>(null)
    override val authState: StateFlow<User?> = _authState.asStateFlow()

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val user = User(
                uid = "mock_${Clock.System.now().toEpochMilliseconds()}",
                email = email,
                displayName = displayName,
                emailVerified = false,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            currentUser = user
            _authState.value = user
            mockToken = "mock_token_${Clock.System.now().toEpochMilliseconds()}"
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val user = User(
                uid = "mock_user_123",
                email = email,
                displayName = "Mock User",
                emailVerified = true,
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
            currentUser = user
            _authState.value = user
            mockToken = "mock_token_${Clock.System.now().toEpochMilliseconds()}"
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return signIn("google@example.com", "")
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<User> {
        return signIn("apple@example.com", "")
    }

    override suspend fun signOut(): Result<Unit> {
        currentUser = null
        _authState.value = null
        mockToken = null
        return Result.success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun getCurrentUser(): User? = currentUser

    override suspend fun getIdToken(forceRefresh: Boolean): String? = mockToken

    override fun isAuthenticated(): Boolean = currentUser != null
}
