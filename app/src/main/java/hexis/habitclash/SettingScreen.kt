package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

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
    val dodgerBlue = Color(0xFF1E90FF)

    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var showChangePassDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
            .background(dodgerBlue)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

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
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text(
                        text = username,
                        fontSize = 18.sp,
                        color = colors.textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = colors.secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Theme", color = colors.textColor)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeViewModel.toggleTheme() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History Log entry (opens the new HistoryScreen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clickable { navController.navigate("History_Screen") },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("History Log", color = colors.textColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clickable { showChangePassDialog = true },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Change Password", color = colors.textColor)
                }
            }

            if (showChangePassDialog) {
                var newPass by remember { mutableStateOf("") }
                var confirmPass by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showChangePassDialog = false },
                    title = { Text("Change Password", color = colors.textColor) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("New Password", color = colors.secondaryTextColor) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                shape = RoundedCornerShape(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPass,
                                onValueChange = { confirmPass = it },
                                label = { Text("Confirm Password", color = colors.secondaryTextColor) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                shape = RoundedCornerShape(28.dp)
                            )
                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(it, color = Color.Red)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newPass.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                            } else if (newPass != confirmPass) {
                                errorMessage = "Passwords do not match"
                            } else {
                                FirebaseAuth.getInstance().currentUser?.updatePassword(newPass)?.addOnCompleteListener {
                                    if (it.isSuccessful) showChangePassDialog = false
                                    else errorMessage = it.exception?.message ?: "Failed to update password"
                                }
                            }
                        }) { Text("Change", color = colors.accentColor) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChangePassDialog = false }) {
                            Text("Cancel", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("About App", color = colors.textColor)
                }
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("About Habit Clash", color = colors.textColor) },
                    text = {
                        Column {
                            Text("Habit Clash", color = colors.textColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Version: 1.0.0", color = colors.secondaryTextColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Developed by: Hexis", color = colors.secondaryTextColor)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("Exit", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delete Account", color = Color.Red)
                }
            }

            if (showDeleteAccountDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAccountDialog = false },
                    title = { Text("Delete Account", color = colors.textColor) },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone.", color = colors.textColor) },
                    confirmButton = {
                        TextButton(onClick = {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user.uid)
                                    .delete()
                                    .addOnCompleteListener {
                                        authViewModel.signout()
                                        showDeleteAccountDialog = false
                                    }
                            } else {
                                authViewModel.signout()
                                showDeleteAccountDialog = false
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Log Out", color = colors.textColor) },
                    text = { Text("Are you sure you want to log out?", color = colors.textColor) },
                    confirmButton = {
                        TextButton(onClick = {
                            authViewModel.signout()
                            showLogoutDialog = false
                        }) {
                            Text("Log Out", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}
