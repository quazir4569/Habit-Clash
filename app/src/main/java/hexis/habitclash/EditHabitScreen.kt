package hexis.habitclash

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    habitId: String
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var goalCount by remember { mutableStateOf("1") }
    var reminderTime by remember { mutableStateOf("") }

    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()

    // Load full habit data
    LaunchedEffect(habitId) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && habitId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("habits")
                .document(habitId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val habit = document.toObject(Habit::class.java)
                        if (habit != null) {
                            title = habit.title
                            description = habit.description
                            category = habit.category
                            frequency = habit.frequency
                            goalCount = habit.goalCount.toString()
                            reminderTime = habit.reminderTime ?: ""
                        }
                    }
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Back button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            InputField("Habit Name", title, colors) { title = it }
            InputField("Description", description, colors) { description = it }
            InputField("Category", category, colors) { category = it }
            InputField("Frequency", frequency, colors) { frequency = it }
            InputField("Goal Count", goalCount, colors, keyboardType = KeyboardType.Number) { goalCount = it }
            InputField("Reminder Time (optional)", reminderTime, colors) { reminderTime = it }

            Spacer(modifier = Modifier.height(24.dp))

            // Update Button
            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null && title.isNotBlank()) {
                        val db = FirebaseFirestore.getInstance()

                        // Create a map of fields to update
                        val updates = hashMapOf<String, Any>()
                        updates["title"] = title
                        updates["description"] = description
                        updates["category"] = category
                        updates["frequency"] = frequency
                        updates["goalCount"] = goalCount.toIntOrNull() ?: 1
                        updates["reminderTime"] = reminderTime

                        db.collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habitId)
                            .update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Habit updated", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditHabitScreen", "Error updating habit", e)
                                Toast.makeText(context, "Failed to update habit", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
            ) {
                Text("Update Habit", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Button
            Button(
                onClick = {
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

            Spacer(modifier = Modifier.height(80.dp)) // Room below the button
        }

        // Bottom navigation at the bottom
        BottomNavigationBar(navController, isDarkMode)
    }
}

/**
 * Input field for habit form with proper styling.
 */
@Composable
private fun InputField(
    label: String,
    value: String,
    colors: hexis.habitclash.ui.theme.AppThemeColors,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = "$label:", color = colors.secondaryTextColor, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Enter $label", color = colors.secondaryTextColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentColor,
                unfocusedBorderColor = colors.fieldBorderColor,
                focusedContainerColor = colors.fieldContainerColor,
                unfocusedContainerColor = colors.fieldContainerColor,
                focusedTextColor = colors.textColor,
                unfocusedTextColor = colors.textColor
            )
        )
    }
}