package com.myopenclaw.domain.models

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,
    val createdAt: Long = 0
)
