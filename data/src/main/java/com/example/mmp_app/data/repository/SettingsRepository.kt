package com.example.mmp_app.data.repository

import android.content.Context
import android.net.Uri
import com.example.mmp_app.core.utils.TokenManager
import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.local.entity.UserProfileEntity
import com.example.mmp_app.data.remote.SettingsApiService
import com.example.mmp_app.domain.model.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val api: SettingsApiService,
    private val tokenManager: TokenManager,
    private val userProfileDao: UserProfileDao
) {
    // Parse error body from Retrofit Response
    private fun parseError(response: Response<*>): String {
        return try {
            val json = response.errorBody()?.string() ?: return "Unknown error"
            val err = Gson().fromJson(json, ApiError::class.java)
            // If validation errors exist, join them
            err.errors?.values?.flatten()?.firstOrNull() ?: err.message
        } catch (e: Exception) { "Unknown error" }
    }

    private suspend fun syncToLocal(user: UserProfile) {
        userProfileDao.insertProfile(
            UserProfileEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role ?: "student", // Provide default role if null
                avatarUrl = user.avatarUrl
            )
        )
    }

    suspend fun getUser(): Result<UserProfile> = try {
        val r = api.getUser()
        if (r.isSuccessful && r.body()?.success == true) {
            val user = r.body()!!.data.user
            syncToLocal(user)
            Result.success(user)
        } else Result.failure(Exception(parseError(r)))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> = try {
        val r = api.updateProfile(request)
        if (r.isSuccessful && r.body()?.success == true) {
            val user = r.body()!!.data.user
            syncToLocal(user)
            Result.success(user)
        } else Result.failure(Exception(parseError(r)))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateProfileWithAvatar(
        name: String?, phone: String?, gender: String?,
        dob: String?, address: String?, avatarUri: Uri?, context: Context
    ): Result<UserProfile> = try {
        val namePart   = name?.toRequestBody("text/plain".toMediaType())
        val phonePart  = phone?.toRequestBody("text/plain".toMediaType())
        val genderPart = gender?.toRequestBody("text/plain".toMediaType())
        val dobPart    = dob?.toRequestBody("text/plain".toMediaType())
        val addrPart   = address?.toRequestBody("text/plain".toMediaType())
        val avatarPart = avatarUri?.let {
            val stream = context.contentResolver.openInputStream(it)!!
            val bytes  = stream.readBytes()
            val body   = bytes.toRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("avatar", "avatar.jpg", body)
        }
        val r = api.updateProfileWithAvatar(namePart, phonePart, genderPart, dobPart, addrPart, avatarPart)
        if (r.isSuccessful && r.body()?.success == true) {
            val user = r.body()!!.data.user
            syncToLocal(user)
            Result.success(user)
        } else Result.failure(Exception(parseError(r)))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun changePassword(
        current: String, new: String, confirm: String
    ): Result<String> = try {
        val r = api.changePassword(ChangePasswordRequest(current, new, confirm))
        if (r.isSuccessful && r.body()?.success == true) {
            // Save new token immediately
            r.body()!!.data?.token?.let { tokenManager.saveToken(it) }
            Result.success(r.body()!!.message)
        } else Result.failure(Exception(parseError(r)))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateNotificationPreferences(
        prefs: NotificationPreferences
    ): Result<NotificationPreferences> = try {
        val r = api.updateNotificationPreferences(NotificationPreferencesRequest(prefs))
        if (r.isSuccessful && r.body()?.success == true)
            Result.success(r.body()!!.data.notificationPreferences)
        else Result.failure(Exception(parseError(r)))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateTwoFactor(
        enabled: Boolean, method: String
    ): Result<TwoFactorData> = try {
        val r = api.updateTwoFactor(TwoFactorRequest(enabled, method))
        if (r.isSuccessful && r.body()?.success == true)
            Result.success(r.body()!!.data)
        else Result.failure(Exception(parseError(r)))
    } catch (e: Exception) { Result.failure(e) }
}
