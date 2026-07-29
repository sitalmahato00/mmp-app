package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ChildDetailDto
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildrenListState(
    val children: List<ChildDetailDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildrenListViewModel @Inject constructor(
    private val repository: ParentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildrenListState())
    val uiState = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    fun loadChildren() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getChildren().collect { result ->
                result.onSuccess { children ->
                    _uiState.update { it.copy(children = children, isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }
}
