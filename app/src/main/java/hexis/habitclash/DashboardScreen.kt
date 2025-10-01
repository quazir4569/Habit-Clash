package hexis.habitclash

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors
import kotlin.math.max

@Composable
fun DashboardScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()
    var username by remember { mutableStateOf("User") }
    var completeDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val totalCurrentStreak = habits.sumOf { it.currentStreak }
    val bestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0
    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompletedToday }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()

        // Load username
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    username = document.getString("username") ?: "User"
                }
            }

        // Load habits realtime
        db.collection("users")
            .document(userId)
            .collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    habits.clear()
                    for (doc in snapshot.documents) {
                        val habit = doc.toObject(Habit::class.java)?.copy(id = doc.id)
                        if (habit != null) habits.add(habit)
                    }
                }
            }
    }

    if (authState.value is AuthState.Unauthenticated) {
        LaunchedEffect(Unit) {
            navController.navigate("Login_Screen")
        }
    }

    progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    if (progress == 1f && totalHabits > 0 && !completeDialog) {
        completeDialog = true
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
                .verticalScroll(scrollState)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(21.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(colors.accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, @$username",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = colors.accentColor,
                                modifier = Modifier.height(26.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Level 0",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (totalCurrentStreak > 0) {
                                    "$totalCurrentStreak Day Streak 🔥"
                                } else {
                                    "Start your streak! 🎯"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (totalCurrentStreak > 0) colors.accentColor else colors.textColor
                            )
                        }
                        if (bestStreak > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Best Streak: $bestStreak days 🏆",
                                fontSize = 11.sp,
                                color = colors.secondaryTextColor
                            )
                        }
                    }
                }
            }

            if (completeDialog) {
                AlertDialog(
                    onDismissRequest = { completeDialog = false },
                    title = {
                        Text(
                            "Congratulations! 🎉",
                            color = colors.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            "You finished all your daily habits! Keep up the amazing work!",
                            color = colors.textColor
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { completeDialog = false }) {
                            Text("Awesome!", color = colors.accentColor)
                        }
                    },
                    containerColor = colors.cardColor
                )
            }

            // Habit Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Habit Progress",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textColor
                    )
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = colors.accentColor,
                    )
                    Text(
                        text = "$completedHabits of $totalHabits tasks completed ${"%.0f".format(progress * 100)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Daily Habits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = colors.textColor,
                        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                    )
                    if (habits.isEmpty()) {
                        Text(
                            "No habits yet. Use the Add Habit button below to create one.",
                            color = colors.secondaryTextColor
                        )
                    } else {
                        habits.forEachIndexed { index, habit ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = habit.isCompletedToday,
                                    onCheckedChange = { isChecked ->
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null && habit.id.isNotEmpty()) {
                                            val updatedDates = if (isChecked) {
                                                StreakCalculator.addTodayCompletion(habit.completionDates)
                                            } else {
                                                StreakCalculator.removeTodayCompletion(habit.completionDates)
                                            }
                                            val newCurrentStreak = StreakCalculator.calculateCurrentStreak(updatedDates)
                                            val newLongestStreak =
                                                max(StreakCalculator.calculateLongestStreak(updatedDates), habit.longestStreak)
                                            val newTotalCompletions = updatedDates.size

                                            habits[index] = habit.copy(
                                                isCompletedToday = isChecked,
                                                completionDates = updatedDates,
                                                currentStreak = newCurrentStreak,
                                                longestStreak = newLongestStreak,
                                                totalCompletions = newTotalCompletions,
                                                lastCompleted = if (isChecked) System.currentTimeMillis() else habit.lastCompleted
                                            )

                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(userId)
                                                .collection("habits")
                                                .document(habit.id)
                                                .update(
                                                    mapOf(
                                                        "isCompletedToday" to isChecked,
                                                        "completionDates" to updatedDates,
                                                        "currentStreak" to newCurrentStreak,
                                                        "longestStreak" to newLongestStreak,
                                                        "totalCompletions" to newTotalCompletions,
                                                        "lastCompleted" to (if (isChecked) System.currentTimeMillis() else habit.lastCompleted)
                                                    )
                                                )
                                                .addOnSuccessListener {
                                                    if (isChecked && newCurrentStreak > 0) {
                                                        val message = StreakCalculator.getStreakMessage(
                                                            newCurrentStreak,
                                                            newLongestStreak
                                                        )
                                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Failed to update. Check your connection.", Toast.LENGTH_SHORT).show()
                                                }
                                        }

                                        val checkedCount = habits.count { it.isCompletedToday }
                                        progress = if (habits.isNotEmpty()) checkedCount.toFloat() / habits.size else 0f
                                    }
                                )
                                HabitItem(
                                    habit = habit,
                                    isDarkMode = isDarkMode,
                                    colors = colors,
                                    onHabitClick = { navController.navigate("EditHabit_Screen/${habit.id}") }
                                )
                            }
                            if (index < habits.size - 1) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
        BottomNavigationBar(navController, isDarkMode)
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    isDarkMode: Boolean,
    colors: AppThemeColors,
    onHabitClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onHabitClick),
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(24.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colors.textColor
            )
            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }
            if (habit.category.isNotBlank()) {
                Text(
                    text = "Category: ${habit.category}",
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "Frequency: ${habit.frequency}, Goal: ${habit.goalCount}",
                color = colors.secondaryTextColor,
                fontSize = 12.sp
            )
            habit.reminderTime?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Reminder: $it",
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }
            if (habit.currentStreak > 0 || habit.totalCompletions > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                if (habit.currentStreak > 0) {
                    Text(
                        text = "🔥 ${habit.currentStreak} day streak" +
                                if (habit.longestStreak > habit.currentStreak)
                                    " (Best: ${habit.longestStreak})"
                                else "",
                        color = colors.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (habit.totalCompletions > 0) {
                    Text(
                        text = "✅ Completed ${habit.totalCompletions} times",
                        color = colors.secondaryTextColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
