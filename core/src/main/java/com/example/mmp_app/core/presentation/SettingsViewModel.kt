package com.example.mmp_app.core.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.example.mmp_app.core.utils.SessionManager
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.AuthRepository
import com.example.mmp_app.domain.repository.ParentRepository
import com.example.mmp_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val parentRepository: ParentRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // One-time events (snackbar messages)
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        // Collect user profile to stay in sync with role
        viewModelScope.launch {
            authRepository.getUserProfile().collect { profile ->
                profile?.role?.let { r ->
                    _uiState.update { it.copy(role = r.lowercase()) }
                }
            }
        }
        
        // Initial load
        refreshRole()
        loadUser()
    }

    private fun refreshRole() {
        val role = sessionManager.getUserRole()?.lowercase() ?: "student"
        _uiState.update { it.copy(role = role) }
    }

    fun loadUser() {
        viewModelScope.launch {
            refreshRole() // Ensure role is up to date
            
            val role = _uiState.value.role
            
            // Reset fields to avoid data leakage between roles
            _uiState.update { it.copy(
                isLoading = true,
                user = null,
                parentProfile = null,
                name = "",
                phone = "",
                gender = "",
                dob = "",
                address = "",
                occupation = "",
                relationToStudent = ""
            ) }
            
            if (role == "parent") {
                parentRepository.getProfile().collect { result ->
                    result.fold(
                        onSuccess = { parent ->
                            _uiState.update { it.copy(
                                isLoading = false,
                                name = parent.name,
                                phone = parent.phone ?: "",
                                gender = parent.gender ?: "",
                                address = parent.address ?: "",
                                occupation = parent.occupation ?: "",
                                relationToStudent = parent.relationToStudent,
                                parentProfile = parent,
                                user = null // Ensure student user is null for parent
                            ) }
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(isLoading = false) }
                            _events.emit(SettingsEvent.Error(e.message ?: "Failed to load parent profile"))
                        }
                    )
                }
            } else {
                repository.getUser().fold(
                    onSuccess = { user ->
                        _uiState.update { it.copy(
                            isLoading  = false,
                            user       = user,
                            parentProfile = null, // Ensure parent profile is null for others
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
    }

    // Profile field updates (local state only — no API call yet)
    fun onNameChange(v: String)    = _uiState.update { it.copy(name = v, fieldErrors = it.fieldErrors - "name") }
    fun onPhoneChange(v: String)   = _uiState.update { it.copy(phone = v, fieldErrors = it.fieldErrors - "phone") }
    fun onGenderChange(v: String)  = _uiState.update { it.copy(gender = v, fieldErrors = it.fieldErrors - "gender") }
    fun onDobChange(v: String)     = _uiState.update { it.copy(dob = v, fieldErrors = it.fieldErrors - "dob") }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v, fieldErrors = it.fieldErrors - "address") }
    fun onOccupationChange(v: String) = _uiState.update { it.copy(occupation = v, fieldErrors = it.fieldErrors - "occupation") }
    fun onAvatarSelected(uri: Uri) = _uiState.update { it.copy(selectedAvatarUri = uri, fieldErrors = it.fieldErrors - "avatar") }

    fun saveProfile(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, fieldErrors = emptyMap()) }
            val s = _uiState.value

            val result = if (s.role == "parent") {
                if (s.selectedAvatarUri != null) {
                    val bytes = context.contentResolver.openInputStream(s.selectedAvatarUri)!!.readBytes()
                    parentRepository.updateProfileMultipart(
                        name = s.name,
                        phone = s.phone.ifBlank { null },
                        address = s.address.ifBlank { null },
                        occupation = s.occupation.ifBlank { null },
                        avatarBytes = bytes
                    ).map { UserProfile(0, it.name, it.email, it.phone, it.gender, null, it.address, it.avatarUrl) }
                } else {
                    parentRepository.updateProfile(UpdateParentProfileRequest(
                        name = s.name,
                        phone = s.phone.ifBlank { null },
                        address = s.address.ifBlank { null },
                        occupation = s.occupation.ifBlank { null }
                    )).map { UserProfile(0, it.name, it.email, it.phone, it.gender, null, it.address, it.avatarUrl) }
                }
            } else {
                val avatarBytes = s.selectedAvatarUri?.let {
                    context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                }
                repository.updateProfile(
                    name      = s.name.ifBlank { null },
                    phone     = s.phone.ifBlank { null },
                    gender    = s.gender.ifBlank { null },
                    dob       = s.dob.ifBlank { null },
                    address   = s.address.ifBlank { null },
                    avatarBytes = avatarBytes
                )
            }

            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(
                        isSavingProfile    = false,
                        user               = if (s.role != "parent") user else null,
                        selectedAvatarUri  = null,
                        name               = user.name,
                        phone              = user.phone ?: "",
                        gender             = user.gender ?: "",
                        dob                = user.dob ?: "",
                        address            = user.address ?: ""
                    ) }
                    // Invalidate cached avatar in Coil so it reloads
                    user.avatarUrl?.let { url ->
                        if (url.contains("storage")) {
                            context.imageLoader.memoryCache?.clear()
                        }
                    }
                    _events.emit(SettingsEvent.Success("Profile updated successfully"))
                    if (s.role == "parent") {
                        loadUser() // Refresh parent specific data
                        return@fold
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSavingProfile = false) }
                    if (e is ValidationException) {
                        _uiState.update { it.copy(fieldErrors = e.errors ?: emptyMap()) }
                        _events.emit(SettingsEvent.Error(e.message ?: "Validation failed"))
                    } else {
                        _events.emit(SettingsEvent.Error(e.message ?: "Failed to update profile"))
                    }
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
    val role: String = "student",
    val user: UserProfile? = null,
    val parentProfile: ParentProfileDto? = null,
    // Profile form
    val name: String = "",
    val phone: String = "",
    val gender: String = "",
    val dob: String = "",
    val address: String = "",
    val occupation: String = "",
    val relationToStudent: String = "",
    val selectedAvatarUri: Uri? = null,
    val isSavingProfile: Boolean = false,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
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
