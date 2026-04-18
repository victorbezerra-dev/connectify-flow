package io.github.victorbezerradev.connectifyflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.victorbezerradev.connectifyflow.modules.users.presentation.list.UsersListScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.UsersList.route,
    ) {
        composable(Routes.UsersList.route) {
            UsersListScreen()
        }
    }
}
