package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ParentDashboardDto
import com.example.mmp_app.domain.model.ParentNoticeDto
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val repository: ParentRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val dashboard: ParentDashboardDto? = null,
        val recentNotices: List<ParentNoticeDto> = emptyList(),
        val isRefreshing: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load dashboard and notices in parallel
            val dashDeferred = async { repository.getDashboard().first() }
            val noticesDeferred = async { repository.getNotices().first() }

            val dashResult = dashDeferred.await()
            val noticesResult = noticesDeferred.await()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    dashboard = dashResult.getOrNull(),
                    recentNotices = (noticesResult.getOrNull() ?: emptyList()).take(3),
                    error = dashResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }
}
