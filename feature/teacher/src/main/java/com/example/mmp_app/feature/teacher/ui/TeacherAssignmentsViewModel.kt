package com.example.mmp_app.feature.teacher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherAssignmentsViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _assignmentsState = MutableStateFlow<UiState<AssignmentListResponse>>(UiState.Loading)
    val assignmentsState = _assignmentsState.asStateFlow()

    private val _subjects = MutableStateFlow<List<TeacherSubjectDto>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _submissionsState = MutableStateFlow<UiState<SubmissionsDataDto>>(UiState.Loading)
    val submissionsState = _submissionsState.asStateFlow()

    // Track which submission's grade form is expanded
    private val _expandedGradeForms = MutableStateFlow<Set<Int>>(emptySet())
    val expandedGradeForms = _expandedGradeForms.asStateFlow()

    init {
        loadAssignments()
        loadSubjects()
    }

    fun toggleGradeForm(submissionId: Int) {
        _expandedGradeForms.value = if (_expandedGradeForms.value.contains(submissionId)) {
            _expandedGradeForms.value - submissionId
        } else {
            _expandedGradeForms.value + submissionId
        }
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _assignmentsState.value = UiState.Loading
            repository.getTeacherAssignmentsList().collect { result ->
                result.onSuccess {
                    _assignmentsState.value = UiState.Success(it)
                }.onFailure {
                    _assignmentsState.value = UiState.Error(it.message ?: "Failed to load assignments")
                }
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            repository.getTeacherClasses().collect { result ->
                result.onSuccess { _subjects.value = it }
            }
        }
    }

    fun createAssignment(
        title: String,
        description: String?,
        subjectId: Int,
        dueDate: String,
        maxMarks: Double?,
        attachment: Any?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createTeacherAssignment(title, description, subjectId, dueDate, maxMarks, attachment)
            result.onSuccess {
                loadAssignments()
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Failed to create assignment")
            }
        }
    }

    fun deleteAssignment(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteTeacherAssignment(id)
            result.onSuccess {
                loadAssignments()
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Failed to delete assignment")
            }
        }
    }

    fun loadSubmissions(assignmentId: Int) {
        viewModelScope.launch {
            _submissionsState.value = UiState.Loading
            repository.getAssignmentSubmissions(assignmentId).collect { result ->
                result.onSuccess {
                    _submissionsState.value = UiState.Success(it.data)
                }.onFailure {
                    _submissionsState.value = UiState.Error(it.message ?: "Failed to load submissions")
                }
            }
        }
    }

    fun gradeSubmission(submissionId: Int, marks: Double, feedback: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val request = GradeRequest(marks, feedback)
            val result = repository.gradeAssignmentSubmission(submissionId, request)
            result.onSuccess {
                // Update local state if we have a successful submission list
                val currentState = _submissionsState.value
                if (currentState is UiState.Success) {
                    val updatedSubmissions = currentState.data.submissions.map {
                        if (it.id == submissionId) {
                            it.copy(
                                status = "graded",
                                marksObtained = marks,
                                teacherFeedback = feedback
                            )
                        } else it
                    }
                    _submissionsState.value = UiState.Success(
                        currentState.data.copy(submissions = updatedSubmissions)
                    )
                }
                // Collapse the form
                _expandedGradeForms.value = _expandedGradeForms.value - submissionId
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Failed to grade submission")
            }
        }
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
