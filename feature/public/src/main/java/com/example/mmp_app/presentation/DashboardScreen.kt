package com.example.mmp_app.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.EventNote
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
import com.example.mmp_app.core.ui.StudentTopBar
import com.example.mmp_app.core.ui.StudentBottomNavBar
import com.example.mmp_app.core.ui.StudentDrawerContent

import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.model.ParentNoticeDto
import com.example.mmp_app.feature.teacher.ui.TeacherDashboard
import com.example.mmp_app.feature.parent.ui.ParentDashboard
import com.example.mmp_app.feature.parent.ui.ParentNoticesScreen
import com.example.mmp_app.feature.parent.ui.ParentProfileScreen
import com.example.mmp_app.feature.parent.ui.ChildMarksScreen
import com.example.mmp_app.feature.parent.ui.ChildTimetableScreen
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
    onNavigateToCreateAssignment: () -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onViewSubmissions: (Int) -> Unit = {},
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
    val teacherProfile by viewModel.teacherProfile.collectAsState()
    val teacherSchedule by viewModel.teacherSchedule.collectAsState()
    val teacherClasses by viewModel.teacherClasses.collectAsState()
    val parentData by viewModel.parentDashboard.collectAsState()
    val parentProfile by viewModel.parentProfile.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
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
        teacherProfile = teacherProfile,
        teacherSchedule = teacherSchedule,
        teacherClasses = teacherClasses,
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
        selectedChildId = selectedChildId,
        onSelectChild = { viewModel.selectChild(it) },
        isDarkTheme = isDarkTheme,
        onToggleTheme = { themeViewModel.toggleTheme() },
        onNavigateToCreateAssignment = onNavigateToCreateAssignment,
        onNavigateToEditAssignment = onNavigateToEditAssignment,
        onViewSubmissions = onViewSubmissions
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
    teacherProfile: TeacherProfileDto? = null,
    teacherSchedule: TodayScheduleDto? = null,
    teacherClasses: List<TeacherSubjectDto> = emptyList(),
    parentData: ParentDashboardDto?,
    parentProfile: ParentProfileDto? = null,
    isLoading: Boolean,
    error: String? = null,
    onLogout: () -> Unit,
    onRetry: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToMarks: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
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
    selectedChildId: Int = 0,
    onSelectChild: (Int) -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateToCreateAssignment: () -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onViewSubmissions: (Int) -> Unit = {},
) {
    MMPAppTheme(darkTheme = isDarkTheme) {
        var selectedItem by remember { mutableIntStateOf(0) }

        val isTopLevelDestination = selectedItem == 0

        val role = userProfile?.role?.lowercase()
        val isDataLoaded = when (role) {
            "student" -> studentData != null
            "teacher" -> teacherData != null
            "parent" -> parentData != null
            else -> false
        }

        if (isLoading && !isDataLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null && !isDataLoaded) {
            ErrorState(error!!, onRetry)
        } else {
            // Use the standard adaptive layout for all screens to ensure nav bar is always present
            val adaptiveInfo = currentWindowAdaptiveInfo()
            val navSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    if (userProfile?.role?.lowercase() == "student") {
                        StudentDrawerContent(
                            primaryColor = Color(0xFF2563EB),
                            cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                            onCloseDrawer = { scope.launch { drawerState.close() } },
                            onProfileClick = { selectedItem = 4 },
                            onSubjectsClick = { onNavigateToSubjects() },
                            onAttendanceClick = { onNavigateToAttendance() },
                            onResultsClick = { selectedItem = 3 },
                            onAssignmentsClick = { onNavigateToAssignments() },
                            onTimetableClick = { selectedItem = 2 },
                            onDownloadsClick = { onNavigateToDownloads() },
                            onNoticesClick = { selectedItem = 1 },
                            onNotificationsClick = { onNavigateToNotifications() },
                            onSettingsClick = { onNavigateToSettings() },
                            onLogoutClick = onLogout
                        )
                    } else if (userProfile?.role?.lowercase() == "teacher") {
                        com.example.mmp_app.feature.teacher.ui.TeacherDrawerContent(
                            profile = teacherProfile,
                            primaryColor = Color(0xFF1565C0),
                            cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                            onCloseDrawer = { scope.launch { drawerState.close() } },
                            onDashboardClick = { 
                                selectedItem = 0
                                scope.launch { drawerState.close() }
                            },
                            onScheduleClick = { selectedItem = 1 },
                            onClassesClick = { selectedItem = 5 },
                            onAttendanceClick = { selectedItem = 5 },
                            onMarksClick = { selectedItem = 5 },
                            onAssignmentsClick = { selectedItem = 2 },
                            onStudentsClick = { 
                                selectedItem = 6
                                scope.launch { drawerState.close() }
                            },
                            onTimetableClick = { /* TODO */ },
                            onReportsClick = { /* TODO */ },
                            onNotificationsClick = { onNavigateToNotifications() },
                            onProfileClick = { selectedItem = 4 },
                            onLogoutClick = onLogout,
                            unreadCount = unreadCount
                        )
                    } else {
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
                }
            ) {
                Scaffold(
                    containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        if (userProfile?.role?.lowercase() == "student") {
                            if (isTopLevelDestination) {
                                StudentTopBar(
                                    textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                                    cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                                    primaryColor = Color(0xFF2563EB),
                                    isDarkTheme = isDarkTheme,
                                    unreadCount = unreadCount,
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onToggleTheme = onToggleTheme,
                                    onNotificationsClick = onNavigateToNotifications
                                )
                            } else {
                                null
                            }
                        } else if (isTopLevelDestination) {
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
                        } else {
                            null
                        }
                    },
                    bottomBar = {
                        if (navSuiteType == NavigationSuiteType.NavigationBar) {
                            if (userProfile?.role?.lowercase() == "student") {
                                StudentBottomNavBar(
                                    primaryColor = Color(0xFF2563EB),
                                    secondaryColor = Color(0xFF60A5FA),
                                    cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                                    textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                                    onIdCardClick = { /* TODO: Route or Dialog */ },
                                    onDownloadsClick = onNavigateToDownloads,
                                    onNoticesClick = { selectedItem = 1 },
                                    onProfileClick = { selectedItem = 4 },
                                    onHomeClick = { selectedItem = 0 },
                                    selectedItemIndex = selectedItem
                                )
                            } else if (userProfile?.role?.lowercase() == "parent") {
                                ModernBottomNavBar(
                                    selectedItem = when(selectedItem) {
                                        0 -> 0 // Dashboard
                                        1 -> 1 // Notices
                                        2 -> 2 // Schedule
                                        3 -> 3 // Results
                                        4 -> 4 // Children
                                        else -> -1
                                    },
                                    onItemSelected = { index ->
                                        selectedItem = index
                                    },
                                    items = listOf(
                                        ModernNavItem(Icons.Rounded.Notifications, "Notices"),
                                        ModernNavItem(Icons.AutoMirrored.Rounded.EventNote, "Schedule"),
                                        ModernNavItem(Icons.Rounded.Star, "Results"),
                                        ModernNavItem(Icons.Rounded.People, "Children")
                                    ),
                                    primaryColor = if (userProfile?.role?.lowercase() == "student") Color(0xFF2563EB) else Color(0xFF6366F1),
                                    secondaryColor = if (userProfile?.role?.lowercase() == "student") Color(0xFF60A5FA) else Color(0xFFA855F7),
                                    cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                                    textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                                )
                            } else if (userProfile?.role?.lowercase() == "teacher") {
                                com.example.mmp_app.feature.teacher.ui.TeacherBottomNavBar(
                                    primaryColor = Color(0xFF1565C0),
                                    secondaryColor = Color(0xFF42A5F5),
                                    cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White,
                                    textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B),
                                    onScheduleClick = { selectedItem = 1 },
                                    onAssignmentsClick = { selectedItem = 2 },
                                    onHomeClick = { selectedItem = 0 },
                                    onNoticesClick = { selectedItem = 3 },
                                    onProfileClick = { selectedItem = 4 },
                                    selectedItemIndex = selectedItem
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
                                        icon = { Icon(Icons.AutoMirrored.Rounded.EventNote, null) },
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
                                        teacherProfile = teacherProfile,
                                        teacherSchedule = teacherSchedule,
                                        teacherClasses = teacherClasses,
                                        parentData = parentData,
                                        parentProfile = parentProfile,
                                        onNavigateToAttendance = onNavigateToAttendance,
                                        onNavigateToMarks = onNavigateToMarks,
                                        onNavigateToAssignments = onNavigateToAssignments,
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
                                        onNavigateToCreateAssignment = onNavigateToCreateAssignment,
                                        onNavigateToEditAssignment = onNavigateToEditAssignment,
                                        onViewSubmissions = onViewSubmissions,
                                        onLogout = onLogout,
                                        onSelectItem = { selectedItem = it },
                                        selectedChildId = selectedChildId,
                                        onSelectChild = onSelectChild,
                                        unreadCount = unreadCount,
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = onToggleTheme,
                                        showSystemHeader = false
                                    )
                                    1 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ParentNoticesScreen(
                                            onBack = { selectedItem = 0 },
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "student") {
                                        NoticesScreen(
                                            onBack = { selectedItem = 0 },
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "teacher") {
                                        com.example.mmp_app.feature.teacher.ui.TodayScheduleScreen(
                                            onBack = { selectedItem = 0 },
                                            onNavigateToTimetable = { /* TODO */ },
                                            onNavigateToClasses = { selectedItem = 5 },
                                            showSystemHeader = true,
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme
                                        )
                                    } else {
                                        UsersScreenContent(userProfile)
                                    }
                                    2 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ChildTimetableScreen(
                                            childId = selectedChildId,
                                            onBack = { selectedItem = 0 },
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "teacher") {
                                        com.example.mmp_app.feature.teacher.ui.TeacherAssignmentsScreen(
                                            onBack = { selectedItem = 0 },
                                            showSystemHeader = true,
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme,
                                            onNavigateToCreate = onNavigateToCreateAssignment,
                                            onNavigateToEdit = onNavigateToEditAssignment,
                                            onViewSubmissions = onViewSubmissions
                                        )
                                    } else {
                                        TimetableScreen(
                                            onBack = { selectedItem = 0 },
                                            onMenuClick = { scope.launch { drawerState.open() } },
                                            showSystemHeader = true
                                        )
                                    }
                                    3 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ChildMarksScreen(
                                            childId = selectedChildId,
                                            onBack = { selectedItem = 0 },
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "student") {
                                        MarksScreen(onBack = { selectedItem = 0 }, showSystemHeader = true)
                                    } else if (userProfile?.role?.lowercase() == "teacher") {
                                        NoticesScreen(onBack = { selectedItem = 0 }, showSystemHeader = true)
                                    } else {
                                        ProfileScreenContent(userProfile, onLogout, onNavigateToSettings)
                                    }
                                    4 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ChildrenListScreen(
                                            onBack = { selectedItem = 0 },
                                            onNavigateToChildDetail = { id -> onNavigateToChildDetails(id, "") },
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "student") {
                                        StudentProfileScreen(
                                            onBack = { selectedItem = 0 },
                                            onLogout = onLogout,
                                            onEditProfile = onNavigateToSettings,
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "teacher") {
                                        com.example.mmp_app.feature.teacher.ui.TeacherProfileScreen(
                                            onLogout = onLogout,
                                            onEditProfile = onNavigateToSettings,
                                            onBack = { selectedItem = 0 },
                                            teacherData = teacherData,
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme
                                        )
                                    }
                                    5 -> if (userProfile?.role?.lowercase() == "parent") {
                                        ParentProfileScreen(
                                            onBack = { selectedItem = 0 },
                                            onLogout = onLogout,
                                            isDarkTheme = isDarkTheme,
                                            showSystemHeader = true
                                        )
                                    } else if (userProfile?.role?.lowercase() == "teacher") {
                                        com.example.mmp_app.feature.teacher.ui.TeacherClassesScreen(
                                            onBack = { selectedItem = 0 },
                                            onNavigateToAttendance = onRecordAttendance,
                                            onNavigateToMarks = onRecordMarks,
                                            showSystemHeader = true,
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme
                                        )
                                    }
                                    6 -> if (userProfile?.role?.lowercase() == "teacher") {
                                        com.example.mmp_app.feature.teacher.ui.StudentsScreen(
                                            onBack = { selectedItem = 0 },
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme
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
    teacherProfile: TeacherProfileDto? = null,
    teacherSchedule: TodayScheduleDto? = null,
    teacherClasses: List<TeacherSubjectDto> = emptyList(),
    parentData: ParentDashboardDto?,
    parentProfile: ParentProfileDto?,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMarks: () -> Unit,
    onNavigateToAssignments: () -> Unit,
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
    selectedChildId: Int = 0,
    onSelectChild: (Int) -> Unit = {},
    unreadCount: Int = 0,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    showSystemHeader: Boolean = true,
    onNavigateToCreateAssignment: () -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onViewSubmissions: (Int) -> Unit = {},
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
                    onToggleTheme = onToggleTheme,
                    showSystemHeader = showSystemHeader
                )
            }
        }
        "teacher" -> {
            if (teacherData != null) {
                TeacherDashboard(
                    data = teacherData,
                    profile = teacherProfile,
                    schedule = teacherSchedule,
                    classes = teacherClasses,
                    onRecordAttendance = onRecordAttendance,
                    onRecordMarks = onRecordMarks,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onNotificationsClick = onNavigateToNotifications,
                    unreadCount = unreadCount,
                    onNavigateToSchedule = { onSelectItem(1) },
                    onNavigateToAttendance = { onSelectItem(5) },
                    onNavigateToMarks = { onSelectItem(5) },
                    onNavigateToAssignments = { onSelectItem(2) },
                    onNavigateToNotices = { onSelectItem(3) },
                    onNavigateToProfile = { onSelectItem(4) },
                    onNavigateToClasses = { onSelectItem(5) },
                    onNavigateToCreateAssignment = onNavigateToCreateAssignment,
                    onNavigateToEditAssignment = onNavigateToEditAssignment,
                    onViewSubmissions = onViewSubmissions,
                    onLogoutClick = onLogout,
                    showSystemHeader = showSystemHeader
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
                    onAttendanceClick = { id, _ -> 
                        onSelectChild(id)
                        onNavigateToChildAttendance(id) 
                    },
                    onMarksClick = { id, _ -> 
                        onSelectChild(id)
                        onSelectItem(3) 
                    },
                    onAssignmentsClick = { id, _ -> 
                        onSelectChild(id)
                        onNavigateToChildAssignments(id) 
                    },
                    onTimetableClick = { id, _ -> 
                        onSelectChild(id)
                        onSelectItem(2) 
                    },
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
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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
