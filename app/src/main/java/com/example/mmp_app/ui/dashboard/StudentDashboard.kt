package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mmp_app.data.remote.model.NoticeDto
import com.example.mmp_app.data.remote.model.StudentDashboardDto
import com.example.mmp_app.data.remote.model.StudentKpiDto
import com.example.mmp_app.ui.theme.MMPAppTheme

@Composable
fun StudentDashboard(
    data: StudentDashboardDto,
    recentNotices: List<NoticeDto> = emptyList(),
    todayClasses: List<com.example.mmp_app.data.remote.model.ClassDto> = emptyList(),
    onAttendanceClick: () -> Unit = {},
    onMarksClick: () -> Unit = {},
    onAssignmentsClick: () -> Unit = {},
    onNoticesClick: () -> Unit = {},
    onFeesClick: () -> Unit = {},
    onRoutineClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onResultsClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hello, ${data.studentName.split(" ").firstOrNull() ?: "Student"}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "👋", fontSize = 24.sp)
                }
                Text(
                    text = "${data.program} - Semester ${data.semester}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                if (data.rollNumber != null) {
                    Text(
                        text = "Roll No: ${data.rollNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }

        // KPI Section (Row of 4 cards)
        item {
            Column {
                Text(
                    text = "Performance Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiSmallCard(
                        label = "Attendance",
                        value = "${data.kpiCards.attendancePercentage.toInt()}%",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    KpiSmallCard(
                        label = "Avg Marks",
                        value = "${data.kpiCards.averageMarks.toInt()}%",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    KpiSmallCard(
                        label = "Assignments",
                        value = "${data.kpiCards.pendingAssignments}",
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    KpiSmallCard(
                        label = "Notices",
                        value = "${data.kpiCards.unreadNotices}",
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions
        item {
            Column {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionIconButton(
                            title = "Attendance",
                            icon = Icons.Rounded.CalendarToday,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f),
                            onClick = onAttendanceClick
                        )
                        ActionIconButton(
                            title = "Marks",
                            icon = Icons.Rounded.Star,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f),
                            onClick = onMarksClick
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionIconButton(
                            title = "Assignments",
                            icon = Icons.AutoMirrored.Rounded.Assignment,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f),
                            onClick = onAssignmentsClick
                        )
                        ActionIconButton(
                            title = "Notices",
                            icon = Icons.Rounded.NotificationsActive,
                            color = Color(0xFFF44336),
                            modifier = Modifier.weight(1f),
                            onClick = onNoticesClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiSmallCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun ActionIconButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1C1E)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDashboardPreview() {
    MMPAppTheme {
        StudentDashboard(
            data = StudentDashboardDto(
                studentName = "Arjun Mehra",
                studentId = 1,
                avatarUrl = null,
                email = "arjun.m@mmp.edu.np",
                phone = "+977 9801234567",
                rollNumber = "BIT-2021-045",
                program = "BIT",
                semester = 4,
                kpiCards = StudentKpiDto(
                    attendancePercentage = 88f,
                    averageMarks = 76.5f,
                    pendingAssignments = 3,
                    unreadNotices = 2
                )
            )
        )
    }
}
