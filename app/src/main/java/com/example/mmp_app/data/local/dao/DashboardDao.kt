package com.example.mmp_app.data.local.dao

import androidx.room.*
import com.example.mmp_app.data.local.entity.AttendanceSummaryEntity
import com.example.mmp_app.data.local.entity.NoticeEntity
import com.example.mmp_app.data.local.entity.StudentDashboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM notices ORDER BY date DESC")
    fun getNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("SELECT * FROM student_dashboard WHERE id = 1")
    fun getStudentDashboard(): Flow<StudentDashboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentDashboard(dashboard: StudentDashboardEntity)

    @Query("DELETE FROM notices")
    suspend fun clearNotices()
}
