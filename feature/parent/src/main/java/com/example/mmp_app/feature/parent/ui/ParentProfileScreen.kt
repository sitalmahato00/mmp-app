package com.example.mmp_app.feature.parent.ui

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
import com.example.mmp_app.domain.model.ParentProfileDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentProfileScreen(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    viewModel: ParentProfileViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()

    val primaryColor = Color(0xFF6366F1)
    val secondaryColor = Color(0xFFA855F7)
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
        state.profile?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Profile Header Card
                item {
                    ParentProfileHeaderCard(data, primaryColor, secondaryColor, isDarkTheme)
                }

                // 2. Personal Information Section
                item {
                    ParentInfoSection(
                        title = "Personal Information",
                        icon = Icons.Rounded.Person,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        ParentInfoRow("Full Name", data.name, Icons.Rounded.Badge, textColor)
                        ParentInfoRow("Email Address", data.email, Icons.Rounded.Email, textColor)
                        ParentInfoRow("Phone Number", data.phone ?: "N/A", Icons.Rounded.Phone, textColor)
                        ParentInfoRow("Gender", data.gender?.replaceFirstChar { it.uppercase() } ?: "Not Specified", Icons.Rounded.Wc, textColor)
                        ParentInfoRow("Occupation", data.occupation ?: "Not Specified", Icons.Rounded.Work, textColor)
                    }
                }

                // 3. System Information Section
                item {
                    ParentInfoSection(
                        title = "System Details",
                        icon = Icons.Rounded.Settings,
                        primaryColor = primaryColor,
                        cardBgColor = cardBgColor,
                        textColor = textColor
                    ) {
                        ParentInfoRow("Relationship", data.relationToStudent.replaceFirstChar { it.uppercase() }, Icons.Rounded.FamilyRestroom, textColor)
                        ParentInfoRow("Linked Children", "${data.childrenCount} Child(ren)", Icons.Rounded.People, textColor)
                        ParentInfoRow("Current Address", data.address ?: "Not Specified", Icons.Rounded.Home, textColor)
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
                if (state.isLoading) {
                    CircularProgressIndicator(color = primaryColor)
                } else if (state.error != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = Color.Red)
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentProfileHeaderCard(data: ParentProfileDto, primaryColor: Color, secondaryColor: Color, isDarkTheme: Boolean) {
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
                    text = data.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Parent Account • ${data.childrenCount} Child(ren)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ParentInfoSection(
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
fun ParentInfoRow(label: String, value: String, icon: ImageVector, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF6366F1).copy(alpha = 0.1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = Color(0xFF6366F1)
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
