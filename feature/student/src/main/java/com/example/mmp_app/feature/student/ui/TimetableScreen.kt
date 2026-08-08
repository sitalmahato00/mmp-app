package com.example.mmp_app.feature.student.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Room
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.DaySchedule
import com.example.mmp_app.domain.model.TimetableClass
import com.example.mmp_app.domain.model.TimetableData
import java.util.*

// Updated colors to match web grid exactly
private val TableHeaderBg = Color(0xFF1E3A5F) // Dark Navy
private val GroupAHeaderBg = Color(0xFF1D4ED8) // Blue
private val GroupBHeaderBg = Color(0xFF16A34A) // Green
private val CommonCardBorder = Color(0xFFA855F7) // Purple
private val CommonCardBg = Color(0xFFFAF5FF)
private val GroupACardBorder = Color(0xFF3B82F6) // Blue
private val GroupACardBg = Color(0xFFEFF6FF)
private val GroupBCardBorder = Color(0xFF22C55E) // Green
private val GroupBCardBg = Color(0xFFF0FDF4)
private val DayCellBg = Color(0xFFF1F5F9)
private val PeriodCellBg = Color(0xFFF8FAFC)
private val FreePeriodText = Color(0xFF94A3B8)
private val DayRowSeparator = Color(0xFF334155)
private val TodayHighlightBg = Color(0xFFFFF7ED) // yellow-50 equivalent
private val TableBorder = Color(0xFFCBD5E1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
) {
    val viewModel: TimetableViewModel = hiltViewModel()
    val timetableData by viewModel.timetableData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadFullTimetable()
    }

    Scaffold(
        topBar = {
            if (showSystemHeader) {
                TopAppBar(
                    title = { Text("My Timetable", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        } else if (onMenuClick != null) {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadFullTimetable() },
            state = pullToRefreshState,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (timetableData != null) {
                    TimetableInfoHeader(timetableData!!)
                    
                    if (timetableData?.hasTimetable == false) {
                        TimetableEmptyState(timetableData?.semester?.toString() ?: "N/A")
                    } else {
                        TimetableGrid(timetableData!!)
                    }
                } else if (isLoading) {
                    TimetableTableSkeleton()
                } else if (error != null) {
                    TimetableErrorState(error!!) { viewModel.loadFullTimetable() }
                }
            }
        }
    }
}

@Composable
fun TimetableInfoHeader(data: TimetableData) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Semester ${data.semester ?: "N/A"} • Section ${data.section ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Academic Year: ${data.academicSession ?: "N/A"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                data.effectiveFrom?.let {
                    Text(
                        text = "Effective: $it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TimetableGrid(data: TimetableData) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    
    val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val currentDay = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH) ?: ""
    
    val filteredTimetable = data.timetable?.filter { it.classes.isNotEmpty() }?.sortedBy { daysOrder.indexOf(it.day) } ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
        // Table Header
        Column(modifier = Modifier.width(IntrinsicSize.Min)) {
            // Row 1
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                TableHeaderCell("DAY", TableHeaderBg, 60.dp)
                TableHeaderCell("PERIOD", TableHeaderBg, 120.dp)
                TableHeaderCell("SUBJECT", TableHeaderBg, 600.dp)
            }
            // Row 2
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(modifier = Modifier.width(60.dp).fillMaxHeight().background(TableHeaderBg).border(0.5.dp, TableBorder))
                Box(modifier = Modifier.width(120.dp).fillMaxHeight().background(TableHeaderBg).border(0.5.dp, TableBorder))
                TableHeaderCell("GROUP A", GroupAHeaderBg, 300.dp)
                TableHeaderCell("GROUP B", GroupBHeaderBg, 300.dp)
            }
        }
        
        // Table Body
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .fillMaxHeight()
                .verticalScroll(verticalScrollState)
        ) {
            filteredTimetable.forEach { daySchedule ->
                val isToday = daySchedule.day.equals(currentDay, ignoreCase = true)
                DayGroup(daySchedule, isToday)
                // Thick separator between days
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(DayRowSeparator))
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun TableHeaderCell(text: String, bgColor: Color, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(bgColor)
            .border(0.5.dp, TableBorder)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DayGroup(daySchedule: DaySchedule, isToday: Boolean) {
    // Group classes by time slot
    val slots = daySchedule.classes.groupBy { "${it.startTime}-${it.endTime}" }
    
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Day Cell (Rowspan equivalent)
        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(if (isToday) TodayHighlightBg else DayCellBg)
                .border(0.5.dp, TableBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = daySchedule.day.take(3).uppercase(),
                fontWeight = FontWeight.ExtraBold,
                color = if (isToday) Color.Black else Color.DarkGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        
        Column(modifier = Modifier.width(720.dp)) {
            slots.forEach { (timeKey, classesInSlot) ->
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    // Period Cell
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .fillMaxHeight()
                            .background(PeriodCellBg)
                            .border(0.5.dp, TableBorder)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstCls = classesInSlot.first()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = firstCls.startTime ?: "", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                            Text(text = "to", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 8.sp)
                            Text(text = firstCls.endTime ?: "", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    // Subject Cells
                    val commonCls = classesInSlot.find { it.group.isNullOrEmpty() }
                    if (commonCls != null) {
                        // CASE 1: COMMON slot
                        Box(
                            modifier = Modifier
                                .width(600.dp)
                                .fillMaxHeight()
                                .border(0.5.dp, TableBorder)
                                .padding(8.dp)
                        ) {
                            if (commonCls.type?.lowercase() == "break") {
                                Text(
                                    text = "BREAK",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            } else {
                                SubjectCard(commonCls, CommonCardBorder, CommonCardBg)
                            }
                        }
                    } else {
                        // CASE 2: SEPARATE slots
                        val groupA = classesInSlot.find { it.group == "A" }
                        val groupB = classesInSlot.find { it.group == "B" }
                        
                        // Group A column
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .fillMaxHeight()
                                .border(0.5.dp, TableBorder)
                                .padding(8.dp)
                        ) {
                            if (groupA != null) {
                                SubjectCard(groupA, GroupACardBorder, GroupACardBg)
                            } else {
                                FreePeriodPlaceholder()
                            }
                        }
                        
                        // Group B column
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .fillMaxHeight()
                                .border(0.5.dp, TableBorder)
                                .padding(8.dp)
                        ) {
                            if (groupB != null) {
                                SubjectCard(groupB, GroupBCardBorder, GroupBCardBg)
                            } else {
                                FreePeriodPlaceholder()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FreePeriodPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Free Period",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = FreePeriodText
        )
    }
}

@Composable
fun SubjectCard(cls: TimetableClass, borderColor: Color, bgColor: Color) {
    val type = cls.type?.lowercase() ?: "theory"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                drawLine(
                    color = borderColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = cls.subject ?: "TBA",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = cls.subjectCode ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = cls.teacher ?: "TBA", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Room, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = cls.room ?: "TBA", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        }
        
        if (type != "theory" && type != "break") {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = borderColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = type.uppercase(),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    color = borderColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun TimetableEmptyState(semester: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Rounded.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No Timetable Available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your timetable for Semester $semester has not been set up yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TimetableTableSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(8.dp))
        repeat(5) {
            Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(modifier = Modifier.width(60.dp).fillMaxHeight(), shape = RoundedCornerShape(4.dp))
                SkeletonBox(modifier = Modifier.width(100.dp).fillMaxHeight(), shape = RoundedCornerShape(4.dp))
                SkeletonBox(modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(4.dp))
            }
        }
    }
}

@Composable
fun TimetableErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
