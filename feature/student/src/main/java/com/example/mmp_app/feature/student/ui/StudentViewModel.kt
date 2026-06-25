package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import com.example.mmp_app.core.utils.SessionManager
import com.example.mmp_app.data.remote.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
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

    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    private val persistentCookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(persistentCookieJar)
        .followRedirects(true)
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _assignments = MutableStateFlow<List<AssignmentDto>>(emptyList())
    val assignments = _assignments.asStateFlow()

    private val _assignmentDetail = MutableStateFlow<AssignmentDetailDto?>(null)
    val assignmentDetail = _assignmentDetail.asStateFlow()

    private val _submissionStatus = MutableStateFlow<SubmissionStatusDto?>(null)
    val submissionStatus = _submissionStatus.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceDto>>(emptyList())
    val attendance = _attendance.asStateFlow()

    private val _attendanceSummary = MutableStateFlow<AttendanceSummaryDto?>(null)
    val attendanceSummary = _attendanceSummary.asStateFlow()

    private val _attendanceBySubject = MutableStateFlow<AttendanceBySubjectDto?>(null)
    val attendanceBySubject = _attendanceBySubject.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectDto>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _fees = MutableStateFlow<FeesResponse?>(null)
    val fees = _fees.asStateFlow()

    private val _notices = MutableStateFlow<List<NoticeDto>>(emptyList())
    val notices = _notices.asStateFlow()

    private val _noticeDetail = MutableStateFlow<NoticeDetailDto?>(null)
    val noticeDetail = _noticeDetail.asStateFlow()

    private val _downloads = MutableStateFlow<List<SubjectDocument>>(emptyList())
    val downloads = _downloads.asStateFlow()

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

    fun downloadMarksheet(examId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMarksheet(examId).collect { result ->
                _isLoading.value = false
                result.onSuccess { 
                    // Ensure URL is absolute
                    val finalUrl = if (it.downloadUrl.startsWith("http")) {
                        it.downloadUrl
                    } else {
                        "https://mmp.sital.info.np${if (it.downloadUrl.startsWith("/")) "" else "/"}${it.downloadUrl}"
                    }
                    _marksheet.value = it.copy(downloadUrl = finalUrl)
                    
                    // If we don't have a session cookie yet, pre-fetch it
                    if (_webSessionCookie.value == null) {
                        performWebLogin()
                    }
                }.onFailure { it ->
                    val errorMessage = if (it is ApiException) {
                        it.getUserFriendlyMessage()
                    } else {
                        it.message ?: "Unknown error"
                    }
                    _error.value = "Failed to get marksheet info: $errorMessage"
                }
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

    fun loadAssignmentDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _assignmentDetail.value = null
            repository.getAssignmentDetail(id).collect { result ->
                _isLoading.value = false
                result.onSuccess { _assignmentDetail.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadSubmissionStatus(submissionId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _submissionStatus.value = null
            repository.getSubmissionStatus(submissionId).collect { result ->
                _isLoading.value = false
                result.onSuccess { _submissionStatus.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun submitAssignment(id: Int, content: String?, filePart: Any? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = if (filePart != null) {
                repository.submitAssignmentWithFile(id, content, filePart)
            } else {
                repository.submitAssignment(id, content)
            }
            _isLoading.value = false
            result.onSuccess {
                loadStudentAssignments() // Refresh list
                _error.value = "Assignment submitted successfully" // Using error flow for toast/message for now
            }.onFailure { e ->
                if (e.toString().contains("Conflict") || e.message?.contains("409") == true) {
                    _error.value = "Already submitted"
                } else {
                    _error.value = e.message ?: "Submission failed"
                }
            }
        }
    }

    fun clearAssignmentDetail() {
        _assignmentDetail.value = null
        _submissionStatus.value = null
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

    fun loadStudentFees() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentFees().collect { result ->
                _isLoading.value = false
                result.onSuccess { _fees.value = it }.onFailure { _error.value = it.message }
            }
        }
    }

    fun loadStudentNotices(type: String = "all", page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            val flow = if (type == "all") {
                repository.getStudentNotices(page)
            } else {
                repository.getNoticesByType(type, page)
            }
            
            flow.collect { result ->
                _isLoading.value = false
                result.onSuccess { _notices.value = it }
                    .onFailure { _error.value = it.message }
            }
        }
    }

    fun loadNoticeDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _noticeDetail.value = null
            repository.getNoticeDetail(id).collect { result ->
                _isLoading.value = false
                result.onSuccess { _noticeDetail.value = it }
                    .onFailure { _error.value = it.message }
            }
        }
    }

    fun clearNoticeDetail() {
        _noticeDetail.value = null
    }

    fun loadStudentDownloads(subjectId: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getStudentDownloads(subjectId).collect { result ->
                _isLoading.value = false
                result.onSuccess { _downloads.value = it }
                    .onFailure { _error.value = it.message }
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
        
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            _error.value = "Credentials missing. Please log out and log in again."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val csrfToken = fetchCsrfToken()
                if (csrfToken.isEmpty()) {
                    _error.value = "Security check failed. Try again later."
                    return@launch
                }

                val loggedIn = webLogin(csrfToken, email, password)
                
                if (loggedIn) {
                    val host = "mmp.sital.info.np"
                    val httpUrl = "https://$host".toHttpUrl()
                    val cookies = persistentCookieJar.loadForRequest(httpUrl)
                    
                    if (cookies.isEmpty()) {
                        _error.value = "Login successful but session cookie missing."
                        return@launch
                    }

                    // Format cookies for WebView injection: name=value; domain=...; path=...
                    val sessionCookie = cookies.joinToString("||") { cookie ->
                        val cookieString = buildString {
                            append("${cookie.name}=${cookie.value}")
                            append("; domain=${cookie.domain}")
                            append("; path=${cookie.path}")
                            if (cookie.secure) append("; secure")
                        }
                        cookieString
                    }
                    _webSessionCookie.value = sessionCookie
                } else {
                    _error.value = "Web login failed. Please verify credentials."
                }
            } catch (e: Exception) {
                _error.value = "Portal login error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchCsrfToken(): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://mmp.sital.info.np/login")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36")
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext ""
            
            val html = response.body?.string() ?: ""
            // Extract _token from: <input name="_token" value="XXXXX">
            val regex = Regex("""<input[^>]*name="_token"[^>]*value="([^"]+)"""")
            val altRegex = Regex("""<input[^>]*value="([^"]+)"[^>]*name="_token"""")
            
            val match = regex.find(html) ?: altRegex.find(html)
            match?.groupValues?.get(1) ?: ""
        }
    }

    private suspend fun webLogin(csrfToken: String, email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            val formBody = FormBody.Builder()
                .add("_token", csrfToken)
                .add("email", email)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url("https://mmp.sital.info.np/login")
                .header("Referer", "https://mmp.sital.info.np/login")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val finalUrl = response.request.url.toString()
            
            // Success = redirected away from /login
            !finalUrl.contains("/login")
        }
    }
}
