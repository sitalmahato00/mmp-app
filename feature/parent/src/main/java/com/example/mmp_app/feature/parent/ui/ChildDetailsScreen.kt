package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailsScreen(
    childId: Int,
    name: String,
    onBack: () -> Unit,
    viewModel: ChildDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Attendance", "Marks", "Assignments", "Timetable")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = state.childDetail?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = androidx.compose.ui.res.painterResource(id = com.example.mmp_app.core.R.drawable.mmplogo)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = state.childDetail?.studentNo ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.childDetail == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTab) {
                        0 -> OverviewTab(state)
                        1 -> AttendanceTab(state)
                        2 -> MarksTab(state)
                        3 -> AssignmentsTab(state)
                        4 -> TimetableTab(state)
                    }
                }

                if (state.error != null) {
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        action = { Button(onClick = { viewModel.loadAllData() }) { Text("Retry") } }
                    ) { Text(text = state.error!!) }
                }
            }
        }
    }
}

@Composable
fun OverviewTab(state: ChildDetailState) {
    val child = state.childDetail ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Attendance",
                    value = "${state.attendanceSummary?.attendancePercentage?.toInt() ?: 0}%",
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg Marks",
                    value = "${state.marksSummary?.averageMarks?.toInt() ?: 0}",
                    color = Color(0xFFF1F8E9),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Student Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    InfoRow("Email", child.email)
                    InfoRow("Phone", child.phone ?: "N/A")
                    InfoRow("Program", child.program)
                    InfoRow("Batch", child.batch)
                    InfoRow("Admission", child.admissionDate?.take(10) ?: "N/A")
                    InfoRow("Status", child.status.uppercase())
                }
            }
        }
    }
}

@Composable
fun AttendanceTab(state: ChildDetailState) {
    val summary = state.attendanceSummary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = (summary?.attendancePercentage?.toFloat() ?: 0f) / 100f,
                            modifier = Modifier.size(100.dp),
                            strokeWidth = 8.dp,
                            color = getStatusColor(summary?.status ?: "")
                        )
                        Text(
                            text = "${summary?.attendancePercentage?.toInt() ?: 0}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CountItem("Present", summary?.present ?: 0, Color(0xFF4CAF50))
                        CountItem("Late", summary?.late ?: 0, Color(0xFFFFC107))
                        CountItem("Absent", summary?.absent ?: 0, Color(0xFFF44336))
                    }
                }
            }
        }

        items(state.attendanceRecords) { record ->
            AttendanceRow(record)
        }
    }
}

@Composable
fun MarksTab(state: ChildDetailState) {
    val summary = state.marksSummary
    val groupedMarks = state.marksList.groupBy { it.exam }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Academic Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Average Marks: ${summary?.averageMarks ?: 0.0}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                        Text(
                            text = "Passed ${summary?.passedCount ?: 0}/${summary?.totalExams ?: 0}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        groupedMarks.forEach { (exam, marks) ->
            item {
                Text(text = exam, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            items(marks) { mark ->
                MarkRow(mark)
            }
        }
    }
}

@Composable
fun AssignmentsTab(state: ChildDetailState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.assignments) { assignment ->
            AssignmentRow(assignment)
        }
    }
}

@Composable
fun TimetableTab(state: ChildDetailState) {
    val timetable = state.timetable ?: return
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val days = timetable.timetable.map { it.day }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedDayIndex,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            days.forEachIndexed { index, day ->
                Tab(
                    selected = selectedDayIndex == index,
                    onClick = { selectedDayIndex = index },
                    text = { Text(day.take(3)) }
                )
            }
        }

        val classes = timetable.timetable.getOrNull(selectedDayIndex)?.classes ?: emptyList()
        if (classes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No classes scheduled", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(classes) { cls ->
                    TimetableClassRow(cls)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CountItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun AttendanceRow(record: ParentAttendanceRecordDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.subject ?: "General", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = record.date.take(10), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            StatusChip(status = record.status)
        }
    }
}

@Composable
fun MarkRow(mark: ParentMarkRecordDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mark.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = mark.subjectCode, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${mark.obtainedMarks}/${mark.fullMarks}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (mark.isPass) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Text(
                    text = if (mark.isPass) "PASS" else "FAIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mark.isPass) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
fun AssignmentRow(assignment: ParentAssignmentDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = assignment.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                AssignmentStatusChip(status = assignment.status)
            }
            Text(text = assignment.subject ?: "No Subject", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Due: ${assignment.dueDate.take(10)}", style = MaterialTheme.typography.labelSmall)
                if (assignment.marks != null) {
                    Text(text = "Marks: ${assignment.marks}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TimetableClassRow(cls: ParentTimetableClassDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.width(80.dp)) {
                Text(text = cls.startTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = cls.endTime, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cls.subject, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = cls.teacher, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = cls.room, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(color = if (cls.type == "theory") Color(0xFFE3F2FD) else Color(0xFFF3E5F5), shape = CircleShape) {
                        Text(text = cls.type.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "present" -> Color(0xFF4CAF50)
        "absent" -> Color(0xFFF44336)
        "late" -> Color(0xFFFFC107)
        else -> Color.Gray
    }
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, color)) {
        Text(text = status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun AssignmentStatusChip(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> Color(0xFFFF9800)
        "submitted" -> Color(0xFF2196F3)
        "graded" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
        Text(text = status.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "good" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFFC107)
        "low" -> Color(0xFFF44336)
        else -> Color(0xFF2196F3)
    }
}
