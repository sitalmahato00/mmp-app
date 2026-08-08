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

    @GET("v1/student/downloads/{id}/file")
    suspend fun getDownloadFile(@Path("id") id: Int): Response<DownloadFileResponse>

    @GET("v1/student/marks/summary")
    suspend fun getStudentMarksSummary(): Response<BaseResponse<List<ExamSummaryDto>>>

    @GET("v1/student/marks/exam/{examId}")
    suspend fun getMarksByExam(@Path("examId") examId: Int): Response<BaseResponse<ExamDetailDto>>

    @GET("v1/student/marks/subject/{subjectId}")
    suspend fun getMarksBySubject(@Path("subjectId") subjectId: Int): Response<BaseResponse<SubjectMarkDto>>

    @GET("v1/student/marks/marksheet")
    suspend fun getMarksheet(
        @Query("exam_id") examId: Int? = null
    ): Response<BaseResponse<MarksheetDto>>

    @GET("v1/student/assignments")
    suspend fun getStudentAssignmentsList(@Query("page") page: Int = 1): Response<StudentAssignmentsResponse>

    @GET("v1/student/assignments/{id}")
    suspend fun getStudentAssignmentDetail(@Path("id") id: Int): Response<StudentAssignmentDetailResponse>

    @Multipart
    @POST("v1/student/assignments/{id}/submit")
    suspend fun submitStudentAssignment(
        @Path("id") assignmentId: Int,
        @Part("student_note") note: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): Response<SubmitResponse>

    @GET("v1/student/assignments/{submissionId}/submission-status")
    suspend fun getStudentSubmissionStatus(@Path("submissionId") submissionId: Int): Response<SubmissionStatusResponse>

    @GET("v1/student/timetable")
    suspend fun getTimetable(): Response<BaseResponse<TimetableData>>

    @GET("v1/student/timetable/{day}")
    suspend fun getTimetableByDay(@Path("day") day: String): Response<BaseResponse<DaySchedule>>

    @GET("v1/student/notices")
    suspend fun getStudentNotices(@Query("page") page: Int = 1): Response<NoticesResponse>

    @GET("v1/student/notices/{id}")
    suspend fun getNoticeDetail(@Path("id") id: Int): Response<NoticeDetailResponse>

    @GET("v1/student/notices/filter/{type}")
    suspend fun getNoticesByType(@Path("type") type: String, @Query("page") page: Int = 1): Response<NoticesResponse>

    // Teacher Endpoints
    @GET("v1/teacher/dashboard")
    suspend fun getTeacherDashboard(): Response<BaseResponse<TeacherDashboardDto>>

    @GET("v1/teacher/profile")
    suspend fun getTeacherProfile(): Response<BaseResponse<TeacherProfileDto>>

    @GET("v1/teacher/today-schedule")
    suspend fun getTeacherTodaySchedule(): Response<BaseResponse<TodayScheduleDto>>

    @GET("v1/teacher/classes")
    suspend fun getTeacherClasses(): Response<BaseResponse<List<TeacherSubjectDto>>>

    @GET("v1/teacher/students/{subjectId}")
    suspend fun getTeacherStudentsBySubject(@Path("subjectId") subjectId: Int): Response<BaseResponse<TeacherStudentsResponseDto>>

    @GET("v1/teacher/marks/components/{subjectId}")
    suspend fun getMarkComponents(@Path("subjectId") subjectId: Int): Response<BaseResponse<MarkComponentsDto>>

    @GET("v1/teacher/assignments")
    suspend fun getTeacherAssignments(): Response<AssignmentListResponse>

    @Multipart
    @POST("v1/teacher/assignments/create")
    suspend fun createAssignment(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("subject_id") subjectId: RequestBody,
        @Part("due_date") dueDate: RequestBody,
        @Part("max_marks") maxMarks: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): Response<MessageResponse>

    @PUT("v1/teacher/assignments/{id}")
    suspend fun updateAssignment(
        @Path("id") id: Int,
        @Body request: UpdateAssignmentRequest
    ): Response<MessageResponse>

    @DELETE("v1/teacher/assignments/{id}")
    suspend fun deleteAssignment(@Path("id") id: Int): Response<MessageResponse>

    @GET("v1/teacher/assignments/{id}/submissions")
    suspend fun getSubmissions(@Path("id") id: Int): Response<SubmissionsResponse>

    @POST("v1/teacher/assignments/{submission}/grade")
    suspend fun gradeSubmission(
        @Path("submission") submissionId: Int,
        @Body request: GradeRequest
    ): Response<MessageResponse>

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

    // Notification Endpoints
    @GET("v1/notifications")
    suspend fun getNotifications(
        @Query("filter") filter: String = "all",
        @Query("per_page") perPage: Int = 15,
        @Query("page") page: Int = 1
    ): Response<BaseResponse<NotificationListData>>

    @GET("v1/notifications/unread-count")
    suspend fun getUnreadCount(): Response<BaseResponse<UnreadCountData>>

    @POST("v1/notifications/mark-all-read")
    suspend fun markAllRead(): Response<BaseResponse<UnreadCountData>>

    @POST("v1/notifications/{id}/mark-read")
    suspend fun markRead(@Path("id") id: String): Response<BaseResponse<MarkReadData>>

    @DELETE("v1/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<BaseResponse<Unit>>

    // User/Settings Endpoints
    @GET("v1/user")
    suspend fun getCurrentUser(): Response<BaseResponse<UserData>>

    @Multipart
    @POST("v1/user/profile")
    suspend fun updateProfile(
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part avatar: MultipartBody.Part?,
        @Query("_method") method: String = "PUT"
    ): Response<BaseResponse<UserData>>

    @PUT("v1/user/profile")
    suspend fun updateProfileJson(
        @Body body: UpdateProfileRequest
    ): Response<BaseResponse<UserData>>

    @POST("v1/user/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<BaseResponse<TokenData>>

    @PUT("v1/user/notification-preferences")
    suspend fun updateNotificationPreferences(
        @Body body: NotificationPreferencesRequest
    ): Response<BaseResponse<NotificationPreferencesData>>

    @PUT("v1/user/two-factor")
    suspend fun updateTwoFactor(
        @Body body: TwoFactorRequest
    ): Response<BaseResponse<TwoFactorData>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(): Response<BaseResponse<TokenData>>
}
