package hexis.habitclash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Theme ViewModel for managing app theme state.
 * Handles switching between light and dark mode.
 */
class ThemeViewModel : ViewModel() {
    // Current theme state (light or dark mode)
    var isDarkMode by mutableStateOf(false)
        private set

    /**
     * Toggles between light and dark theme.
     * Updates theme state when called.
     */
    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}