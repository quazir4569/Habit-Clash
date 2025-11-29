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
            // Login
            composable("Login_Screen") {
                LoginScreen(navController, authViewModel, themeViewModel)
            }

            // Registration
            composable("Registration_Screen") {
                RegistrationScreen(navController, authViewModel, themeViewModel)
            }

            // Dashboard
            composable("Dashboard_Screen") {
                DashboardScreen(navController, authViewModel, themeViewModel)
            }

            composable("FriendList_Screen") {
                FriendListScreen(navController, themeViewModel)
            }

            // Add Habit
            composable("AddHabit_Screen") {
                AddHabitScreen(navController, themeViewModel)
            }

            // Settings
            composable("Settings_Screen") {
                SettingsScreen(navController, authViewModel, themeViewModel)
            }

            // Analytics
            composable("Analytics_Screen") {
                AnalyticsScreen(navController, themeViewModel)
            }

            // Edit Habit
            composable(
                "Edit_Habit/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                EditHabitScreen(navController, themeViewModel, habitId)
            }

            // History
            composable("History_Screen") {
                HistoryScreen(navController, themeViewModel)
            }

            // Forgot / Reset Password
            composable("ResetPassword_Screen") {
                ResetPasswordScreen(navController)
            }
        }
    }
}
