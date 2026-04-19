package io.github.victorbezerradev.connectifyflow.app.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.victorbezerradev.connectifyflow.modules.users.domain.models.User
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.UsersListScreen
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.UsersListViewModel
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.actions.UsersUiAction
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.effects.UsersUiEffect
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.profile.UserProfileScreen
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.webview.UserWebViewScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val TAG = "AppNavGraph"

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.UsersList.route,
    ) {
        composable(Routes.UsersList.route) {
            val viewModel: UsersListViewModel = hiltViewModel()
            HandleUsersListEffects(viewModel, navController, context)

            UsersListScreen(viewModel = viewModel)
        }

        composable(
            route = Routes.UserProfile.route + "/{userJson}",
            arguments = listOf(navArgument("userJson") { type = NavType.StringType }),
        ) { backStackEntry ->
            val viewModel: UsersListViewModel = hiltViewModel()
            val user = decodeUser(backStackEntry.arguments?.getString("userJson"))
            HandleProfileEffects(viewModel, navController, context)

            UserProfileScreen(user = user, onAction = viewModel::onAction)
        }

        composable(
            route = Routes.UserWebView.route + "/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType }),
        ) { backStackEntry ->
            val url =
                URLDecoder.decode(
                    backStackEntry.arguments?.getString("url") ?: "",
                    StandardCharsets.UTF_8.toString(),
                )
            UserWebViewScreen(
                url = url,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun HandleUsersListEffects(
    viewModel: UsersListViewModel,
    navController: NavHostController,
    context: Context,
) {
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is UsersUiEffect.NavigateToProfile -> navigateToProfile(navController, effect.user)
                is UsersUiEffect.OpenWebPage -> navigateToWebView(navController, effect.url)
                is UsersUiEffect.ShowSnackbar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun HandleProfileEffects(
    viewModel: UsersListViewModel,
    navController: NavHostController,
    context: Context,
) {
    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is UsersUiEffect.NavigateBack -> navController.popBackStack()
                is UsersUiEffect.SendEmail ->
                    launchIntent(
                        context = context,
                        intent =
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:${effect.email}".toUri()
                            },
                        viewModel = viewModel,
                        errorType = "email app",
                    )
                is UsersUiEffect.CallPhone ->
                    launchIntent(
                        context = context,
                        intent =
                            Intent(Intent.ACTION_DIAL).apply {
                                data = "tel:${effect.phone}".toUri()
                            },
                        viewModel = viewModel,
                        errorType = "dialer",
                    )
                is UsersUiEffect.OpenWebPage ->
                    launchIntent(
                        context = context,
                        intent = Intent(Intent.ACTION_VIEW, effect.url.toUri()),
                        viewModel = viewModel,
                        errorType = "website",
                    )
                is UsersUiEffect.ShowSnackbar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }
}

private fun navigateToProfile(
    navController: NavHostController,
    user: User,
) {
    val userJson = Json.encodeToString(user)
    val encodedJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
    navController.navigate(Routes.UserProfile.route + "/$encodedJson")
}

private fun navigateToWebView(
    navController: NavHostController,
    url: String,
) {
    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
    navController.navigate(Routes.UserWebView.route + "/$encodedUrl")
}

private fun decodeUser(encodedJson: String?): User {
    val userJson = URLDecoder.decode(encodedJson ?: "", StandardCharsets.UTF_8.toString())
    return Json.decodeFromString(userJson)
}

private fun launchIntent(
    context: Context,
    intent: Intent,
    viewModel: UsersListViewModel,
    errorType: String,
) {
    runCatching {
        context.startActivity(intent)
    }.onFailure { throwable ->
        Log.e(TAG, "Failed to open $errorType", throwable)

        if (throwable is ActivityNotFoundException) {
            viewModel.onAction(
                UsersUiAction.ShowError("Could not open $errorType"),
            )
        } else {
            viewModel.onAction(
                UsersUiAction.ShowError("Unexpected error while opening $errorType"),
            )
        }
    }
}
