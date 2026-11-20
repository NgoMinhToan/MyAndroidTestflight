package com.mycompany.testflight.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mycompany.testflight.R
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.TestflightColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FriendlyNavigationBar(
    selectedRoute: String?,
    onSelectApps: () -> Unit,
    onSelectSettings: () -> Unit,
) {
    val appsSelected = selectedRoute?.startsWith(Routes.Apps) == true
    val settingsSelected = selectedRoute == Routes.Settings
    val itemColors = ShortNavigationBarItemDefaults.colors(
        selectedIconColor = TestflightColors.CoralStrong,
        selectedTextColorTopIconPosition = TestflightColors.CoralStrong,
        selectedTextColorStartIconPosition = TestflightColors.CoralStrong,
        selectedIndicatorColor = TestflightColors.Cream,
        unselectedIconColor = TestflightColors.Muted,
        unselectedTextColor = TestflightColors.Muted,
    )

    Column {
        HorizontalDivider(thickness = 1.dp, color = TestflightColors.Line)
        ShortNavigationBar(
            containerColor = TestflightColors.White,
            contentColor = TestflightColors.Navy,
            windowInsets = ShortNavigationBarDefaults.windowInsets,
        ) {
            ShortNavigationBarItem(
                selected = appsSelected,
                onClick = onSelectApps,
                icon = {
                    Icon(
                        imageVector = if (appsSelected) Icons.Filled.Apps else Icons.Outlined.Apps,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.nav_apps),
                        style = FriendlyType.TabLabel,
                    )
                },
                colors = itemColors,
            )
            ShortNavigationBarItem(
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
                    Text(
                        text = stringResource(R.string.nav_settings),
                        style = FriendlyType.TabLabel,
                    )
                },
                colors = itemColors,
            )
        }
    }
}
