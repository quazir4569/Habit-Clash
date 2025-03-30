package hexis.habitclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import hexis.habitclash.ui.theme.HabitClashTheme

/**
 * Main app entry point.
 * Sets up theme and starts the app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Full screen mode

        // Set up view models
        val authViewModel: AuthViewModel by viewModels()
        val themeViewModel: ThemeViewModel by viewModels()

        setContent {
            // Apply theme based on dark/light mode setting
            HabitClashTheme(darkTheme = themeViewModel.isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationApp(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}