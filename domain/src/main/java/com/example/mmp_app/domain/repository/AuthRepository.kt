package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.*

sealed class LoginResult {
    data class OtpRequired(val email: String) : LoginResult()
    data class Success(val loginResponse: LoginResponse) : LoginResult()
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<LoginResult>
    suspend fun verifyOtp(email: String, otp: String): Result<LoginResponse>
    suspend fun logout()
}
