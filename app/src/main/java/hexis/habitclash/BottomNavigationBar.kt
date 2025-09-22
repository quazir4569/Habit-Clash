package hexis.habitclash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun BottomNavigationBar(
    navController: NavController,
    isDarkMode: Boolean
) {
    // NOTE: Routes must match your NavHost composable routes exactly.
    val items = listOf(
        BottomItem("Dashboard_Screen", Icons.Filled.Home, "Dashboard"),
        BottomItem("AddHabit_Screen", Icons.Filled.Add, "Add"),
        BottomItem("Analytics_Screen", Icons.Filled.Info, "Analytics"), // <- new tab with Info icon
        BottomItem("Settings_Screen", Icons.Filled.Settings, "Settings")
    )

    NavigationBar {
        val currentDest: NavDestination? = navController.currentBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDest?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            if (item.route == "Dashboard_Screen") {
                                popUpTo("Dashboard_Screen") { inclusive = false }
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private data class BottomItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
