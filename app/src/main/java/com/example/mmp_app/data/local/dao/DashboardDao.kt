package com.example.mmp_app.data.local.dao

import androidx.room.*
import com.example.mmp_app.data.local.entity.*
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

    @Query("SELECT * FROM marks")
    fun getMarks(): Flow<List<MarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<MarkEntity>)

    @Query("SELECT * FROM assignments")
    fun getAssignments(): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecordEntity>)
}
