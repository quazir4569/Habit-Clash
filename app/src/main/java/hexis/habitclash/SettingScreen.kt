package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Settings screen for app preferences and user account.
 * Shows user username, email, theme toggle, notification toggle, about section, change password, delete account, and logout button.
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

    // State for username
    var username by remember { mutableStateOf("") }

    // State for notification toggle (for demo purposes, not persisted)
    var notificationsEnabled by remember { mutableStateOf(true) }

    // State for showing dialogs
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Fetch username from Firestore
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("username") ?: "User"
                    }
                }
        }
    }

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
                    onClick = {
                        navController.navigate("Dashboard_Screen") {
                            popUpTo("Dashboard_Screen") { inclusive = false }
                        }
                    },
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

            // User info card with username and email
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Username
                    Text(
                        text = username,
                        fontSize = 18.sp,
                        color = colors.textColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = colors.secondaryTextColor,
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
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Notification toggle card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 16.sp,
                        color = colors.textColor,
                        fontWeight = FontWeight.Normal
                    )

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accentColor,
                            checkedTrackColor = colors.accentColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Password button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clickable { showChangePasswordDialog = true },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Change Password",
                        fontSize = 16.sp,
                        color = colors.textColor,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Change Password Dialog
            if (showChangePasswordDialog) {
                var newPassword by remember { mutableStateOf("") }
                var confirmPassword by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showChangePasswordDialog = false },
                    title = {
                        Text(
                            text = "Change Password",
                            color = colors.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password", color = colors.secondaryTextColor) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentColor,
                                    unfocusedBorderColor = colors.fieldBorderColor,
                                    focusedContainerColor = colors.fieldContainerColor,
                                    unfocusedContainerColor = colors.fieldContainerColor,
                                    focusedTextColor = colors.textColor,
                                    unfocusedTextColor = colors.textColor
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password", color = colors.secondaryTextColor) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentColor,
                                    unfocusedBorderColor = colors.fieldBorderColor,
                                    focusedContainerColor = colors.fieldContainerColor,
                                    unfocusedContainerColor = colors.fieldContainerColor,
                                    focusedTextColor = colors.textColor,
                                    unfocusedTextColor = colors.textColor
                                )
                            )

                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                            } else if (newPassword != confirmPassword) {
                                errorMessage = "Passwords do not match"
                            } else {
                                currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showChangePasswordDialog = false
                                    } else {
                                        errorMessage = task.exception?.message ?: "Failed to update password"
                                    }
                                }
                            }
                        }) {
                            Text("Change", color = colors.accentColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChangePasswordDialog = false }) {
                            Text("Cancel", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About App button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clickable { showAboutDialog = true },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "About App",
                        fontSize = 16.sp,
                        color = colors.textColor,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // About App Dialog
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = {
                        Text(
                            text = "About HabitClash",
                            color = colors.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "HabitClash",
                                fontSize = 16.sp,
                                color = colors.textColor
                            )
                            Text(
                                text = "Version: 1.0.0",
                                fontSize = 14.sp,
                                color = colors.secondaryTextColor
                            )
                            Text(
                                text = "Developed by: Your Name",
                                fontSize = 14.sp,
                                color = colors.secondaryTextColor
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("OK", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Account button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clickable { showDeleteAccountDialog = true },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Delete Account",
                        fontSize = 16.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Delete Account Dialog
            if (showDeleteAccountDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAccountDialog = false },
                    title = {
                        Text(
                            text = "Delete Account",
                            color = colors.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to delete your account? This action cannot be undone.",
                            color = colors.textColor
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            currentUser?.delete()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Delete user data from Firestore
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(currentUser.uid)
                                        .delete()
                                        .addOnSuccessListener {
                                            authViewModel.signout()
                                            showDeleteAccountDialog = false
                                        }
                                } else {
                                    // Handle error (e.g., user needs to re-authenticate)
                                    // For simplicity, we'll just sign out
                                    authViewModel.signout()
                                    showDeleteAccountDialog = false
                                }
                            }
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAccountDialog = false }) {
                            Text("Cancel", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
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
