package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentMarksState(
    val children: List<ChildDetailDto> = emptyList(),
    val selectedChildId: Int? = null,
    val selectedChild: ChildDetailDto? = null,
    val summary: ParentMarksSummaryDto? = null,
    val marks: List<ParentMarkRecordDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSummaryLoading: Boolean = false,
    val isMarksLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentMarksViewModel @Inject constructor(
    private val repository: ParentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentMarksState())
    val uiState = _uiState.asStateFlow()

    init {
        val initialChildId = savedStateHandle.get<Int>("childId")
        _uiState.update { it.copy(selectedChildId = initialChildId) }
        loadChildren()
    }

    fun loadChildren() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getChildren().collect { result ->
                result.onSuccess { children ->
                    val selectedId = _uiState.value.selectedChildId ?: children.firstOrNull()?.id
                    val selectedChild = children.find { it.id == selectedId }
                    _uiState.update { it.copy(
                        children = children,
                        selectedChildId = selectedId,
                        selectedChild = selectedChild,
                        isLoading = false
                    ) }
                    
                    if (selectedId != null) {
                        loadMarksData(selectedId)
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun onChildSelected(childId: Int) {
        val selectedChild = _uiState.value.children.find { it.id == childId }
        _uiState.update { it.copy(selectedChildId = childId, selectedChild = selectedChild) }
        loadMarksData(childId)
    }

    fun loadMarksData(childId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSummaryLoading = true, isMarksLoading = true) }
            
            // Call both APIs together
            launch {
                repository.getChildMarksSummary(childId).collect { result ->
                    result.onSuccess { summary ->
                        _uiState.update { it.copy(summary = summary, isSummaryLoading = false) }
                    }.onFailure {
                        _uiState.update { it.copy(isSummaryLoading = false) }
                    }
                }
            }
            
            launch {
                repository.getChildMarks(childId).collect { result ->
                    result.onSuccess { marks ->
                        _uiState.update { it.copy(marks = marks, isMarksLoading = false) }
                    }.onFailure {
                        _uiState.update { it.copy(isMarksLoading = false) }
                    }
                }
            }
        }
    }

    fun refresh() {
        _uiState.value.selectedChildId?.let { loadMarksData(it) }
        loadChildren()
    }
}
