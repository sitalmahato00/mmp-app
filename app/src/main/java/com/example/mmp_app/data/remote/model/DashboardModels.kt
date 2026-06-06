package com.example.mmp_app.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentDashboardDto(
    @SerialName("student_name") val studentName: String,
    @SerialName("student_id") val studentId: Int,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("roll_number") val rollNumber: String? = null,
    @SerialName("program") val program: String,
    @SerialName("semester") val semester: Int,
    @SerialName("kpi_cards") val kpiCards: StudentKpiDto
)

@Serializable
data class StudentKpiDto(
    @SerialName("attendance_percentage") val attendancePercentage: Float,
    @SerialName("average_marks") val averageMarks: Float,
    @SerialName("pending_assignments") val pendingAssignments: Int,
    @SerialName("unread_notices") val unreadNotices: Int
)

@Serializable
data class AttendanceSummaryDto(
    @SerialName("total_classes") val totalClasses: Int,
    @SerialName("present") val present: Int,
    @SerialName("absent") val absent: Int,
    @SerialName("late") val late: Int,
    @SerialName("attendance_percentage") val attendancePercentage: Float,
    @SerialName("status") val status: String
)

@Serializable
data class AttendanceDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("subject") val subject: String? = null,
    @SerialName("date") val date: String,
    @SerialName("status") val status: String,
    @SerialName("session") val session: String? = null
)

@Serializable
data class MarksSummaryDto(
    @SerialName("average_marks") val averageMarks: Float,
    @SerialName("total_exams") val totalExams: Int,
    @SerialName("exams") val exams: List<ExamSummaryDto> = emptyList()
)

@Serializable
data class ExamSummaryDto(
    @SerialName("exam_id") val examId: Int,
    @SerialName("exam_name") val examName: String,
    @SerialName("total_marks") val totalMarks: Int,
    @SerialName("obtained_marks") val obtainedMarks: Float,
    @SerialName("percentage") val percentage: Float
)

@Serializable
data class MarkDto(
    @SerialName("subject") val subject: String,
    @SerialName("obtained_marks") val score: Float,
    @SerialName("total_marks") val total: Float,
    @SerialName("percentage") val percentage: Float,
    @SerialName("date") val date: String? = null
)

@Serializable
data class AssignmentDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("subject") val subject: String,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_marks") val maxMarks: Int,
    @SerialName("status") val status: String
)

@Serializable
data class NoticeDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val content: String,
    @SerialName("category") val type: String,
    @SerialName("published_at") val date: String
)

@Serializable
data class TeacherDashboardDto(
    @SerialName("teacher_name") val teacherName: String,
    @SerialName("total_classes") val totalClasses: Int,
    @SerialName("total_students") val totalStudents: Int,
    @SerialName("pending_marks") val pendingMarks: Int,
    @SerialName("pending_assignments") val pendingAssignments: Int,
    @SerialName("today_classes") val todayClasses: List<ClassDto> = emptyList()
)

@Serializable
data class ClassDto(
    @SerialName("id") val id: Int,
    @SerialName("subject") val subject: String,
    @SerialName("time") val time: String,
    @SerialName("room") val room: String
)

@Serializable
data class ParentDashboardDto(
    @SerialName("children_count") val childrenCount: Int,
    @SerialName("children") val children: List<ChildDto>
)

@Serializable
data class ChildDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("program") val program: String? = null,
    @SerialName("attendance_percentage") val attendancePercentage: Float = 0f,
    @SerialName("average_marks") val averageMarks: Float = 0f,
    @SerialName("semester") val semester: Int? = null,
    @SerialName("roll_number") val rollNumber: String? = null
)

@Serializable
data class AttendanceBySubjectDto(
    @SerialName("subject_name") val subjectName: String,
    @SerialName("total_classes") val totalClasses: Int,
    @SerialName("present") val present: Int,
    @SerialName("absent") val absent: Int,
    @SerialName("late") val late: Int,
    @SerialName("attendance_percentage") val attendancePercentage: Float
)

@Serializable
data class SubjectDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String? = null
)

@Serializable
data class StudentAttendanceItem(
    @SerialName("student_id") val studentId: Int,
    @SerialName("status") val status: String
)

@Serializable
data class MarkRecordRequest(
    @SerialName("class_id") val classId: Int,
    @SerialName("subject") val subject: String,
    @SerialName("date") val date: String,
    @SerialName("marks") val marks: List<StudentMarkItem>
)

@Serializable
data class StudentMarkItem(
    @SerialName("student_id") val studentId: Int,
    @SerialName("score") val score: Float,
    @SerialName("total") val total: Float
)
