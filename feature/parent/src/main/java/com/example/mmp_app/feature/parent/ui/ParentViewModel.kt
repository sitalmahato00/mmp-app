package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _parentDashboard = MutableStateFlow<ParentDashboardDto?>(null)
    val parentDashboard = _parentDashboard.asStateFlow()

    private val _childDashboard = MutableStateFlow<StudentDashboardDto?>(null)
    val childDashboard = _childDashboard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadParentDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getParentDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess { _parentDashboard.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadChildDashboard(childId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getChildDashboard(childId).collect { result ->
                _isLoading.value = false
                result.onSuccess { _childDashboard.value = it }.onFailure { _error.value = it.message }
            }
        }
    }
}
