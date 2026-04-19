package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileImage(
    imageUrl: String?,
    userName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(4.dp, Color.White),
        shadowElevation = 8.dp,
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Profile picture of $userName",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = { ProfilePlaceholder(name = userName) },
            error = { ProfilePlaceholder(name = userName) },
        )
    }
}
