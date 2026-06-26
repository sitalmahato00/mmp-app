package com.example.mmp_app.feature.student.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.data.repository.SettingsRepository
import com.example.mmp_app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // One-time events (snackbar messages)
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init { loadUser() }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getUser().fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(
                        isLoading  = false,
                        user       = user,
                        name       = user.name,
                        phone      = user.phone ?: "",
                        gender     = user.gender ?: "",
                        dob        = user.dob ?: "",
                        address    = user.address ?: "",
                        twoFactorEnabled = user.twoFactorEnabled,
                        twoFactorMethod  = user.twoFactorMethod ?: "email",
                        notifPrefs = user.notificationPreferences ?: NotificationPreferences()
                    ) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.Error(e.message ?: "Failed to load profile"))
                }
            )
        }
    }

    // Profile field updates (local state only — no API call yet)
    fun onNameChange(v: String)    = _uiState.update { it.copy(name = v) }
    fun onPhoneChange(v: String)   = _uiState.update { it.copy(phone = v) }
    fun onGenderChange(v: String)  = _uiState.update { it.copy(gender = v) }
    fun onDobChange(v: String)     = _uiState.update { it.copy(dob = v) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v) }
    fun onAvatarSelected(uri: Uri) = _uiState.update { it.copy(selectedAvatarUri = uri) }

    fun saveProfile(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true) }
            val s = _uiState.value
            val result = if (s.selectedAvatarUri != null) {
                repository.updateProfileWithAvatar(
                    s.name, s.phone, s.gender.ifBlank { null },
                    s.dob.ifBlank { null }, s.address.ifBlank { null },
                    s.selectedAvatarUri, context
                )
            } else {
                repository.updateProfile(UpdateProfileRequest(
                    name    = s.name,
                    phone   = s.phone.ifBlank { null },
                    gender  = s.gender.ifBlank { null },
                    dob     = s.dob.ifBlank { null },
                    address = s.address.ifBlank { null }
                ))
            }
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isSavingProfile = false, user = user, selectedAvatarUri = null) }
                    _events.emit(SettingsEvent.Success("Profile updated successfully"))
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSavingProfile = false) }
                    _events.emit(SettingsEvent.Error(e.message ?: "Failed to update profile"))
                }
            )
        }
    }

    // Password fields (local only)
    fun onCurrentPasswordChange(v: String) = _uiState.update { it.copy(currentPassword = v) }
    fun onNewPasswordChange(v: String)     = _uiState.update { it.copy(newPassword = v) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v) }

    fun changePassword() {
        val s = _uiState.value
        if (s.newPassword != s.confirmPassword) {
            viewModelScope.launch { _events.emit(SettingsEvent.Error("Passwords do not match")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true) }
            repository.changePassword(s.currentPassword, s.newPassword, s.confirmPassword).fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(
                        isChangingPassword = false,
                        currentPassword = "", newPassword = "", confirmPassword = ""
                    ) }
                    _events.emit(SettingsEvent.PasswordChanged)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isChangingPassword = false) }
                    _events.emit(SettingsEvent.Error(e.message ?: "Failed to change password"))
                }
            )
        }
    }

    // Notification prefs — call API immediately on toggle
    fun toggleNotifPref(key: String, value: Boolean) {
        val current = _uiState.value.notifPrefs
        val updated = when (key) {
            "email_notices"     -> current.copy(emailNotices = value)
            "email_marks"       -> current.copy(emailMarks = value)
            "email_assignments" -> current.copy(emailAssignments = value)
            "push_notices"      -> current.copy(pushNotices = value)
            "push_marks"        -> current.copy(pushMarks = value)
            "push_assignments"  -> current.copy(pushAssignments = value)
            "push_attendance"   -> current.copy(pushAttendance = value)
            else -> current
        }
        // Optimistic update
        _uiState.update { it.copy(notifPrefs = updated) }
        viewModelScope.launch {
            repository.updateNotificationPreferences(updated).fold(
                onSuccess = { saved -> _uiState.update { it.copy(notifPrefs = saved) } },
                onFailure = { _ ->
                    // Revert on failure
                    _uiState.update { it.copy(notifPrefs = current) }
                    _events.emit(SettingsEvent.Error("Failed to save notification preference"))
                }
            )
        }
    }

    // 2FA toggle
    fun setTwoFactor(enabled: Boolean, method: String = _uiState.value.twoFactorMethod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating2FA = true) }
            repository.updateTwoFactor(enabled, method).fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(
                        isUpdating2FA    = false,
                        twoFactorEnabled = data.twoFactorEnabled,
                        twoFactorMethod  = data.twoFactorMethod
                    ) }
                    _events.emit(SettingsEvent.Success(
                        if (data.twoFactorEnabled) "2FA enabled" else "2FA disabled"
                    ))
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isUpdating2FA = false) }
                    _events.emit(SettingsEvent.Error(e.message ?: "Failed to update 2FA"))
                }
            )
        }
    }
}

// UI State
data class SettingsUiState(
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    // Profile form
    val name: String = "",
    val phone: String = "",
    val gender: String = "",
    val dob: String = "",
    val address: String = "",
    val selectedAvatarUri: Uri? = null,
    val isSavingProfile: Boolean = false,
    // Password form
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isChangingPassword: Boolean = false,
    // Notification prefs
    val notifPrefs: NotificationPreferences = NotificationPreferences(),
    // 2FA
    val twoFactorEnabled: Boolean = false,
    val twoFactorMethod: String = "email",
    val isUpdating2FA: Boolean = false
)

// One-time events
sealed class SettingsEvent {
    data class Success(val message: String) : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
    object PasswordChanged : SettingsEvent()
}
