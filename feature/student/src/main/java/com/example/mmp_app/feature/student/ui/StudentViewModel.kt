package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import com.example.mmp_app.core.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _studentDashboard = MutableStateFlow<StudentDashboardDto?>(null)
    val studentDashboard = _studentDashboard.asStateFlow()
    
    private val _webSessionCookie = MutableStateFlow<String?>(null)
    val webSessionCookie = _webSessionCookie.asStateFlow()

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadStudentMarks() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentMarksSummary().collect { result ->
                _isLoading.value = false
                result.onSuccess { _marksSummary.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadMarksByExam(examId: String) {
        viewModelScope.launch {
            // As per user requirement, we try to use cached data from summary first
            val cachedExam = _marksSummary.value?.exams?.find { it.examId == examId }
            if (cachedExam != null) {
                _examDetail.value = ExamDetailDto(
                    examId = cachedExam.examId,
                    examName = cachedExam.examName,
                    category = cachedExam.category,
                    startDate = cachedExam.startDate,
                    marks = cachedExam.subjects
                )
                return@launch
            }

            _isLoading.value = true
            repository.getMarksByExam(examId).collect { result ->
                _isLoading.value = false
                result.onSuccess { _examDetail.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadMarksBySubject(subjectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMarksBySubject(subjectId).collect { result ->
                _isLoading.value = false
                result.onSuccess { _subjectMarks.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun downloadMarksheet() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMarksheet().collect { result ->
                _isLoading.value = false
                result.onSuccess { _marksheet.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun clearMarksheetState() {
        _marksheet.value = null
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

    fun loadStudentDashboard() {

        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentDashboard().collect { result ->
                _isLoading.value = false
                result.onSuccess { _studentDashboard.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun performWebLogin() {
        val email = sessionManager.getUserEmail()
        val password = sessionManager.getUserPassword()
        
        if (email == null || password == null) {
            _error.value = "Credentials not found. Please re-login."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val client = OkHttpClient.Builder()
                    .followRedirects(false)
                    .build()

                // 1. GET login page to get CSRF token
                val getRequest = Request.Builder()
                    .url("https://mmp.sital.info.np/login")
                    .build()
                
                val getResponse = withContext(Dispatchers.IO) { client.newCall(getRequest).execute() }
                val getHtml = getResponse.body?.string() ?: ""
                
                val pattern = Pattern.compile("name=\"_token\" value=\"([^\"]+)\"")
                val matcher = pattern.matcher(getHtml)
                val csrfToken = if (matcher.find()) matcher.group(1) else ""
                
                // Get cookies from the first request
                val initialCookies = getResponse.headers("Set-Cookie")
                val cookieHeader = initialCookies.joinToString("; ") { it.split(";")[0] }

                // 2. POST login
                val formBody = FormBody.Builder()
                    .add("_token", csrfToken)
                    .add("email", email)
                    .add("password", password)
                    .build()

                val postRequest = Request.Builder()
                    .url("https://mmp.sital.info.np/login")
                    .header("Cookie", cookieHeader)
                    .post(formBody)
                    .build()

                val postResponse = withContext(Dispatchers.IO) { client.newCall(postRequest).execute() }
                
                // On success, Laravel should redirect (302) and set new session cookies
                val postCookies = postResponse.headers("Set-Cookie")
                if (postCookies.isNotEmpty()) {
                    // Capture all cookies (laravel_session, XSRF-TOKEN, etc.)
                    val sessionCookie = postCookies.joinToString("; ") { it.split(";")[0] }
                    _webSessionCookie.value = sessionCookie
                } else {
                    _error.value = "Failed to obtain web session."
                }
            } catch (e: Exception) {
                _error.value = "Web login error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
