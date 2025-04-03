package hexis.habitclash

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

    // States for form fields
    var habitName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("") }
    var selectedGoalCount by remember { mutableStateOf("") }
    var selectedReminderTime by remember { mutableStateOf("") }

    // Predefined options for radio buttons
    val categories = listOf("Health", "Productivity", "Personal")
    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val goalCounts = listOf("1", "2", "3")
    val reminderTimes = listOf("Morning", "Afternoon", "Evening")

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
                        tint = colors.textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Name
            Text(
                text = "Habit Name:",
                fontSize = 16.sp,
                color = colors.textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                placeholder = { Text("Enter Habit Name", color = colors.secondaryTextColor) },
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

            // Description
            Text(
                text = "Description:",
                fontSize = 16.sp,
                color = colors.textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Enter Description", color = colors.secondaryTextColor) },
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

            // Category (Radio Buttons)
            Text(
                text = "Category:",
                fontSize = 16.sp,
                color = colors.textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        RadioButton(
                            selected = (category == selectedCategory),
                            onClick = { selectedCategory = category },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentColor,
                                unselectedColor = colors.secondaryTextColor
                            )
                        )
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = colors.textColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency (Radio Buttons)
            Text(
                text = "Frequency:",
                fontSize = 16.sp,
                color = colors.textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                frequencies.forEach { frequency ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        RadioButton(
                            selected = (frequency == selectedFrequency),
                            onClick = { selectedFrequency = frequency },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentColor,
                                unselectedColor = colors.secondaryTextColor
                            )
                        )
                        Text(
                            text = frequency,
                            fontSize = 14.sp,
                            color = colors.textColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Count (Radio Buttons)
            Text(
                text = "Goal Count:",
                fontSize = 16.sp,
                color = colors.textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                goalCounts.forEach { count ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        RadioButton(
                            selected = (count == selectedGoalCount),
                            onClick = { selectedGoalCount = count },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentColor,
                                unselectedColor = colors.secondaryTextColor
                            )
                        )
                        Text(
                            text = count,
                            fontSize = 14.sp,
                            color = colors.textColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder Time (Radio Buttons)
            Text(
                text = "Reminder Time (optional):",
                fontSize = 16.sp,
                color = colors.textColor,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                reminderTimes.forEach { time ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        RadioButton(
                            selected = (time == selectedReminderTime),
                            onClick = { selectedReminderTime = time },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentColor,
                                unselectedColor = colors.secondaryTextColor
                            )
                        )
                        Text(
                            text = time,
                            fontSize = 14.sp,
                            color = colors.textColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add Habit Button
            Button(
                onClick = {
                    if (habitName.isBlank() || selectedCategory.isBlank() || selectedFrequency.isBlank() || selectedGoalCount.isBlank()) {
                        // Basic validation: ensure required fields are filled
                        return@Button
                    }

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
                            "time" to selectedReminderTime // For display purposes on Dashboard
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Add Habit", fontSize = 18.sp, color = Color.White)
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}
