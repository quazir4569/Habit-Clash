package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)

    var habitName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("") }
    var selectedGoalCount by remember { mutableStateOf("") }
    var selectedReminderTime by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val categories = listOf("Health", "Productivity", "Personal")
    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val goalCounts = listOf("1", "2", "3")
    val reminderTimes = listOf("Morning", "Afternoon", "Evening")

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("Dashboard_Screen") {
                            popUpTo("Dashboard_Screen") { inclusive = false }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Add a Habit",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                FormField("Habit Name", habitName, onChange = { habitName = it }, isDarkMode)
                FormField("Description", description, onChange = { description = it }, isDarkMode)

                RadioGroup("Category:", categories, selectedCategory, onOptionSelected = { selectedCategory = it }, isDarkMode)
                RadioGroup("Frequency:", frequencies, selectedFrequency, onOptionSelected = { selectedFrequency = it }, isDarkMode)
                RadioGroup("Goal Count:", goalCounts, selectedGoalCount, onOptionSelected = { selectedGoalCount = it }, isDarkMode)
                RadioGroup("Reminder Time (optional):", reminderTimes, selectedReminderTime, onOptionSelected = { selectedReminderTime = it }, isDarkMode)

                Spacer(modifier = Modifier.height(24.dp))

                // Confirmation Dialog
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    val habit = hashMapOf(
                                        "title" to habitName,
                                        "description" to description,
                                        "category" to selectedCategory,
                                        "frequency" to selectedFrequency,
                                        "goalCount" to selectedGoalCount.toInt(),
                                        "reminderTime" to selectedReminderTime,
                                        "completed" to false,
                                        "time" to selectedReminderTime
                                    )
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(userId)
                                        .collection("habits")
                                        .add(habit)
                                        .addOnSuccessListener {
                                            navController.navigate("Dashboard_Screen") {
                                                popUpTo("Dashboard_Screen") { inclusive = false }
                                            }
                                        }
                                }
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Confirm Add") },
                        text = { Text("Are you sure you want to add this habit?") },
                        containerColor = colors.cardColor,
                        titleContentColor = colors.textColor,
                        textContentColor = colors.secondaryTextColor
                    )
                }

                // Add Habit Button
                Button(
                    onClick = {
                        if (habitName.isBlank() || selectedCategory.isBlank() || selectedFrequency.isBlank() || selectedGoalCount.isBlank()) return@Button
                        showDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
                ) {
                    Text("Add Habit", fontSize = 18.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    isDarkMode: Boolean
) {
    val colors = getAppThemeColors(isDarkMode)
    Text(text = "$label:", fontSize = 16.sp, color = colors.textColor, modifier = Modifier.padding(bottom = 8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text("Enter $label", color = colors.secondaryTextColor) },
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

@Composable
fun RadioGroup(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isDarkMode: Boolean
) {
    val colors = getAppThemeColors(isDarkMode)
    Text(text = label, fontSize = 16.sp, color = colors.textColor, modifier = Modifier.padding(bottom = 8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = colors.accentColor,
                        unselectedColor = colors.secondaryTextColor
                    )
                )
                Text(option, color = colors.textColor, fontSize = 14.sp)
            }
        }
    }
}
