package com.example.mmp_app.feature.student.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onBack: () -> Unit,
    onSubjectClick: (Int, String, String?) -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()

    val subjects by viewModel.subjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStudentSubjects()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Subjects") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
            when {
                isLoading && subjects.isEmpty() -> {
                    SubjectsSkeleton()
                }
                subjects.isEmpty() && !isLoading -> {
                    MarksEmptyState(message = "No subjects enrolled yet.")
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(subjects) { subject ->
                            SubjectCard(subject) {
                                onSubjectClick(subject.id, subject.name, subject.code)
                            }
                        }
                    }
                }
            }
            
            if (error != null && subjects.isEmpty()) {
                ErrorView(error!!) { viewModel.loadStudentSubjects() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Int,
    subjectName: String,
    subjectCode: String?,
    onBack: () -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()

    val marks by viewModel.subjectMarks.collectAsState()
    val attendance by viewModel.attendanceBySubject.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Marks", "Attendance")

    LaunchedEffect(subjectId) {
        viewModel.loadMarksBySubject(subjectId)
        viewModel.loadAttendanceBySubject(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = subjectName, style = MaterialTheme.typography.titleMedium)
                        if (subjectCode != null) {
                            Text(
                                text = subjectCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && marks == null && attendance == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    when (selectedTabIndex) {
                        0 -> SubjectMarksTab(marks)
                        1 -> SubjectAttendanceTab(attendance)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectCard(subject: SubjectDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val subjectCode = subject.code
                if (subjectCode != null) {
                    Text(
                        text = subjectCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun SubjectMarksTab(subjectMark: SubjectMarkDto?) {
    if (subjectMark == null || subjectMark.marks.isEmpty()) {
        MarksEmptyState(message = "No marks recorded for this subject.")
        return
    }

    val examMarks = subjectMark.marks
    val avg = examMarks.map { it.percentage }.average().toFloat()
    val highest = examMarks.maxOf { it.obtainedMarks }
    val lowest = examMarks.minOf { it.obtainedMarks }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubjectStatsCard(avg, highest, lowest)
        }
        item {
            Text("Exam Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(examMarks) { mark ->
            ExamMarkRow(mark)
        }
    }
}

@Composable
fun SubjectAttendanceTab(attendance: AttendanceBySubjectDto?) {
    if (attendance == null) {
        MarksEmptyState(message = "No attendance records found.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val statusColor = when {
                attendance.attendancePercentage >= 75f -> Color(0xFF4CAF50)
                attendance.attendancePercentage >= 60f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            val statusLabel = when {
                attendance.attendancePercentage >= 75f -> "Good"
                attendance.attendancePercentage >= 60f -> "Medium"
                else -> "Low"
            }
            
            AttendancePercentageCard(attendance.attendancePercentage, statusLabel)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total", attendance.totalClasses.toString(), Icons.Rounded.Functions, Color(0xFF2196F3), Modifier.weight(1f))
                StatCard("Present", attendance.present.toString(), Icons.Rounded.CheckCircle, Color(0xFF4CAF50), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Absent", attendance.absent.toString(), Icons.Rounded.Cancel, Color(0xFFF44336), Modifier.weight(1f))
                StatCard("Late", attendance.late.toString(), Icons.Rounded.Schedule, Color(0xFFFF9800), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SubjectStatsCard(avg: Float, highest: Float, lowest: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Average Marks", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${avg.toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            VerticalDivider(modifier = Modifier.height(60.dp).padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Column {
                StatLabelValue("Highest", highest.toInt().toString())
                Spacer(modifier = Modifier.height(8.dp))
                StatLabelValue("Lowest", lowest.toInt().toString())
            }
        }
    }
}

@Composable
fun StatLabelValue(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExamMarkRow(mark: ExamMarkDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mark.examName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Score: ${mark.obtainedMarks.toInt()}/${mark.totalMarks.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            SubjectGradeBadge(mark.percentage)
        }
    }
}

@Composable
fun SubjectGradeBadge(percentage: Float) {
    val (color, grade) = when {
        percentage >= 90 -> Color(0xFF4CAF50) to "A"
        percentage >= 80 -> Color(0xFF2196F3) to "B"
        percentage >= 70 -> Color(0xFFFFC107) to "C"
        percentage >= 60 -> Color(0xFFFF9800) to "D"
        else -> Color(0xFFF44336) to "F"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = grade,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SubjectsSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
