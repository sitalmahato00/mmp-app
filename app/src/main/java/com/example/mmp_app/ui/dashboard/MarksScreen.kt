package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.data.remote.model.MarkDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksScreen(
    onBack: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val marks by viewModel.marks.collectAsState()
    val studentDashboard by viewModel.studentDashboard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStudentMarks()
        viewModel.loadStudentDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = "2nd Semester",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        if (isLoading && marks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FF))
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ResultsSummaryCard(studentDashboard?.kpiCards?.averageMarks ?: 0f)
                }

                item {
                    Text(
                        text = "Subject Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(marks) { mark ->
                    MarkItem(mark)
                }
            }
        }
    }
}

@Composable
fun ResultsSummaryCard(averageMarks: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SGPA", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = String.format("%.2f", averageMarks / 10), // Assuming averageMarks is out of 100
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00C853)
                )
            }
            
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFF0F0F0)))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Grade", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = when {
                        averageMarks >= 90 -> "A+"
                        averageMarks >= 80 -> "A"
                        averageMarks >= 70 -> "B+"
                        averageMarks >= 60 -> "B"
                        else -> "C"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00C853)
                )
            }
        }
    }
}

@Composable
fun MarkItem(mark: MarkDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF7C4DFF).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = Color(0xFF7C4DFF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = mark.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f", mark.percentage / 10), // Assuming GPA format in UI
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = when {
                        mark.percentage >= 90 -> "A+"
                        mark.percentage >= 80 -> "A"
                        mark.percentage >= 70 -> "B+"
                        else -> "B"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00C853)
                )
            }
        }
    }
}
