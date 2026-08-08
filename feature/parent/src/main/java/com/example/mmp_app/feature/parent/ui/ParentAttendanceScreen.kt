package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAttendanceScreen(
    childId: Int,
    onBack: () -> Unit,
    viewModel: ParentAttendanceViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text("Attendance", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
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
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // CHILD SELECTOR DROPDOWN
                if (uiState.children.size > 1) {
                    item {
                        ChildSelector(
                            children = uiState.children,
                            selectedChildId = uiState.selectedChildId,
                            onChildSelected = { viewModel.onChildSelected(it) },
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                if (uiState.isLoading || uiState.isSummaryLoading) {
                    item { ShimmerSummaryCard() }
                    items(5) { ShimmerRecordRow() }
                } else if (uiState.error != null && uiState.records.isEmpty()) {
                    item {
                        AttendanceErrorState(uiState.error!!, onRetry = { viewModel.refresh() })
                    }
                } else {
                    // SUMMARY CARD
                    uiState.summary?.let { summary ->
                        item {
                            AttendanceSummaryCard(summary, isDarkTheme)
                        }
                    }

                    // ATTENDANCE RECORDS LIST
                    if (uiState.records.isEmpty()) {
                        item {
                            AttendanceEmptyState()
                        }
                    } else {
                        item {
                            Text(
                                text = "Attendance History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        items(uiState.records) { record ->
                            AttendanceRecordRow(record, isDarkTheme)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSelector(
    children: List<ChildDetailDto>,
    selectedChildId: Int,
    onChildSelected: (Int) -> Unit,
    isDarkTheme: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedChild = children.find { it.id == selectedChildId } ?: children.firstOrNull()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (!selectedChild?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = selectedChild?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Person, null, modifier = Modifier.padding(6.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedChild?.name ?: "Select Child",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            children.forEach { child ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(24.dp), shape = CircleShape) {
                                if (child.avatarUrl?.isNotEmpty() == true) {
                                    AsyncImage(model = child.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Rounded.Person, null, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(child.name)
                        }
                    },
                    onClick = {
                        onChildSelected(child.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceSummaryCard(summary: ParentAttendanceSummaryDto, isDarkTheme: Boolean) {
    val statusColor = when {
        summary.attendancePercentage >= 75 -> Color(0xFF10B981) // Green
        summary.attendancePercentage >= 60 -> Color(0xFFF59E0B) // Orange
        else -> Color(0xFFEF4444) // Red
    }

    val statusLabel = when (summary.status.lowercase()) {
        "good" -> "Attendance is Good"
        "medium" -> "Needs Improvement"
        "low" -> "Attendance is Critical"
        else -> "Attendance Status: ${summary.status.replaceFirstChar { it.uppercase() }}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (summary.attendancePercentage / 100).toFloat() },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 10.dp,
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "${summary.attendancePercentage.toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttendanceStatBox("Present", summary.present.toString(), Color(0xFF10B981), Modifier.weight(1f))
                AttendanceStatBox("Absent", summary.absent.toString(), Color(0xFFEF4444), Modifier.weight(1f))
                AttendanceStatBox("Late", summary.late.toString(), Color(0xFFF59E0B), Modifier.weight(1f))
                AttendanceStatBox("Total", summary.totalClasses.toString(), if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AttendanceStatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (color == Color(0xFF94A3B8) || color == Color(0xFF64748B)) color else color.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AttendanceRecordRow(record: ParentAttendanceRecordDto, isDarkTheme: Boolean) {
    val (statusColor, statusLabel) = when (record.status.lowercase()) {
        "present" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "late" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        "absent" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.subject ?: "General Attendance",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                )
                Text(
                    text = formatAttendanceDate(record.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                val remarks = record.remarks
                if (!remarks.isNullOrEmpty()) {
                    Text(
                        text = remarks,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Surface(
                color = statusColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = record.status.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatAttendanceDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun ShimmerSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp))
    }
}

@Composable
fun ShimmerRecordRow() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(80.dp))
    }
}

@Composable
fun AttendanceEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.EventBusy, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No attendance records found", color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun AttendanceErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
