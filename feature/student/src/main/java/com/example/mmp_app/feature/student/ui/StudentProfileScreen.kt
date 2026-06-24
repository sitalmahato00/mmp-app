package com.example.mmp_app.feature.student.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.StudentDashboardDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val studentData by viewModel.studentDashboard.collectAsState()

    LaunchedEffect(Unit) {
        if (studentData == null) {
            viewModel.loadStudentDashboard()
        }
    }

    val primaryColor = Color(0xFF2563EB)
    val secondaryColor = Color(0xFF60A5FA)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                actions = {
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
        studentData?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Profile Header Card (Adapted from StudentDashboard's ProfileGradientCard)
                item {
                    ProfileHeaderCard(data, primaryColor, secondaryColor, isDarkTheme)
                }

                // 2. Personal Information Section
                item {
                    InfoSection(
                        title = "Personal Information",
                        icon = Icons.Rounded.Person,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        InfoRow("Full Name", data.studentName, Icons.Rounded.Badge, textColor)
                        InfoRow("Email Address", data.email ?: "N/A", Icons.Rounded.Email, textColor)
                        InfoRow("Phone Number", data.phone ?: "N/A", Icons.Rounded.Phone, textColor)
                        InfoRow("Gender", "Not Specified", Icons.Rounded.Wc, textColor)
                        InfoRow("Date of Birth", "Not Specified", Icons.Rounded.Cake, textColor)
                    }
                }

                // 3. Academic Information Section
                item {
                    InfoSection(
                        title = "Academic Details",
                        icon = Icons.Rounded.School,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        InfoRow("Student ID", data.studentId.toString(), Icons.Rounded.Fingerprint, textColor)
                        InfoRow("Roll Number", data.rollNumber ?: "N/A", Icons.Rounded.FormatListNumbered, textColor)
                        InfoRow("Program", data.program, Icons.Rounded.Book, textColor)
                        InfoRow("Semester", "Semester ${data.semester}", Icons.Rounded.Timeline, textColor)
                        InfoRow("Department", "Computer Science", Icons.Rounded.Business, textColor)
                    }
                }

                // 4. Contact/Address Section
                item {
                    InfoSection(
                        title = "Contact Details",
                        icon = Icons.Rounded.LocationOn,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        InfoRow("Current Address", "Not Specified", Icons.Rounded.Home, textColor)
                        InfoRow("Emergency Contact", "Not Specified", Icons.Rounded.ContactPhone, textColor)
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
fun ProfileHeaderCard(data: StudentDashboardDto, primaryColor: Color, secondaryColor: Color, isDarkTheme: Boolean) {
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
                    if (!data.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = data.avatarUrl,
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
                    text = data.studentName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${data.program} • Semester ${data.semester}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun InfoSection(
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
fun InfoRow(label: String, value: String, icon: ImageVector, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF2563EB).copy(alpha = 0.1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = Color(0xFF2563EB)
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
