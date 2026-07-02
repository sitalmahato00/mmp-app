package com.example.mmp_app.domain.model

import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

// GET /api/v1/user response
data class UserResponse(
    val success: Boolean,
    val data: UserData
)

data class UserData(val user: UserProfile)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val gender: String? = null,        // "male" | "female" | "other" | null
    val dob: String? = null,           // "YYYY-MM-DD" | null
    val address: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val role: String? = null,
    @SerializedName("panel_type") val panelType: String? = null,
    @SerializedName("two_factor_enabled") val twoFactorEnabled: Boolean = false,
    @SerializedName("two_factor_method") val twoFactorMethod: String? = "email",   // "email" | "phone"
    @SerializedName("notification_preferences") val notificationPreferences: NotificationPreferences? = null
)

data class NotificationPreferences(
    @SerializedName("email_notices") val emailNotices: Boolean = true,
    @SerializedName("email_marks") val emailMarks: Boolean = true,
    @SerializedName("email_assignments") val emailAssignments: Boolean = true,
    @SerializedName("push_notices") val pushNotices: Boolean = true,
    @SerializedName("push_marks") val pushMarks: Boolean = true,
    @SerializedName("push_assignments") val pushAssignments: Boolean = true,
    @SerializedName("push_attendance") val pushAttendance: Boolean = true
)

// PUT /api/v1/user/profile request
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val gender: String? = null,
    val dob: String? = null,    // format: "YYYY-MM-DD"
    val address: String? = null
)

data class ProfileUpdateResponse(
    val success: Boolean,
    val message: String,
    val data: UserData
)

// POST /api/v1/user/change-password
data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

data class ChangePasswordResponse(
    val success: Boolean,
    val message: String,
    val data: TokenData? = null   // null if 422
)

data class TokenData(
    val token: String,
    @SerializedName("token_type") val tokenType: String
)

// PUT /api/v1/user/notification-preferences
// IMPORTANT: preferences must be nested under "notification_preferences" key
data class NotificationPreferencesRequest(
    @SerializedName("notification_preferences")
    val notificationPreferences: NotificationPreferences
)

data class NotificationPreferencesResponse(
    val success: Boolean,
    val message: String,
    val data: NotificationPreferencesData
)

data class NotificationPreferencesData(
    @SerializedName("notification_preferences")
    val notificationPreferences: NotificationPreferences
)

// PUT /api/v1/user/two-factor
data class TwoFactorRequest(
    @SerializedName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerializedName("two_factor_method") val twoFactorMethod: String  // required even when disabling
)

data class TwoFactorResponse(
    val success: Boolean,
    val message: String,
    val data: TwoFactorData
)

data class TwoFactorData(
    @SerializedName("two_factor_enabled") val twoFactorEnabled: Boolean,
    @SerializedName("two_factor_method") val twoFactorMethod: String
)

// POST /api/auth/refresh-token
data class RefreshTokenResponse(
    val success: Boolean,
    val data: TokenData
)

// Generic error response (HTTP 422 / 401 / 500)
@Serializable
data class ApiError(
    val success: Boolean,
    val message: String,
    val errors: Map<String, List<String>>? = null  // validation errors map
)

class ValidationException(val messageStr: String, val errors: Map<String, List<String>>?) : Exception(messageStr)
