package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Add Habit Screen for creating new habits.
 * Allows users to enter habit details and save to Firebase.
 */
@Composable
fun AddHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    // Theme and colors
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)

    // Form state variables
    var habitName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("") }
    var selectedGoalCount by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // Dropdown menu states
    var categoryExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var goalCountExpanded by remember { mutableStateOf(false) }

    // Dropdown options
    val categories = listOf("Health", "Productivity", "Personal")
    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val goalCounts = listOf("1", "2", "3")

    val scrollState = rememberScrollState()

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
                    onClick = {
                        navController.navigate("Dashboard_Screen") {
                            popUpTo("Dashboard_Screen") { inclusive = false }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textColor
                    )
                }

                // Screen title with Inter SemiBold font
                Text(
                    "Add a Habit",
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
                FormField("Habit Name", habitName, onChange = { habitName = it }, isDarkMode)
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

                // Confirmation dialog
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                // Save habit to Firebase
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    val habit = hashMapOf(
                                        "title" to habitName,
                                        "description" to description,
                                        "category" to selectedCategory,
                                        "frequency" to selectedFrequency,
                                        "goalCount" to selectedGoalCount.toInt(),
                                        "completed" to false
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

                // Add button
                Button(
                    onClick = {
                        // Validate required fields
                        if (habitName.isBlank() || selectedCategory.isBlank() ||
                            selectedFrequency.isBlank() || selectedGoalCount.isBlank()) return@Button
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

        // Bottom navigation
        BottomNavigationBar(navController, isDarkMode)
    }
}

/**
 * Reusable text input field component.
 * Used for text inputs in the habit form.
 */
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

/**
 * Reusable dropdown field component.
 * Used for selection fields in the habit form.
 */
@Composable
fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    isDarkMode: Boolean
) {
    val colors = getAppThemeColors(isDarkMode)

    Text(text = "$label:", fontSize = 16.sp, color = colors.textColor, modifier = Modifier.padding(bottom = 8.dp))

    Box(modifier = Modifier.fillMaxWidth()) {
        // Dropdown field
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { },
            readOnly = true,
            placeholder = { Text("Select $label", color = colors.secondaryTextColor) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            trailingIcon = {
                IconButton(onClick = { onExpandChange(!expanded) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Open Dropdown",
                        tint = colors.secondaryTextColor
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentColor,
                unfocusedBorderColor = colors.fieldBorderColor,
                focusedContainerColor = colors.fieldContainerColor,
                unfocusedContainerColor = colors.fieldContainerColor,
                focusedTextColor = colors.textColor,
                unfocusedTextColor = colors.textColor
            )
        )

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .align(Alignment.TopStart)
                .background(colors.fieldContainerColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = colors.textColor) },
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }
}