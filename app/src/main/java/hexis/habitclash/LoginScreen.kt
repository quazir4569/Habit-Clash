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
 * Login screen for user authentication.
 * Allows users to log in with email and password.
 */
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)

    // Handle authentication state changes
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("Dashboard_Screen")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login your Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textColor
        )

        Spacer(modifier = Modifier.height(30.dp))

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
            text = "Pass:",
            color = colors.secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your pass", color = colors.secondaryTextColor) },
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

        Text(
            text = "Forgot Pass?",
            fontSize = 14.sp,
            color = colors.secondaryTextColor,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        Button(
            onClick = {
                authViewModel.login(email, password)
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = authState.value !== AuthState.Loading
        ) {
            Text(text = "Log In", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        TextButton(onClick = {
            navController.navigate("Registration_Screen")
        }) {
            Text(
                text = "Register",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accentColor
            )
        }
    }
}