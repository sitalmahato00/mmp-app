package com.example.mmp_app.core.network

import com.example.mmp_app.core.utils.SessionManager
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only attempt to refresh if we have a token
        val currentToken = sessionManager.getAuthToken() ?: return null

        synchronized(this) {
            val newToken = sessionManager.getAuthToken()
            
            // If token was already updated by another thread, use it
            if (newToken != currentToken && newToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }

            // Perform refresh
            val refreshedToken = refreshToken()
            if (refreshedToken != null) {
                sessionManager.saveAuthToken(refreshedToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $refreshedToken")
                    .build()
            }
        }

        return null
    }

    private fun refreshToken(): String? {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

        val token = sessionManager.getAuthToken() ?: return null

        val request = Request.Builder()
            .url("https://mmp.sital.info.np/api/auth/refresh-token")
            .post("".toRequestBody(null))
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    if (json.getBoolean("success")) {
                        json.getJSONObject("data").getString("token")
                    } else {
                        null
                    }
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
