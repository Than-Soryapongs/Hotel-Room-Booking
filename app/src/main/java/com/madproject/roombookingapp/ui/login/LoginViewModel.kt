package com.madproject.roombookingapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madproject.roombookingapp.data.model.AuthResponse
import com.madproject.roombookingapp.data.model.LoginRequest
import com.madproject.roombookingapp.data.repository.AuthRepository
import com.madproject.roombookingapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank()) {
            _loginState.value = LoginState.Error("Username or email is required")
            return
        }
        if (password.isBlank()) {
            _loginState.value = LoginState.Error("Password is required")
            return
        }

        viewModelScope.launch {
            repository.login(LoginRequest(usernameOrEmail, password)).collect { resource ->
                when (resource) {
                    is Resource.Loading<*> -> _loginState.value = LoginState.Loading
                    is Resource.Success<*> -> _loginState.value = LoginState.Success(resource.data!! as AuthResponse)
                    is Resource.Error<*> -> _loginState.value = LoginState.Error(resource.message!!)
                }
            }
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val response: com.madproject.roombookingapp.data.model.AuthResponse) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}