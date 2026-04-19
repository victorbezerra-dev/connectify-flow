package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import io.github.victorbezerradev.connectifyflow.R
import io.github.victorbezerradev.connectifyflow.core.websocket.ConnectionState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.CommunicationStatusState

@SuppressLint("ClickableViewAccessibility")
@Composable
fun ConnectionStatusAnimation(
    connectionState: ConnectionState,
    communicationStatus: CommunicationStatusState,
    modifier: Modifier = Modifier,
) {
    var targetSize by remember { mutableStateOf(0.dp) }
    val size by animateDpAsState(
        targetValue = targetSize,
        animationSpec =
            tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing,
            ),
        label = "size",
    )

    LaunchedEffect(Unit) {
        targetSize = 120.dp
    }

    val animationState =
        remember(connectionState, communicationStatus) {
            resolveRiveState(
                connectionState = connectionState,
                communicationStatus = communicationStatus,
            )
        }

    Box(
        modifier =
            modifier
                .size(size)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.size(size),
            factory = { context ->
                RiveAnimationView(context).apply {
                    setRiveResource(
                        resId = R.raw.boom_boop_ping,
                        stateMachineName = RiveConstants.STATE_MACHINE_NAME,
                    )
                    autoplay = true
                    isClickable = false
                    isFocusable = false
                    setOnTouchListener { _, _ -> true }
                }
            },
            update = { view ->
                view.setButtonOver(animationState.buttonOver)

                if (animationState.triggerClick) {
                    view.fireState(
                        RiveConstants.STATE_MACHINE_NAME,
                        RiveConstants.CLICK,
                    )
                }
            },
        )
    }
}

private object RiveConstants {
    const val STATE_MACHINE_NAME = "State Machine 1"
    const val CLICK = "Click"
    const val BUTTON_OVER = "Button_Over"
}

private data class RiveAnimationState(
    val buttonOver: Boolean,
    val triggerClick: Boolean,
)

private fun resolveRiveState(
    connectionState: ConnectionState,
    communicationStatus: CommunicationStatusState,
): RiveAnimationState =
    RiveAnimationState(
        buttonOver = connectionState is ConnectionState.Connected,
        triggerClick = communicationStatus is CommunicationStatusState.Sending,
    )

private fun RiveAnimationView.setButtonOver(value: Boolean) {
    setBooleanState(
        RiveConstants.STATE_MACHINE_NAME,
        RiveConstants.BUTTON_OVER,
        value,
    )
}
