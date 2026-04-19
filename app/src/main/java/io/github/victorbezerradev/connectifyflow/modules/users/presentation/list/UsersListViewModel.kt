package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators.UsersConnectionCoordinator
import io.github.victorbezerradev.connectifyflow.modules.users.domain.repositories.UsersRepository
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.effects.UsersUiEffect
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.UsersUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class UsersListViewModel
    @Inject
    constructor(
        private val coordinator: UsersConnectionCoordinator,
        private val usersRepository: UsersRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(UsersUiState())
        val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

        private val _uiEffect = MutableSharedFlow<UsersUiEffect>()
        val uiEffect: SharedFlow<UsersUiEffect> = _uiEffect.asSharedFlow()

        private var observeCoordinatorJob: Job? = null

        init {
            observeCoordinatorState()
            loadUsers()
        }

        fun onAction(action: UsersUiAction) {
            when (action) {
                UsersUiAction.ScreenStarted -> {
                    coordinator.start()
                    loadUsers()
                }

                is UsersUiAction.OpenProfileClicked -> {
                    viewModelScope.launch {
                        _uiEffect.emit(UsersUiEffect.NavigateToProfile(action.user))
                    }
                }

                is UsersUiAction.OpenWebPageClicked -> {
                    viewModelScope.launch {
                        _uiEffect.emit(UsersUiEffect.OpenWebPage(action.url))
                    }
                }

                UsersUiAction.BackClicked -> {
                    viewModelScope.launch {
                        _uiEffect.emit(UsersUiEffect.NavigateBack)
                    }
                }

                is UsersUiAction.EmailClicked -> {
                    viewModelScope.launch {
                        _uiEffect.emit(UsersUiEffect.SendEmail(action.email))
                    }
                }

                is UsersUiAction.CallClicked -> {
                    viewModelScope.launch {
                        _uiEffect.emit(UsersUiEffect.CallPhone(action.phone))
                    }
                }

                is UsersUiAction.ShowError -> {
                    viewModelScope.launch {
                        _uiEffect.emit(UsersUiEffect.ShowSnackbar(action.message))
                    }
                }
            }
        }

        fun loadUsers() {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = true,
                        errorMessage = null,
                    )

                _uiState.value =
                    try {
                        val users = usersRepository.getUsers()

                        _uiState.value.copy(
                            isLoading = false,
                            users = users,
                            errorMessage = null,
                        )
                    } catch (e: IOException) {
                        Log.e("UsersListViewModel", "IO Error: ${e.message}", e)
                        _uiState.value.copy(
                            isLoading = false,
                            users = emptyList(),
                            errorMessage = "Connection error while loading users.",
                        )
                    } catch (e: HttpException) {
                        Log.e("UsersListViewModel", "HTTP Error: ${e.code()} ${e.message()}", e)
                        _uiState.value.copy(
                            isLoading = false,
                            users = emptyList(),
                            errorMessage = "HTTP error while loading users.",
                        )
                    } catch (e: IllegalArgumentException) {
                        Log.e("UsersListViewModel", "Validation Error: ${e.message}", e)
                        _uiState.value.copy(
                            isLoading = false,
                            users = emptyList(),
                            errorMessage = "The received data is invalid.",
                        )
                    } catch (
                        @Suppress("TooGenericExceptionCaught") e: Exception,
                    ) {
                        Log.e("UsersListViewModel", "Unknown Error: ${e.message}", e)
                        _uiState.value.copy(
                            isLoading = false,
                            users = emptyList(),
                            errorMessage = "An unexpected error occurred.",
                        )
                    }
            }
        }

        private fun observeCoordinatorState() {
            observeCoordinatorJob?.cancel()

            observeCoordinatorJob =
                viewModelScope.launch {
                    coordinator.uiState.collectLatest { coordinatorState ->
                        _uiState.value =
                            _uiState.value.copy(
                                connectionState = coordinatorState.connectionState,
                                communicationStatus = coordinatorState.communicationStatus,
                                heartbeatCountdown = coordinatorState.heartbeatCountdown,
                            )
                    }
                }
        }

        override fun onCleared() {
            observeCoordinatorJob?.cancel()
            coordinator.stop()
            super.onCleared()
        }
    }
