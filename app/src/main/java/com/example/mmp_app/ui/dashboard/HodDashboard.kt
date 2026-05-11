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
import com.example.mmp_app.data.remote.model.HodDashboardDto
import com.example.mmp_app.data.remote.model.NoticeDto
import com.example.mmp_app.ui.dashboard.components.KpiCard
import com.example.mmp_app.ui.dashboard.components.NoticeItem
import com.example.mmp_app.ui.theme.MMPAppTheme

@Composable
fun HodDashboard(data: HodDashboardDto) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "${data.departmentName} Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(
                        title = "Total Teachers",
                        value = "${data.totalTeachers}",
                        icon = Icons.Rounded.People,
                        containerColor = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Total Students",
                        value = "${data.totalStudents}",
                        icon = Icons.Rounded.School,
                        containerColor = Color(0xFFF1F8E9),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(
                        title = "Pending Approvals",
                        value = "${data.pendingApprovals}",
                        icon = Icons.Rounded.VerifiedUser,
                        containerColor = Color(0xFFFFF3E0),
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
fun HodDashboardPreview() {
    MMPAppTheme {
        HodDashboard(
            data = HodDashboardDto(
                departmentName = "Computer Science",
                totalTeachers = 15,
                totalStudents = 450,
                pendingApprovals = 5,
                recentNotices = listOf(
                    NoticeDto(
                        id = 1,
                        title = "Department Meeting",
                        content = "All staff meeting on Monday at 10 AM.",
                        date = "2024-05-15",
                        type = "Admin"
                    )
                )
            )
        )
    }
}
