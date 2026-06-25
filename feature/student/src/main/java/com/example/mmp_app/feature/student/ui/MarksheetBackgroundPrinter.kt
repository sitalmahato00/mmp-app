package com.example.mmp_app.feature.student.ui

import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.print.PrintAttributes
import android.print.PrintManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun MarksheetBackgroundPrinter(
    printUrl: String?,
    sessionCookie: String?,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    LaunchedEffect(printUrl, sessionCookie) {
        if (printUrl != null && sessionCookie != null) {
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    
                    if (url?.contains("/login") == true || url?.contains("sign-in") == true) {
                        onError("Authentication failed. Please try again.")
                        return
                    }
                    
                    // Page loaded successfully — inject JS cleanup and trigger print
                    val cleanupJs = """
                        (function() {
                            // Remove nav, sidebar, buttons, etc.
                            document.querySelectorAll('nav, .navbar, .sidebar, .btn, button, .no-print, header, footer, aside')
                                .forEach(el => el.remove());
                            // Force white background
                            document.body.style.backgroundColor = 'white';
                            document.body.style.margin = '0';
                            document.body.style.padding = '0';
                        })();
                    """.trimIndent()
                    
                    view?.evaluateJavascript(cleanupJs, null)
                    
                    // Delay to allow JS to apply, then trigger print
                    Handler(Looper.getMainLooper()).postDelayed({
                        triggerPrint(view!!)
                        onComplete()
                    }, 1500)
                }

                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    onError("Failed to load marksheet: $description")
                }
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)

            val domain = "mmp.sital.info.np"
            sessionCookie.split("||").forEach {
                val singleCookie = it.trim()
                if (singleCookie.isNotEmpty()) {
                    cookieManager.setCookie("https://$domain", singleCookie)
                }
            }
            cookieManager.flush()
            
            webView.loadUrl(printUrl)
        }
    }
}
