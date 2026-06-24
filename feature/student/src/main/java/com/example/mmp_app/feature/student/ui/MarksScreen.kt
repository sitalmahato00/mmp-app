package com.example.mmp_app.feature.student.ui

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
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
    var selectedId by remember { mutableStateOf("") }
    
    var isPreviewing by remember { mutableStateOf(false) }
    var printUrl by remember { mutableStateOf<String?>(null) }
    var loadingMarksheetId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
            isPreviewing = false
            loadingMarksheetId = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStudentMarks()
    }
    
    val context = LocalContext.current
    
    if (isPreviewing && sessionCookie != null && printUrl != null) {
        MarksheetPreviewPage(
            url = printUrl!!,
            cookie = sessionCookie!!,
            onBack = { isPreviewing = false }
        )
        return
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
                            onDownloadMarksheet = {
                                loadingMarksheetId = "summary"
                                viewModel.downloadMarksheet()
                            },
                            onExamClick = { examId ->
                                selectedId = examId
                                viewModel.loadMarksByExam(examId)
                                currentView = MarksView.ExamDetail
                            },
                            onDownloadExamMarksheet = { examId ->
                                loadingMarksheetId = examId
                                viewModel.downloadMarksheet(examId)
                            },
                            loadingId = loadingMarksheetId
                        )
                        MarksView.ExamDetail -> ExamDetailView(
                            examDetail, 
                            onDownloadMarksheet = { 
                                loadingMarksheetId = examDetail?.examId
                                viewModel.downloadMarksheet(examDetail?.examId) 
                            },
                            loadingId = loadingMarksheetId
                        )
                        MarksView.SubjectMarks -> SubjectMarksView(subjectMarks)
                    }
                }
            }

            if (isPreviewing && sessionCookie == null) {
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
    
    // Logic moved out of Scaffold to be top-level conditional
    LaunchedEffect(marksheet) {
        marksheet?.let {
            loadingMarksheetId = null
            if (it.downloadUrl.endsWith(".pdf", ignoreCase = true)) {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(it.downloadUrl))
                context.startActivity(intent)
            } else {
                printUrl = it.downloadUrl
                isPreviewing = true
            }
            viewModel.clearMarksheetState()
        }
    }

    LaunchedEffect(isPreviewing, printUrl) {
        if (isPreviewing && printUrl != null && sessionCookie == null) {
            viewModel.performWebLogin()
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
    onDownloadMarksheet: () -> Unit,
    onExamClick: (String) -> Unit,
    onDownloadExamMarksheet: (String) -> Unit,
    loadingId: String? = null
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Exam Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onDownloadMarksheet, enabled = loadingId == null) {
                    if (loadingId == "summary") {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Marksheet")
                }
            }
        }
        
        items(summary.exams) { exam ->
            EnhancedExamCard(
                exam = exam,
                onCardClick = { onExamClick(exam.examId) },
                onDownloadClick = { onDownloadExamMarksheet(exam.examId) },
                isLoading = loadingId == exam.examId
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
    onDownloadClick: () -> Unit,
    isLoading: Boolean = false
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
                onClick = onDownloadClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                contentPadding = PaddingValues(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("View Official Marksheet", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
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
fun ExamDetailView(detail: ExamDetailDto?, onDownloadMarksheet: () -> Unit, loadingId: String? = null) {
    if (detail == null) return
    EnhancedExamCard(
        ExamSummaryDto(
            examId = detail.examId ?: "",
            examName = detail.examName,
            category = detail.category,
            startDate = detail.startDate,
            subjects = detail.marks
        ),
        onDownloadClick = onDownloadMarksheet,
        isLoading = loadingId != null && loadingId == detail.examId
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksheetPreviewPage(
    url: String,
    cookie: String,
    onBack: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isWebViewLoading by remember { mutableStateOf(true) }

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Marksheet Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            webViewRef?.let { webView ->
                                val context = webView.context
                                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                                val jobName = "Marksheet_${System.currentTimeMillis()}"
                                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                                val printAttributes = PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                    .build()
                                printManager.print(jobName, printAdapter, printAttributes)
                            }
                        },
                        enabled = !isWebViewLoading
                    ) {
                        Icon(Icons.Rounded.Print, contentDescription = "Print")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            defaultTextEncodingName = "utf-8"
                        }
                        
                        setInitialScale(1)
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isWebViewLoading = false
                                val script = """
                                    (function() {
                                        const noise = 'nav, .navbar, .sidebar, .btn, button, .no-print, header, footer, aside, [role="navigation"]';
                                        document.querySelectorAll(noise).forEach(el => el.remove());
                                        
                                        const containerSelectors = ['.card', '.marksheet', '.print-content', 'main', '#app > div:nth-child(2)'];
                                        let mainContent = null;
                                        for (const selector of containerSelectors) {
                                            const found = document.querySelector(selector);
                                            if (found && found.innerText.length > 100) {
                                                mainContent = found;
                                                break;
                                            }
                                        }

                                        if (mainContent) {
                                            const clone = mainContent.cloneNode(true);
                                            document.body.innerHTML = '';
                                            document.body.appendChild(clone);
                                            clone.style.width = '100%';
                                            clone.style.margin = '0';
                                            clone.style.padding = '0';
                                            clone.style.boxShadow = 'none';
                                            clone.style.border = 'none';
                                        }

                                        const style = document.createElement('style');
                                        style.innerHTML = `
                                            body { background: white !important; margin: 0 !important; padding: 10px !important; width: 100% !important; overflow-x: hidden !important; }
                                            * { max-width: 100% !important; box-sizing: border-box !important; }
                                            table { width: 100% !important; border-collapse: collapse !important; font-size: 10px !important; }
                                            th, td { padding: 4px !important; border: 1px solid #ddd !important; }
                                            .container, .row, .col-md-12 { padding: 0 !important; margin: 0 !important; width: 100% !important; max-width: 100% !important; }
                                            img { height: auto !important; max-width: 150px !important; }
                                            a[href*="back"], .breadcrumb { display: none !important; }
                                        `;
                                        document.head.appendChild(style);
                                        
                                        if (!document.querySelector('meta[name="viewport"]')) {
                                            const meta = document.createElement('meta');
                                            meta.name = "viewport";
                                            meta.content = "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no";
                                            document.head.appendChild(meta);
                                        }
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(script, null)
                            }
                        }
                        
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookie.split("||").forEach {
                            val singleCookie = it.trim()
                            if (singleCookie.isNotEmpty()) {
                                cookieManager.setCookie(url, singleCookie)
                            }
                        }
                        cookieManager.flush()
                        loadUrl(url)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isWebViewLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
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
