package com.example.mmp_app.data.remote

import com.example.mmp_app.data.remote.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MmpApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<BaseResponse<OtpResponse>>

    @POST("auth/login")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): Response<BaseResponse<LoginResponse>>

    @GET("v1/student/dashboard")
    suspend fun getStudentDashboard(): Response<BaseResponse<StudentDashboardDto>>

    @GET("v1/teacher/dashboard")
    suspend fun getTeacherDashboard(): Response<BaseResponse<TeacherDashboardDto>>

    @GET("v1/parent/dashboard")
    suspend fun getParentDashboard(): Response<BaseResponse<ParentDashboardDto>>

    @GET("v1/hod/dashboard")
    suspend fun getHodDashboard(): Response<BaseResponse<HodDashboardDto>>
}
