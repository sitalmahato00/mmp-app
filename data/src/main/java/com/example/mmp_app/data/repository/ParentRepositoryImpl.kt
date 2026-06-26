package com.example.mmp_app.data.repository

import com.example.mmp_app.data.remote.ParentApiService
import com.example.mmp_app.data.remote.exception.handleApiResponse
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.ParentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParentRepositoryImpl @Inject constructor(
    private val apiService: ParentApiService,
    private val json: Json
) : ParentRepository {

    override fun getDashboard(): Flow<Result<ParentDashboardDto>> = flow {
        try {
            val response = apiService.getDashboard()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildren(): Flow<Result<List<ChildDetailDto>>> = flow {
        try {
            val response = apiService.getChildren()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildDetail(childId: Int): Flow<Result<ChildDetailDto>> = flow {
        try {
            val response = apiService.getChildDetail(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildAttendance(childId: Int): Flow<Result<List<ParentAttendanceRecordDto>>> = flow {
        try {
            val response = apiService.getChildAttendance(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildAttendanceSummary(childId: Int): Flow<Result<ParentAttendanceSummaryDto>> = flow {
        try {
            val response = apiService.getChildAttendanceSummary(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildMarks(childId: Int): Flow<Result<List<ParentMarkRecordDto>>> = flow {
        try {
            val response = apiService.getChildMarks(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildMarksSummary(childId: Int): Flow<Result<ParentMarksSummaryDto>> = flow {
        try {
            val response = apiService.getChildMarksSummary(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildAssignments(childId: Int): Flow<Result<List<ParentAssignmentDto>>> = flow {
        try {
            val response = apiService.getChildAssignments(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildAssignmentDetail(childId: Int, assignmentId: Int): Flow<Result<ParentAssignmentDto>> = flow {
        try {
            val response = apiService.getChildAssignmentDetail(childId, assignmentId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getChildTimetable(childId: Int): Flow<Result<ParentTimetableDto>> = flow {
        try {
            val response = apiService.getChildTimetable(childId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getNotices(): Flow<Result<List<ParentNoticeDto>>> = flow {
        try {
            val response = apiService.getNotices()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getNoticeDetail(noticeId: Int): Flow<Result<ParentNoticeDto>> = flow {
        try {
            val response = apiService.getNoticeDetail(noticeId)
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getProfile(): Flow<Result<ParentProfileDto>> = flow {
        try {
            val response = apiService.getProfile()
            emit(Result.success(handleApiResponse(response, json)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun updateProfile(request: UpdateParentProfileRequest): Result<ParentProfileDto> {
        return try {
            val response = apiService.updateProfile(request)
            Result.success(handleApiResponse(response, json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
