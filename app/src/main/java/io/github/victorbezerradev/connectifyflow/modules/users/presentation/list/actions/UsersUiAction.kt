package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions

import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User

sealed interface UsersUiAction {
    data object ScreenStarted : UsersUiAction

    data object ScreenPaused : UsersUiAction

    data class OpenProfileClicked(val user: User) : UsersUiAction

    data class OpenWebPageClicked(val url: String) : UsersUiAction

    data object BackClicked : UsersUiAction

    data class EmailClicked(val email: String) : UsersUiAction

    data class CallClicked(val phone: String) : UsersUiAction

    data class ShowError(val message: String) : UsersUiAction
}
