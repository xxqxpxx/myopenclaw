package com.myopenclaw.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShowChart
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
    data object Markets : BottomNavItem("markets", "Signals", Icons.Outlined.ShowChart)
    data object Insights : BottomNavItem("insights", "AI Chat", Icons.Outlined.Chat)
    data object Watchlist : BottomNavItem("watchlist", "Watchlist", Icons.Default.BookmarkBorder)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Markets,
        BottomNavItem.Insights,
        BottomNavItem.Watchlist,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Dark1,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val testTag = when(item) {
                is BottomNavItem.Home -> "bottom_nav_home"
                is BottomNavItem.Markets -> "bottom_nav_markets"
                is BottomNavItem.Insights -> "bottom_nav_insights"
                is BottomNavItem.Watchlist -> "bottom_nav_watchlist"
                is BottomNavItem.Profile -> "bottom_nav_profile"
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
