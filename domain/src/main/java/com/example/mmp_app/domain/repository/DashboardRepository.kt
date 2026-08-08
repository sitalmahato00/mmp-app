package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getStudentDashboard(): Flow<Result<StudentDashboardDto>>
    fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>>
    fun getTeacherProfile(): Flow<Result<TeacherProfileDto>>
    fun getTeacherTodaySchedule(): Flow<Result<TodayScheduleDto>>
    fun getTeacherClasses(): Flow<Result<List<TeacherSubjectDto>>>
    fun getTeacherStudentsBySubject(subjectId: Int): Flow<Result<TeacherStudentsResponseDto>>
    fun getMarkComponents(subjectId: Int): Flow<Result<MarkComponentsDto>>
    fun getTeacherAssignmentsList(): Flow<Result<AssignmentListResponse>>
    suspend fun createTeacherAssignment(
        title: String,
        description: String?,
        subjectId: Int,
        dueDate: String,
        maxMarks: Double?,
        attachment: Any? // Could be MultipartBody.Part or Uri or File
    ): Result<MessageResponse>
    suspend fun updateTeacherAssignment(id: Int, request: UpdateAssignmentRequest): Result<MessageResponse>
    suspend fun deleteTeacherAssignment(id: Int): Result<MessageResponse>
    fun getAssignmentSubmissions(id: Int): Flow<Result<SubmissionsResponse>>
    suspend fun gradeAssignmentSubmission(submissionId: Int, request: GradeRequest): Result<MessageResponse>
    fun getParentDashboard(): Flow<Result<ParentDashboardDto>>
    fun getStudentMarks(): Flow<Result<List<MarkDto>>>
    fun getStudentMarksSummary(): Flow<Result<MarksSummaryDto>>
    fun getMarksByExam(examId: Int): Flow<Result<ExamDetailDto>>
    fun getMarksBySubject(subjectId: Int): Flow<Result<SubjectMarkDto>>
    fun getMarksheet(examId: Int? = null): Flow<Result<MarksheetDto>>
    fun getStudentAssignmentsList(page: Int = 1): Flow<Result<StudentAssignmentsResponse>>
    fun getStudentAssignmentDetail(id: Int): Flow<Result<StudentAssignmentDetailDto>>
    suspend fun submitStudentAssignment(id: Int, note: String?, attachment: Any?): Result<SubmitResponse>
    fun getStudentSubmissionStatus(submissionId: Int): Flow<Result<SubmissionStatusDto>>
    fun getStudentAttendance(): Flow<Result<List<AttendanceDto>>>
    fun getStudentAttendanceSummary(): Flow<Result<AttendanceSummaryDto>>
    fun getStudentAttendanceBySubject(subjectId: Int): Flow<Result<AttendanceBySubjectDto>>
    fun getStudentSubjects(): Flow<Result<List<SubjectDto>>>
    fun getTimetable(): Flow<Result<TimetableData>>
    fun getTimetableByDay(day: String): Flow<Result<DaySchedule>>
    fun getStudentNotices(page: Int = 1): Flow<Result<List<NoticeDto>>>
    fun getNoticeDetail(id: Int): Flow<Result<NoticeDetailDto>>
    fun getNoticesByType(type: String, page: Int = 1): Flow<Result<List<NoticeDto>>>
    fun getStudentDownloads(subjectId: Int? = null): Flow<Result<List<SubjectDocument>>>
    fun getDownloadFile(id: Int): Flow<Result<DownloadFile>>
    suspend fun recordAttendance(request: AttendanceRecordRequest): Result<Unit>
    suspend fun recordMarks(request: MarkRecordRequest): Result<Unit>
    suspend fun getClassStudents(classId: Int): Result<List<UserDto>>
    fun getChildDashboard(childId: Int): Flow<Result<StudentDashboardDto>>
}
