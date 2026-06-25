package com.example.mmp_app.data.repository

import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.exception.handleApiResponse
import com.example.mmp_app.domain.model.MarkReadData
import com.example.mmp_app.domain.model.NotificationListData
import com.example.mmp_app.domain.model.UnreadCountData
import com.example.mmp_app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val json: Json
) : NotificationRepository {

    override fun getNotifications(filter: String, page: Int, perPage: Int): Flow<Result<NotificationListData>> = flow {
        try {
            val response = apiService.getNotifications(filter, perPage, page)
            val result = handleApiResponse(response, json)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getUnreadCount(): Flow<Result<UnreadCountData>> = flow {
        try {
            val response = apiService.getUnreadCount()
            val result = handleApiResponse(response, json)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun markAllRead(): Result<UnreadCountData> {
        return try {
            val response = apiService.markAllRead()
            val result = handleApiResponse(response, json)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markRead(id: String): Result<MarkReadData> {
        return try {
            val response = apiService.markRead(id)
            val result = handleApiResponse(response, json)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteNotification(id)
            handleApiResponse(response, json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
