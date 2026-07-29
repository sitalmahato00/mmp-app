package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParentDashboardDto(
    @SerialName("parent_name") val parentName: String,
    @SerialName("children_count") val childrenCount: Int,
    @SerialName("children") val children: List<ChildSummaryDto>
)

@Serializable
data class ChildSummaryDto(
    val id: Int,
    val name: String,
    @SerialName("student_id") val studentId: Int,
    val program: String,
    val semester: Int,
    val section: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("attendance_percent") val attendancePercent: Double,
    @SerialName("attendance_status") val attendanceStatus: String
)

@Serializable
data class ChildDetailDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("student_no") val studentNo: String,
    @SerialName("roll_number") val rollNumber: String?,
    val program: String,
    val department: String,
    val semester: Int,
    val section: String,
    val status: String,
    val batch: String,
    @SerialName("registration_number") val registrationNumber: String? = null,
    @SerialName("admission_date") val admissionDate: String? = null
)

@Serializable
data class ParentAttendanceRecordDto(
    val id: Int,
    val subject: String?,
    val date: String,
    val status: String,
    val remarks: String?
)

@Serializable
data class ParentAttendanceSummaryDto(
    @SerialName("total_classes") val totalClasses: Int,
    val present: Int,
    val late: Int,
    val absent: Int,
    @SerialName("attendance_percentage") val attendancePercentage: Double,
    val status: String
)

@Serializable
data class ParentMarkRecordDto(
    val id: Int,
    val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    val exam: String,
    @SerialName("exam_type") val examType: String,
    @SerialName("obtained_marks") val obtainedMarks: Double,
    @SerialName("full_marks") val fullMarks: Double,
    @SerialName("pass_marks") val passMarks: Double,
    @SerialName("is_pass") val isPass: Boolean,
    @SerialName("is_absent") val isAbsent: Boolean
)

@Serializable
data class ParentMarksSummaryDto(
    @SerialName("total_exams") val totalExams: Int,
    @SerialName("average_marks") val averageMarks: Double,
    @SerialName("passed_count") val passedCount: Int,
    @SerialName("failed_count") val failedCount: Int
)

@Serializable
data class ParentAssignmentDto(
    val id: Int,
    val title: String,
    val subject: String?,
    @SerialName("due_date") val dueDate: String,
    val status: String,
    val marks: Double?,
    val feedback: String?,
    val description: String? = null
)

@Serializable
data class ParentTimetableDto(
    @SerialName("has_timetable") val hasTimetable: Boolean,
    val semester: Int,
    val section: String,
    @SerialName("effective_from") val effectiveFrom: String?,
    val timetable: List<ParentDayTimetableDto>
)

@Serializable
data class ParentDayTimetableDto(
    val day: String,
    val classes: List<ParentTimetableClassDto>
)

@Serializable
data class ParentTimetableClassDto(
    val id: Int,
    val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    val teacher: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val room: String,
    val type: String
)

@Serializable
data class ParentNoticeDto(
    val id: Int,
    val title: String,
    val type: String,
    @SerialName("published_at") val publishedAt: String,
    val content: String? = null
)

@Serializable
data class ParentProfileDto(
    val name: String,
    val email: String,
    val phone: String? = null,
    val gender: String? = null,
    val address: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val occupation: String? = null,
    @SerialName("relation_to_student") val relationToStudent: String = "parent",
    @SerialName("children_count") val childrenCount: Int = 0
)

// Update profile request
@Serializable
data class UpdateParentProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val occupation: String? = null
)
