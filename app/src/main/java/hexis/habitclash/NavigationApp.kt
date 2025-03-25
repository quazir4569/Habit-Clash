package hexis.habitclash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationApp(modifier: Modifier = Modifier, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel){

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Login_Screen", builder = {

        composable("Login_Screen"){
            LoginScreen(navController, authViewModel, themeViewModel)
        }

        composable("Registration_Screen"){
            RegistrationScreen(navController, authViewModel, themeViewModel)
        }

        composable("Dashboard_Screen"){
            DashboardScreen(navController, authViewModel)
        }

        composable("Add_Habit") {
            AddHabitScreen(navController = navController)
        }


    } )
}