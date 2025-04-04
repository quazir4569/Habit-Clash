package hexis.habitclash

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Edit Habit Screen for modifying existing habits.
 * Allows users to update habit details or delete habits.
 */
@Composable
fun EditHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    habitId: String
) {
    // Form state variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("") }
    var selectedGoalCount by remember { mutableStateOf("") }

    // Dropdown menu states
    var categoryExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var goalCountExpanded by remember { mutableStateOf(false) }

    // Dropdown options
    val categories = listOf("Health", "Productivity", "Personal")
    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val goalCounts = listOf("1", "2", "3")

    // Context and theme setup
    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()

    // Load habit data from Firebase
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
                            // Populate form with existing habit data
                            title = habit.title
                            description = habit.description
                            selectedCategory = habit.category
                            selectedFrequency = habit.frequency
                            selectedGoalCount = habit.goalCount.toString()
                        }
                    }
                }
        }
    }

    // Main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        // Content area
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            // Header with back button and title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textColor)
                }

                // Screen title with Inter SemiBold font
                Text(
                    "Edit Habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Form fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Basic input fields
                FormField("Habit Name", title, onChange = { title = it }, isDarkMode)
                FormField("Description", description, onChange = { description = it }, isDarkMode)

                // Dropdown selectors
                DropdownField("Category", selectedCategory, categories, categoryExpanded,
                    onExpandChange = { categoryExpanded = it },
                    onOptionSelected = { selectedCategory = it; categoryExpanded = false },
                    isDarkMode)

                DropdownField("Frequency", selectedFrequency, frequencies, frequencyExpanded,
                    onExpandChange = { frequencyExpanded = it },
                    onOptionSelected = { selectedFrequency = it; frequencyExpanded = false },
                    isDarkMode)

                DropdownField("Goal Count", selectedGoalCount, goalCounts, goalCountExpanded,
                    onExpandChange = { goalCountExpanded = it },
                    onOptionSelected = { selectedGoalCount = it; goalCountExpanded = false },
                    isDarkMode)

                Spacer(modifier = Modifier.height(24.dp))

                // Update button
                Button(
                    onClick = {
                        // Update habit in Firebase
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null && title.isNotBlank()) {
                            val db = FirebaseFirestore.getInstance()

                            // Create update map
                            val updates = hashMapOf<String, Any>()
                            updates["title"] = title
                            updates["description"] = description
                            updates["category"] = selectedCategory
                            updates["frequency"] = selectedFrequency
                            updates["goalCount"] = selectedGoalCount.toIntOrNull() ?: 1

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

                // Delete button
                Button(
                    onClick = {
                        // Delete habit from Firebase
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

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom navigation
        BottomNavigationBar(navController, isDarkMode)
    }
}