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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Edit Habit Screen for modifying existing habits.
 */
@Composable
fun EditHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    habitId: String
) {
    // form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("") }
    var selectedGoalCount by remember { mutableStateOf("") }

    // dropdown expanded state
    var categoryExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var goalCountExpanded by remember { mutableStateOf(false) }

    // options
    val categories = listOf("Health", "Productivity", "Personal")
    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val goalCounts = listOf("1", "2", "3", "4", "5")

    // theme + basics
    val context = LocalContext.current
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()

    // fetch existing habit and prefill
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
                    val habit = document.toObject(Habit::class.java)
                    if (habit != null) {
                        title = habit.title
                        description = habit.description
                        selectedCategory = habit.category
                        selectedFrequency = habit.frequency
                        selectedGoalCount = habit.goalCount.toString()
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            // header row
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
                Text(
                    "Edit Habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // form body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // basic fields
                FormField(
                    label = "Habit Name",
                    value = title,
                    onChange = { title = it },
                    isDarkMode = isDarkMode
                )
                FormField(
                    label = "Description",
                    value = description,
                    onChange = { description = it },
                    isDarkMode = isDarkMode,
                    singleLine = false
                )

                // dropdowns
                DropdownField(
                    label = "Category",
                    selected = selectedCategory,
                    options = categories,
                    expanded = categoryExpanded,
                    onExpandChange = { categoryExpanded = it },
                    onOptionSelected = {
                        selectedCategory = it
                        categoryExpanded = false
                    },
                    isDarkMode = isDarkMode
                )

                DropdownField(
                    label = "Frequency",
                    selected = selectedFrequency,
                    options = frequencies,
                    expanded = frequencyExpanded,
                    onExpandChange = { frequencyExpanded = it },
                    onOptionSelected = {
                        selectedFrequency = it
                        frequencyExpanded = false
                    },
                    isDarkMode = isDarkMode
                )

                DropdownField(
                    label = "Goal Count",
                    selected = selectedGoalCount,
                    options = goalCounts,
                    expanded = goalCountExpanded,
                    onExpandChange = { goalCountExpanded = it },
                    onOptionSelected = {
                        selectedGoalCount = it
                        goalCountExpanded = false
                    },
                    isDarkMode = isDarkMode
                )

                Spacer(modifier = Modifier.height(24.dp))

                // save
                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null && title.isNotBlank()) {
                            val updates = hashMapOf<String, Any>(
                                "title" to title,
                                "description" to description,
                                "category" to selectedCategory,
                                "frequency" to selectedFrequency,
                                "goalCount" to (selectedGoalCount.toIntOrNull() ?: 1)
                            )

                            FirebaseFirestore.getInstance()
                                .collection("users")
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

                // delete
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

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // bottom nav stays the same
        BottomNavigationBar(navController, isDarkMode)
    }
}


@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    isDarkMode: Boolean,
    singleLine: Boolean = true
) {
    val colors = getAppThemeColors(isDarkMode)
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, color = colors.secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (singleLine) 0.dp else 100.dp),
            singleLine = singleLine,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentColor,
                unfocusedBorderColor = colors.fieldBorderColor,
                focusedContainerColor = colors.fieldContainerColor,
                unfocusedContainerColor = colors.fieldContainerColor,
                focusedTextColor = colors.textColor,
                unfocusedTextColor = colors.textColor
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            placeholder = { Text("Enter $label", color = colors.secondaryTextColor) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    isDarkMode: Boolean
) {
    val colors = getAppThemeColors(isDarkMode)

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label, color = colors.secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandChange
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accentColor,
                    unfocusedBorderColor = colors.fieldBorderColor,
                    focusedContainerColor = colors.fieldContainerColor,
                    unfocusedContainerColor = colors.fieldContainerColor,
                    focusedTextColor = colors.textColor,
                    unfocusedTextColor = colors.textColor
                ),
                placeholder = { Text("Choose $label", color = colors.secondaryTextColor) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onOptionSelected(opt) }
                    )
                }
            }
        }
    }
}
