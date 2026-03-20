package com.myopenclaw.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.Dark1
import com.myopenclaw.ui.theme.Green2
import com.myopenclaw.ui.theme.White

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconVector: ImageVector
) {
    data object Home : BottomNavItem("home", "Home", Icons.Outlined.Home)
    data object Explore : BottomNavItem("explore", "Explore", Icons.Outlined.Explore)
    data object Files : BottomNavItem("files", "Files", Icons.Outlined.FolderOpen)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Outlined.Settings)
}

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Files,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = Dark1,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val testTag = when(item) {
                is BottomNavItem.Home -> "bottom_nav_home"
                is BottomNavItem.Explore -> "bottom_nav_explore"
                is BottomNavItem.Files -> "bottom_nav_files"
                is BottomNavItem.Settings -> "bottom_nav_settings"
            }
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) },
                modifier = Modifier.testTag(testTag),
                icon = {
                    Icon(
                        imageVector = item.iconVector,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Green2,
                    selectedTextColor = Green2,
                    indicatorColor = Green2.copy(alpha = 0.15f),
                    unselectedIconColor = White.copy(alpha = 0.5f),
                    unselectedTextColor = White.copy(alpha = 0.5f)
                )
            )
        }
    }
}
