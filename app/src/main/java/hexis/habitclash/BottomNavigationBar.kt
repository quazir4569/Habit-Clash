package hexis.habitclash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Bottom navigation bar used across the app.
 * Handles moving between main screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController, isDarkMode: Boolean = false) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colors = getAppThemeColors(isDarkMode)

    // List of navigation items
    val items = listOf(
        BottomNavItem(
            title = "Dashboard",
            icon = Icons.Default.Home,
            route = "Dashboard_Screen"
        ),
        BottomNavItem(
            title = "Add Habit",
            icon = Icons.Default.Add,
            route = "Add_Habit"
        ),
        BottomNavItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            route = "Settings_Screen"
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        shadowElevation = 8.dp,
        color = colors.fieldContainerColor
    ) {
        NavigationBar(
            modifier = Modifier.height(80.dp),
            containerColor = colors.backgroundColor,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                    },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    onClick = {
                        // Only navigate if not already on this screen
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Clear back stack to avoid piling up screens
                                popUpTo("Dashboard_Screen") {
                                    saveState = false
                                    inclusive = (item.route == "Dashboard_Screen")
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.accentColor,
                        selectedTextColor = colors.accentColor,
                        indicatorColor = colors.backgroundColor,
                        unselectedIconColor = colors.secondaryTextColor,
                        unselectedTextColor = colors.secondaryTextColor
                    )
                )
            }
        }
    }
}

/**
 * Data for a bottom navigation item.
 */
data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)