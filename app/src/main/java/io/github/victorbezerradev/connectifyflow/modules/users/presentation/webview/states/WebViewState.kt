package io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.states

import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class WebViewState {
    var isLoading by mutableStateOf(true)
    var progress by mutableIntStateOf(0)
    var canGoBack by mutableStateOf(false)
    var webView: WebView? by mutableStateOf(null)
}
