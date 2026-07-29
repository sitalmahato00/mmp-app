package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ParentAssignmentDto
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParentAssignmentsState(
    val assignments: List<ParentAssignmentDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ParentAssignmentsViewModel @Inject constructor(
    private val repository: ParentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentChildId: Int = savedStateHandle["childId"] ?: 0
    
    private val _uiState = MutableStateFlow(ParentAssignmentsState())
    val uiState = _uiState.asStateFlow()

    init {
        if (currentChildId != 0) {
            loadAssignments()
        } else {
            // If no ID provided, try to load children and pick the first one
            loadFirstChildAssignments()
        }
    }

    fun setChildId(id: Int) {
        if (id != 0 && id != currentChildId) {
            currentChildId = id
            loadAssignments()
        }
    }

    private fun loadFirstChildAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getChildren().collect { result ->
                result.onSuccess { children ->
                    val firstChildId = children.firstOrNull()?.id ?: 0
                    if (firstChildId != 0) {
                        currentChildId = firstChildId
                        loadAssignments()
                    } else {
                        _uiState.update { it.copy(isLoading = false, assignments = emptyList()) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun loadAssignments() {
        if (currentChildId == 0) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getChildAssignments(currentChildId).collect { result ->
                result.onSuccess { assignments ->
                    _uiState.update { it.copy(assignments = assignments, isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun refresh() {
        if (currentChildId == 0) {
            loadFirstChildAssignments()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            repository.getChildAssignments(currentChildId).collect { result ->
                result.onSuccess { assignments ->
                    _uiState.update { it.copy(assignments = assignments, isRefreshing = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isRefreshing = false) }
                }
            }
        }
    }
}
