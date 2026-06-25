package com.example.mmp_app.feature.student.ui

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mmp_app.domain.model.NotificationPreferencesDto
import com.example.mmp_app.core.presentation.ThemeViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E293B) else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (user == null && isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (user == null && error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.ErrorOutline, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = error ?: "Failed to load settings", color = textColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Button(onClick = { viewModel.loadCurrentUser() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Retry")
                    }
                }
            } else if (user != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { 
                        SectionHeader("Edit Profile", Icons.Rounded.Person, primaryColor, textColor)
                        StyledCard(cardBgColor) {
                            EditProfileSection(user, viewModel, validationErrors, primaryColor, isLoading)
                        }
                    }
                    
                    item { 
                        SectionHeader("Change Password", Icons.Rounded.Lock, primaryColor, textColor)
                        StyledCard(cardBgColor) {
                            ChangePasswordSection(viewModel, validationErrors, primaryColor, isLoading)
                        }
                    }
                    
                    item { 
                        SectionHeader("Notifications", Icons.Rounded.Notifications, primaryColor, textColor)
                        StyledCard(cardBgColor) {
                            NotificationPreferencesSection(user?.notificationPreferences, viewModel, primaryColor, isLoading)
                        }
                    }
                    
                    item { 
                        SectionHeader("Security", Icons.Rounded.Security, primaryColor, textColor)
                        StyledCard(cardBgColor) {
                            TwoFactorSection(user, viewModel, primaryColor, isLoading)
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
            
            if (isLoading && user != null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector, color: Color, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun StyledCard(backgroundColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSection(
    user: com.example.mmp_app.domain.model.FullUserDetailDto?,
    viewModel: SettingsViewModel,
    validationErrors: Map<String, List<String>>,
    primaryColor: Color,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    var name by remember(user?.name) { mutableStateOf(user?.name ?: "") }
    var phone by remember(user?.phone) { mutableStateOf(user?.phone ?: "") }
    var gender by remember(user?.gender) { mutableStateOf(user?.gender ?: "") }
    var dob by remember(user?.dob) { mutableStateOf(user?.dob ?: "") }
    var address by remember(user?.address) { mutableStateOf(user?.address ?: "") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        avatarUri = uri
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.padding(bottom = 8.dp)) {
            AsyncImage(
                model = avatarUri ?: user?.avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clickable { imagePicker.launch("image/*") },
                contentScale = ContentScale.Crop
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clickable { imagePicker.launch("image/*") },
                shape = CircleShape,
                color = primaryColor,
                tonalElevation = 4.dp
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.padding(6.dp), tint = Color.White)
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Person, null, tint = primaryColor) },
            shape = RoundedCornerShape(16.dp),
            isError = validationErrors.containsKey("name"),
            supportingText = { validationErrors["name"]?.firstOrNull()?.let { Text(it) } }
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Phone, null, tint = primaryColor) },
            shape = RoundedCornerShape(16.dp),
            isError = validationErrors.containsKey("phone"),
            supportingText = { validationErrors["phone"]?.firstOrNull()?.let { Text(it) } }
        )

        var genderExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                leadingIcon = { Icon(Icons.Rounded.Wc, null, tint = primaryColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                listOf("male", "female", "other").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            gender = option
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                dob = String.format("%04d-%02d-%02d", year, month + 1, day)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dob,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Cake, null, tint = primaryColor) },
                shape = RoundedCornerShape(16.dp),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = primaryColor,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Box(Modifier.matchParentSize().clickable { datePickerDialog.show() })
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Home, null, tint = primaryColor) },
            shape = RoundedCornerShape(16.dp)
        )

        Button(
            onClick = {
                val file = avatarUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, "avatar_upload.jpg")
                    val outputStream = FileOutputStream(tempFile)
                    inputStream?.copyTo(outputStream)
                    tempFile
                }
                viewModel.updateProfile(name, phone.ifBlank { null }, gender.ifBlank { null }, dob.ifBlank { null }, address.ifBlank { null }, file)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotBlank() && !isLoading // Prevent sending empty name and multiple clicks
        ) {
            Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun ChangePasswordSection(viewModel: SettingsViewModel, validationErrors: Map<String, List<String>>, primaryColor: Color, isLoading: Boolean = false) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // Instant validation
    val isPasswordStrong = remember(newPassword) {
        newPassword.length >= 8 && 
        newPassword.any { it.isUpperCase() } && 
        newPassword.any { it.isLowerCase() } && 
        newPassword.any { it.isDigit() }
    }
    
    val passwordsMatch = remember(newPassword, confirmPassword) {
        newPassword == confirmPassword && confirmPassword.isNotEmpty()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.LockOpen, null, tint = primaryColor) },
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showCurrent = !showCurrent }) {
                    Icon(if (showCurrent) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                }
            },
            isError = validationErrors.containsKey("current_password"),
            supportingText = { validationErrors["current_password"]?.firstOrNull()?.let { Text(it) } }
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = primaryColor) },
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showNew = !showNew }) {
                    Icon(if (showNew) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                }
            },
            supportingText = { 
                if (newPassword.isEmpty()) {
                    Text("Min 8 chars, mixed case + numbers")
                } else if (!isPasswordStrong) {
                    Text("Weak password: Need mixed case and digits", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Strong password", color = Color(0xFF10B981)) // Success green
                }
            },
            isError = newPassword.isNotEmpty() && !isPasswordStrong
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.CheckCircle, null, tint = primaryColor) },
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirm = !showConfirm }) {
                    Icon(if (showConfirm) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                }
            },
            supportingText = {
                if (confirmPassword.isNotEmpty()) {
                    if (passwordsMatch) {
                        Text("Passwords match", color = Color(0xFF10B981))
                    } else {
                        Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            isError = confirmPassword.isNotEmpty() && !passwordsMatch
        )

        Button(
            onClick = {
                if (passwordsMatch && isPasswordStrong) {
                    viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                    currentPassword = ""; newPassword = ""; confirmPassword = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = currentPassword.isNotBlank() && isPasswordStrong && passwordsMatch && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Update Password", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NotificationPreferencesSection(prefs: NotificationPreferencesDto?, viewModel: SettingsViewModel, primaryColor: Color, isLoading: Boolean = false) {
    var localPrefs by remember(prefs) { 
        mutableStateOf(prefs ?: NotificationPreferencesDto(false, false, false, false, false, false, false)) 
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Email Notifications", style = MaterialTheme.typography.titleMedium, color = primaryColor, fontWeight = FontWeight.Bold)
        NotificationToggle("Notices", localPrefs.emailNotices, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(emailNotices = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }
        NotificationToggle("Marks", localPrefs.emailMarks, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(emailMarks = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }
        NotificationToggle("Assignments", localPrefs.emailAssignments, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(emailAssignments = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Push Notifications", style = MaterialTheme.typography.titleMedium, color = primaryColor, fontWeight = FontWeight.Bold)
        NotificationToggle("Notices", localPrefs.pushNotices, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(pushNotices = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }
        NotificationToggle("Marks", localPrefs.pushMarks, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(pushMarks = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }
        NotificationToggle("Assignments", localPrefs.pushAssignments, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(pushAssignments = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }
        NotificationToggle("Attendance", localPrefs.pushAttendance, primaryColor, !isLoading) { 
            localPrefs = localPrefs.copy(pushAttendance = it)
            viewModel.updateNotificationPreferences(localPrefs) 
        }
    }
}

@Composable
fun NotificationToggle(label: String, checked: Boolean, primaryColor: Color, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
        )
    }
}

@Composable
fun TwoFactorSection(user: com.example.mmp_app.domain.model.FullUserDetailDto?, viewModel: SettingsViewModel, primaryColor: Color, isLoading: Boolean = false) {
    // Use local state to handle the switch immediately, then update when user state changes
    var localEnabled by remember(user?.twoFactorEnabled) { mutableStateOf(user?.twoFactorEnabled ?: false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Two-Factor Authentication", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("Verification codes will be sent to your email", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = localEnabled,
                onCheckedChange = { 
                    localEnabled = it
                    // Force email method
                    viewModel.updateTwoFactor(it, if (it) "email" else null) 
                },
                enabled = !isLoading,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
            )
        }

        AnimatedVisibility(
            visible = localEnabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                Text("Verification Method", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = true, 
                            onClick = null, // Always selected since it's the only method
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                        )
                        Text("Email (${user?.email ?: "loading..."})")
                    }
                }
            }
        }
    }
}

