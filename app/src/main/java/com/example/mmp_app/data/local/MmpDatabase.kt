package com.example.mmp_app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.local.entity.AttendanceSummaryEntity
import com.example.mmp_app.data.local.entity.NoticeEntity
import com.example.mmp_app.data.local.entity.StudentDashboardEntity
import com.example.mmp_app.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        NoticeEntity::class,
        AttendanceSummaryEntity::class,
        StudentDashboardEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MmpDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dashboardDao(): DashboardDao
}
