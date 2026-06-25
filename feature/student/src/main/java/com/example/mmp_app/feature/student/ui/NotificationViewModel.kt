package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.NotificationItem
import com.example.mmp_app.domain.model.NotificationUiState
import com.example.mmp_app.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        loadNotifications(refresh = true)
        startPolling()
    }

    fun loadNotifications(filter: String = _uiState.value.filter, refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _uiState.update { it.copy(isLoading = true, currentPage = 1, filter = filter, notifications = emptyList()) }
            } else if (_uiState.value.isLoading || !_uiState.value.hasMore) {
                return@launch
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            repository.getNotifications(filter, _uiState.value.currentPage, 15).collect { result ->
                result.onSuccess { data ->
                    _uiState.update { state ->
                        state.copy(
                            notifications = if (refresh) data.notifications else state.notifications + data.notifications,
                            unreadCount = data.unreadCount,
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = data.pagination.hasMore,
                            currentPage = data.pagination.currentPage + 1,
                            error = null
                        )
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = e.message) }
                }
            }
        }
    }

    fun loadMore() {
        loadNotifications(refresh = false)
    }

    fun markRead(id: String) {
        val originalState = _uiState.value
        // Optimistic update
        _uiState.update { state ->
            val updatedList = state.notifications.map { 
                if (it.id == id && !it.isRead) it.copy(isRead = true) else it 
            }
            val wasUnread = state.notifications.find { it.id == id }?.isRead == false
            state.copy(
                notifications = updatedList,
                unreadCount = if (wasUnread) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount
            )
        }

        viewModelScope.launch {
            repository.markRead(id).onFailure {
                // Revert on failure
                _uiState.value = originalState
            }
        }
    }

    fun markAllRead() {
        val originalState = _uiState.value
        // Optimistic update
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) },
                unreadCount = 0
            )
        }

        viewModelScope.launch {
            repository.markAllRead().onFailure {
                // Revert on failure
                _uiState.value = originalState
            }
        }
    }

    fun deleteNotification(id: String) {
        val originalState = _uiState.value
        // Optimistic update
        _uiState.update { state ->
            val itemToDelete = state.notifications.find { it.id == id }
            state.copy(
                notifications = state.notifications.filter { it.id != id },
                unreadCount = if (itemToDelete?.isRead == false) (state.unreadCount - 1).coerceAtLeast(0) else state.unreadCount
            )
        }

        viewModelScope.launch {
            repository.deleteNotification(id).onFailure {
                // Revert on failure
                _uiState.value = originalState
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(60000) // 60 seconds
                repository.getUnreadCount().collect { result ->
                    result.onSuccess { data ->
                        _uiState.update { it.copy(unreadCount = data.unreadCount) }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
