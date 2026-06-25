package com.example.mmp_app.data.repository

import com.example.mmp_app.core.utils.SessionManager
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.exception.handleApiResponse
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val sessionManager: SessionManager,
    private val json: Json
) : SettingsRepository {

    override fun getCurrentUser(): Flow<Result<FullUserDetailDto>> = flow {
        try {
            val response = apiService.getCurrentUser()
            val result = handleApiResponse(response, json)
            emit(Result.success(result.user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun updateProfile(
        name: String,
        phone: String?,
        gender: String?,
        dob: String?,
        address: String?,
        avatarFile: File?
    ): Result<SettingsUserDto> {
        return try {
            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val phoneBody = phone?.toRequestBody("text/plain".toMediaTypeOrNull())
            val genderBody = gender?.toRequestBody("text/plain".toMediaTypeOrNull())
            val dobBody = dob?.toRequestBody("text/plain".toMediaTypeOrNull())
            val addressBody = address?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val avatarPart = avatarFile?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("avatar", it.name, requestFile)
            }

            val response = apiService.updateProfile(
                nameBody, phoneBody, genderBody, dobBody, addressBody, avatarPart
            )
            val result = handleApiResponse(response, json)
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(
        current: String,
        new: String,
        confirm: String
    ): Result<String> {
        return try {
            val response = apiService.changePassword(ChangePasswordRequest(current, new, confirm))
            val result = handleApiResponse(response, json)
            val newToken = result.token
            sessionManager.saveAuthToken(newToken)
            Result.success(newToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationPreferences(
        prefs: NotificationPreferencesDto
    ): Result<NotificationPreferencesDto> {
        return try {
            val response = apiService.updateNotificationPreferences(NotificationPreferencesRequest(prefs))
            val result = handleApiResponse(response, json)
            Result.success(result.notificationPreferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTwoFactor(
        enabled: Boolean,
        method: String?
    ): Result<TwoFactorDataDto> {
        return try {
            val response = apiService.updateTwoFactor(TwoFactorRequest(enabled, method))
            val result = handleApiResponse(response, json)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
