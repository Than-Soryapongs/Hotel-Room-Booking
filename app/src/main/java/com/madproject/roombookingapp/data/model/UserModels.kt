package com.madproject.roombookingapp.data.model

/**
 * Data contracts for the authenticated user profile domain.
 */
data class UpdateProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val gender: String?,
    val address: String?
)

data class ChangeEmailRequest(
    val newEmail: String,
    val password: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class ProfilePictureResponse(
    val profileImageUrl: String?,
    val message: String,
    val fileSize: Long?,
    val fileName: String?
)
