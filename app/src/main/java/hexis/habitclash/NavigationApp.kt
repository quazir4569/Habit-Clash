package hexis.habitclash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "Login_Screen"
        ) {
            composable("Login_Screen") {
                LoginScreen(navController, authViewModel, themeViewModel)
            }
            composable("Registration_Screen") {
                RegistrationScreen(navController, authViewModel, themeViewModel)
            }
            composable("Dashboard_Screen") {
                DashboardScreen(navController, authViewModel, themeViewModel)
            }
            composable("AddHabit_Screen") {
                AddHabitScreen(navController, themeViewModel)
            }
            composable("Settings_Screen") {
                SettingsScreen(navController, authViewModel, themeViewModel)
            }
            composable("Analytics_Screen") {
                AnalyticsScreen(navController, themeViewModel)
            }
            composable(
                "Edit_Habit/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStack ->
                val habitId = backStack.arguments?.getString("habitId") ?: ""
                EditHabitScreen(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    habitId = habitId
                )
            }
            // new history route
            composable("History_Screen") {
                HistoryScreen(
                    navController = navController,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}
