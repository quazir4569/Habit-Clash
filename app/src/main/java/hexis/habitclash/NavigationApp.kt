package hexis.habitclash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavigationApp(modifier: Modifier = Modifier, authViewModel: AuthViewModel){

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "Login_Screen", builder = {

        composable("Login_Screen"){
            LoginScreen(navController, authViewModel)
        }

        composable("Registration_Screen"){
            RegistrationScreen(navController, authViewModel)
        }

        composable("Dashboard_Screen"){
            DashboardScreen(navController, authViewModel)
        }

    } )
}