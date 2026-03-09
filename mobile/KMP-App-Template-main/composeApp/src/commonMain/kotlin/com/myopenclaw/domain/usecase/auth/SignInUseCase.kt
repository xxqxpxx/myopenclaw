package com.myopenclaw.domain.usecase.auth

import com.myopenclaw.domain.models.User
import com.myopenclaw.domain.repository.AuthRepository

class SignInUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        // Validation
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(Exception("Invalid email address"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Password is required"))
        }

        return authRepository.signIn(email, password)
    }
}
