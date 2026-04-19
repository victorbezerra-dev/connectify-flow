package io.github.victorbezerradev.connectifyflow.modules.users.presentation.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.victorbezerradev.connectifyflow.modules.users.domain.coordinators.UsersConnectionCoordinator
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.domain.repositories.UsersRepository
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.effects.UsersUiEffect
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.states.UsersUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        private var loadUsersJob: Job? = null
        private var hasLoadedUsers = false

        init {
            observeCoordinatorState()
        }

        fun onAction(action: UsersUiAction) {
            when (action) {
                UsersUiAction.ScreenStarted -> {
                    coordinator.start()

                    if (!hasLoadedUsers) {
                        loadUsers()
                    }
                }

                UsersUiAction.ScreenPaused -> {
                    coordinator.stop()
                }

                is UsersUiAction.OpenProfileClicked -> {
                    emitEffect(UsersUiEffect.NavigateToProfile(action.user))
                }

                is UsersUiAction.OpenWebPageClicked -> {
                    emitEffect(UsersUiEffect.OpenWebPage(action.url))
                }

                UsersUiAction.BackClicked -> {
                    emitEffect(UsersUiEffect.NavigateBack)
                }

                is UsersUiAction.EmailClicked -> {
                    emitEffect(UsersUiEffect.SendEmail(action.email))
                }

                is UsersUiAction.CallClicked -> {
                    emitEffect(UsersUiEffect.CallPhone(action.phone))
                }

                is UsersUiAction.ShowError -> {
                    emitEffect(UsersUiEffect.ShowSnackbar(action.message))
                }
            }
        }

        fun loadUsers() {
            loadUsersJob?.cancel()

            loadUsersJob =
                viewModelScope.launch {
                    showLoading()

                    try {
                        val users = usersRepository.getUsers()
                        onUsersLoaded(users)
                    } catch (e: CancellationException) {
                        Log.d(TAG, "Users loading cancelled")
                        throw e
                    } catch (e: IOException) {
                        handleIOException(e)
                    } catch (e: HttpException) {
                        handleHttpException(e)
                    } catch (
                        e:
                            @Suppress("TooGenericExceptionCaught")
                            RuntimeException,
                    ) {
                        handleUnexpectedException(e)
                    }
                }
        }

        private fun showLoading() {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
        }

        private fun onUsersLoaded(users: List<User>) {
            hasLoadedUsers = true

            _uiState.update {
                it.copy(
                    isLoading = false,
                    users = users,
                    errorMessage = null,
                )
            }
        }

        private fun handleIOException(exception: IOException) {
            Log.e(TAG, "IO Error: ${exception.message}", exception)
            showLoadError("Connection error while loading users.")
        }

        private fun handleHttpException(exception: HttpException) {
            Log.e(TAG, "HTTP Error: ${exception.code()} ${exception.message()}", exception)
            showLoadError("HTTP error while loading users.")
        }

        private fun handleUnexpectedException(exception: RuntimeException) {
            Log.e(TAG, "Unexpected Error: ${exception.message}", exception)
            showLoadError("An unexpected error occurred.")
        }

        private fun showLoadError(message: String) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    users = emptyList(),
                    errorMessage = message,
                )
            }
        }

        private fun observeCoordinatorState() {
            Log.i(TAG, "observeCoordinatorState called")
            if (observeCoordinatorJob?.isActive == true) return

            observeCoordinatorJob =
                viewModelScope.launch {
                    coordinator.uiState.collect { coordinatorState ->
                        Log.i(TAG, "Coordinator state updated: $coordinatorState")

                        _uiState.update {
                            it.copy(
                                connectionState = coordinatorState.connectionState,
                                communicationStatus = coordinatorState.communicationStatus,
                                heartbeatCountdown = coordinatorState.heartbeatCountdown,
                            )
                        }
                    }
                }
        }

        private fun emitEffect(effect: UsersUiEffect) {
            viewModelScope.launch {
                _uiEffect.emit(effect)
            }
        }

        override fun onCleared() {
            Log.i(TAG, "onCleared is called")
            loadUsersJob?.cancel()
            observeCoordinatorJob?.cancel()
            observeCoordinatorJob = null
            loadUsersJob = null
            super.onCleared()
        }

        companion object {
            private const val TAG = "UsersListViewModel"
        }
    }
