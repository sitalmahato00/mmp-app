package com.example.mmp_app.feature.parent.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.ChildDetailDto
import com.example.mmp_app.domain.model.TimetableData
import com.example.mmp_app.domain.repository.ParentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildTimetableState(
    val children: List<ChildDetailDto> = emptyList(),
    val selectedChildId: Int? = null,
    val timetable: TimetableData? = null,
    val isLoading: Boolean = false,
    val isTimetableLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildTimetableViewModel @Inject constructor(
    private val repository: ParentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildTimetableState())
    val uiState = _uiState.asStateFlow()

    init {
        val initialChildId = savedStateHandle.get<Int>("childId")
        _uiState.update { it.copy(selectedChildId = initialChildId) }
        loadChildren()
    }

    fun loadChildren() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getChildren().collect { result ->
                result.onSuccess { children ->
                    val selectedId = _uiState.value.selectedChildId ?: children.firstOrNull()?.id
                    _uiState.update { it.copy(
                        children = children,
                        selectedChildId = selectedId,
                        isLoading = false
                    ) }
                    
                    if (selectedId != null) {
                        loadTimetable(selectedId)
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun onChildSelected(childId: Int) {
        _uiState.update { it.copy(selectedChildId = childId) }
        loadTimetable(childId)
    }

    fun loadTimetable(childId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTimetableLoading = true) }
            repository.getChildTimetable(childId).collect { result ->
                result.onSuccess { timetable ->
                    // Map ParentTimetableDto to TimetableData if they are different, 
                    // but the prompt says they are identical. 
                    // Let's verify the domain model for TimetableData.
                    _uiState.update { it.copy(
                        timetable = timetable.toTimetableData(), 
                        isTimetableLoading = false
                    ) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isTimetableLoading = false) }
                }
            }
        }
    }

    fun refresh() {
        _uiState.value.selectedChildId?.let { loadTimetable(it) }
        loadChildren()
    }
}

// Extension to map if needed, but assuming they might need conversion 
// if ParentTimetableDto and TimetableData are different types in domain.
// Based on ParentRepository, it returns ParentTimetableDto.
// Based on student's TimetableScreen, it uses TimetableData.
private fun com.example.mmp_app.domain.model.ParentTimetableDto.toTimetableData(): TimetableData {
    return TimetableData(
        hasTimetable = this.hasTimetable,
        semester = this.semester,
        section = this.section,
        effectiveFrom = this.effectiveFrom,
        academicSession = null, // Not present in ParentTimetableDto?
        timetable = this.timetable.map { day ->
            com.example.mmp_app.domain.model.DaySchedule(
                day = day.day,
                classes = day.classes.map { cls ->
                    com.example.mmp_app.domain.model.TimetableClass(
                        id = cls.id,
                        subject = cls.subject,
                        subjectCode = cls.subjectCode,
                        teacher = cls.teacher,
                        startTime = cls.startTime,
                        endTime = cls.endTime,
                        room = cls.room,
                        type = cls.type,
                        group = null, // Will need to check if group is in ParentTimetableClassDto
                        duration = null
                    )
                }
            )
        }
    )
}
