package io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.states.WebViewState

@Composable
fun UserWebViewContent(
    url: String,
    state: WebViewState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        WebViewContainer(
            url = url,
            state = state,
        )

        if (state.isLoading || (state.progress < 100)) {
            LoadingOverlay()
        }
    }
}
