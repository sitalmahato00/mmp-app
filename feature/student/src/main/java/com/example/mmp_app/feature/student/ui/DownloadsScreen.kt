package com.example.mmp_app.feature.student.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mmp_app.core.ui.SkeletonBox
import com.example.mmp_app.domain.model.SubjectDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val downloads by viewModel.downloads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Syllabus", "Notes", "Lab Manual", "Question", "Form", "Circular")

    LaunchedEffect(Unit) {
        viewModel.loadStudentDownloads()
    }

    val filteredDownloads = remember(downloads, selectedCategory) {
        if (selectedCategory == "All") {
            downloads
        } else {
            downloads.filter { 
                it.category?.replace("_", " ")?.lowercase() == selectedCategory.lowercase() 
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Materials") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            if (isLoading && downloads.isEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) {
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
                    }
                }
            } else if (filteredDownloads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CloudOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("No resources found in this category", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDownloads) { download ->
                        DownloadItemCard(download)
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(download: SubjectDocument) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (download.file_type?.lowercase() == "pdf") Icons.Rounded.PictureAsPdf else Icons.Rounded.Description,
                    contentDescription = null,
                    tint = if (download.file_type?.lowercase() == "pdf") Color.Red else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    download.category?.let { cat ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = cat.replace("_", " ").uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            val desc = download.description
            if (!desc.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(download.file_url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Open", fontSize = 13.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        downloadFile(context, download)
                    },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Download", fontSize = 13.sp)
                }
            }
        }
    }
}

fun downloadFile(context: Context, download: SubjectDocument) {
    try {
        val request = DownloadManager.Request(Uri.parse(download.file_url))
            .setTitle(download.title)
            .setDescription("Downloading study material...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, 
                "${download.title.replace(" ", "_")}.${download.file_type ?: "pdf"}"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
