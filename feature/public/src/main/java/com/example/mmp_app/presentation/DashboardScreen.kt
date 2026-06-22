package com.example.mmp_app.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val studentData by viewModel.studentDashboard.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val teacherData by viewModel.teacherDashboard.collectAsState()
    val parentData by viewModel.parentDashboard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(userProfile) {
        when (userProfile?.role?.lowercase()) {
            "student" -> {
                viewModel.loadStudentDashboard()
                viewModel.loadStudentNotices()
            }
            "teacher" -> viewModel.loadTeacherDashboard()
            "parent" -> viewModel.loadParentDashboard()
        }
    }

    DashboardAdaptiveContent(
        userProfile = userProfile,
        studentData = studentData,
        recentNotices = notices,
        teacherData = teacherData,
        parentData = parentData,
        isLoading = isLoading,
        error = error,
        onLogout = onLogout,
        onRetry = {
            when (userProfile?.role?.lowercase()) {
                "student" -> {
                    viewModel.loadStudentDashboard()
                    viewModel.loadStudentNotices()
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
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun DashboardAdaptiveContent(
    userProfile: UserProfile?,
    studentData: StudentDashboardDto?,
    recentNotices: List<NoticeDto> = emptyList(),
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
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val navSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)

    NavigationSuiteScaffold(
        layoutType = navSuiteType,
        navigationSuiteItems = {
            item(
                selected = selectedItem == 0,
                onClick = { selectedItem = 0 },
                icon = { Icon(Icons.Rounded.Dashboard, contentDescription = null) },
                label = { Text("Overview") }
            )
            item(
                selected = selectedItem == 1,
                onClick = { selectedItem = 1 },
                icon = { Icon(Icons.Rounded.People, contentDescription = null) },
                label = { Text("Users") }
            )
            item(
                selected = selectedItem == 2,
                onClick = { onLogout() },
                icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                label = { Text("Logout") }
            )
        }
    ) {
        when (selectedItem) {
            0 -> DashboardContent(
                userProfile = userProfile,
                studentData = studentData,
                recentNotices = recentNotices,
                teacherData = teacherData,
                parentData = parentData,
                isLoading = isLoading,
                error = error,
                onLogout = onLogout,
                onRetry = onRetry,
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
                onNavigateToSettings = onNavigateToSettings
            )
            1 -> UsersScreenContent(userProfile = userProfile)
            else -> DashboardContent(
                userProfile = userProfile,
                studentData = studentData,
                teacherData = teacherData,
                parentData = parentData,
                isLoading = isLoading,
                error = error,
                onLogout = onLogout,
                onRetry = onRetry
            )
        }
    }
}

@Composable
fun UsersScreenContent(userProfile: UserProfile?) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Users") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.People, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
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
            Text(
                text = "Connect with your classmates and teachers.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    userProfile: UserProfile?,
    studentData: StudentDashboardDto?,
    recentNotices: List<NoticeDto> = emptyList(),
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
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mmplogo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
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
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Rounded.Dashboard, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                if (userProfile?.role?.lowercase() == "student") {
                    NavigationDrawerItem(
                        label = { Text("Attendance") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToAttendance()
                        },
                        icon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Marks / Results") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToResults()
                        },
                        icon = { Icon(Icons.Rounded.Star, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Subjects") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToSubjects()
                        },
                        icon = { Icon(Icons.Rounded.Book, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Assignments") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToAssignments()
                        },
                        icon = { Icon(Icons.Rounded.Assignment, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Timetable") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToTimetable()
                        },
                        icon = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Study Materials") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToDownloads()
                        },
                        icon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Notices / News") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToNotices()
                        },
                        icon = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    NavigationDrawerItem(
                        label = { Text("Profile") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToProfile()
                        },
                        icon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToSettings()
                        },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Attendance") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToAttendance()
                        },
                        icon = { Icon(Icons.Rounded.DateRange, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Marks & Results") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            onNavigateToMarks()
                        },
                        icon = { Icon(Icons.Rounded.Star, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.mmplogo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "MMP College", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = userProfile?.role ?: "Dashboard",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToNotices) {
                            Icon(imageVector = Icons.Rounded.Notifications, contentDescription = null)
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            AsyncImage(
                                model = (if (userProfile?.role?.lowercase() == "student") studentData?.avatarUrl else null) 
                                    ?: userProfile?.avatarUrl 
                                    ?: "https://ui-avatars.com/api/?name=${userProfile?.name}&background=random",
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                )
            }
        ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (isLoading && studentData == null && teacherData == null && parentData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null && studentData == null && teacherData == null && parentData == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Something went wrong", style = MaterialTheme.typography.titleLarge)
                    Text(text = error!!, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Retry")
                    }
                }
            } else {
                when (userProfile?.role?.lowercase()) {
                    "student" -> {
                        if (studentData != null) {
                            StudentDashboard(
                                data = studentData,
                                recentNotices = recentNotices,
                                todayClasses = emptyList(), // Replace with real data if available
                                onAttendanceClick = onNavigateToAttendance,
                                onMarksClick = onNavigateToMarks,
                                onAssignmentsClick = onNavigateToAssignments,
                                onNoticesClick = onNavigateToNotices,
                                onFeesClick = onNavigateToFees,
                                onRoutineClick = onNavigateToRoutines,
                                onExamsClick = onNavigateToExams,
                                onResultsClick = onNavigateToResults
                            )
                        } else {
                            // Empty state if role is student but data is null
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No student data found.")
                            }
                        }
                    }
                    "teacher" -> {
                        if (teacherData != null) {
                            TeacherDashboard(
                                data = teacherData,
                                onRecordAttendance = onRecordAttendance,
                                onRecordMarks = onRecordMarks
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No teacher data found.")
                            }
                        }
                    }
                    "parent" -> {
                        if (parentData != null) {
                            ParentDashboard(
                                data = parentData,
                                onChildClick = onNavigateToChildDetails
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No parent data found.")
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Dashboard for ${userProfile?.role} is coming soon!")
                            Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun DashboardAdaptivePreview() {
    MMPAppTheme {
        DashboardAdaptiveContent(
            userProfile = UserProfile(
                id = 1,
                name = "Student User",
                email = "student@mmp.edu.np",
                role = "Student"
            ),
            studentData = null,
            teacherData = null,
            parentData = null,
            isLoading = false,
            onLogout = {}
        )
    }
}
