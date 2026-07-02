package com.example.mmp_app.data.remote

import com.example.mmp_app.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface ParentApiService {
    @GET("api/v1/parent/dashboard")
    suspend fun getDashboard(): Response<BaseResponse<ParentDashboardDto>>

    @GET("api/v1/parent/children")
    suspend fun getChildren(): Response<BaseResponse<List<ChildDetailDto>>>

    @GET("api/v1/parent/children/{childId}")
    suspend fun getChildDetail(@Path("childId") childId: Int): Response<BaseResponse<ChildDetailDto>>

    @GET("api/v1/parent/child/{childId}/attendance")
    suspend fun getChildAttendance(@Path("childId") childId: Int): Response<BaseResponse<List<ParentAttendanceRecordDto>>>

    @GET("api/v1/parent/child/{childId}/attendance/summary")
    suspend fun getChildAttendanceSummary(@Path("childId") childId: Int): Response<BaseResponse<ParentAttendanceSummaryDto>>

    @GET("api/v1/parent/child/{childId}/marks")
    suspend fun getChildMarks(@Path("childId") childId: Int): Response<BaseResponse<List<ParentMarkRecordDto>>>

    @GET("api/v1/parent/child/{childId}/marks/summary")
    suspend fun getChildMarksSummary(@Path("childId") childId: Int): Response<BaseResponse<ParentMarksSummaryDto>>

    @GET("api/v1/parent/child/{childId}/assignments")
    suspend fun getChildAssignments(@Path("childId") childId: Int): Response<BaseResponse<List<ParentAssignmentDto>>>

    @GET("api/v1/parent/child/{childId}/assignments/{assignmentId}")
    suspend fun getChildAssignmentDetail(
        @Path("childId") childId: Int,
        @Path("assignmentId") assignmentId: Int
    ): Response<BaseResponse<ParentAssignmentDto>>

    @GET("api/v1/parent/child/{childId}/timetable")
    suspend fun getChildTimetable(@Path("childId") childId: Int): Response<BaseResponse<ParentTimetableDto>>

    @GET("api/v1/parent/notices")
    suspend fun getNotices(): Response<BaseResponse<List<ParentNoticeDto>>>

    @GET("api/v1/parent/notices/{noticeId}")
    suspend fun getNoticeDetail(@Path("noticeId") noticeId: Int): Response<BaseResponse<ParentNoticeDto>>

    @GET("api/v1/parent/profile")
    suspend fun getProfile(): Response<BaseResponse<ParentProfileDto>>

    @PUT("api/v1/parent/profile")
    suspend fun updateProfile(@Body request: UpdateParentProfileRequest): Response<BaseResponse<ParentProfileDto>>

    @Multipart
    @PUT("api/v1/parent/profile")
    suspend fun updateProfileMultipart(
        @Part("name") name: okhttp3.RequestBody?,
        @Part("phone") phone: okhttp3.RequestBody?,
        @Part("address") address: okhttp3.RequestBody?,
        @Part("occupation") occupation: okhttp3.RequestBody?,
        @Part avatar: okhttp3.MultipartBody.Part?
    ): Response<BaseResponse<ParentProfileDto>>
}
