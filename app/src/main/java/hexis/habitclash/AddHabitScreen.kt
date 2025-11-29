package hexis.habitclash

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors

@Composable
fun AddHabitScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val context = LocalContext.current

    var habitName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Health") }
    var frequency by rememberSaveable { mutableStateOf("Daily") }
    var reminderTime by rememberSaveable { mutableStateOf<String?>(null) }
    var goalCount by rememberSaveable { mutableStateOf(1) }

    var showSuccess by rememberSaveable { mutableStateOf(false) }
    var reminderMenuExpanded by remember { mutableStateOf(false) }
    var showConfirm by rememberSaveable { mutableStateOf(false) }

    val categories = listOf("Health", "Productivity", "Personal", "Fitness", "Study")
    val frequencies = listOf("Daily", "Weekly", "Monthly")
    val reminderOptions = listOf("Morning", "Afternoon", "Evening")
    val dodgerBlue = Color(0xFF1E90FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dodgerBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Create Habit",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SectionCard(colors) {
                    Text("Basics", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.textColor)
                    Spacer(Modifier.height(12.dp))

                    LabeledField("Habit Name", colors) {
                        OutlinedTextField(
                            value = habitName,
                            onValueChange = { habitName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Drink Water", color = colors.secondaryTextColor) },
                            shape = RoundedCornerShape(18.dp),
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

                    Spacer(Modifier.height(10.dp))

                    LabeledField("Description", colors) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp),
                            placeholder = { Text("Optional details…", color = colors.secondaryTextColor) },
                            shape = RoundedCornerShape(18.dp),
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

                Spacer(Modifier.height(12.dp))

                SectionCard(colors) {
                    Text("Type & Schedule", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.textColor)
                    Spacer(Modifier.height(12.dp))

                    Text("Category", color = colors.secondaryTextColor, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    ChipRow(
                        options = categories,
                        selected = category,
                        onSelect = { category = it },
                        colors = colors
                    )

                    Spacer(Modifier.height(14.dp))

                    Text("Frequency", color = colors.secondaryTextColor, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    ChipRow(
                        options = frequencies,
                        selected = frequency,
                        onSelect = { frequency = it },
                        colors = colors
                    )
                }

                Spacer(Modifier.height(12.dp))

                SectionCard(colors) {
                    Text("Goal & Reminder", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.textColor)
                    Spacer(Modifier.height(12.dp))

                    Text("Daily Goal (times per day)", color = colors.secondaryTextColor, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${goalCount}×", color = colors.textColor, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(12.dp))
                        Slider(
                            value = goalCount.toFloat(),
                            onValueChange = { goalCount = it.toInt().coerceIn(1, 5) },
                            valueRange = 1f..5f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Reminder (optional)", color = colors.secondaryTextColor, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))

                    Box {
                        ReminderField(
                            text = reminderTime ?: "No reminder",
                            hasValue = reminderTime != null,
                            colors = colors,
                            onClick = { reminderMenuExpanded = true },
                            onClear = { reminderTime = null }
                        )
                        DropdownMenu(
                            expanded = reminderMenuExpanded,
                            onDismissRequest = { reminderMenuExpanded = false }
                        ) {
                            reminderOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        reminderTime = opt
                                        reminderMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(84.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (habitName.isBlank()) {
                            Toast.makeText(context, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId == null) {
                            Toast.makeText(context, "You must be logged in", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        showConfirm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor)
                ) {
                    Text("Save Habit", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (showSuccess) {
            AlertDialog(
                onDismissRequest = { showSuccess = false },
                title = {
                    Text("Habit Added 🎉", color = colors.textColor, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Your habit has been saved. You can view it on the dashboard.",
                        color = colors.textColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccess = false
                            val popped = navController.popBackStack("Dashboard_Screen", inclusive = false)
                            if (!popped) {
                                navController.navigate("Dashboard_Screen") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    ) { Text("Go to Dashboard", color = colors.accentColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showSuccess = false }) { Text("Stay Here", color = colors.accentColor) }
                },
                containerColor = colors.cardColor
            )
        }

        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Add Habit") },
                text = { Text("Save this habit and go to your dashboard?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirm = false

                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId == null) {
                                Toast.makeText(context, "You must be logged in", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val habit = hashMapOf(
                                "title" to habitName.trim(),
                                "description" to description.trim(),
                                "category" to category,
                                "frequency" to frequency,
                                "goalCount" to goalCount,
                                "reminderTime" to reminderTime,
                                "isCompletedToday" to false
                            )

                            val dbRef = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("habits")

                            dbRef.add(habit)
                                .addOnSuccessListener { docRef ->
                                    // schedule notification if user chose a reminder bucket
                                    if (reminderTime != null) {
                                        ReminderScheduler.scheduleRemindersForHabit(
                                            context = context,
                                            userId = userId,
                                            habitId = docRef.id,
                                            habitTitle = habitName.trim(),
                                            reminderTime = reminderTime,
                                            frequency = frequency,
                                            goalCount = goalCount
                                        )
                                    }

                                    Toast.makeText(context, "Habit added", Toast.LENGTH_SHORT).show()

                                    val popped = navController.popBackStack("Dashboard_Screen", inclusive = false)
                                    if (!popped) {
                                        navController.navigate("Dashboard_Screen") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Saved locally (offline) — fix Firestore to sync.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    ) { Text("Add & Go to Dashboard") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

/* ============== Reusable UI bits ============== */

@Composable
private fun SectionCard(
    colors: AppThemeColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    colors: AppThemeColors,
    content: @Composable () -> Unit
) {
    Text(label, color = colors.secondaryTextColor, fontSize = 13.sp)
    Spacer(Modifier.height(8.dp))
    content()
}

@Composable
private fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    colors: AppThemeColors
) {
    Column {
        val half = (options.size + 1) / 2
        val rows = listOf(options.take(half), options.drop(half))
        rows.forEachIndexed { idx, row ->
            if (row.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { opt ->
                        FilterChip(
                            selected = opt == selected,
                            onClick = { onSelect(opt) },
                            label = { Text(opt) },
                            leadingIcon = if (opt == selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accentColor,
                                selectedLabelColor = Color.White,
                                containerColor = colors.fieldContainerColor,
                                labelColor = colors.textColor
                            )
                        )
                    }
                }
                if (idx == 0) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReminderField(
    text: String,
    hasValue: Boolean,
    colors: AppThemeColors,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.fieldContainerColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (hasValue) colors.textColor else colors.secondaryTextColor,
            modifier = Modifier.weight(1f)
        )
        if (hasValue) {
            TextButton(onClick = onClear) {
                Text("Clear", color = colors.accentColor)
            }
        }
        Spacer(Modifier.width(6.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = colors.accentColor),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("Choose", color = Color.White)
        }
    }
}
