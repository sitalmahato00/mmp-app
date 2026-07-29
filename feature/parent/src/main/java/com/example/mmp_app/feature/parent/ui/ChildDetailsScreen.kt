package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.ChildDetailDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailsScreen(
    childId: Int,
    onBack: () -> Unit,
    onNavigateToAttendance: (Int) -> Unit,
    onNavigateToMarks: (Int) -> Unit,
    onNavigateToAssignments: (Int) -> Unit,
    onNavigateToTimetable: (Int) -> Unit,
    viewModel: ChildDetailViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(childId) {
        viewModel.initChildId(childId)
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.childDetail?.name ?: "Child Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.childDetail == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.childDetail == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadAllData() }) {
                        Text("Retry")
                    }
                }
            } else if (uiState.childDetail != null) {
                val child = uiState.childDetail!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TOP INFO CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Large avatar
                            Surface(
                                modifier = Modifier.size(100.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (child.avatarUrl?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = child.avatarUrl,
                                        contentDescription = child.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = child.name.firstOrNull()?.toString() ?: "",
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = child.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            StatusBadge(status = child.status)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Info rows
                            InfoRow(icon = Icons.Rounded.School, label = "Program", value = child.program ?: "N/A", isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.AccountBalance, label = "Department", value = child.department ?: "N/A", isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.Badge, label = "Student No", value = child.studentNo, isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.Numbers, label = "Roll Number", value = child.rollNumber ?: "N/A", isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.Description, label = "Reg. Number", value = child.registrationNumber ?: "N/A", isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.CalendarToday, label = "Semester", value = "${child.semester} | Section: ${child.section ?: "N/A"}", isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.TrackChanges, label = "Batch", value = child.batch ?: "N/A", isDarkTheme = isDarkTheme)
                            InfoRow(icon = Icons.Rounded.EventAvailable, label = "Admitted", value = child.admissionDate ?: "N/A", isDarkTheme = isDarkTheme)
                        }
                    }

                    // BOTTOM ACTION BUTTONS (2x2 grid)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ActionCard(
                                title = "Attendance",
                                icon = Icons.Rounded.BarChart,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToAttendance(child.id) },
                                isDarkTheme = isDarkTheme
                            )
                            ActionCard(
                                title = "Marks",
                                icon = Icons.AutoMirrored.Rounded.Assignment,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToMarks(child.id) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ActionCard(
                                title = "Assignments",
                                icon = Icons.Rounded.Book,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToAssignments(child.id) },
                                isDarkTheme = isDarkTheme
                            )
                            ActionCard(
                                title = "Timetable",
                                icon = Icons.AutoMirrored.Rounded.EventNote,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigateToTimetable(child.id) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String?) {
    val currentStatus = status ?: "Unknown"
    val backgroundColor = if (currentStatus.lowercase() == "active") Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
    val textColor = if (currentStatus.lowercase() == "active") Color(0xFF166534) else Color(0xFF64748B)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = currentStatus.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, isDarkTheme: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isDarkTheme) Color(0xFF6366F1) else Color(0xFF2563EB)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF6366F1)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
            )
        }
    }
}
