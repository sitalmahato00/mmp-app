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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.*

import kotlinx.coroutines.launch

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
        viewModel.loadStudentAttendance() // This loads detail and summary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> AttendanceSummaryView(summary, isLoading, error)
                    1 -> AttendanceDetailView(attendanceDetails, isLoading, error)
                    2 -> AttendanceBySubjectView(viewModel)
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryView(
    summary: AttendanceSummaryDto?,
    isLoading: Boolean,
    error: String?
) {
    if (isLoading && summary == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(24.dp))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp))
                    SkeletonBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp))
                    SkeletonBox(modifier = Modifier.weight(1f).height(80.dp), shape = RoundedCornerShape(16.dp))
                }
            }
        }
    } else if (error != null && summary == null) {
        ErrorState(error)
    } else if (summary != null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AttendancePercentageCard(summary.attendancePercentage, summary.status)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Total", summary.totalClasses.toString(), Icons.Rounded.Functions, Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("Present", summary.present.toString(), Icons.Rounded.CheckCircle, Color(0xFF4CAF50), Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Absent", summary.absent.toString(), Icons.Rounded.Cancel, Color(0xFFF44336), Modifier.weight(1f))
                    StatCard("Late", summary.late.toString(), Icons.Rounded.Schedule, Color(0xFFFF9800), Modifier.weight(1f))
                }
            }
        }
    } else {
        EmptyState("No attendance data found.")
    }
}

@Composable
fun AttendanceDetailView(
    details: List<AttendanceDto>,
    isLoading: Boolean,
    error: String?
) {
    var selectedRecord by remember { mutableStateOf<AttendanceDto?>(null) }


    if (isLoading && details.isEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(10) {
                SkeletonBox(modifier = Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(12.dp))
            }
        }
    } else if (error != null && details.isEmpty()) {
        ErrorState(error)
    } else if (details.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(details) { record ->
                AttendanceRow(record) { selectedRecord = record }
            }
        }
    } else {
        EmptyState("No attendance records found.")
    }

    selectedRecord?.let { record ->
        AttendanceDetailModal(record) { selectedRecord = null }
    }
}

@Composable
fun AttendanceBySubjectView(viewModel: StudentViewModel) {

    val subjects by viewModel.subjects.collectAsState()
    val subjectAttendance by viewModel.attendanceBySubject.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<SubjectDto?>(null) }

    
    LaunchedEffect(Unit) {
        viewModel.loadStudentSubjects()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Subject", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedSubject?.name ?: "Choose a subject")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
            
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject.name) },
                        onClick = {
                            selectedSubject = subject
                            expanded = false
                            viewModel.loadAttendanceBySubject(subject.id)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading && subjectAttendance == null) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (subjectAttendance != null) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AttendancePercentageCard(subjectAttendance!!.attendancePercentage, "Subject Percentage")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Total", subjectAttendance!!.totalClasses.toString(), Icons.Rounded.Functions, Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("Present", subjectAttendance!!.present.toString(), Icons.Rounded.CheckCircle, Color(0xFF4CAF50), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Absent", subjectAttendance!!.absent.toString(), Icons.Rounded.Cancel, Color(0xFFF44336), Modifier.weight(1f))
                    StatCard("Late", subjectAttendance!!.late.toString(), Icons.Rounded.Schedule, Color(0xFFFF9800), Modifier.weight(1f))
                }
            }
        } else {
            EmptyState("Select a subject to view attendance details.")
        }
    }
}

@Composable
fun AttendancePercentageCard(percentage: Float, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Attendance", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = if (percentage >= 75) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = CircleShape
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AttendanceRow(record: AttendanceDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.subject ?: "Unknown Subject", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = record.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            val statusColor = when (record.status.lowercase()) {
                "present" -> Color(0xFF4CAF50)
                "absent" -> Color(0xFFF44336)
                "late" -> Color(0xFFFF9800)
                else -> Color.Gray
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
            ) {
                Text(
                    text = record.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AttendanceDetailModal(record: AttendanceDto, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Record Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailItem("Subject", record.subject ?: "N/A")
                DetailItem("Date", record.date)
                DetailItem("Session", record.session ?: "Default")
                DetailItem("Status", record.status)
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Gray)
    }
}
