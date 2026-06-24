package com.example.mmp_app.feature.student.ui

import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.NoticeAttachment
import com.example.mmp_app.domain.model.NoticeDetailDto
import com.example.mmp_app.domain.model.NoticeDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    onBack: () -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val notices by viewModel.notices.collectAsState()
    val noticeDetail by viewModel.noticeDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedNoticeId by remember { mutableStateOf<Int?>(null) }
    var selectedType by remember { mutableStateOf("all") }

    LaunchedEffect(selectedType) {
        viewModel.loadStudentNotices(type = selectedType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedNoticeId == null) "Notices" else "Notice Detail") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedNoticeId != null) {
                            selectedNoticeId = null
                            viewModel.clearNoticeDetail()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(targetState = selectedNoticeId, label = "notice_transition") { id ->
                if (id == null) {
                    NoticesList(
                        notices = notices,
                        isLoading = isLoading,
                        selectedType = selectedType,
                        onTypeSelect = { selectedType = it },
                        onNoticeClick = { 
                            selectedNoticeId = it.id
                            viewModel.loadNoticeDetail(it.id)
                        }
                    )
                } else {
                    NoticeDetailView(
                        detail = noticeDetail,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun NoticesList(
    notices: List<NoticeDto>,
    isLoading: Boolean,
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    onNoticeClick: (NoticeDto) -> Unit
) {
    val filterTypes = listOf(
        "all" to "All",
        "general" to "General",
        "exam" to "Exam",
        "event" to "Event",
        "department" to "Department",
        "academic" to "Academic"
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FF))) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTypes) { (type, label) ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelect(type) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        if (isLoading && notices.isEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(6) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        } else if (notices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.NotificationsOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No notices found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notices) { notice ->
                    NoticeItem(notice, onClick = { onNoticeClick(notice) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeItem(notice: NoticeDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                notice.type?.let { type ->
                    TypeBadge(type = type)
                }
                Text(
                    text = formatDate(notice.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (notice.attachmentCount > 0) {
                    Icon(
                        Icons.Rounded.AttachFile,
                        contentDescription = "Attachments",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notice.content ?: "No content available",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NoticeDetailView(
    detail: NoticeDetailDto?,
    isLoading: Boolean
) {
    if (isLoading && detail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (detail != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                detail.type?.let { type ->
                    TypeBadge(type = type)
                }
                Text(
                    text = formatDate(detail.publishedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = detail.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        autoLinkMask = Linkify.WEB_URLS
                        linksClickable = true
                        movementMethod = LinkMovementMethod.getInstance()
                        setTextColor(android.graphics.Color.DKGRAY)
                        textSize = 16f
                    }
                },
                update = { textView ->
                    textView.text = detail.content ?: "No content available"
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (detail.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "📎 Attachments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                detail.attachments.forEach { attachment ->
                    AttachmentItem(attachment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AttachmentItem(attachment: NoticeAttachment) {
    val context = LocalContext.current
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (attachment.isImage && !attachment.url.isNullOrEmpty()) {
                AsyncImage(
                    model = attachment.url,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).background(Color.LightGray, RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    imageVector = if (attachment.isPdf) Icons.Rounded.PictureAsPdf else Icons.Rounded.Description,
                    contentDescription = null,
                    tint = if (attachment.isPdf) Color.Red else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatFileSize(attachment.fileSize ?: 0),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            TextButton(onClick = {
                attachment.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }) {
                Text("Open")
            }
        }
    }
}

@Composable
fun TypeBadge(type: String) {
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

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0KB"
    val kb = size / 1024
    if (kb < 1024) return "${kb}KB"
    val mb = kb / 1024
    return "${mb}MB"
}
