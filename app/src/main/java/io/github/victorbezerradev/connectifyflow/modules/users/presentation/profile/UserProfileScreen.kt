package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.components.FullScreenImageModal
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components.ProfileTopBar
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components.UserProfileContent

@Composable
fun UserProfileScreen(
    user: User,
    onAction: (UsersUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isImageModalVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileTopBar(
                onBackClick = { onAction(UsersUiAction.BackClicked) },
            )
        },
    ) { innerPadding ->
        UserProfileContent(
            user = user,
            onAction = onAction,
            onImageClick = { isImageModalVisible = true },
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (isImageModalVisible) {
        FullScreenImageModal(
            imageUrl = user.profileImageUrl,
            userName = user.name,
            onDismiss = { isImageModalVisible = false },
        )
    }
}
