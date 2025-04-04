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
 * Main entry point for the app.
 * Sets up ViewModels, theme, and navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display

        // Initialize ViewModels
        val authViewModel: AuthViewModel by viewModels()
        val themeViewModel: ThemeViewModel by viewModels()

        setContent {
            // Apply theme based on current mode
            HabitClashTheme(darkTheme = themeViewModel.isDarkMode) {
                // Main app scaffold
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Set up navigation
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