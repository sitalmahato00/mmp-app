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
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.TeacherSubjectDto
import com.example.mmp_app.domain.model.UserDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassesScreen(
    onBack: () -> Unit,
    onNavigateToAttendance: (Int, String) -> Unit,
    onNavigateToMarks: (Int, String) -> Unit,
    showSystemHeader: Boolean = true
) {
    val viewModel: TeacherViewModel = hiltViewModel()
    val classes by viewModel.teacherClasses.collectAsState()
    val markComponentsMap by viewModel.markComponents.collectAsState()
    val subjectStudentsMap by viewModel.subjectStudents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var selectedSubject by remember { mutableStateOf<TeacherSubjectDto?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTeacherClasses()
    }

    val primaryColor = Color(0xFF1565C0)
    val lightBlue = Color(0xFFE3F2FD)
    val backgroundColor = Color(0xFFF5F7FA)

    val content = @Composable { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading && classes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (error != null && classes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.WifiOff, null, modifier = Modifier.size(60.dp), tint = Color.Red)
                        Text(text = error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.loadTeacherClasses() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Banner
                    item {
                        ClassesSummaryBanner(
                            subjectCount = classes.size,
                            primaryColor = primaryColor,
                            lightBlue = lightBlue
                        )
                    }

                    if (classes.isEmpty()) {
                        item {
                            EmptyClassesState(primaryColor)
                        }
                    } else {
                        items(classes) { subject ->
                            val components = markComponentsMap[subject.id] ?: emptyList()
                            val studentsCount = subjectStudentsMap[subject.id]?.size ?: 0
                            
                            SubjectCard(
                                subject = subject,
                                studentsCount = studentsCount,
                                markComponents = components,
                                primaryColor = primaryColor,
                                onClick = {
                                    selectedSubject = subject
                                    showSheet = true
                                },
                                onAttendanceClick = { onNavigateToAttendance(subject.id, subject.name) },
                                onMarksClick = { onNavigateToMarks(subject.id, subject.name) }
                            )
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
                    title = {
                        Column {
                            Text("My Classes", fontWeight = FontWeight.Bold)
                            Surface(
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${classes.size} Subjects",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.loadTeacherClasses() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = backgroundColor
        ) { padding ->
            content(padding)
        }
    } else {
        content(PaddingValues(0.dp))
    }

    if (showSheet && selectedSubject != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            SubjectDetailSheetContent(
                subject = selectedSubject!!,
                components = markComponentsMap[selectedSubject!!.id] ?: emptyList(),
                students = subjectStudentsMap[selectedSubject!!.id] ?: emptyList(),
                primaryColor = primaryColor,
                onAttendanceClick = {
                    showSheet = false
                    onNavigateToAttendance(selectedSubject!!.id, selectedSubject!!.name)
                },
                onMarksClick = {
                    showSheet = false
                    onNavigateToMarks(selectedSubject!!.id, selectedSubject!!.name)
                }
            )
        }
    }
}

@Composable
fun ClassesSummaryBanner(subjectCount: Int, primaryColor: Color, lightBlue: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = lightBlue)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryStatBox(
                icon = Icons.Rounded.Book,
                value = "$subjectCount",
                label = "Total Subjects",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(primaryColor.copy(alpha = 0.2f)))
            SummaryStatBox(
                icon = Icons.Rounded.Groups,
                value = "–",
                label = "Students",
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryStatBox(icon: ImageVector, value: String, label: String, primaryColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = primaryColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = primaryColor)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun SubjectCard(
    subject: TeacherSubjectDto,
    studentsCount: Int,
    markComponents: List<String>,
    primaryColor: Color,
    onClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onMarksClick: () -> Unit
) {
    val colors = listOf(Color(0xFF1565C0), Color(0xFFF57C00), Color(0xFF388E3C), Color(0xFF7B1FA2), Color(0xFFC62828))
    val cardColor = colors[subject.id % colors.size]
    val icon = when(subject.id % 3) {
        0 -> Icons.Rounded.Code
        1 -> Icons.Rounded.Web
        else -> Icons.Rounded.Book
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = cardColor.copy(alpha = 0.1f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = cardColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = subject.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Surface(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Permanent", // Placeholder for employment_type
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                SubjectStatCol(Icons.Rounded.People, "$studentsCount", "Students", Modifier.weight(1f))
                SubjectStatCol(Icons.Rounded.Assignment, "0", "Assignments", Modifier.weight(1f))
                SubjectStatCol(Icons.Rounded.CheckCircle, "0", "Attendance", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onMarksClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
                ) {
                    Icon(Icons.Rounded.EditNote, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enter Marks", fontSize = 13.sp)
                }
                Button(
                    onClick = onAttendanceClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mark Attendance", fontSize = 13.sp)
                }
            }

            if (markComponents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    markComponents.take(2).forEach { component ->
                        MarkComponentChip(component)
                    }
                    if (markComponents.size > 2) {
                        MarkComponentChip("+${markComponents.size - 2}")
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectStatCol(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun MarkComponentChip(component: String) {
    val displayName = component.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    Surface(
        color = Color.LightGray.copy(alpha = 0.2f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}

@Composable
fun SubjectDetailSheetContent(
    subject: TeacherSubjectDto,
    components: List<String>,
    students: List<com.example.mmp_app.domain.model.StudentItemDto>,
    primaryColor: Color,
    onAttendanceClick: () -> Unit,
    onMarksClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = primaryColor.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Rounded.Book, null, modifier = Modifier.padding(14.dp), tint = primaryColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = subject.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = subject.code, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Mark Components", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(components) { MarkComponentChip(it) }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Students", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        if (students.isEmpty()) {
            Text(text = "No students assigned yet", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                students.take(5).forEach { student ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = student.name, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Roll: –", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onMarksClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enter Marks")
            }
            Button(
                onClick = onAttendanceClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Mark Attendance")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EmptyClassesState(primaryColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.School, null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No subjects assigned yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "Contact admin to get subjects assigned", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}
