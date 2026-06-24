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
    url: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: StudentViewModel = hiltViewModel()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val sessionCookie by viewModel.webSessionCookie.collectAsState()
    val error by viewModel.error.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

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
                            val printAdapter = webView.createPrintDocumentAdapter("marksheet_${System.currentTimeMillis()}")
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
                                setSupportZoom(true)
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Aggressive script to hide all web application layout and show only the marksheet content
                                    val script = """
                                        (function() {
                                            function hide(selector) {
                                                document.querySelectorAll(selector).forEach(function(el) {
                                                    el.style.setProperty('display', 'none', 'important');
                                                });
                                            }
                                            
                                            // Hide common UI elements
                                            ['nav', 'footer', 'header', 'aside', '.navbar', '.main-header', 
                                             '.main-footer', '.sidebar', '.bottom-nav', '.mobile-bottom-nav',
                                             '.fixed-bottom', '.no-print', '.breadcrumb', '.content-header',
                                             '#sidebar', '.sidebar-wrapper', '.btn', 'button'
                                            ].forEach(hide);

                                            // Force body reset
                                            document.body.style.setProperty('margin', '0', 'important');
                                            document.body.style.setProperty('padding', '0', 'important');
                                            document.body.style.setProperty('background-color', 'white', 'important');
                                            
                                            // Fix Laravel/Tailwind/Bootstrap specific wrappers
                                            var app = document.getElementById('app');
                                            if (app) {
                                                app.style.setProperty('background-color', 'white', 'important');
                                                app.style.setProperty('padding', '0', 'important');
                                            }
                                            
                                            var main = document.querySelector('main');
                                            if (main) {
                                                main.style.setProperty('margin', '0', 'important');
                                                main.style.setProperty('padding', '0', 'important');
                                                main.style.setProperty('margin-top', '0', 'important');
                                            }

                                            // Make the marksheet container take full width and hide shadows
                                            var card = document.querySelector('.card, .bg-white, .p-6, .max-w-4xl');
                                            if (card) {
                                                card.style.setProperty('box-shadow', 'none', 'important');
                                                card.style.setProperty('margin', '0', 'important');
                                                card.style.setProperty('width', '100%', 'important');
                                                card.style.setProperty('max-width', '100%', 'important');
                                                card.style.setProperty('padding', '10px', 'important');
                                            }
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(script, null)
                                }
                            }
                            
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setCookie(url, sessionCookie)
                            cookieManager.flush()
                            
                            webViewInstance = this
                            loadUrl(url)
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
