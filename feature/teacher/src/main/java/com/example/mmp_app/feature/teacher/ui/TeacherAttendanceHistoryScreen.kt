package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.utils.NepaliDateUtils
import com.example.mmp_app.domain.model.HistorySession
import com.example.mmp_app.domain.model.HistoryMeta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceHistoryScreen(
    onBack: () -> Unit,
    onTakeAttendance: () -> Unit,
    onViewDetail: (Int) -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val viewModel: TeacherAttendanceViewModel = hiltViewModel()
    val state by viewModel.historyState.collectAsState()

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF5F7FA)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text("Attendance", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, null)
                        }
                        IconButton(onClick = { viewModel.loadHistory() }) {
                            Icon(Icons.Rounded.Refresh, null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cardBgColor,
                        titleContentColor = textColor
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTakeAttendance,
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, "Take Attendance")
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
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadHistory() }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    val history = currentState.data.data
                    val meta = currentState.data.meta

                    if (history.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No attendance sessions yet.", color = Color.Gray)
                            Text("Tap + to take attendance.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                if (meta != null) {
                                    AttendanceStatsCard(meta, textColor, cardBgColor)
                                }
                            }
                            items(history) { session ->
                                AttendanceHistoryCard(
                                    session = session,
                                    textColor = textColor,
                                    cardBgColor = cardBgColor,
                                    primaryColor = primaryColor,
                                    onClick = { onViewDetail(session.sessionId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceStatsCard(meta: HistoryMeta, textColor: Color, cardBgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Sessions", style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.6f))
                Text(meta.totalSessions.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            }
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray.copy(alpha = 0.2f)))
            Column {
                Text("Total Marked", style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.6f))
                Text(meta.totalMarked.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun AttendanceHistoryCard(
    session: HistorySession,
    textColor: Color,
    cardBgColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    // BS Date display
    val bsDate = try {
        val parts = session.date.split("-")
        NepaliDateUtils.adToBs(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (e: Exception) { session.date }

    val attendancePercent = if (session.totalStudents > 0) (session.present.toFloat() / session.totalStudents * 100).toInt() else 0
    
    val accentColor = when {
        attendancePercent == 100 -> Color(0xFF2E7D32)
        attendancePercent >= 75 -> Color(0xFF1565C0)
        else -> Color(0xFFE65100)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = session.subjectCode,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "📅 ${bsDate} BS",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = session.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
            
            val period = session.period
            if (!period.isNullOrBlank()) {
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatItem("✅ ${session.present}", "Present", accentColor)
                    StatItem("❌ ${session.absent}", "Absent", Color(0xFFC62828))
                    StatItem("👥 ${session.totalStudents}", "Total", textColor.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Attendance: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
                LinearProgressIndicator(
                    progress = { attendancePercent / 100f },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$attendancePercent%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "View Detail",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelLarge,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
