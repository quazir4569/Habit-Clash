package hexis.habitclash

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Screen for adding a new habit.
 * User can enter habit name and time, then save to Firebase.
 */
@Composable
fun AddHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)

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
                    text = "Add a Habit",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit name input
            Text(
                text = "Habit Name:",
                color = colors.secondaryTextColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Enter habit name", color = colors.secondaryTextColor) },
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

            // Time input
            Text(
                text = "Time:",
                color = colors.secondaryTextColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                placeholder = { Text("e.g. 8:00 AM", color = colors.secondaryTextColor) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    // Save habit to Firebase
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null && title.isNotBlank() && time.isNotBlank()) {
                        val db = FirebaseFirestore.getInstance()
                        val habit = Habit(title = title, time = time)

                        db.collection("users")
                            .document(userId)
                            .collection("habits")
                            .add(habit)
                            .addOnSuccessListener {
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.e("AddHabitScreen", "Error adding habit", e)
                                Toast.makeText(context, "Failed to save habit", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Make sure you're logged in and fields aren't empty", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Add Habit", color = Color.White, fontSize = 18.sp)
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}