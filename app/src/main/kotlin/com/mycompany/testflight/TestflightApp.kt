package com.mycompany.testflight

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mycompany.testflight.designsystem.tokens.TestflightColors
import com.mycompany.testflight.navigation.FriendlyNavigationBar
import com.mycompany.testflight.navigation.Routes
import com.mycompany.testflight.navigation.TestflightNavHost

@Composable
fun TestflightApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var suppressTabTransitions by remember { mutableStateOf(false) }

    // Returning to a tab restores that tab's saved back stack. That is not a navigation the
    // user just performed, so it should snap into place instead of replaying the restored
    // destination's enter animation. The flag is cleared once the target has composed.
    LaunchedEffect(currentRoute) {
        if (suppressTabTransitions) {
            withFrameNanos { }
            suppressTabTransitions = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TestflightColors.Cream,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            FriendlyNavigationBar(
                selectedRoute = currentRoute,
                onSelectApps = {
                    when (currentRoute) {
                        Routes.Apps -> Unit
                        // Already inside the apps section: return to the catalog instead of
                        // restoring the detail through a fresh navigation.
                        Routes.AppDetail ->
                            navController.popBackStack(Routes.Apps, inclusive = false)

                        else -> {
                            suppressTabTransitions = true
                            navController.navigateToTab(Routes.Apps)
                        }
                    }
                },
                onSelectSettings = {
                    if (currentRoute != Routes.Settings) {
                        suppressTabTransitions = true
                        navController.navigateToTab(Routes.Settings)
                    }
                },
            )
        },
    ) { innerPadding ->
        TestflightNavHost(
            navController = navController,
            onOpenSettings = { navController.navigateToTab(Routes.Settings) },
            suppressTransitions = suppressTabTransitions,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
