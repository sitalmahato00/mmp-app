package com.example.mmp_app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.data.remote.model.HodDashboardDto
import com.example.mmp_app.data.remote.model.ParentDashboardDto
import com.example.mmp_app.data.remote.model.StudentDashboardDto
import com.example.mmp_app.data.remote.model.TeacherDashboardDto
import com.example.mmp_app.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _studentDashboard = MutableStateFlow<StudentDashboardDto?>(null)
    val studentDashboard = _studentDashboard.asStateFlow()

    private val _teacherDashboard = MutableStateFlow<TeacherDashboardDto?>(null)
    val teacherDashboard = _teacherDashboard.asStateFlow()

    private val _parentDashboard = MutableStateFlow<ParentDashboardDto?>(null)
    val parentDashboard = _parentDashboard.asStateFlow()

    private val _hodDashboard = MutableStateFlow<HodDashboardDto?>(null)
    val hodDashboard = _hodDashboard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadStudentDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _studentDashboard.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadTeacherDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTeacherDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _teacherDashboard.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadParentDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getParentDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _parentDashboard.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadHodDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getHodDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _hodDashboard.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }
}
