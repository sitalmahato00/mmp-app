package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.data.remote.model.AttendanceRecordRequest
import com.example.mmp_app.data.remote.model.StudentAttendanceItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
    classId: Int,
    subject: String,
    onBack: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val students by viewModel.classStudents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val attendanceStates = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(classId) {
        viewModel.loadClassStudents(classId)
    }

    LaunchedEffect(students) {
        students.forEach { student ->
            if (!attendanceStates.containsKey(student.id)) {
                attendanceStates[student.id] = "Present"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Record Attendance", style = MaterialTheme.typography.titleMedium)
                        Text(subject, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val request = AttendanceRecordRequest(
                                classId = classId,
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                attendance = attendanceStates.map { (id, status) ->
                                    StudentAttendanceItem(id, status)
                                }
                            )
                            viewModel.recordAttendance(request, onSuccess = onBack)
                        },
                        enabled = !isLoading && students.isNotEmpty()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students) { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = student.name, fontWeight = FontWeight.Bold)
                                Text(text = student.email, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            val currentStatus = attendanceStates[student.id] ?: "Present"
                            
                            Row {
                                AttendanceChip(
                                    label = "P",
                                    selected = currentStatus == "Present",
                                    onClick = { attendanceStates[student.id] = "Present" },
                                    selectedColor = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AttendanceChip(
                                    label = "A",
                                    selected = currentStatus == "Absent",
                                    onClick = { attendanceStates[student.id] = "Absent" },
                                    selectedColor = Color.Red
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AttendanceChip(
                                    label = "L",
                                    selected = currentStatus == "Late",
                                    onClick = { attendanceStates[student.id] = "Late" },
                                    selectedColor = Color(0xFFFBC02D)
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedColor,
            selectedLabelColor = Color.White
        )
    )
}
