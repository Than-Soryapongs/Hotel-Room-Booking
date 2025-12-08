package com.madproject.roombookingapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madproject.roombookingapp.data.model.ChangeEmailRequest
import com.madproject.roombookingapp.data.model.ChangePasswordRequest
import com.madproject.roombookingapp.data.model.UpdateProfileRequest
import com.madproject.roombookingapp.data.model.UserResponse
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableLiveData<ProfileState>(ProfileState.Loading)
    val profileState: LiveData<ProfileState> = _profileState

    private val _uiMessage = MutableLiveData<String?>()
    val uiMessage: LiveData<String?> = _uiMessage

    init {
        loadProfile()
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                repository.getUserData()?.let {
                    _profileState.value = ProfileState.Data(it)
                }
            }

            when (val result = repository.refreshProfile()) {
                is Resource.Success -> _profileState.value = ProfileState.Data(result.data!!)
                is Resource.Error -> {
                    if (_profileState.value !is ProfileState.Data) {
                        _profileState.value = ProfileState.Error(result.message ?: "Failed to load profile")
                    }
                    _uiMessage.value = result.message
                }
                else -> Unit
            }
        }
    }

    fun updateProfile(firstName: String?, lastName: String?, gender: String?, address: String?) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val request = UpdateProfileRequest(firstName, lastName, gender, address)
            when (val result = repository.updateProfile(request)) {
                is Resource.Success -> {
                    _profileState.value = ProfileState.Data(result.data!!)
                    _uiMessage.value = "Profile updated"
                }
                is Resource.Error -> _profileState.value = ProfileState.Error(result.message ?: "Update failed")
                else -> Unit
            }
        }
    }

    fun changeEmail(newEmail: String, password: String) {
        if (newEmail.isBlank() || password.isBlank()) {
            _uiMessage.value = "Email and password are required"
            return
        }
        viewModelScope.launch {
            when (val result = repository.changeEmail(ChangeEmailRequest(newEmail, password))) {
                is Resource.Success -> _uiMessage.value = result.data?.get("message")
                is Resource.Error -> _uiMessage.value = result.message
                else -> Unit
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        if (oldPassword.isBlank() || newPassword.isBlank()) {
            _uiMessage.value = "Both password fields are required"
            return
        }
        viewModelScope.launch {
            when (val result = repository.changePassword(ChangePasswordRequest(oldPassword, newPassword))) {
                is Resource.Success -> _uiMessage.value = result.data?.get("message")
                is Resource.Error -> _uiMessage.value = result.message
                else -> Unit
            }
        }
    }

    fun uploadProfilePicture(bytes: ByteArray, fileName: String = "avatar.jpg") {
        viewModelScope.launch {
            when (val result = repository.uploadProfilePicture(bytes, fileName)) {
                is Resource.Success -> {
                    _uiMessage.value = result.data?.message
                    loadProfile(true)
                }
                is Resource.Error -> _uiMessage.value = result.message
                else -> Unit
            }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            when (val result = repository.deleteProfilePicture()) {
                is Resource.Success -> {
                    _uiMessage.value = result.data?.get("message")
                    loadProfile(true)
                }
                is Resource.Error -> _uiMessage.value = result.message
                else -> Unit
            }
        }
    }

    fun consumeMessage() {
        _uiMessage.value = null
    }

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Data(val user: UserResponse) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
}
