package com.example.mmp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notices")
@Serializable
data class NoticeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val date: String,
    val type: String // Internal, Public, etc.
)

@Entity(tableName = "attendance_summary")
data class AttendanceSummaryEntity(
    @PrimaryKey val studentId: Int,
    val percentage: Float,
    val totalClasses: Int,
    val presentCount: Int
)

@Entity(tableName = "student_dashboard")
data class StudentDashboardEntity(
    @PrimaryKey val id: Int = 1, // Singleton for current student
    val attendancePercentage: Float,
    val averageMarks: Float,
    val pendingAssignments: Int,
    val unreadNotices: Int
)
