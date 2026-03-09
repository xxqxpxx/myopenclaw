package com.myopenclaw.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val state: StateFlow<PasswordResetState> = _state.asStateFlow()

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            _state.value = PasswordResetState.Loading

            resetPasswordUseCase(email)
                .onSuccess {
                    _state.value = PasswordResetState.Success
                }
                .onFailure { exception ->
                    _state.value = PasswordResetState.Error(
                        exception.message ?: "Failed to send reset email"
                    )
                }
        }
    }

    fun resetState() {
        _state.value = PasswordResetState.Idle
    }
}
