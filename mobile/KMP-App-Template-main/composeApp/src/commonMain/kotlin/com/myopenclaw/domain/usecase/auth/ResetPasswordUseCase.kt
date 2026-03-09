package com.myopenclaw.domain.usecase.auth

import com.myopenclaw.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(Exception("Invalid email address"))
        }

        return authRepository.sendPasswordResetEmail(email)
    }
}
