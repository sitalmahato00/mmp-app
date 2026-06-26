package com.example.mmp_app.core.presentation

import androidx.lifecycle.ViewModel
import com.example.mmp_app.core.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(sessionManager.isDarkTheme())
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        sessionManager.saveTheme(newValue)
    }
}
