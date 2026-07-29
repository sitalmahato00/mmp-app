package com.example.mmp_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.domain.repository.AuthRepository



import com.example.mmp_app.feature.auth.ui.AuthViewModel
import com.example.mmp_app.feature.auth.ui.LoginScreen
import com.example.mmp_app.feature.auth.ui.OtpVerificationScreen
import com.example.mmp_app.feature.auth.ui.SplashScreen
import com.example.mmp_app.feature.parent.ui.ChildDetailsScreen
import com.example.mmp_app.feature.parent.ui.ParentAttendanceScreen
import com.example.mmp_app.feature.parent.ui.ParentAssignmentsScreen
import com.example.mmp_app.feature.parent.ui.ParentResultsScreen
import com.example.mmp_app.feature.student.ui.AssignmentsScreen
import com.example.mmp_app.feature.student.ui.AttendanceScreen
import com.example.mmp_app.feature.student.ui.DownloadsScreen
import com.example.mmp_app.feature.student.ui.FeesScreen
import com.example.mmp_app.feature.student.ui.MarksScreen
import com.example.mmp_app.feature.student.ui.NoticesScreen
import com.example.mmp_app.feature.student.ui.NotificationScreen
import com.example.mmp_app.core.ui.SettingsScreen
import com.example.mmp_app.feature.student.ui.StudentProfileScreen
import com.example.mmp_app.feature.student.ui.SubjectDetailScreen
import com.example.mmp_app.feature.student.ui.SubjectsScreen
import com.example.mmp_app.feature.student.ui.TimetableScreen
import com.example.mmp_app.feature.teacher.ui.TeacherAttendanceScreen
import com.example.mmp_app.feature.teacher.ui.TeacherMarksScreen
import com.example.mmp_app.presentation.*
import com.example.mmp_app.core.presentation.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            
            MMPAppTheme(darkTheme = isDarkTheme) {
                MainContent(authRepository, isDarkTheme)
            }
        }
    }
}

@Composable
fun MainContent(authRepository: AuthRepository, isDarkTheme: Boolean) {
    val context = LocalContext.current
    var splashFinished by remember { mutableStateOf(false) }
    val navigationState = rememberNavigationState(
        startRoute = Routes.Splash,
        topLevelRoutes = setOf(Routes.Splash, Routes.Login, Routes.Dashboard)
    )
    val navigator = remember { Navigator(navigationState) }
    val authViewModel: AuthViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val otpSent by authViewModel.otpSent.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val userProfile by authRepository.getUserProfile().collectAsState(initial = null)


    LaunchedEffect(otpSent) {
        if (otpSent) {
            navigator.navigate(Routes.OtpVerification)
        }
    }

    LaunchedEffect(isLoggedIn, userProfile, splashFinished) {
        if (!splashFinished) return@LaunchedEffect
        if (isLoggedIn || userProfile != null) {
            navigator.replace(Routes.Dashboard)
        } else {
            navigator.replace(Routes.Login)
        }
    }

    val entryProvider = entryProvider<Routes> {
        entry<Routes.Splash> {
            SplashScreen(onNavigateNext = {
                splashFinished = true
            })
        }
        entry<Routes.Login> {
            LoginScreen(
                email = email,
                onEmailChange = authViewModel::onEmailChanged,
                password = password,
                onPasswordChange = authViewModel::onPasswordChanged,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLogin = authViewModel::login
            )
        }
        entry<Routes.OtpVerification> {
            OtpVerificationScreen(
                email = email,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onVerify = authViewModel::verifyOtp,
                onResend = authViewModel::login,
                onOtpValueChange = authViewModel::resetError,
                onBackToLogin = {
                    authViewModel.resetAuthState()
                    navigator.goBack()
                }
            )
        }
        entry<Routes.Dashboard> {
            DashboardScreen(
                userProfile = userProfile,
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        authViewModel.resetAuthState()
                    }
                },
                onNavigateToAttendance = { navigator.navigate(Routes.Attendance) },
                onNavigateToMarks = { navigator.navigate(Routes.Marks) },
                onNavigateToAssignments = { navigator.navigate(Routes.Assignments) },
                onNavigateToFees = { navigator.navigate(Routes.Fees) },
                onNavigateToNotices = { navigator.navigate(Routes.Notices) },
                onRecordAttendance = { classId, subject -> 
                    navigator.navigate(Routes.RecordAttendance(classId, subject)) 
                },
                onRecordMarks = { classId, subject -> 
                    navigator.navigate(Routes.RecordMarks(classId, subject)) 
                },
                onNavigateToChildDetails = { childId, name ->
                    navigator.navigate(Routes.ChildDetails(childId, name))
                },
                onNavigateToChildAttendance = { id -> navigator.navigate(Routes.ChildAttendance(id)) },
                onNavigateToChildAssignments = { id -> navigator.navigate(Routes.ChildAssignments(id)) },
                onNavigateToChildResults = { id -> navigator.navigate(Routes.ChildResults(id)) },
                onNavigateToRoutines = { navigator.navigate(Routes.Routines) },
                onNavigateToExams = { navigator.navigate(Routes.Exams) },
                onNavigateToResults = { navigator.navigate(Routes.Results) },
                onNavigateToSubjects = { navigator.navigate(Routes.Subjects) },
                onNavigateToTimetable = { navigator.navigate(Routes.Timetable) },
                onNavigateToDownloads = { navigator.navigate(Routes.Downloads) },
                onNavigateToProfile = { navigator.navigate(Routes.Profile) },
                onNavigateToSettings = { navigator.navigate(Routes.Settings) },
                onNavigateToNotifications = { navigator.navigate(Routes.Notifications) },
                onNavigateToChildrenList = { navigator.navigate(Routes.ChildrenList) }
            )
        }
        entry<Routes.Attendance> {
            AttendanceScreen(onBack = { navigator.goBack() }, isDarkTheme = isDarkTheme)
        }
        entry<Routes.Marks> {
            MarksScreen(onBack = { navigator.goBack() })
        }
        entry<Routes.Results> {
            MarksScreen(onBack = { navigator.goBack() })
        }
        entry<Routes.Assignments> {
            AssignmentsScreen(onBack = { navigator.goBack() })
        }
        entry<Routes.Subjects> {
            SubjectsScreen(
                onBack = { navigator.goBack() },
                onSubjectClick = { id, name, code ->
                    navigator.navigate(Routes.SubjectDetail(id, name, code))
                }
            )
        }
        entry<Routes.SubjectDetail> { route ->
            SubjectDetailScreen(
                subjectId = route.subjectId,
                subjectName = route.subjectName,
                subjectCode = route.subjectCode,
                onBack = { navigator.goBack() }
            )
        }
        entry<Routes.Routines> { PlaceholderScreen("Routines", onBack = { navigator.goBack() }) }
        entry<Routes.Exams> { PlaceholderScreen("Exams", onBack = { navigator.goBack() }) }
        entry<Routes.Timetable> { TimetableScreen(onBack = { navigator.goBack() }) }
        entry<Routes.Downloads> { DownloadsScreen(onBack = { navigator.goBack() }) }
        entry<Routes.Profile> {
            StudentProfileScreen(
                onBack = { navigator.goBack() },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        authViewModel.resetAuthState()
                    }
                },
                onEditProfile = { navigator.navigate(Routes.Settings) },
                isDarkTheme = isDarkTheme
            )
        }
        entry<Routes.Settings> { 
            SettingsScreen(
                onBack = { navigator.goBack() },
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                        authViewModel.resetAuthState()
                        navigator.replace(Routes.Login)
                    }
                }
            ) 
        }
        entry<Routes.Notifications> { 
            NotificationScreen(
                onBack = { navigator.goBack() },
                onOpenUrl = { url ->
                    try {
                        val intent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
                        intent.launchUrl(context, android.net.Uri.parse(url))
                    } catch (e: Exception) {
                        // Fallback to basic browser intent if custom tabs fail
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    }
                }
            )
        }
        entry<Routes.Fees> { FeesScreen(onBack = { navigator.goBack() }) }
        entry<Routes.Notices> { NoticesScreen(onBack = { navigator.goBack() }) }
        entry<Routes.RecordAttendance> { route ->
            TeacherAttendanceScreen(
                classId = route.classId,
                subject = route.subject,
                onBack = { navigator.goBack() }
            )
        }
        entry<Routes.RecordMarks> { route ->
            TeacherMarksScreen(
                classId = route.classId,
                subject = route.subject,
                onBack = { navigator.goBack() }
            )
        }
        entry<Routes.ChildDetails> { route ->
            ChildDetailsScreen(
                childId = route.childId,
                onBack = { navigator.goBack() },
                onNavigateToAttendance = { id -> navigator.navigate(Routes.ChildAttendance(id)) },
                onNavigateToAssignments = { id -> navigator.navigate(Routes.ChildAssignments(id)) },
                onNavigateToResults = { id -> navigator.navigate(Routes.ChildResults(id)) },
                onNavigateToInfo = { /* Handle Info */ },
                isDarkTheme = isDarkTheme
            )
        }
        entry<Routes.ChildrenList> {
            PlaceholderScreen("My Children", onBack = { navigator.goBack() })
        }
        entry<Routes.ChildAttendance> { route ->
            ParentAttendanceScreen(
                childId = route.childId,
                onBack = { navigator.goBack() },
                isDarkTheme = isDarkTheme
            )
        }
        entry<Routes.ChildAssignments> { route ->
            ParentAssignmentsScreen(
                childId = route.childId,
                onBack = { navigator.goBack() },
                isDarkTheme = isDarkTheme
            )
        }
        entry<Routes.ChildResults> { route ->
            ParentResultsScreen(
                childId = route.childId,
                onBack = { navigator.goBack() },
                isDarkTheme = isDarkTheme
            )
        }
    }

    val backStack = navigationState.backStacks[navigationState.topLevelRoute]!!

    NavDisplay(
        backStack = backStack.toList(),
        onBack = { navigator.goBack() },
        entryProvider = entryProvider
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$title Screen is coming soon!", color = Color.Gray)
        }
    }
}
