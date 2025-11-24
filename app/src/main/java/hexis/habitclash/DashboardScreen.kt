package hexis.habitclash

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors
import kotlin.math.max

// Badge data model (place at top for now, refactor as needed)
data class AchievementBadge(
    val id: String,
    val name: String,
    val description: String,
    val milestone: Int,
    val category: String,
    var isAchieved: Boolean = false
)

// Define scalable milestone badge catalog
val milestoneBadges = listOf(
    AchievementBadge("streak_7", "7-Day Streak", "Achieve a 7-day streak", 7, "streak"),
    AchievementBadge("streak_30", "30-Day Streak", "Achieve a 30-day streak", 30, "streak")
)

@Composable
fun DashboardScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()
    val logo = painterResource(R.drawable.hc)
    val dodgerBlue = Color(0xFF1E90FF)

    var username by remember { mutableStateOf("User") }
    var completeDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var showDialog by remember { mutableStateOf(false) }
    var showFriendDialog by remember { mutableStateOf(false) }
    var showMultiplayerLeaderboardDialog by remember { mutableStateOf(false) }

    val todayKey = remember { StreakCalculator.getTodayKey() }

    val totalCurrentStreak = habits.sumOf { it.currentStreak }
    val currentTier = StreakCalculator.getTierForStreak(totalCurrentStreak)
    val bestStreak = habits.maxOfOrNull { it.longestStreak } ?: 0
    val totalHabits = habits.size
    val completedHabits = habits.count { it.completionDates.contains(todayKey) }
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }

    // Compute badges achieved (scalable for future milestones)
    val achievedBadges = milestoneBadges.map { badge ->
        badge.copy(isAchieved = (totalCurrentStreak >= badge.milestone))
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()
        viewModel.loadFriends()

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { d -> username = d.getString("username") ?: "User" }

        db.collection("users").document(userId).collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    habits.clear()
                    snapshot.documents.forEach { doc ->
                        doc.toObject(Habit::class.java)?.copy(id = doc.id)?.let { habits += it }
                    }
                }
            }
    }

    LaunchedEffect(progress) {
        if (progress == 1f) {
            completeDialog = true
        }
    }

    if (authState.value is AuthState.Unauthenticated) {
        LaunchedEffect(Unit) { navController.navigate("Login_Screen") }
    }

    progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
    /*if (progress == 1f && !completeDialog) {completeDialog = true}*/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dodgerBlue)
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
                colors = CardDefaults.cardColors(containerColor = colors.cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(21.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {

                        Image(
                            painter = logo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                        )
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
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hello, @$username",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
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
                                        "Level 0",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (totalCurrentStreak > 0) "$totalCurrentStreak Day Streak 🔥" else "Start your streak! 🎯",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (totalCurrentStreak > 0) colors.accentColor else colors.textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tier: ${currentTier.label}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (currentTier) {
                                    StreakCalculator.Tier.BRONZE -> Color(0xFFCD7F32)
                                    StreakCalculator.Tier.SILVER -> Color(0xFFC0C0C0)
                                    StreakCalculator.Tier.GOLD -> Color(0xFFFFD700)
                                    else -> colors.textColor
                                }
                            )
                        }

                        // BADGE ROW: Achievements (Milestone Badges)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Achievements",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textColor
                        )
                        Row {
                            achievedBadges.forEach { badge ->
                                val badgeColor =
                                    if (badge.isAchieved) Color(0xFF4CAF50) else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .padding(end = 7.dp)
                                        .background(badgeColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        badge.name.take(2),
                                        color = Color.White,
                                        fontWeight = Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }

                        if (bestStreak > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Best Streak: $bestStreak days 🏆",
                                fontSize = 11.sp,
                                color = colors.secondaryTextColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Button(onClick = {
                                showDialog = true

                                fetchLeaderboard { data ->
                                    leaderboard = data
                                }
                            }
                            ) {
                                Text(
                                    "Leaderboard",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textColor
                                )
                            }
                            Button(onClick = {
                                showMultiplayerLeaderboardDialog = true
                                viewModel.loadFriendsLeaderboard()
                            }
                            ) {
                                Text(
                                    "Multiplayer",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textColor
                                )
                            }

                            if (showMultiplayerLeaderboardDialog) {
                                MultiplayerDialog(
                                    leaderboard = viewModel.leaderboard.value,
                                    onDismiss = { showMultiplayerLeaderboardDialog = false }
                                )
                            }
                        }
                    }
                }
            }

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
                        "Habit Progress",
                        fontSize = 20.sp,
                        fontWeight = Bold,
                        color = colors.textColor
                    )

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = colors.accentColor
                    )

                    Text(
                        "$completedHabits of $totalHabits tasks completed ${"%.0f".format(progress * 100)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Daily Habits",
                        fontWeight = Bold,
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
                                val isCheckedToday = habit.completionDates.contains(todayKey)

                                Checkbox(
                                    checked = isCheckedToday,
                                    onCheckedChange = { isChecked ->
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null && habit.id.isNotEmpty()) {
                                            val updatedDates =
                                                if (isChecked)
                                                    StreakCalculator.addTodayCompletion(
                                                        habit.completionDates,
                                                        todayKey
                                                    )
                                                else
                                                    StreakCalculator.removeTodayCompletion(
                                                        habit.completionDates,
                                                        todayKey
                                                    )

                                            val newCurrent =
                                                StreakCalculator.calculateCurrentStreak(
                                                    updatedDates,
                                                    todayKey
                                                )
                                            val newLongest = max(
                                                StreakCalculator.calculateLongestStreak(updatedDates),
                                                habit.longestStreak
                                            )
                                            val newTotal = updatedDates.size

                                            habits[index] = habit.copy(
                                                isCompletedToday = isChecked,
                                                completionDates = updatedDates,
                                                currentStreak = newCurrent,
                                                longestStreak = newLongest,
                                                totalCompletions = newTotal,
                                                lastCompleted = if (isChecked) System.currentTimeMillis() else habit.lastCompleted
                                            )

                                            val db = FirebaseFirestore.getInstance()
                                            val habitRef = db.collection("users").document(userId)
                                                .collection("habits").document(habit.id)
                                            val logRef = db.collection("users").document(userId)
                                                .collection("completion_logs")
                                                .document("${habit.id}_${todayKey}")

                                            habitRef.update(
                                                mapOf(
                                                    "isCompletedToday" to isChecked,
                                                    "completionDates" to updatedDates,
                                                    "currentStreak" to newCurrent,
                                                    "longestStreak" to newLongest,
                                                    "totalCompletions" to newTotal,
                                                    "lastCompleted" to if (isChecked) System.currentTimeMillis() else habit.lastCompleted
                                                )
                                            )

                                            if (isChecked) {
                                                logRef.set(
                                                    mapOf(
                                                        "habitId" to habit.id,
                                                        "userId" to userId,
                                                        "dateKey" to todayKey,
                                                        "frequency" to habit.frequency,
                                                        "completed" to true,
                                                        "updatedAt" to FieldValue.serverTimestamp()
                                                    )
                                                )
                                            } else {
                                                logRef.delete().addOnFailureListener {
                                                    logRef.set(
                                                        mapOf(
                                                            "habitId" to habit.id,
                                                            "userId" to userId,
                                                            "dateKey" to todayKey,
                                                            "frequency" to habit.frequency,
                                                            "completed" to false,
                                                            "updatedAt" to FieldValue.serverTimestamp()
                                                        )
                                                    )
                                                }
                                            }

                                            // motivational alert
                                            if (isChecked && newCurrent > 0) {
                                                MotivationalAlerts.checkAndShow(
                                                    context = context,
                                                    habitId = habit.id,
                                                    currentStreak = newCurrent
                                                )
                                            }

                                            /*if (isChecked && newCurrent > 0) {
                                                Toast.makeText(
                                                    context,
                                                    StreakCalculator.getStreakMessage(
                                                        newCurrent,
                                                        newLongest
                                                    ),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }*/
                                        }
                                        val checkedCount =
                                            habits.count { it.completionDates.contains(todayKey) }
                                        progress =
                                            if (habits.isNotEmpty()) checkedCount.toFloat() / habits.size else 0f
                                    }
                                )

                                HabitItem(
                                    habit = habit,
                                    isDarkMode = isDarkMode,
                                    colors = colors,
                                    onHabitClick = { navController.navigate("Edit_Habit/${habit.id}") }
                                )
                            }
                            if (index < habits.size - 1) Spacer(modifier = Modifier.height(24.dp))
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

                        }) {
                            Text("Lets Go back!")
                        }
                    }
                )
            }

            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    AnimatedVisibility(
                        visible = showDialog,
                        enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f),
                        exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
                    ) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Leaderboard") },
                            text = {

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Rank", fontWeight = Bold)
                                        Text("Name", fontWeight = Bold)
                                        Text("Score", fontWeight = Bold)
                                    }
                                    HorizontalDivider(
                                        Modifier.padding(vertical = 4.dp),
                                        thickness = DividerDefaults.Thickness,
                                        color = Color.Blue
                                    )
                                    leaderboard.forEach { entry ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(entry.rank.toString())
                                            Text(entry.name)
                                            Text(entry.score.toString())
                                        }
                                        HorizontalDivider()
                                    }
                                }
                            },

                            confirmButton = {
                                TextButton(onClick = {
                                    showDialog = false

                                }) { Text("Return!") }
                            }
                        )
                    }
                }
            }

            if (showFriendDialog) {
                Dialog(onDismissRequest = { showFriendDialog = false }) {
                    AnimatedVisibility(
                        visible = showFriendDialog,
                        enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f),
                        exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
                    ) {

                        AlertDialog(
                            onDismissRequest = { showFriendDialog = false },
                            title = { Text("Add a Friend") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = username,
                                        onValueChange = { username = it },
                                        label = { Text("Enter user's name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    viewModel.addFriendMessage?.let {
                                        Text(
                                            text = it,
                                            color = if (it.contains(
                                                    "success",
                                                    true
                                                )
                                            ) Color.Green else Color.Red,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (username.isNotBlank()) {
                                        viewModel.addFriendByUsername(username.trim())
                                        username = ""
                                    }
                                }) {
                                    Text("Add")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showFriendDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
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
                habit.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colors.textColor
            )
            if (habit.description.isNotBlank()) Text(
                habit.description,
                color = colors.secondaryTextColor,
                fontSize = 12.sp
            )
            if (habit.category.isNotBlank()) Text(
                "Category: ${habit.category}",
                color = colors.secondaryTextColor,
                fontSize = 12.sp
            )
            Text(
                "Frequency: ${habit.frequency}, Goal: ${habit.goalCount}",
                color = colors.secondaryTextColor,
                fontSize = 12.sp
            )
            habit.reminderTime?.takeIf { it.isNotBlank() }
                ?.let { Text("Reminder: $it", color = colors.secondaryTextColor, fontSize = 12.sp) }
            if (habit.currentStreak > 0 || habit.totalCompletions > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                if (habit.currentStreak > 0) {
                    Text(
                        text = "🔥 ${habit.currentStreak} day streak" + if (habit.longestStreak > habit.currentStreak) " (Best: ${habit.longestStreak})" else "",
                        color = colors.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (habit.totalCompletions > 0) {
                    Text(
                        "✅ Completed ${habit.totalCompletions} times",
                        color = colors.secondaryTextColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

fun fetchLeaderboard(onResult: (List<LeaderboardEntry>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users").get()
        .addOnSuccessListener { result ->
            val tempList = mutableListOf<LeaderboardEntry>()
            var processedCount = 0

            if (result.isEmpty) {
                onResult(emptyList())
                return@addOnSuccessListener
            }

            result.documents.forEach { userDoc ->
                val username = userDoc.getString("username") ?: "Unknown"

                userDoc.reference.collection("completion_logs").get()
                    .addOnSuccessListener { actions ->
                        val completedCount = actions.size()
                        tempList.add(
                            LeaderboardEntry(
                                rank = 0,
                                name = username,
                                score = completedCount
                            )
                        )

                        processedCount++
                        if (processedCount == result.size()) {
                            val ranked = tempList
                                .sortedByDescending { it.score }
                                .mapIndexed { idx, entry ->
                                    entry.copy(rank = idx + 1)
                                }
                            onResult(ranked)
                        }
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching leaderboard", e)
            onResult(emptyList())
        }
}

@Composable
fun MultiplayerDialog(
    leaderboard: List<LeaderboardEntry>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f)
        ) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Multiplayer Leaderboard") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rank", fontWeight = Bold)
                            Text("Name", fontWeight = Bold)
                            Text("Score", fontWeight = Bold)
                        }

                        HorizontalDivider(
                            Modifier.padding(vertical = 4.dp),
                            thickness = DividerDefaults.Thickness,
                            color = Color.Blue
                        )

                        leaderboard.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.rank.toString())
                                Text(entry.name)
                                Text(entry.score.toString())
                            }
                            HorizontalDivider()
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Return!")
                    }
                }
            )
        }
    }
}
