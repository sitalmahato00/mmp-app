package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.data.remote.MmpApiService
import com.example.mmp_app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(private val api: MmpApiService) : ViewModel() {

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects = _subjects.asStateFlow()

    private val _subjectMeta = MutableStateFlow<SubjectMeta?>(null)
    val subjectMeta = _subjectMeta.asStateFlow()

    private val _subjectDetail = MutableStateFlow<SubjectDetail?>(null)
    val subjectDetail = _subjectDetail.asStateFlow()

    private val _documents = MutableStateFlow<List<SubjectDocument>>(emptyList())
    val documents = _documents.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadSubjects() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = api.getSubjects()
                if (res.isSuccessful && res.body()?.success == true) {
                    _subjects.value = res.body()!!.data
                    _subjectMeta.value = res.body()!!.meta
                } else {
                    _error.value = "Failed to load subjects"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSubjectDetail(subjectId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val res = api.getSubjectDetail(subjectId)
                if (res.isSuccessful && res.body()?.success == true) {
                    _subjectDetail.value = res.body()!!.data
                } else {
                    _error.value = "Failed to load subject detail"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadDocuments(subjectId: Int) {
        viewModelScope.launch {
            try {
                val res = api.getDownloads(subjectId = subjectId)
                if (res.isSuccessful && res.body()?.success == true) {
                    _documents.value = res.body()!!.data
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
