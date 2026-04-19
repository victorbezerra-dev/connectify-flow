package io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.states.WebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    url: String,
    state: WebViewState,
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )

                configureForBetterBatteryAndLifecycle()

                webViewClient =
                    object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?,
                        ) {
                            state.isLoading = true
                            state.canGoBack = view?.canGoBack() == true
                        }

                        override fun onPageFinished(
                            view: WebView?,
                            url: String?,
                        ) {
                            state.isLoading = false
                            state.canGoBack = view?.canGoBack() == true
                        }
                    }

                webChromeClient =
                    object : WebChromeClient() {
                        override fun onProgressChanged(
                            view: WebView?,
                            newProgress: Int,
                        ) {
                            state.progress = newProgress
                            state.isLoading = newProgress < 100
                        }
                    }

                state.webView = this
                loadUrl(url)
            }
        },
    )
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.configureForBetterBatteryAndLifecycle() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadsImagesAutomatically = true
        allowFileAccess = false
        allowContentAccess = false
        setSupportMultipleWindows(false)
        javaScriptCanOpenWindowsAutomatically = false
        cacheMode = WebSettings.LOAD_DEFAULT
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        builtInZoomControls = false
        displayZoomControls = false
        mediaPlaybackRequiresUserGesture = true
    }

    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = false
    overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
}
