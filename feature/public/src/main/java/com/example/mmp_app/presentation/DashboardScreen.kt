package com.example.mmp_app.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.core.R
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.core.ui.StudentDashboard

import com.example.mmp_app.domain.model.*
import com.example.mmp_app.feature.teacher.ui.TeacherDashboard
import com.example.mmp_app.feature.parent.ui.ParentDashboard
import kotlinx.coroutines.launch


@Composable
fun DashboardScreen(
    userProfile: UserProfile?,
    onLogout: () -> Unit,

    onNavigateToAttendance: () -> Unit = {},
    onNavigateToMarks: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToFees: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onRecordAttendance: (Int, String) -> Unit = { _, _ -> },
    onRecordMarks: (Int, String) -> Unit = { _, _ -> },
    onNavigateToChildDetails: (Int, String) -> Unit = { _, _ -> },
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    
    val studentData by viewModel.studentDashboard.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val attendanceSummary by viewModel.attendanceSummary.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val timetable by viewModel.timetable.collectAsState()
    val teacherData by viewModel.teacherDashboard.collectAsState()
    val parentData by viewModel.parentDashboard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(userProfile) {
        when (userProfile?.role?.lowercase()) {
            "student" -> {
                viewModel.loadStudentDashboardData()
            }
            "teacher" -> viewModel.loadTeacherDashboard()
            "parent" -> viewModel.loadParentDashboard()
        }
    }

    DashboardAdaptiveContent(
        userProfile = userProfile,
        studentData = studentData,
        recentNotices = notices,
        attendanceSummary = attendanceSummary,
        subjects = subjects,
        assignments = assignments,
        timetable = timetable,
        teacherData = teacherData,
        parentData = parentData,
        isLoading = isLoading,
        error = error,
        onLogout = onLogout,
        onRetry = {
            when (userProfile?.role?.lowercase()) {
                "student" -> {
                    viewModel.loadStudentDashboardData()
                }
                "teacher" -> viewModel.loadTeacherDashboard()
                "parent" -> viewModel.loadParentDashboard()
            }
        },
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToMarks = onNavigateToMarks,
        onNavigateToAssignments = onNavigateToAssignments,
        onNavigateToFees = onNavigateToFees,
        onNavigateToNotices = onNavigateToNotices,
        onRecordAttendance = onRecordAttendance,
        onRecordMarks = onRecordMarks,
        onNavigateToChildDetails = onNavigateToChildDetails,
        onNavigateToRoutines = onNavigateToRoutines,
        onNavigateToExams = onNavigateToExams,
        onNavigateToResults = onNavigateToResults,
        onNavigateToSubjects = onNavigateToSubjects,
        onNavigateToTimetable = onNavigateToTimetable,
        onNavigateToDownloads = onNavigateToDownloads,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToSettings = onNavigateToSettings,
        isDarkTheme = isDarkTheme,
        onToggleTheme = { themeViewModel.toggleTheme() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdaptiveContent(
    userProfile: UserProfile?,
    studentData: StudentDashboardDto?,
    recentNotices: List<NoticeDto> = emptyList(),
    attendanceSummary: AttendanceSummaryDto? = null,
    subjects: List<SubjectDto> = emptyList(),
    assignments: List<AssignmentDto> = emptyList(),
    timetable: List<ClassDto> = emptyList(),
    teacherData: TeacherDashboardDto?,
    parentData: ParentDashboardDto?,
    isLoading: Boolean,
    error: String? = null,
    onLogout: () -> Unit,
    onRetry: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToMarks: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToFees: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onRecordAttendance: (Int, String) -> Unit = { _, _ -> },
    onRecordMarks: (Int, String) -> Unit = { _, _ -> },
    onNavigateToChildDetails: (Int, String) -> Unit = { _, _ -> },
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val navSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mmplogo),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = userProfile?.name ?: "Guest User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userProfile?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = selectedItem == 0,
                    onClick = { 
                        selectedItem = 0
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Rounded.Dashboard, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                // Add other drawer items if needed...
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, "Menu", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.mmplogo),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "MMP College", 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                        }
                        IconButton(onClick = onNavigateToNotices) {
                            Icon(Icons.Rounded.NotificationsNone, null)
                        }
                    }
                )
            },
            bottomBar = {
                if (navSuiteType == NavigationSuiteType.NavigationBar) {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                            .navigationBarsPadding(),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            modifier = Modifier.height(72.dp)
                        ) {
                            NavigationBarItem(
                                selected = selectedItem == 0,
                                onClick = { selectedItem = 0 },
                                icon = { Icon(Icons.Rounded.Dashboard, null) },
                                label = { Text("Home") }
                            )
                            if (userProfile?.role?.lowercase() == "student") {
                                NavigationBarItem(
                                    selected = selectedItem == 1,
                                    onClick = { selectedItem = 1 },
                                    icon = { Icon(Icons.Rounded.AutoStories, null) },
                                    label = { Text("Courses") }
                                )
                                NavigationBarItem(
                                    selected = selectedItem == 2,
                                    onClick = { selectedItem = 2 },
                                    icon = { Icon(Icons.Rounded.EventNote, null) },
                                    label = { Text("Schedule") }
                                )
                            } else {
                                NavigationBarItem(
                                    selected = selectedItem == 1,
                                    onClick = { selectedItem = 1 },
                                    icon = { Icon(Icons.Rounded.Groups, null) },
                                    label = { Text("Users") }
                                )
                            }
                            NavigationBarItem(
                                selected = selectedItem == 3,
                                onClick = { selectedItem = 3 },
                                icon = { Icon(Icons.Rounded.AccountCircle, null) },
                                label = { Text("Profile") }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Row(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (navSuiteType == NavigationSuiteType.NavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        NavigationRailItem(
                            selected = selectedItem == 0,
                            onClick = { selectedItem = 0 },
                            icon = { Icon(Icons.Rounded.Dashboard, null) },
                            label = { Text("Home") }
                        )
                        // Rail items matching bottom nav...
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && studentData == null && teacherData == null && parentData == null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (error != null && studentData == null && teacherData == null && parentData == null) {
                        ErrorState(error!!, onRetry)
                    } else {
                        when (selectedItem) {
                            0 -> MainDashboardContent(
                                userProfile, studentData, recentNotices, attendanceSummary, 
                                subjects, assignments, timetable, teacherData, parentData,
                                onNavigateToAttendance, onNavigateToMarks, onNavigateToAssignments,
                                onNavigateToFees, onNavigateToNotices, onRecordAttendance,
                                onRecordMarks, onNavigateToChildDetails, onNavigateToRoutines,
                                onNavigateToExams, onNavigateToResults, onNavigateToSubjects,
                                onNavigateToTimetable, onNavigateToDownloads, onNavigateToProfile,
                                onLogout
                            )
                            1 -> if (userProfile?.role?.lowercase() == "student") {
                                SubjectsScreenContent(onNavigateToSubjects)
                            } else {
                                UsersScreenContent(userProfile)
                            }
                            2 -> TimetableScreenContent(onNavigateToTimetable)
                            3 -> ProfileScreenContent(userProfile, onLogout, onNavigateToSettings)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Something went wrong", style = MaterialTheme.typography.titleLarge)
        Text(text = error, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Retry")
        }
    }
}

@Composable
fun MainDashboardContent(
    userProfile: UserProfile?,
    studentData: StudentDashboardDto?,
    recentNotices: List<NoticeDto>,
    attendanceSummary: AttendanceSummaryDto?,
    subjects: List<SubjectDto>,
    assignments: List<AssignmentDto>,
    timetable: List<ClassDto>,
    teacherData: TeacherDashboardDto?,
    parentData: ParentDashboardDto?,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarks: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onRecordAttendance: (Int, String) -> Unit,
    onRecordMarks: (Int, String) -> Unit,
    onNavigateToChildDetails: (Int, String) -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    when (userProfile?.role?.lowercase()) {
        "student" -> {
            if (studentData != null) {
                StudentDashboard(
                    data = studentData,
                    recentNotices = recentNotices,
                    attendanceSummary = attendanceSummary,
                    subjects = subjects,
                    assignments = assignments,
                    todayClasses = timetable,
                    onAttendanceClick = onNavigateToAttendance,
                    onMarksClick = onNavigateToMarks,
                    onAssignmentsClick = onNavigateToAssignments,
                    onNoticesClick = onNavigateToNotices,
                    onFeesClick = onNavigateToFees,
                    onRoutineClick = onNavigateToRoutines,
                    onExamsClick = onNavigateToExams,
                    onResultsClick = onNavigateToResults,
                    onSubjectsClick = onNavigateToSubjects,
                    onTimetableClick = onNavigateToTimetable,
                    onDownloadsClick = onNavigateToDownloads,
                    onProfileClick = onNavigateToProfile
                )
            }
        }
        "teacher" -> {
            if (teacherData != null) {
                TeacherDashboard(
                    data = teacherData,
                    onRecordAttendance = onRecordAttendance,
                    onRecordMarks = onRecordMarks
                )
            }
        }
        "parent" -> {
            if (parentData != null) {
                ParentDashboard(
                    data = parentData,
                    onChildClick = onNavigateToChildDetails
                )
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = onLogout) { Text("Logout") }
            }
        }
    }
}

@Composable
fun SubjectsScreenContent(onNavigateToSubjects: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Book, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Academic Courses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Button(onClick = onNavigateToSubjects, modifier = Modifier.padding(top = 16.dp)) {
            Text("View Full Curriculum")
        }
    }
}

@Composable
fun TimetableScreenContent(onNavigateToTimetable: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Class Timetable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Button(onClick = onNavigateToTimetable, modifier = Modifier.padding(top = 16.dp)) {
            Text("Open Full Timetable")
        }
    }
}

@Composable
fun ProfileScreenContent(userProfile: UserProfile?, onLogout: () -> Unit, onSettingsClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        AsyncImage(
            model = userProfile?.avatarUrl ?: "https://ui-avatars.com/api/?name=${userProfile?.name}&background=random",
            contentDescription = "Profile",
            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(userProfile?.name ?: "User", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(userProfile?.email ?: "", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ListItem(
                    headlineContent = { Text("Settings") },
                    leadingContent = { Icon(Icons.Rounded.Settings, null) },
                    modifier = Modifier.clickable { onSettingsClick() }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Sign Out") },
                    leadingContent = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { onLogout() }
                )
            }
        }
    }
}

@Composable
fun UsersScreenContent(userProfile: UserProfile?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.People, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when(userProfile?.role?.lowercase()) {
                "student" -> "Classmates & Teachers"
                "teacher" -> "My Students"
                else -> "Users Directory"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(text = "Connect with your classmates and teachers.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun DashboardAdaptivePreview() {
    MMPAppTheme {
        DashboardAdaptiveContent(
            userProfile = UserProfile(id = 1, name = "Student User", email = "student@mmp.edu.np", role = "Student"),
            studentData = null, teacherData = null, parentData = null, isLoading = false, onLogout = {}
        )
    }
}
