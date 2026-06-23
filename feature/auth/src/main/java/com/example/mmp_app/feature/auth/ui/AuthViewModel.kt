package com.example.mmp_app.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.repository.AuthRepository
import com.example.mmp_app.domain.repository.LoginResult

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent = _otpSent.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Email and password are required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(_email.value, _password.value)
            _isLoading.value = false
            result.onSuccess { loginResult ->
                when (loginResult) {
                    is LoginResult.OtpRequired -> {
                        _otpSent.value = true
                    }
                    is LoginResult.Success -> {
                        _isLoggedIn.value = true
                    }
                }
            }.onFailure {
                _errorMessage.value = it.message ?: "An unknown error occurred"
            }
        }
    }

    fun verifyOtp(otp: String) {
        if (otp.length < 6) {
            _errorMessage.value = "Enter 6-digit OTP"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.verifyOtp(_email.value, otp)
            _isLoading.value = false
            result.onSuccess {
                _isLoggedIn.value = true
            }.onFailure {
                _errorMessage.value = it.message ?: "OTP verification failed"
            }
        }
    }
    
    fun resetError() {
        _errorMessage.value = null
    }

    fun resetAuthState() {
        _otpSent.value = false
        _isLoggedIn.value = false
        _email.value = ""
        _password.value = ""
    }
}
