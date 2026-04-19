package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators.UsersConnectionCoordinator
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.UsersUiState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UsersListViewModel
    @Inject
    constructor(
        private val coordinator: UsersConnectionCoordinator,
    ) : ViewModel() {
        val uiState: StateFlow<UsersUiState> = coordinator.uiState

        fun onAction(action: UsersUiAction) {
            when (action) {
                UsersUiAction.ScreenStarted -> coordinator.start()
            }
        }

        override fun onCleared() {
            coordinator.stop()
            super.onCleared()
        }
    }
