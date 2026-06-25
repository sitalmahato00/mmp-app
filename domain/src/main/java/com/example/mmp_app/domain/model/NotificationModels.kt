package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("title") val title: String,
    @SerialName("body") val body: String,
    @SerialName("action_url") val actionUrl: String? = null,
    @SerialName("icon") val icon: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class NotificationPagination(
    @SerialName("total") val total: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("has_more") val hasMore: Boolean
)

@Serializable
data class NotificationListData(
    @SerialName("notifications") val notifications: List<NotificationItem>,
    @SerialName("unread_count") val unreadCount: Int,
    @SerialName("pagination") val pagination: NotificationPagination
)

@Serializable
data class UnreadCountData(
    @SerialName("unread_count") val unreadCount: Int
)

@Serializable
data class MarkReadData(
    @SerialName("notification") val notification: NotificationItem
)

data class NotificationUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentPage: Int = 1,
    val filter: String = "all", // "all" | "unread"
    val error: String? = null
)
