package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
    classId: Int,
    subject: String,
    onBack: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val viewModel: TeacherViewModel = hiltViewModel()

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

    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8F9FF)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Record Attendance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                        }
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cardBgColor,
                        titleContentColor = textColor
                    )
                )
            }
        },
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)

        if (isLoading && students.isEmpty()) {
            LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(10) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        } else {
            Column(modifier = contentModifier) {
                if (error != null) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error!!,
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(students) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = student.name, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(text = student.email, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                        }
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
