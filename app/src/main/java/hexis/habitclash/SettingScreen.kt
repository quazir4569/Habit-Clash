package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Settings screen for app preferences and user account.
 * Shows user email, theme toggle, and logout button.
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode = themeViewModel.isDarkMode
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "Not logged in"
    val authState = authViewModel.authState.observeAsState()
    val colors = getAppThemeColors(isDarkMode)

    // Redirect to login if not authenticated
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("Login_Screen") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User email card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userEmail,
                        fontSize = 16.sp,
                        color = colors.textColor,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme toggle card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Theme",
                        fontSize = 16.sp,
                        color = colors.textColor,
                        fontWeight = FontWeight.Normal
                    )

                    // Dark/light mode switch
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeViewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accentColor,
                            checkedTrackColor = colors.accentColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout button
            Button(
                onClick = {
                    authViewModel.signout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 18.sp, color = Color.White)
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}