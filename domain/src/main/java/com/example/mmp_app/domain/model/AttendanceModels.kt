package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSessionRequest(
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("date") val date: String,   // AD "YYYY-MM-DD"
    @SerialName("period") val period: String?
)

@Serializable
data class StartSessionResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: SessionData? = null
)

@Serializable
data class SessionData(
    @SerialName("session_id") val sessionId: Int,
    @SerialName("subject") val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("date") val date: String,
    @SerialName("period") val period: String? = null,
    @SerialName("is_existing") val isExisting: Boolean = false,
    @SerialName("attendance_count") val attendanceCount: Int = 0,
    @SerialName("existing_attendance") val existingAttendance: List<ExistingAttendance> = emptyList()
)

@Serializable
data class ExistingAttendance(
    @SerialName("student_id") val studentId: Int,
    @SerialName("status") val status: String,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class HistoryResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<HistorySession> = emptyList(),
    @SerialName("meta") val meta: HistoryMeta? = null
)

@Serializable
data class HistorySession(
    @SerialName("session_id") val sessionId: Int,
    @SerialName("subject") val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("date") val date: String,
    @SerialName("period") val period: String? = null,
    @SerialName("total_students") val totalStudents: Int = 0,
    @SerialName("present") val present: Int = 0,
    @SerialName("absent") val absent: Int = 0,
    @SerialName("late") val late: Int = 0
)

@Serializable
data class HistoryMeta(
    @SerialName("total_sessions") val totalSessions: Int = 0,
    @SerialName("total_marked") val totalMarked: Int = 0
)

@Serializable
data class BulkMarkRequest(
    @SerialName("attendance_session_id") val attendanceSessionId: Int,
    @SerialName("attendance") val attendance: List<AttendanceRecord>
)

@Serializable
data class AttendanceRecord(
    @SerialName("student_id") val studentId: Int,
    @SerialName("status") val status: String,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class SingleMarkRequest(
    @SerialName("attendance_session_id") val attendanceSessionId: Int,
    @SerialName("student_id") val studentId: Int,
    @SerialName("status") val status: String,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class SessionDetailResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: SessionDetailData? = null
)

@Serializable
data class SessionDetailData(
    @SerialName("session_id") val sessionId: Int,
    @SerialName("subject") val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("date") val date: String,
    @SerialName("period") val period: String? = null,
    @SerialName("students") val students: List<StudentAttendanceDetail> = emptyList()
)

@Serializable
data class StudentAttendanceDetail(
    @SerialName("id") val studentId: Int,
    @SerialName("name") val name: String,
    @SerialName("student_no") val studentNo: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("status") val status: String,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class StudentsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<StudentItemDto>
)
