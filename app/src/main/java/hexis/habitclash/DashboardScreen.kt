package hexis.habitclash

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Main dashboard screen showing user's habits.
 * Loads habits from Firebase and displays them in a list.
 */
@Composable
fun DashboardScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()
    var username by remember { mutableStateOf("User") }
    val totalCount = habits.size
    val checkedCount = habits.count { it.isCompletedToday }
    var completeDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("username") ?: "User"
                    }
                }

            FirebaseFirestore.getInstance()
                .collection("users")
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
    }

    if (authState.value is AuthState.Unauthenticated) {
        LaunchedEffect(Unit) {
            navController.navigate("Login_Screen")
        }
    }

    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompletedToday }
    progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

    if (progress == 1f && !completeDialog) {
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

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Hello, @$username",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                text = "0 Day Streak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textColor
                            )
                        }
                    }
                }
            }

            if (completeDialog) {
                AlertDialog(
                    onDismissRequest = { completeDialog = false },
                    title = { Text("Congratulation!") },
                    text = { Text("You finished your daily habits!") },
                    confirmButton = {
                        TextButton(onClick = {
                            completeDialog = false
                            navController.popBackStack()
                        }) {
                            Text("Lets Go back!")
                        }
                    }
                )
            }

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
                    Text(text = "Habit Progress", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = Color.Blue,
                    )

                    Text(
                        text = "$checkedCount of $totalCount tasks completed ${"%.0f".format(progress * 100)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Daily Habits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = colors.textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                                        habits[index] = habit.copy(isCompletedToday = isChecked)

                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null && habit.id.isNotEmpty()) {
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(userId)
                                                .collection("habits")
                                                .document(habit.id)
                                                .update("isCompletedToday", isChecked)
                                        }

                                        val checkedNow = habits.count { it.isCompletedToday }
                                        progress = if (habits.isNotEmpty()) checkedNow.toFloat() / habits.size else 0f
                                    }
                                )

                                HabitItemWithEdit(
                                    habit = habit,
                                    isDarkMode = isDarkMode,
                                    colors = colors,
                                    navController = navController
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

        BottomNavigationBar(navController = navController, isDarkMode = isDarkMode)
    }
}

/**
 * Displays a habit item with ability to toggle completion and navigate to edit screen.
 */
@Composable
fun HabitItemWithEdit(
    habit: Habit,
    isDarkMode: Boolean,
    colors: AppThemeColors,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("EditHabit_Screen/${habit.id}")
            },
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
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

            if (!habit.reminderTime.isNullOrBlank()) {
                Text(
                    text = "Reminder: ${habit.reminderTime}",
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}
