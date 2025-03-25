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
import hexis.habitclash.ui.theme.*

@Composable
fun RegistrationScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode

    // Colors based on theme
    val backgroundColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryTextColor = if (isDarkMode) DarkSecondaryText else LightSecondaryText
    val fieldContainerColor = if (isDarkMode) DarkFieldContainer else LightFieldContainer
    val fieldBorderColor = if (isDarkMode) DarkFieldBorder else LightFieldBorder
    val accentColor = PrimaryBlue

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("Dashboard_Screen")
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message,Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "Create an Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Full Name Field
        Text(
            text = "Full Name:",
            color = secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = { Text("Enter your full name", color = secondaryTextColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = fieldBorderColor,
                focusedContainerColor = fieldContainerColor,
                unfocusedContainerColor = fieldContainerColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        Text(
            text = "Email:",
            color = secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email", color = secondaryTextColor) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = fieldBorderColor,
                focusedContainerColor = fieldContainerColor,
                unfocusedContainerColor = fieldContainerColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        Text(
            text = "Pass:",
            color = secondaryTextColor,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your pass", color = secondaryTextColor) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = fieldBorderColor,
                focusedContainerColor = fieldContainerColor,
                unfocusedContainerColor = fieldContainerColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Requirements
        Text(
            text = "• Pass must be 6 or more characters",
            fontSize = 12.sp,
            color = textColor,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Register Button with Basic Validation
        Button(
            onClick = {
                authViewModel.registration(email, password)
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = authState.value !== AuthState.Loading
        ) {
            Text(text = "Register", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Back to Login Link
        TextButton(onClick = {
            navController.navigate("Login_Screen")
        }) {
            Text(
                text = "Already have an account? Log In",
                fontSize = 14.sp,
                color = accentColor
            )
        }

        // Add theme toggle
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { themeViewModel.toggleTheme() },
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.width(200.dp)
        ) {
            Text(
                text = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                color = Color.White
            )
        }
    }
}