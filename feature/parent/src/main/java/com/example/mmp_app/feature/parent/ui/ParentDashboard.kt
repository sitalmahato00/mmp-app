package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.ChildSummaryDto
import com.example.mmp_app.domain.model.ParentDashboardDto
import com.example.mmp_app.domain.model.ParentNoticeDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: ParentDashboardViewModel = hiltViewModel(),
    onNavigateToChildDetail: (Int) -> Unit = {},
    onNavigateToAttendance: (Int) -> Unit = {},
    onNavigateToMarks: (Int) -> Unit = {},
    onNavigateToAssignments: (Int) -> Unit = {},
    onNavigateToTimetable: (Int) -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading && uiState.dashboard == null) {
        DashboardShimmer()
    } else if (uiState.error != null && uiState.dashboard == null) {
        ErrorState(message = uiState.error!!, onRetry = { viewModel.load() })
    } else {
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            ParentDashboard(
                data = uiState.dashboard ?: return@PullToRefreshBox,
                recentNotices = uiState.recentNotices,
                onChildClick = { id, _ -> onNavigateToChildDetail(id) },
                onAttendanceClick = { id, _ -> onNavigateToAttendance(id) },
                onMarksClick = { id, _ -> onNavigateToMarks(id) },
                onAssignmentsClick = { id, _ -> onNavigateToAssignments(id) },
                onTimetableClick = { _, _ -> onNavigateToTimetable(0) },
                onNoticesClick = onNavigateToNotices,
                onProfileClick = onNavigateToProfile,
                onLogoutClick = onLogout
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboard(
    data: ParentDashboardDto,
    recentNotices: List<ParentNoticeDto> = emptyList(),
    onChildClick: (Int, String) -> Unit = { _, _ -> },
    onAttendanceClick: (Int, String) -> Unit = { _, _ -> },
    onMarksClick: (Int, String) -> Unit = { _, _ -> },
    onAssignmentsClick: (Int, String) -> Unit = { _, _ -> },
    onTimetableClick: (Int, String) -> Unit = { _, _ -> },
    onNoticesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    var showChildPicker by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(Int, String) -> Unit>({ _, _ -> }) }
    val sheetState = rememberModalBottomSheetState()

    val handleAction = { action: (Int, String) -> Unit ->
        if (data.children.size == 1) {
            val child = data.children.first()
            action(child.id, child.name)
        } else {
            pendingAction = action
            showChildPicker = true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Section 1: Header
        item {
            ParentHeader(
                name = data.parentName,
                childrenCount = data.childrenCount
            )
        }

        // Section 2: My Children
        item {
            SectionHeader(
                title = "My Children (${data.childrenCount})",
                modifier = Modifier.padding(16.dp)
            )
        }

        items(data.children) { child ->
            ChildCard(
                child = child,
                onClick = { onChildClick(child.id, child.name) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Section 3: Quick Actions
        item {
            QuickActionsGrid(
                onAttendanceClick = { handleAction(onAttendanceClick) },
                onMarksClick = { handleAction(onMarksClick) },
                onAssignmentsClick = { handleAction(onAssignmentsClick) },
                onTimetableClick = { handleAction(onTimetableClick) },
                modifier = Modifier.padding(16.dp)
            )
        }

        // Section 4: Recent Notices
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Notices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNoticesClick) {
                    Text("See All")
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        items(recentNotices) { notice ->
            NoticeRow(
                notice = notice,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        
        if (recentNotices.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No notices available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showChildPicker) {
        ModalBottomSheet(
            onDismissRequest = { showChildPicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Select Child",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                data.children.forEach { child ->
                    ListItem(
                        headlineContent = { Text(child.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(child.studentId) },
                        leadingContent = {
                            Surface(modifier = Modifier.size(40.dp), shape = CircleShape) {
                                if (child.avatarUrl.isNotEmpty()) {
                                    AsyncImage(model = child.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Rounded.Person, null, modifier = Modifier.padding(8.dp))
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            showChildPicker = false
                            pendingAction(child.id, child.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ParentHeader(name: String, childrenCount: Int) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = greeting,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Welcome, $name",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.People,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Parent  ·  $childrenCount Child${if (childrenCount > 1) "ren" else ""} enrolled",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ChildCard(child: ChildSummaryDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (child.avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = child.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${child.studentId} · Semester ${child.semester} · Section ${child.section}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = child.program,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance: ${child.attendancePercent.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AttendanceStatusBadge(child.attendanceStatus)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (child.attendancePercent / 100).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = getStatusColor(child.attendanceStatus),
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onAttendanceClick: () -> Unit,
    onMarksClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onTimetableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(title = "Quick Actions")
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionCard(
                title = "Attendance",
                subtitle = "View child's attendance",
                icon = Icons.Rounded.BarChart,
                color = Color(0xFF3B82F6),
                onClick = onAttendanceClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionCard(
                title = "Marks",
                subtitle = "View results & grades",
                icon = Icons.AutoMirrored.Rounded.Assignment,
                color = Color(0xFF10B981),
                onClick = onMarksClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionCard(
                title = "Assignments",
                subtitle = "Track pending work",
                icon = Icons.Rounded.Task,
                color = Color(0xFFF59E0B),
                onClick = onAssignmentsClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionCard(
                title = "Timetable",
                subtitle = "Class schedule & rooms",
                icon = Icons.Rounded.CalendarToday,
                color = Color(0xFF8B5CF6),
                onClick = onTimetableClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = color
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NoticeRow(notice: ParentNoticeDto, modifier: Modifier = Modifier) {
    val (icon, color) = when (notice.type.lowercase()) {
        "exam" -> Icons.Rounded.Quiz to Color(0xFFEF4444)
        "event" -> Icons.Rounded.Event to Color(0xFFF59E0B)
        "department" -> Icons.Rounded.Business to Color(0xFF3B82F6)
        else -> Icons.Rounded.Info to Color(0xFF6B7280)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = color
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notice.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                    Text(
                        text = " · ${getRelativeTime(notice.publishedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceStatusBadge(status: String) {
    val (color, text) = when (status.lowercase()) {
        "good" -> Color(0xFF10B981) to "Good"
        "medium" -> Color(0xFFF59E0B) to "Average"
        "low" -> Color(0xFFEF4444) to "Low"
        else -> Color.Gray to status.uppercase()
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "good" -> Color(0xFF10B981)
        "medium" -> Color(0xFFF59E0B)
        "low" -> Color(0xFFEF4444)
        else -> Color.Gray
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun DashboardShimmer() {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp)))
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.width(150.dp).height(24.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp)))
    }
}

fun getRelativeTime(isoString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString.substring(0, 19)) ?: return isoString
        val now = System.currentTimeMillis()
        val diff = now - date.time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            days > 0 -> "$days day${if (days > 1L) "s" else ""} ago"
            hours > 0 -> "$hours hour${if (hours > 1L) "s" else ""} ago"
            minutes > 0 -> "$minutes minute${if (minutes > 1L) "s" else ""} ago"
            else -> "Just now"
        }
    } catch (e: Exception) {
        isoString.split("T").firstOrNull() ?: isoString
    }
}
