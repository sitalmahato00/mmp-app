package com.example.mmp_app.feature.teacher.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.SubmissionItemDto
import com.example.mmp_app.domain.model.SubmissionsDataDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentSubmissionsScreen(
    assignmentId: Int,
    onBack: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val viewModel: TeacherAssignmentsViewModel = hiltViewModel()
    val state by viewModel.submissionsState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(assignmentId) {
        viewModel.loadSubmissions(assignmentId)
    }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8F9FF)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text("Submissions", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
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
                        Button(onClick = { viewModel.loadSubmissions(assignmentId) }) {
                            Text("Retry")
                        }
                    }
                }
                is UiState.Success -> {
                    val data = currentState.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AssignmentInfoCard(data.assignment, primaryColor, cardBgColor, textColor)
                        }
                        
                        item {
                            Text(
                                text = "${data.submissions.size} / ${data.total} submitted",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        if (data.submissions.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("No submissions yet", color = Color.Gray)
                                }
                            }
                        } else {
                            items(data.submissions) { submission ->
                                SubmissionCard(
                                    submission = submission,
                                    maxMarks = data.assignment.maxMarks ?: 100.0,
                                    primaryColor = primaryColor,
                                    cardBgColor = cardBgColor,
                                    textColor = textColor,
                                    isDarkTheme = isDarkTheme,
                                    onGradeSuccess = { viewModel.loadSubmissions(assignmentId) }
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
fun AssignmentInfoCard(
    assignment: com.example.mmp_app.domain.model.AssignmentBriefDto, 
    primaryColor: Color,
    cardBgColor: Color,
    textColor: Color
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = assignment.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(text = "Due: ${assignment.dueDate}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Max Marks: ${assignment.maxMarks ?: "–"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            
            if (assignment.attachmentUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(assignment.attachmentUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.AttachFile, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Assignment File")
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(
    submission: SubmissionItemDto,
    maxMarks: Double,
    primaryColor: Color,
    cardBgColor: Color,
    textColor: Color,
    isDarkTheme: Boolean,
    onGradeSuccess: () -> Unit
) {
    val context = LocalContext.current
    var showGradeDialog by remember { mutableStateOf(false) }

    val statusColor = when (submission.status.lowercase()) {
        "graded" -> Color(0xFF2E7D32)
        "late" -> Color(0xFFE65100)
        else -> Color(0xFF1565C0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.LightGray.copy(alpha = 0.3f)
                ) {
                    AsyncImage(
                        model = submission.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = rememberVectorPainter(Icons.Rounded.Person)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = submission.studentName ?: "Unknown", fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = submission.studentNo ?: "–", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = submission.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Submitted: ${submission.submittedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            val studentNote = submission.studentNote
            if (!studentNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\"$studentNote\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            val attachmentUrl = submission.attachmentUrl
            if (attachmentUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val isImage = attachmentUrl.let { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".gif") }
                if (isImage) {
                    AsyncImage(
                        model = attachmentUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachmentUrl))
                                context.startActivity(intent)
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachmentUrl))
                                context.startActivity(intent)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Description, null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "View Document", modifier = Modifier.weight(1f), color = textColor)
                        Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Marks: ${submission.marksObtained ?: "–"} / $maxMarks",
                    fontWeight = FontWeight.Bold,
                    color = if (submission.marksObtained != null) Color(0xFF2E7D32) else textColor
                )
                Spacer(modifier = Modifier.weight(1f))
                if (submission.status.lowercase() != "graded") {
                    Button(
                        onClick = { showGradeDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Grade", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!submission.teacherFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Feedback: ${submission.teacherFeedback}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }

    if (showGradeDialog) {
        GradeDialog(
            submissionId = submission.id,
            maxMarks = maxMarks,
            onDismiss = { showGradeDialog = false },
            onSuccess = {
                showGradeDialog = false
                onGradeSuccess()
            }
        )
    }
}

@Composable
fun GradeDialog(
    submissionId: Int,
    maxMarks: Double,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val viewModel: TeacherAssignmentsViewModel = hiltViewModel()
    var marks by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grade Submission") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = marks,
                    onValueChange = { if (it.isEmpty() || (it.toDoubleOrNull() ?: 0.0) <= maxMarks) marks = it },
                    label = { Text("Marks Obtained (Max $maxMarks)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Feedback (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marksVal = marks.toDoubleOrNull()
                    if (marksVal == null) {
                        Toast.makeText(context, "Please enter valid marks", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.gradeSubmission(
                        submissionId = submissionId,
                        marks = marksVal,
                        feedback = feedback,
                        onSuccess = {
                            isSubmitting = false
                            onSuccess()
                        },
                        onError = {
                            isSubmitting = false
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Save Grade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
