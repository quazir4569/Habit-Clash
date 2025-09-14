package hexis.habitclash.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Dark theme color scheme for the app.
 * Used when dark mode is active.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlue,
    background = DarkBackground,
    surface = DarkFieldContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText
)

/**
 * Light theme color scheme for the app.
 * Used when light mode is active.
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlue,
    background = LightBackground,
    surface = LightFieldContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText
)

/**
 * Main app theme composition.
 * Sets up colors, typography, and shapes for the entire app.
 */
@Composable
fun HabitClashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,   // Use dynamic colors on Android 12+
    content: @Composable () -> Unit
) {
    // Determine which color scheme to use
    val colorScheme = when {
        // Use dynamic color on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Use static color schemes otherwise
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply Material Theme with our configuration
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}