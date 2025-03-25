package hexis.habitclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import hexis.habitclash.ui.theme.HabitClashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel : AuthViewModel by viewModels()
        val themeViewModel : ThemeViewModel by viewModels()

        setContent {
            HabitClashTheme(darkTheme = themeViewModel.isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationApp(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel, themeViewModel = themeViewModel)
                }
            }
        }
    }
}