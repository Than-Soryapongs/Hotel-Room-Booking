package com.madproject.roombookingapp.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madproject.roombookingapp.data.model.SignupRequest
import com.madproject.roombookingapp.data.model.SignupResponse
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.util.Resource
import com.madproject.roombookingapp.util.Validation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _signupState = MutableLiveData<SignupState>()
    val signupState: LiveData<SignupState> = _signupState

    fun signup(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        gender: String
    ) {
        // Validation
        val usernameValidation = Validation.validateUsername(username)
        if (!usernameValidation.isValid) {
            _signupState.value = SignupState.Error(usernameValidation.errorMessage!!)
            return
        }

        val emailValidation = Validation.validateEmail(email)
        if (!emailValidation.isValid) {
            _signupState.value = SignupState.Error(emailValidation.errorMessage!!)
            return
        }

        val passwordValidation = Validation.validatePassword(password)
        if (!passwordValidation.isValid) {
            _signupState.value = SignupState.Error(passwordValidation.errorMessage!!)
            return
        }

        if (password != confirmPassword) {
            _signupState.value = SignupState.Error("Passwords do not match")
            return
        }

        val firstNameValidation = Validation.validateName(firstName, "First name")
        if (!firstNameValidation.isValid) {
            _signupState.value = SignupState.Error(firstNameValidation.errorMessage!!)
            return
        }

        val lastNameValidation = Validation.validateName(lastName, "Last name")
        if (!lastNameValidation.isValid) {
            _signupState.value = SignupState.Error(lastNameValidation.errorMessage!!)
            return
        }

        viewModelScope.launch {
            val request = SignupRequest(username, email, password, firstName, lastName, gender)
            repository.signup(request).collect { resource ->
                when (resource) {
                    is Resource.Loading<*> -> _signupState.value = SignupState.Loading
                    is Resource.Success<*> -> _signupState.value = SignupState.Success(resource.data!! as SignupResponse)
                    is Resource.Error<*> -> _signupState.value = SignupState.Error(resource.message!!)
                }
            }
        }
    }

    sealed class SignupState {
        object Loading : SignupState()
        data class Success(val response: com.madproject.roombookingapp.data.model.SignupResponse) : SignupState()
        data class Error(val message: String) : SignupState()
    }
}