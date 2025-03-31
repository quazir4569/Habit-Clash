package hexis.habitclash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Sets up app navigation.
 * Connects all screens and handles routing.
 */
@Composable
fun NavigationApp(modifier: Modifier = Modifier, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "Login_Screen"
    ) {
        // Auth screens
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

        // Edit habit screen
        composable(
            route = "Edit_Habit/{habitId}",
            arguments = listOf(
                navArgument("habitId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
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