package com.example.mmp_app.data.remote

import com.example.mmp_app.core.utils.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiServiceProvider: Provider<SettingsApiService>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("X-Retry-Count") != null) return null
        val refreshResponse = runBlocking { apiServiceProvider.get().refreshToken() }
        return if (refreshResponse.isSuccessful) {
            val newToken = refreshResponse.body()?.data?.token ?: return null
            tokenManager.saveToken(newToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .header("X-Retry-Count", "1")
                .build()
        } else null
    }
}
