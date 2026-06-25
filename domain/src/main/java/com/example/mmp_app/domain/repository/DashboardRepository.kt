package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getStudentDashboard(): Flow<Result<StudentDashboardDto>>
    fun getTeacherDashboard(): Flow<Result<TeacherDashboardDto>>
    fun getParentDashboard(): Flow<Result<ParentDashboardDto>>
    fun getStudentMarks(): Flow<Result<List<MarkDto>>>
    fun getStudentMarksSummary(): Flow<Result<MarksSummaryDto>>
    fun getMarksByExam(examId: String): Flow<Result<ExamDetailDto>>
    fun getMarksBySubject(subjectId: Int): Flow<Result<SubjectMarkDto>>
    fun getMarksheet(examId: String? = null): Flow<Result<MarksheetDto>>
    fun getStudentAssignments(): Flow<Result<List<AssignmentDto>>>
    fun getAssignmentDetail(id: Int): Flow<Result<AssignmentDetailDto>>
    suspend fun submitAssignment(id: Int, content: String?): Result<SubmissionDto>
    suspend fun submitAssignmentWithFile(id: Int, content: String?, filePart: Any?): Result<SubmissionDto>
    fun getSubmissionStatus(submissionId: Int): Flow<Result<SubmissionStatusDto>>
    fun getStudentAttendance(): Flow<Result<List<AttendanceDto>>>
    fun getStudentAttendanceSummary(): Flow<Result<AttendanceSummaryDto>>
    fun getStudentAttendanceBySubject(subjectId: Int): Flow<Result<AttendanceBySubjectDto>>
    fun getStudentSubjects(): Flow<Result<List<SubjectDto>>>
    fun getStudentTimetable(): Flow<Result<List<ClassDto>>>
    fun getStudentNotices(page: Int = 1): Flow<Result<List<NoticeDto>>>
    fun getNoticeDetail(id: Int): Flow<Result<NoticeDetailDto>>
    fun getNoticesByType(type: String, page: Int = 1): Flow<Result<List<NoticeDto>>>
    fun getStudentDownloads(subjectId: Int? = null): Flow<Result<List<SubjectDocument>>>
    fun getDownloadFile(id: Int): Flow<Result<DownloadFile>>
    suspend fun recordAttendance(request: AttendanceRecordRequest): Result<Unit>
    suspend fun recordMarks(request: MarkRecordRequest): Result<Unit>
    suspend fun getClassStudents(classId: Int): Result<List<UserDto>>
    fun getChildDashboard(childId: Int): Flow<Result<StudentDashboardDto>>
    fun getStudentFees(): Flow<Result<FeesResponse>>
}
