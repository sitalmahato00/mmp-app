package com.example.mmp_app.feature.teacher.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.core.utils.NepaliDateUtils
import com.example.mmp_app.domain.model.StudentAttendanceDetail
import com.example.mmp_app.domain.model.SessionDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSessionDetailScreen(
    sessionId: Int,
    onBack: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val viewModel: TeacherAttendanceViewModel = hiltViewModel()
    val state by viewModel.sessionDetailState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSessionDetail(sessionId)
    }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF5F7FA)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text("Session Detail", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
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
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val currentState = state) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
                }
                is UiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentState.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadSessionDetail(sessionId) }) { Text("Retry") }
                    }
                }
                is UiState.Success -> {
                    val data = currentState.data

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SessionHeaderCard(data, textColor, cardBgColor, primaryColor)
                        }
                        
                        item {
                            SessionStatsRow(data, textColor, cardBgColor)
                        }

                        items(data.students) { record ->
                            StudentAttendanceRecordRow(
                                record = record,
                                onEdit = { status, remarks -> viewModel.markStudent(sessionId, record.studentId, status, remarks) },
                                textColor = textColor,
                                cardBgColor = cardBgColor,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionHeaderCard(
    data: SessionDetailData,
    textColor: Color,
    cardBgColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = data.subjectCode,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = primaryColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = data.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            val bsDate = try {
                val parts = data.date.split("-")
                NepaliDateUtils.adToBs(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } catch (e: Exception) { data.date }
            
            val period = data.period
            Text(text = "📅 $bsDate BS" + (if (period != null) " | $period" else ""), 
                 style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun SessionStatsRow(data: SessionDetailData, textColor: Color, cardBgColor: Color) {
    val present = data.students.count { it.status == "present" }
    val absent = data.students.count { it.status == "absent" }
    val late = data.students.count { it.status == "late" }
    val total = data.students.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem("✅ $present", "Present", Color(0xFF2E7D32))
            StatItem("❌ $absent", "Absent", Color(0xFFC62828))
            StatItem("🕐 $late", "Late", Color(0xFFE65100))
            StatItem("👥 $total", "Total", textColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun StudentAttendanceRecordRow(
    record: StudentAttendanceDetail,
    onEdit: (String, String?) -> Unit,
    textColor: Color,
    cardBgColor: Color,
    primaryColor: Color
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf(record.status) }
    var currentRemarks by remember { mutableStateOf(record.remarks ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.LightGray.copy(alpha = 0.2f)) {
                    if (!record.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(model = record.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(record.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = record.name, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = record.studentNo, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                }
                
                if (!isEditing) {
                    StatusBadge(record.status)
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp), tint = primaryColor)
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttendanceStatusButton("Present", currentStatus == "present", Color(0xFF2E7D32), Modifier.weight(1f)) { currentStatus = "present" }
                    AttendanceStatusButton("Absent", currentStatus == "absent", Color(0xFFC62828), Modifier.weight(1f)) { currentStatus = "absent" }
                    AttendanceStatusButton("Late", currentStatus == "late", Color(0xFFE65100), Modifier.weight(1f)) { currentStatus = "late" }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentRemarks,
                    onValueChange = { currentRemarks = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Remarks...", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                    Button(onClick = { 
                        onEdit(currentStatus, if (currentRemarks.isBlank()) null else currentRemarks)
                        isEditing = false 
                    }, shape = RoundedCornerShape(8.dp)) {
                        Text("Save")
                    }
                }
            } else if (!record.remarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Remarks: ${record.remarks}", style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status.lowercase()) {
        "present" -> Color(0xFF2E7D32) to "Present"
        "absent" -> Color(0xFFC62828) to "Absent"
        "late" -> Color(0xFFE65100) to "Late"
        else -> Color.Gray to status.uppercase()
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
