package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ParentNoticeDto
import com.example.mmp_app.domain.repository.DashboardRepository
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentNoticesState(
    val notices: List<ParentNoticeDto> = emptyList(),
    val filteredNotices: List<ParentNoticeDto> = emptyList(),
    val currentFilter: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNotice: ParentNoticeDto? = null
)

@HiltViewModel
class ParentNoticesViewModel @Inject constructor(
    private val repository: ParentRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentNoticesState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotices()
    }

    fun loadNotices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Load public notices (student notices)
            dashboardRepository.getStudentNotices().collect { result ->
                result.onSuccess { data ->
                    val parentNotices = data.map {
                        ParentNoticeDto(
                            id = it.id,
                            title = it.title,
                            type = it.type ?: "General",
                            publishedAt = it.publishedAt,
                            content = it.content
                        )
                    }
                    _uiState.update { it.copy(notices = parentNotices, isLoading = false) }
                    applyFilter(_uiState.value.currentFilter)
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun loadNoticeDetail(noticeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getNoticeDetail(noticeId).collect { result ->
                result.onSuccess { detail ->
                    _uiState.update { it.copy(selectedNotice = detail, isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun clearSelectedNotice() {
        _uiState.update { it.copy(selectedNotice = null) }
    }

    fun applyFilter(filter: String) {
        val filtered = if (filter == "All") {
            _uiState.value.notices
        } else {
            _uiState.value.notices.filter { it.type.equals(filter, ignoreCase = true) }
        }
        _uiState.update { it.copy(currentFilter = filter, filteredNotices = filtered) }
    }
}
