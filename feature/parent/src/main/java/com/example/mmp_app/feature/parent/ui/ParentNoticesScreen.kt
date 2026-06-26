package com.example.mmp_app.feature.parent.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.ParentNoticeDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentNoticesScreen(
    onBack: () -> Unit = {},
    viewModel: ParentNoticesViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    
    val primaryColor = Color(0xFF2563EB)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8F9FF)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(if (state.selectedNotice == null) "College Notices" else "Notice Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.selectedNotice != null) {
                            viewModel.clearSelectedNotice()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBgColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(targetState = state.selectedNotice, label = "notice_transition") { selectedNotice ->
                if (selectedNotice == null) {
                    ParentNoticesList(
                        state = state,
                        viewModel = viewModel,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        cardBgColor = cardBgColor,
                        isDarkTheme = isDarkTheme
                    )
                } else {
                    ParentNoticeDetailView(
                        notice = selectedNotice,
                        textColor = textColor,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun ParentNoticesList(
    state: ParentNoticesState,
    viewModel: ParentNoticesViewModel,
    primaryColor: Color,
    textColor: Color,
    cardBgColor: Color,
    isDarkTheme: Boolean
) {
    val filterTypes = listOf(
        "All" to "All",
        "general" to "General",
        "exam" to "Exam",
        "event" to "Event",
        "department" to "Department"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTypes) { (type, label) ->
                FilterChip(
                    selected = state.currentFilter.lowercase() == type.lowercase(),
                    onClick = { viewModel.applyFilter(label) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (state.isLoading && state.notices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else if (state.filteredNotices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.NotificationsOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No notices found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredNotices) { notice ->
                    ParentNoticeCard(
                        notice = notice, 
                        textColor = textColor,
                        cardBgColor = cardBgColor,
                        onClick = { viewModel.loadNoticeDetail(notice.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ParentNoticeDetailView(
    notice: ParentNoticeDto,
    textColor: Color,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParentTypeBadge(type = notice.type)
            Text(
                text = notice.publishedAt.take(10),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = notice.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = notice.content ?: "No content available",
            style = MaterialTheme.typography.bodyLarge,
            color = textColor.copy(alpha = 0.8f),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun ParentNoticeCard(
    notice: ParentNoticeDto, 
    textColor: Color,
    cardBgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ParentTypeBadge(type = notice.type)
                Text(
                    text = notice.publishedAt.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            notice.content?.let { content ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ParentTypeBadge(type: String) {
    val (color, label) = when (type.lowercase()) {
        "general" -> Color.Gray to "General"
        "exam" -> Color(0xFFE53935) to "Exam"
        "event" -> Color(0xFF1E88E5) to "Event"
        "department" -> Color(0xFF8E24AA) to "Department"
        "academic" -> Color(0xFF43A047) to "Academic"
        else -> Color.Gray to type.replaceFirstChar { it.uppercase() }
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
