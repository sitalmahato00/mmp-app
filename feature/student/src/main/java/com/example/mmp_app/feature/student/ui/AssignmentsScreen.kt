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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.AssignmentDetailDto
import com.example.mmp_app.domain.model.AssignmentDto
import com.example.mmp_app.domain.model.SubmissionStatusDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    onBack: () -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedAssignmentId by remember { mutableStateOf<Int?>(null) }
    val assignmentDetail by viewModel.assignmentDetail.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStudentAssignments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedAssignmentId == null) "Assignments" else "Assignment Detail") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedAssignmentId != null) {
                            selectedAssignmentId = null
                            viewModel.clearAssignmentDetail()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(targetState = selectedAssignmentId, label = "screen_transition") { id ->
                if (id == null) {
                    AssignmentsList(
                        assignments = assignments,
                        isLoading = isLoading,
                        onAssignmentClick = { 
                            selectedAssignmentId = it.id
                            viewModel.loadAssignmentDetail(it.id)
                        }
                    )
                } else {
                    AssignmentDetailView(
                        assignmentId = id,
                        detail = assignmentDetail,
                        isLoading = isLoading,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsList(
    assignments: List<AssignmentDto>,
    isLoading: Boolean,
    onAssignmentClick: (AssignmentDto) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Submitted", "All")

    val filteredAssignments = remember(assignments, selectedTab) {
        val sorted = assignments.sortedWith(compareBy<AssignmentDto> { 
            // Simple overdue check (naive)
            val isOverdue = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val dueDate = sdf.parse(it.dueDate)
                dueDate?.before(Date()) ?: false
            } catch (e: Exception) { false }
            if (isOverdue && it.status == "not_submitted") 0 else 1
        }.thenBy { it.dueDate })

        when (selectedTab) {
            0 -> sorted.filter { it.status == "not_submitted" || it.status == "pending" }
            1 -> sorted.filter { it.status == "submitted" || it.status == "graded" }
            else -> sorted
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (isLoading && assignments.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FF)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(8) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        } else if (filteredAssignments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Assignment, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No assignments found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FF)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAssignments, key = { it.id }) { assignment ->
                    AssignmentItem(assignment, onClick = { onAssignmentClick(assignment) })
                }
            }
        }
    }
}

@Composable
fun AssignmentItem(assignment: AssignmentDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = assignment.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = assignment.subject ?: "No Subject", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Due: ${formatDate(assignment.dueDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (!assignment.obtainedMarks.isNullOrEmpty()) {
                    Text(
                        text = "Marks: ${assignment.obtainedMarks}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            StatusBadge(status = assignment.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, label) = when (status.lowercase()) {
        "not_submitted" -> Color.Red to "Not Submitted"
        "pending" -> Color(0xFFE65100) to "Pending"
        "submitted" -> Color(0xFF1976D2) to "Submitted"
        "graded" -> Color(0xFF2E7D32) to "Graded"
        else -> MaterialTheme.colorScheme.outline to status.replaceFirstChar { it.uppercase() }
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

@Composable
fun AssignmentDetailView(
    assignmentId: Int,
    detail: AssignmentDetailDto?,
    isLoading: Boolean,
    viewModel: StudentViewModel
) {
    val context = LocalContext.current
    var showSubmitSheet by remember { mutableStateOf(false) }

    if (isLoading && detail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (detail != null) {
        LaunchedEffect(assignmentId) {
            val assignments = viewModel.assignments.value
            val currentStatus = assignments.find { it.id == assignmentId }?.status ?: "not_submitted"
            if (currentStatus == "submitted" || currentStatus == "graded") {
                viewModel.loadSubmissionStatus(assignmentId) // This is a simplification, in real app you might need submissionId
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = detail.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = detail.subject ?: "General", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    DetailRow(icon = Icons.Rounded.Event, label = "Due Date", value = formatDate(detail.dueDate))
                    DetailRow(icon = Icons.Rounded.Score, label = "Max Marks", value = detail.maxMarks?.toString() ?: "N/A")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = detail.description ?: "No description provided.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!detail.attachmentUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(detail.attachmentUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Teacher Attachment")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Submission Section
            val assignments = viewModel.assignments.collectAsState().value
            val currentStatus = assignments.find { it.id == assignmentId }?.status ?: "not_submitted"

            if (currentStatus == "submitted" || currentStatus == "graded") {
                SubmissionStatusSection(viewModel, assignmentId)
            } else {
                Button(
                    onClick = { showSubmitSheet = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Assignment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSubmitSheet) {
        SubmitAssignmentSheet(
            onDismiss = { showSubmitSheet = false },
            onSubmit = { content, fileUri ->
                if (fileUri != null) {
                    val part = uriToMultipart(fileUri, context)
                    viewModel.submitAssignment(assignmentId, content, part)
                } else {
                    viewModel.submitAssignment(assignmentId, content)
                }
                showSubmitSheet = false
            }
        )
    }
}

@Composable
fun SubmissionStatusSection(viewModel: StudentViewModel, assignmentId: Int) {
    val submissionStatus by viewModel.submissionStatus.collectAsState()
    
    // Note: submissionId is needed but if we don't have it, we show general status
    // In a real flow, we'd fetch it using getSubmissionStatus if we had the ID

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(Modifier.width(8.dp))
                Text("Submission Received", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (submissionStatus != null) {
                Text(text = "Marks: ${submissionStatus?.marksObtained ?: "Pending"}", style = MaterialTheme.typography.bodyLarge)
                if (!submissionStatus?.feedback.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Feedback:", fontWeight = FontWeight.Bold)
                    Text(text = submissionStatus?.feedback ?: "")
                }
            } else {
                Text("Your assignment has been submitted and is awaiting grading.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAssignmentSheet(
    onDismiss: () -> Unit,
    onSubmit: (String?, Uri?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        fileName = uri?.let { getFileName(context, it) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text("Submit Assignment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Add a note (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Type your submission notes here...") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedCard(
                onClick = {
                    filePickerLauncher.launch("*/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(text = fileName ?: "Attach File", fontWeight = FontWeight.Bold)
                        Text(text = if (fileName == null) "PDF, Word, or Images (max 10MB)" else "Tap to change file", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onSubmit(content.ifBlank { null }, selectedFileUri) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = content.isNotBlank() || selectedFileUri != null
            ) {
                Text("Confirm Submission")
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

fun formatDate(dateStr: String): String {
    return try {
        // Handle various formats or fallback to original if parsing fails
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date ?: "")
    } catch (e: Exception) {
        // Fallback for Nepali date strings or other non-ISO formats
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

fun uriToMultipart(uri: Uri, context: Context): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    val fileName = getFileName(context, uri) ?: "upload.file"
    val inputStream = contentResolver.openInputStream(uri)!!
    val bytes = inputStream.readBytes()
    val requestBody = bytes.toRequestBody(mimeType.toMediaType())
    return MultipartBody.Part.createFormData("file", fileName, requestBody)
}
