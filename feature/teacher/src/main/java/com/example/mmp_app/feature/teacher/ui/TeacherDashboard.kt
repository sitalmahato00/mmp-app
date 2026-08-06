package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.domain.model.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    data: TeacherDashboardDto,
    profile: TeacherProfileDto? = null,
    schedule: TodayScheduleDto? = null,
    classes: List<TeacherSubjectDto> = emptyList(),
    onRecordAttendance: (Int, String) -> Unit = { _, _ -> },
    onRecordMarks: (Int, String) -> Unit = { _, _ -> },
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadCount: Int = 0,
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToMarks: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    showSystemHeader: Boolean = false
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val primaryColor = Color(0xFF1565C0)
    val secondaryColor = Color(0xFF42A5F5)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF5F7FA)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1A1A1A)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White
    val accentColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE3F2FD)

    val dashboardContent = @Composable { paddingValues: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Welcome Card
            item {
                TeacherWelcomeCard(
                    data = data,
                    profile = profile,
                    subjectCount = classes.size,
                    primaryColor = primaryColor,
                    isDarkTheme = isDarkTheme
                )
            }

            // 2. Quick Action Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard("Schedule", Icons.Rounded.CalendarMonth, primaryColor, cardBgColor, Modifier.weight(1f), onNavigateToSchedule)
                    QuickActionCard("Attendance", Icons.Rounded.CheckCircle, primaryColor, cardBgColor, Modifier.weight(1f), onNavigateToAttendance)
                    QuickActionCard("Marks", Icons.Rounded.EditNote, primaryColor, cardBgColor, Modifier.weight(1f), onNavigateToMarks)
                    QuickActionCard("Assignments", Icons.AutoMirrored.Rounded.Assignment, primaryColor, cardBgColor, Modifier.weight(1f), onNavigateToAssignments)
                }
            }

            // 3. Teaching Overview
            item {
                TeachingOverviewCard(
                    data = data,
                    subjectCount = classes.size,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textColor = textColor,
                    accentColor = accentColor,
                    cardBgColor = cardBgColor
                )
            }

            // 4. Today's Classes
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Classes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = schedule?.day ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val todayClasses = schedule?.classes ?: emptyList()
                    if (todayClasses.isEmpty()) {
                        TeacherEmptyScheduleState(cardBgColor, textColor)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(todayClasses) { cls ->
                                TeacherScheduleCard(cls, primaryColor, textColor, cardBgColor)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(110.dp)) }
        }
    }

    if (showSystemHeader) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                TeacherDrawerContent(
                    profile = profile,
                    primaryColor = primaryColor,
                    cardBgColor = cardBgColor,
                    onCloseDrawer = { scope.launch { drawerState.close() } },
                    onDashboardClick = { scope.launch { drawerState.close() } },
                    onScheduleClick = onNavigateToSchedule,
                    onClassesClick = onNavigateToClasses,
                    onAttendanceClick = onNavigateToAttendance,
                    onMarksClick = onNavigateToMarks,
                    onAssignmentsClick = onNavigateToAssignments,
                    onStudentsClick = { /* TODO */ },
                    onTimetableClick = { /* TODO */ },
                    onReportsClick = { /* TODO */ },
                    onNotificationsClick = onNotificationsClick,
                    onProfileClick = onNavigateToProfile,
                    onLogoutClick = onLogoutClick,
                    unreadCount = unreadCount
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TeacherTopBar(
                        textColor = textColor,
                        cardBgColor = cardBgColor,
                        primaryColor = primaryColor,
                        isDarkTheme = isDarkTheme,
                        unreadCount = unreadCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onToggleTheme = onToggleTheme,
                        onNotificationsClick = onNotificationsClick
                    )
                },
                bottomBar = {
                    TeacherBottomNavBar(
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor,
                        onScheduleClick = onNavigateToSchedule,
                        onAssignmentsClick = onNavigateToAssignments,
                        onHomeClick = { /* Already at home */ },
                        onNoticesClick = onNavigateToNotices,
                        onProfileClick = onNavigateToProfile
                    )
                },
                containerColor = backgroundColor
            ) { paddingValues ->
                dashboardContent(paddingValues)
            }
        }
    } else {
        dashboardContent(PaddingValues(0.dp))
    }
}

@Composable
fun TeacherWelcomeCard(
    data: TeacherDashboardDto,
    profile: TeacherProfileDto?,
    subjectCount: Int,
    primaryColor: Color,
    isDarkTheme: Boolean
) {
    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
    }

    val contentTextColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good Morning 👋"
        hour < 17 -> "Good Afternoon ☀️"
        else -> "Good Evening 🌙"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(colors = gradientColors),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = greeting, style = MaterialTheme.typography.bodyMedium, color = subTextColor)
                    Text(
                        text = profile?.name ?: data.teacherName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentTextColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile?.designation ?: "Teacher"} • ${profile?.department ?: "Department"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subTextColor
                    )
                }
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    val avatarUrl = profile?.avatarUrl
                    if (!avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(16.dp), tint = primaryColor)
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "📚 $subjectCount Subjects",
                        style = MaterialTheme.typography.labelLarge,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, color: Color, cardBgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TeachingOverviewCard(
    data: TeacherDashboardDto,
    subjectCount: Int,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color,
    accentColor: Color,
    cardBgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Teaching Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = accentColor.copy(alpha = 0.4f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = (data.totalClasses / 10f).coerceIn(0f, 1f) * 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${data.totalClasses}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Classes\nTaken",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewLegendRow("Classes Taken", "${data.totalClasses}", Color(0xFF4CAF50), textColor)
                    OverviewLegendRow("Pending Marks", "${data.pendingMarks}", Color(0xFFF44336), textColor)
                    OverviewLegendRow("Pending Assignments", "${data.pendingAssignments}", Color(0xFFFF9800), textColor)
                    OverviewLegendRow("Total Subjects", "$subjectCount", primaryColor, textColor)
                }
            }
        }
    }
}

@Composable
fun OverviewLegendRow(label: String, value: String, color: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun TeacherScheduleCard(cls: ClassSlotDto, primaryColor: Color, textColor: Color, cardBgColor: Color) {
    val typeColor = if (cls.type?.lowercase() == "lab") Color(0xFFFF9800) else primaryColor

    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = typeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = cls.type ?: "Lecture",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = typeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${cls.startTime} - ${cls.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = cls.subject ?: "No Subject",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Room, contentDescription = null, modifier = Modifier.size(14.dp), tint = textColor.copy(alpha = 0.5f))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Room: ${cls.room ?: "TBA"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${cls.program ?: ""} ${cls.semester ?: ""} Sem ${cls.section ?: ""}",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TeacherEmptyScheduleState(cardBgColor: Color, textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = textColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No classes scheduled for today",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTopBar(
    textColor: Color,
    cardBgColor: Color,
    primaryColor: Color,
    isDarkTheme: Boolean,
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    com.example.mmp_app.core.R.drawable.mmplogo.let { logo ->
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = logo),
                            contentDescription = "College Logo",
                            modifier = Modifier.padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
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
            IconButton(onClick = onMenuClick) {
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
                BadgedBox(badge = {
                    if (unreadCount > 0) {
                        Badge { Text(unreadCount.toString()) }
                    }
                }) {
                    Icon(Icons.Rounded.NotificationsNone, contentDescription = "Notifications", tint = textColor.copy(alpha = 0.7f))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = cardBgColor,
            titleContentColor = textColor
        ),
        modifier = Modifier.shadow(2.dp)
    )
}

@Composable
fun TeacherBottomNavBar(
    primaryColor: Color,
    secondaryColor: Color,
    cardBgColor: Color,
    textColor: Color,
    onScheduleClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNoticesClick: () -> Unit,
    onProfileClick: () -> Unit,
    selectedItemIndex: Int = 0
) {
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
                TeacherCapsuleNavItem(Icons.Rounded.CalendarMonth, "Schedule", onScheduleClick, textColor, selectedItemIndex == 1, activeColor = primaryColor)
                TeacherCapsuleNavItem(Icons.AutoMirrored.Rounded.Assignment, "Assignments", onAssignmentsClick, textColor, selectedItemIndex == 2, activeColor = primaryColor)
                
                Spacer(modifier = Modifier.width(72.dp))
                
                TeacherCapsuleNavItem(Icons.Rounded.Notifications, "Notices", onNoticesClick, textColor, selectedItemIndex == 3, activeColor = primaryColor)
                TeacherCapsuleNavItem(Icons.Rounded.Person, "Profile", onProfileClick, textColor, selectedItemIndex == 4, activeColor = primaryColor)
            }
        }
        
        Surface(
            modifier = Modifier
                .size(72.dp)
                .offset(y = (-20).dp)
                .shadow(8.dp, CircleShape)
                .clickable(onClick = onHomeClick),
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
}

@Composable
fun TeacherCapsuleNavItem(icon: ImageVector, label: String, onClick: () -> Unit, textColor: Color, isSelected: Boolean, activeColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else textColor.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) activeColor else textColor.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun TeacherDrawerContent(
    profile: TeacherProfileDto?,
    primaryColor: Color,
    cardBgColor: Color,
    onCloseDrawer: () -> Unit,
    onDashboardClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onClassesClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onMarksClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onTimetableClick: () -> Unit,
    onReportsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    unreadCount: Int
) {
    ModalDrawerSheet(
        drawerContainerColor = cardBgColor,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.8f))))
                .padding(24.dp)
        ) {
            Column {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                ) {
                    if (!profile?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = profile?.avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.padding(14.dp), tint = primaryColor)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = profile?.name ?: "Teacher Name",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${profile?.designation ?: "Teacher"} • ${profile?.department ?: "IT"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Online", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        LazyColumn(modifier = Modifier.padding(12.dp)) {
            item { TeacherDrawerItem("Dashboard", Icons.Rounded.Home, true, onDashboardClick) }
            item { TeacherDrawerItem("Today's Schedule", Icons.Rounded.CalendarMonth, false, { onCloseDrawer(); onScheduleClick() }) }
            item { TeacherDrawerItem("My Classes", Icons.Rounded.AutoStories, false, { onCloseDrawer(); onClassesClick() }) }
            item {
                TeacherExpandableDrawerItem(
                    "Attendance",
                    Icons.Rounded.CheckCircle,
                    listOf("Mark Attendance" to onAttendanceClick, "History" to {})
                )
            }
            item {
                TeacherExpandableDrawerItem(
                    "Marks Entry",
                    Icons.Rounded.EditNote,
                    listOf("Pending" to {}, "Submit Marks" to onMarksClick)
                )
            }
            item {
                TeacherExpandableDrawerItem(
                    "Assignments",
                    Icons.AutoMirrored.Rounded.Assignment,
                    listOf("All" to onAssignmentsClick, "Create New" to {})
                )
            }
            item { TeacherDrawerItem("Students", Icons.Rounded.Groups, false, { onCloseDrawer(); onStudentsClick() }) }
            item { TeacherDrawerItem("Timetable", Icons.Rounded.Schedule, false, { onCloseDrawer(); onTimetableClick() }) }
            item {
                TeacherExpandableDrawerItem(
                    "Reports",
                    Icons.Rounded.BarChart,
                    listOf("Attendance Report" to {}, "Marks Report" to {})
                )
            }
            item {
                TeacherDrawerItem(
                    "Notifications",
                    Icons.Rounded.Notifications,
                    false,
                    { onCloseDrawer(); onNotificationsClick() },
                    badgeCount = unreadCount
                )
            }
            item { TeacherDrawerItem("Profile", Icons.Rounded.AccountCircle, false, { onCloseDrawer(); onProfileClick() }) }
            
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.Red) },
                    selected = false,
                    onClick = { onCloseDrawer(); onLogoutClick() },
                    icon = { Icon(Icons.Rounded.Logout, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Composable
fun TeacherDrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, badgeCount: Int = 0) {
    NavigationDrawerItem(
        label = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label)
                if (badgeCount > 0) {
                    Badge { Text(badgeCount.toString()) }
                }
            }
        },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun TeacherExpandableDrawerItem(label: String, icon: ImageVector, subItems: List<Pair<String, () -> Unit>>) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        NavigationDrawerItem(
            label = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label)
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            selected = false,
            onClick = { expanded = !expanded },
            icon = { Icon(icon, contentDescription = null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        if (expanded) {
            subItems.forEach { (subLabel, onClick) ->
                NavigationDrawerItem(
                    label = { Text(subLabel, modifier = Modifier.padding(start = 16.dp)) },
                    selected = false,
                    onClick = onClick,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherDashboardPreview() {
    MMPAppTheme {
        TeacherDashboard(
            data = TeacherDashboardDto(
                teacherName = "Binay pokheral",
                totalClasses = 1,
                totalStudents = 0,
                pendingMarks = 0,
                pendingAssignments = 0
            ),
            profile = TeacherProfileDto(
                name = "Binay pokheral",
                email = "hellogoog94@gmail.com",
                designation = "Teacher",
                department = "Information Technology",
                employmentType = "permanent"
            ),
            classes = listOf(
                TeacherSubjectDto(1, "c programming", "cs001"),
                TeacherSubjectDto(2, "Web Development", "WE001")
            )
        )
    }
}
