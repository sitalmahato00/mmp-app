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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.domain.model.ParentAssignmentDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAssignmentsScreen(
    childId: Int,
    onBack: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val primaryColor = Color(0xFF6366F1)

    // Mock data for assignments
    val assignments = listOf(
        ParentAssignmentDto(1, "Database Normalization", "DBMS", "2023-11-15", "Pending", null, null, "Explain 1NF, 2NF and 3NF with examples."),
        ParentAssignmentDto(2, "Kotlin Coroutines", "Mobile App Dev", "2023-11-10", "Submitted", "18/20", "Good work on the implementation.", "Implement a simple flow with coroutines."),
        ParentAssignmentDto(3, "Network Security Essay", "Cyber Security", "2023-11-05", "Graded", "15/20", "Could be more detailed.", "Write a 500-word essay on modern network threats.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignments", fontWeight = FontWeight.Bold) },
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
            AssignmentFilterTabs(isDarkTheme)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assignments) { assignment ->
                    ParentAssignmentCard(assignment, isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun AssignmentFilterTabs(isDarkTheme: Boolean) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Pending", "Submitted", "Graded")
    
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
        contentColor = Color(0xFF6366F1),
        edgePadding = 16.dp,
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(title) }
            )
        }
    }
}

@Composable
fun ParentAssignmentCard(assignment: ParentAssignmentDto, isDarkTheme: Boolean) {
    val statusColor = when (assignment.status.lowercase()) {
        "pending" -> Color(0xFFEF4444)
        "submitted" -> Color(0xFFF59E0B)
        "graded" -> Color(0xFF10B981)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = assignment.subject ?: "General",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                StatusBadge(assignment.status, statusColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Due: ${assignment.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                if (assignment.marks != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Marks: ${assignment.marks}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            if (!assignment.feedback.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.Gray.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Teacher's Feedback:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(assignment.feedback!!, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
