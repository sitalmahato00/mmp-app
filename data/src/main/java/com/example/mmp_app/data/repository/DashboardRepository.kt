package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.entity.AssignmentEntity
import com.example.mmp_app.data.local.entity.NoticeEntity
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.exception.handleApiResponse
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val apiService: MmpApiService,
    private val dashboardDao: DashboardDao,
    private val json: Json
) : DashboardRepository {

    override fun getStudentDashboard(): Flow<Result<StudentDashboardDto>> = flow {
        try {
            val response = apiService.getStudentDashboard()
            val dashboard = handleApiResponse(response, json)
            emit(Result.success(dashboard))
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
            val assignments = handleApiResponse(response, json)
            
            // Cache assignments
            dashboardDao.insertAssignments(assignments.map { 
                AssignmentEntity(it.id, it.title, it.subject, it.dueDate, it.status)
            })
            
            emit(Result.success(assignments))
        } catch (e: Exception) {
            // Try to load from cache on failure
            val cached = dashboardDao.getAssignments().firstOrNull()
            if (!cached.isNullOrEmpty()) {
                emit(Result.success(cached.map { 
                    AssignmentDto(it.id, it.title, it.subject, null, it.dueDate, 100, it.status)
                }))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    override fun getAssignmentDetail(id: Int): Flow<Result<AssignmentDto>> = flow {
        try {
            val response = apiService.getAssignmentDetail(id)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun submitAssignment(id: Int, content: String?, filePart: Any?): Result<SubmissionDto> {
        return try {
            val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val multipartFile = filePart as? MultipartBody.Part
            val response = apiService.submitAssignment(id, contentBody, multipartFile)
            Result.success(handleApiResponse(response, json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSubmissionStatus(id: Int): Flow<Result<SubmissionDto>> = flow {
        try {
            val response = apiService.getSubmissionStatus(id)
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

    override fun getStudentTimetable(): Flow<Result<List<ClassDto>>> = flow {
        try {
            val response = apiService.getStudentTimetable()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentNotices(): Flow<Result<List<NoticeDto>>> = flow {
        try {
            val response = apiService.getStudentNotices()
            val notices = handleApiResponse(response, json)
            
            // Cache notices
            dashboardDao.insertNotices(notices.map { 
                NoticeEntity(it.id, it.title, it.content, it.date, it.type)
            })
            
            emit(Result.success(notices))
        } catch (e: Exception) {
            // Try to load from cache on failure
            val cached = dashboardDao.getNotices().firstOrNull()
            if (!cached.isNullOrEmpty()) {
                emit(Result.success(cached.map { 
                    NoticeDto(it.id, it.title, it.content, it.type, it.date)
                }))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    override fun getStudentDownloads(subjectId: Int?): Flow<Result<List<SubjectDocument>>> = flow {
        try {
            val response = apiService.getDownloads(subjectId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.data))
            } else {
                emit(Result.failure(Exception("Failed to fetch downloads")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun recordAttendance(request: AttendanceRecordRequest): Result<Unit> {
        return try {
            handleApiResponse<Unit>(apiService.recordAttendance(request), json)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordMarks(request: MarkRecordRequest): Result<Unit> {
        return try {
            handleApiResponse<Unit>(apiService.recordMarks(request), json)

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
