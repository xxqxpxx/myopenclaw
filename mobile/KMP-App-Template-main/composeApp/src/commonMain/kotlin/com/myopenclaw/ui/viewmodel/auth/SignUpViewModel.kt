package com.myopenclaw.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.data.auth.AppleSignInProvider
import com.myopenclaw.data.auth.GoogleSignInProvider
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.session.SessionManager
import com.myopenclaw.domain.models.User
import com.myopenclaw.domain.repository.AuthRepository
import com.myopenclaw.domain.usecase.auth.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val authRepository: AuthRepository,
    private val googleSignInProvider: GoogleSignInProvider,
    private val appleSignInProvider: AppleSignInProvider,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun signUp(email: String, password: String, displayName: String) {
        // println("SignUpViewModel: signUp called with email='$email', displayName='$displayName', password.length=${password.length}")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            // println("SignUpViewModel: State set to Loading")

            signUpUseCase(email, password, displayName)
                .onSuccess { user ->
                    // println("SignUpViewModel: Sign up SUCCESS, user=${user.email}")
                    // Save session and wait for RevenueCat sync to complete before setting success state
                    sessionManager.saveSession(user)
                    // println("SignUpViewModel: Session saved via SessionManager (including RevenueCat sync)")
                    _state.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    // println("SignUpViewModel: Sign up FAILED, error=${exception.message}")
                    exception.printStackTrace()
                    _state.value = AuthState.Error(
                        exception.message ?: "Sign up failed"
                    )
                }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            try {
                val result = googleSignInProvider.signIn()

                if (result.isSuccess && result.idToken != null) {
                    authRepository.signInWithGoogle(result.idToken)
                        .onSuccess { user ->
                            // Save session and wait for RevenueCat sync to complete before setting success state
                            sessionManager.saveSession(user)
                            // println("SignUpViewModel: Session saved via SessionManager (including RevenueCat sync)")
                            _state.value = AuthState.Success(user)
                        }
                        .onFailure { exception ->
                            _state.value = AuthState.Error(
                                exception.message ?: "Google sign-in failed"
                            )
                        }
                } else {
                    _state.value = AuthState.Error(
                        result.error ?: "Google sign-in was cancelled or failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(
                    e.message ?: "Google sign-in failed"
                )
            }
        }
    }

    fun signUpWithApple() {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            try {
                val result = appleSignInProvider.signIn()

                if (result.isSuccess && result.idToken != null && result.nonce != null) {
                    authRepository.signInWithApple(result.idToken, result.nonce)
                        .onSuccess { user ->
                            // Save session and wait for RevenueCat sync to complete before setting success state
                            sessionManager.saveSession(user)
                            // println("SignUpViewModel: Session saved via SessionManager (including RevenueCat sync)")
                            _state.value = AuthState.Success(user)
                        }
                        .onFailure { exception ->
                            _state.value = AuthState.Error(
                                exception.message ?: "Apple sign-in failed"
                            )
                        }
                } else {
                    _state.value = AuthState.Error(
                        result.error ?: "Apple sign-in was cancelled or failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(
                    e.message ?: "Apple sign-in failed"
                )
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
