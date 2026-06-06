package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.entity.*
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.exception.ApiException
import com.example.mmp_app.data.remote.exception.handleApiResponse
import com.example.mmp_app.data.remote.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

interface DashboardRepository {
    fun getStudentDashboard(): Flow<Result<StudentDashboardDto>>
    fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>>
    fun getParentDashboard(): Flow<Result<ParentDashboardDto>>
    fun getStudentMarks(): Flow<Result<List<MarkDto>>>
    fun getStudentMarksSummary(): Flow<Result<MarksSummaryDto>>
    fun getMarksByExam(examId: Int): Flow<Result<ExamDetailDto>>
    fun getMarksBySubject(subjectId: Int): Flow<Result<SubjectMarkDto>>
    fun getMarksheet(): Flow<Result<MarksheetDto>>
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
    private val json: Json
) : DashboardRepository {

    override fun getStudentDashboard(): Flow<Result<StudentDashboardDto>> = flow {
        try {
            val response = apiService.getStudentDashboard()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>> = flow {
        try {
            val response = apiService.getTeacherDashboard()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getParentDashboard(): Flow<Result<ParentDashboardDto>> = flow {
        try {
            val response = apiService.getParentDashboard()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentMarks(): Flow<Result<List<MarkDto>>> = flow {
        try {
            val summary = handleApiResponse<MarksSummaryDto>(apiService.getStudentMarksSummary(), json)
            val marks = summary.exams.map { 
                MarkDto(subject = it.examName, score = it.obtainedMarks, total = it.totalMarks.toFloat(), percentage = it.percentage)
            }
            emit(Result.success(marks))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentMarksSummary(): Flow<Result<MarksSummaryDto>> = flow {
        try {
            val response = apiService.getStudentMarksSummary()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getMarksByExam(examId: Int): Flow<Result<ExamDetailDto>> = flow {
        try {
            val response = apiService.getMarksByExam(examId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getMarksBySubject(subjectId: Int): Flow<Result<SubjectMarkDto>> = flow {
        try {
            val response = apiService.getMarksBySubject(subjectId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getMarksheet(): Flow<Result<MarksheetDto>> = flow {
        try {
            val response = apiService.getMarksheet()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAssignments(): Flow<Result<List<AssignmentDto>>> = flow {
        try {
            val response = apiService.getStudentAssignments()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAttendance(): Flow<Result<List<AttendanceDto>>> = flow {
        try {
            val response = apiService.getStudentAttendanceDetail()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAttendanceSummary(): Flow<Result<AttendanceSummaryDto>> = flow {
        try {
            val response = apiService.getStudentAttendanceSummary()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentAttendanceBySubject(subjectId: Int): Flow<Result<AttendanceBySubjectDto>> = flow {
        try {
            val response = apiService.getAttendanceBySubject(subjectId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentSubjects(): Flow<Result<List<SubjectDto>>> = flow {
        try {
            val response = apiService.getStudentSubjects()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentNotices(): Flow<Result<List<NoticeDto>>> = flow {
        try {
            val response = apiService.getStudentNotices()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun recordAttendance(request: AttendanceRecordRequest): Result<Unit> {
        return try {
            handleApiResponse(apiService.recordAttendance(request), json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordMarks(request: MarkRecordRequest): Result<Unit> {
        return try {
            handleApiResponse(apiService.recordMarks(request), json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClassStudents(classId: Int): Result<List<UserDto>> {
        return try {
            Result.success(handleApiResponse(apiService.getClassStudents(classId), json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getChildDashboard(childId: Int): Flow<Result<StudentDashboardDto>> = flow {
        try {
            val response = apiService.getChildDashboard(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
