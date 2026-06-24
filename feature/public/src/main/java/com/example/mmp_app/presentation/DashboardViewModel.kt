package com.example.mmp_app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
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

    private val _marks = MutableStateFlow<List<MarkDto>>(emptyList())
    val marks = _marks.asStateFlow()

    private val _marksSummary = MutableStateFlow<MarksSummaryDto?>(null)
    val marksSummary = _marksSummary.asStateFlow()

    private val _examDetail = MutableStateFlow<ExamDetailDto?>(null)
    val examDetail = _examDetail.asStateFlow()

    private val _subjectMarks = MutableStateFlow<SubjectMarkDto?>(null)
    val subjectMarks = _subjectMarks.asStateFlow()

    private val _marksheet = MutableStateFlow<MarksheetDto?>(null)
    val marksheet = _marksheet.asStateFlow()

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

    private val _timetable = MutableStateFlow<List<ClassDto>>(emptyList())
    val timetable = _timetable.asStateFlow()

    private val _notices = MutableStateFlow<List<NoticeDto>>(emptyList())
    val notices = _notices.asStateFlow()

    private val _downloads = MutableStateFlow<List<SubjectDocument>>(emptyList())
    val downloads = _downloads.asStateFlow()

    private val _classStudents = MutableStateFlow<List<UserDto>>(emptyList())
    val classStudents = _classStudents.asStateFlow()

    private val _childDashboard = MutableStateFlow<StudentDashboardDto?>(null)
    val childDashboard = _childDashboard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun loadStudentDashboardData() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            
            // We run these in parallel
            val jobs = listOf(
                launch { fetchStudentDashboard() },
                launch { fetchStudentNotices() },
                launch { fetchStudentAttendance() },
                launch { fetchStudentSubjects() },
                launch { fetchStudentAssignments() },
                launch { fetchStudentTimetable() },
                launch { fetchStudentDownloads() }
            )
            
            jobs.joinAll()
            _isLoading.value = false
        }
    }

    private suspend fun fetchStudentDashboard() {
        repository.getStudentDashboard().collect { result ->
            result.onSuccess { _studentDashboard.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    private suspend fun fetchStudentNotices() {
        repository.getStudentNotices().collect { result ->
            result.onSuccess { _notices.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    private suspend fun fetchStudentAttendance() {
        repository.getStudentAttendanceSummary().collect { result ->
            result.onSuccess { _attendanceSummary.value = it }
                .onFailure { _error.value = it.message }
        }
        repository.getStudentAttendance().collect { result ->
            result.onSuccess { _attendance.value = it }
        }
    }

    private suspend fun fetchStudentSubjects() {
        repository.getStudentSubjects().collect { result ->
            result.onSuccess { _subjects.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    private suspend fun fetchStudentAssignments() {
        repository.getStudentAssignments().collect { result ->
            result.onSuccess { _assignments.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    private suspend fun fetchStudentTimetable() {
        repository.getStudentTimetable().collect { result ->
            result.onSuccess { _timetable.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    private suspend fun fetchStudentDownloads() {
        repository.getStudentDownloads().collect { result ->
            result.onSuccess { _downloads.value = it }
        }
    }

    fun loadStudentDashboard() {
        viewModelScope.launch {
            _error.value = null
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
            _error.value = null
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
            _error.value = null
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

    fun loadStudentMarks() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getStudentMarksSummary().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _marksSummary.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadMarksByExam(examId: Int) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getMarksByExam(examId).collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _examDetail.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadMarksBySubject(subjectId: Int) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getMarksBySubject(subjectId).collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _subjectMarks.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun downloadMarksheet() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getMarksheet().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _marksheet.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadStudentAssignments() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getStudentAssignments().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _assignments.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadStudentAttendance() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getStudentAttendance().collect { result ->
                result.onSuccess {
                    _attendance.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
            repository.getStudentAttendanceSummary().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _attendanceSummary.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadStudentSubjects() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getStudentSubjects().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _subjects.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadStudentTimetable() {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getStudentTimetable().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _timetable.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadAttendanceBySubject(subjectId: Int) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            repository.getStudentAttendanceBySubject(subjectId).collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _attendanceBySubject.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadStudentNotices() {
        viewModelScope.launch {
            // Not clearing error here because it often runs in parallel with dashboard
            _isLoading.value = true
            repository.getStudentNotices().collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _notices.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun loadClassStudents(classId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getClassStudents(classId)
            _isLoading.value = false
            result.onSuccess {
                _classStudents.value = it
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun loadChildDashboard(childId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getChildDashboard(childId).collect { result ->
                _isLoading.value = false
                result.onSuccess {
                    _childDashboard.value = it
                }.onFailure {
                    _error.value = it.message
                }
            }
        }
    }

    fun recordAttendance(request: AttendanceRecordRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.recordAttendance(request)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun recordMarks(request: MarkRecordRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.recordMarks(request)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _error.value = it.message
            }
        }
    }
}
