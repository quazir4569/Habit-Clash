package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ResetPasswordScreen(navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val dodgerBlue = Color(0xFF1E90FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dodgerBlue)

    ) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(dodgerBlue),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Reset Password", fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            message = "Password reset link sent to $email"
                            isSuccess = true
                        }
                        .addOnFailureListener {
                            message = "Unable to send reset link. Try another email."
                            isSuccess = false
                        }
                } else {
                    message = "Please enter your email."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Reset Link")
        }

        message?.let {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = it,
                color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate("Login_Screen") }) {
            Text("Back to Login")
        }
    }
}}
