package hexis.habitclash

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hexis.habitclash.ui.theme.getAppThemeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDark = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDark)

    val rows = remember { mutableStateListOf<HistoryRow>() }
    val habitTitles = remember { mutableStateMapOf<String, String>() }

    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        // if not signed in, stop loading and show a message
        if (uid == null) {
            loading = false
            errorMsg = "You’re not signed in."
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()

        // load habit titles
        db.collection("users").document(uid).collection("habits")
            .get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { d ->
                    habitTitles[d.id] = d.getString("title") ?: "Untitled"
                }
            }
            .addOnFailureListener { e ->
                Log.e("HistoryScreen", "Failed to load habits", e)
            }

        // listen to recent logs
        db.collection("users").document(uid)
            .collection("completion_logs")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, err ->
                loading = false
                if (err != null) {
                    errorMsg = "Couldn’t load history. Check your connection / Firestore."
                    Log.e("HistoryScreen", "Snapshot error", err)
                    return@addSnapshotListener
                }
                if (snap == null) {
                    errorMsg = "No data available."
                    return@addSnapshotListener
                }

                rows.clear()
                snap.documents.forEach { d ->
                    val habitId = d.getString("habitId") ?: return@forEach
                    val completed = d.getBoolean("completed") == true
                    val updatedAt = d.getTimestamp("updatedAt")?.toDate() ?: Date()
                    rows += HistoryRow(
                        dateKey = utcDayKey(updatedAt),
                        habitTitle = habitTitles[habitId] ?: habitId,
                        actionText = if (completed) "Completed" else "Unchecked",
                        whenText = prettyWhen(updatedAt)
                    )
                }

                errorMsg = null
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        TopAppBar(
            title = { Text("History Log", color = colors.textColor, fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.cardColor)
        )

        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.accentColor)
                }
            }
            errorMsg != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMsg!!, color = colors.secondaryTextColor)
                }
            }
            rows.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No history yet.", color = colors.secondaryTextColor)
                }
            }
            else -> {
                val grouped = rows.groupBy { it.dateKey }
                val datesDesc = grouped.keys.sortedDescending()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    datesDesc.forEach { date ->
                        item(key = "header_$date") {
                            DateHeader(date, colors)
                        }
                        val itemsForDate = grouped[date].orEmpty()
                        itemsIndexed(itemsForDate, key = { idx, item -> "$date-$idx-${item.hashCode()}" }) { _, r ->
                            HistoryRowCard(r, colors)
                        }
                    }
                }
            }
        }

        BottomNavigationBar(navController, isDark)
    }
}

@Composable
private fun DateHeader(date: String, colors: hexis.habitclash.ui.theme.AppThemeColors) {
    Text(
        text = prettyDateLabel(date),
        style = MaterialTheme.typography.titleSmall,
        color = colors.textColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun HistoryRowCard(
    row: HistoryRow,
    colors: hexis.habitclash.ui.theme.AppThemeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.cardColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(row.habitTitle, color = colors.textColor, modifier = Modifier.weight(1f))
            Text(row.actionText, color = if (row.actionText == "Completed") Color(0xFF2E7D32) else Color(0xFFC62828))
            Spacer(Modifier.width(12.dp))
            Text(row.whenText, color = colors.secondaryTextColor)
        }
    }
}

data class HistoryRow(
    val dateKey: String,
    val habitTitle: String,
    val actionText: String,
    val whenText: String
)

private fun utcDayKey(date: Date = Date()): String {
    val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(date)
}

private fun prettyDateLabel(key: String): String {
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outFmt = SimpleDateFormat("MMM d, yyyy", Locale.US)
        outFmt.format(inFmt.parse(key) ?: Date())
    } catch (_: Exception) {
        key
    }
}

private fun prettyWhen(d: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - d.time
    val min = 60_000L
    val hr = 60 * min
    val day = 24 * hr
    return when {
        diff < min -> "just now"
        diff < hr -> "${diff / min}m ago"
        diff < day -> "${diff / hr}h ago"
        else -> SimpleDateFormat("MMM d", Locale.US).format(d)
    }
}
