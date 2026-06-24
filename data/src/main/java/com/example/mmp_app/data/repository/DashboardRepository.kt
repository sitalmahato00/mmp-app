package com.example.mmp_app.data.repository

import com.example.mmp_app.data.local.dao.DashboardDao
import com.example.mmp_app.data.local.entity.AssignmentEntity
import com.example.mmp_app.data.local.entity.NoticeEntity
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.data.remote.exception.handleApiResponse
import com.example.mmp_app.data.remote.exception.handleRawResponse
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
            val examList = handleApiResponse(apiService.getStudentMarksSummary(), json)
            val marks = examList.flatMap { it.subjects }
            emit(Result.success(marks))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getStudentMarksSummary(): Flow<Result<MarksSummaryDto>> = flow {
        try {
            val response = apiService.getStudentMarksSummary()
            val examList = handleApiResponse(response, json)
            
            val fullMarksPerSubject = 25f
            
            val processedExams = examList.map { exam ->
                val updatedSubjects = exam.subjects.map { subject ->
                    val percentage = if (subject.total > 0) (subject.score / subject.total * 100) else 0f
                    subject.copy(percentage = percentage)
                }
                
                val obtained = updatedSubjects.sumOf { it.score.toDouble() }.toFloat()
                val total = (updatedSubjects.size * fullMarksPerSubject)
                val percentage = if (total > 0) (obtained / total * 100) else 0f
                
                exam.copy(
                    subjects = updatedSubjects,
                    obtainedMarks = obtained,
                    totalMarks = total.toInt(),
                    percentage = percentage
                )
            }
            
            val allSubjects = processedExams.flatMap { it.subjects }
            val totalObtained = allSubjects.sumOf { it.score.toDouble() }.toFloat()
            val totalFull = (allSubjects.size * fullMarksPerSubject)
            val overallAverage = if (totalFull > 0) (totalObtained / totalFull * 100) else 0f
            
            emit(Result.success(MarksSummaryDto(
                averageMarks = overallAverage,
                totalExams = processedExams.size,
                exams = processedExams
            )))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getMarksByExam(examId: String): Flow<Result<ExamDetailDto>> = flow {
        try {
            val response = apiService.getMarksByExam(examId)
            val detail = handleApiResponse(response, json)
            val updatedMarks = detail.marks.map { mark ->
                val percentage = if (mark.total > 0) (mark.score / mark.total * 100) else 0f
                mark.copy(percentage = percentage)
            }
            emit(Result.success(detail.copy(marks = updatedMarks)))
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
            val result = handleRawResponse(response)
            val assignments = result.data
            
            // Cache assignments
            dashboardDao.insertAssignments(assignments.map { 
                AssignmentEntity(it.id, it.title, it.subject ?: "N/A", it.dueDate, it.status)
            })
            
            emit(Result.success(assignments))
        } catch (e: Exception) {
            // Try to load from cache on failure
            val cached = dashboardDao.getAssignments().firstOrNull()
            if (!cached.isNullOrEmpty()) {
                emit(Result.success(cached.map { 
                    AssignmentDto(it.id, it.title, it.subject, null, it.dueDate, null, null, it.status)
                }))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    override fun getAssignmentDetail(id: Int): Flow<Result<AssignmentDetailDto>> = flow {
        try {
            val response = apiService.getAssignmentDetail(id)
            val result = handleRawResponse(response)
            emit(Result.success(result.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun submitAssignment(id: Int, content: String?): Result<SubmissionDto> {
        return try {
            val request = mutableMapOf<String, String>()
            content?.let { request["content"] = it }
            val response = apiService.submitAssignment(id, request)
            val result = handleRawResponse(response)
            Result.success(result.data ?: throw Exception("Submission failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitAssignmentWithFile(id: Int, content: String?, filePart: Any?): Result<SubmissionDto> {
        return try {
            val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val multipartFile = filePart as? MultipartBody.Part
            val response = apiService.submitAssignmentWithFile(id, contentBody, multipartFile)
            val result = handleRawResponse(response)
            Result.success(result.data ?: throw Exception("Submission failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSubmissionStatus(submissionId: Int): Flow<Result<SubmissionStatusDto>> = flow {
        try {
            val response = apiService.getSubmissionStatus(submissionId)
            val result = handleRawResponse(response)
            emit(Result.success(result.data))
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

    override fun getStudentNotices(page: Int): Flow<Result<List<NoticeDto>>> = flow {
        try {
            val response = apiService.getStudentNotices(page)
            val result = handleRawResponse(response)
            val notices = result.data
            
            // Cache notices
            dashboardDao.insertNotices(notices.map { 
                NoticeEntity(it.id, it.title, it.content, it.publishedAt, it.type, it.attachmentCount)
            })
            
            emit(Result.success(notices))
        } catch (e: Exception) {
            // Try to load from cache on failure
            val cached = dashboardDao.getNotices().firstOrNull()
            if (!cached.isNullOrEmpty()) {
                emit(Result.success(cached.map { 
                    NoticeDto(it.id, it.title, it.content, it.type, it.attachmentCount, it.publishedAt)
                }))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    override fun getNoticeDetail(id: Int): Flow<Result<NoticeDetailDto>> = flow {
        try {
            val response = apiService.getNoticeDetail(id)
            val result = handleRawResponse(response)
            emit(Result.success(result.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getNoticesByType(type: String, page: Int): Flow<Result<List<NoticeDto>>> = flow {
        try {
            val response = apiService.getNoticesByType(type, page)
            val result = handleRawResponse(response)
            emit(Result.success(result.data))
        } catch (e: Exception) {
            emit(Result.failure(e))
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

    override fun getDownloadFile(id: Int): Flow<Result<DownloadFile>> = flow {
        try {
            val response = apiService.getDownloadFile(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.data))
            } else {
                emit(Result.failure(Exception("Failed to fetch download file URL")))
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

    override fun getStudentFees(): Flow<Result<FeesResponse>> = flow {
        try {
            val response = apiService.getStudentFees()
            emit(Result.success(handleRawResponse(response)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
