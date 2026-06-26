package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ParentNoticeDto
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
    val error: String? = null
)

@HiltViewModel
class ParentNoticesViewModel @Inject constructor(
    private val repository: ParentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentNoticesState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotices()
    }

    fun loadNotices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getNotices().collect { result ->
                result.onSuccess { data ->
                    _uiState.update { it.copy(notices = data, isLoading = false) }
                    applyFilter(_uiState.value.currentFilter)
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
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
