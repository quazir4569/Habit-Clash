package hexis.habitclash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Manages app theme state.
 * Tracks and switches between dark and light mode.
 */
class ThemeViewModel : ViewModel() {
    var isDarkMode by mutableStateOf(false)
        private set

    /**
     * Switches between dark and light mode.
     */
    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}