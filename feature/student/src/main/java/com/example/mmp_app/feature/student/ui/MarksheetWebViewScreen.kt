package com.example.mmp_app.feature.student.ui

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksheetWebViewScreen(
    examId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: StudentViewModel = hiltViewModel()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val sessionCookie by viewModel.webSessionCookie.collectAsState()
    val error by viewModel.error.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val marksheetUrl = "https://mmp.sital.info.np/student/marks/$examId"

    LaunchedEffect(Unit) {
        viewModel.performWebLogin()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Official Marksheet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        webViewInstance?.let { webView ->
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            val printAdapter = webView.createPrintDocumentAdapter("marksheet_exam_$examId")
                            printManager.print("Marksheet", printAdapter, PrintAttributes.Builder().build())
                        }
                    }) {
                        Icon(Icons.Rounded.Print, contentDescription = "Print")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (sessionCookie != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = true
                                displayZoomControls = false
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                }
                            }
                            
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setCookie("mmp.sital.info.np", sessionCookie)
                            
                            webViewInstance = this
                            loadUrl(marksheetUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Alignment.Center.let { Modifier.align(it) })
            }
            
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }
    }
}
