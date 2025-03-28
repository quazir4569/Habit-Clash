package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import hexis.habitclash.ui.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode = themeViewModel.isDarkMode

    // Colors based on theme
    val backgroundColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryTextColor = if (isDarkMode) DarkSecondaryText else LightSecondaryText
    val accentColor = PrimaryBlue
    val cardColor = if (isDarkMode) DarkFieldContainer else Color.White

    // State for showing the Preference dialog (for Dark Mode toggle)
    var showPreferenceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Setting",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Logo Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LOGO\nWILL GO\nHERE",
                color = Color.Black,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Account Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Navigate to Account Screen or show dialog */ },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Account",
                    fontSize = 18.sp,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to Account",
                    tint = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notification Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Navigate to Notification Screen or show dialog */ },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Notification",
                    fontSize = 18.sp,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to Notification",
                    tint = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preference Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPreferenceDialog = true },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Preference",
                    fontSize = 18.sp,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to Preference",
                    tint = secondaryTextColor
                )
            }
        }

        // Preference Dialog (for Dark Mode toggle)
        if (showPreferenceDialog) {
            AlertDialog(
                onDismissRequest = { showPreferenceDialog = false },
                title = {
                    Text(
                        text = "Preferences",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dark Mode",
                            fontSize = 16.sp,
                            color = textColor
                        )
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { themeViewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = accentColor,
                                checkedTrackColor = accentColor.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPreferenceDialog = false }) {
                        Text("OK", color = accentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPreferenceDialog = false }) {
                        Text("Cancel", color = accentColor)
                    }
                },
                containerColor = cardColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // About Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Navigate to About Screen or show dialog */ },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "About",
                    fontSize = 18.sp,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go to About",
                    tint = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Return Dashboard Button
        Button(
            onClick = {
                // Navigate back to DashboardScreen
                navController.navigate("Dashboard_Screen") {
                    // Pop up to the DashboardScreen to avoid creating a new instance
                    popUpTo("Dashboard_Screen") { inclusive = false }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Return Dashboard", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logout Button
        Button(
            onClick = {
                authViewModel.signout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Sign Out",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 18.sp, color = Color.White)
        }
    }
}
