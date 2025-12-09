package com.madproject.roombookingapp.data.repository

import com.madproject.roombookingapp.data.local.TokenManager
import com.madproject.roombookingapp.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.madproject.roombookingapp.data.remote.ApiService
import com.madproject.roombookingapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    fun signup(request: SignupRequest): Flow<Resource<SignupResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.signup(request)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Signup failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun login(request: LoginRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Tokens are in cookies, but save user data
                tokenManager.saveUserData(authResponse.user)
                emit(Resource.Success(authResponse))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun verifyEmail(token: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.verifyEmail(token)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Verification failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun resendVerification(email: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.resendVerification(email)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to resend"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun forgotPassword(email: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    fun resetPassword(token: String, newPassword: String): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.resetPassword(ResetPasswordRequest(token, newPassword))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Reset failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error"))
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
            tokenManager.clearUserData()
        } catch (e: Exception) {
            // Handle error silently or log it
        }
    }

    suspend fun getUserData(): UserResponse? = tokenManager.getUserData()

    suspend fun isLoggedIn(): Boolean = tokenManager.getUserData() != null

    suspend fun refreshProfile(): Resource<UserResponse> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenManager.saveUserData(user)
                Resource.Success(user)
            } else {
                Resource.Error(response.message() ?: "Failed to load profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<UserResponse> {
        return try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenManager.saveUserData(user)
                Resource.Success(user)
            } else {
                Resource.Error(response.message() ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun changeEmail(request: ChangeEmailRequest): Resource<Map<String, String>> {
        return try {
            val response = apiService.requestEmailChange(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else Resource.Error(response.message() ?: "Failed to change email")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun changePassword(request: ChangePasswordRequest): Resource<Map<String, String>> {
        return try {
            val response = apiService.changePassword(request.oldPassword, request.newPassword)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else Resource.Error(response.message() ?: "Failed to change password")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun uploadProfilePicture(bytes: ByteArray, fileName: String): Resource<ProfilePictureResponse> {
        return try {
            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = apiService.uploadProfilePicture(part)
            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                refreshProfile()
                Resource.Success(profileResponse)
            } else Resource.Error(response.message() ?: "Failed to upload profile picture")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteProfilePicture(): Resource<Map<String, String>> {
        return try {
            val response = apiService.deleteProfilePicture()
            if (response.isSuccessful && response.body() != null) {
                refreshProfile()
                Resource.Success(response.body()!!)
            } else Resource.Error(response.message() ?: "Failed to delete profile picture")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}