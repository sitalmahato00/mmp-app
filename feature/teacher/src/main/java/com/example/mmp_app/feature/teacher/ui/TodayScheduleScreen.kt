package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.domain.model.ClassSlotDto
import com.example.mmp_app.domain.model.TodayScheduleDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScheduleScreen(
    onBack: () -> Unit,
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    showSystemHeader: Boolean = true,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val viewModel: TeacherViewModel = hiltViewModel()
    val schedule by viewModel.teacherSchedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTodaySchedule()
    }

    val primaryColor = Color(0xFF1565C0)
    val lightBlue = Color(0xFFE3F2FD)
    val backgroundColor = Color(0xFFF5F7FA)

    val content = @Composable { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading && schedule == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (error != null && schedule == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.WifiOff,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.Red
                        )
                        Text(text = error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.loadTodaySchedule() }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                schedule?.let { data ->
                    TodayScheduleContent(
                        data = data,
                        primaryColor = primaryColor,
                        lightBlue = lightBlue,
                        onNavigateToTimetable = onNavigateToTimetable,
                        onNavigateToClasses = onNavigateToClasses
                    )
                }
            }
        }
    }

    if (showSystemHeader) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Today's Schedule", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                        }
                        IconButton(onClick = { viewModel.loadTodaySchedule() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = backgroundColor,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            content(padding)
        }
    } else {
        content(PaddingValues(0.dp))
    }
}

@Composable
fun TodayScheduleContent(
    data: TodayScheduleDto,
    primaryColor: Color,
    lightBlue: Color,
    onNavigateToTimetable: () -> Unit,
    onNavigateToClasses: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Date Banner
        item {
            DateBanner(data, lightBlue, primaryColor)
        }

        // Day Selector
        item {
            DaySelector(activeDay = data.day, primaryColor = primaryColor)
        }

        if (data.classes.isEmpty()) {
            item {
                EmptyScheduleState(data.day, onNavigateToTimetable, onNavigateToClasses)
            }
        } else {
            item {
                Surface(
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${data.classes.size} classes today",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = primaryColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(data.classes) { cls ->
                TeacherClassCard(cls, primaryColor)
            }
        }
    }
}

@Composable
fun DateBanner(data: TodayScheduleDto, lightBlue: Color, primaryColor: Color) {
    val displayDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(data.today)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        data.today
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(lightBlue, Color(0xFFBBDEFB))),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = data.day,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
            Icon(
                Icons.Rounded.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = primaryColor
            )
        }
    }
}

@Composable
fun DaySelector(activeDay: String, primaryColor: Color) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val fullDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(days.zip(fullDays)) { (short, full) ->
                val isActive = full.equals(activeDay, ignoreCase = true)
                FilterChip(
                    selected = isActive,
                    onClick = {
                        // In a real app, this would show a Snackbar
                    },
                    label = { Text(short) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White,
                        labelColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = if (!isActive) FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = Color.LightGray) else null
                )
            }
        }
    }
}

@Composable
fun EmptyScheduleState(
    day: String,
    onNavigateToTimetable: () -> Unit,
    onNavigateToClasses: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFBDBDBD)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Classes Today",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You have no scheduled classes\nfor $day",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onNavigateToTimetable,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View Full Timetable")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onNavigateToClasses,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("My Subjects")
        }
    }
}

@Composable
fun TeacherClassCard(cls: ClassSlotDto, primaryColor: Color) {
    val typeColor = when (cls.type?.lowercase()) {
        "lecture" -> Color(0xFF1565C0)
        "lab" -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(typeColor)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cls.subject ?: "No Subject",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = cls.subjectCode ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Surface(
                        color = typeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = cls.type ?: "Lecture",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = typeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScheduleInfoItem(Icons.Rounded.Schedule, "${cls.startTime} – ${cls.endTime}")
                    ScheduleInfoItem(Icons.Rounded.Room, cls.room ?: "TBA")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScheduleInfoItem(Icons.Rounded.Groups, "${cls.program ?: ""} Sem ${cls.semester ?: ""}")
                    if (!cls.section.isNullOrEmpty()) {
                        Surface(
                            color = primaryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Section ${cls.section}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = primaryColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
