package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.MarkReadData
import com.example.mmp_app.domain.model.NotificationListData
import com.example.mmp_app.domain.model.UnreadCountData
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(filter: String, page: Int, perPage: Int): Flow<Result<NotificationListData>>
    fun getUnreadCount(): Flow<Result<UnreadCountData>>
    suspend fun markAllRead(): Result<UnreadCountData>
    suspend fun markRead(id: String): Result<MarkReadData>
    suspend fun deleteNotification(id: String): Result<Unit>
}
