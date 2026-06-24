package com.example.mmp_app.feature.student.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onBack: () -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()

    val summary by viewModel.attendanceSummary.collectAsState()
    val attendanceDetails by viewModel.attendance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Summary", "Detail", "By Subject")

    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.loadStudentAttendance()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Show Calendar */ }) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> AttendanceSummaryView(summary, attendanceDetails, isLoading, error)
                    1 -> AttendanceDetailView(attendanceDetails, isLoading, error)
                    2 -> AttendanceBySubjectView(summary, attendanceDetails, isLoading)
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryView(
    summary: AttendanceSummaryDto?,
    details: List<AttendanceDto>,
    isLoading: Boolean,
    error: String?
) {
    if (isLoading && summary == null) {
        AttendanceSummarySkeleton()
    } else if (error != null && summary == null) {
        ErrorState(error)
    } else if (summary != null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AttendanceHeroCard(summary)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("Total Classes", summary.totalClasses.toString(), Icons.Rounded.Functions, Color(0xFF673AB7), Modifier.weight(1f))
                    KpiCard("Present", summary.present.toString(), Icons.Rounded.CheckCircle, Color(0xFF4CAF50), Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard("Absent", summary.absent.toString(), Icons.Rounded.Cancel, Color(0xFFF44336), Modifier.weight(1f))
                    KpiCard("Late", summary.late.toString(), Icons.Rounded.Schedule, Color(0xFFFF9800), Modifier.weight(1f))
                }
            }
            item {
                RecentAttendanceSection(details)
            }
        }
    } else {
        EmptyState("No attendance data found.")
    }
}

@Composable
fun AttendanceHeroCard(summary: AttendanceSummaryDto) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), MaterialTheme.colorScheme.surface)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.background(gradientBrush)) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Overall Attendance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.attendancePercentage.toInt()}%",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = if (summary.attendancePercentage >= 80) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = summary.status.uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Keep it up! You're doing great. 🎉",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { summary.attendancePercentage / 100f },
                        modifier = Modifier.size(130.dp),
                        strokeWidth = 12.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = summary.present.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Present Days",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "out of ${summary.totalClasses}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    color: Color, 
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun RecentAttendanceSection(records: List<AttendanceDto>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Attendance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { }) {
                Text("View All", color = Color(0xFF1A56BE), fontWeight = FontWeight.Bold)
            }
        }
        
        records.take(5).forEach { record ->
            RecentAttendanceRow(record)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RecentAttendanceRow(record: AttendanceDto) {
    val statusColor = when (record.status.lowercase()) {
        "present" -> Color(0xFF4CAF50)
        "absent" -> Color(0xFFF44336)
        "late" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (record.status.lowercase()) {
                        "present" -> Icons.Rounded.CheckCircle
                        "absent" -> Icons.Rounded.Cancel
                        "late" -> Icons.Rounded.Schedule
                        else -> Icons.Rounded.Help
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.subject ?: "General Session",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = record.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = record.status.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AttendanceDetailView(
    details: List<AttendanceDto>,
    isLoading: Boolean,
    error: String?
) {
    var selectedStatus by remember { mutableStateOf("All Status") }
    
    val filteredDetails = remember(details, selectedStatus) {
        if (selectedStatus == "All Status") {
            details
        } else {
            details.filter { it.status.equals(selectedStatus, ignoreCase = true) }
        }
    }

    if (isLoading && details.isEmpty()) {
        AttendanceDetailSkeleton()
    } else if (error != null && details.isEmpty()) {
        ErrorState(error)
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailFilterChip(
                    label = "May 2026",
                    icon = Icons.Rounded.CalendarToday,
                    modifier = Modifier.weight(1.3f),
                    onClick = { /* Implement date filter if needed */ }
                )
                
                var showStatusMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    DetailFilterChip(
                        label = selectedStatus,
                        icon = Icons.Rounded.FilterList,
                        onClick = { showStatusMenu = true }
                    )
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        listOf("All Status", "Present", "Absent", "Late").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDetails) { record ->
                    AttendanceDetailItem(record)
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("<", color = Color.Gray, modifier = Modifier.padding(16.dp))
                        Text("1", color = Color(0xFF1A56BE), fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                        Text("2", color = Color.Gray, modifier = Modifier.padding(16.dp))
                        Text("3", color = Color.Gray, modifier = Modifier.padding(16.dp))
                        Text(">", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            
            DetailFooter(details)
        }
    }
}

@Composable
fun DetailFilterChip(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceDetailItem(record: AttendanceDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateParts = record.date.split("-")
            val month = dateParts.getOrNull(1) ?: ""
            val day = dateParts.getOrNull(2) ?: ""
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(44.dp)) {
                Text(
                    text = when(month) {
                        "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"; "05" -> "May"; "06" -> "Jun"
                        "07" -> "Jul"; "08" -> "Aug"; "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
                        else -> month
                    }, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(day, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.subject ?: "General Session",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            val statusColor = when (record.status.lowercase()) {
                "present" -> Color(0xFF4CAF50)
                "absent" -> Color(0xFFF44336)
                "late" -> Color(0xFFFF9800)
                else -> Color.Gray
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = record.status.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DetailFooter(details: List<AttendanceDto>) {
    val total = details.size
    val present = details.count { it.status.equals("present", ignoreCase = true) }
    val absent = details.count { it.status.equals("absent", ignoreCase = true) }
    val late = details.count { it.status.equals("late", ignoreCase = true) }
    
    val presentPercent = if (total > 0) (present.toFloat() / total * 100).toInt() else 0
    val absentPercent = if (total > 0) (absent.toFloat() / total * 100).toInt() else 0
    val latePercent = if (total > 0) (late.toFloat() / total * 100).toInt() else 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FooterStat("Present", present.toString(), "$presentPercent%", Color(0xFF4CAF50))
            FooterStat("Absent", absent.toString(), "$absentPercent%", Color(0xFFF44336))
            FooterStat("Late", late.toString(), "$latePercent%", Color(0xFFFF9800))
        }
    }
}

@Composable
fun FooterStat(label: String, count: String, percent: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(4.dp))
            Text("($percent)", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceBySubjectView(
    summary: AttendanceSummaryDto?,
    details: List<AttendanceDto>,
    isLoading: Boolean
) {
    val subjectAttendance = remember(details) {
        details.filter { it.subject != null }
            .groupBy { it.subject!! }
            .map { (name, records) ->
                val total = records.size
                val present = records.count { it.status.equals("present", ignoreCase = true) }
                val absent = records.count { it.status.equals("absent", ignoreCase = true) }
                val late = records.count { it.status.equals("late", ignoreCase = true) }
                val percentage = if (total > 0) (present.toFloat() / total) * 100f else 0f
                
                AttendanceBySubjectDto(
                    subjectName = name,
                    totalClasses = total,
                    present = present,
                    absent = absent,
                    late = late,
                    attendancePercentage = percentage
                )
            }
            .sortedBy { it.attendancePercentage }
    }

    if (isLoading && subjectAttendance.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1A56BE))
        }
    } else if (subjectAttendance.isEmpty()) {
        EmptyState("No subject-wise data available.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SubjectOverviewHero(summary)
            }
            
            item {
                Text("Subject-wise Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            items(subjectAttendance) { subjectData ->
                SubjectAttendanceCard(subjectData)
            }
            
            val lowAttendanceSubjects = subjectAttendance.filter { it.attendancePercentage < 75 }
            if (lowAttendanceSubjects.isNotEmpty()) {
                item {
                    AttendanceRiskSection(lowAttendanceSubjects)
                }
            }
            
            item {
                SubjectAnalyticsChart(subjectAttendance)
            }
        }
    }
}

@Composable
fun SubjectOverviewHero(summary: AttendanceSummaryDto?) {
    val percentage = summary?.attendancePercentage ?: 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Overall Semester Attendance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("${percentage.toInt()}%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(subject: AttendanceBySubjectDto) {
    val percent = subject.attendancePercentage
    val color = when {
        percent >= 90 -> Color(0xFF4CAF50)
        percent >= 75 -> MaterialTheme.colorScheme.primary
        percent >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    val status = when {
        percent >= 90 -> "Excellent"
        percent >= 75 -> "Good"
        percent >= 60 -> "Warning"
        else -> "At Risk"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(subject.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Total Classes: ${subject.totalClasses}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${percent.toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${subject.present} Present / ${subject.absent} Absent", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        status, 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceRiskSection(lowAttendanceSubjects: List<AttendanceBySubjectDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("⚠ Low Attendance Warning", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                lowAttendanceSubjects.forEach { subject ->
                    Text("${subject.subjectName} (${subject.attendancePercentage.toInt()}%)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    val needed = ((0.75f * subject.totalClasses) - subject.present).coerceAtLeast(0f).toInt()
                    if (needed > 0) {
                        Text("Need $needed more classes to reach 75% threshold", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectAnalyticsChart(subjectAttendance: List<AttendanceBySubjectDto>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Semester Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F2F5))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                subjectAttendance.forEach { subject ->
                    val percent = subject.attendancePercentage
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(subject.subjectName, style = MaterialTheme.typography.labelMedium, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text("${percent.toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A56BE))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = if (percent < 75) Color(0xFFFF9800) else Color(0xFF1A56BE),
                            trackColor = Color(0xFFF0F2F5),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceSummarySkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(20.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(20.dp))
        }
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(150.dp), shape = RoundedCornerShape(20.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(20.dp))
    }
}

@Composable
fun AttendanceDetailSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonBox(modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp))
        }
        repeat(5) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp))
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray)
        }
    }
}
