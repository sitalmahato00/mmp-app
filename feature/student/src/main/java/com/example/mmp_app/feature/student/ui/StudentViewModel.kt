package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import com.example.mmp_app.core.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
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
        if (!examId.isNullOrEmpty()) {
            // Construct URL directly for specific exams to avoid API dependency
            _marksheet.value = MarksheetDto(downloadUrl = "https://mmp.sital.info.np/student/marks/$examId")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.getMarksheet().collect { result ->
                _isLoading.value = false
                result.onSuccess { 
                    _marksheet.value = it
                }.onFailure { 
                    _error.value = "Failed to get marksheet info: ${it.message ?: "Unknown error"}"
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
                val cookieJar = object : CookieJar {
                    private val cookieStore = mutableMapOf<String, List<Cookie>>()
                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        cookieStore[url.host] = cookies
                    }
                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        return cookieStore[url.host] ?: emptyList()
                    }
                }

                val client = OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .followRedirects(true)
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                // 1. GET login page to get CSRF token
                val getRequest = Request.Builder()
                    .url("https://mmp.sital.info.np/login")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36")
                    .build()
                
                val getResponse = withContext(Dispatchers.IO) { client.newCall(getRequest).execute() }
                if (!getResponse.isSuccessful) {
                    _error.value = "Portal unreachable (${getResponse.code})"
                    return@launch
                }
                
                val getHtml = getResponse.body?.string() ?: ""
                
                // Extremely flexible CSRF regex
                val tokenPattern = Pattern.compile("name=\"_token\" value=\"([^\"]+)\"")
                val tokenPatternAlt = Pattern.compile("value=\"([^\"]+)\" name=\"_token\"")
                
                var csrfToken = ""
                val matcher = tokenPattern.matcher(getHtml)
                if (matcher.find()) {
                    csrfToken = matcher.group(1) ?: ""
                } else {
                    val matcherAlt = tokenPatternAlt.matcher(getHtml)
                    if (matcherAlt.find()) {
                        csrfToken = matcherAlt.group(1) ?: ""
                    }
                }
                
                if (csrfToken.isEmpty()) {
                    _error.value = "Security check failed. Try again later."
                    return@launch
                }

                // 2. POST login
                val formBody = FormBody.Builder()
                    .add("_token", csrfToken)
                    .add("email", email)
                    .add("password", password)
                    .build()

                val postRequest = Request.Builder()
                    .url("https://mmp.sital.info.np/login")
                    .header("Referer", "https://mmp.sital.info.np/login")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36")
                    .post(formBody)
                    .build()

                val postResponse = withContext(Dispatchers.IO) { client.newCall(postRequest).execute() }
                val finalUrl = postResponse.request.url.toString()
                
                if (!finalUrl.contains("/login")) {
                    val host = "mmp.sital.info.np"
                    val httpUrl = HttpUrl.Builder().scheme("https").host(host).build()
                    val cookies = cookieJar.loadForRequest(httpUrl)
                    
                    if (cookies.isEmpty()) {
                        _error.value = "Login successful but session cookie missing."
                        return@launch
                    }

                    val sessionCookie = cookies.joinToString("||") { cookie ->
                        "${cookie.name}=${cookie.value}; domain=${cookie.domain}; path=${cookie.path}" + (if (cookie.secure) "; secure" else "")
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
}
