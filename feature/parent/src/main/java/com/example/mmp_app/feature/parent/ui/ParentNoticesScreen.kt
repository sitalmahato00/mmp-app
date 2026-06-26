package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.ParentNoticeDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentNoticesScreen(
    onNoticeClick: (Int) -> Unit,
    viewModel: ParentNoticesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val filters = listOf("All", "General", "Exam", "Event", "Department")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("College Notices", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(state.currentFilter),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = state.currentFilter == filter,
                        onClick = { viewModel.applyFilter(filter) },
                        label = { Text(filter) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.filteredNotices.isEmpty()) {
                    Text("No notices found", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredNotices) { notice ->
                            NoticeCard(notice = notice, onClick = { onNoticeClick(notice.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeCard(notice: ParentNoticeDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = getNoticeTypeColor(notice.type).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = notice.type.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = getNoticeTypeColor(notice.type)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = notice.publishedAt.take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

fun getNoticeTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "exam" -> Color(0xFFF44336)
        "event" -> Color(0xFF2196F3)
        "department" -> Color(0xFF9C27B0)
        else -> Color(0xFF4CAF50)
    }
}
