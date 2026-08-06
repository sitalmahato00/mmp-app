package com.example.mmp_app.feature.teacher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentsUiState>(StudentsUiState.Loading)
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Int?>(null)
    val selectedSubjectId: StateFlow<Int?> = _selectedSubjectId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allStudentsMap = MutableStateFlow<Map<Int, List<StudentItemDto>>>(emptyMap())
    private val _subjects = MutableStateFlow<List<TeacherSubjectDto>>(emptyList())
    val subjects: StateFlow<List<TeacherSubjectDto>> = _subjects.asStateFlow()

    val displayStudents: StateFlow<List<StudentItemDto>> = combine(
        _allStudentsMap, _selectedSubjectId, _searchQuery
    ) { allMap, selectedId, query ->
        val base = if (selectedId == null) {
            allMap.values.flatten().distinctBy { it.id }
        } else {
            allMap[selectedId] ?: emptyList()
        }
        
        if (query.isBlank()) {
            base
        } else {
            base.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.studentNo?.contains(query, ignoreCase = true) == true ||
                it.email?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = StudentsUiState.Loading
            repository.getTeacherClasses().collect { result ->
                result.onSuccess { subjectsList ->
                    _subjects.value = subjectsList
                    
                    val jobs = subjectsList.map { subject ->
                        async {
                            repository.getTeacherStudentsBySubject(subject.id).first().onSuccess { response ->
                                _allStudentsMap.update { it + (subject.id to response.students) }
                            }
                        }
                    }
                    jobs.awaitAll()
                    _uiState.value = StudentsUiState.Success
                }.onFailure {
                    _uiState.value = StudentsUiState.Error(it.message ?: "Failed to load subjects")
                }
            }
        }
    }

    fun selectSubject(subjectId: Int?) {
        _selectedSubjectId.value = subjectId
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }
}

sealed class StudentsUiState {
    object Loading : StudentsUiState()
    object Success : StudentsUiState()
    data class Error(val message: String) : StudentsUiState()
}
