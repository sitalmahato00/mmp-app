package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.ChildDetailDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailsScreen(
    childId: Int,
    onBack: () -> Unit,
    onNavigateToAttendance: (Int) -> Unit,
    onNavigateToAssignments: (Int) -> Unit,
    onNavigateToResults: (Int) -> Unit,
    onNavigateToInfo: (Int) -> Unit,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Student Profile Card
            StudentProfileCard(
                name = "Loading...", 
                semester = "Semester",
                rollNo = "Roll No:",
                avatarUrl = null
            )

            // Quick Actions Grid (4 white cards with icons)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionSquare("Attendance", Icons.Rounded.CalendarMonth, Modifier.weight(1f)) { onNavigateToAttendance(childId) }
                ActionSquare("Assignment", Icons.Rounded.Assignment, Modifier.weight(1f)) { onNavigateToAssignments(childId) }
                ActionSquare("Results", Icons.Rounded.Star, Modifier.weight(1f)) { onNavigateToResults(childId) }
                ActionSquare("Info", Icons.Rounded.Info, Modifier.weight(1f)) { onNavigateToInfo(childId) }
            }

            // Two Cards for Tasks and Exams
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallStatCard("Pending Tasks", "0", Icons.Rounded.Assignment, Color(0xFFFFEBEE), Color(0xFFD32F2F), Modifier.weight(1f))
                SmallStatCard("Total Exams", "0", Icons.Rounded.Quiz, Color(0xFFE8EAF6), Color(0xFF3F51B5), Modifier.weight(1f))
            }

            // Assignment Progress Card
            AssignmentProgressCard(isDarkTheme)
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StudentProfileCard(name: String, semester: String, rollNo: String, avatarUrl: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE0F2FE).copy(alpha = 0.5f), Color(0xFFEEF2FF).copy(alpha = 0.8f))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Student Profile", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                    Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("• $semester", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                    Text(rollNo, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Color(0xFF2563EB)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                    }
                }
                
                Surface(
                    modifier = Modifier.size(86.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(model = avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(20.dp), tint = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionSquare(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Icon(icon, contentDescription = title, modifier = Modifier.padding(10.dp), tint = Color(0xFF2563EB))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun SmallStatCard(title: String, value: String, icon: ImageVector, iconBg: Color, iconTint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = iconBg) {
                    Icon(icon, contentDescription = null, modifier = Modifier.padding(6.dp), tint = iconTint)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun AssignmentProgressCard(isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Assignment Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Color(0xFF2563EB).copy(alpha = 0.2f),
                trackColor = Color(0xFFEFF6FF)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ProgressItem("0", "Pending", Color(0xFFEF4444))
                ProgressItem("0", "Submitted", Color(0xFFF59E0B))
                ProgressItem("0", "Graded", Color(0xFF10B981))
            }
        }
    }
}

@Composable
fun ProgressItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
        }
    }
}
