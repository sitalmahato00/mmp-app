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
    val subjects = viewModel.subjects.collectAsState().value
    val assignments = viewModel.assignments.collectAsState().value
    val timetable = viewModel.timetable.collectAsState().value
    val downloads = viewModel.downloads.collectAsState().value
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
        downloads = downloads,
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
    downloads: List<SubjectDocument> = emptyList(),
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
    MMPAppTheme(darkTheme = isDarkTheme) {
        var selectedItem by remember { mutableIntStateOf(0) }
        val isStudent = userProfile?.role?.lowercase() == "student"

        if (isStudent && selectedItem == 0) {
            MainDashboardContent(
                userProfile, studentData, recentNotices, attendanceSummary, 
                subjects, assignments, timetable, downloads, teacherData, parentData,
                onNavigateToAttendance, onNavigateToMarks, onNavigateToAssignments,
                onNavigateToFees, onNavigateToNotices, onRecordAttendance,
                onRecordMarks, onNavigateToChildDetails, onNavigateToRoutines,
                onNavigateToExams, onNavigateToResults, onNavigateToSubjects,
                onNavigateToTimetable, onNavigateToDownloads, onNavigateToProfile,
                onLogout, isDarkTheme, onToggleTheme
            )
        } else {
            // Otherwise, use the standard adaptive layout
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
                            Surface(modifier = Modifier.size(64.dp), shape = CircleShape) {
                                if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = userProfile?.avatarUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.mmplogo),
                                        contentDescription = "College Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
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
                                        subjects, assignments, timetable, downloads, teacherData, parentData,
                                        onNavigateToAttendance, onNavigateToMarks, onNavigateToAssignments,
                                        onNavigateToFees, onNavigateToNotices, onRecordAttendance,
                                        onRecordMarks, onNavigateToChildDetails, onNavigateToRoutines,
                                        onNavigateToExams, onNavigateToResults, onNavigateToSubjects,
                                        onNavigateToTimetable, onNavigateToDownloads, onNavigateToProfile,
                                        onLogout, isDarkTheme, onToggleTheme
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
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
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
    downloads: List<SubjectDocument>,
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
    onLogout: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
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
                    materialCount = downloads.size,
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
                    onProfileClick = onNavigateToProfile,
                    onLogoutClick = onLogout,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
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
                Text("Dashboard for ${userProfile?.role ?: "Guest"}")
            }
        }
    }
}

@Composable
fun SubjectsScreenContent(onNavigateToSubjects: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Subjects Screen")
            Button(onClick = onNavigateToSubjects) {
                Text("View Detailed Subjects")
            }
        }
    }
}

@Composable
fun TimetableScreenContent(onNavigateToTimetable: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Timetable Screen")
            Button(onClick = onNavigateToTimetable) {
                Text("View Detailed Timetable")
            }
        }
    }
}

@Composable
fun ProfileScreenContent(userProfile: UserProfile?, onLogout: () -> Unit, onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            if (!userProfile?.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = userProfile?.avatarUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Rounded.Person,
                    null,
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = userProfile?.name ?: "Guest", style = MaterialTheme.typography.headlineMedium)
        Text(text = userProfile?.email ?: "", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Role: ${userProfile?.role?.uppercase()}", style = MaterialTheme.typography.labelLarge)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Settings, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun UsersScreenContent(userProfile: UserProfile?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("User Management Screen (Role: ${userProfile?.role})")
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardAdaptivePreview() {
    MMPAppTheme {
        DashboardAdaptiveContent(
            userProfile = UserProfile(1, "John Student", "john@example.com", "student"),
            studentData = null,
            teacherData = null,
            parentData = null,
            isLoading = false,
            onLogout = {}
        )
    }
}
