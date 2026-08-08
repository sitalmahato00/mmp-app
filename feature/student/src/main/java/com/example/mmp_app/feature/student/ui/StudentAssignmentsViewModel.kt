package com.example.mmp_app.feature.student.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class StudentAssignmentsViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _assignments = MutableStateFlow<UiState<StudentAssignmentsResponse>>(UiState.Loading)
    val assignments: StateFlow<UiState<StudentAssignmentsResponse>> = _assignments.asStateFlow()

    private val _assignmentDetail = MutableStateFlow<UiState<StudentAssignmentDetailDto>>(UiState.Loading)
    val assignmentDetail: StateFlow<UiState<StudentAssignmentDetailDto>> = _assignmentDetail.asStateFlow()

    private val _submissionStatus = MutableStateFlow<UiState<SubmissionStatusDto>>(UiState.Loading)
    val submissionStatus: StateFlow<UiState<SubmissionStatusDto>> = _submissionStatus.asStateFlow()

    fun loadAssignments(page: Int = 1) {
        viewModelScope.launch {
            _assignments.value = UiState.Loading
            repository.getStudentAssignmentsList(page).collect { result ->
                result.onSuccess {
                    _assignments.value = UiState.Success(it)
                }.onFailure {
                    _assignments.value = UiState.Error(it.message ?: "Failed to load assignments")
                }
            }
        }
    }

    fun loadAssignmentDetail(id: Int) {
        viewModelScope.launch {
            _assignmentDetail.value = UiState.Loading
            repository.getStudentAssignmentDetail(id).collect { result ->
                result.onSuccess {
                    _assignmentDetail.value = UiState.Success(it)
                }.onFailure {
                    _assignmentDetail.value = UiState.Error(it.message ?: "Failed to load details")
                }
            }
        }
    }

    fun loadSubmissionStatus(submissionId: Int) {
        viewModelScope.launch {
            _submissionStatus.value = UiState.Loading
            repository.getStudentSubmissionStatus(submissionId).collect { result ->
                result.onSuccess {
                    _submissionStatus.value = UiState.Success(it)
                }.onFailure {
                    _submissionStatus.value = UiState.Error(it.message ?: "Failed to load submission status")
                }
            }
        }
    }

    fun submitAssignment(
        assignmentId: Int,
        note: String?,
        fileUri: Uri?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var filePart: MultipartBody.Part? = null
                if (fileUri != null) {
                    val inputStream = context.contentResolver.openInputStream(fileUri)
                    val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
                    val fileName = getFileName(context, fileUri)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()

                    if (bytes.size > 10 * 1024 * 1024) {
                        onError("File size exceeds 10 MB limit")
                        return@launch
                    }

                    val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    filePart = MultipartBody.Part.createFormData("attachment", fileName, requestFile)
                }

                val result = repository.submitStudentAssignment(assignmentId, note, filePart)
                result.onSuccess {
                    onSuccess()
                }.onFailure {
                    onError(it.message ?: "Submission failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "file"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
