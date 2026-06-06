package com.example.mmp_app.ui.dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.data.remote.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksScreen(
    onBack: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val summary by viewModel.marksSummary.collectAsState()
    val examDetail by viewModel.examDetail.collectAsState()
    val subjectMarks by viewModel.subjectMarks.collectAsState()
    val marksheet by viewModel.marksheet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var currentView by remember { mutableStateOf<MarksView>(MarksView.Summary) }
    var selectedId by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStudentMarks()
    }
    
    LaunchedEffect(marksheet) {
        marksheet?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.downloadUrl))
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentView) {
                            MarksView.Summary -> "Marks & Results"
                            MarksView.ExamDetail -> examDetail?.examName ?: "Exam Results"
                            MarksView.SubjectMarks -> subjectMarks?.subjectName ?: "Subject History"
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
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))) {
            
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
}

@Composable
fun MarksSummaryView(
    summary: MarksSummaryDto,
    onRetry: () -> Unit = {},
    onExamClick: (Int) -> Unit
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
            Text("Recent Exams", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        
        if (summary.exams.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No exam records found.", color = Color.Gray)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = mark.examName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        mark.date?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Overall Average", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${average.toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = totalExams.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = "Exams", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                Text(text = exam.examName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "${exam.obtainedMarks.toInt()}/${exam.totalMarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mark.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Score: ${mark.score.toInt()}/${mark.total.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
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
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Refresh")
            }
        }
    }
}
