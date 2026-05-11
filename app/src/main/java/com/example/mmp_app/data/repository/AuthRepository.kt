package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.local.entity.UserProfileEntity
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.model.*
import com.example.mmp_app.utils.SessionManager
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<OtpResponse>
    suspend fun verifyOtp(email: String, otp: String): Result<LoginResponse>
    suspend fun logout()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val userProfileDao: UserProfileDao,
    private val sessionManager: SessionManager,
    private val json: Json
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<OtpResponse> {
        return try {
            val response: Response<BaseResponse<OtpResponse>> = apiService.login(LoginRequest(email, password))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.success || body.requires2fa == true) {
                    Result.success(body.data ?: OtpResponse(otpSent = true))
                } else {
                    Result.failure(Exception(body.message))
                }
            } else {
                val errorMsg = parseError(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<LoginResponse> {
        return try {
            val response: Response<BaseResponse<LoginResponse>> = apiService.verifyOtp(OtpVerifyRequest(email, otp))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()!!.data!!
                sessionManager.saveAuthToken(loginData.accessToken)
                
                val user = loginData.user
                userProfileDao.insertProfile(
                    UserProfileEntity(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role,
                        avatarUrl = user.avatarUrl
                    )
                )
                Result.success(loginData)
            } else {
                val errorMsg = parseError(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val baseResponse = json.decodeFromString<BaseResponse<Unit>>(errorBody)
                
                // If there are detailed errors, join them into a single string
                if (!baseResponse.errors.isNullOrEmpty()) {
                    baseResponse.errors.values.flatten().joinToString("\n")
                } else {
                    baseResponse.message
                }
            } else {
                "An unknown error occurred (Code: ${response.code()})"
            }
        } catch (e: Exception) {
            "Error: ${response.code()} ${response.message()}"
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
        userProfileDao.clearProfile()
    }
}
