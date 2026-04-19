package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.effects

import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User

sealed interface UsersUiEffect {
    data class NavigateToProfile(val user: User) : UsersUiEffect

    data class OpenWebPage(val url: String) : UsersUiEffect

    data class ShowSnackbar(val message: String) : UsersUiEffect

    data class SendEmail(val email: String) : UsersUiEffect

    data class CallPhone(val phone: String) : UsersUiEffect

    data object NavigateBack : UsersUiEffect
}
