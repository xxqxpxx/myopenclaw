package com.myopenclaw.data.repository

import com.myopenclaw.data.auth.AuthManager
import com.myopenclaw.domain.models.User
import com.myopenclaw.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val authManager: AuthManager
) : AuthRepository {

    override val authState: StateFlow<User?> = authManager.authState

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        // println("AuthRepositoryImpl: signUp called with email='$email', displayName='$displayName'")
        val result = authManager.signUp(email, password, displayName)
        // println("AuthRepositoryImpl: signUp result - success=${result.isSuccess}, error=${result.exceptionOrNull()?.message}")
        return result
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return authManager.signIn(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return authManager.signInWithGoogle(idToken)
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<User> {
        return authManager.signInWithApple(idToken, nonce)
    }

    override suspend fun signOut(): Result<Unit> {
        return authManager.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authManager.sendPasswordResetEmail(email)
    }

    override suspend fun getCurrentUser(): User? {
        return authManager.getCurrentUser()
    }

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        return authManager.getIdToken(forceRefresh)
    }

    override fun isAuthenticated(): Boolean {
        return authManager.isAuthenticated()
    }
}
