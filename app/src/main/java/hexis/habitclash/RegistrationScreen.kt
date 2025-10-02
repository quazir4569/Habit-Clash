package hexis.habitclash

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Registration screen for new users.
 * Collects username, email, and password to create a new account.
 */
@Composable
fun RegistrationScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    // State variables for registration fields
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Get authentication state and context
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)

    // Handle authentication state changes
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("Dashboard_Screen")
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message,Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    // Main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Headline using Inter SemiBold font
        Text(
            text = "Create an Account",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textColor
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Username field
        Text(
            text = "Username:",
            color = colors.secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Enter your username", color = colors.secondaryTextColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentColor,
                unfocusedBorderColor = colors.fieldBorderColor,
                focusedContainerColor = colors.fieldContainerColor,
                unfocusedContainerColor = colors.fieldContainerColor,
                focusedTextColor = colors.textColor,
                unfocusedTextColor = colors.textColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email field
        Text(
            text = "Email:",
            color = colors.secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email", color = colors.secondaryTextColor) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentColor,
                unfocusedBorderColor = colors.fieldBorderColor,
                focusedContainerColor = colors.fieldContainerColor,
                unfocusedContainerColor = colors.fieldContainerColor,
                focusedTextColor = colors.textColor,
                unfocusedTextColor = colors.textColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        Text(
            text = "Password:",
            color = colors.secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your password", color = colors.secondaryTextColor) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(28.dp),
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

        // Password requirements text
        Text(
            text = "• Password must be 6 or more characters",
            fontSize = 12.sp,
            color = colors.textColor,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Register button
        Button(
            onClick = {
                authViewModel.registration(email, password, username)
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = authState.value !== AuthState.Loading
        ) {
            Text(text = "Register", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login link
        TextButton(onClick = {
            navController.navigate("Login_Screen")
        }) {
            Text(
                text = "Already have an account? Log In",
                fontSize = 14.sp,
                color = colors.accentColor
            )
        }
    }
}