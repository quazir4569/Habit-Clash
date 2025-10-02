package hexis.habitclash

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val habits = remember { mutableStateListOf<Habit>() }
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scrollState = rememberScrollState()

    var username by remember { mutableStateOf("User") }
    var showDialog by remember { mutableStateOf(false) }

    // window keys so UI knows what counts as “done”
    val todayKey = remember { dayKeyUtc() }
    val weekKey = remember { weekKeyUtc() }
    val monthKey = remember { monthKeyUtc() }

    // completion flags
    val doneMap = remember { mutableStateMapOf<String, Boolean>() }

    // load user + habits
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { d -> username = d.getString("username") ?: "User" }

        db.collection("users").document(uid)
            .collection("habits")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    habits.clear()
                    for (doc in snap.documents) {
                        doc.toObject(Habit::class.java)?.copy(id = doc.id)?.let { habits += it }
                    }
                }
            }

        // listen to completion logs for the three windows we care about
        db.collection("users").document(uid)
            .collection("completion_logs")
            .whereIn("dateKey", listOf(todayKey, weekKey, monthKey))
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    // rebuild map on every change
                    doneMap.clear()
                    for (doc in snap.documents) {
                        val habitId = doc.getString("habitId") ?: continue
                        val dateKey = doc.getString("dateKey") ?: continue
                        val completed = doc.getBoolean("completed") == true
                        doneMap["${habitId}_${dateKey}"] = completed
                    }
                }
            }
    }

    // redirect to login if needed
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("Login_Screen")
        }
    }

    val totalToday = habits.count { it.frequency.equals("Daily", ignoreCase = true) } // only daily target for the day
    val completedToday = habits.count {
        it.frequency.equals("Daily", ignoreCase = true) &&
                (doneMap["${it.id}_${todayKey}"] ?: false)
    }
    val progress = if (totalToday > 0) completedToday.toFloat() / totalToday else 0f
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }

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
                                text = "0 Day Streak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textColor)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { showDialog = true

                            fetchLeaderboard { data ->
                                leaderboard = data
                            }}
                            ) {
                            Text("Show Leaderboard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textColor)
                        }
                    }
                }
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
                                Column {
                                    // Header
                                    Row {
                                        Text("Rank", modifier = Modifier.weight(1f))
                                        Text("Name", modifier = Modifier.weight(3f))
                                        Text("Score", modifier = Modifier.weight(2f))
                                    }
                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        color = Color.Gray
                                    )

                                    leaderboard.forEach { entry ->
                                        Row {
                                            Text(entry.rank.toString(), modifier = Modifier.weight(1f))
                                            Text(entry.name, modifier = Modifier.weight(3f))
                                            Text(entry.score.toString(), modifier = Modifier.weight(2f))
                                        }
                                        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                    navController.popBackStack()
                                }) {
                                    Text("Return!")
                                }
                            }
                        )
                    }
                }
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
                    Text(text = "Habit Progress", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textColor)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = colors.accentColor,
                    )

                    Text(
                        text = "$completedToday of $totalToday tasks completed ${"%.0f".format(progress * 100)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Your Habits",
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
                                val windowKey = when (habit.frequency.lowercase(Locale.US)) {
                                    "daily" -> todayKey
                                    "weekly" -> weekKey
                                    "monthly" -> monthKey
                                    else -> todayKey
                                }
                                val key = "${habit.id}_${windowKey}"
                                val isChecked = doneMap[key] ?: false

                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        toggleCompletion(
                                            navController = navController,
                                            habit = habit,
                                            windowKey = windowKey,
                                            checked = checked
                                        )
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

        BottomNavigationBar(navController, isDarkMode)
    }
}

@Composable
fun HabitItemWithEdit(
    habit: Habit,
    isDarkMode: Boolean,
    colors: AppThemeColors,
    navController: NavController,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("Edit_Habit/${habit.id}") },
        verticalAlignment = Alignment.Top
    ) {
        Spacer(modifier = Modifier.width(24.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(habit.title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = colors.textColor)
            if (habit.description.isNotBlank()) {
                Text(habit.description, color = colors.secondaryTextColor, fontSize = 12.sp)
            }
            if (habit.category.isNotBlank()) {
                Text("Category: ${habit.category}", color = colors.secondaryTextColor, fontSize = 12.sp)
            }
            Text("Frequency: ${habit.frequency}, Goal: ${habit.goalCount}", color = colors.secondaryTextColor, fontSize = 12.sp)
            habit.reminderTime?.takeIf { it.isNotBlank() }?.let {
                Text("Reminder: $it", color = colors.secondaryTextColor, fontSize = 12.sp)
            }
        }
    }
}

private fun toggleCompletion(
    navController: NavController,
    habit: Habit,
    windowKey: String,
    checked: Boolean
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val docId = "${habit.id}_$windowKey"
    val ref = db.collection("users").document(uid)
        .collection("completion_logs").document(docId)

    if (checked) {
        val data = hashMapOf(
            "habitId" to habit.id,
            "userId" to uid,
            "dateKey" to windowKey,
            "frequency" to habit.frequency,
            "completed" to true,
            "updatedAt" to Timestamp.now()
        )
        ref.set(data)
    } else {
        // uncheck removes the record for that window
        ref.delete().addOnFailureListener {
            // if delete fails, it will mark as not completed
            ref.set(
                mapOf(
                    "habitId" to habit.id,
                    "userId" to uid,
                    "dateKey" to windowKey,
                    "frequency" to habit.frequency,
                    "completed" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }
    }
}

private fun dayKeyUtc(dateMillis: Long = System.currentTimeMillis()): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(dateMillis)
}

private fun weekKeyUtc(dateMillis: Long = System.currentTimeMillis()): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
    cal.timeInMillis = dateMillis
    val year = cal.get(Calendar.YEAR)
    val week = cal.get(Calendar.WEEK_OF_YEAR)
    return String.format(Locale.US, "%04d-W%02d", year, week)
}

private fun monthKeyUtc(dateMillis: Long = System.currentTimeMillis()): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(dateMillis)
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

