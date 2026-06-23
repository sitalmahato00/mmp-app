package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mmp_app.domain.model.ClassDto
import com.example.mmp_app.domain.model.TeacherDashboardDto
import com.example.mmp_app.core.ui.KpiCard
import com.example.mmp_app.core.ui.theme.MMPAppTheme


@Composable
fun TeacherDashboard(
    data: TeacherDashboardDto,
    onRecordAttendance: (Int, String) -> Unit = { _, _ -> },
    onRecordMarks: (Int, String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Teacher Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title = "Today's Classes",
                    value = "${data.todayClasses.size}",
                    icon = Icons.Rounded.Schedule,
                    containerColor = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Total Students",
                    value = "${data.totalStudents}",
                    icon = Icons.Rounded.Groups,
                    containerColor = Color(0xFFF1F8E9),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(data.todayClasses) { classItem ->
            ClassItem(
                classItem = classItem,
                onAttendanceClick = { onRecordAttendance(classItem.id, classItem.subject) },
                onMarksClick = { onRecordMarks(classItem.id, classItem.subject) }
            )
        }
    }
}

@Composable
fun ClassItem(
    classItem: ClassDto,
    onAttendanceClick: () -> Unit,
    onMarksClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = classItem.subject, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = "${classItem.time} | Room: ${classItem.room}", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAttendanceClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Attendance", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = onMarksClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Marks", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherDashboardPreview() {
    MMPAppTheme {
        TeacherDashboard(
            data = TeacherDashboardDto(
                teacherName = "John Doe",
                totalClasses = 5,
                totalStudents = 120,
                pendingMarks = 3,
                pendingAssignments = 2,
                todayClasses = listOf(
                    ClassDto(1, "Advanced Mathematics", "09:00 AM - 10:30 AM", "Room 301"),
                    ClassDto(2, "Physics Practical", "11:00 AM - 12:30 PM", "Lab 2")
                )
            )
        )
    }
}
