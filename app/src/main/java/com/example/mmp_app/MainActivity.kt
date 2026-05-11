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
import com.example.mmp_app.ui.dashboard.DashboardScreen
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
    val navigationState = rememberNavigationState(
        startRoute = Routes.Login,
        topLevelRoutes = setOf(Routes.Login, Routes.Dashboard)
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

    LaunchedEffect(isLoggedIn, userProfile) {
        if (isLoggedIn || userProfile != null) {
            navigator.replace(Routes.Dashboard)
        } else {
            navigator.replace(Routes.Login)
        }
    }

    val entryProvider = entryProvider<Routes> {
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
                }
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
