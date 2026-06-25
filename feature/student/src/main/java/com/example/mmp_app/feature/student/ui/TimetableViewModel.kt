package com.example.mmp_app.feature.student.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mmp_app.domain.model.DaySchedule
import com.example.mmp_app.domain.model.TimetableClass
import com.example.mmp_app.domain.model.TimetableData
import com.example.mmp_app.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _timetableData = MutableStateFlow<TimetableData?>(null)
    val timetableData = _timetableData.asStateFlow()

    private val _todayClasses = MutableStateFlow<List<TimetableClass>>(emptyList())
    val todayClasses = _todayClasses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadFullTimetable() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getTimetable().collect { result ->
                result.onSuccess { data ->
                    _timetableData.value = data
                    
                    // If the timetable list is empty or has empty classes, we fetch individual days
                    // This fixes the issue where the main endpoint returns empty class lists
                    if (data.timetable.isEmpty() || data.timetable.all { it.classes.isEmpty() }) {
                        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                        days.forEach { dayName ->
                            launch {
                                repository.getTimetableByDay(dayName).collect { dayResult ->
                                    dayResult.onSuccess { daySchedule ->
                                        updateDayInTimetable(daySchedule)
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    _error.value = it.message ?: "Failed to load timetable"
                }
                _isLoading.value = false
            }
        }
    }

    private fun updateDayInTimetable(daySchedule: DaySchedule) {
        val currentData = _timetableData.value ?: return
        val updatedTimetable = currentData.timetable.toMutableList()
        val index = updatedTimetable.indexOfFirst { it.day.equals(daySchedule.day, ignoreCase = true) }
        
        if (index != -1) {
            updatedTimetable[index] = daySchedule
        } else {
            updatedTimetable.add(daySchedule)
        }
        
        _timetableData.value = currentData.copy(timetable = updatedTimetable.toList())
    }

    fun loadToday() {
        viewModelScope.launch {
            val today = Calendar.getInstance()
                .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH) ?: "Monday"
            
            repository.getTimetableByDay(today).collect { result ->
                result.onSuccess {
                    _todayClasses.value = it.classes
                }.onFailure {
                    // silent fail for dashboard widget
                }
            }
        }
    }
}
