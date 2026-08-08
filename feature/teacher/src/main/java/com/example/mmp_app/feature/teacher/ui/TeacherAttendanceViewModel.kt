package com.example.mmp_app.feature.teacher.ui

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.core.utils.NepaliDateUtils
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherAttendanceViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<HistoryResponse>>(UiState.Loading)
    val historyState = _historyState.asStateFlow()

    private val _subjects = MutableStateFlow<List<TeacherSubjectDto>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _selectedBsDate = MutableStateFlow(NepaliDateUtils.getTodayBs())
    val selectedBsDate = _selectedBsDate.asStateFlow()

    private val _selectedAdDate = MutableStateFlow(NepaliDateUtils.bsToAd(NepaliDateUtils.getTodayBs()))
    val selectedAdDate = _selectedAdDate.asStateFlow()

    private val _sessionState = MutableStateFlow<UiState<SessionData>>(UiState.Loading)
    val sessionState = _sessionState.asStateFlow()

    private val _studentsState = MutableStateFlow<UiState<List<StudentItemDto>>>(UiState.Loading)
    val studentsState = _studentsState.asStateFlow()

    val attendanceMap = mutableStateMapOf<Int, AttendanceRecord>()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        loadHistory()
        loadSubjects()
    }

    fun onBsDateSelected(bsDate: String) {
        _selectedBsDate.value = bsDate
        _selectedAdDate.value = NepaliDateUtils.bsToAd(bsDate)
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            repository.getAttendanceHistory().collect { result ->
                result.onSuccess { _historyState.value = UiState.Success(it) }
                    .onFailure { _historyState.value = UiState.Error(it.message ?: "Failed to load history") }
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            repository.getTeacherClasses().collect { result ->
                result.onSuccess { _subjects.value = it }
            }
        }
    }

    fun startSession(subjectId: Int, period: String?, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val request = StartSessionRequest(subjectId, _selectedAdDate.value, period)
            val result = repository.startAttendanceSession(request)
            result.onSuccess { response ->
                val session = response.data
                if (session != null) {
                    _sessionState.value = UiState.Success(session)
                    
                    // Pre-fill attendance map if existing
                    if (session.isExisting) {
                        session.existingAttendance.forEach { 
                            attendanceMap[it.studentId] = AttendanceRecord(it.studentId, it.status, it.remarks)
                        }
                    }
                    
                    // Load students
                    loadStudents(subjectId)
                    onSuccess(session.sessionId)
                } else {
                    onError(response.message ?: "Failed to start session")
                }
            }.onFailure {
                onError(it.message ?: "Failed to start session")
            }
        }
    }

    private fun loadStudents(subjectId: Int) {
        viewModelScope.launch {
            _studentsState.value = UiState.Loading
            repository.getTeacherStudentsBySubject(subjectId).collect { result ->
                result.onSuccess { 
                    _studentsState.value = UiState.Success(it.students.map { s ->
                        StudentItemDto(s.id, s.name, avatarUrl = s.avatarUrl, studentNo = s.studentNo, rollNumber = s.rollNumber, section = s.section)
                    }) 
                }.onFailure { 
                    _studentsState.value = UiState.Error(it.message ?: "Failed to load students") 
                }
            }
        }
    }

    fun markStudent(sessionId: Int, studentId: Int, status: String, remarks: String?) {
        attendanceMap[studentId] = AttendanceRecord(studentId, status, remarks)
        viewModelScope.launch {
            repository.markSingleAttendance(SingleMarkRequest(sessionId, studentId, status, remarks))
        }
    }

    fun markAll(status: String) {
        val currentState = _studentsState.value
        if (currentState is UiState.Success) {
            val sessionState = _sessionState.value
            if (sessionState is UiState.Success) {
                currentState.data.forEach { student ->
                    markStudent(sessionState.data.sessionId, student.id, status, null)
                }
            }
        }
    }

    fun saveAllAndFinish(sessionId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val records = attendanceMap.values.toList()
            val result = repository.bulkMarkAttendance(BulkMarkRequest(sessionId, records))
            _isSaving.value = false
            result.onSuccess { 
                loadHistory() // Refresh history
                onSuccess() 
            }.onFailure { 
                onError(it.message ?: "Failed to save attendance") 
            }
        }
    }

    // Session Detail
    private val _sessionDetailState = MutableStateFlow<UiState<SessionDetailData>>(UiState.Loading)
    val sessionDetailState = _sessionDetailState.asStateFlow()

    fun loadSessionDetail(sessionId: Int) {
        viewModelScope.launch {
            _sessionDetailState.value = UiState.Loading
            repository.getAttendanceSession(sessionId).collect { result ->
                result.onSuccess { 
                    val data = it.data
                    if (data != null) {
                        _sessionDetailState.value = UiState.Success(data)
                    } else {
                        _sessionDetailState.value = UiState.Error(it.message ?: "Session not found")
                    }
                }.onFailure { 
                    _sessionDetailState.value = UiState.Error(it.message ?: "Failed to load session detail") 
                }
            }
        }
    }
}
