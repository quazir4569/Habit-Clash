package hexis.habitclash.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// App primary color used for accent elements
val PrimaryBlue = Color(0xFF2563EB)

// Light theme colors
val LightBackground = Color(0xFFF8FAFC)
val LightText = Color.Black
val LightSecondaryText = Color(0xFF7A7A7A)
val LightFieldContainer = Color.White
val LightFieldBorder = Color(0xFFCECECE)

// Dark theme colors
val DarkBackground = Color(0xFF121212)
val DarkText = Color.White
val DarkSecondaryText = Color(0xFFB0B0B0)
val DarkFieldContainer = Color(0xFF2A2A2A)
val DarkFieldBorder = Color(0xFF444444)

/**
 * Data class containing all themed colors for the app.
 * Used to consistently apply colors throughout the app.
 */
data class AppThemeColors(
    val backgroundColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val fieldContainerColor: Color,
    val fieldBorderColor: Color,
    val accentColor: Color,
    val cardColor: Color
)

/**
 * Gets app colors based on current theme.
 * Returns appropriate color set for light or dark mode.
 */
@Composable
fun getAppThemeColors(isDarkMode: Boolean): AppThemeColors {
    return AppThemeColors(
        backgroundColor = if (isDarkMode) DarkBackground else LightBackground,
        textColor = if (isDarkMode) DarkText else LightText,
        secondaryTextColor = if (isDarkMode) DarkSecondaryText else LightSecondaryText,
        fieldContainerColor = if (isDarkMode) DarkFieldContainer else LightFieldContainer,
        fieldBorderColor = if (isDarkMode) DarkFieldBorder else LightFieldBorder,
        accentColor = PrimaryBlue,
        cardColor = if (isDarkMode) DarkFieldContainer else Color.White
    )
}