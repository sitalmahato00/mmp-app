package com.example.mmp_app.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentDashboardDto(
    @SerialName("attendance_percentage") val attendancePercentage: Float,
    @SerialName("average_marks") val averageMarks: Float,
    @SerialName("pending_assignments") val pendingAssignments: Int,
    @SerialName("unread_notices") val unreadNotices: Int,
    @SerialName("recent_notices") val recentNotices: List<NoticeDto>
)

@Serializable
data class NoticeDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("date") val date: String,
    @SerialName("type") val type: String
)

@Serializable
data class TeacherDashboardDto(
    @SerialName("today_classes") val todayClasses: List<ClassDto>,
    @SerialName("total_students") val totalStudents: Int
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
    @SerialName("children") val children: List<ChildDto>
)

@Serializable
data class ChildDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("role") val role: String,
    @SerialName("attendance_percentage") val attendancePercentage: Float,
    @SerialName("average_marks") val averageMarks: Float
)

@Serializable
data class HodDashboardDto(
    @SerialName("department_name") val departmentName: String,
    @SerialName("total_teachers") val totalTeachers: Int,
    @SerialName("total_students") val totalStudents: Int,
    @SerialName("pending_approvals") val pendingApprovals: Int,
    @SerialName("recent_notices") val recentNotices: List<NoticeDto>
)
