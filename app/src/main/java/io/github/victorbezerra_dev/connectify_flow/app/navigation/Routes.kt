package io.github.victorbezerra_dev.connectify_flow.app.navigation

sealed class Routes(val route: String) {

    data object UsersList : Routes("users_list")

    data object UserProfile : Routes("user_profile") {
        fun createRoute(userId: String) = "user_profile/$userId"
        const val routeWithArgs = "user_profile/userId"
    }

    data object UserWebView : Routes("user_webview")

}