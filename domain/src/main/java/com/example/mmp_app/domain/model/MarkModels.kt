package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PendingMark(
    @SerialName("exam_id") val examId: Int,
    @SerialName("exam_name") val examName: String,
    @SerialName("category") val category: String,  // "monthly_assessment" or "ctevt_final"
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("subject") val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("total_students") val totalStudents: Int,
    @SerialName("entered") val entered: Int,
    @SerialName("remaining") val remaining: Int,
    @SerialName("is_complete") val isComplete: Boolean,
    @SerialName("marks_open") val marksOpen: Boolean
)

@Serializable
data class PendingMarksResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<PendingMark>
)

@Serializable
data class MarkEntryResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: MarkEntryData
)

@Serializable
data class MarkEntryData(
    @SerialName("exam") val exam: ExamInfo,
    @SerialName("subject") val subject: SubjectInfo,
    @SerialName("scheme") val scheme: MarkingScheme? = null,
    @SerialName("total_students") val totalStudents: Int,
    @SerialName("students") val students: List<StudentMarkEntry>
)

@Serializable
data class ExamInfo(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("status") val status: String,
    @SerialName("marks_open") val marksOpen: Boolean,
    @SerialName("assessment_full_marks") val assessmentFullMarks: String? = null,
    @SerialName("assessment_pass_marks") val assessmentPassMarks: String? = null
)

@Serializable
data class SubjectInfo(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String,
    @SerialName("has_theory") val hasTheory: Boolean = false,
    @SerialName("has_practical") val hasPractical: Boolean = false
)

@Serializable
data class MarkingScheme(
    @SerialName("full_marks_internal_theory") val fullMarksInternalTheory: Double = 0.0,
    @SerialName("pass_marks_internal_theory") val passMarksInternalTheory: Double = 0.0,
    @SerialName("full_marks_external_theory") val fullMarksExternalTheory: Double = 0.0,
    @SerialName("pass_marks_external_theory") val passMarksExternalTheory: Double = 0.0,
    @SerialName("full_marks_internal_practical") val fullMarksInternalPractical: Double = 0.0,
    @SerialName("pass_marks_internal_practical") val passMarksInternalPractical: Double = 0.0,
    @SerialName("full_marks_external_practical") val fullMarksExternalPractical: Double = 0.0,
    @SerialName("pass_marks_external_practical") val passMarksExternalPractical: Double = 0.0
)

@Serializable
data class StudentMarkEntry(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("student_no") val studentNo: String,
    @SerialName("roll_number") val rollNumber: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("mark") val mark: ExistingMark? = null
)

@Serializable
data class ExistingMark(
    @SerialName("id") val id: Int? = null,
    @SerialName("status") val status: String,
    @SerialName("is_absent") val isAbsent: Boolean = false,
    // monthly_assessment
    @SerialName("assessment_obtained_marks") val assessmentObtainedMarks: String? = null,
    @SerialName("assessment_attendance_percent") val assessmentAttendancePercent: String? = null,
    // ctevt
    @SerialName("internal_theory_marks") val internalTheoryMarks: String? = null,
    @SerialName("external_theory_marks") val externalTheoryMarks: String? = null,
    @SerialName("internal_practical_marks") val internalPracticalMarks: String? = null,
    @SerialName("external_practical_marks") val externalPracticalMarks: String? = null,
    @SerialName("total_marks") val totalMarks: Double = 0.0,
    @SerialName("is_passed") val isPassed: Boolean = false,
    @SerialName("result_remark") val resultRemark: String? = null,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class SubmitMarkRequest(
    @SerialName("exam_id") val examId: Int,
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("student_id") val studentId: Int,
    @SerialName("is_absent") val isAbsent: Boolean = false,
    @SerialName("assessment_obtained_marks") val assessmentObtainedMarks: Double? = null,
    @SerialName("assessment_attendance_percent") val assessmentAttendancePercent: Double? = null,
    @SerialName("internal_theory_marks") val internalTheoryMarks: Double? = null,
    @SerialName("external_theory_marks") val externalTheoryMarks: Double? = null,
    @SerialName("internal_practical_marks") val internalPracticalMarks: Double? = null,
    @SerialName("external_practical_marks") val externalPracticalMarks: Double? = null,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class BulkSubmitRequest(
    @SerialName("exam_id") val examId: Int,
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("marks") val marks: List<SubmitMarkRequest>
)

@Serializable
data class MarkSubmitResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: MarkSubmitData? = null
)

@Serializable
data class MarkSubmitData(
    @SerialName("mark_id") val markId: Int,
    @SerialName("student_id") val studentId: Int,
    @SerialName("total_marks") val totalMarks: Double,
    @SerialName("is_passed") val isPassed: Boolean,
    @SerialName("result_remark") val resultRemark: String,
    @SerialName("status") val status: String
)

@Serializable
data class MarkHistoryItem(
    @SerialName("mark_id") val markId: Int,
    @SerialName("exam") val exam: String,
    @SerialName("category") val category: String,
    @SerialName("subject") val subject: String,
    @SerialName("subject_code") val subjectCode: String,
    @SerialName("student") val student: String,
    @SerialName("student_no") val studentNo: String,
    @SerialName("total_marks") val totalMarks: Double,
    @SerialName("is_passed") val isPassed: Boolean,
    @SerialName("status") val status: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class MarksHistoryResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<MarkHistoryItem>,
    @SerialName("pagination") val pagination: PaginationDto? = null
)

@Serializable
data class ComponentsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: SubjectComponentsData
)

@Serializable
data class SubjectComponentsData(
    @SerialName("subject") val subject: String,
    @SerialName("code") val code: String,
    @SerialName("has_theory") val hasTheory: Boolean,
    @SerialName("has_practical") val hasPractical: Boolean,
    @SerialName("default_scheme") val defaultScheme: MarkingScheme? = null,
    @SerialName("open_exams") val openExams: List<OpenExamInfo> = emptyList()
)

@Serializable
data class OpenExamInfo(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("marks_open") val marksOpen: Boolean,
    @SerialName("status") val status: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("scheme") val scheme: MarkingScheme? = null
)
