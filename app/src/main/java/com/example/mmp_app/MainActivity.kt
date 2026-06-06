package com.example.mmp_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.mmp_app.data.local.dao.UserProfileDao
import com.example.mmp_app.data.repository.AuthRepository
import com.example.mmp_app.ui.auth.AuthViewModel
import com.example.mmp_app.ui.auth.LoginScreen
import com.example.mmp_app.ui.auth.OtpVerificationScreen
import com.example.mmp_app.ui.auth.SplashScreen
import com.example.mmp_app.ui.dashboard.*
import com.example.mmp_app.ui.navigation.*
import com.example.mmp_app.ui.theme.MMPAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userProfileDao: UserProfileDao
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MMPAppTheme {
                MainContent(userProfileDao, authRepository)
            }
        }
    }
}

@Composable
fun MainContent(userProfileDao: UserProfileDao, authRepository: AuthRepository) {
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

    val userProfile by userProfileDao.getUserProfile().collectAsState(initial = null)

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
                }
            )
        }
        entry<Routes.Attendance> {
            AttendanceScreen(onBack = { navigator.goBack() })
        }
        entry<Routes.Marks> {
            MarksScreen(onBack = { navigator.goBack() })
        }
        entry<Routes.Assignments> {
            AssignmentsScreen(onBack = { navigator.goBack() })
        }
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
