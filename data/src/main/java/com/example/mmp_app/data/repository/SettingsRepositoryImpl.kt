package com.example.mmp_app.data.repository

import com.example.mmp_app.core.utils.TokenManager
import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.local.entity.UserProfileEntity
import com.example.mmp_app.data.remote.SettingsApiService
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.SettingsRepository
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val api: SettingsApiService,
    private val tokenManager: TokenManager,
    private val userProfileDao: UserProfileDao
) : SettingsRepository {

    // Parse error body from Retrofit Response
    private fun parseError(response: Response<*>): Exception {
        return try {
            val json = response.errorBody()?.string() ?: return Exception("Unknown error")
            val err = Gson().fromJson(json, ApiError::class.java)
            ValidationException(err.message, err.errors)
        } catch (e: Exception) { Exception("Unknown error") }
    }

    private suspend fun syncToLocal(user: UserProfile) {
        userProfileDao.insertProfile(
            UserProfileEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role ?: "student",
                avatarUrl = user.avatarUrl
            )
        )
    }

    override suspend fun getUser(): Result<UserProfile> = try {
        val r = api.getUser()
        if (r.isSuccessful && r.body()?.success == true) {
            val user = r.body()!!.data.user
            syncToLocal(user)
            Result.success(user)
        } else Result.failure(parseError(r))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateProfile(
        name: String?,
        phone: String?,
        gender: String?,
        dob: String?,
        address: String?,
        avatarBytes: ByteArray?
    ): Result<UserProfile> = try {
        val response = if (avatarBytes != null) {
            val toRequestBody = { s: String? -> s?.toRequestBody("text/plain".toMediaType()) }

            val avatarPart = avatarBytes.toRequestBody("image/*".toMediaType())
                .let { MultipartBody.Part.createFormData("avatar", "avatar.jpg", it) }

            api.updateProfileWithAvatar(
                name    = toRequestBody(name),
                phone   = toRequestBody(phone),
                gender  = toRequestBody(gender),
                dob     = toRequestBody(dob),
                address = toRequestBody(address),
                avatar  = avatarPart
            )
        } else {
            api.updateProfileJson(
                UpdateProfileRequest(
                    name    = name,
                    phone   = phone,
                    gender  = gender,
                    dob     = dob,
                    address = address
                )
            )
        }

        if (response.isSuccessful && response.body()?.success == true) {
            val user = response.body()!!.data.user
            syncToLocal(user)
            Result.success(user)
        } else Result.failure(parseError(response))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun changePassword(
        current: String, new: String, confirm: String
    ): Result<String> = try {
        val r = api.changePassword(ChangePasswordRequest(current, new, confirm))
        if (r.isSuccessful && r.body()?.success == true) {
            r.body()!!.data?.token?.let { tokenManager.saveToken(it) }
            Result.success(r.body()!!.message)
        } else Result.failure(parseError(r))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateNotificationPreferences(
        prefs: NotificationPreferences
    ): Result<NotificationPreferences> = try {
        val r = api.updateNotificationPreferences(NotificationPreferencesRequest(prefs))
        if (r.isSuccessful && r.body()?.success == true)
            Result.success(r.body()!!.data.notificationPreferences)
        else Result.failure(parseError(r))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateTwoFactor(
        enabled: Boolean, method: String
    ): Result<TwoFactorData> = try {
        val r = api.updateTwoFactor(TwoFactorRequest(enabled, method))
        if (r.isSuccessful && r.body()?.success == true)
            Result.success(r.body()!!.data)
        else Result.failure(parseError(r))
    } catch (e: Exception) { Result.failure(e) }
}
