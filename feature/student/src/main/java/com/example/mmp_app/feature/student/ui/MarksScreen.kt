package com.example.mmp_app.feature.student.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksScreen(
    onBack: () -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()

    val summary by viewModel.marksSummary.collectAsState()
    val examDetail by viewModel.examDetail.collectAsState()
    val subjectMarks by viewModel.subjectMarks.collectAsState()
    val marksheet by viewModel.marksheet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var currentView by remember { mutableStateOf<MarksView>(MarksView.Summary) }
    var selectedId by remember { mutableStateOf("") }
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStudentMarks()
    }
    
    LaunchedEffect(marksheet) {
        marksheet?.let {
            selectedUrl = it.downloadUrl
            currentView = MarksView.Marksheet
            viewModel.clearMarksheetState()
        }
    }

    Scaffold(
        topBar = {
            if (currentView != MarksView.Marksheet) {
                TopAppBar(
                    title = {
                        Text(
                            when (currentView) {
                                MarksView.Summary -> "Marks & Results"
                                MarksView.ExamDetail -> examDetail?.examName ?: "Exam Results"
                                MarksView.SubjectMarks -> subjectMarks?.subjectName ?: "Subject History"
                                MarksView.Marksheet -> "" // Handled by its own Scaffold
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentView == MarksView.Summary) onBack()
                            else currentView = MarksView.Summary
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (currentView == MarksView.Summary) {
                            IconButton(onClick = { viewModel.downloadMarksheet() }) {
                                Icon(Icons.Rounded.Download, contentDescription = "Download Marksheet")
                            }
                        } else if (currentView == MarksView.ExamDetail) {
                            IconButton(onClick = {
                                selectedUrl = "https://mmp.sital.info.np/student/marks/$selectedId"
                                currentView = MarksView.Marksheet
                            }) {
                                Icon(Icons.Rounded.Description, contentDescription = "View Official Marksheet")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .then(if (currentView != MarksView.Marksheet) Modifier.padding(padding) else Modifier)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            
            when {
                isLoading && (summary == null && examDetail == null && subjectMarks == null) -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    when (currentView) {
                        MarksView.Summary -> MarksSummaryView(
                            summary ?: MarksSummaryDto(), 
                            onRetry = { viewModel.loadStudentMarks() }
                        ) { examId ->
                            selectedId = examId
                            viewModel.loadMarksByExam(examId)
                            currentView = MarksView.ExamDetail
                        }
                        MarksView.ExamDetail -> ExamDetailView(examDetail) { _ ->
                            // viewModel.loadMarksBySubject(subjectId)
                            // currentView = MarksView.SubjectMarks
                        }
                        MarksView.SubjectMarks -> SubjectMarksView(subjectMarks)
                        MarksView.Marksheet -> MarksheetWebViewScreen(selectedUrl ?: "") {
                            currentView = if (selectedId.isNotEmpty()) MarksView.ExamDetail else MarksView.Summary
                        }
                    }
                }
            }
        }
    }
}

sealed class MarksView {
    object Summary : MarksView()
    object ExamDetail : MarksView()
    object SubjectMarks : MarksView()
    object Marksheet : MarksView()
}

@Composable
fun MarksSummaryView(
    summary: MarksSummaryDto,
    onRetry: () -> Unit = {},
    onExamClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ResultOverviewCard(summary.averageMarks, summary.totalExams)
        }
        item {
            Text("Recent Exams", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        
        if (summary.exams.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No exam records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(summary.exams) { exam ->
                ExamResultCard(exam) { onExamClick(exam.examId) }
            }
        }
    }
}

@Composable
fun ExamDetailView(
    detail: ExamDetailDto?,
    onSubjectClick: (Int) -> Unit
) {
    if (detail == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(detail.marks) { mark ->
            SubjectMarkRow(mark) { onSubjectClick(1) } // Mock ID
        }
    }
}

@Composable
fun SubjectMarksView(subjectMark: SubjectMarkDto?) {
    if (subjectMark == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subjectMark.marks) { mark ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = mark.examName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        mark.date?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    GradeBadge(mark.percentage)
                }
            }
        }
    }
}

@Composable
fun ResultOverviewCard(average: Float, totalExams: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1A56BE), Color(0xFF3F78E0))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Overall Average", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = "${average.toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = totalExams.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        Text(text = "Exams", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ExamResultCard(exam: ExamSummaryDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exam.examName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "${exam.obtainedMarks.toInt()}/${exam.totalMarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            GradeBadge(exam.percentage)
        }
    }
}

@Composable
fun SubjectMarkRow(mark: MarkDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (mark.code != null) "${mark.subject} (${mark.code})" else mark.subject, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Score: ${mark.score.toInt()}/${mark.total.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            GradeBadge(mark.percentage)
        }
    }
}

@Composable
fun GradeBadge(percentage: Float) {
    val (color, grade) = when {
        percentage >= 90 -> Color(0xFF4CAF50) to "A+"
        percentage >= 80 -> Color(0xFF4CAF50) to "A"
        percentage >= 70 -> Color(0xFF8BC34A) to "B+"
        percentage >= 60 -> Color(0xFFCDDC39) to "B"
        percentage >= 50 -> Color(0xFFFFC107) to "C+"
        percentage >= 40 -> Color(0xFFFF9800) to "C"
        else -> Color(0xFFF44336) to "F"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = grade,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MarksEmptyState(message: String, onRetry: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Rounded.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Refresh")
            }
        }
    }
}
