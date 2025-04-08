package hexis.habitclash

import android.util.Half.toFloat
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors

/**
 * Dashboard screen showing user progress and habits.
 * Main screen after login where users can view and check off habits.
 */
@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    // State and data variables
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()
    var username by remember { mutableStateOf("User") }
    val totalCount = habits.size
    val checkedCount = habits.count {it.isCompletedToday }
    var completeDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val newUpdatedHabit = remember {mutableStateListOf<String>()}

    // Load user data from Firebase
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Load username
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("username") ?: "User"
                    }
                }

            // Load habits
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("habits")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val updatedHabits = mutableListOf<Habit>()
                        for (doc in snapshot.documents) {
                            val habit = doc.toObject(Habit::class.java)?.copy(id = doc.id)
                            if (habit != null) {
                                // Only add if it's not in the "recently updated" set
                                if (!newUpdatedHabit.contains(habit.id)) {
                                    updatedHabits.add(habit)
                                }
                            }
                        }

                        // Replace only habits that weren't just updated
                        updatedHabits.forEach { newHabit ->
                            val index = habits.indexOfFirst { it.id == newHabit.id }
                            if (index != -1) {
                                habits[index] = newHabit
                            } else {
                                habits.add(newHabit)
                            }
                        }

                        // Clean up the recently updated list
                        newUpdatedHabit.clear()

                        // Update progress
                        progress = if (habits.isNotEmpty()) {
                            habits.count { it.isCompletedToday }.toFloat() / habits.size
                        } else 0f
                    }
                }

        }
    }

    // Check authentication status
    if (authState.value is AuthState.Unauthenticated) {
        LaunchedEffect(Unit) {
            navController.navigate("Login_Screen")
        }
    }

    // Calculate progress
    val totalHabits = habits.size
    val completedHabits = habits.count {it.isCompletedToday}
    progress = if(totalHabits > 0 ) completedHabits.toFloat() / totalHabits else 0f

    // Show completion dialog if all habits are done
    if( progress == 1f && !completeDialog){
        completeDialog = true
    }

    // Main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Profile card
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
                    // User avatar
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

                    // User info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Username with Inter SemiBold font
                        Text(
                            text = "Hello, @$username",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            color = colors.textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Level and streak indicators
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

            // Progress card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Progress headline with Inter SemiBold font
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = colors.textColor
                    )

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = colors.accentColor,
                    )

                    // Progress stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Task completion text
                        Text(
                            text = "$checkedCount of $totalCount tasks completed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textColor
                        )

                        // Percentage indicator
                        Text(
                            text = "${"%.0f".format(progress * 100)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentColor
                        )
                    }
                }
            }

            // Completion dialog
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

            // Habits card
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
                    // Habits headline with Inter SemiBold font
                    Text(
                        text = "Daily Habits",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = colors.textColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Show placeholder text if no habits exist
                    if (habits.isEmpty()) {
                        Text(
                            "No habits yet. Use the Add Habit button below to create one.",
                            fontSize = 12.sp,
                            color = colors.secondaryTextColor
                        )
                    } else {
                        // List of habits with checkboxes
                        habits.forEachIndexed { index, habit ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                // Checkbox to mark habit as complete
                                Checkbox(
                                    checked = habit.isCompletedToday,
                                    onCheckedChange = { isChecked ->
                                        val updated = habit.copy(isCompletedToday = isChecked)


                                        val userId = FirebaseAuth.getInstance().currentUser?.uid

                                        if (userId != null && habit.id.isNotEmpty()) {
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(userId)
                                                .collection("habits")
                                                .document(habit.id)
                                                .update("isCompletedToday", isChecked)
                                        }

                                        newUpdatedHabit.add(habit.id)

                                        habits[index] = updated

                                        // Update progress when checkbox is clicked
                                        progress = if(habits.isNotEmpty()){ habits.count{it.isCompletedToday}.toFloat() / habits.size }else 0f
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(0.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.accentColor,
                                        uncheckedColor = colors.secondaryTextColor
                                    )
                                )

                                // Habit details
                                HabitItemWithEdit(
                                    habit = habit,
                                    isDarkMode = isDarkMode,
                                    colors = colors,
                                    navController = navController,
                                )
                            }

                            // Add spacing between habits
                            if (index < habits.size - 1) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }

        // Bottom navigation bar
        BottomNavigationBar(navController, isDarkMode)
    }
}

/**
 * Individual habit item component.
 * Shows habit details and handles click to edit.
 */
@Composable
fun HabitItemWithEdit(
    habit: Habit,
    isDarkMode: Boolean,
    colors: AppThemeColors,
    navController: NavController,
) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("Edit_Habit/${habit.id}")
            },
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(24.dp))

        // Habit information
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Habit title
            Text(
                text = habit.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colors.textColor
            )

            // Habit description (if any)
            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }

            // Habit category (if any)
            if (habit.category.isNotBlank()) {
                Text(
                    text = "Category: ${habit.category}",
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }

            // Habit frequency and goal
            Text(
                text = "Frequency: ${habit.frequency}, Goal: ${habit.goalCount}",
                color = colors.secondaryTextColor,
                fontSize = 12.sp
            )
        }
    }
}