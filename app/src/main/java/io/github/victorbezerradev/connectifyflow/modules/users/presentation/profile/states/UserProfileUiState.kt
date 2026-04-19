package io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.states

import androidx.compose.ui.unit.Dp
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.models.InfoItemData

data class UserProfileUiState(
    val user: User,
    val infoItems: List<InfoItemData>,
    val actionsState: ProfileActionsState,
    val statusBarPadding: Dp,
    val navigationBarPadding: Dp,
)
