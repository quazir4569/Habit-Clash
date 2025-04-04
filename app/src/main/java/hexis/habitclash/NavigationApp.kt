package hexis.habitclash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Main navigation component for the app.
 * Sets up navigation routes and connections between screens.
 */
@Composable
fun NavigationApp(modifier: Modifier = Modifier, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "Login_Screen"  // Start with login screen
    ) {
        // Authentication screens
        composable("Login_Screen") {
            LoginScreen(navController, authViewModel, themeViewModel)
        }

        composable("Registration_Screen") {
            RegistrationScreen(navController, authViewModel, themeViewModel)
        }

        // Main app screens
        composable("Dashboard_Screen") {
            DashboardScreen(navController, authViewModel, themeViewModel)
        }

        composable("Add_Habit") {
            AddHabitScreen(navController = navController, themeViewModel = themeViewModel)
        }

        // Edit habit screen with habit ID parameter
        composable(
            route = "Edit_Habit/{habitId}",
            arguments = listOf(
                navArgument("habitId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extract habit ID from navigation arguments
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""

            EditHabitScreen(
                navController = navController,
                themeViewModel = themeViewModel,
                habitId = habitId
            )
        }

        composable("Settings_Screen") {
            SettingsScreen(navController, authViewModel, themeViewModel)
        }
    }
}