package com.example.mmp_app.feature.teacher.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.core.ui.NepaliDatePickerDialog
import com.example.mmp_app.core.utils.NepaliDateUtils
import com.example.mmp_app.domain.model.StudentItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val viewModel: TeacherAttendanceViewModel = hiltViewModel()
    val subjects by viewModel.subjects.collectAsState()
    val selectedBsDate by viewModel.selectedBsDate.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Form state
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var period by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF5F7FA)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    if (showDatePicker) {
        NepaliDatePickerDialog(
            initialBsDate = selectedBsDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { viewModel.onBsDateSelected(it) }
        )
    }

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text(if (currentStep == 1) "Take Attendance" else "Mark Attendance", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { if (currentStep == 2) currentStep = 1 else onBack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cardBgColor,
                        titleContentColor = textColor
                    )
                )
            }
        },
        bottomBar = {
            if (currentStep == 2 && studentsState is UiState.Success) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = cardBgColor
                ) {
                    Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = {
                                val session = (sessionState as? UiState.Success)?.data
                                if (session != null) {
                                    val students = (studentsState as UiState.Success).data
                                    val unmarkedCount = students.count { !viewModel.attendanceMap.containsKey(it.id) }
                                    
                                    if (unmarkedCount > 0) {
                                        // Show dialog or just mark as absent
                                        // For simplicity here, we'll mark as absent if they choose in a real app
                                        // But here let's just toast
                                        Toast.makeText(context, "Please mark all students", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.saveAllAndFinish(session.sessionId, onFinish, { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() })
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Save All & Finish", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        if (currentStep == 1) {
            AttendanceSetupForm(
                modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp),
                subjects = subjects,
                selectedSubjectId = selectedSubjectId,
                onSubjectSelected = { selectedSubjectId = it },
                selectedBsDate = selectedBsDate,
                onDateClick = { showDatePicker = true },
                period = period,
                onPeriodChange = { period = it },
                onStart = {
                    if (selectedSubjectId == null) {
                        Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show()
                        return@AttendanceSetupForm
                    }
                    viewModel.startSession(
                        subjectId = selectedSubjectId!!,
                        period = period,
                        onSuccess = { currentStep = 2 },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                primaryColor = primaryColor,
                textColor = textColor,
                cardBgColor = cardBgColor
            )
        } else {
            MarkAttendanceList(
                modifier = Modifier.padding(padding).fillMaxSize(),
                viewModel = viewModel,
                sessionState = sessionState,
                studentsState = studentsState,
                primaryColor = primaryColor,
                textColor = textColor,
                cardBgColor = cardBgColor
            )
        }
    }
}

@Composable
fun AttendanceSetupForm(
    modifier: Modifier,
    subjects: List<com.example.mmp_app.domain.model.TeacherSubjectDto>,
    selectedSubjectId: Int?,
    onSubjectSelected: (Int) -> Unit,
    selectedBsDate: String,
    onDateClick: () -> Unit,
    period: String,
    onPeriodChange: (String) -> Unit,
    onStart: () -> Unit,
    primaryColor: Color,
    textColor: Color,
    cardBgColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Subject Picker
        Column {
            Text("Subject*", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val selectedSubject = subjects.find { it.id == selectedSubjectId }
                        Text(
                            text = selectedSubject?.let { "${it.name} (${it.code})" } ?: "Select Subject",
                            color = if (selectedSubjectId != null) textColor else Color.Gray
                        )
                        Icon(Icons.Rounded.ArrowDropDown, null, tint = primaryColor)
                    }
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.85f)) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text("${subject.name} (${subject.code})") },
                            onClick = {
                                onSubjectSelected(subject.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Date Picker (BS)
        Column {
            Text("Date* (BS)", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().clickable { onDateClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$selectedBsDate BS", color = textColor)
                    Icon(Icons.Rounded.CalendarToday, null, tint = primaryColor)
                }
            }
        }

        // Period
        Column {
            Text("Period (optional)", style = MaterialTheme.typography.labelLarge, color = textColor.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = period,
                onValueChange = onPeriodChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 1st Period") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = cardBgColor,
                    unfocusedContainerColor = cardBgColor
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Start Attendance Session", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun MarkAttendanceList(
    modifier: Modifier,
    viewModel: TeacherAttendanceViewModel,
    sessionState: UiState<com.example.mmp_app.domain.model.SessionData>,
    studentsState: UiState<List<StudentItemDto>>,
    primaryColor: Color,
    textColor: Color,
    cardBgColor: Color
) {
    Column(modifier = modifier) {
        if (sessionState is UiState.Success && studentsState is UiState.Success) {
            val session = sessionState.data
            val students = studentsState.data
            val markedCount = viewModel.attendanceMap.size

            // Header Info
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = session.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = " (${session.subjectCode})", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val bsDate = NepaliDateUtils.adToBs(session.date.substringBefore('T').split("-")[0].toInt(), 
                                                     session.date.substringBefore('T').split("-")[1].toInt(), 
                                                     session.date.substringBefore('T').split("-")[2].toInt())
                    Text(text = "📅 $bsDate BS" + (if (session.period != null) " | ${session.period}" else ""), 
                         style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.7f))
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Marked: $markedCount / ${students.size}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = primaryColor)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.markAll("present") }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("All Present", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { viewModel.markAll("absent") }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Rounded.Cancel, null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("All Absent", color = Color(0xFFC62828), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            if (session.isExisting) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Session exists. Reviewing/editing existing records.", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    StudentMarkRow(
                        student = student,
                        attendanceRecord = viewModel.attendanceMap[student.id],
                        onStatusChange = { status -> viewModel.markStudent(session.sessionId, student.id, status, viewModel.attendanceMap[student.id]?.remarks) },
                        onRemarksChange = { remarks -> viewModel.markStudent(session.sessionId, student.id, viewModel.attendanceMap[student.id]?.status ?: "present", remarks) },
                        textColor = textColor,
                        cardBgColor = cardBgColor
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
    }
}

@Composable
fun StudentMarkRow(
    student: StudentItemDto,
    attendanceRecord: com.example.mmp_app.domain.model.AttendanceRecord?,
    onStatusChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    textColor: Color,
    cardBgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = Color.LightGray.copy(alpha = 0.2f)) {
                    if (!student.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(model = student.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(student.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = student.name, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = student.studentNo ?: "", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttendanceStatusButton("Present", attendanceRecord?.status == "present", Color(0xFF2E7D32), Modifier.weight(1f)) { onStatusChange("present") }
                AttendanceStatusButton("Absent", attendanceRecord?.status == "absent", Color(0xFFC62828), Modifier.weight(1f)) { onStatusChange("absent") }
                AttendanceStatusButton("Late", attendanceRecord?.status == "late", Color(0xFFE65100), Modifier.weight(1f)) { onStatusChange("late") }
            }

            var showRemarks by remember { mutableStateOf(!attendanceRecord?.remarks.isNullOrBlank()) }
            
            if (showRemarks) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = attendanceRecord?.remarks ?: "",
                    onValueChange = onRemarksChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add remarks...", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            } else {
                TextButton(onClick = { showRemarks = true }, modifier = Modifier.padding(top = 4.dp)) {
                    Text("+ Add Remarks", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (isSelected) color else Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) color else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
