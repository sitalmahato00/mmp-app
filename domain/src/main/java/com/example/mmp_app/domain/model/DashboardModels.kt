package com.example.mmp_app.domain.model


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
    @SerialName("current_semester") val currentSemester: Int? = null,
    @SerialName("section") val section: String? = null,
    @SerialName("department") val department: String? = null,
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
    @SerialName("average_marks") val averageMarks: Float = 0f,
    @SerialName("total_exams") val totalExams: Int = 0,
    @SerialName("exams") val exams: List<ExamSummaryDto> = emptyList()
)

@Serializable
data class ExamSummaryDto(
    @SerialName("exam_id") val examId: Int,
    @SerialName("exam_name") val examName: String,
    @SerialName("category") val category: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("subjects") val subjects: List<MarkDto> = emptyList(),
    @SerialName("total_marks") val totalMarks: Int = 0,
    @SerialName("obtained_marks") val obtainedMarks: Float = 0f,
    @SerialName("percentage") val percentage: Float = 0f
)

@Serializable
data class MarkDto(
    @SerialName("subject") val subject: String,
    @SerialName("code") val code: String? = null,
    @SerialName("total") val score: Float,
    @SerialName("full_marks") val total: Float = 25f,
    @SerialName("pass_marks") val passMarks: Float = 10f,
    @SerialName("attendance") val attendance: String = "100%",
    @SerialName("percentage") val percentage: Float = 0f,
    @SerialName("result") val result: String? = null,
    @SerialName("is_passed") val isPassed: Boolean = true,
    @SerialName("is_absent") val isAbsent: Boolean = false,
    @SerialName("date") val date: String? = null
)

@Serializable
data class AssignmentListResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<AssignmentItemDto>,
    @SerialName("meta") val meta: AssignmentMetaDto? = null
)

@Serializable
data class AssignmentItemDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("subject") val subject: String? = null,
    @SerialName("subject_code") val subjectCode: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_marks") val maxMarks: Double? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("submissions_count") val submissionsCount: Int = 0,
    @SerialName("is_overdue") val isOverdue: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AssignmentMetaDto(
    @SerialName("total") val total: Int,
    @SerialName("upcoming") val upcoming: Int,
    @SerialName("overdue") val overdue: Int
)

@Serializable
data class SubmissionsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: SubmissionsDataDto
)

@Serializable
data class SubmissionsDataDto(
    @SerialName("assignment") val assignment: AssignmentBriefDto,
    @SerialName("total") val total: Int,
    @SerialName("submissions") val submissions: List<SubmissionItemDto>
)

@Serializable
data class AssignmentBriefDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("max_marks") val maxMarks: Double? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("attachment_url") val attachmentUrl: String? = null
)

@Serializable
data class SubmissionItemDto(
    @SerialName("id") val id: Int,
    @SerialName("student_id") val studentId: Int,
    @SerialName("student_name") val studentName: String? = null,
    @SerialName("student_no") val studentNo: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("student_note") val studentNote: String? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("status") val status: String,            // "submitted", "graded", "late"
    @SerialName("marks_obtained") val marksObtained: Double? = null,
    @SerialName("teacher_feedback") val teacherFeedback: String? = null,
    @SerialName("submitted_at") val submittedAt: String
)

@Serializable
data class MessageResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String
)

@Serializable
data class GradeRequest(
    @SerialName("marks_obtained") val marksObtained: Double,
    @SerialName("teacher_feedback") val teacherFeedback: String? = null
)

@Serializable
data class UpdateAssignmentRequest(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("max_marks") val maxMarks: Double? = null
)

@Serializable
data class StudentAssignmentsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<StudentAssignmentItemDto>,
    @SerialName("pagination") val pagination: PaginationDto
)

@Serializable
data class StudentAssignmentItemDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("subject") val subject: String? = null,
    @SerialName("subject_code") val subjectCode: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_marks") val maxMarks: Double? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("status") val status: String,                // "not_submitted", "submitted", "graded"
    @SerialName("is_overdue") val isOverdue: Boolean = false,
    @SerialName("submission") val submission: SubmissionBriefDto? = null
)

@Serializable
data class SubmissionBriefDto(
    @SerialName("id") val id: Int,
    @SerialName("student_note") val studentNote: String? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("marks_obtained") val marksObtained: Double? = null,
    @SerialName("teacher_feedback") val teacherFeedback: String? = null,
    @SerialName("submitted_at") val submittedAt: String
)

@Serializable
data class StudentAssignmentDetailResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: StudentAssignmentDetailDto
)

@Serializable
data class StudentAssignmentDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("subject") val subject: String? = null,
    @SerialName("subject_code") val subjectCode: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_marks") val maxMarks: Double? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("submission") val submission: SubmissionBriefDto? = null
)

@Serializable
data class AssignmentDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("subject") val subject: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_marks") val maxMarks: Float? = null,
    @SerialName("obtained_marks") val obtainedMarks: Float? = null,
    @SerialName("status") val status: String
)

@Serializable
data class AssignmentDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("subject") val subject: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_marks") val maxMarks: Float? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null
)

@Serializable
data class SubmissionDto(
    @SerialName("submission_id") val submissionId: Int,
    @SerialName("status") val status: String,
    @SerialName("attachment_url") val attachmentUrl: String? = null
)

@Serializable
data class SubmissionStatusDto(
    @SerialName("id") val id: Int,
    @SerialName("status") val status: String,
    @SerialName("student_note") val studentNote: String? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("marks_obtained") val marksObtained: Float? = null,
    @SerialName("max_marks") val maxMarks: Float? = null,
    @SerialName("feedback") val feedback: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class PaginationDto(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("total") val total: Int
)

@Serializable
data class AssignmentsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<AssignmentDto>,
    @SerialName("pagination") val pagination: PaginationDto
)

@Serializable
data class AssignmentDetailResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: AssignmentDetailDto
)

@Serializable
data class SubmissionStatusResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: SubmissionStatusDto
)

@Serializable
data class SubmitResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: SubmissionDto? = null
)

@Serializable
data class NoticeAttachment(
    @SerialName("id") val id: Int,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_type") val fileType: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("is_image") val isImage: Boolean = false,
    @SerialName("is_pdf") val isPdf: Boolean = false
)

@Serializable
data class NoticeDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("attachments") val attachments: List<NoticeAttachment> = emptyList(),
    @SerialName("published_at") val publishedAt: String
)

@Serializable
data class NoticeDetailResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: NoticeDetailDto
)

@Serializable
data class NoticeDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("attachment_count") val attachmentCount: Int = 0,
    @SerialName("published_at") val publishedAt: String
)

@Serializable
data class NoticesResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<NoticeDto>,
    @SerialName("pagination") val pagination: PaginationDto
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
data class TeacherProfileDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("employee_id") val employeeId: String? = null,
    @SerialName("designation") val designation: String,
    @SerialName("department") val department: String,
    @SerialName("qualification") val qualification: String? = null,
    @SerialName("specialization") val specialization: String? = null,
    @SerialName("employment_type") val employmentType: String,
    @SerialName("join_date") val joinDate: String? = null
)

@Serializable
data class TodayScheduleDto(
    @SerialName("today") val today: String,
    @SerialName("day") val day: String,
    @SerialName("classes") val classes: List<ClassSlotDto>
)

@Serializable
data class ClassSlotDto(
    @SerialName("id") val id: Int,
    @SerialName("subject") val subject: String? = null,
    @SerialName("subject_code") val subjectCode: String? = null,
    @SerialName("program") val program: String? = null,
    @SerialName("semester") val semester: Int? = null,
    @SerialName("section") val section: String? = null,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("room") val room: String? = null,
    @SerialName("type") val type: String? = null
)

@Serializable
data class MarkComponentsDto(
    @SerialName("components") val components: List<String>
)

@Serializable
data class StudentItemDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("student_no") val studentNo: String? = null,
    @SerialName("roll_number") val rollNumber: String? = null,
    @SerialName("section") val section: String? = null
)

@Serializable
data class TeacherStudentsResponseDto(
    @SerialName("subject") val subject: String,
    @SerialName("code") val code: String,
    @SerialName("program_id") val programId: Int,
    @SerialName("semester") val semester: Int,
    @SerialName("total") val total: Int,
    @SerialName("students") val students: List<StudentItemDto>
)

@Serializable
data class TeacherSubjectDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String
)

@Serializable
data class ClassDto(
    @SerialName("id") val id: Int,
    @SerialName("subject") val subject: String,
    @SerialName("time") val time: String,
    @SerialName("room") val room: String
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
data class ExamDetailDto(
    @SerialName("exam_id") val examId: Int? = null,
    @SerialName("exam_name") val examName: String,
    @SerialName("category") val category: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("subjects") val marks: List<MarkDto>
)

@Serializable
data class SubjectMarkDto(
    @SerialName("subject_name") val subjectName: String,
    @SerialName("marks") val marks: List<ExamMarkDto>
)

@Serializable
data class ExamMarkDto(
    @SerialName("exam_name") val examName: String,
    @SerialName("obtained_marks") val obtainedMarks: Float,
    @SerialName("total_marks") val totalMarks: Float,
    @SerialName("percentage") val percentage: Float,
    @SerialName("date") val date: String? = null
)

@Serializable
data class MarksheetDto(
    @SerialName("download_url") val downloadUrl: String
)

@Serializable
data class AttendanceRecordRequest(
    @SerialName("class_id") val classId: Int,
    @SerialName("date") val date: String,
    @SerialName("attendance") val attendance: List<StudentAttendanceItem>
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
