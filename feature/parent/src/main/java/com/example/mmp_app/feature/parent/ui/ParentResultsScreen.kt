package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentResultsScreen(
    childId: Int = 0,
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    showSystemHeader: Boolean = true,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val primaryColor = Color(0xFF6366F1)

    val content = @Composable { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ResultSummaryCard(isDarkTheme)
            }
            
            item {
                Text(
                    "Marksheet - First Terminal Exam",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            item {
                MarksheetHeader(isDarkTheme)
            }

            items(listOf(
                MarkRowData("Mathematics", "100", "40", "85", "A"),
                MarkRowData("Physics", "100", "40", "78", "B+"),
                MarkRowData("Chemistry", "100", "40", "92", "A+"),
                MarkRowData("English", "100", "40", "88", "A"),
                MarkRowData("Computer Science", "100", "40", "95", "A+")
            )) { data ->
                MarkRow(data, isDarkTheme)
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL MARKS", fontWeight = FontWeight.ExtraBold, color = primaryColor)
                        Text("438 / 500", fontWeight = FontWeight.ExtraBold, color = primaryColor, fontSize = 18.sp)
                    }
                }
            }

            item {
                Button(
                    onClick = { /* TODO: Download logic */ },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Marksheet (PDF)")
                }
            }
        }
    }

    if (showSystemHeader) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Exam Results", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                        titleContentColor = textColor
                    )
                )
            },
            containerColor = backgroundColor
        ) { padding ->
            content(padding)
        }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
fun ResultSummaryCard(isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Semester Grade Point Average (SGPA)", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            Text("3.76", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                Text("Rank: 4th in Class", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun MarksheetHeader(isDarkTheme: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Subject", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Full", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 12.sp)
        Text("Pass", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 12.sp)
        Text("Marks", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 12.sp)
        Text("Grade", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 12.sp)
    }
}

data class MarkRowData(val subject: String, val full: String, val pass: String, val obtained: String, val grade: String)

@Composable
fun MarkRow(data: MarkRowData, isDarkTheme: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(data.subject, modifier = Modifier.weight(2f), fontWeight = FontWeight.Medium)
            Text(data.full, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text(data.pass, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text(data.obtained, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
            Text(data.grade, modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.LightGray.copy(alpha = 0.3f))
    }
}
