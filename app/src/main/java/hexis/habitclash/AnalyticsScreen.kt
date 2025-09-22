package hexis.habitclash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

// one completion row from Firestore
data class CompletionLog(
    val habitId: String = "",
    val completedAt: Timestamp = Timestamp.now(),
    val dateKey: String = "" // "YYYY-MM-DD" in UTC
)

@Composable
fun AnalyticsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDarkMode = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDarkMode)
    val scroll = rememberScrollState()

    var loading by remember { mutableStateOf(true) }
    var logs by remember { mutableStateOf(listOf<CompletionLog>()) }

    // load recent completions when we land here
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            logs = loadCompletions(uid)
        }
        loading = false
    }

    // build date windows as "yyyy-MM-dd" strings
    val todayKey = remember { utcKey(Date()) }
    val last14Keys = remember {
        buildKeys(daysBack = 14) // 14 keys including today
    }
    val last7Keys = remember { buildKeys(daysBack = 7) }
    val last30Keys = remember { buildKeys(daysBack = 30) }

    // group logs by dateKey for quick lookups
    val logsByDate: Map<String, List<CompletionLog>> = remember(logs) {
        logs.groupBy { it.dateKey }
    }

    // line series: number of completions per day for last 14 days
    val series14 = last14Keys.map { key -> (logsByDate[key]?.size ?: 0).toFloat() }

    // completion rate windows
    val completionAnyLast7 = last7Keys.count { !logsByDate[it].isNullOrEmpty() }
    val completionAnyLast30 = last30Keys.count { !logsByDate[it].isNullOrEmpty() }
    val pct7 = if (last7Keys.isNotEmpty()) completionAnyLast7 * 100f / last7Keys.size else 0f
    val pct30 = if (last30Keys.isNotEmpty()) completionAnyLast30 * 100f / last30Keys.size else 0f

    // show most recent 10 completions
    val recent = logs.sortedByDescending { it.completedAt.toDate().time }.take(10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        TopBar(colors, navController)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
           // Line Chart
            SectionCard(colors) {
                Text("Streak & Trends", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textColor)
                Spacer(Modifier.height(12.dp))
                LineChart(
                    values = series14,
                    labels = last14Keys.map { key -> dayOfMonthFromKey(key) },
                    colors = colors,
                    height = 160.dp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Last 14 days (number of completions per day)",
                    color = colors.secondaryTextColor,
                    fontSize = 12.sp
                )
            }


            SectionCard(colors) {
                Text("Completion Rate", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textColor)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatPill(title = "7 days", value = "${"%.0f".format(pct7)}%", colors = colors)
                    StatPill(title = "30 days", value = "${"%.0f".format(pct30)}%", colors = colors)
                }
                Spacer(Modifier.height(12.dp))
                BarChart(
                    values = last7Keys.map { key -> if (logsByDate[key].isNullOrEmpty()) 0f else 1f },
                    labels = last7Keys,
                    colors = colors,
                    height = 120.dp
                )
                Spacer(Modifier.height(4.dp))
                Text("Last 7 days (any completion counts as 1)", color = colors.secondaryTextColor, fontSize = 12.sp)
            }

            // Recent Activities List
            SectionCard(colors) {
                Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textColor)
                Spacer(Modifier.height(8.dp))
                if (recent.isEmpty()) {
                    Text("No recent completions yet.", color = colors.secondaryTextColor)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recent.forEach { item ->
                            // format timestamp as yyyy-MM-dd (UTC)
                            val whenKey = utcKey(item.completedAt.toDate())
                            ActivityRow(
                                title = "Habit ${item.habitId.take(8)}…",
                                subtitle = whenKey,
                                colors = colors
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        BottomNavigationBar(navController, isDarkMode)
    }
}


// fetches latest completion docs for the user
private suspend fun loadCompletions(uid: String): List<CompletionLog> {
    val db = FirebaseFirestore.getInstance()
    val snap = db.collection("users")
        .document(uid)
        .collection("habit_completions")
        .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .limit(120)
        .get()
        .await()

    return snap.documents.mapNotNull { doc ->
        val habitId = doc.getString("habitId") ?: return@mapNotNull null
        val ts = doc.getTimestamp("completedAt") ?: Timestamp.now()
        val dateKey = doc.getString("dateKey") ?: utcKey(ts.toDate())
        CompletionLog(habitId = habitId, completedAt = ts, dateKey = dateKey)
    }
}


// simple top bar with back arrow
@Composable
private fun TopBar(colors: AppThemeColors, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textColor)
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.textColor)
        }
    }
}

// card wrapper per section
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
private fun StatPill(title: String, value: String, colors: AppThemeColors) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.fieldContainerColor,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textColor)
            Text(title, fontSize = 12.sp, color = colors.secondaryTextColor)
        }
    }
}

// one row for recent activity
@Composable
private fun ActivityRow(title: String, subtitle: String, colors: AppThemeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(colors.accentColor, RoundedCornerShape(50))
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = colors.textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = colors.secondaryTextColor, fontSize = 12.sp)
        }
    }
}


// simple line chart for last 14 days
@Composable
private fun LineChart(
    values: List<Float>,
    labels: List<String>,
    colors: AppThemeColors,
    height: androidx.compose.ui.unit.Dp
) {
    val maxVal = max(1f, values.maxOrNull() ?: 1f)
    val minVal = min(0f, values.minOrNull() ?: 0f)
    val range = max(1f, maxVal - minVal)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (values.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val stepX = if (values.size > 1) w / (values.size - 1) else w

        // faint grid for readability
        val gridYCount = 4
        repeat(gridYCount + 1) { i ->
            val y = h * (i / gridYCount.toFloat())
            drawLine(
                color = colors.fieldBorderColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        // connect points
        val path = Path()
        values.forEachIndexed { idx, v ->
            val x = stepX * idx
            val norm = if (range == 0f) 0f else (v - minVal) / range
            val y = h - (norm * h)
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = colors.accentColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // dot markers
        values.forEachIndexed { idx, v ->
            val x = stepX * idx
            val norm = if (range == 0f) 0f else (v - minVal) / range
            val y = h - (norm * h)
            drawCircle(
                color = colors.accentColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}

// 7-day bars
@Composable
private fun BarChart(
    values: List<Float>,
    labels: List<String>,
    colors: AppThemeColors,
    height: androidx.compose.ui.unit.Dp
) {
    val maxVal = max(1f, values.maxOrNull() ?: 1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (values.isEmpty()) return@Canvas

        val barCount = values.size
        val w = size.width
        val h = size.height
        val barW = w / (barCount * 1.6f) // spacing
        val step = w / barCount

        // baseline
        drawLine(
            color = colors.fieldBorderColor,
            start = Offset(0f, h - 2f),
            end = Offset(w, h - 2f),
            strokeWidth = 2f
        )

        values.forEachIndexed { idx, v ->
            val xCenter = step * idx + step / 2f
            val barH = if (maxVal == 0f) 0f else (v / maxVal) * (h * 0.9f)
            drawRoundRect(
                color = colors.accentColor,
                topLeft = Offset(xCenter - barW / 2f, h - barH),
                size = androidx.compose.ui.geometry.Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
        }
    }
}


private val utcFormatter by lazy {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

private fun utcKey(date: Date): String = utcFormatter.format(date)

private fun buildKeys(daysBack: Int): List<String> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val keys = ArrayList<String>(daysBack)
    for (i in (daysBack - 1) downTo 0) {
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, -i)
        keys.add(utcKey(c.time))
    }
    return keys
}

private fun dayOfMonthFromKey(key: String): String {
    // key ends with "-dd"
    val dd = key.substring(key.length - 2)
    return if (dd.startsWith("0")) dd.substring(1) else dd
}
