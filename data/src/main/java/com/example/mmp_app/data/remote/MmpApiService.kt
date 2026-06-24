package com.example.mmp_app.data.remote

import com.example.mmp_app.domain.model.*
import kotlinx.serialization.json.JsonElement

import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface MmpApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<BaseResponse<JsonElement>>

    @POST("auth/login")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): Response<BaseResponse<LoginResponse>>

    // Student Endpoints
    @GET("v1/student/dashboard")
    suspend fun getStudentDashboard(): Response<BaseResponse<StudentDashboardDto>>

    @GET("v1/student/attendance/summary")
    suspend fun getStudentAttendanceSummary(): Response<BaseResponse<AttendanceSummaryDto>>

    @GET("v1/student/attendance/detail")
    suspend fun getStudentAttendanceDetail(@Query("page") page: Int = 1): Response<BaseResponse<List<AttendanceDto>>>

    @GET("v1/student/attendance/by-subject/{subjectId}")
    suspend fun getAttendanceBySubject(@Path("subjectId") subjectId: Int): Response<BaseResponse<AttendanceBySubjectDto>>

    @GET("v1/student/subjects")
    suspend fun getStudentSubjects(): Response<BaseResponse<List<SubjectDto>>>

    @GET("v1/student/subjects")
    suspend fun getSubjects(): Response<SubjectsResponse>

    @GET("v1/student/subjects/{id}")
    suspend fun getSubjectDetail(@Path("id") id: Int): Response<SubjectDetailResponse>

    @GET("v1/student/downloads")
    suspend fun getDownloads(
        @Query("subject_id") subjectId: Int? = null
    ): Response<DownloadsResponse>

    @GET("v1/student/marks/summary")
    suspend fun getStudentMarksSummary(): Response<BaseResponse<List<ExamSummaryDto>>>

    @GET("v1/student/marks/exam/{examId}")
    suspend fun getMarksByExam(@Path("examId") examId: String): Response<BaseResponse<ExamDetailDto>>

    @GET("v1/student/marks/subject/{subjectId}")
    suspend fun getMarksBySubject(@Path("subjectId") subjectId: Int): Response<BaseResponse<SubjectMarkDto>>

    @GET("v1/student/marksheet")
    suspend fun getMarksheet(): Response<BaseResponse<MarksheetDto>>

    @GET("v1/student/assignments")
    suspend fun getStudentAssignments(@Query("page") page: Int = 1): Response<AssignmentsResponse>

    @GET("v1/student/assignments/{id}")
    suspend fun getAssignmentDetail(@Path("id") id: Int): Response<AssignmentDetailResponse>

    @POST("v1/student/assignments/{id}/submit")
    suspend fun submitAssignment(
        @Path("id") id: Int,
        @Body request: Map<String, String>
    ): Response<SubmitResponse>

    @Multipart
    @POST("v1/student/assignments/{id}/submit")
    suspend fun submitAssignmentWithFile(
        @Path("id") id: Int,
        @Part("content") content: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Response<SubmitResponse>

    @GET("v1/student/assignments/{submissionId}/submission-status")
    suspend fun getSubmissionStatus(@Path("submissionId") submissionId: Int): Response<SubmissionStatusResponse>

    @GET("v1/student/timetable")
    suspend fun getStudentTimetable(): Response<BaseResponse<List<ClassDto>>>

    @GET("v1/student/notices")
    suspend fun getStudentNotices(@Query("page") page: Int = 1): Response<BaseResponse<List<NoticeDto>>>

    // Teacher Endpoints
    @GET("v1/teacher/dashboard")
    suspend fun getTeacherDashboard(): Response<BaseResponse<TeacherDashboardDto>>

    @POST("v1/teacher/attendance/bulk-mark")
    suspend fun recordAttendance(@Body body: AttendanceRecordRequest): Response<BaseResponse<Unit>>

    @POST("v1/teacher/marks/submit")
    suspend fun recordMarks(@Body body: MarkRecordRequest): Response<BaseResponse<Unit>>

    @GET("v1/teacher/students")
    suspend fun getClassStudents(@Path("classId") classId: Int): Response<BaseResponse<List<UserDto>>>

    // Parent Endpoints
    @GET("v1/parent/dashboard")
    suspend fun getParentDashboard(): Response<BaseResponse<ParentDashboardDto>>

    @GET("v1/parent/child/{childId}/attendance/summary")
    suspend fun getChildAttendanceSummary(@Path("childId") childId: Int): Response<BaseResponse<AttendanceSummaryDto>>

    @GET("v1/parent/child/{childId}/dashboard")
    suspend fun getChildDashboard(@Path("childId") childId: Int): Response<BaseResponse<StudentDashboardDto>>

    @GET("v1/student/fees")
    suspend fun getStudentFees(): Response<FeesResponse>
}
