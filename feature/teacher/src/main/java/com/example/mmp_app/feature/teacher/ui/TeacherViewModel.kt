package com.example.mmp_app.feature.teacher.ui

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
class TeacherViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _teacherDashboard = MutableStateFlow<TeacherDashboardDto?>(null)
    val teacherDashboard = _teacherDashboard.asStateFlow()

    private val _classStudents = MutableStateFlow<List<UserDto>>(emptyList())
    val classStudents = _classStudents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadTeacherDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTeacherDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess { _teacherDashboard.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadClassStudents(classId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getClassStudents(classId)
            _isLoading.value = false
            result.onSuccess { _classStudents.value = it }.onFailure { _error.value = it.message }
        }
    }

    fun recordAttendance(request: AttendanceRecordRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.recordAttendance(request)
            _isLoading.value = false
            result.onSuccess { onSuccess() }.onFailure { _error.value = it.message }
        }
    }

    fun recordMarks(request: MarkRecordRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.recordMarks(request)
            _isLoading.value = false
            result.onSuccess { onSuccess() }.onFailure { _error.value = it.message }
        }
    }
}
