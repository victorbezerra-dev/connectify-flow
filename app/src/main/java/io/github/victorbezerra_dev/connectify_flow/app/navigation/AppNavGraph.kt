package io.github.victorbezerra_dev.connectify_flow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.UsersList.route
    ) {
        composable(Routes.UsersList.route) {

        }
    }
}