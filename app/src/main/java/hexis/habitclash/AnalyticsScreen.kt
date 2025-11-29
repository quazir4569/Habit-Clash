package hexis.habitclash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hexis.habitclash.ui.theme.AppThemeColors
import hexis.habitclash.ui.theme.getAppThemeColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val isDark = themeViewModel.isDarkMode
    val colors = getAppThemeColors(isDark)

    // basic numbers for summary
    var habitsCount by remember { mutableStateOf(0) }
    var dailyCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // recent completions list (state list so LazyColumn reacts)
    val recent = remember { mutableStateListOf<CompletionRow>() }

    val dodgerBlue = Color(0xFF1E90FF)

    // pull logs for the last 14 days
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()

        // count how many habits exist (for today's %)
        db.collection("users").document(uid)
            .collection("habits")
            .get()
            .addOnSuccessListener { snap ->
                habitsCount = snap.size()
            }

        // preset 14 days so the chart never looks empty
        val keys = lastNDatesKeys(14)
        val counts = keys.associateWith { 0 }.toMutableMap()
        val recentsTmp = mutableListOf<CompletionRow>()

        db.collection("users").document(uid)
            .collection("completion_logs")
            .get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { d ->
                    val dateKey = d.getString("dateKey") ?: return@forEach
                    val completed = d.getBoolean("completed") == true
                    val habitId = d.getString("habitId") ?: "unknown"
                    val updatedAt = (d.getTimestamp("updatedAt")?.toDate()) ?: Date()

                    if (completed && counts.containsKey(dateKey)) {
                        counts[dateKey] = (counts[dateKey] ?: 0) + 1
                    }
                    if (completed) {
                        recentsTmp += CompletionRow(
                            dateKey = dateKey,
                            habitId = habitId,
                            whenText = prettyWhen(updatedAt)
                        )
                    }
                }

                // keep days in order
                dailyCounts = counts.toList().sortedBy { it.first }.toMap()

                // newest first for the feed
                recentsTmp.sortByDescending { it.dateKey }
                recent.clear()
                recent.addAll(recentsTmp.take(25))
            }
    }

    val todayKey = utcDayKey()
    val todayDone = dailyCounts[todayKey] ?: 0
    val todayPct = if (habitsCount > 0) (todayDone.toFloat() / habitsCount).coerceIn(0f, 1f) else 0f

    // quick streak: count consecutive days from today with > 0 completions
    val streak = remember(dailyCounts) { calcStreak(dailyCounts) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dodgerBlue)
    ) {
        // simple top bar so users can go back
        TopAppBar(
            title = { Text("Analytics", color = colors.textColor, fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.cardColor)
        )

        Spacer(Modifier.height(8.dp))

        // today's summary
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardColor)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Today", color = colors.secondaryTextColor)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { todayPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${(todayPct * 100).toInt()}% • $todayDone / $habitsCount completed",
                    color = colors.textColor
                )
                Spacer(Modifier.height(6.dp))
                Text("Current streak: $streak day(s)", color = colors.textColor)
            }
        }

        Spacer(Modifier.height(16.dp))

        // charts + recent list
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            ChartCard(title = "Last 14 Days (Completed per day)", colors = colors) {
                BarChart(
                    labels = dailyCounts.keys.toList().map { it.takeLast(5) },
                    values = dailyCounts.values.toList(),
                    barColor = colors.accentColor,
                    axisColor = if (isDark) Color(0x55FFFFFF) else Color(0x33000000)
                )
            }

            Spacer(Modifier.height(16.dp))

            ChartCard(title = "Trend (Last 14 Days)", colors = colors) {
                LineChart(
                    values = dailyCounts.values.toList(),
                    lineColor = colors.accentColor,
                    axisColor = if (isDark) Color(0x55FFFFFF) else Color(0x33000000)
                )
            }

            Spacer(Modifier.height(16.dp))

            ChartCard(title = "Recent Activity", colors = colors) {
                if (recent.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent completions yet.", color = colors.secondaryTextColor)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recent) { r ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    r.dateKey.takeLast(5),
                                    color = colors.secondaryTextColor,
                                    modifier = Modifier.width(56.dp)
                                )
                                Text(
                                    "Completed • ${r.habitId}",
                                    color = colors.textColor,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(r.whenText, color = colors.secondaryTextColor)
                            }
                        }
                    }
                }
            }
        }

        // bottom nav to leave the page easily
        BottomNavigationBar(navController, isDark)
    }
}

/* simple list item model for the recent feed */
data class CompletionRow(
    val dateKey: String,
    val habitId: String,
    val whenText: String
)

/* small titled container used by charts + list */
@Composable
private fun ChartCard(
    title: String,
    colors: AppThemeColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = colors.textColor, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/* compact bar chart using Canvas (keeps dependencies light) */
@Composable
private fun BarChart(
    labels: List<String>,
    values: List<Int>,
    barColor: Color,
    axisColor: Color,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
) {
    val maxVal = max(values.maxOrNull() ?: 0, 1)
    val barCount = values.size.coerceAtLeast(1)

    Column(modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // axes
            drawLine(axisColor, Offset(0f, h - 1f), Offset(w, h - 1f), strokeWidth = 2f)
            drawLine(axisColor, Offset(1f, 0f), Offset(1f, h), strokeWidth = 2f)

            val gap = w * 0.02f
            val barW = (w - gap * (barCount + 1)) / barCount

            values.forEachIndexed { i, v ->
                val x = gap + i * (barW + gap)
                val frac = v.toFloat() / maxVal
                val barH = (h - 8f) * frac
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, h - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        // three tick labels to avoid crowding
        if (labels.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.firstOrNull()?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                labels.getOrNull(labels.lastIndex / 2)?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                labels.lastOrNull()?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

/* tiny line chart for a quick trend view */
@Composable
private fun LineChart(
    values: List<Int>,
    lineColor: Color,
    axisColor: Color,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
) {
    val data = if (values.isEmpty()) listOf(0) else values
    val maxVal = max(data.maxOrNull() ?: 0, 1)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // axes
        drawLine(axisColor, Offset(0f, h - 1f), Offset(w, h - 1f), strokeWidth = 2f)
        drawLine(axisColor, Offset(1f, 0f), Offset(1f, h), strokeWidth = 2f)

        val stepX = if (data.size > 1) (w / (data.size - 1)) else 0f
        val pts = data.mapIndexed { i, v ->
            val frac = v.toFloat() / maxVal
            Offset(i * stepX, h - (h - 8f) * frac)
        }

        if (pts.size >= 2) {
            val path = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        } else {
            drawCircle(color = lineColor, radius = 6f, center = pts.first())
        }
    }
}

/* count consecutive days with > 0 completions, starting today */
private fun calcStreak(daily: Map<String, Int>): Int {
    if (daily.isEmpty()) return 0
    val ordered = daily.toList().sortedBy { it.first } // oldest -> newest
    val today = utcDayKey()
    val idx = ordered.indexOfFirst { it.first == today }
    if (idx == -1) return 0

    var streak = 0
    for (i in idx downTo 0) {
        val c = ordered[i].second
        if (c > 0) streak++ else break
    }
    return streak
}

/* build last N days (UTC) like 2025-09-22 */
private fun lastNDatesKeys(n: Int): List<String> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val out = ArrayList<String>(n)
    repeat(n) {
        out += utcDayKey(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return out.reversed()
}

/* stable day key in UTC so queries line up */
private fun utcDayKey(date: Date = Date()): String {
    val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(date)
}

/* quick “time ago” text for the recent list */
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
