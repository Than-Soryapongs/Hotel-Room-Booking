package com.madproject.roombookingapp.ui.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madproject.roombookingapp.data.model.MessageResponse
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _forgotPasswordState = MutableLiveData<ForgotPasswordState>()
    val forgotPasswordState: LiveData<ForgotPasswordState> = _forgotPasswordState

    fun forgotPassword(email: String) {
        val emailValidation = Validation.validateEmail(email)
        if (!emailValidation.isValid) {
            _forgotPasswordState.value = ForgotPasswordState.Error(emailValidation.errorMessage!!)
            return
        }

        viewModelScope.launch {
            repository.forgotPassword(email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _forgotPasswordState.value = ForgotPasswordState.Loading
                    is Resource.Success -> _forgotPasswordState.value = ForgotPasswordState.Success(resource.data!!)
                    is Resource.Error -> _forgotPasswordState.value = ForgotPasswordState.Error(resource.message!!)
                }
            }
        }
    }

    sealed class ForgotPasswordState {
        object Loading : ForgotPasswordState()
        data class Success(val response: MessageResponse) : ForgotPasswordState()
        data class Error(val message: String) : ForgotPasswordState()
    }
}