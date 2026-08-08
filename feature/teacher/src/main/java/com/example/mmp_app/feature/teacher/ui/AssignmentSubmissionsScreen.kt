package com.example.mmp_app.feature.teacher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.SubmissionItemDto
import com.example.mmp_app.domain.model.SubmissionsDataDto
import com.example.mmp_app.domain.model.AssignmentBriefDto

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
    val expandedForms by viewModel.expandedGradeForms.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(assignmentId) {
        viewModel.loadSubmissions(assignmentId)
    }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF5F7FA)
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        repeat(3) {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(bottom = 12.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)))
                        }
                    }
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
                    val gradedCount = data.submissions.count { it.status.lowercase() == "graded" }
                    val pendingCount = data.submissions.size - gradedCount

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AssignmentHeaderCard(
                                assignment = data.assignment,
                                submitted = data.submissions.size,
                                graded = gradedCount,
                                pending = pendingCount,
                                isDarkTheme = isDarkTheme,
                                cardBgColor = cardBgColor,
                                textColor = textColor
                            )
                        }

                        if (data.submissions.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Rounded.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                    Text("No submissions yet", fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text("Students haven't submitted this assignment yet", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        } else {
                            items(data.submissions, key = { it.id }) { submission ->
                                SubmissionCard(
                                    submission = submission,
                                    maxMarks = data.assignment.maxMarks,
                                    isExpanded = expandedForms.contains(submission.id),
                                    onToggleExpand = { viewModel.toggleGradeForm(submission.id) },
                                    onGrade = { marks, feedback, onDone, onError ->
                                        viewModel.gradeSubmission(submission.id, marks, feedback, { onDone() }, onError)
                                    },
                                    isDarkTheme = isDarkTheme,
                                    cardBgColor = cardBgColor,
                                    textColor = textColor
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentHeaderCard(
    assignment: AssignmentBriefDto,
    submitted: Int,
    graded: Int,
    pending: Int,
    isDarkTheme: Boolean,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Due: ${formatAssignmentDate(assignment.dueDate)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Rounded.Score, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Max Marks: ${assignment.maxMarks ?: "No max marks"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$submitted submitted • $graded graded • $pending pending",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color(0xFF818CF8) else Color(0xFF1565C0)
                )
            }
            
            if (assignment.attachmentUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { openUrl(context, assignment.attachmentUrl!!) },
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
    maxMarks: Double?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onGrade: (Double, String, () -> Unit, (String) -> Unit) -> Unit,
    isDarkTheme: Boolean,
    cardBgColor: Color,
    textColor: Color
) {
    val context = LocalContext.current
    val status = submission.status.lowercase()
    val isGraded = status == "graded"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Student Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Color.LightGray.copy(alpha = 0.3f)
                ) {
                    if (!submission.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = submission.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            val initial = submission.studentName?.firstOrNull()?.uppercase() ?: "?"
                            Text(text = initial.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = submission.studentName ?: "Unknown Student", fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = submission.studentNo ?: "–", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(status)
                    Text(text = formatDateShort(submission.submittedAt), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student Note
            if (!submission.studentNote.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ChatBubbleOutline, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text("Student's Note", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = submission.studentNote!!, style = MaterialTheme.typography.bodySmall, color = textColor)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Submitted File
            Text("Submitted File", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            if (submission.attachmentUrl != null) {
                val type = getAttachmentType(submission.attachmentUrl)
                if (type == AttachmentType.IMAGE) {
                    AsyncImage(
                        model = submission.attachmentUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { openUrl(context, submission.attachmentUrl!!) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openUrl(context, submission.attachmentUrl!!) },
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Description, null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = submission.attachmentUrl!!.substringAfterLast('/'),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                            IconButton(onClick = { openUrl(context, submission.attachmentUrl!!) }) {
                                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(20.dp), tint = Color(0xFF1565C0))
                            }
                        }
                    }
                }
            } else {
                Text("No file attached", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Grade Section
            if (isGraded && !isExpanded) {
                Surface(
                    color = if (isDarkTheme) Color(0xFF064E3B) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Graded", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Marks: ${submission.marksObtained} / ${maxMarks ?: "–"}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onToggleExpand) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Grade", fontSize = 12.sp)
                        }
                    }
                }
                
                if (!submission.teacherFeedback.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isDarkTheme) Color(0xFF78350F).copy(alpha = 0.3f) else Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Feedback, null, modifier = Modifier.size(14.dp), tint = Color(0xFFF57C00))
                                Spacer(Modifier.width(6.dp))
                                Text("Your Feedback", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = submission.teacherFeedback!!, style = MaterialTheme.typography.bodySmall, color = textColor)
                        }
                    }
                }
            } else {
                GradeForm(
                    initialMarks = submission.marksObtained?.toString() ?: "",
                    initialFeedback = submission.teacherFeedback ?: "",
                    maxMarks = maxMarks,
                    isEdit = isGraded,
                    onSave = { marks, feedback, onDone, onError ->
                        onGrade(marks, feedback, onDone, onError)
                    },
                    onCancel = if (isGraded) onToggleExpand else null,
                    cardBgColor = cardBgColor,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
fun GradeForm(
    initialMarks: String,
    initialFeedback: String,
    maxMarks: Double?,
    isEdit: Boolean,
    onSave: (Double, String, () -> Unit, (String) -> Unit) -> Unit,
    onCancel: (() -> Unit)?,
    cardBgColor: Color,
    textColor: Color
) {
    var marksInput by remember { mutableStateOf(initialMarks) }
    var feedbackInput by remember { mutableStateOf(initialFeedback) }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isEdit) "Update Grade" else "Grade This Submission",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = marksInput,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) marksInput = it },
                label = { Text("Marks Obtained") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("/ ${maxMarks ?: "–"}") },
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = feedbackInput,
            onValueChange = { feedbackInput = it },
            label = { Text("Feedback (optional)") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            if (onCancel != null) {
                TextButton(onClick = onCancel, enabled = !isSaving) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Button(
                onClick = {
                    val marksVal = marksInput.toDoubleOrNull()
                    if (marksVal == null || marksVal < 0) {
                        Toast.makeText(context, "Invalid marks", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (maxMarks != null && marksVal > maxMarks) {
                        Toast.makeText(context, "Marks cannot exceed $maxMarks", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isSaving = true
                    onSave(marksVal, feedbackInput, {
                        isSaving = false
                        Toast.makeText(context, "Grade saved successfully", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        isSaving = false
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    })
                },
                enabled = !isSaving && marksInput.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Grade")
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, label) = when (status.lowercase()) {
        "submitted" -> Color(0xFF1565C0) to "Submitted"
        "graded" -> Color(0xFF2E7D32) to "Graded ✓"
        "late" -> Color(0xFFE65100) to "Late"
        else -> Color.Gray to status.replaceFirstChar { it.uppercase() }
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getAttachmentType(url: String?): AttachmentType {
    if (url == null) return AttachmentType.NONE
    val ext = url.substringAfterLast('.').lowercase()
    return when (ext) {
        "jpg", "jpeg", "png", "gif", "webp" -> AttachmentType.IMAGE
        else -> AttachmentType.DOCUMENT
    }
}

enum class AttachmentType { NONE, IMAGE, DOCUMENT }

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
    }
}

fun formatDate(isoDate: String): String {
    return try {
        val datePart = isoDate.substringBefore('T')
        datePart
    } catch (e: Exception) { isoDate }
}

fun formatDateShort(isoDate: String): String {
    return try {
        val timePart = isoDate.substringAfter('T').take(5)
        val datePart = isoDate.substringBefore('T').drop(5) // MM-DD
        "$datePart $timePart"
    } catch (e: Exception) { isoDate }
}
