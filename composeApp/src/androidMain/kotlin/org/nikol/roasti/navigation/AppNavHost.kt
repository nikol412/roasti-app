package org.nikol.roasti.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.nikol.roasti.ui.components.BottomBar
import org.nikol.roasti.ui.features.recipepage.RecipeContentScreen
import org.nikol.roasti.ui.screens.FeedScreen
import org.nikol.roasti.ui.screens.LoginScreen
import org.nikol.roasti.ui.screens.ProfileScreen
import org.nikol.roasti.ui.screens.RecipesScreen

// 🔑 KMP: entire navigation lives in commonMain — will work on iOS without changes

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Recipes.route) {
                RecipesScreen {
                    navController.navigate(Screen.RecipeItem.createRoute(it))
                }
            }

            composable(
                route = Screen.RecipeItem.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                RecipeContentScreen(id = id, onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}
