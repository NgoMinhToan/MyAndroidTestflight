package com.mycompany.testflight.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mycompany.testflight.R

/**
 * Material 3 bottom navigation for the app's top-level destinations. Colors, indicator,
 * label typography and window insets all come from the theme, so the bar stays consistent
 * with every other Material 3 surface in the app.
 */
@Composable
fun FriendlyNavigationBar(
    selectedRoute: String?,
    onSelectApps: () -> Unit,
    onSelectSettings: () -> Unit,
) {
    val appsSelected = selectedRoute?.startsWith(Routes.Apps) == true
    val settingsSelected = selectedRoute == Routes.Settings

    NavigationBar {
        NavigationBarItem(
            selected = appsSelected,
            onClick = onSelectApps,
            icon = {
                Icon(
                    imageVector = if (appsSelected) Icons.Filled.Apps else Icons.Outlined.Apps,
                    contentDescription = null,
                )
            },
            label = {
                Text(text = stringResource(R.string.nav_apps))
            },
        )
        NavigationBarItem(
            selected = settingsSelected,
            onClick = onSelectSettings,
            icon = {
                Icon(
                    imageVector = if (settingsSelected) {
                        Icons.Filled.Settings
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = null,
                )
            },
            label = {
                Text(text = stringResource(R.string.nav_settings))
            },
        )
    }
}
