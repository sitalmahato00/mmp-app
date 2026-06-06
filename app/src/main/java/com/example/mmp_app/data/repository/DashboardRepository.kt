package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.entity.*
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface DashboardRepository {
    fun getStudentDashboard(): Flow<Result<StudentDashboardDto>>
    fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>>
    fun getParentDashboard(): Flow<Result<ParentDashboardDto>>
    fun getStudentMarks(): Flow<Result<List<MarkDto>>>
    fun getStudentAssignments(): Flow<Result<List<AssignmentDto>>>
    fun getStudentAttendance(): Flow<Result<List<AttendanceDto>>>
    fun getStudentAttendanceSummary(): Flow<Result<AttendanceSummaryDto>>
    fun getStudentAttendanceBySubject(subjectId: Int): Flow<Result<AttendanceBySubjectDto>>
    fun getStudentSubjects(): Flow<Result<List<SubjectDto>>>
    fun getStudentNotices(): Flow<Result<List<NoticeDto>>>
    suspend fun recordAttendance(request: AttendanceRecordRequest): Result<Unit>
    suspend fun recordMarks(request: MarkRecordRequest): Result<Unit>
    suspend fun getClassStudents(classId: Int): Result<List<UserDto>>
    fun getChildDashboard(childId: Int): Flow<Result<StudentDashboardDto>>
}

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val dashboardDao: DashboardDao
) : DashboardRepository {

    override fun getStudentDashboard(): Flow<Result<StudentDashboardDto>> = flow {
        try {
            val response = apiService.getStudentDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception(response.body()?.message ?: "Failed to fetch dashboard")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
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

    override fun getStudentMarks(): Flow<Result<List<MarkDto>>> = flow {
        try {
            val summaryResponse = apiService.getStudentMarksSummary()
            if (summaryResponse.isSuccessful && summaryResponse.body()?.success == true) {
                val exams = summaryResponse.body()!!.data!!.exams
                // In a real app, we might fetch detail for each exam, but for now let's map what we have or just use dummy marks
                // The API GET /marks/summary has an exams list
                val marks = exams.map { 
                    MarkDto(subject = it.examName, score = it.obtainedMarks, total = it.totalMarks.toFloat(), percentage = it.percentage)
                }
                emit(Result.success(marks))
            } else {
                emit(Result.failure(Exception("Failed to fetch marks summary")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAssignments(): Flow<Result<List<AssignmentDto>>> = flow {
        try {
            val response = apiService.getStudentAssignments()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch assignments")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAttendance(): Flow<Result<List<AttendanceDto>>> = flow {
        try {
            val response = apiService.getStudentAttendanceDetail()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch attendance detail")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAttendanceSummary(): Flow<Result<AttendanceSummaryDto>> = flow {
        try {
            val response = apiService.getStudentAttendanceSummary()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch attendance summary")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAttendanceBySubject(subjectId: Int): Flow<Result<AttendanceBySubjectDto>> = flow {
        try {
            val response = apiService.getAttendanceBySubject(subjectId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch attendance by subject")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentSubjects(): Flow<Result<List<SubjectDto>>> = flow {
        try {
            val response = apiService.getStudentSubjects()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch subjects")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentNotices(): Flow<Result<List<NoticeDto>>> = flow {
        try {
            val response = apiService.getStudentNotices()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception("Failed to fetch notices")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun recordAttendance(request: AttendanceRecordRequest): Result<Unit> {
        return try {
            val response = apiService.recordAttendance(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to record attendance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordMarks(request: MarkRecordRequest): Result<Unit> {
        return try {
            val response = apiService.recordMarks(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to record marks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClassStudents(classId: Int): Result<List<UserDto>> {
        return try {
            val response = apiService.getClassStudents(classId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch students"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getChildDashboard(childId: Int): Flow<Result<StudentDashboardDto>> = flow {
        try {
            val response = apiService.getChildDashboard(childId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!.data!!))
            } else {
                emit(Result.failure(Exception(response.body()?.message ?: "Failed to fetch child dashboard")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
