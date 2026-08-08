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
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>> = flow {
        try {
            val response = apiService.getTeacherDashboard()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTeacherProfile(): Flow<Result<TeacherProfileDto>> = flow {
        try {
            val response = apiService.getTeacherProfile()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTeacherTodaySchedule(): Flow<Result<TodayScheduleDto>> = flow {
        try {
            val response = apiService.getTeacherTodaySchedule()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTeacherClasses(): Flow<Result<List<TeacherSubjectDto>>> = flow {
        try {
            val response = apiService.getTeacherClasses()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTeacherStudentsBySubject(subjectId: Int): Flow<Result<TeacherStudentsResponseDto>> = flow {
        try {
            val response = apiService.getTeacherStudentsBySubject(subjectId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getMarkComponents(subjectId: Int): Flow<Result<MarkComponentsDto>> = flow {
        try {
            val response = apiService.getMarkComponents(subjectId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTeacherAssignmentsList(): Flow<Result<AssignmentListResponse>> = flow {
        try {
            val response = apiService.getTeacherAssignments()
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to load assignments")))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override suspend fun createTeacherAssignment(
        title: String,
        description: String?,
        subjectId: Int,
        dueDate: String,
        maxMarks: Double?,
        attachment: Any?
    ): Result<MessageResponse> {
        return try {
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val subjectPart = subjectId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val datePart = dueDate.toRequestBody("text/plain".toMediaTypeOrNull())
            val maxMarksPart = maxMarks?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = attachment as? okhttp3.MultipartBody.Part

            val response = apiService.createAssignment(
                titlePart, descPart, subjectPart, datePart, maxMarksPart, filePart
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create assignment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTeacherAssignment(id: Int, request: UpdateAssignmentRequest): Result<MessageResponse> {
        return try {
            val response = apiService.updateAssignment(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update assignment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTeacherAssignment(id: Int): Result<MessageResponse> {
        return try {
            val response = apiService.deleteAssignment(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to delete assignment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAssignmentSubmissions(id: Int): Flow<Result<SubmissionsResponse>> = flow {
        try {
            val response = apiService.getSubmissions(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to load submissions")))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override suspend fun gradeAssignmentSubmission(submissionId: Int, request: GradeRequest): Result<MessageResponse> {
        return try {
            val response = apiService.gradeSubmission(submissionId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to grade submission"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getParentDashboard(): Flow<Result<ParentDashboardDto>> = flow {
        try {
            val response = apiService.getParentDashboard()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentMarks(): Flow<Result<List<MarkDto>>> = flow {
        try {
            val examList = handleApiResponse(apiService.getStudentMarksSummary(), json)
            val marks = examList.flatMap { it.subjects }
            emit(Result.success(marks))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
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
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getMarksByExam(examId: Int): Flow<Result<ExamDetailDto>> = flow {
        try {
            val response = apiService.getMarksByExam(examId)
            val detail = handleApiResponse(response, json)
            val updatedMarks = detail.marks.map { mark ->
                val percentage = if (mark.total > 0) (mark.score / mark.total * 100) else 0f
                mark.copy(percentage = percentage)
            }
            emit(Result.success(detail.copy(marks = updatedMarks)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getMarksBySubject(subjectId: Int): Flow<Result<SubjectMarkDto>> = flow {
        try {
            val response = apiService.getMarksBySubject(subjectId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getMarksheet(examId: Int?): Flow<Result<MarksheetDto>> = flow {
        try {
            val response = apiService.getMarksheet(examId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentAssignmentsList(page: Int): Flow<Result<StudentAssignmentsResponse>> = flow {
        try {
            val response = apiService.getStudentAssignmentsList(page)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to load assignments")))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentAssignmentDetail(id: Int): Flow<Result<StudentAssignmentDetailDto>> = flow {
        try {
            val response = apiService.getStudentAssignmentDetail(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.data))
            } else {
                emit(Result.failure(Exception("Failed to load assignment detail")))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override suspend fun submitStudentAssignment(id: Int, note: String?, attachment: Any?): Result<SubmitResponse> {
        return try {
            val notePart = note?.toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = attachment as? okhttp3.MultipartBody.Part
            val response = apiService.submitStudentAssignment(id, notePart, filePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit assignment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getStudentSubmissionStatus(submissionId: Int): Flow<Result<SubmissionStatusDto>> = flow {
        try {
            val response = apiService.getStudentSubmissionStatus(submissionId)
            if (response.isSuccessful && response.body() != null) {
                emit(Result.success(response.body()!!.data))
            } else {
                emit(Result.failure(Exception("Failed to load submission status")))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentAttendance(): Flow<Result<List<AttendanceDto>>> = flow {
        try {
            val response = apiService.getStudentAttendanceDetail()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentAttendanceSummary(): Flow<Result<AttendanceSummaryDto>> = flow {
        try {
            val response = apiService.getStudentAttendanceSummary()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentAttendanceBySubject(subjectId: Int): Flow<Result<AttendanceBySubjectDto>> = flow {
        try {
            val response = apiService.getAttendanceBySubject(subjectId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getStudentSubjects(): Flow<Result<List<SubjectDto>>> = flow {
        try {
            val response = apiService.getStudentSubjects()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTimetable(): Flow<Result<TimetableData>> = flow {
        try {
            val response = apiService.getTimetable()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getTimetableByDay(day: String): Flow<Result<DaySchedule>> = flow {
        try {
            val response = apiService.getTimetableByDay(day)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
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
                if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
            }
        }
    }

    override fun getNoticeDetail(id: Int): Flow<Result<NoticeDetailDto>> = flow {
        try {
            val response = apiService.getNoticeDetail(id)
            val result = handleRawResponse(response)
            emit(Result.success(result.data))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }

    override fun getNoticesByType(type: String, page: Int): Flow<Result<List<NoticeDto>>> = flow {
        try {
            val response = apiService.getNoticesByType(type, page)
            val result = handleRawResponse(response)
            emit(Result.success(result.data))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
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
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
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
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
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
            if (e is kotlinx.coroutines.CancellationException) throw e; emit(Result.failure(e))
        }
    }
}
