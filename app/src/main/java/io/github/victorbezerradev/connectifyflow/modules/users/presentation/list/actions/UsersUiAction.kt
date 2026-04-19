package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions

sealed interface UsersUiAction {
    data object ScreenStarted : UsersUiAction
}
