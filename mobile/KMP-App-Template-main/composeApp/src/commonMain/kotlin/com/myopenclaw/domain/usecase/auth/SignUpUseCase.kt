package com.myopenclaw.domain.usecase.auth

import com.myopenclaw.domain.models.User
import com.myopenclaw.domain.repository.AuthRepository

class SignUpUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        // println("SignUpUseCase: invoke called")
        // println("SignUpUseCase: email='$email' (isBlank=${email.isBlank()}, contains@=${email.contains("@")})")
        // println("SignUpUseCase: password.length=${password.length}")
        // println("SignUpUseCase: displayName='$displayName' (isBlank=${displayName.isBlank()})")

        // Validation
        if (email.isBlank() || !email.contains("@")) {
            // println("SignUpUseCase: VALIDATION FAILED - Invalid email address")
            return Result.failure(Exception("Invalid email address"))
        }

        if (password.length < 8) {
            // println("SignUpUseCase: VALIDATION FAILED - Password too short")
            return Result.failure(Exception("Password must be at least 8 characters"))
        }

        if (displayName.isBlank()) {
            // println("SignUpUseCase: VALIDATION FAILED - Name is required")
            return Result.failure(Exception("Name is required"))
        }

        // println("SignUpUseCase: Validation passed, calling authRepository.signUp")
        return authRepository.signUp(email, password, displayName)
    }
}
