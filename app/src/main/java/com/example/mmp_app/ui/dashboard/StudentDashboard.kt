package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mmp_app.data.remote.model.NoticeDto
import com.example.mmp_app.data.remote.model.StudentDashboardDto
import com.example.mmp_app.ui.dashboard.components.KpiCard
import com.example.mmp_app.ui.dashboard.components.NoticeItem
import com.example.mmp_app.ui.theme.MMPAppTheme

@Composable
fun StudentDashboard(data: StudentDashboardDto) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Academic Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(
                        title = "Attendance",
                        value = "${data.attendancePercentage}%",
                        icon = Icons.Rounded.DateRange,
                        containerColor = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Avg Marks",
                        value = "${data.averageMarks}%",
                        icon = Icons.Rounded.Star,
                        containerColor = Color(0xFFF1F8E9),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(
                        title = "Assignments",
                        value = "${data.pendingAssignments}",
                        icon = Icons.Rounded.Edit,
                        containerColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Notices",
                        value = "${data.unreadNotices}",
                        icon = Icons.Rounded.Notifications,
                        containerColor = Color(0xFFFFEBEE),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Recent Notices",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(data.recentNotices) { notice ->
            NoticeItem(notice = notice)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDashboardPreview() {
    MMPAppTheme {
        StudentDashboard(
            data = StudentDashboardDto(
                attendancePercentage = 85f,
                averageMarks = 78.5f,
                pendingAssignments = 3,
                unreadNotices = 2,
                recentNotices = listOf(
                    NoticeDto(
                        id = 1,
                        title = "Holiday Notice",
                        content = "College will remain closed on Friday.",
                        date = "2024-05-10",
                        type = "General"
                    ),
                    NoticeDto(
                        id = 2,
                        title = "Exam Schedule",
                        content = "The mid-term exam starts from next week.",
                        date = "2024-05-12",
                        type = "Academic"
                    )
                )
            )
        )
    }
}
