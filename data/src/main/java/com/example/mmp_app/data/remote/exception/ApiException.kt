package com.example.mmp_app.data.remote.exception

import com.example.mmp_app.domain.model.BaseResponse
import kotlinx.serialization.json.Json
import retrofit2.Response


sealed class ApiException : Exception() {
    data class NetworkException(val throwable: Throwable) : ApiException()
    data class ServerException(val code: Int, override val message: String) : ApiException()
    data class ValidationException(val errors: Map<String, List<String>>) : ApiException()
    object UnauthorizedException : ApiException()
    object ForbiddenException : ApiException()
    object NotFoundException : ApiException()
    object ConflictException : ApiException()
    object RateLimitException : ApiException()

    fun getUserFriendlyMessage(): String = when (this) {
        is NetworkException -> "Network error. Please check your connection."
        is UnauthorizedException -> "Session expired. Please login again."
        is ForbiddenException -> "You don't have permission to access this resource."
        is NotFoundException -> "Resource not found (404)."
        is RateLimitException -> "Too many requests. Please try again later."
        is ValidationException -> "Invalid input. Please check your data."
        is ServerException -> if (message.isNotBlank()) "Server error ($code): $message" else "Server error ($code)"
        is ConflictException -> "Data conflict occurred."
    }
}

suspend inline fun <reified T> handleApiResponse(
    response: Response<BaseResponse<T>>,
    json: Json = Json { ignoreUnknownKeys = true }
): T {
    return when {
        !response.isSuccessful -> {
            when (response.code()) {
                401 -> throw ApiException.UnauthorizedException
                403 -> throw ApiException.ForbiddenException
                404 -> throw ApiException.NotFoundException
                409 -> throw ApiException.ConflictException
                429 -> throw ApiException.RateLimitException
                422 -> {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = errorBody?.let { 
                        try {
                            json.decodeFromString<BaseResponse<T>>(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    throw ApiException.ValidationException(errorResponse?.errors ?: emptyMap())
                }
                else -> throw ApiException.ServerException(
                    response.code(),
                    response.message()
                )
            }
        }
        response.body() == null -> throw ApiException.ServerException(500, "Empty response body")
        !response.body()!!.success -> throw ApiException.ServerException(
            response.code(),
            response.body()!!.message ?: "Request failed"
        )
        else -> response.body()!!.data ?: throw ApiException.ServerException(500, "No data in response")
    }
}

suspend inline fun <reified T> handleRawResponse(
    response: Response<T>
): T {
    if (!response.isSuccessful) {
        when (response.code()) {
            401 -> throw ApiException.UnauthorizedException
            403 -> throw ApiException.ForbiddenException
            404 -> throw ApiException.NotFoundException
            409 -> throw ApiException.ConflictException
            429 -> throw ApiException.RateLimitException
            else -> throw ApiException.ServerException(response.code(), response.message())
        }
    }
    return response.body() ?: throw ApiException.ServerException(500, "Empty response body")
}
