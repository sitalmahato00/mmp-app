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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAttendanceScreen(
    childId: Int,
    onBack: () -> Unit,
    isDarkTheme: Boolean = false
) {
    // Assuming we have a ViewModel that can fetch attendance for a specific child
    // For now, it might be the same ParentDashboardViewModel or a new one
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Summary", "Detail")

    val primaryColor = Color(0xFF6366F1)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = primaryColor
                    )
                }
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
                // Mocking data for now as we don't have the full viewmodel logic yet
                if (selectedTabIndex == 0) {
                    ParentAttendanceSummaryView(isDarkTheme)
                } else {
                    ParentAttendanceDetailView(isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun ParentAttendanceSummaryView(isDarkTheme: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            AttendanceHero(75f, isDarkTheme)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AttendanceStatCard("Present", "45", Icons.Rounded.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
                AttendanceStatCard("Absent", "10", Icons.Rounded.Cancel, Color(0xFFEF4444), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AttendanceHero(percentage: Float, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(100.dp),
                    strokeWidth = 10.dp,
                    color = Color(0xFF6366F1),
                    trackColor = Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    "${percentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text("Overall Attendance", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("Good Standing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Updated today", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AttendanceStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ParentAttendanceDetailView(isDarkTheme: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(listOf("Math", "Science", "English", "History")) { subject ->
            AttendanceRecordItem(subject, "2023-10-25", "Present")
        }
    }
}

@Composable
fun AttendanceRecordItem(subject: String, date: String, status: String) {
    val statusColor = if (status == "Present") Color(0xFF10B981) else Color(0xFFEF4444)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(subject, fontWeight = FontWeight.Bold)
                Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
