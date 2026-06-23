package com.example.mmp_app.domain.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BaseResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("requires_2fa") val requires2fa: Boolean? = false,
    @SerialName("data") val data: T? = null,
    @SerialName("errors") val errors: Map<String, List<String>>? = null
)

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class OtpResponse(
    @SerialName("otp_sent") val otpSent: Boolean,
    @SerialName("role") val role: String? = null
)

@Serializable
data class OtpVerifyRequest(
    @SerialName("email") val email: String,
    @SerialName("otp") val otp: String
)

@Serializable
data class LoginResponse(
    @SerialName("token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("user") val user: UserDto
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("role") val role: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)
