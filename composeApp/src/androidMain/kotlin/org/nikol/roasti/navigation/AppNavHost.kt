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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import org.nikol.roasti.feature.auth.domain.model.AuthState
import org.nikol.roasti.ui.components.BottomBar
import org.nikol.roasti.ui.features.auth.login.LoginRoute
import org.nikol.roasti.ui.features.auth.register.RegisterRoute
import org.nikol.roasti.ui.features.createrecipe.CreateRecipeRoute
import org.nikol.roasti.ui.features.editrecipe.EditRecipeRoute
import org.nikol.roasti.ui.features.profile.ProfileRoute
import org.nikol.roasti.ui.features.recipepage.RecipeContentRoute
import org.nikol.roasti.ui.features.recipesteps.RecipeStepsRoute
import org.nikol.roasti.ui.screens.FeedRoute
import org.nikol.roasti.ui.screens.RecipesRoute
import org.nikol.roasti.ui.uikit.LoadingStub

private val BottomBarHeight = 80.dp

@Composable
fun AppNavHost(
) {
    val viewModel: AppNavigationViewModel = koinViewModel()
    val authState = viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.bootstrap()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (authState.value) {
            AuthState.Loading -> LoadingStub(modifier = Modifier.fillMaxSize())
            is AuthState.Error, AuthState.Guest -> AuthNavHost()
            is AuthState.Authenticated -> MainNavHost()
        }
    }
}

@Composable
private fun AuthNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterRoute(
                onNavigateToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Login.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MainNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }
    val navigationBarBottomInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val mainScreenContentPadding = PaddingValues(bottom = BottomBarHeight + navigationBarBottomInset)

    Box(modifier = Modifier.fillMaxSize()) {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Feed.route,
            ) {
                composable(Screen.Feed.route) {
                    FeedRoute(contentPadding = mainScreenContentPadding)
                }

                composable(Screen.Recipes.route) {
                    RecipesRoute(
                        contentPadding = mainScreenContentPadding,
                        onRecipeClick = { navController.navigate(Screen.RecipeItem.createRoute(it)) },
                        onCreateClick = { navController.navigate(Screen.CreateRecipe.route) },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                    )
                }

                composable(Screen.CreateRecipe.route) {
                    CreateRecipeRoute(
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileRoute(contentPadding = mainScreenContentPadding)
                }

                composable(
                    route = Screen.RecipeItem.route,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    RecipeContentRoute(
                        id = id,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { navController.navigate(Screen.EditRecipe.createRoute(id)) },
                        onStartBrewing = { startStep ->
                            navController.navigate(Screen.RecipeSteps.createRoute(id, startStep))
                        },
                    )
                }

                composable(
                    route = Screen.EditRecipe.route,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    EditRecipeRoute(
                        id = id,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable(
                    route = Screen.RecipeSteps.route,
                    arguments = listOf(
                        navArgument("id") { type = NavType.StringType },
                        navArgument("startStep") { type = NavType.IntType },
                    )
                ) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    val startStep = entry.arguments?.getInt("startStep") ?: 0
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
