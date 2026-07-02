package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.*

interface SettingsRepository {
    suspend fun getUser(): Result<UserProfile>
    
    suspend fun updateProfile(
        name: String?,
        phone: String?,
        gender: String?,
        dob: String?,
        address: String?,
        avatarBytes: ByteArray?
    ): Result<UserProfile>

    suspend fun changePassword(
        current: String, new: String, confirm: String
    ): Result<String>

    suspend fun updateNotificationPreferences(
        prefs: NotificationPreferences
    ): Result<NotificationPreferences>

    suspend fun updateTwoFactor(
        enabled: Boolean, method: String
    ): Result<TwoFactorData>
}
