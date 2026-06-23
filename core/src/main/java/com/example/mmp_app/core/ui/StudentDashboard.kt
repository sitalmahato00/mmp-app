package com.example.mmp_app.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.core.ui.MeasurableProgressBar
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.domain.model.*

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import com.example.mmp_app.core.ui.theme.*

@Composable
fun StudentDashboard(
    data: StudentDashboardDto,
    attendanceSummary: AttendanceSummaryDto? = null,
    subjects: List<SubjectDto> = emptyList(),
    assignments: List<AssignmentDto> = emptyList(),
    recentNotices: List<NoticeDto> = emptyList(),
    todayClasses: List<ClassDto> = emptyList(),
    materialCount: Int = 12,

    onAttendanceClick: () -> Unit = {},
    onMarksClick: () -> Unit = {},
    onAssignmentsClick: () -> Unit = {},
    onNoticesClick: () -> Unit = {},
    onFeesClick: () -> Unit = {},
    onRoutineClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onResultsClick: () -> Unit = {},
    onSubjectsClick: () -> Unit = {},
    onTimetableClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Smart Welcome Card
        item {
            WelcomeCard(data, subjects.size, primaryColor, textColor)
        }

        // Quick Actions Row
        item {
            QuickActionsRow(
                onAttendanceClick,
                onAssignmentsClick,
                onResultsClick,
                onSubjectsClick
            )
        }

        // 2. Academic Overview Card
        item {
            AcademicOverviewCard(
                attendanceSummary ?: AttendanceSummaryDto(
                    totalClasses = 20,
                    present = 16,
                    absent = 2,
                    late = 0,
                    attendancePercentage = 80f,
                    status = "Good"
                ),
                primaryColor, textColor
            )
        }

        // 3. Academic Snapshot Cards (Statistics)
        item {
            AcademicSnapshotGrid(
                data = data,
                subjectCount = subjects.size,
                assignmentCount = assignments.size,
                materialCount = materialCount,
                onDownloadsClick = onDownloadsClick
            )
        }

        // 4. Enrolled Subjects
        if (subjects.isNotEmpty()) {
            item {
                EnrolledSubjectsSection(
                    subjects = subjects,
                    cardColor = MaterialTheme.colorScheme.surface,
                    primaryColor = primaryColor,
                    textColor = textColor,
                    onSubjectsClick = onSubjectsClick
                )
            }
        }

        // 5. Today's Schedule
        item {
            TodayScheduleSection(
                classes = todayClasses,
                cardColor = MaterialTheme.colorScheme.surface,
                primaryColor = primaryColor,
                textColor = textColor
            )
        }

        // 6. Assignment Progress
        item {
            AssignmentProgressCard(
                assignments = assignments,
                cardColor = MaterialTheme.colorScheme.surface,
                successColor = Color(0xFF10B981),
                warningColor = Color(0xFFF59E0B),
                errorColor = MaterialTheme.colorScheme.error,
                textColor = textColor
            )
        }

        // 7. Recent Activity (Notices)
        item {
            ActivityFeedSection(
                notices = recentNotices,
                primaryColor = primaryColor,
                textColor = textColor
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun WelcomeCard(data: StudentDashboardDto, subjectCount: Int, primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(WelcomeGradientStart, WelcomeGradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good Morning 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = data.studentName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "${data.program} • Semester ${data.semester}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = primaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.AutoStories, 
                                null, 
                                modifier = Modifier.size(14.dp), 
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$subjectCount Subjects",
                                style = MaterialTheme.typography.labelMedium,
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onAttendanceClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onResultsClick: () -> Unit,
    onSubjectsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionItem("Attendance", Icons.Rounded.CalendarToday, Color(0xFF5C6BC0), onAttendanceClick)
        QuickActionItem("Assignments", Icons.Rounded.Assignment, Color(0xFFFF7043), onAssignmentsClick)
        QuickActionItem("Results", Icons.Rounded.Star, Color(0xFF66BB6A), onResultsClick)
        QuickActionItem("Subjects", Icons.Rounded.Book, Color(0xFF7E57C2), onSubjectsClick)
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(80.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AcademicOverviewCard(summary: AttendanceSummaryDto, primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Academic Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = textColor.copy(alpha = 0.05f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = (summary.attendancePercentage / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${summary.attendancePercentage.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "Attendance",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AttendanceStatRow("Present", summary.present.toString(), Color(0xFF4CAF50), textColor)
                    AttendanceStatRow("Absent", summary.absent.toString(), Color(0xFFF44336), textColor)
                    AttendanceStatRow("Total", summary.totalClasses.toString(), textColor, textColor)
                }
            }
        }
    }
}

@Composable
fun AttendanceStatRow(label: String, value: String, color: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f), modifier = Modifier.width(50.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun AcademicSnapshotGrid(
    data: StudentDashboardDto,
    subjectCount: Int,
    assignmentCount: Int,
    materialCount: Int,
    onDownloadsClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SnapshotCard("Subjects", subjectCount.toString(), Icons.Rounded.Book, Color(0xFF5C6BC0), Modifier.weight(1f))
        SnapshotCard("Assignments", assignmentCount.toString(), Icons.Rounded.Assignment, Color(0xFFFFA726), Modifier.weight(1f))
    }
}

@Composable
fun SnapshotCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun EnrolledSubjectsSection(
    subjects: List<SubjectDto>,
    cardColor: Color,
    primaryColor: Color,
    textColor: Color,
    onSubjectsClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Subjects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            TextButton(onClick = onSubjectsClick) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(subjects) { subject ->
                SubjectMiniCard(subject, cardColor, primaryColor, textColor)
            }
        }
    }
}

@Composable
fun SubjectMiniCard(subject: SubjectDto, cardColor: Color, primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (subject.code ?: subject.name).take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subject.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 2,
                minLines = 2
            )
            Text(
                text = subject.code ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TodayScheduleSection(classes: List<ClassDto>, cardColor: Color, primaryColor: Color, textColor: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (classes.isEmpty()) {
            Text(
                text = "No classes scheduled for today",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                classes.forEach { cls ->
                    ScheduleItem(cls, cardColor, primaryColor, textColor)
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(cls: ClassDto, cardColor: Color, primaryColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(80.dp)) {
                Text(
                    text = cls.time.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = cls.time.split(" ").lastOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(primaryColor.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = cls.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = textColor.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Room ${cls.room}",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentProgressCard(assignments: List<AssignmentDto>, cardColor: Color, successColor: Color, warningColor: Color, errorColor: Color, textColor: Color) {
    val pending = assignments.count { it.status.lowercase() == "pending" }
    val submitted = assignments.count { it.status.lowercase() == "submitted" }
    val graded = assignments.count { it.status.lowercase() == "graded" }
    val total = assignments.size.coerceAtLeast(1)
    val progress = (submitted + graded).toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Assignments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            MeasurableProgressBar(
                progress = progress,
                color = successColor
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = textColor.copy(alpha = 0.1f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = warningColor,
                            startAngle = -90f,
                            sweepAngle = (submitted.toFloat() / total) * 360f,
                            useCenter = false,
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$total",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    ProgressStatRow("Pending", pending.toString(), errorColor, textColor)
                    ProgressStatRow("Submitted", submitted.toString(), warningColor, textColor)
                    ProgressStatRow("Graded", graded.toString(), successColor, textColor)
                }
            }
        }
    }
}

@Composable
fun ProgressStatRow(label: String, value: String, color: Color, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun ActivityFeedSection(notices: List<NoticeDto>, primaryColor: Color, textColor: Color) {
    Column {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (notices.isEmpty()) {
            Text(
                text = "No recent activity",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.5f)
            )
        } else {
            Column {
                notices.take(4).forEachIndexed { index, notice ->
                    ActivityItem(notice, primaryColor, textColor, isLast = index == notices.take(4).lastIndex)
                }
            }
        }
    }
}

@Composable
fun ActivityItem(notice: NoticeDto, primaryColor: Color, textColor: Color, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = primaryColor
            ) {}
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(primaryColor.copy(alpha = 0.2f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = notice.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = notice.date,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f)
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
                studentName = "Sital Kumar Mahato",
                studentId = 1,
                avatarUrl = null,
                email = "sital.m@mmp.edu.np",
                phone = "+977 9801234567",
                rollNumber = "23BCA0415",
                program = "BCA 2A",
                semester = 2,
                kpiCards = StudentKpiDto(
                    attendancePercentage = 92f,
                    averageMarks = 84.5f,
                    pendingAssignments = 3,
                    unreadNotices = 2
                )
            ),
            attendanceSummary = AttendanceSummaryDto(
                totalClasses = 119,
                present = 110,
                absent = 9,
                late = 0,
                attendancePercentage = 92f,
                status = "Good"
            ),
            subjects = List(6) { SubjectDto(it, "Subject $it", "SUB$it") },
            todayClasses = listOf(
                ClassDto(1, "Data Structures", "09:00 AM", "CS-201"),
                ClassDto(2, "Web Technology", "10:00 AM", "CS-204"),
                ClassDto(3, "Database", "11:00 AM", "CS-205")
            )
        )
    }
}
