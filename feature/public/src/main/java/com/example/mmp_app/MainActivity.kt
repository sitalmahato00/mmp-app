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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.mmp_app.core.ui.theme.MMPAppTheme
import com.example.mmp_app.domain.model.*
import com.example.mmp_app.domain.repository.AuthRepository



import com.example.mmp_app.feature.auth.ui.AuthViewModel
import com.example.mmp_app.feature.auth.ui.LoginScreen
import com.example.mmp_app.feature.auth.ui.OtpVerificationScreen
import com.example.mmp_app.feature.auth.ui.SplashScreen
import com.example.mmp_app.feature.parent.ui.ChildDetailsScreen
import com.example.mmp_app.feature.student.ui.AssignmentsScreen
import com.example.mmp_app.feature.student.ui.AttendanceScreen
import com.example.mmp_app.feature.student.ui.MarksScreen
import com.example.mmp_app.feature.student.ui.SubjectsScreen
import com.example.mmp_app.feature.teacher.ui.TeacherAttendanceScreen
import com.example.mmp_app.feature.teacher.ui.TeacherMarksScreen
import com.example.mmp_app.presentation.*
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
                MainContent(authRepository)
            }
        }
    }
}

@Composable
fun MainContent(authRepository: AuthRepository) {
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
                onNavigateToRoutines = { navigator.navigate(Routes.Routines) },
                onNavigateToExams = { navigator.navigate(Routes.Exams) },
                onNavigateToResults = { navigator.navigate(Routes.Results) },
                onNavigateToSubjects = { navigator.navigate(Routes.Subjects) },
                onNavigateToTimetable = { navigator.navigate(Routes.Timetable) },
                onNavigateToDownloads = { navigator.navigate(Routes.Downloads) },
                onNavigateToProfile = { navigator.navigate(Routes.Profile) },
                onNavigateToSettings = { navigator.navigate(Routes.Settings) }
            )
        }
        entry<Routes.Attendance> {
            AttendanceScreen(onBack = { navigator.goBack() })
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
            // SubjectDetailScreen is missing, use placeholder for now or find it
            PlaceholderScreen("Subject Detail ${route.subjectName}", onBack = { navigator.goBack() })
        }
        entry<Routes.Routines> { PlaceholderScreen("Routines", onBack = { navigator.goBack() }) }
        entry<Routes.Exams> { PlaceholderScreen("Exams", onBack = { navigator.goBack() }) }
        entry<Routes.Timetable> { PlaceholderScreen("Timetable", onBack = { navigator.goBack() }) }
        entry<Routes.Downloads> { PlaceholderScreen("Study Materials", onBack = { navigator.goBack() }) }
        entry<Routes.Profile> { PlaceholderScreen("Profile", onBack = { navigator.goBack() }) }
        entry<Routes.Settings> { PlaceholderScreen("Settings", onBack = { navigator.goBack() }) }
        entry<Routes.Fees> { PlaceholderScreen("Fees", onBack = { navigator.goBack() }) }
        entry<Routes.Notices> { PlaceholderScreen("Notices", onBack = { navigator.goBack() }) }
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
                name = route.name,
                onBack = { navigator.goBack() }
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
