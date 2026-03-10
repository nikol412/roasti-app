package org.nikol.roasti.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.nikol.roasti.ui.components.BottomBar
import org.nikol.roasti.ui.features.recipepage.RecipeContentRoute
import org.nikol.roasti.ui.features.recipesteps.RecipeStepsRoute
import org.nikol.roasti.ui.screens.FeedRoute
import org.nikol.roasti.ui.screens.LoginScreen
import org.nikol.roasti.ui.screens.ProfileRoute
import org.nikol.roasti.ui.screens.RecipesRoute

private val BottomBarHeight = 80.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }
    val navigationBarBottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val mainScreenContentPadding = PaddingValues(bottom = BottomBarHeight + navigationBarBottomInset)

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route,
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

                    composable(Screen.Feed.route) {
                        FeedRoute(contentPadding = mainScreenContentPadding)
                    }

                    composable(Screen.Recipes.route) {
                        RecipesRoute(
                            contentPadding = mainScreenContentPadding,
                            onRecipeClick = { navController.navigate(Screen.RecipeItem.createRoute(it)) },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileRoute(contentPadding = mainScreenContentPadding)
                    }

                    composable(
                        route = Screen.RecipeItem.route,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: return@composable
                        RecipeContentRoute(
                            id = id,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                            onBackClick = { navController.popBackStack() },
                            onStartBrewing = { startStep ->
                                navController.navigate(Screen.RecipeSteps.createRoute(id, startStep))
                            },
                        )
                    }

                    composable(
                        route = Screen.RecipeSteps.route,
                        arguments = listOf(
                            navArgument("id") { type = NavType.StringType },
                            navArgument("startStep") { type = NavType.IntType },
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: return@composable
                        val startStep = backStackEntry.arguments?.getInt("startStep") ?: 0
                        RecipeStepsRoute(
                            id = id,
                            startStep = startStep,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable,
                            onBackClick = { navController.popBackStack() },
                        )
                    }
                }
            }

            if (showBottomBar) {
                BottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Feed.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}
