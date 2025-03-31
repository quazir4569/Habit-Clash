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
 * Screen for editing an existing habit.
 * Pre-fills with current habit data and allows updating or deleting.
 */
@Composable
fun EditHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    habitId: String,
    habitTitle: String,
    habitTime: String
) {
    var title by remember { mutableStateOf(habitTitle) }
    var time by remember { mutableStateOf(habitTime) }
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
                    text = "Edit Habit",
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

            // Update button
            Button(
                onClick = {
                    // Update habit in Firebase
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null && title.isNotBlank() && time.isNotBlank()) {
                        val db = FirebaseFirestore.getInstance()
                        val updatedHabit = hashMapOf(
                            "title" to title,
                            "time" to time
                        )

                        db.collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habitId)
                            .update(updatedHabit as Map<String, Any>)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Habit updated", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditHabitScreen", "Error updating habit", e)
                                Toast.makeText(context, "Failed to update habit", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Make sure all fields are filled", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Update Habit", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete button
            Button(
                onClick = {
                    // Delete from Firebase
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habitId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditHabitScreen", "Error deleting habit", e)
                                Toast.makeText(context, "Failed to delete habit", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Delete Habit", color = Color.White, fontSize = 18.sp)
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}