package com.example.mmp_app.feature.student.ui

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.*
import kotlin.math.roundToInt

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
    val sessionCookie by viewModel.webSessionCookie.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentView by remember { mutableStateOf<MarksView>(MarksView.Summary) }
    
    var showOfficialMarksheet by remember { mutableStateOf(false) }
    var selectedExamForMarksheet by remember { mutableStateOf<ExamSummaryDto?>(null) }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStudentMarks()
        if (viewModel.studentDashboard.value == null) {
            viewModel.loadStudentDashboard()
        }
    }
    
    val context = LocalContext.current
    
    if (showOfficialMarksheet && selectedExamForMarksheet != null) {
        OfficialMarksheetDialog(
            student = viewModel.studentDashboard.collectAsState().value,
            exam = selectedExamForMarksheet!!,
            onDismiss = { showOfficialMarksheet = false }
        )
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
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
            
            when {
                isLoading && (summary == null && examDetail == null && subjectMarks == null) -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                summary?.exams?.isEmpty() == true && !isLoading -> {
                    MarkEmptyState("No exam results published yet") { viewModel.loadStudentMarks() }
                }
                else -> {
                    when (currentView) {
                        MarksView.Summary -> MarksSummaryView(
                            summary ?: MarksSummaryDto(), 
                            onExamClick = { examId ->
                                viewModel.loadMarksByExam(examId)
                                currentView = MarksView.ExamDetail
                            },
                            onViewOfficialMarksheet = { exam ->
                                selectedExamForMarksheet = exam
                                showOfficialMarksheet = true
                            }
                        )
                        MarksView.ExamDetail -> ExamDetailView(
                            examDetail,
                            onViewOfficialMarksheet = { exam ->
                                selectedExamForMarksheet = exam
                                showOfficialMarksheet = true
                            }
                        )
                        MarksView.SubjectMarks -> SubjectMarksView(subjectMarks)
                    }
                }
            }

            if (showOfficialMarksheet) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF4F46E5))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Generating Marksheet...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Please wait a moment", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    
    LaunchedEffect(marksheet) {
        if (marksheet != null) {
            // No longer using marksheet object for preview
            viewModel.clearMarksheetState()
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
    onExamClick: (String) -> Unit,
    onViewOfficialMarksheet: (ExamSummaryDto) -> Unit
) {
    // Calculate Overall Average if not provided accurately by API
    // Formula: sum(obtained) / sum(full) * 100
    val totalObtained = summary.exams.sumOf { it.obtainedMarks.toDouble() }
    val totalFull = summary.exams.sumOf { it.totalMarks.toDouble() }
    val calculatedAvg = if (totalFull > 0) (totalObtained / totalFull * 100).toFloat() else summary.averageMarks
    val allPassed = summary.exams.all { exam -> exam.subjects.all { it.isPassed } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ResultOverviewCard(calculatedAvg, summary.totalExams, allPassed)
        }
        
        item {
            Text("Exam Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
        
        items(summary.exams) { exam ->
            EnhancedExamCard(
                exam = exam,
                onCardClick = { onExamClick(exam.examId) },
                onViewOfficialMarksheet = { onViewOfficialMarksheet(exam) }
            )
        }
    }
}

@Composable
fun ResultOverviewCard(average: Float, totalExams: Int, allPassed: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4F46E5), Color(0xFF3730A3))
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Marks & Results", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Semester 1", 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White, 
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avgColor = when {
                        average >= 60 -> Color(0xFF4ADE80) // Green
                        average >= 40 -> Color(0xFFFB923C) // Orange
                        else -> Color(0xFFF87171) // Red
                    }
                    Text(
                        text = "${average.format(1)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = avgColor
                    )
                    Text("Overall Avg", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalExams.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text("Exams", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (allPassed) "All Pass" else "Failed",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (allPassed) Color(0xFF4ADE80) else Color(0xFFF87171),
                        fontSize = 24.sp
                    )
                    Text("Status", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun EnhancedExamCard(
    exam: ExamSummaryDto,
    onCardClick: () -> Unit = {},
    onViewOfficialMarksheet: () -> Unit
) {
    val totalObtained = exam.subjects.sumOf { it.score.toDouble() }
    val totalFull = exam.subjects.sumOf { it.total.toDouble() }.let { if (it == 0.0) exam.subjects.size * 25.0 else it }
    val percentage = if (totalFull > 0) (totalObtained / totalFull * 100) else 0.0
    val allPassed = exam.subjects.all { it.isPassed }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = exam.examName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                Surface(
                    color = if (allPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (allPassed) "PASSED" else "FAILED",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (allPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            if (allPassed) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                            contentDescription = null,
                            tint = if (allPassed) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Text(
                text = "${exam.category ?: "Assessment"} • ${exam.startDate ?: "2026-05-23"}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6B7280)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Obtained", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = totalObtained.format(2),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A)
                        )
                        Text(
                            text = " / ${totalFull.toInt()}",
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Percentage", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                    Text(
                        text = "${percentage.format(1)}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F2937)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "Subject breakdown", 
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall, 
                color = Color(0xFF9CA3AF)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            exam.subjects.forEach { subject ->
                SubjectBreakdownRow(subject)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onViewOfficialMarksheet,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text("View Official Marksheet", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SubjectBreakdownRow(mark: MarkDto) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mark.code ?: "---",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = mark.subject,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mark.isAbsent) {
                Text(
                    "Absent",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            } else {
                Text(
                    text = "${mark.score.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (mark.isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Text(
                    text = "/${mark.total.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

@Composable
fun ExamDetailView(detail: ExamDetailDto?, onViewOfficialMarksheet: (ExamSummaryDto) -> Unit) {
    if (detail == null) return
    val examSummary = ExamSummaryDto(
        examId = detail.examId ?: "",
        examName = detail.examName,
        category = detail.category,
        startDate = detail.startDate,
        subjects = detail.marks
    )
    EnhancedExamCard(
        examSummary,
        onViewOfficialMarksheet = { onViewOfficialMarksheet(examSummary) }
    )
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
                        Text(text = mark.examName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        mark.date?.let { Text(text = it, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280)) }
                    }
                    Text(
                        "${mark.obtainedMarks.toInt()}/${mark.totalMarks.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}

@Composable
fun OfficialMarksheetDialog(
    student: StudentDashboardDto?,
    exam: ExamSummaryDto,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                    Text("Official Marksheet", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    
                    val context = LocalContext.current
                    IconButton(onClick = { 
                        MarksheetPrinter(context).print(student, exam)
                    }) {
                        Icon(Icons.Rounded.Print, contentDescription = "Print")
                    }
                }
                
                // Content
                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        OfficialMarksheetView(student, exam)
                    }
                }
            }
        }
    }
}

@Composable
fun OfficialMarksheetView(student: StudentDashboardDto?, exam: ExamSummaryDto) {
    val allPassed = exam.subjects.all { it.isPassed }
    val totalObtained = exam.subjects.sumOf { it.score.toDouble() }
    val totalFull = exam.subjects.sumOf { it.total.toDouble() }.let { if (it == 0.0) exam.subjects.size * 25.0 else it }
    val percentage = if (totalFull > 0) (totalObtained / totalFull * 100) else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Status Badge
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Surface(
                color = if (allPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (allPassed) "PASSED" else "FAILED",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (allPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
            }
        }

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Using the JPEG version found in drawables as ICO is not natively supported well in Android
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.mmp_app.core.R.drawable.mmplogo),
                contentDescription = "MMP Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Institution Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "MANMOHAN MEMORIAL POLYTECHNIC",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            )
            Text(
                student?.program ?: "Computer Engineering",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center
            )
            Text(
                "Diploma in ${student?.program ?: "Computer Engineering"}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), color = Color(0xFFE5E7EB))
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            exam.examName.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C3AED)
        )
        Text(
            "Monthly Assessment - ${exam.startDate ?: "N/A"}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6B7280)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Student Info Grid
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB)).padding(12.dp)) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text("STUDENT NAME", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Text(student?.studentName?.uppercase() ?: "N/A", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("ROLL NO.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Text(student?.rollNumber ?: "N/A", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("SEMESTER", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Text(student?.semester?.toString() ?: "N/A", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("SECTION", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                // Try to extract section from roll number or default to A
                val section = student?.rollNumber?.lastOrNull()?.toString()?.uppercase()?.takeIf { it in "A".."Z" } ?: "A"
                Text(section, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SUBJECT", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
            Text("FULL", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("PASS", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("OBTAINED", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("REMARKS", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        HorizontalDivider(color = Color(0xFFE5E7EB))
        
        // Table Rows
        exam.subjects.forEach { mark ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(mark.subject, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(mark.code ?: "", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                }
                Text(mark.total.toInt().toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text(mark.passMarks.toInt().toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Text(
                    mark.score.format(2), 
                    modifier = Modifier.weight(0.8f), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall, 
                    fontWeight = FontWeight.Bold,
                    color = if (mark.isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                    Surface(
                        color = if (mark.isPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (mark.isPassed) "Pass" else "Fail",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (mark.isPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFF3F4F6))
        }
        
        // Total Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOTAL", modifier = Modifier.weight(2f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(totalFull.toInt().toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text("-", modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            Text(totalObtained.format(2), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
            Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                Surface(
                    color = if (allPassed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (allPassed) "PASS" else "FAIL",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (allPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Summary Footer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL MARKS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(totalObtained.format(2), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(" / ${totalFull.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF))
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PERCENTAGE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Text("${percentage.format(1)}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C3AED))
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RESULT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Text(
                    if (allPassed) "PASSED" else "FAILED", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = if (allPassed) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Signatures
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Prepared By:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.width(140.dp), color = Color(0xFF111827), thickness = 1.dp)
                Text("Examination Department", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Head of Department", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.width(140.dp), color = Color(0xFF111827), thickness = 1.dp)
                Text(student?.program ?: "Computer Engineering", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Generated on ${java.text.SimpleDateFormat("yyyy-MM-dd 'at' hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9CA3AF)
        )
    }
}

fun triggerPrint(webView: WebView) {
    val context = webView.context
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "Marksheet_${System.currentTimeMillis()}"
    val printAdapter = webView.createPrintDocumentAdapter(jobName)
    val printAttributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .build()
    printManager.print(jobName, printAdapter, printAttributes)
}

@Composable
fun MarkEmptyState(message: String, onRetry: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Rounded.Inbox,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFD1D5DB)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Please check back later", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Refresh Page")
            }
        }
    }
}

// Extension to format floats
fun Float.format(digits: Int) = "%.${digits}f".format(this)
fun Double.format(digits: Int) = "%.${digits}f".format(this)
