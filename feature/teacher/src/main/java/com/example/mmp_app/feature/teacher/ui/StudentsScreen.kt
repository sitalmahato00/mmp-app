package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.StudentItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: StudentsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val displayStudents by viewModel.displayStudents.collectAsState()
    val selectedId by viewModel.selectedSubjectId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedStudent by remember { mutableStateOf<StudentItemDto?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = Color(0xFFF5F7FA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Rounded.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearch(it) },
                placeholder = { Text("Search students...") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearch("") }) {
                            Icon(Icons.Rounded.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = primaryColor
                )
            )

            // Subject filter chips
            if (subjects.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    // "All" chip
                    item {
                        FilterChip(
                            selected = selectedId == null,
                            onClick = { viewModel.selectSubject(null) },
                            label = { Text("All (${displayStudents.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    items(subjects, key = { it.id }) { subject ->
                        FilterChip(
                            selected = selectedId == subject.id,
                            onClick = { viewModel.selectSubject(subject.id) },
                            label = { Text(subject.name.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Content
            when (uiState) {
                is StudentsUiState.Loading -> StudentsLoadingState()
                is StudentsUiState.Error -> StudentsErrorState(
                    message = (uiState as StudentsUiState.Error).message,
                    onRetry = { viewModel.load() }
                )
                is StudentsUiState.Success -> {
                    if (displayStudents.isEmpty()) {
                        StudentsEmptyState(hasSearch = searchQuery.isNotBlank())
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = "${displayStudents.size} students found",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                )
                            }
                            items(displayStudents, key = { it.id }) { student ->
                                StudentCard(
                                    student = student,
                                    primaryColor = primaryColor,
                                    onClick = {
                                        selectedStudent = student
                                        showSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet && selectedStudent != null) {
        StudentDetailSheet(
            student = selectedStudent!!,
            primaryColor = primaryColor,
            onDismiss = {
                showSheet = false
                selectedStudent = null
            }
        )
    }
}

@Composable
fun StudentCard(
    student: StudentItemDto,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                color = Color(0xFFE3F2FD)
            ) {
                AsyncImage(
                    model = student.avatarUrl,
                    contentDescription = student.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.Person)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!student.studentNo.isNullOrBlank()) {
                        Text(
                            text = "# ${student.studentNo}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    if (!student.section.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = "Sec ${student.section}",
                                fontSize = 11.sp,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = student.email ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD1D5DB)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailSheet(
    student: StudentItemDto,
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large avatar
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(3.dp, primaryColor, CircleShape),
                color = Color(0xFFE3F2FD)
            ) {
                AsyncImage(
                    model = student.avatarUrl,
                    contentDescription = student.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.Person)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = student.name,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = student.email ?: "",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(24.dp))

            // Info rows
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StudentInfoRow(
                        icon = Icons.Rounded.Badge,
                        label = "Student ID",
                        value = student.studentNo ?: "–",
                        primaryColor = primaryColor
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))
                    StudentInfoRow(
                        icon = Icons.Rounded.Numbers,
                        label = "Roll Number",
                        value = student.rollNumber ?: "–",
                        primaryColor = primaryColor
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))
                    StudentInfoRow(
                        icon = Icons.Rounded.Groups,
                        label = "Section",
                        value = student.section ?: "–",
                        primaryColor = primaryColor
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StudentInfoRow(icon: ImageVector, label: String, value: String, primaryColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = primaryColor.copy(alpha = 0.1f)
        ) {
            Icon(
                icon, null,
                modifier = Modifier.padding(8.dp),
                tint = primaryColor
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF6B7280))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
        }
    }
}

@Composable
fun StudentsLoadingState() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun StudentsErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.WifiOff, null, Modifier.size(64.dp), tint = Color.Red.copy(alpha = 0.6f))
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text("Retry Connection")
        }
    }
}

@Composable
fun StudentsEmptyState(hasSearch: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasSearch) Icons.Rounded.SearchOff else Icons.Rounded.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (hasSearch) "No students match your search" else "No students found",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Gray
        )
        if (hasSearch) {
            Text(
                text = "Try adjusting your search or filters",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
