package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage

@Composable
fun FullScreenImageModal(
    imageUrl: String?,
    userName: String,
    onDismiss: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    FullScreenImageDialog(onDismiss = onDismiss) {
        FullScreenImageContent(
            imageUrl = imageUrl,
            userName = userName,
            scale = scale,
            offset = offset,
            onTransform = { pan, zoom ->
                val newScale = (scale * zoom).coerceIn(1f, 5f)
                scale = newScale
                offset =
                    if (newScale > 1f) {
                        offset + pan
                    } else {
                        Offset.Zero
                    }
            },
        )

        CloseButton(
            onDismiss = onDismiss,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .padding(top = 32.dp),
        )
    }
}

@Composable
private fun FullScreenImageDialog(
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@Composable
private fun FullScreenImageContent(
    imageUrl: String?,
    userName: String,
    scale: Float,
    offset: Offset,
    onTransform: (pan: Offset, zoom: Float) -> Unit,
) {
    if (imageUrl != null) {
        ZoomableAsyncImage(
            imageUrl = imageUrl,
            scale = scale,
            offset = offset,
            onTransform = onTransform,
            onError = {
                FullScreenPlaceholder(userName)
            },
        )
    } else {
        FullScreenPlaceholder(userName)
    }
}

@Composable
private fun ZoomableAsyncImage(
    imageUrl: String,
    scale: Float,
    offset: Offset,
    onTransform: (pan: Offset, zoom: Float) -> Unit,
    onError: @Composable () -> Unit,
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = "Full screen image",
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onTransform(pan, zoom)
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        contentScale = ContentScale.Fit,
        error = {
            onError()
        },
    )
}

@Composable
private fun CloseButton(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onDismiss,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
        )
    }
}

@Composable
private fun FullScreenPlaceholder(name: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
        )
    }
}
