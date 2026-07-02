package com.example.mmp_app.feature.parent.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.ParentProfileDto
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentProfileScreen(
    onLogout: () -> Unit,
    viewModel: ParentProfileViewModel = hiltViewModel(),
    isDarkTheme: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    var showImagePicker by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF6366F1)
    val secondaryColor = Color(0xFFA855F7)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    // Form states
    var name by remember(state.profile) { mutableStateOf(state.profile?.name ?: "") }
    var phone by remember(state.profile) { mutableStateOf(state.profile?.phone ?: "") }
    var address by remember(state.profile) { mutableStateOf(state.profile?.address ?: "") }
    var occupation by remember(state.profile) { mutableStateOf(state.profile?.occupation ?: "") }

    // Image Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            bytes?.let { viewModel.onAvatarSelected(it) }
        }
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                bytes?.let { viewModel.onAvatarSelected(it) }
            }
        }
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = backgroundColor
    ) { padding ->
        if (state.isLoading && state.profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Profile Header
                item {
                    ParentProfileHeader(
                        profile = state.profile,
                        selectedAvatar = state.selectedAvatarBytes,
                        onCameraClick = { showImagePicker = true },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isDarkTheme = isDarkTheme
                    )
                }

                // 2. Edit Profile Section
                item {
                    ParentSettingsSection(
                        title = "Personal Information",
                        icon = Icons.Rounded.Person,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        cardBgColor = cardBgColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Rounded.Badge, null, tint = primaryColor) },
                                isError = state.fieldErrors.containsKey("name"),
                                supportingText = state.fieldErrors["name"]?.let { { Text(it.joinToString(", ")) } }
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Rounded.Phone, null, tint = primaryColor) },
                                isError = state.fieldErrors.containsKey("phone"),
                                supportingText = state.fieldErrors["phone"]?.let { { Text(it.joinToString(", ")) } }
                            )

                            OutlinedTextField(
                                value = occupation ?: "",
                                onValueChange = { occupation = it },
                                label = { Text("Occupation") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Rounded.Work, null, tint = primaryColor) },
                                isError = state.fieldErrors.containsKey("occupation"),
                                supportingText = state.fieldErrors["occupation"]?.let { { Text(it.joinToString(", ")) } }
                            )

                            OutlinedTextField(
                                value = address ?: "",
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Rounded.Home, null, tint = primaryColor) },
                                isError = state.fieldErrors.containsKey("address"),
                                supportingText = state.fieldErrors["address"]?.let { { Text(it.joinToString(", ")) } }
                            )

                            Button(
                                onClick = { viewModel.updateProfile(name, phone, address, occupation) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = !state.isUpdating,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                if (state.isUpdating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                else Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 3. Linked Children Section
                item {
                    ParentSettingsSection(
                        title = "Linked Children",
                        icon = Icons.Rounded.People,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        cardBgColor = cardBgColor
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "You have ${state.profile?.childrenCount ?: 0} children enrolled in the system.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 4. Logout Action
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout from Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showImagePicker) {
        ModalBottomSheet(
            onDismissRequest = { showImagePicker = false },
            containerColor = cardBgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Update Profile Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                ListItem(
                    headlineContent = { Text("Camera") },
                    leadingContent = { Icon(Icons.Rounded.CameraAlt, null) },
                    modifier = Modifier.clickable {
                        val file = File(context.cacheDir, "temp_photo.jpg")
                        tempPhotoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        cameraLauncher.launch(tempPhotoUri!!)
                        showImagePicker = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Gallery") },
                    leadingContent = { Icon(Icons.Rounded.PhotoLibrary, null) },
                    modifier = Modifier.clickable {
                        galleryLauncher.launch("image/*")
                        showImagePicker = false
                    }
                )
                if (state.profile?.avatarUrl != null || state.selectedAvatarBytes != null) {
                    ListItem(
                        headlineContent = { Text("Remove Photo", color = Color.Red) },
                        leadingContent = { Icon(Icons.Rounded.Delete, null, tint = Color.Red) },
                        modifier = Modifier.clickable {
                            // Logic to remove photo
                            showImagePicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ParentProfileHeader(
    profile: ParentProfileDto?,
    selectedAvatar: ByteArray?,
    onCameraClick: () -> Unit,
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
            .padding(16.dp)
            .height(220.dp)
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
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
                    ) {
                        AsyncImage(
                            model = selectedAvatar ?: profile?.avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = com.example.mmp_app.core.R.drawable.mmplogo)
                        )
                    }
                    IconButton(
                        onClick = onCameraClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(Icons.Rounded.CameraAlt, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = profile?.name ?: "Parent Name",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = profile?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ParentSettingsSection(
    title: String,
    icon: ImageVector,
    primaryColor: Color,
    textColor: Color,
    cardBgColor: Color,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f)
            )
        }
        
        AnimatedVisibility(visible = expanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBgColor,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    content()
                }
            }
        }
    }
}
