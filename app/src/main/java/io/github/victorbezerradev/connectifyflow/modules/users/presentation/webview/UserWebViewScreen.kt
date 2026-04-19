import android.util.Log
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.components.UserWebViewContent
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.components.UserWebViewTopBar
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.states.WebViewState

private const val TAG = "UserWebViewScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserWebViewScreen(
    url: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Profile Link",
) {
    val webViewState = rememberWebViewState()

    BackHandler(enabled = webViewState.canGoBack) {
        webViewState.webView?.goBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            UserWebViewTopBar(
                title = title,
                onBackClick = {
                    val webView = webViewState.webView
                    if (webView?.canGoBack() == true) {
                        webView.goBack()
                    } else {
                        onBackClick()
                    }
                },
                onReload = { webViewState.webView?.reload() },
            )
        },
    ) { padding ->
        UserWebViewContent(
            url = url,
            state = webViewState,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun rememberWebViewState(): WebViewState {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { WebViewState() }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                val webView = state.webView ?: return@LifecycleEventObserver

                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        webView.onResume()
                        webView.resumeTimers()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        webView.onPause()
                        webView.pauseTimers()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        webView.releaseSafely()
                        state.webView = null
                    }

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.webView?.releaseSafely()
            state.webView = null
        }
    }

    return state
}

fun WebView.releaseSafely() {
    try {
        stopLoading()
        onPause()
        pauseTimers()
        clearMatches()
        clearFocus()
        loadUrl("about:blank")
        removeAllViews()
        destroy()
    } catch (
        @Suppress("TooGenericExceptionCaught") exception: Exception,
    ) {
        Log.e(TAG, "Error releasing WebView", exception)
    }
}
