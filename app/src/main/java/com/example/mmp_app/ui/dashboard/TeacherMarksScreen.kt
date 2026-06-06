package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.data.remote.model.MarkRecordRequest
import com.example.mmp_app.data.remote.model.StudentMarkItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMarksScreen(
    classId: Int,
    subject: String,
    onBack: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val students by viewModel.classStudents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val markStates = remember { mutableStateMapOf<Int, String>() }
    var totalMarks by remember { mutableStateOf("100") }

    LaunchedEffect(classId) {
        viewModel.loadClassStudents(classId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Record Marks", style = MaterialTheme.typography.titleMedium)
                        Text(subject, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val request = MarkRecordRequest(
                                classId = classId,
                                subject = subject,
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                marks = markStates.mapNotNull { (id, score) ->
                                    score.toFloatOrNull()?.let {
                                        StudentMarkItem(id, it, totalMarks.toFloatOrNull() ?: 100f)
                                    }
                                }
                            )
                            viewModel.recordMarks(request, onSuccess = onBack)
                        },
                        enabled = !isLoading && students.isNotEmpty() && markStates.isNotEmpty()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && students.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = { if (it.all { c -> c.isDigit() }) totalMarks = it },
                    label = { Text("Total Marks") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students) { student ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = student.name, fontWeight = FontWeight.Bold)
                                Text(text = student.email, style = MaterialTheme.typography.bodySmall)
                            }
                            
                            OutlinedTextField(
                                value = markStates[student.id] ?: "",
                                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) markStates[student.id] = it },
                                modifier = Modifier.width(80.dp),
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}
