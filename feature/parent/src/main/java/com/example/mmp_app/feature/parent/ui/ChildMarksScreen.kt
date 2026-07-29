package com.example.mmp_app.feature.parent.ui

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.feature.student.ui.OfficialMarksheetDialog
import com.example.mmp_app.feature.student.ui.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildMarksScreen(
    childId: Int,
    onBack: () -> Unit,
    viewModel: ParentMarksViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val primaryColor = Color(0xFF6366F1)

    var showExamPicker by remember { mutableStateOf(false) }
    var showMarksheet by remember { mutableStateOf(false) }
    var selectedExamForMarksheet by remember { mutableStateOf<ExamSummaryDto?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBgColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.children.isEmpty()) {
                MarksShimmer()
            } else if (uiState.error != null && uiState.marks.isEmpty()) {
                ErrorState(message = uiState.error!!, onRetry = { viewModel.refresh() })
            } else {
                val assessmentMarks = uiState.marks.filter { it.examType.lowercase() == "assessment" }
                val groupedMarks = assessmentMarks.groupBy { it.exam }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Child Selector
                    if (uiState.children.size > 1) {
                        item {
                            ChildSelector(
                                children = uiState.children,
                                selectedChildId = uiState.selectedChildId,
                                onChildSelected = { viewModel.onChildSelected(it) },
                                cardBgColor = cardBgColor,
                                textColor = textColor
                            )
                        }
                    }

                    // 2. Summary Card
                    item {
                        MarksSummaryCard(
                            summary = uiState.summary ?: ParentMarksSummaryDto(0, 0.0, 0, 0),
                            cardBgColor = cardBgColor,
                            textColor = textColor,
                            primaryColor = primaryColor
                        )
                    }

                    // 3. Section Header
                    item {
                        Text(
                            "Assessment Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    // 4. Grouped Marks
                    if (groupedMarks.isEmpty()) {
                        item {
                            EmptyMarksState("No assessment marks published yet")
                        }
                    } else {
                        groupedMarks.forEach { (examName, marks) ->
                            item {
                                ExamGroupHeader(examName, textColor)
                            }
                            items(marks) { mark ->
                                SubjectMarkRow(mark, textColor)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = textColor.copy(alpha = 0.05f))
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }

                // 5. View Marksheet Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Button(
                        onClick = { showExamPicker = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("View Marksheet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showExamPicker) {
        val exams = uiState.marks.filter { it.examType.lowercase() == "assessment" }.map { it.exam }.distinct()
        ExamPickerDialog(
            exams = exams,
            onExamSelected = { examName ->
                val examMarks = uiState.marks.filter { it.exam == examName && it.examType.lowercase() == "assessment" }
                val student = mapToStudentDashboard(uiState.selectedChild)
                val examSummary = mapToExamSummary(examName, examMarks)
                selectedExamForMarksheet = examSummary
                showExamPicker = false
                showMarksheet = true
            },
            onDismiss = { showExamPicker = false }
        )
    }

    if (showMarksheet && selectedExamForMarksheet != null) {
        OfficialMarksheetDialog(
            student = mapToStudentDashboard(uiState.selectedChild),
            exam = selectedExamForMarksheet!!,
            onDismiss = { showMarksheet = false }
        )
    }
}

@Composable
fun ChildSelector(
    children: List<ChildDetailDto>,
    selectedChildId: Int?,
    onChildSelected: (Int) -> Unit,
    cardBgColor: Color,
    textColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedChild = children.find { it.id == selectedChildId } ?: children.firstOrNull()

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape) {
                    AsyncImage(
                        model = selectedChild?.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    selectedChild?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = textColor.copy(alpha = 0.6f))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).background(cardBgColor)
        ) {
            children.forEach { child ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(32.dp), shape = CircleShape) {
                                AsyncImage(model = child.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(child.name, fontWeight = FontWeight.Medium)
                        }
                    },
                    onClick = {
                        onChildSelected(child.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MarksSummaryCard(
    summary: ParentMarksSummaryDto,
    cardBgColor: Color,
    textColor: Color,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    label = "Total Assessments",
                    value = summary.totalExams.toString(),
                    icon = Icons.Rounded.EditNote,
                    color = primaryColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                SummaryItem(
                    label = "Average Marks",
                    value = String.format("%.1f", summary.averageMarks),
                    icon = Icons.Rounded.BarChart,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    label = "Passed",
                    value = summary.passedCount.toString(),
                    icon = Icons.Rounded.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                SummaryItem(
                    label = "Failed",
                    value = summary.failedCount.toString(),
                    icon = Icons.Rounded.Cancel,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
            Icon(icon, null, modifier = Modifier.padding(8.dp), tint = color)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ExamGroupHeader(name: String, textColor: Color) {
    Text(
        text = name,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        color = textColor,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SubjectMarkRow(mark: ParentMarkRecordDto, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(mark.subject, fontWeight = FontWeight.Bold, color = textColor)
            Text(mark.subjectCode, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("${mark.obtainedMarks} / ${mark.fullMarks}", fontWeight = FontWeight.Bold, color = textColor)
            Text("Pass: ${mark.passMarks}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        val (chipColor, chipText) = when {
            mark.isAbsent -> Color.Gray to "Absent"
            mark.isPass -> Color(0xFF10B981) to "Pass"
            else -> Color(0xFFEF4444) to "Fail"
        }
        Surface(
            color = chipColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = chipText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = chipColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExamPickerDialog(
    exams: List<String>,
    onExamSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Exam") },
        text = {
            LazyColumn {
                items(exams) { exam ->
                    ListItem(
                        headlineContent = { Text(exam) },
                        modifier = Modifier.clickable { onExamSelected(exam) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MarksShimmer() {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp)))
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.width(150.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp)))
    }
}

@Composable
fun EmptyMarksState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Search, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color.Gray)
        }
    }
}

private fun mapToStudentDashboard(child: ChildDetailDto?): StudentDashboardDto? {
    if (child == null) return null
    return StudentDashboardDto(
        studentName = child.name,
        studentId = child.id,
        avatarUrl = child.avatarUrl,
        email = child.email,
        phone = child.phone,
        rollNumber = child.rollNumber ?: "N/A",
        program = child.program,
        semester = child.semester,
        section = child.section,
        department = child.department,
        kpiCards = StudentKpiDto(0f, 0f, 0, 0) // Mocked as not needed for marksheet
    )
}

private fun mapToExamSummary(examName: String, marks: List<ParentMarkRecordDto>): ExamSummaryDto {
    val subjects = marks.map { mark ->
        MarkDto(
            subject = mark.subject,
            code = mark.subjectCode,
            score = mark.obtainedMarks.toFloat(),
            total = mark.fullMarks.toFloat(),
            passMarks = mark.passMarks.toFloat(),
            isPassed = mark.isPass,
            isAbsent = mark.isAbsent
        )
    }
    val totalObtained = marks.sumOf { it.obtainedMarks }.toFloat()
    val totalFull = marks.sumOf { it.fullMarks }.toFloat()
    val percentage = if (totalFull > 0) (totalObtained / totalFull * 100) else 0f
    
    return ExamSummaryDto(
        examId = 0,
        examName = examName,
        category = "assessment",
        subjects = subjects,
        totalMarks = totalFull.toInt(),
        obtainedMarks = totalObtained,
        percentage = percentage
    )
}
