package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState

@Composable
fun StatusIndicator(
    status: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val color =
        when (status) {
            is ConnectionState.Connected -> Color(0xFF16A34A)
            is ConnectionState.Connecting -> Color(0xFFF59E0B)
            else -> Color(0xFFDC2626)
        }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Restart,
            ),
        label = "scale",
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Restart,
            ),
        label = "alpha",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2 * scale,
                alpha = alpha,
            )
            drawCircle(
                color = color,
                radius = size.minDimension / 2,
            )
        }
    }
}
