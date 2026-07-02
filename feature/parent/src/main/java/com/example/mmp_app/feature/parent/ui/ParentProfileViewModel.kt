package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ParentProfileDto
import com.example.mmp_app.domain.model.UpdateParentProfileRequest
import com.example.mmp_app.domain.model.ValidationException
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentProfileState(
    val profile: ParentProfileDto? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedAvatarBytes: ByteArray? = null,
    val fieldErrors: Map<String, List<String>> = emptyMap()
)

@HiltViewModel
class ParentProfileViewModel @Inject constructor(
    private val repository: ParentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentProfileState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getProfile().collect { result ->
                result.onSuccess { data ->
                    _uiState.update { it.copy(profile = data, isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun updateProfile(name: String, phone: String?, address: String?, occupation: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null, successMessage = null, fieldErrors = emptyMap()) }
            val avatarBytes = _uiState.value.selectedAvatarBytes
            val result = if (avatarBytes != null) {
                repository.updateProfileMultipart(name, phone, address, occupation, avatarBytes)
            } else {
                val request = UpdateParentProfileRequest(name, phone, address, occupation)
                repository.updateProfile(request)
            }
            result.onSuccess { data ->
                _uiState.update { it.copy(profile = data, isUpdating = false, successMessage = "Profile updated successfully", selectedAvatarBytes = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isUpdating = false) }
                if (error is ValidationException) {
                    _uiState.update { it.copy(fieldErrors = error.errors ?: emptyMap()) }
                    _uiState.update { it.copy(error = error.message) }
                } else {
                    _uiState.update { it.copy(error = error.message) }
                }
            }
        }
    }

    fun onAvatarSelected(bytes: ByteArray) {
        _uiState.update { it.copy(selectedAvatarBytes = bytes) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
