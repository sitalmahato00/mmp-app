package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.entity.NoticeEntity
import com.example.mmp_app.data.local.entity.StudentDashboardEntity
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.model.HodDashboardDto
import com.example.mmp_app.data.remote.model.ParentDashboardDto
import com.example.mmp_app.data.remote.model.StudentDashboardDto
import com.example.mmp_app.data.remote.model.TeacherDashboardDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface DashboardRepository {
    fun getStudentDashboard(): Flow<Result<StudentDashboardDto>>
    fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>>
    fun getParentDashboard(): Flow<Result<ParentDashboardDto>>
    fun getHodDashboard(): Flow<Result<HodDashboardDto>>
}

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val dashboardDao: DashboardDao
) : DashboardRepository {

    override fun getStudentDashboard(): Flow<Result<StudentDashboardDto>> = flow {
        // Emit cached data first
        val cachedStats = dashboardDao.getStudentDashboard().first()
        val cachedNotices = dashboardDao.getNotices().first()
        
        if (cachedStats != null) {
            emit(Result.success(StudentDashboardDto(
                attendancePercentage = cachedStats.attendancePercentage,
                averageMarks = cachedStats.averageMarks,
                pendingAssignments = cachedStats.pendingAssignments,
                unreadNotices = cachedStats.unreadNotices,
                recentNotices = cachedNotices.map { 
                    com.example.mmp_app.data.remote.model.NoticeDto(
                        it.id, it.title, it.content, it.date, it.type
                    )
                }
            )))
        }

        // Fetch from network
        try {
            val response = apiService.getStudentDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                
                // Update cache
                dashboardDao.insertStudentDashboard(StudentDashboardEntity(
                    attendancePercentage = data.attendancePercentage,
                    averageMarks = data.averageMarks,
                    pendingAssignments = data.pendingAssignments,
                    unreadNotices = data.unreadNotices
                ))
                dashboardDao.insertNotices(data.recentNotices.map {
                    NoticeEntity(it.id, it.title, it.content, it.date, it.type)
                })
                
                emit(Result.success(data))
            } else {
                if (cachedStats == null) {
                    emit(Result.failure(Exception(response.body()?.message ?: "Failed to fetch dashboard")))
                }
            }
        } catch (e: Exception) {
            if (cachedStats == null) {
                emit(Result.failure(e))
            }
        }
    }

    override fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>> = flow {
        try {
            val response = apiService.getTeacherDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception(response.body()?.message ?: "Failed to fetch teacher dashboard")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getParentDashboard(): Flow<Result<ParentDashboardDto>> = flow {
        try {
            val response = apiService.getParentDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception(response.body()?.message ?: "Failed to fetch parent dashboard")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getHodDashboard(): Flow<Result<HodDashboardDto>> = flow {
        try {
            val response = apiService.getHodDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception(response.body()?.message ?: "Failed to fetch HOD dashboard")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
