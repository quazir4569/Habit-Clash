package hexis.habitclash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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

@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("habits")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        habits.clear()
                        for (doc in snapshot.documents) {
                            val habit = doc.toObject(Habit::class.java)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
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
                        habits.forEach { habit ->
                            HabitItem(
                                habit = habit,
                                colors = colors
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}
//Habit Portion
@Composable
fun HabitItem(
    habit: Habit,
    colors: AppThemeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.accentColor),
            contentAlignment = Alignment.Center
        ) {}

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(habit.title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = colors.textColor)
            if (habit.description.isNotBlank()) {
                Text(habit.description, color = colors.secondaryTextColor, fontSize = 13.sp)
            }
            if (habit.category.isNotBlank()) {
                Text("Category: ${habit.category}", color = colors.secondaryTextColor, fontSize = 13.sp)
            }
            Text("Frequency: ${habit.frequency}, Goal: ${habit.goalCount}", color = colors.secondaryTextColor, fontSize = 13.sp)
            if (!habit.reminderTime.isNullOrBlank()) {
                Text("Reminder: ${habit.reminderTime}", color = colors.secondaryTextColor, fontSize = 13.sp)
            }
        }

        if (habit.isCompletedToday) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}
