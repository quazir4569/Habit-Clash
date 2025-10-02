package hexis.habitclash.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hexis.habitclash.R

// Define font families
val Poppins = FontFamily(
    Font(R.font.poppins, FontWeight.Normal)
)

val InterSemiBold = FontFamily(
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

// App typography settings
val Typography = Typography(
    // Default body text style
    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // Style for headlines
    titleLarge = TextStyle(
        fontFamily = InterSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),

    // Style for sub-headlines
    titleMedium = TextStyle(
        fontFamily = InterSemiBold,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
)