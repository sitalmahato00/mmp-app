package com.example.mmp_app.feature.teacher.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.AssignmentItemDto
import com.example.mmp_app.domain.model.AssignmentListResponse
import com.example.mmp_app.domain.model.AssignmentMetaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentsScreen(
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit = {},
    onViewSubmissions: (Int) -> Unit = {},
    showSystemHeader: Boolean = true,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val viewModel: TeacherAssignmentsViewModel = hiltViewModel()
    val state by viewModel.assignmentsState.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("ALL") }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = Color(0xFFF5F7FA)

    val content = @Composable { padding: PaddingValues ->
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
                        Text(currentState.message, color = Color.Red)
                        Button(onClick = { viewModel.loadAssignments() }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    val assignments = currentState.data.data
                    val meta = currentState.data.meta
                    
                    val filteredList = when(selectedFilter) {
                        "UPCOMING" -> assignments.filter { !it.isOverdue }
                        "OVERDUE" -> assignments.filter { it.isOverdue }
                        else -> assignments
                    }

                    Column {
                        // Summary Bar
                        if (meta != null) {
                            AssignmentSummaryBar(meta, primaryColor)
                        }

                        // Filter Tabs
                        FilterTabs(selectedFilter) { selectedFilter = it }

                        if (filteredList.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No assignments yet. Tap + to create one.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredList) { assignment ->
                                    AssignmentCard(
                                        assignment = assignment,
                                        onViewSubmissions = { onViewSubmissions(assignment.id) },
                                        onDelete = {
                                            viewModel.deleteAssignment(assignment.id, {}, {})
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSystemHeader) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Assignments", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                        }
                        IconButton(onClick = { viewModel.loadAssignments() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = primaryColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create Assignment")
                }
            },
            containerColor = backgroundColor,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            content(padding)
        }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
fun AssignmentSummaryBar(meta: AssignmentMetaDto, primaryColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip("Total: ${meta.total}", Color.Gray, Modifier.weight(1f))
        SummaryChip("Upcoming: ${meta.upcoming}", Color(0xFF2E7D32), Modifier.weight(1f))
        SummaryChip("Overdue: ${meta.overdue}", Color(0xFFC62828), Modifier.weight(1f))
    }
}

@Composable
fun SummaryChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun FilterTabs(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterTab("ALL", selected == "ALL") { onSelect("ALL") }
        FilterTab("UPCOMING", selected == "UPCOMING") { onSelect("UPCOMING") }
        FilterTab("OVERDUE", selected == "OVERDUE") { onSelect("OVERDUE") }
    }
}

@Composable
fun FilterTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val contentColor = if (isSelected) Color.White else Color.Gray
    val bgColor = if (isSelected) Color(0xFF1565C0) else Color.Transparent

    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .height(36.dp),
        color = bgColor,
        shape = RoundedCornerShape(18.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(label, color = contentColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentItemDto,
    onViewSubmissions: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dueDateColor = if (assignment.isOverdue) Color(0xFFC62828) else Color(0xFF2E7D32)
    val cardBg = if (assignment.isOverdue) Color(0xFFFFEBEE) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF1565C0).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = assignment.subjectCode ?: "N/A",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color(0xFF1565C0),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.MoreVert, null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { /* TODO */ },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = { 
                                showMenu = false
                                onDelete() 
                            },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = Color.Red) }
                        )
                        DropdownMenuItem(
                            text = { Text("View Submissions") },
                            onClick = { 
                                showMenu = false
                                onViewSubmissions() 
                            },
                            leadingIcon = { Icon(Icons.Rounded.People, null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = assignment.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            val description = assignment.description
            if (!description.isNullOrBlank()) {
                Text(text = description, color = Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp), tint = dueDateColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Due: ${assignment.dueDate}", color = dueDateColor, style = MaterialTheme.typography.labelMedium)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Icon(Icons.Rounded.People, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${assignment.submissionsCount} submissions", color = Color.Gray, style = MaterialTheme.typography.labelMedium)

                if (assignment.attachmentUrl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(assignment.attachmentUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Rounded.AttachFile, null, tint = Color(0xFF1565C0))
                    }
                }
            }
        }
    }
}
