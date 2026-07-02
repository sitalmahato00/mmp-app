package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentResultsScreen(
    onMenuClick: () -> Unit = {},
    showSystemHeader: Boolean = true,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val primaryColor = Color(0xFF6366F1)

    val content = @Composable { padding: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.Assessment,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = primaryColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Exam Results",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    "Select a child to view their detailed marksheet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showSystemHeader) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Results", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    }
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
