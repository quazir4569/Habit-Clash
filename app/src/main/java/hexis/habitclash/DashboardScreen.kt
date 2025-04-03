package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
 * Main dashboard screen showing user's habits.
 * Loads habits from Firebase and displays them in a list.
 */
@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    var username by remember { mutableStateOf("User") }
    val checkStates = remember { mutableStateListOf<Boolean>() }
    val totalCount = habits.size
    val checkedCount = habits.count {it.isCompletedToday }
    val progress =  if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f
    var checked by remember { mutableStateOf(true) }




    // Load data from Firebase when screen opens
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
                        habits.clear()
                        for (doc in snapshot.documents) {
                            val habit = doc.toObject(Habit::class.java)?.copy(id = doc.id)
                            if (habit != null) habits.add(habit)
                        }
                    }
                }
        }
    }

    // Redirect to login if not authenticated
    if (authState.value is AuthState.Unauthenticated) {
        LaunchedEffect(Unit) {
            navController.navigate("Login_Screen")
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
                    // Profile icon
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
                        Text(
                            text = "Hello, @$username",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Level and streak
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Level pill
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

                            // Streak indicator
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
                    Text(text = "Habit Progress", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    // Progress Bar
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

                    // Show message if no habits, otherwise show habit list
                    if (habits.isEmpty()) {
                        Text(
                            "No habits yet. Use the Add Habit button below to create one.",
                            color = colors.secondaryTextColor
                        )
                    } else {
                        habits.forEachIndexed { index, habit ->



                            HabitItemWithEdit(

                                habit = habit,
                                isDarkMode = isDarkMode,
                                colors = colors,
                                navController = navController,
                                onCheckboxClicked = {updatinghabits ->
                                    habits[index] = updatinghabits


                                }

                            )


                            // Only add spacing if it's not the last item
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

/**
 * Displays a habit item with ability to toggle completion and navigate to edit screen.
 */
@Composable
fun HabitItemWithEdit(
    habit: Habit,
    isDarkMode: Boolean,
    colors: AppThemeColors,
    navController: NavController,
    onCheckboxClicked: (Habit) -> Unit
) {
    var isCompleted by remember { mutableStateOf(habit.isCompletedToday) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("Edit_Habit/${habit.id}")
            },
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isCompleted) colors.accentColor else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = colors.accentColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable(onClick = {
                    // Toggle completion state
                    isCompleted = !isCompleted

                    onCheckboxClicked(habit.copy(isCompletedToday = isCompleted))

                    // Update in Firestore
                    if (userId != null && habit.id.isNotEmpty()) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("habits")
                            .document(habit.id)
                            .update("isCompletedToday", isCompleted)
                    }
                }),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Habit details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title
            Text(
                text = habit.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colors.textColor
            )

            // Description
            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }

            // Category
            if (habit.category.isNotBlank()) {
                Text(
                    text = "Category: ${habit.category}",
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }

            // Frequency and Goal
            Text(
                text = "Frequency: ${habit.frequency}, Goal: ${habit.goalCount}",
                color = colors.secondaryTextColor,
                fontSize = 12.sp
            )

            // Reminder
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