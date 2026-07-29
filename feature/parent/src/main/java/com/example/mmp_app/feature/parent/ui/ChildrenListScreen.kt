package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.ChildDetailDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildrenListScreen(
    onBack: () -> Unit,
    onNavigateToChildDetail: (Int) -> Unit,
    viewModel: ChildrenListViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false,
    showSystemHeader: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    val content = @Composable { padding: PaddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadChildren() },
            modifier = Modifier.padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.children.isEmpty() && !uiState.isLoading && uiState.error == null) {
                    EmptyChildrenState()
                } else if (uiState.error != null && uiState.children.isEmpty()) {
                    ChildListErrorState(uiState.error!!, onRetry = { viewModel.loadChildren() })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.isLoading && uiState.children.isEmpty()) {
                            items(5) {
                                ShimmerChildCard()
                            }
                        } else {
                            items(uiState.children) { child ->
                                ChildCard(
                                    child = child,
                                    onClick = { onNavigateToChildDetail(child.id) },
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSystemHeader) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("My Children", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            content(padding)
        }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
fun ChildCard(
    child: ChildDetailDto,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val cardColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (child.avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = child.avatarUrl,
                        contentDescription = child.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = child.name.firstOrNull()?.toString() ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    ChildStatusBadge(status = child.status)
                }
                Text(
                    text = "Student No: ${child.studentNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Text(
                    text = "Program: ${child.program}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Text(
                    text = "Semester ${child.semester} • Section ${child.section}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
fun ChildStatusBadge(status: String) {
    val backgroundColor = if (status.lowercase() == "active") Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
    val textColor = if (status.lowercase() == "active") Color(0xFF166534) else Color(0xFF64748B)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ShimmerChildCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp))
    }
}

@Composable
fun EmptyChildrenState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No children linked to your account", color = Color.Gray)
    }
}

@Composable
fun ChildListErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
