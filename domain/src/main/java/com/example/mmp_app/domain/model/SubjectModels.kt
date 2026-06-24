package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String,
    @SerialName("type") val type: String,           // "theory", "practical", "both"
    @SerialName("credit_hours") val credit_hours: Int,
    @SerialName("full_marks_theory") val full_marks_theory: Int,
    @SerialName("full_marks_practical") val full_marks_practical: Int,
    @SerialName("pass_marks_theory") val pass_marks_theory: Int,
    @SerialName("pass_marks_practical") val pass_marks_practical: Int,
    @SerialName("syllabus_url") val syllabus_url: String?   // nullable
)

@Serializable
data class SubjectMeta(
    @SerialName("program") val program: String,
    @SerialName("semester") val semester: Int,
    @SerialName("total") val total: Int
)

@Serializable
data class SubjectsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<Subject>,
    @SerialName("meta") val meta: SubjectMeta
)

@Serializable
data class SubjectMarks(
    @SerialName("full_marks_theory") val full_marks_theory: Int,
    @SerialName("full_marks_practical") val full_marks_practical: Int,
    @SerialName("pass_marks_theory") val pass_marks_theory: Int,
    @SerialName("pass_marks_practical") val pass_marks_practical: Int,
    @SerialName("internal_theory") val internal_theory: String,
    @SerialName("external_theory") val external_theory: String,
    @SerialName("internal_practical") val internal_practical: String,
    @SerialName("external_practical") val external_practical: String
) {
    fun internalTheoryInt() = internal_theory.toIntOrNull() ?: 0
    fun externalTheoryInt() = external_theory.toIntOrNull() ?: 0
    fun internalPracticalInt() = internal_practical.toIntOrNull() ?: 0
    fun externalPracticalInt() = external_practical.toIntOrNull() ?: 0
}

@Serializable
data class TeacherBrief(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String?,
    @SerialName("avatar_url") val avatar_url: String?,
    @SerialName("designation") val designation: String?
)

@Serializable
data class SubjectDocument(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("category") val category: String?,
    @SerialName("file_url") val file_url: String,
    @SerialName("file_type") val file_type: String?,
    @SerialName("subject_id") val subject_id: String?,
    @SerialName("uploaded_at") val uploaded_at: String
)

@Serializable
data class SubjectDetail(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String,
    @SerialName("type") val type: String,
    @SerialName("credit_hours") val credit_hours: Int,
    @SerialName("details") val details: String?,
    @SerialName("syllabus_url") val syllabus_url: String?,
    @SerialName("marks") val marks: SubjectMarks,
    @SerialName("teachers") val teachers: List<TeacherBrief>?,
    @SerialName("documents") val documents: List<SubjectDocument>?
)

@Serializable
data class SubjectDetailResponse(
    @SerialName("success") val success: Boolean, 
    @SerialName("data") val data: SubjectDetail
)

@Serializable
data class DownloadFile(
    @SerialName("file_url") val file_url: String,
    @SerialName("file_name") val file_name: String
)

@Serializable
data class DownloadFileResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: DownloadFile
)

@Serializable
data class DownloadsPagination(
    @SerialName("current_page") val current_page: Int, 
    @SerialName("last_page") val last_page: Int, 
    @SerialName("per_page") val per_page: Int, 
    @SerialName("total") val total: Int
)

@Serializable
data class DownloadsResponse(
    @SerialName("success") val success: Boolean, 
    @SerialName("data") val data: List<SubjectDocument>, 
    @SerialName("pagination") val pagination: DownloadsPagination?
)
