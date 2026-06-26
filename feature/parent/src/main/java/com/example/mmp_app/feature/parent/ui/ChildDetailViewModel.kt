package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildDetailState(
    val childId: Int = 0,
    val childDetail: ChildDetailDto? = null,
    val attendanceSummary: ParentAttendanceSummaryDto? = null,
    val attendanceRecords: List<ParentAttendanceRecordDto> = emptyList(),
    val marksSummary: ParentMarksSummaryDto? = null,
    val marksList: List<ParentMarkRecordDto> = emptyList(),
    val assignments: List<ParentAssignmentDto> = emptyList(),
    val timetable: ParentTimetableDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildDetailViewModel @Inject constructor(
    private val repository: ParentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildDetailState())
    val uiState = _uiState.asStateFlow()

    private var childId: Int = savedStateHandle.get<Int>("childId") ?: 0

    init {
        if (childId != 0) {
            _uiState.update { it.copy(childId = childId) }
            loadAllData()
        }
    }

    fun initChildId(id: Int) {
        if (childId == 0) {
            childId = id
            _uiState.update { it.copy(childId = id) }
            loadAllData()
        }
    }

    fun loadAllData() {
        if (childId == 0) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val detailJob = async { repository.getChildDetail(childId).collectLatest { res -> res.onSuccess { d -> _uiState.update { it.copy(childDetail = d) } } } }
                val attSummaryJob = async { repository.getChildAttendanceSummary(childId).collectLatest { res -> res.onSuccess { s -> _uiState.update { it.copy(attendanceSummary = s) } } } }
                val attRecordsJob = async { repository.getChildAttendance(childId).collectLatest { res -> res.onSuccess { r -> _uiState.update { it.copy(attendanceRecords = r) } } } }
                val marksSummaryJob = async { repository.getChildMarksSummary(childId).collectLatest { res -> res.onSuccess { s -> _uiState.update { it.copy(marksSummary = s) } } } }
                val marksListJob = async { repository.getChildMarks(childId).collectLatest { res -> res.onSuccess { l -> _uiState.update { it.copy(marksList = l) } } } }
                val assignmentsJob = async { repository.getChildAssignments(childId).collectLatest { res -> res.onSuccess { a -> _uiState.update { it.copy(assignments = a) } } } }
                val timetableJob = async { repository.getChildTimetable(childId).collectLatest { res -> res.onSuccess { t -> _uiState.update { it.copy(timetable = t) } } } }

                detailJob.await()
                attSummaryJob.await()
                attRecordsJob.await()
                marksSummaryJob.await()
                marksListJob.await()
                assignmentsJob.await()
                timetableJob.await()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
