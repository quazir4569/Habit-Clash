package hexis.habitclash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun NavigationApp(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "Login_Screen",
        modifier = modifier
    ) {
        composable("Login_Screen") {
            LoginScreen(navController, authViewModel, themeViewModel)
        }

        composable("Dashboard_Screen") {
            DashboardScreen(navController, authViewModel, themeViewModel)
        }

        composable("AddHabit_Screen") {
            AddHabitScreen(navController, themeViewModel)
        }

        composable(
            route = "Edit_Habit/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            EditHabitScreen(navController, themeViewModel, habitId)
        }

        composable("Settings_Screen") {
            SettingsScreen(navController, authViewModel, themeViewModel)
        }

        composable("Analytics_Screen") {
            AnalyticsScreen(navController, themeViewModel)
        }
    }
}
