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

    private val _teacherSchedule = MutableStateFlow<TodayScheduleDto?>(null)
    val teacherSchedule = _teacherSchedule.asStateFlow()

    private val _teacherClasses = MutableStateFlow<List<TeacherSubjectDto>>(emptyList())
    val teacherClasses = _teacherClasses.asStateFlow()

    private val _markComponents = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val markComponents = _markComponents.asStateFlow()

    private val _subjectStudents = MutableStateFlow<Map<Int, List<StudentItemDto>>>(emptyMap())
    val subjectStudents = _subjectStudents.asStateFlow()

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
                result.onSuccess { _teacherDashboard.value = it }.onFailure { _error.value = it.message }
            }
            _isLoading.value = false
        }
    }

    fun loadTodaySchedule() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTeacherTodaySchedule().collect { result ->
                result.onSuccess { _teacherSchedule.value = it }.onFailure { _error.value = it.message }
            }
            _isLoading.value = false
        }
    }

    fun loadTeacherClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTeacherClasses().collect { result ->
                result.onSuccess { 
                    _teacherClasses.value = it
                    it.forEach { subject ->
                        loadMarkComponents(subject.id)
                        loadSubjectStudents(subject.id)
                    }
                }.onFailure { _error.value = it.message }
            }
            _isLoading.value = false
        }
    }

    private fun loadMarkComponents(subjectId: Int) {
        viewModelScope.launch {
            repository.getMarkComponents(subjectId).collect { result ->
                result.onSuccess {
                    val currentMap = _markComponents.value.toMutableMap()
                    currentMap[subjectId] = it.components
                    _markComponents.value = currentMap
                }
            }
        }
    }

    private fun loadSubjectStudents(subjectId: Int) {
        viewModelScope.launch {
            repository.getTeacherStudentsBySubject(subjectId).collect { result ->
                result.onSuccess {
                    val currentMap = _subjectStudents.value.toMutableMap()
                    currentMap[subjectId] = it.students
                    _subjectStudents.value = currentMap
                }
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
