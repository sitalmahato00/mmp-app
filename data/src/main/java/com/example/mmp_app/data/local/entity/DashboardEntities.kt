package com.example.mmp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notices")
@Serializable
data class NoticeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String?,
    val publishedAt: String,
    val type: String?,
    val attachmentCount: Int = 0
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

@Entity(tableName = "marks")
data class MarkEntity(
    @PrimaryKey val id: Int,
    val subject: String,
    val score: Float,
    val total: Float,
    val date: String
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subject: String,
    val dueDate: String,
    val status: String // Pending, Submitted, etc.
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val status: String,
    val subject: String?
)
