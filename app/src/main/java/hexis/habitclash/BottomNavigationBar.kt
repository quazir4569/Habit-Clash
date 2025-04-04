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
 * Bottom navigation bar for app-wide navigation.
 * Provides quick access to main app screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController, isDarkMode: Boolean = false) {
    // Get current route to highlight active tab
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colors = getAppThemeColors(isDarkMode)

    // Navigation items configuration
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

    // Navigation bar with rounded corners
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
            // Create navigation items
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
 * Data class representing a bottom navigation item.
 * Stores information needed to display and navigate to a screen.
 */
data class BottomNavItem(
    val title: String,   // Display text
    val icon: androidx.compose.ui.graphics.vector.ImageVector,  // Icon
    val route: String    // Navigation route
)