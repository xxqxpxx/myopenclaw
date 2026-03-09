package com.myopenclaw.domain.repository

import com.myopenclaw.domain.models.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun signUp(email: String, password: String, displayName: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithApple(idToken: String, nonce: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun getIdToken(forceRefresh: Boolean = false): String?
    fun isAuthenticated(): Boolean
    val authState: StateFlow<User?>
}
