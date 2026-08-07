package com.example.mmp_app.feature.teacher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.core.presentation.SettingsViewModel
import com.example.mmp_app.domain.model.TeacherDashboardDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    teacherData: TeacherDashboardDto? = null,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        settingsViewModel.loadUser()
    }

    val primaryColor = Color(0xFF10B981) // Green for Teacher
    val secondaryColor = Color(0xFF34D399)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = primaryColor)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, "Toggle Theme")
                    }
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Profile", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        settingsState.user?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Profile Header Card
                item {
                    TeacherProfileHeaderCard(
                        name = settingsState.name.ifBlank { data.name },
                        email = data.email,
                        avatarUrl = data.avatarUrl,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isDarkTheme = isDarkTheme
                    )
                }

                // 2. Personal Information Section
                item {
                    TeacherInfoSection(
                        title = "Personal Information",
                        icon = Icons.Rounded.Person,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        TeacherInfoRow("Full Name", settingsState.name.ifBlank { data.name }, Icons.Rounded.Badge, textColor, primaryColor)
                        TeacherInfoRow("Email Address", data.email, Icons.Rounded.Email, textColor, primaryColor)
                        TeacherInfoRow("Phone Number", settingsState.phone.ifBlank { data.phone ?: "N/A" }, Icons.Rounded.Phone, textColor, primaryColor)
                        TeacherInfoRow("Gender", settingsState.gender.ifBlank { "Not Specified" }.replaceFirstChar { it.uppercase() }, Icons.Rounded.Wc, textColor, primaryColor)
                    }
                }

                // 3. Professional Details Section
                item {
                    TeacherInfoSection(
                        title = "Professional Details",
                        icon = Icons.Rounded.Work,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        TeacherInfoRow("Role", data.role?.replaceFirstChar { it.uppercase() } ?: "Teacher", Icons.Rounded.VerifiedUser, textColor, primaryColor)
                        teacherData?.let {
                            TeacherInfoRow("Total Students", "${it.totalStudents}", Icons.Rounded.Groups, textColor, primaryColor)
                            TeacherInfoRow("Total Classes", "${it.totalClasses}", Icons.Rounded.Schedule, textColor, primaryColor)
                        }
                        TeacherInfoRow("Address", settingsState.address.ifBlank { data.address ?: "Not Specified" }, Icons.Rounded.Home, textColor, primaryColor)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout from Device", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
    }
}

@Composable
fun TeacherProfileHeaderCard(
    name: String,
    email: String,
    avatarUrl: String?,
    primaryColor: Color,
    secondaryColor: Color,
    isDarkTheme: Boolean
) {
    val gradientColors = if (isDarkTheme) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
    } else {
        listOf(primaryColor, secondaryColor)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 300f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.2f)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
                ) {
                    if (!avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp),
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TeacherInfoSection(
    title: String,
    icon: ImageVector,
    primaryColor: Color,
    cardBgColor: Color,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun TeacherInfoRow(label: String, value: String, icon: ImageVector, textColor: Color, primaryColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = primaryColor.copy(alpha = 0.1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = primaryColor
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}
