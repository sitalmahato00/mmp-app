package com.example.mmp_app.core.ui

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mmp_app.core.R
import com.example.mmp_app.core.presentation.*
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.Success -> snackbarHost.showSnackbar(event.message)
                is SettingsEvent.Error   -> snackbarHost.showSnackbar(event.message)
                is SettingsEvent.PasswordChanged -> {
                    snackbarHost.showSnackbar("Password changed successfully")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    } else if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        if (state.isLoading && state.user == null && state.parentProfile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val isParent = state.role.lowercase().trim() == "parent"
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { Spacer(modifier = Modifier.height(padding.calculateTopPadding())) }
                
                item {
                    SettingsSection(title = "Edit Profile", icon = Icons.Rounded.Person) {
                        ProfileSection(state, viewModel, context)
                    }
                }

                item {
                    SettingsSection(title = "Change Password", icon = Icons.Rounded.Lock) {
                        PasswordSection(state, viewModel)
                    }
                }

                if (!isParent) {
                    item {
                        SettingsSection(title = "Notification Preferences", icon = Icons.Rounded.Notifications) {
                            NotificationPrefsSection(state, viewModel)
                        }
                    }

                    item {
                        SettingsSection(title = "Two-Factor Authentication", icon = Icons.Rounded.Security) {
                            TwoFactorSection(state, viewModel)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout from Account")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null
            )
        }
        
        AnimatedVisibility(visible = expanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSection(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    context: Context
) {
    var showImagePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onAvatarSelected(it) }
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempPhotoUri?.let { viewModel.onAvatarSelected(it) }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.LightGray.copy(alpha = 0.3f)
            ) {
                AsyncImage(
                    model = state.selectedAvatarUri ?: state.user?.avatarUrl ?: state.parentProfile?.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(id = R.drawable.mmplogo)
                )
            }
            IconButton(
                onClick = { showImagePicker = true },
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = state.fieldErrors.containsKey("name"),
            supportingText = state.fieldErrors["name"]?.let { { Text(it.joinToString(", ")) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.phone,
            onValueChange = { viewModel.onPhoneChange(it) },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = state.fieldErrors.containsKey("phone"),
            supportingText = state.fieldErrors["phone"]?.let { { Text(it.joinToString(", ")) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GenderDropdown(state.gender, state.fieldErrors["gender"]) { viewModel.onGenderChange(it) }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.role.lowercase().trim() == "parent") {
            OutlinedTextField(
                value = state.occupation,
                onValueChange = { viewModel.onOccupationChange(it) },
                label = { Text("Occupation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = state.fieldErrors.containsKey("occupation"),
                supportingText = state.fieldErrors["occupation"]?.let { { Text(it.joinToString(", ")) } }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.relationToStudent,
                onValueChange = {},
                readOnly = true,
                label = { Text("Relation to Student") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        } else {
            DobPicker(state.dob, state.fieldErrors["dob"]) { viewModel.onDobChange(it) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.address,
            onValueChange = { viewModel.onAddressChange(it) },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = state.fieldErrors.containsKey("address"),
            supportingText = state.fieldErrors["address"]?.let { { Text(it.joinToString(", ")) } }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveProfile(context) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !state.isSavingProfile,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isSavingProfile) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Save Profile Changes")
        }
    }

    if (showImagePicker) {
        ModalBottomSheet(onDismissRequest = { showImagePicker = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Update Profile Photo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                ListItem(
                    headlineContent = { Text("Camera") },
                    leadingContent = { Icon(Icons.Rounded.CameraAlt, null) },
                    modifier = Modifier.clickable {
                        val file = File(context.cacheDir, "temp_photo.jpg")
                        tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(current: String, errors: List<String>?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("male", "female", "other")
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = current.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            isError = errors != null,
            supportingText = errors?.let { { Text(it.joinToString(", ")) } }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DobPicker(current: String, errors: List<String>?, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val dialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
            onSelect(formatted)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = current,
        onValueChange = {},
        readOnly = true,
        label = { Text("Date of Birth") },
        modifier = Modifier.fillMaxWidth().clickable { dialog.show() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = if (errors != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            disabledLabelColor = if (errors != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        isError = errors != null,
        supportingText = errors?.let { { Text(it.joinToString(", ")) } }
    )
}

@Composable
fun PasswordSection(state: SettingsUiState, viewModel: SettingsViewModel) {
    var curVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var conVisible by remember { mutableStateOf(false) }

    Column {
        PasswordField(
            value = state.currentPassword,
            onValueChange = { viewModel.onCurrentPasswordChange(it) },
            label = "Current Password",
            visible = curVisible,
            onVisibleChange = { curVisible = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(
            value = state.newPassword,
            onValueChange = { viewModel.onNewPasswordChange(it) },
            label = "New Password",
            visible = newVisible,
            onVisibleChange = { newVisible = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(
            value = state.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            label = "Confirm Password",
            visible = conVisible,
            onVisibleChange = { conVisible = it }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.changePassword() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !state.isChangingPassword && state.currentPassword.isNotBlank() && state.newPassword.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isChangingPassword) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Update Password")
        }
    }
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String, visible: Boolean, onVisibleChange: (Boolean) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { onVisibleChange(!visible) }) {
                Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun NotificationPrefsSection(state: SettingsUiState, viewModel: SettingsViewModel) {
    Column {
        Text("Email Notifications", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        NotificationRow("Notices", state.notifPrefs.emailNotices) { viewModel.toggleNotifPref("email_notices", it) }
        NotificationRow("Marks", state.notifPrefs.emailMarks) { viewModel.toggleNotifPref("email_marks", it) }
        NotificationRow("Assignments", state.notifPrefs.emailAssignments) { viewModel.toggleNotifPref("email_assignments", it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Push Notifications", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        NotificationRow("Notices", state.notifPrefs.pushNotices) { viewModel.toggleNotifPref("push_notices", it) }
        NotificationRow("Marks", state.notifPrefs.pushMarks) { viewModel.toggleNotifPref("push_marks", it) }
        NotificationRow("Assignments", state.notifPrefs.pushAssignments) { viewModel.toggleNotifPref("push_assignments", it) }
        NotificationRow("Attendance", state.notifPrefs.pushAttendance) { viewModel.toggleNotifPref("push_attendance", it) }
    }
}

@Composable
fun NotificationRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun TwoFactorSection(state: SettingsUiState, viewModel: SettingsViewModel) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable Two-Factor Auth", style = MaterialTheme.typography.bodyLarge)
                Text("Receive a code when you log in", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (state.isUpdating2FA) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Switch(checked = state.twoFactorEnabled, onCheckedChange = { viewModel.setTwoFactor(it) })
        }
        
        if (state.twoFactorEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verification Method", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = state.twoFactorMethod == "email", onClick = { viewModel.setTwoFactor(true, "email") })
                Text("Email", modifier = Modifier.clickable { viewModel.setTwoFactor(true, "email") })
                Spacer(modifier = Modifier.width(24.dp))
                RadioButton(selected = state.twoFactorMethod == "phone", onClick = { viewModel.setTwoFactor(true, "phone") })
                Text("Phone", modifier = Modifier.clickable { viewModel.setTwoFactor(true, "phone") })
            }
        }
    }
}
