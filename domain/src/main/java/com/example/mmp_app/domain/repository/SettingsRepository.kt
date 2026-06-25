package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SettingsRepository {
    fun getCurrentUser(): Flow<Result<FullUserDetailDto>>
    
    suspend fun updateProfile(
        name: String,
        phone: String?,
        gender: String?,
        dob: String?,
        address: String?,
        avatarFile: File?
    ): Result<SettingsUserDto>
    
    suspend fun changePassword(
        current: String,
        new: String,
        confirm: String
    ): Result<String> // Returns new token
    
    suspend fun updateNotificationPreferences(
        prefs: NotificationPreferencesDto
    ): Result<NotificationPreferencesDto>
    
    suspend fun updateTwoFactor(
        enabled: Boolean,
        method: String?
    ): Result<TwoFactorDataDto>
}
