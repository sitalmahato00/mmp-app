package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailsScreen(
    childId: Int,
    name: String,
    onBack: () -> Unit,
    viewModel: ChildDetailViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    
    val primaryColor = Color(0xFF2563EB)
    val secondaryColor = Color(0xFF60A5FA)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val accentColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(text = "Child Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBgColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (state.isLoading && state.childDetail == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Profile Card
                    item {
                        ChildProfileCard(state.childDetail, primaryColor, isDarkTheme)
                    }

                    // 2. Quick Actions
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard("Attendance", Icons.Rounded.CalendarToday, primaryColor, cardBgColor, Modifier.weight(1f)) { }
                            QuickActionCard("Assignments", Icons.AutoMirrored.Rounded.Assignment, primaryColor, cardBgColor, Modifier.weight(1f)) { }
                            QuickActionCard("Results", Icons.Rounded.Star, primaryColor, cardBgColor, Modifier.weight(1f)) { }
                            QuickActionCard("Info", Icons.Rounded.Info, primaryColor, cardBgColor, Modifier.weight(1f)) { }
                        }
                    }

                    // 3. Academic Overview (Attendance)
                    item {
                        state.attendanceSummary?.let { summary ->
                            AcademicOverviewCard(summary, primaryColor, secondaryColor, textColor, accentColor, cardBgColor)
                        }
                    }

                    // 4. Stats Summary
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val pendingTasks = state.assignments.count { it.status.lowercase() == "pending" }
                                StatsSummaryCard("Pending Tasks", pendingTasks.toString(), Icons.AutoMirrored.Rounded.Assignment, Color(0xFFEF4444), cardBgColor, Modifier.weight(1f))
                                StatsSummaryCard("Total Exams", (state.marksSummary?.totalExams ?: 0).toString(), Icons.Rounded.Quiz, primaryColor, cardBgColor, Modifier.weight(1f))
                            }
                        }
                    }

                    // 5. Assignment Progress
                    item {
                        AssignmentProgressCard(state.assignments, primaryColor, secondaryColor, textColor, accentColor, cardBgColor)
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun ChildProfileCard(child: ChildDetailDto?, primaryColor: Color, isDarkTheme: Boolean) {
    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E293B))
    } else {
        listOf(Color(0xFFF1F5F9), Color(0xFFDBEAFE), Color(0xFFEFF6FF))
    }

    val contentTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF1E293B).copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(colors = gradientColors),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Student Profile", style = MaterialTheme.typography.bodyMedium, color = subTextColor)
                    Text(
                        text = child?.name ?: "Loading...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentTextColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${child?.program ?: ""} • Semester ${child?.semester ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                    Text(
                        text = "Roll No: ${child?.studentNo ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                }
                Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = if (isDarkTheme) Color(0xFF334155) else Color.White, shadowElevation = 4.dp) {
                    if (!child?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = child?.avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(14.dp), tint = primaryColor)
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                    Spacer(Modifier.width(8.dp))
                    Text(text = child?.status?.uppercase() ?: "", style = MaterialTheme.typography.labelLarge, color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, color: Color, cardBgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(10.dp), tint = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun AcademicOverviewCard(summary: ParentAttendanceSummaryDto, primaryColor: Color, secondaryColor: Color, textColor: Color, accentColor: Color, cardBgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Attendance Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(color = accentColor.copy(alpha = 0.4f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor)), startAngle = -90f, sweepAngle = (summary.attendancePercentage.toFloat() / 100f) * 360f, useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${summary.attendancePercentage.toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = "Present", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewStatRow("Present", summary.present.toString(), Color(0xFF10B981), textColor)
                    OverviewStatRow("Absent", summary.absent.toString(), Color(0xFFEF4444), textColor)
                    OverviewStatRow("Late", summary.late.toString(), Color(0xFFF59E0B), textColor)
                }
            }
        }
    }
}

@Composable
fun OverviewStatRow(label: String, value: String, color: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.6f), modifier = Modifier.width(60.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun StatsSummaryCard(label: String, value: String, icon: ImageVector, color: Color, cardBgColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
                    Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = color)
                }
                Spacer(Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AssignmentProgressCard(assignments: List<ParentAssignmentDto>, primaryColor: Color, secondaryColor: Color, textColor: Color, accentColor: Color, cardBgColor: Color) {
    val pending = assignments.count { it.status.lowercase() == "pending" }
    val submitted = assignments.count { it.status.lowercase() == "submitted" }
    val graded = assignments.count { it.status.lowercase() == "graded" }
    val total = assignments.size.coerceAtLeast(1)
    
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Assignment Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(progress = { (submitted + graded).toFloat() / total }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = secondaryColor, trackColor = accentColor)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem("Pending", pending.toString(), Color(0xFFEF4444), textColor)
                StatItem("Submitted", submitted.toString(), Color(0xFFF59E0B), textColor)
                StatItem("Graded", graded.toString(), Color(0xFF10B981), textColor)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}
