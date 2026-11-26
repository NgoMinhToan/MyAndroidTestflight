package dev.minhtoan.mytestflight.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.minhtoan.mytestflight.feature.applist.AppListRoute
import dev.minhtoan.mytestflight.feature.applist.detail.AppDetailRoute
import dev.minhtoan.mytestflight.feature.settings.SettingsRoute

object Routes {
    const val Apps = "apps"
    const val Settings = "settings"
    const val AppDetail = "apps/{appId}"

    fun appDetail(appId: String): String = "apps/${Uri.encode(appId)}"
}

private const val NAV_DURATION_MS = 300

private fun slideIn(offsetX: (fullWidth: Int) -> Int): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing),
        initialOffsetX = offsetX,
    )

private fun slideOut(offsetX: (fullWidth: Int) -> Int): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing),
        targetOffsetX = offsetX,
    )

@Composable
fun TestflightNavHost(
    navController: NavHostController,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Tab switches restore a saved back stack; when that happens the restored destination
     * should appear without replaying its open animation.
     */
    suppressTransitions: Boolean = false,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Apps,
        modifier = modifier,
        enterTransition = {
            when {
                suppressTransitions -> EnterTransition.None
                targetState.destination.route == Routes.AppDetail -> slideIn { it }

                else -> EnterTransition.None
            }
        },
        exitTransition = {
            when {
                suppressTransitions -> ExitTransition.None
                targetState.destination.route == Routes.AppDetail -> slideOut { -it / 3 }

                else -> ExitTransition.None
            }
        },
        popEnterTransition = {
            when {
                suppressTransitions -> EnterTransition.None
                initialState.destination.route == Routes.AppDetail -> slideIn { -it / 3 }

                else -> EnterTransition.None
            }
        },
        popExitTransition = {
            when {
                suppressTransitions -> ExitTransition.None
                initialState.destination.route == Routes.AppDetail -> slideOut { it }

                else -> ExitTransition.None
            }
        },
        // Navigation Compose defaults predictive back to a scale-down preview. Mirror the
        // regular pop transitions so the gesture preview and the back button look identical.
        predictivePopEnterTransition = {
            when {
                initialState.destination.route == Routes.AppDetail &&
                    targetState.destination.route == Routes.Apps -> slideIn { -it / 3 }

                else -> EnterTransition.None
            }
        },
        predictivePopExitTransition = {
            when {
                initialState.destination.route == Routes.AppDetail &&
                    targetState.destination.route == Routes.Apps -> slideOut { it }

                else -> ExitTransition.None
            }
        },
    ) {
        composable(route = Routes.Apps) {
            AppListRoute(
                onOpenApp = { appId ->
                    navController.navigate(Routes.appDetail(appId))
                },
                onOpenSettings = onOpenSettings,
            )
        }
        composable(
            route = Routes.AppDetail,
            arguments = listOf(
                navArgument("appId") { type = NavType.StringType },
            ),
        ) {
            AppDetailRoute(onBack = { navController.popBackStack() })
        }
        composable(route = Routes.Settings) {
            SettingsRoute()
        }
    }
}
