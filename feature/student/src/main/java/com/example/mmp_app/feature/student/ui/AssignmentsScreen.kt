package com.example.mmp_app.feature.student.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    onBack: () -> Unit,
    showSystemHeader: Boolean = true,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val viewModel: StudentAssignmentsViewModel = hiltViewModel()
    val assignmentsState by viewModel.assignments.collectAsState()
    val detailState by viewModel.assignmentDetail.collectAsState()

    var selectedAssignmentId by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAssignments()
    }

    val primaryColor = Color(0xFF1565C0)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF5F7FA)

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text(if (selectedAssignmentId == null) "My Assignments" else "Assignment Detail") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectedAssignmentId != null) {
                                selectedAssignmentId = null
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                        }
                        IconButton(onClick = { 
                            if (selectedAssignmentId == null) viewModel.loadAssignments() 
                            else viewModel.loadAssignmentDetail(selectedAssignmentId!!)
                        }) {
                            Icon(Icons.Rounded.Refresh, "Refresh")
                        }
                    }
                )
            }
        },
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AnimatedContent(targetState = selectedAssignmentId, label = "screen_transition") { id ->
                if (id == null) {
                    StudentAssignmentsList(
                        state = assignmentsState,
                        onAssignmentClick = { assignmentId ->
                            selectedAssignmentId = assignmentId
                            viewModel.loadAssignmentDetail(assignmentId)
                        },
                        onRetry = { viewModel.loadAssignments() }
                    )
                } else {
                    StudentAssignmentDetailView(
                        state = detailState,
                        onRetry = { viewModel.loadAssignmentDetail(id) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAssignmentsList(
    state: UiState<StudentAssignmentsResponse>,
    onAssignmentClick: (Int) -> Unit,
    onRetry: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ALL", "PENDING", "SUBMITTED", "GRADED")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (state) {
            is UiState.Loading -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(5) { SkeletonBox(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp)) }
                }
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.WifiOff, null, modifier = Modifier.size(60.dp), tint = Color.Red)
                    Text(state.message, modifier = Modifier.padding(16.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
            is UiState.Success -> {
                val assignments = state.data.data
                val filteredList = when (selectedTab) {
                    1 -> assignments.filter { it.status == "not_submitted" }
                    2 -> assignments.filter { it.status == "submitted" }
                    3 -> assignments.filter { it.status == "graded" }
                    else -> assignments
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No assignments yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList, key = { it.id }) { assignment ->
                            StudentAssignmentCard(assignment, onClick = { onAssignmentClick(assignment.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentCard(
    assignment: StudentAssignmentItemDto,
    onClick: () -> Unit
) {
    val isOverdue = assignment.isOverdue && assignment.status == "not_submitted"
    val isGraded = assignment.status == "graded"
    
    val backgroundColor = when {
        isOverdue -> Color(0xFFFFEBEE)
        isGraded -> Color(0xFFE8F5E9)
        else -> Color.White
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
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
                StatusChip(assignment.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = assignment.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            val description = assignment.description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description!!,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            val dueDateColor = getDueDateColor(assignment.dueDate, assignment.isOverdue)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(16.dp), tint = dueDateColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Due: ${formatDate(assignment.dueDate)}", color = dueDateColor, style = MaterialTheme.typography.labelSmall)
                
                if (assignment.attachmentUrl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Rounded.AttachFile, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
            }

            assignment.submission?.let { submission ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!submission.studentNote.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Description, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text("Note submitted", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    if (submission.attachmentUrl != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AttachFile, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text("File submitted", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    if (submission.marksObtained != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Graded: ${submission.marksObtained} / ${assignment.maxMarks ?: "-"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "View Details",
                color = Color(0xFF1565C0),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun StudentAssignmentDetailView(
    state: UiState<StudentAssignmentDetailDto>,
    onRetry: () -> Unit,
    viewModel: StudentAssignmentsViewModel
) {
    val context = LocalContext.current

    when (state) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        is UiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.message, modifier = Modifier.padding(16.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
        is UiState.Success -> {
            val detail = state.data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF1565C0).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = detail.subjectCode ?: "N/A",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFF1565C0),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(text = detail.subject ?: "", color = Color(0xFF1565C0), style = MaterialTheme.typography.labelLarge)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = detail.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarToday, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Due Date: ${formatDate(detail.dueDate)}", style = MaterialTheme.typography.bodyMedium)
                }
                if (detail.maxMarks != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Score, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Maximum Marks: ${detail.maxMarks}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Assignment Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val description = detail.description
                Text(text = description ?: "No description provided.", style = MaterialTheme.typography.bodyLarge)

                val attachmentUrl = detail.attachmentUrl
                if (attachmentUrl != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AttachmentPreview(url = attachmentUrl, label = "View Assignment File")
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(32.dp))

                // Submission Section
                val submission = detail.submission
                if (submission == null) {
                    SubmitWorkSection(assignmentId = detail.id, viewModel = viewModel)
                } else {
                    YourSubmissionSection(submission = submission, maxMarks = detail.maxMarks)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SubmitWorkSection(
    assignmentId: Int,
    viewModel: StudentAssignmentsViewModel
) {
    val context = LocalContext.current
    var note by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fileUri = it
            fileName = getFileName(context, it)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Submit Your Work", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 500) note = it },
                label = { Text("Your Note") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add notes about your work...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "${note.length}/500",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Attach File (optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.05f))
                    .clickable { filePicker.launch("*/*") }
                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (fileName == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CloudUpload, null, tint = Color.Gray)
                        Text("Tap to attach your file or image", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Icon(Icons.Rounded.Description, null, tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(8.dp))
                        Text(text = fileName!!, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        IconButton(onClick = { fileUri = null; fileName = null }) {
                            Icon(Icons.Rounded.Close, null, tint = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    isSubmitting = true
                    viewModel.submitAssignment(
                        assignmentId = assignmentId,
                        note = note.ifBlank { null },
                        fileUri = fileUri,
                        context = context,
                        onSuccess = {
                            isSubmitting = false
                            Toast.makeText(context, "Assignment submitted!", Toast.LENGTH_SHORT).show()
                            viewModel.loadAssignmentDetail(assignmentId)
                        },
                        onError = {
                            isSubmitting = false
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting && (note.isNotBlank() || fileUri != null),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Submit Assignment")
            }
        }
    }
}

@Composable
fun YourSubmissionSection(
    submission: SubmissionBriefDto,
    maxMarks: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Your Submission", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                StatusChip(if (submission.marksObtained != null) "graded" else "submitted")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text = "Submitted on: ${submission.submittedAt}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            val studentNote = submission.studentNote
            if (!studentNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Your Note:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = studentNote!!, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            val submittedAttachment = submission.attachmentUrl
            if (submittedAttachment != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Your Attachment:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                AttachmentPreview(url = submittedAttachment, label = "View Submitted File")
            }

            if (submission.marksObtained != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Marks: ${submission.marksObtained} / ${maxMarks ?: "-"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                
                val feedback = submission.teacherFeedback
                if (!feedback.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Teacher's Feedback:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFFFF9C4),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = feedback!!, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You can only submit once. Contact your teacher to resubmit.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun AttachmentPreview(url: String, label: String) {
    val context = LocalContext.current
    val isImage = url.lowercase().let { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") || it.endsWith(".gif") }
    
    if (isImage) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
            contentScale = ContentScale.Crop
        )
    } else {
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Rounded.Description, null)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, label) = when (status.lowercase()) {
        "not_submitted" -> Color(0xFFD32F2F) to "Not Submitted"
        "submitted" -> Color(0xFF1976D2) to "Submitted"
        "graded" -> Color(0xFF388E3C) to "Graded ✓"
        else -> Color.Gray to status.replaceFirstChar { it.uppercase() }
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getDueDateColor(dueDateStr: String, isOverdue: Boolean): Color {
    if (isOverdue) return Color(0xFFD32F2F)
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dueDate = sdf.parse(dueDateStr)
        val now = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        val diff = (dueDate?.time ?: 0) - now.time
        val days = diff / (24 * 60 * 60 * 1000)
        if (days <= 3) Color(0xFFE65100) else Color(0xFF388E3C)
    } catch (e: Exception) {
        Color.Gray
    }
}

fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date ?: "")
    } catch (e: Exception) {
        dateStr
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = it.getString(index)
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}
