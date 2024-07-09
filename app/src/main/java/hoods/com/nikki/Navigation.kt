package hoods.com.nikki

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import hoods.com.nikki.ui.HomeScreen
import hoods.com.nikki.ui.login.LoginScreen
import hoods.com.nikki.ui.signup.PolicyScreen
import hoods.com.nikki.ui.signup.PricacyScreen
import hoods.com.nikki.ui.signup.SignUpScreen

sealed class Route(val path: String) {
    object LoginScreen : Route("Login")
    object SignUpScreen : Route("SignUp")
    object PrivacyScreen : Route("Privacy")
    object PolicyScreen : Route("Policy")
    data class HomeScreen(val email: String) : Route("Home?email=$email")
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyNavigation(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = "login_flow",
    ) {
        navigation(startDestination = Route.LoginScreen.path, route = "login_flow") {
            composable(route = Route.LoginScreen.path) {
                LoginScreen(
                    onLoginClick = { email ->
                        if (email != null) {
                            navHostController.navigate(
                                Route.HomeScreen(email).path
                            ) {
                                popUpTo("login_flow") {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onSignUpClick = {
                        navHostController.navigateToSingleTop(
                            Route.SignUpScreen.path
                        )
                    }
                )
            }
            composable(route = Route.SignUpScreen.path) {
                SignUpScreen(
                    onSignUpClick = { email ->
                        if (email != null) {
                            navHostController.navigate(
                                Route.HomeScreen(email).path
                            ) {
                                popUpTo("login_flow") {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onLoginClick = {
                        navHostController.navigateToSingleTop(
                            Route.LoginScreen.path
                        )
                    },
                    onPrivacyClick = {
                        navHostController.navigate(
                            Route.PrivacyScreen.path
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onPolicyClick = {
                        navHostController.navigate(
                            Route.PolicyScreen.path
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(route = Route.PolicyScreen.path) {
                PolicyScreen {
                    navHostController.navigateUp()
                }
            }
            composable(route = Route.PrivacyScreen.path) {
                PricacyScreen {
                    navHostController.navigateUp()
                }
            }
        }
        composable(
            route = "Home?email={email}",
            arguments = listOf(navArgument("email") { defaultValue = "" })
        ) { navBackStackEntry ->
            val email = navBackStackEntry.arguments?.getString("email") ?: ""
            HomeScreen(navController = navHostController, email = email)
        }
    }
}

fun NavController.navigateToSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
