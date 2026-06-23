package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.StudentDashboard
import com.example.mmp_app.feature.parent.ui.ParentViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailsScreen(
    childId: Int,
    name: String,
    onBack: () -> Unit
) {
    val viewModel: ParentViewModel = hiltViewModel()

    val childData by viewModel.childDashboard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(childId) {
        viewModel.loadChildDashboard(childId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$name's Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && childData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                childData?.let {
                    StudentDashboard(data = it)
                }
            }
        }
    }
}
