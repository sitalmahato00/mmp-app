package com.example.mmp_app.data.remote

import com.example.mmp_app.data.remote.model.*
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("v1/student/marks/summary")
    suspend fun getStudentMarksSummary(): Response<BaseResponse<MarksSummaryDto>>

    @GET("v1/student/marks/exam/{examId}")
    suspend fun getMarksByExam(@Path("examId") examId: Int): Response<BaseResponse<ExamDetailDto>>

    @GET("v1/student/marks/subject/{subjectId}")
    suspend fun getMarksBySubject(@Path("subjectId") subjectId: Int): Response<BaseResponse<SubjectMarkDto>>

    @GET("v1/student/marksheet")
    suspend fun getMarksheet(): Response<BaseResponse<MarksheetDto>>

    @GET("v1/student/assignments")
    suspend fun getStudentAssignments(@Query("page") page: Int = 1): Response<BaseResponse<List<AssignmentDto>>>

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
}
