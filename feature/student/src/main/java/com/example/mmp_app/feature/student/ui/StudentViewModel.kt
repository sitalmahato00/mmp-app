package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _studentDashboard = MutableStateFlow<StudentDashboardDto?>(null)
    val studentDashboard = _studentDashboard.asStateFlow()

    private val _marksSummary = MutableStateFlow<MarksSummaryDto?>(null)
    val marksSummary = _marksSummary.asStateFlow()

    private val _assignments = MutableStateFlow<List<AssignmentDto>>(emptyList())
    val assignments = _assignments.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceDto>>(emptyList())
    val attendance = _attendance.asStateFlow()

    private val _attendanceSummary = MutableStateFlow<AttendanceSummaryDto?>(null)
    val attendanceSummary = _attendanceSummary.asStateFlow()

    private val _attendanceBySubject = MutableStateFlow<AttendanceBySubjectDto?>(null)
    val attendanceBySubject = _attendanceBySubject.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectDto>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadStudentDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess { _studentDashboard.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadStudentAttendance() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentAttendance().collect { result ->
                result.onSuccess { _attendance.value = it }.onFailure { _error.value = it.message }
            }
            repository.getStudentAttendanceSummary().collect { result ->
                _isLoading.value = false
                result.onSuccess { _attendanceSummary.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadStudentAssignments() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentAssignments().collect { result ->
                _isLoading.value = false
                result.onSuccess { _assignments.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadStudentSubjects() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentSubjects().collect { result ->
                _isLoading.value = false
                result.onSuccess { _subjects.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadAttendanceBySubject(subjectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentAttendanceBySubject(subjectId).collect { result ->
                _isLoading.value = false
                result.onSuccess { _attendanceBySubject.value = it }.onFailure { _error.value = it.message }
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
