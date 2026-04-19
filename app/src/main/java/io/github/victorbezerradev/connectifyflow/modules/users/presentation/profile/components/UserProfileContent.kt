package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.models.toInfoItems
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.models.toProfileActionsState
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.states.UserProfileUiState

@Composable
fun UserProfileContent(
    user: User,
    onAction: (UsersUiAction) -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    val uiState =
        UserProfileUiState(
            user = user,
            infoItems = user.toInfoItems(),
            actionsState = user.toProfileActionsState(),
            statusBarPadding = statusBarPadding.calculateTopPadding(),
            navigationBarPadding = navigationBarPadding.calculateBottomPadding(),
        )

    UserProfileLayout(
        uiState = uiState,
        onAction = onAction,
        onImageClick = onImageClick,
        modifier = modifier,
    )
}
