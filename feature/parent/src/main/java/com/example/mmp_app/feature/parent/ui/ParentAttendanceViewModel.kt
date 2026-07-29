package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentAttendanceState(
    val children: List<ChildDetailDto> = emptyList(),
    val selectedChildId: Int = 0,
    val summary: ParentAttendanceSummaryDto? = null,
    val records: List<ParentAttendanceRecordDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSummaryLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentAttendanceViewModel @Inject constructor(
    private val repository: ParentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentAttendanceState())
    val uiState = _uiState.asStateFlow()

    init {
        val initialChildId = savedStateHandle.get<Int>("childId") ?: 0
        _uiState.update { it.copy(selectedChildId = initialChildId) }
        loadChildren(initialChildId)
    }

    private fun loadChildren(initialChildId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getChildren().collectLatest { result ->
                result.onSuccess { children ->
                    val selectedId = if (initialChildId != 0) initialChildId 
                                   else children.firstOrNull()?.id ?: 0
                    _uiState.update { it.copy(children = children, selectedChildId = selectedId) }
                    if (selectedId != 0) {
                        loadAttendance(selectedId)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }

    fun onChildSelected(childId: Int) {
        if (_uiState.value.selectedChildId == childId) return
        _uiState.update { it.copy(selectedChildId = childId) }
        loadAttendance(childId)
    }

    fun loadAttendance(childId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSummaryLoading = true, error = null) }
            
            // Load summary
            repository.getChildAttendanceSummary(childId).collectLatest { result ->
                result.onSuccess { summary ->
                    _uiState.update { it.copy(summary = summary) }
                }
            }

            // Load records
            repository.getChildAttendance(childId).collectLatest { result ->
                result.onSuccess { records ->
                    _uiState.update { it.copy(records = records.sortedByDescending { it.date }, isSummaryLoading = false, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSummaryLoading = false, isLoading = false, error = e.message) }
                }
            }
        }
    }

    fun refresh() {
        val currentId = _uiState.value.selectedChildId
        if (currentId != 0) {
            loadAttendance(currentId)
        } else {
            loadChildren(0)
        }
    }
}
