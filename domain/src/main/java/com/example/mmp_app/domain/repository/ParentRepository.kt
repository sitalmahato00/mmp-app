package com.example.mmp_app.domain.repository

import com.example.mmp_app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ParentRepository {
    fun getDashboard(): Flow<Result<ParentDashboardDto>>
    fun getChildren(): Flow<Result<List<ChildDetailDto>>>
    fun getChildDetail(childId: Int): Flow<Result<ChildDetailDto>>
    fun getChildAttendance(childId: Int): Flow<Result<List<ParentAttendanceRecordDto>>>
    fun getChildAttendanceSummary(childId: Int): Flow<Result<ParentAttendanceSummaryDto>>
    fun getChildMarks(childId: Int): Flow<Result<List<ParentMarkRecordDto>>>
    fun getChildMarksSummary(childId: Int): Flow<Result<ParentMarksSummaryDto>>
    fun getChildAssignments(childId: Int): Flow<Result<List<ParentAssignmentDto>>>
    fun getChildAssignmentDetail(childId: Int, assignmentId: Int): Flow<Result<ParentAssignmentDto>>
    fun getChildTimetable(childId: Int): Flow<Result<ParentTimetableDto>>
    fun getNotices(): Flow<Result<List<ParentNoticeDto>>>
    fun getNoticeDetail(noticeId: Int): Flow<Result<ParentNoticeDto>>
    fun getProfile(): Flow<Result<ParentProfileDto>>
    suspend fun updateProfile(request: UpdateParentProfileRequest): Result<ParentProfileDto>
}
