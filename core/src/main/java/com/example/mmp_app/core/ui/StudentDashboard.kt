package com.example.mmp_app.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.core.ui.theme.*
import com.example.mmp_app.core.R
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    data: StudentDashboardDto,
    attendanceSummary: AttendanceSummaryDto? = null,
    subjects: List<SubjectDto> = emptyList(),
    assignments: List<AssignmentDto> = emptyList(),
    recentNotices: List<NoticeDto> = emptyList(),
    todayClasses: List<ClassDto> = emptyList(),
    materialCount: Int = 12,

    onAttendanceClick: () -> Unit = {},
    onMarksClick: () -> Unit = {},
    onAssignmentsClick: () -> Unit = {},
    onNoticesClick: () -> Unit = {},
    onFeesClick: () -> Unit = {},
    onRoutineClick: () -> Unit = {},
    onExamsClick: () -> Unit = {},
    onResultsClick: () -> Unit = {},
    onSubjectsClick: () -> Unit = {},
    onTimetableClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onIdCardClick: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val primaryColor = Color(0xFF2563EB)
    val secondaryColor = Color(0xFF60A5FA)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val accentColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFDBEAFE)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = cardBgColor,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "☰ MENU",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                DrawerMenuItem("My Profile", Icons.Rounded.Person) { scope.launch { drawerState.close() }; onProfileClick() }
                DrawerMenuItem("Subjects", Icons.Rounded.Book) { scope.launch { drawerState.close() }; onSubjectsClick() }
                DrawerMenuItem("Attendance", Icons.Rounded.CalendarToday) { scope.launch { drawerState.close() }; onAttendanceClick() }
                DrawerMenuItem("Marks & Results", Icons.Rounded.Star) { scope.launch { drawerState.close() }; onResultsClick() }
                DrawerMenuItem("Assignments", Icons.AutoMirrored.Rounded.Assignment) { scope.launch { drawerState.close() }; onAssignmentsClick() }
                DrawerMenuItem("Timetable", Icons.Rounded.Schedule) { scope.launch { drawerState.close() }; onTimetableClick() }
                DrawerMenuItem("Notices", Icons.Rounded.Notifications) { scope.launch { drawerState.close() }; onNoticesClick() }
                DrawerMenuItem("Notifications", Icons.Rounded.NotificationsActive) { scope.launch { drawerState.close() }; onNotificationsClick() }
                DrawerMenuItem("Settings", Icons.Rounded.Settings) { scope.launch { drawerState.close() }; onSettingsClick() }
                Spacer(Modifier.weight(1f))
                DrawerMenuItem("Logout", Icons.Rounded.Logout) { scope.launch { drawerState.close() }; onLogoutClick() }
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.mmplogo),
                                    contentDescription = "College Logo",
                                    modifier = Modifier.padding(4.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "MMP College",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu", tint = primaryColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Theme Toggle",
                                tint = textColor.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Rounded.NotificationsNone, contentDescription = "Notifications", tint = textColor.copy(alpha = 0.7f))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cardBgColor,
                        titleContentColor = textColor
                    ),
                    modifier = Modifier.shadow(2.dp)
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .shadow(12.dp, RoundedCornerShape(36.dp)),
                        shape = RoundedCornerShape(36.dp),
                        color = cardBgColor
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CapsuleNavItem(Icons.Rounded.AssignmentInd, "ID Card", onIdCardClick, textColor)
                            CapsuleNavItem(Icons.Rounded.Download, "Downloads", onDownloadsClick, textColor)
                            
                            Spacer(modifier = Modifier.width(72.dp)) // Center space
                            
                            CapsuleNavItem(Icons.Rounded.Notifications, "Notices", onNoticesClick, textColor)
                            CapsuleNavItem(Icons.Rounded.Person, "Profile", onProfileClick, textColor)
                        }
                    }
                    
                    // Floating Center Button
                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .offset(y = (-20).dp)
                            .shadow(8.dp, CircleShape)
                            .clickable(onClick = { /* Home/Dashboard Action */ }),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(primaryColor, secondaryColor))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.GridView, contentDescription = "Dashboard", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Premium Profile Card
                item {
                    ProfileGradientCard(data, subjects.size, primaryColor, textColor, isDarkTheme)
                }

                // Quick Action Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard("Attendance", Icons.Rounded.CalendarToday, primaryColor, cardBgColor, Modifier.weight(1f), onAttendanceClick)
                        QuickActionCard("Assignments", Icons.AutoMirrored.Rounded.Assignment, primaryColor, cardBgColor, Modifier.weight(1f), onAssignmentsClick)
                        QuickActionCard("Results", Icons.Rounded.Star, primaryColor, cardBgColor, Modifier.weight(1f), onResultsClick)
                        QuickActionCard("Subjects", Icons.Rounded.Book, primaryColor, cardBgColor, Modifier.weight(1f), onSubjectsClick)
                    }
                }

                // 2. Academic Overview
                item {
                    val summary = attendanceSummary ?: AttendanceSummaryDto(20, 16, 2, 0, 80f, "Good")
                    AcademicOverviewCard(summary, primaryColor, secondaryColor, textColor, accentColor, cardBgColor)
                }
                
                // 3. Stats Summary Cards (Reduced size, re-added downloads and unread notices)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsSummaryCard("Unread Notices", recentNotices.size.toString(), Icons.Rounded.NotificationsActive, primaryColor, cardBgColor, Modifier.weight(1f), onNoticesClick)
                            StatsSummaryCard("Downloads", materialCount.toString(), Icons.Rounded.CloudDownload, Color(0xFF0EA5E9), cardBgColor, Modifier.weight(1f), onDownloadsClick)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsSummaryCard("Subjects", subjects.size.toString(), Icons.Rounded.Book, primaryColor, cardBgColor, Modifier.weight(1f), onSubjectsClick)
                            StatsSummaryCard("Pending Tasks", assignments.count { it.status.lowercase() == "pending" }.toString(), Icons.AutoMirrored.Rounded.Assignment, Color(0xFFEF4444), cardBgColor, Modifier.weight(1f), onAssignmentsClick)
                        }
                    }
                }

                // 4. Enrolled Subjects (Restored section)
                if (subjects.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "My Subjects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                                Text(text = "View All", style = MaterialTheme.typography.labelLarge, color = primaryColor, modifier = Modifier.clickable { onSubjectsClick() })
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(subjects) { subject ->
                                    SubjectMiniCard(subject, primaryColor, textColor, cardBgColor)
                                }
                            }
                        }
                    }
                }

                // 5. Today's Schedule
                if (todayClasses.isNotEmpty()) {
                    item {
                        Column {
                            Text(text = "Today's Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            todayClasses.forEach { cls ->
                                ModernScheduleCard(cls, primaryColor, textColor, cardBgColor)
                            }
                        }
                    }
                }

                // 6. Assignment Progress
                item {
                    AssignmentProgressCard(assignments, primaryColor, secondaryColor, textColor, accentColor, cardBgColor)
                }

                // 7. Recent Activity (Notices)
                item {
                    ActivityFeedSection(recentNotices, primaryColor, textColor)
                }

                item { Spacer(modifier = Modifier.height(110.dp)) }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun ProfileGradientCard(data: StudentDashboardDto, subjectCount: Int, primaryColor: Color, textColor: Color, isDarkTheme: Boolean) {
    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E293B))
    } else {
        listOf(Color(0xFFF1F5F9), Color(0xFFDBEAFE), Color(0xFFEFF6FF))
    }

    val contentTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF1E293B).copy(alpha = 0.6f)
    val badgeBgColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.85f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(colors = gradientColors),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.05f),
                radius = 350f,
                center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, -size.height * 0.2f)
            )
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Good Morning 👋", style = MaterialTheme.typography.bodyMedium, color = subTextColor)
                    Text(text = data.studentName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = contentTextColor, maxLines = 1)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${data.program} • Semester ${data.semester}", style = MaterialTheme.typography.bodySmall, color = subTextColor)
                }
                Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = if (isDarkTheme) Color(0xFF334155) else Color.White, shadowElevation = 4.dp) {
                    if (!data.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = data.avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(14.dp), tint = primaryColor)
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(color = badgeBgColor, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "$subjectCount Subjects", style = MaterialTheme.typography.labelLarge, color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, color: Color, cardBgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(110.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFDBEAFE).copy(alpha = 0.7f)) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(10.dp), tint = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun AcademicOverviewCard(summary: AttendanceSummaryDto, primaryColor: Color, secondaryColor: Color, textColor: Color, accentColor: Color, cardBgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Academic Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(color = accentColor.copy(alpha = 0.4f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor)), startAngle = -90f, sweepAngle = (summary.attendancePercentage / 100f) * 360f, useCenter = false, style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${summary.attendancePercentage.toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = "Attendance", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OverviewStatRow("Present", summary.present.toString(), Color(0xFF10B981), textColor)
                    OverviewStatRow("Absent", summary.absent.toString(), Color(0xFFEF4444), textColor)
                    OverviewStatRow("Late", summary.late.toString(), Color(0xFFF59E0B), textColor)
                    OverviewStatRow("Total", summary.totalClasses.toString(), primaryColor, textColor)
                }
            }
        }
    }
}

@Composable
fun OverviewStatRow(label: String, value: String, color: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.6f), modifier = Modifier.width(70.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun StatsSummaryCard(label: String, value: String, icon: ImageVector, color: Color, cardBgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
                    Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = color)
                }
                Spacer(Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SubjectMiniCard(subject: SubjectDto, primaryColor: Color, textColor: Color, cardBgColor: Color) {
    Card(modifier = Modifier.width(140.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(32.dp).background(primaryColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text(text = (subject.code ?: subject.name).take(2).uppercase(), style = MaterialTheme.typography.labelSmall, color = primaryColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = subject.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, maxLines = 2, minLines = 2)
            Text(text = subject.code ?: "", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ModernScheduleCard(cls: ClassDto, primaryColor: Color, textColor: Color, cardBgColor: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(80.dp)) {
                Text(text = cls.time.split(" ").firstOrNull() ?: "", fontWeight = FontWeight.Bold, color = textColor)
                Text(text = cls.time.split(" ").lastOrNull() ?: "", style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
            }
            Box(modifier = Modifier.width(3.dp).height(32.dp).background(primaryColor, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = cls.subject, fontWeight = FontWeight.Bold, color = textColor)
                Text(text = "Room ${cls.room}", style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AssignmentProgressCard(assignments: List<AssignmentDto>, primaryColor: Color, secondaryColor: Color, textColor: Color, accentColor: Color, cardBgColor: Color) {
    val pending = assignments.count { it.status.lowercase() == "pending" }
    val submitted = assignments.count { it.status.lowercase() == "submitted" }
    val graded = assignments.count { it.status.lowercase() == "graded" }
    val total = assignments.size.coerceAtLeast(1)
    
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Assignment Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(progress = { (submitted + graded).toFloat() / total }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = secondaryColor, trackColor = accentColor)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem("Pending", pending.toString(), Color(0xFFEF4444), textColor)
                StatItem("Submitted", submitted.toString(), Color(0xFFF59E0B), textColor)
                StatItem("Graded", graded.toString(), Color(0xFF10B981), textColor)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ActivityFeedSection(notices: List<NoticeDto>, primaryColor: Color, textColor: Color) {
    Column {
        Text(text = "Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(20.dp))
        if (notices.isEmpty()) {
            Text(text = "No recent activity", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.5f))
        } else {
            Column {
                notices.take(4).forEachIndexed { index, notice ->
                    ActivityItem(notice, primaryColor, textColor, isLast = index == notices.take(4).lastIndex)
                }
            }
        }
    }
}

@Composable
fun ActivityItem(notice: NoticeDto, primaryColor: Color, textColor: Color, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(12.dp), shape = CircleShape, color = primaryColor) {}
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).weight(1f).background(primaryColor.copy(alpha = 0.1f)))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(text = notice.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = notice.date, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun CapsuleNavItem(icon: ImageVector, label: String, onClick: () -> Unit, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(4.dp)) {
        Icon(icon, contentDescription = label, tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f), fontSize = 10.sp)
    }
}
