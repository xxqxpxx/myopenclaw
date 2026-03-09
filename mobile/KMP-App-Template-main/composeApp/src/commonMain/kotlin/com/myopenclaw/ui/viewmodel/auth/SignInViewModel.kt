package com.myopenclaw.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.data.auth.AppleSignInProvider
import com.myopenclaw.data.auth.GoogleSignInProvider
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.data.session.SessionManager
import com.myopenclaw.domain.models.User
import com.myopenclaw.domain.repository.AuthRepository
import com.myopenclaw.domain.usecase.auth.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    private val authRepository: AuthRepository,
    private val googleSignInProvider: GoogleSignInProvider,
    private val appleSignInProvider: AppleSignInProvider,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        // println("SignInViewModel: signIn called with email='$email'")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            // println("SignInViewModel: State set to Loading")

            signInUseCase(email, password)
                .onSuccess { user ->
                    // println("SignInViewModel: Sign in SUCCESS, user=${user.email}")
                    // Save session and wait for RevenueCat sync to complete before setting success state
                    sessionManager.saveSession(user)
                    // println("SignInViewModel: Session saved via SessionManager (including RevenueCat sync)")
                    _state.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    // println("SignInViewModel: Sign in FAILED, error=${exception.message}")
                    _state.value = AuthState.Error(
                        exception.message ?: "Sign in failed"
                    )
                }
        }
    }

    fun signInWithGoogle() {
        // println("SignInViewModel: signInWithGoogle called")
        viewModelScope.launch {
            _state.value = AuthState.Loading
            // println("SignInViewModel: State set to Loading, calling googleSignInProvider.signIn()")

            try {
                val result = googleSignInProvider.signIn()
                // println("SignInViewModel: GoogleSignInProvider returned - isSuccess=${result.isSuccess}, hasToken=${result.idToken != null}, error=${result.error}")

                if (result.isSuccess && result.idToken != null) {
                    // println("SignInViewModel: Got Google ID token, signing in with Firebase...")
                    authRepository.signInWithGoogle(result.idToken)
                        .onSuccess { user ->
                            // println("SignInViewModel: Firebase sign-in SUCCESS, user=${user.email}")
                            // Save session and wait for RevenueCat sync to complete before setting success state
                            sessionManager.saveSession(user)
                            // println("SignInViewModel: Session saved via SessionManager (including RevenueCat sync)")
                            _state.value = AuthState.Success(user)
                        }
                        .onFailure { exception ->
                            // println("SignInViewModel: Firebase sign-in FAILED - ${exception.message}")
                            _state.value = AuthState.Error(
                                exception.message ?: "Google sign-in failed"
                            )
                        }
                } else {
                    // println("SignInViewModel: Google sign-in failed or cancelled - ${result.error}")
                    _state.value = AuthState.Error(
                        result.error ?: "Google sign-in was cancelled or failed"
                    )
                }
            } catch (e: Exception) {
                // println("SignInViewModel: Exception during Google sign-in - ${e.message}")
                e.printStackTrace()
                _state.value = AuthState.Error(
                    e.message ?: "Google sign-in failed"
                )
            }
        }
    }

    fun signInWithApple() {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            try {
                val result = appleSignInProvider.signIn()

                if (result.isSuccess && result.idToken != null && result.nonce != null) {
                    authRepository.signInWithApple(result.idToken, result.nonce)
                        .onSuccess { user ->
                            // Save session and wait for RevenueCat sync to complete before setting success state
                            sessionManager.saveSession(user)
                            // println("SignInViewModel: Session saved via SessionManager (including RevenueCat sync)")
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
