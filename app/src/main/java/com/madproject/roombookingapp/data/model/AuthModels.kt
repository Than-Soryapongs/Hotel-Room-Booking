package com.madproject.roombookingapp.data.model

data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val gender: String
)

data class SignupResponse(
    val message: String,
    val email: String
)

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserResponse
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val pendingEmail: String?,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val address: String?,
    val profileImageUrl: String?,
    val roles: Set<String>,
    val enabled: Boolean,
    val emailVerified: Boolean,
    val createdAt: String,
    val lastLoginAt: String?
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class MessageResponse(
    val message: String,
    val email: String? = null,
    val username: String? = null
)