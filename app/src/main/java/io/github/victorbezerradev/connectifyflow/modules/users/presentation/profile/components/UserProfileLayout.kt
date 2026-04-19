package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.victorbezerradev.connectifyflow.core.ui.themes.ScreenBackground
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.states.UserProfileUiState

@Composable
internal fun UserProfileLayout(
    uiState: UserProfileUiState,
    onAction: (UsersUiAction) -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(
                    top = uiState.statusBarPadding,
                    bottom = uiState.navigationBarPadding,
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ProfileHeaderCard(
            user = uiState.user,
            onImageClick = onImageClick,
        )

        InfoSection(
            title = "Information",
            items = uiState.infoItems,
        )

        ActionSection(
            hasPhone = uiState.actionsState.phone != null,
            hasProfileLink = uiState.actionsState.profileLink != null,
            onSendEmailClick = {
                onAction(UsersUiAction.EmailClicked(uiState.user.email))
            },
            onCallClick = {
                uiState.actionsState.phone?.let {
                    onAction(UsersUiAction.CallClicked(it))
                }
            },
            onOpenWebsiteClick = {
                uiState.actionsState.profileLink?.let {
                    onAction(UsersUiAction.OpenWebPageClicked(it))
                }
            },
        )
    }
}
