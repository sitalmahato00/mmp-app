package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.data.remote.exception.ApiException
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FullUserDetailDto?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _validationErrors = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val validationErrors = _validationErrors.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCurrentUser().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _user.value = it
                }.onFailure {
                    handleError(it)
                }
            }
        }
    }

    fun updateProfile(
        name: String,
        phone: String?,
        gender: String?,
        dob: String?,
        address: String?,
        avatarFile: File?
    ) {
        viewModelScope.launch {
            clearMessages()
            _isLoading.value = true
            val result = repository.updateProfile(name, phone, gender, dob, address, avatarFile)
            _isLoading.value = false
            result.onSuccess {
                _successMessage.value = "Profile updated successfully"
                loadCurrentUser()
            }.onFailure {
                handleError(it)
            }
        }
    }

    fun changePassword(current: String, new: String, confirm: String) {
        viewModelScope.launch {
            clearMessages()
            _isLoading.value = true
            val result = repository.changePassword(current, new, confirm)
            _isLoading.value = false
            result.onSuccess {
                _successMessage.value = "Password changed successfully"
            }.onFailure {
                handleError(it)
            }
        }
    }

    fun updateNotificationPreferences(prefs: NotificationPreferencesDto) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateNotificationPreferences(prefs)
            _isLoading.value = false
            result.onSuccess {
                _successMessage.value = "Notification preferences updated"
                // Update local user state if needed
                _user.value = _user.value?.copy(notificationPreferences = it)
            }.onFailure {
                handleError(it)
            }
        }
    }

    fun updateTwoFactor(enabled: Boolean, method: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateTwoFactor(enabled, method)
            _isLoading.value = false
            result.onSuccess {
                _successMessage.value = if (enabled) "Two-factor authentication enabled" else "Two-factor authentication disabled"
                _user.value = _user.value?.copy(
                    twoFactorEnabled = it.twoFactorEnabled,
                    twoFactorMethod = it.twoFactorMethod
                )
            }.onFailure {
                handleError(it)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        if (throwable is ApiException.ValidationException) {
            _validationErrors.value = throwable.errors
            _error.value = "Please fix the errors below"
        } else if (throwable is ApiException) {
            _error.value = throwable.getUserFriendlyMessage()
        } else {
            _error.value = throwable.message ?: "An unexpected error occurred"
        }
    }

    private fun clearMessages() {
        _error.value = null
        _validationErrors.value = emptyMap()
        _successMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
