package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.local.entity.UserProfileEntity
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.AuthRepository
import com.example.mmp_app.domain.repository.LoginResult
import com.example.mmp_app.core.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val userProfileDao: UserProfileDao,
    private val sessionManager: SessionManager,
    private val json: Json
) : AuthRepository {
    override fun getUserProfile(): Flow<UserProfile?> = userProfileDao.getUserProfile().map { entity ->
        entity?.let {
            UserProfile(it.id, it.name, it.email, it.role, it.avatarUrl)
        }
    }

    override suspend fun login(email: String, password: String): Result<LoginResult> {

        return try {
            val response = apiService.login(LoginRequest(email, password))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.success) {
                    if (body.requires2fa == true) {
                        Result.success(LoginResult.OtpRequired(email))
                    } else {
                        val loginData = json.decodeFromJsonElement<LoginResponse>(body.data!!)
                        saveUserSession(loginData)
                        sessionManager.saveCredentials(email, password)
                        Result.success(LoginResult.Success(loginData))
                    }
                } else {
                    Result.failure(Exception(body.message ?: "Login failed"))
                }
            } else {
                val errorMsg = parseError(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveUserSession(loginData: LoginResponse) {
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
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<LoginResponse> {
        return try {
            val response: Response<BaseResponse<LoginResponse>> = apiService.verifyOtp(OtpVerifyRequest(email, otp))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()!!.data!!
                saveUserSession(loginData)
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
                val errors = baseResponse.errors
                if (!errors.isNullOrEmpty()) {
                    errors.values.flatten().joinToString("\n")
                } else {
                    baseResponse.message ?: "An unknown error occurred"
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
