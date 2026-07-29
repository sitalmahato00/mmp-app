package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.ChildDetailDto
import androidx.compose.material.icons.rounded.Warning
import com.example.mmp_app.feature.student.ui.TimetableEmptyState
import com.example.mmp_app.feature.student.ui.TimetableGrid
import com.example.mmp_app.feature.student.ui.TimetableInfoHeader
import com.example.mmp_app.feature.student.ui.TimetableTableSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildTimetableScreen(
    childId: Int,
    onBack: () -> Unit,
    viewModel: ChildTimetableViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    showSystemHeader: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(childId) {
        if (childId != 0) {
            viewModel.setChildId(childId)
        }
    }
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    
    val pullToRefreshState = rememberPullToRefreshState()

    val content = @Composable { paddingValues: PaddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading || uiState.isTimetableLoading,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Child Selector
                if (uiState.children.size > 1) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        ChildSelector(
                            children = uiState.children,
                            selectedChildId = uiState.selectedChildId,
                            onChildSelected = { viewModel.onChildSelected(it) },
                            cardBgColor = cardBgColor,
                            textColor = textColor
                        )
                    }
                }

                if (uiState.timetable != null) {
                    TimetableInfoHeader(uiState.timetable!!)
                    
                    if (uiState.timetable?.hasTimetable == false) {
                        TimetableEmptyState(uiState.timetable?.semester?.toString() ?: "N/A")
                    } else {
                        TimetableGrid(uiState.timetable!!)
                    }
                } else if (uiState.isLoading || uiState.isTimetableLoading) {
                    TimetableTableSkeleton()
                } else if (uiState.error != null) {
                    TimetableErrorState(uiState.error!!) { viewModel.refresh() }
                }
            }
        }
    }

    if (showSystemHeader) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Timetable", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cardBgColor,
                        titleContentColor = textColor
                    )
                )
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            content(paddingValues)
        }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
private fun TimetableErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
