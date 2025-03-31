package hexis.habitclash

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

@Composable
fun AddHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textColor)
            }
            Text(
                "Add a Habit",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields
        InputField("Habit Name", title) { title = it }
        InputField("Description", description) { description = it }
        InputField("Category", category) { category = it }
        InputField("Frequency", frequency) { frequency = it }
        InputField("Goal Count", goalCount, keyboardType = KeyboardType.Number) { goalCount = it }
        InputField("Reminder Time (optional)", reminderTime) { reminderTime = it }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null && title.isNotBlank()) {
                    val db = FirebaseFirestore.getInstance()
                    val habit = Habit(
                        title = title,
                        description = description,
                        category = category,
                        frequency = frequency,
                        goalCount = goalCount.toIntOrNull() ?: 1,
                        reminderTime = reminderTime,
                        userId = userId
                    )
                    db.collection("users")
                        .document(userId)
                        .collection("habits")
                        .add(habit)
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Log.e("AddHabitScreen", "Error adding habit", it)
                            Toast.makeText(context, "Failed to save habit", Toast.LENGTH_SHORT).show()
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
            Text("Add Habit", color = Color.White, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(80.dp)) // this is to give room below the button
    }

    BottomNavigationBar(navController, isDarkMode)
}
@Composable
private fun InputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val colors = getAppThemeColors(isSystemInDarkTheme())
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

