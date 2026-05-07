package org.nikol.roasti.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import org.nikol.roasti.ui.components.LocalBottomBarScrollBehavior
import org.nikol.roasti.ui.components.rememberBottomBarScrollBehavior
import org.nikol.roasti.ui.features.auth.login.LoginRoute
import org.nikol.roasti.ui.features.auth.register.RegisterRoute
import org.nikol.roasti.ui.features.createrecipe.CreateRecipeRoute
import org.nikol.roasti.ui.features.editrecipe.EditRecipeRoute
import org.nikol.roasti.ui.features.profile.ProfileRoute
import org.nikol.roasti.ui.features.settings.SettingsRoute
import org.nikol.roasti.ui.features.recipepage.RecipeContentRoute
import org.nikol.roasti.ui.features.recipesteps.RecipeStepsRoute
import org.nikol.roasti.ui.screens.FeedRoute
import org.nikol.roasti.ui.screens.PostComposeRoute
import org.nikol.roasti.ui.screens.PostDetailRoute
import org.nikol.roasti.ui.screens.RecipesRoute
import org.nikol.roasti.ui.uikit.LoadingStub

private const val VerticalSlideDurationMillis = 280

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

    val bottomBarScrollBehavior = rememberBottomBarScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Feed.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    scrollBehavior = bottomBarScrollBehavior,
                )
            }
        },
    ) { innerPadding ->
        // Status bar is edge-to-edge: content draws under it; each screen's top bar (if any)
        // applies its own status-bar scrim via TopAppBar's default windowInsets.
        // Bottom bar inset (innerPadding.bottom) is forwarded to each route so their lists
        // can scroll under the bar while padding their last items above it.
        CompositionLocalProvider(
            LocalBottomBarScrollBehavior provides bottomBarScrollBehavior,
        ) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Feed.route,
                ) {
                    tabComposable(Screen.Feed.route) {
                        FeedRoute(
                            contentPadding = innerPadding,
                            onPostClick = { postId ->
                                navController.navigate(Screen.PostDetail.createRoute(postId))
                            },
                            onCreatePost = {
                                navController.navigate(Screen.PostCompose.createRoute())
                            },
                            onEditPost = { postId ->
                                navController.navigate(Screen.PostCompose.createRoute(postId))
                            },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                        )
                    }

                tabComposable(Screen.Recipes.route) {
                    RecipesRoute(
                        contentPadding = innerPadding,
                        onRecipeClick = { navController.navigate(Screen.RecipeItem.createRoute(it)) },
                        onCreateClick = { navController.navigate(Screen.CreateRecipe.route) },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }

                composable(Screen.CreateRecipe.route) {
                    CreateRecipeRoute(
                        onBackClick = { navController.popBackStack() },
                    )
                }

                tabComposable(Screen.Profile.route) {
                    ProfileRoute(
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        contentPadding = innerPadding,
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsRoute(
                        onBackClick = { navController.popBackStack() },
                        contentPadding = innerPadding,
                    )
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
                    route = Screen.PostCompose.route,
                    arguments = listOf(
                        navArgument(Screen.PostCompose.ARG_POST_ID) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
                ) { entry ->
                    val id = entry.arguments?.getString(Screen.PostCompose.ARG_POST_ID)
                    PostComposeRoute(
                        postId = id,
                        onClose = { navController.popBackStack() },
                    )
                }

                composable(
                    route = Screen.PostDetail.route,
                    arguments = listOf(navArgument(Screen.PostDetail.ARG_ID) { type = NavType.StringType }),
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(VerticalSlideDurationMillis),
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(VerticalSlideDurationMillis),
                        )
                    },
                ) { entry ->
                    val id = entry.arguments?.getString(Screen.PostDetail.ARG_ID) ?: return@composable
                    PostDetailRoute(
                        postId = id,
                        onClose = { navController.popBackStack() },
                        onEditPost = { postId ->
                            navController.navigate(Screen.PostCompose.createRoute(postId))
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
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
        }
    }
}
