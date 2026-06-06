package com.example.mmp_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        NoticeEntity::class,
        AttendanceSummaryEntity::class,
        StudentDashboardEntity::class,
        MarkEntity::class,
        AssignmentEntity::class,
        AttendanceRecordEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MmpDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dashboardDao(): DashboardDao
}
