package com.myopenclaw.ui.viewmodel.auth

import com.myopenclaw.domain.models.User

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class PasswordResetState {
    data object Idle : PasswordResetState()
    data object Loading : PasswordResetState()
    data object Success : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}
