package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDataDto(
    val user: SettingsUserDto
)

@Serializable
data class SettingsUserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String,
    @SerialName("panel_type") val panelType: String? = null
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String
)

@Serializable
data class TokenDataDto(
    val token: String,
    @SerialName("token_type") val tokenType: String
)

@Serializable
data class NotificationPreferencesRequest(
    @SerialName("notification_preferences") val notificationPreferences: NotificationPreferencesDto
)

@Serializable
data class NotificationPreferencesDto(
    @SerialName("email_notices") val emailNotices: Boolean,
    @SerialName("email_marks") val emailMarks: Boolean,
    @SerialName("email_assignments") val emailAssignments: Boolean,
    @SerialName("push_notices") val pushNotices: Boolean,
    @SerialName("push_marks") val pushMarks: Boolean,
    @SerialName("push_assignments") val pushAssignments: Boolean,
    @SerialName("push_attendance") val pushAttendance: Boolean
)

@Serializable
data class NotificationPreferencesDataDto(
    @SerialName("notification_preferences") val notificationPreferences: NotificationPreferencesDto
)

@Serializable
data class TwoFactorRequest(
    @SerialName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerialName("two_factor_method") val twoFactorMethod: String? = null
)

@Serializable
data class TwoFactorDataDto(
    @SerialName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerialName("two_factor_method") val twoFactorMethod: String? = null
)

@Serializable
data class FullUserDetailDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val address: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("notification_preferences") val notificationPreferences: NotificationPreferencesDto? = null,
    @SerialName("two_factor_enabled") val twoFactorEnabled: Boolean = false,
    @SerialName("two_factor_method") val twoFactorMethod: String? = null
)

@Serializable
data class FullUserDataDto(
    val user: FullUserDetailDto
)
