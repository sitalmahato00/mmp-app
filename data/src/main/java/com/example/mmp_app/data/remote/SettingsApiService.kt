package com.example.mmp_app.data.remote

import com.example.mmp_app.domain.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface SettingsApiService {

    // Get full user profile including all settings fields
    @GET("v1/user")
    suspend fun getUser(): Response<UserResponse>

    // Update profile
    @Multipart
    @PUT("v1/user/profile")
    suspend fun updateProfileWithAvatar(
        @Part("name") name: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): Response<ProfileUpdateResponse>

    @PUT("v1/user/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ProfileUpdateResponse>

    // Change password
    @POST("v1/user/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    // Notification preferences
    @PUT("v1/user/notification-preferences")
    suspend fun updateNotificationPreferences(
        @Body request: NotificationPreferencesRequest
    ): Response<NotificationPreferencesResponse>

    // Two-factor authentication
    @PUT("v1/user/two-factor")
    suspend fun updateTwoFactor(
        @Body request: TwoFactorRequest
    ): Response<TwoFactorResponse>

    // Refresh token
    @POST("auth/refresh-token")
    suspend fun refreshToken(): Response<RefreshTokenResponse>
}
