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
import com.example.mmp_app.core.presentation.ThemeViewModel
import com.example.mmp_app.core.ui.ModernBottomNavBar
import com.example.mmp_app.core.ui.ModernNavItem
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.core.ui.StudentDashboard

import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.model.ParentNoticeDto
import com.example.mmp_app.feature.teacher.ui.TeacherDashboard
import com.example.mmp_app.feature.parent.ui.ParentDashboard
import com.example.mmp_app.feature.parent.ui.ParentNoticesScreen
import com.example.mmp_app.feature.parent.ui.ParentProfileScreen
import com.example.mmp_app.feature.parent.ui.ChildMarksScreen
import com.example.mmp_app.feature.parent.ui.ChildrenListScreen
import com.example.mmp_app.feature.student.ui.MarksScreen
import com.example.mmp_app.feature.student.ui.NoticesScreen
import com.example.mmp_app.feature.student.ui.StudentProfileScreen
import com.example.mmp_app.feature.student.ui.NotificationViewModel
import com.example.mmp_app.feature.student.ui.TimetableScreen
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
    onNavigateToChildAttendance: (Int) -> Unit = {},
    onNavigateToChildAssignments: (Int) -> Unit = {},
    onNavigateToChildResults: (Int) -> Unit = {},
    onNavigateToChildrenList: () -> Unit = {},
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val notificationState by notificationViewModel.uiState.collectAsState()
    
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    
    val studentData by viewModel.studentDashboard.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val attendanceSummary by viewModel.attendanceSummary.collectAsState()
    val subjects = viewModel.subjects.collectAsState().value
    val assignments = viewModel.assignments.collectAsState().value
    val timetable = viewModel.timetable.collectAsState().value
    val downloads = viewModel.downloads.collectAsState().value
    val userProfileState by viewModel.userProfile.collectAsState()
    val teacherData by viewModel.teacherDashboard.collectAsState()
    val parentData by viewModel.parentDashboard.collectAsState()
    val parentProfile by viewModel.parentProfile.collectAsState()
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
        userProfile = userProfileState ?: userProfile,
        studentData = studentData,
        recentNotices = notices,
        attendanceSummary = attendanceSummary,
        subjects = subjects,
        assignments = assignments,
        timetable = timetable,
        downloads = downloads,
        teacherData = teacherData,
        parentData = parentData,
        parentProfile = parentProfile,
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
        onNavigateToChildAttendance = onNavigateToChildAttendance,
        onNavigateToChildAssignments = onNavigateToChildAssignments,
        onNavigateToChildResults = onNavigateToChildResults,
        onNavigateToChildrenList = {
            if (parentData != null && parentData!!.children.size == 1) {
                val child = parentData!!.children[0]
                onNavigateToChildDetails(child.id, child.name)
            } else {
                onNavigateToChildrenList()
            }
        },
        onNavigateToRoutines = onNavigateToRoutines,
        onNavigateToExams = onNavigateToExams,
        onNavigateToResults = onNavigateToResults,
        onNavigateToSubjects = onNavigateToSubjects,
        onNavigateToTimetable = onNavigateToTimetable,
        onNavigateToDownloads = onNavigateToDownloads,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToNotifications = onNavigateToNotifications,
        unreadCount = notificationState.unreadCount,
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
    timetable: List<TimetableClass> = emptyList(),
    downloads: List<SubjectDocument> = emptyList(),
    teacherData: TeacherDashboardDto?,
    parentData: ParentDashboardDto?,
    parentProfile: ParentProfileDto? = null,
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
    onNavigateToChildAttendance: (Int) -> Unit = {},
    onNavigateToChildAssignments: (Int) -> Unit = {},
    onNavigateToChildResults: (Int) -> Unit = {},
    onNavigateToChildrenList: () -> Unit = {},
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToResults: () -> Unit = {},
    onNavigateToSubjects: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    unreadCount: Int = 0,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
) {
    MMPAppTheme(darkTheme = isDarkTheme) {
        var selectedItem by remember { mutableIntStateOf(0) }

        if (isLoading && studentData == null && teacherData == null && parentData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null && studentData == null && teacherData == null && parentData == null) {
            ErrorState(error!!, onRetry)
        } else if (userProfile?.role?.lowercase() == "student" && selectedItem == 0) {
            MainDashboardContent(
                userProfile = userProfile,
                studentData = studentData,
                recentNotices = recentNotices,
                attendanceSummary = attendanceSummary,
                subjects = subjects,
                assignments = assignments,
                timetable = timetable,
                downloads = downloads,
                teacherData = teacherData,
                parentData = parentData,
                parentProfile = parentProfile,
                onNavigateToAttendance = onNavigateToAttendance,
                onNavigateToMarks = onNavigateToMarks,
                onNavigateToAssignments = onNavigateToAssignments,
                onNavigateToFees = onNavigateToFees,
                onNavigateToNotices = onNavigateToNotices,
                onRecordAttendance = onRecordAttendance,
                onRecordMarks = onRecordMarks,
                onNavigateToChildDetails = onNavigateToChildDetails,
                onNavigateToChildAttendance = onNavigateToChildAttendance,
                onNavigateToChildAssignments = onNavigateToChildAssignments,
                onNavigateToChildResults = onNavigateToChildResults,
                onNavigateToChildrenList = {
            if (parentData != null && parentData!!.children.size == 1) {
                val child = parentData!!.children[0]
                onNavigateToChildDetails(child.id, child.name)
            } else {
                onNavigateToChildrenList()
            }
        },
                onNavigateToRoutines = onNavigateToRoutines,
                onNavigateToExams = onNavigateToExams,
                onNavigateToResults = onNavigateToResults,
                onNavigateToSubjects = onNavigateToSubjects,
                onNavigateToTimetable = onNavigateToTimetable,
                onNavigateToDownloads = onNavigateToDownloads,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToNotifications = onNavigateToNotifications,
                onLogout = onLogout,
                onSelectItem = { selectedItem = it },
                unreadCount = unreadCount,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                showSystemHeader = true
            )
        } else {
            // Use the standard adaptive layout for all screens to ensure nav bar is always present
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
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                val avatarUrl = if (userProfile?.role?.lowercase() == "parent") parentProfile?.avatarUrl else userProfile?.avatarUrl
                                val name = if (userProfile?.role?.lowercase() == "parent") parentProfile?.name ?: userProfile?.name else userProfile?.name
                                
                                if (!avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val firstLetter = name?.firstOrNull()?.toString() ?: "U"
                                        Text(
                                            text = firstLetter,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (if (userProfile?.role?.lowercase() == "parent") parentProfile?.name ?: userProfile?.name else userProfile?.name) ?: "Guest User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = userProfile?.role?.replaceFirstChar { it.uppercase() } ?: "User",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        NavigationDrawerItem(
                            label = { Text("Dashboard") },
                            selected = selectedItem == 0,
                            onClick = { 
                                selectedItem = 0
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(Icons.Rounded.Home, null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        if (userProfile?.role?.lowercase() == "parent") {
                            NavigationDrawerItem(
                                label = { Text("My Children") },
                                selected = selectedItem == 4,
                                onClick = {
                                    selectedItem = 4
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.People, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Notices") },
                                selected = selectedItem == 1,
                                onClick = {
                                    selectedItem = 1
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.Notifications, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Results") },
                                selected = selectedItem == 3,
                                onClick = {
                                    selectedItem = 3
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.Star, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Profile") },
                                selected = selectedItem == 5,
                                onClick = {
                                    selectedItem = 5
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.Person, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Settings") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToSettings()
                                },
                                icon = { Icon(Icons.Rounded.Settings, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }

                        if (userProfile?.role?.lowercase() == "student") {
                            NavigationDrawerItem(
                                label = { Text("Profile") },
                                selected = selectedItem == 4,
                                onClick = {
                                    selectedItem = 4
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.Person, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Academic Results") },
                                selected = selectedItem == 3,
                                onClick = {
                                    selectedItem = 3
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.Star, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Timetable") },
                                selected = selectedItem == 2,
                                onClick = {
                                    selectedItem = 2
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.EventNote, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        
                        if (userProfile?.role?.lowercase() == "teacher") {
                            NavigationDrawerItem(
                                label = { Text("Students") },
                                selected = selectedItem == 1,
                                onClick = {
                                    selectedItem = 1
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.Groups, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                            NavigationDrawerItem(
                                label = { Text("Schedule") },
                                selected = selectedItem == 2,
                                onClick = {
                                    selectedItem = 2
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(Icons.Rounded.CalendarMonth, null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                        
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
                                IconButton(onClick = onNavigateToNotifications) {
                                    BadgedBox(badge = {
                                        if (unreadCount > 0) {
                                            Badge { Text(unreadCount.toString()) }
                                        }
                                    }) {
                                        Icon(Icons.Rounded.NotificationsNone, null)
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (navSuiteType == NavigationSuiteType.NavigationBar) {
                            if (userProfile?.role?.lowercase() == "parent") {
                                ModernBottomNavBar(
                                    selectedItem = selectedItem,
                                    onItemSelected = { selectedItem = it },
                                    items = listOf(
                                        ModernNavItem(Icons.Rounded.Notifications, "Notices"),
                                        ModernNavItem(Icons.Rounded.EventNote, "Schedule"),
                                        ModernNavItem(Icons.Rounded.Star, "Results"),
                                        ModernNavItem(Icons.Rounded.People, "Children")
                                    ),
                                    primaryColor = if (userProfile?.role?.lowercase() == "student") Color(0xFF2563EB) else Color(0xFF6366F1),
                                    secondaryColor = if (userProfile?.role?.lowercase() == "student") Color(0xFF60A5FA) else Color(0xFFA855F7),
                                    cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                                    textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                                )
                            } else {
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
                                                icon = { Icon(Icons.Rounded.Notifications, null) },
                                                label = { Text("Notices") }
                                            )
                                            NavigationBarItem(
                                                selected = selectedItem == 2,
                                                onClick = { selectedItem = 2 },
                                                icon = { Icon(Icons.Rounded.EventNote, null) },
                                                label = { Text("Schedule") }
                                            )
                                            NavigationBarItem(
                                                selected = selectedItem == 3,
                                                onClick = { selectedItem = 3 },
                                                icon = { Icon(Icons.Rounded.Star, null) },
                                                label = { Text("Results") }
                                            )
                                            NavigationBarItem(
                                                selected = selectedItem == 4,
                                                onClick = { selectedItem = 4 },
                                                icon = { Icon(Icons.Rounded.AccountCircle, null) },
                                                label = { Text("Profile") }
                                            )
                                        } else {
                                            NavigationBarItem(
                                                selected = selectedItem == 1,
                                                onClick = { selectedItem = 1 },
                                                icon = { Icon(Icons.Rounded.Groups, null) },
                                                label = { Text("Users") }
                                            )
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
                                if (userProfile?.role?.lowercase() == "parent") {
                                    NavigationRailItem(
                                        selected = selectedItem == 1,
                                        onClick = { selectedItem = 1 },
                                        icon = { Icon(Icons.Rounded.Notifications, null) },
                                        label = { Text("Notices") }
                                    )
                                    NavigationRailItem(
                                        selected = selectedItem == 2,
                                        onClick = { selectedItem = 2 },
                                        icon = { Icon(Icons.Rounded.EventNote, null) },
                                        label = { Text("Schedule") }
                                    )
                                    NavigationRailItem(
                                        selected = selectedItem == 3,
                                        onClick = { selectedItem = 3 },
                                        icon = { Icon(Icons.Rounded.AccountCircle, null) },
                                        label = { Text("Profile") }
                                    )
                                }
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
                                        userProfile = userProfile,
                                        studentData = studentData,
                                        recentNotices = recentNotices,
                                        attendanceSummary = attendanceSummary,
                                        subjects = subjects,
                                        assignments = assignments,
                                        timetable = timetable,
                                        downloads = downloads,
                                        teacherData = teacherData,
                                        parentData = parentData,
                                        parentProfile = parentProfile,
                                        onNavigateToAttendance = onNavigateToAttendance,
                                        onNavigateToMarks = onNavigateToMarks,
                                        onNavigateToAssignments = onNavigateToAssignments,
                                        onNavigateToFees = onNavigateToFees,
                                        onNavigateToNotices = onNavigateToNotices,
                                        onRecordAttendance = onRecordAttendance,
                                        onRecordMarks = onRecordMarks,
                                        onNavigateToChildDetails = onNavigateToChildDetails,
                                        onNavigateToChildAttendance = onNavigateToChildAttendance,
                                        onNavigateToChildAssignments = onNavigateToChildAssignments,
                                        onNavigateToChildResults = onNavigateToChildResults,
                                        onNavigateToChildrenList = {
            if (parentData != null && parentData!!.children.size == 1) {
                val child = parentData!!.children[0]
                onNavigateToChildDetails(child.id, child.name)
            } else {
                onNavigateToChildrenList()
            }
        },
                                        onNavigateToRoutines = onNavigateToRoutines,
                                        onNavigateToExams = onNavigateToExams,
                                        onNavigateToResults = onNavigateToResults,
                                        onNavigateToSubjects = onNavigateToSubjects,
                                        onNavigateToTimetable = onNavigateToTimetable,
                                        onNavigateToDownloads = onNavigateToDownloads,
                                        onNavigateToProfile = onNavigateToProfile,
                                        onNavigateToSettings = onNavigateToSettings,
                                        onNavigateToNotifications = onNavigateToNotifications,
                                        onLogout = onLogout,
                                        onSelectItem = { selectedItem = it },
                                        unreadCount = unreadCount,
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = onToggleTheme,
                                        showSystemHeader = false
                                    )
                                    1 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ParentNoticesScreen(
                                            onMenuClick = { scope.launch { drawerState.open() } },
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = false
                                        )
                                    } else if (userProfile?.role?.lowercase() == "student") {
                                        NoticesScreen(onBack = { selectedItem = 0 })
                                    } else {
                                        UsersScreenContent(userProfile)
                                    }
                                    2 -> TimetableScreen(
                                        onMenuClick = { scope.launch { drawerState.open() } },
                                        showSystemHeader = false
                                    )
                                    3 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ChildMarksScreen(
                                            childId = 0,
                                            onBack = { selectedItem = 0 },
                                            isDarkTheme = isDarkTheme
                                        )
                                    } else if (userProfile?.role?.lowercase() == "student") {
                                        MarksScreen(onBack = { selectedItem = 0 })
                                    } else {
                                        ProfileScreenContent(userProfile, onLogout, onNavigateToSettings)
                                    }
                                    4 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ChildrenListScreen(
                                            onBack = { selectedItem = 0 },
                                            onNavigateToChildDetail = { id -> onNavigateToChildDetails(id, "") },
                                            isDarkTheme = isDarkTheme
                                        )
                                    } else if (userProfile?.role?.lowercase() == "student") {
                                        StudentProfileScreen(
                                            onBack = { selectedItem = 0 },
                                            onLogout = onLogout,
                                            onEditProfile = onNavigateToSettings,
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
                                    5 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ParentProfileScreen(
                                            onBack = { selectedItem = 0 },
                                            onLogout = onLogout,
                                            isDarkTheme = isDarkTheme
                                        )
                                    }
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
    timetable: List<TimetableClass>,
    downloads: List<SubjectDocument>,
    teacherData: TeacherDashboardDto?,
    parentData: ParentDashboardDto?,
    parentProfile: ParentProfileDto?,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarks: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToNotices: () -> Unit,
    onRecordAttendance: (Int, String) -> Unit,
    onRecordMarks: (Int, String) -> Unit,
    onNavigateToChildDetails: (Int, String) -> Unit,
    onNavigateToChildAttendance: (Int) -> Unit,
    onNavigateToChildAssignments: (Int) -> Unit,
    onNavigateToChildResults: (Int) -> Unit,
    onNavigateToChildrenList: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    onSelectItem: (Int) -> Unit = {},
    unreadCount: Int = 0,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true
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
                    onMarksClick = { onSelectItem(3) },
                    onAssignmentsClick = onNavigateToAssignments,
                    onNoticesClick = { onSelectItem(1) },
                    onFeesClick = onNavigateToFees,
                    onRoutineClick = onNavigateToRoutines,
                    onExamsClick = onNavigateToExams,
                    onResultsClick = { onSelectItem(3) },
                    onSubjectsClick = onNavigateToSubjects,
                    onTimetableClick = { onSelectItem(2) },
                    onDownloadsClick = onNavigateToDownloads,
                    onProfileClick = { onSelectItem(4) },
                    onSettingsClick = onNavigateToSettings,
                    onNotificationsClick = onNavigateToNotifications,
                    onLogoutClick = onLogout,
                    unreadCount = unreadCount,
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
                    recentNotices = recentNotices.map { notice ->
                        ParentNoticeDto(
                            id = notice.id,
                            title = notice.title,
                            type = notice.type ?: "general",
                            publishedAt = notice.publishedAt,
                            content = notice.content
                        )
                    },
                    onChildClick = onNavigateToChildDetails,
                    onAttendanceClick = { id, _ -> onNavigateToChildAttendance(id) },
                    onMarksClick = { id, _ -> onSelectItem(3) },
                    onAssignmentsClick = { id, _ -> onNavigateToChildAssignments(id) },
                    onTimetableClick = { id, _ -> onSelectItem(2) },
                    onNoticesClick = { onSelectItem(1) },
                    onProfileClick = { onSelectItem(5) },
                    onSettingsClick = onNavigateToSettings,
                    onChildrenListClick = { onSelectItem(4) },
                    onLogoutClick = onLogout,
                    onToggleTheme = onToggleTheme,
                    isDarkTheme = isDarkTheme,
                    showSystemHeader = showSystemHeader,
                    parentAvatarUrl = userProfile?.avatarUrl
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
