package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.data.local.entity.UserProfileEntity
import com.example.mmp_app.data.remote.model.HodDashboardDto
import com.example.mmp_app.data.remote.model.ParentDashboardDto
import com.example.mmp_app.data.remote.model.StudentDashboardDto
import com.example.mmp_app.data.remote.model.TeacherDashboardDto
import com.example.mmp_app.ui.theme.MMPAppTheme

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SupervisedUserCircle
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import com.example.mmp_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userProfile: UserProfileEntity?,
    onLogout: () -> Unit
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val studentData by viewModel.studentDashboard.collectAsState()
    val teacherData by viewModel.teacherDashboard.collectAsState()
    val parentData by viewModel.parentDashboard.collectAsState()
    val hodData by viewModel.hodDashboard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(userProfile) {
        when (userProfile?.role) {
            "Student" -> viewModel.loadStudentDashboard()
            "Teacher" -> viewModel.loadTeacherDashboard()
            "Parent" -> viewModel.loadParentDashboard()
            "HOD" -> viewModel.loadHodDashboard()
        }
    }

    DashboardAdaptiveContent(
        userProfile = userProfile,
        studentData = studentData,
        teacherData = teacherData,
        parentData = parentData,
        hodData = hodData,
        isLoading = isLoading,
        onLogout = onLogout
    )
}

@Composable
fun DashboardAdaptiveContent(
    userProfile: UserProfileEntity?,
    studentData: StudentDashboardDto?,
    teacherData: TeacherDashboardDto?,
    parentData: ParentDashboardDto?,
    hodData: HodDashboardDto?,
    isLoading: Boolean,
    onLogout: () -> Unit
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
        DashboardContent(
            userProfile = userProfile,
            studentData = studentData,
            teacherData = teacherData,
            parentData = parentData,
            hodData = hodData,
            isLoading = isLoading,
            onLogout = onLogout
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    userProfile: UserProfileEntity?,
    studentData: StudentDashboardDto?,
    teacherData: TeacherDashboardDto?,
    parentData: ParentDashboardDto?,
    hodData: HodDashboardDto?,
    isLoading: Boolean,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.mmplogo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
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
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Rounded.Notifications, contentDescription = null)
                    }
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (isLoading && studentData == null && teacherData == null && parentData == null && hodData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (userProfile?.role) {
                    "Student" -> studentData?.let { StudentDashboard(data = it) }
                    "Teacher" -> teacherData?.let { TeacherDashboard(data = it) }
                    "Parent" -> parentData?.let { ParentDashboard(data = it) }
                    "HOD" -> hodData?.let { HodDashboard(data = it) }
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

@Preview(showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
fun DashboardAdaptivePreview() {
    MMPAppTheme {
        DashboardAdaptiveContent(
            userProfile = UserProfileEntity(
                id = 1,
                name = "HOD Admin",
                email = "hod@mmp.edu.np",
                role = "HOD"
            ),
            studentData = null,
            teacherData = null,
            parentData = null,
            hodData = HodDashboardDto(
                departmentName = "Computer Science",
                totalTeachers = 12,
                totalStudents = 350,
                pendingApprovals = 3,
                recentNotices = emptyList()
            ),
            isLoading = false,
            onLogout = {}
        )
    }
}
