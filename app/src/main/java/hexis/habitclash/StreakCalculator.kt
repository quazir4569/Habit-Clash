package hexis.habitclash

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object StreakCalculator {

    /** Tier definitions based on streak milestones. */
    enum class Tier(val label: String) {
        BRONZE("Bronze"),
        SILVER("Silver"),
        GOLD("Gold"),
        NONE("No Tier")
    }

    /** Get the user tier based on current streak value. */
    fun getTierForStreak(streak: Int): Tier {
        return when {
            streak >= 20 -> Tier.GOLD
            streak >= 10 -> Tier.SILVER
            streak >= 5 -> Tier.BRONZE
            else -> Tier.NONE
        }
    }

    /** Stable UTC key like 2025-10-06 (used for today and comparisons). */
    fun getTodayKey(date: Date = Date()): String {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        f.timeZone = TimeZone.getTimeZone("UTC")
        return f.format(date)
    }

    /** Add today’s key if it isn’t already in the list. */
    fun addTodayCompletion(existing: List<String>, todayKey: String = getTodayKey()): List<String> {
        if (existing.any { it == todayKey }) return existing
        return existing + todayKey
    }

    /** Remove today’s key if present. */
    fun removeTodayCompletion(existing: List<String>, todayKey: String = getTodayKey()): List<String> {
        return existing.filterNot { it == todayKey }
    }

    /** Count consecutive days ending at todayKey. */
    fun calculateCurrentStreak(dates: List<String>, todayKey: String = getTodayKey()): Int {
        if (dates.isEmpty()) return 0
        val set = dates.toHashSet()

        val tz = TimeZone.getTimeZone("UTC")
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }
        val cal = Calendar.getInstance(tz)

        // start at todayKey (not system today, so tests are deterministic)
        cal.time = f.parse(todayKey)!!

        var streak = 0
        while (true) {
            val key = f.format(cal.time)
            if (set.contains(key)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        return streak
    }

    /** Find the longest sequence of consecutive day keys in the list. */
    fun calculateLongestStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0
        val tz = TimeZone.getTimeZone("UTC")
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }

        val sorted = dates.sorted()
        var maxStreak = 0
        var current = 0

        var prevDate: Date? = null
        val cal = Calendar.getInstance(tz)

        for (key in sorted) {
            val d = f.parse(key)!!
            if (prevDate == null) {
                current = 1
            } else {
                cal.time = prevDate!!
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val expectedNextKey = f.format(cal.time)
                current = if (expectedNextKey == key) current + 1 else 1
            }
            if (current > maxStreak) maxStreak = current
            prevDate = d
        }
        return maxStreak
    }

    // small friendly toast message for wins.
    /*fun getstreakmessage(current: int, longest: int): string = when {
        current == 1 -> "day 1 — nice start! 🌱"
        current > longest -> "new best: $current days! 🏆"
        current % 30 == 0 -> "🔥 $current-day streak!"
        else -> "$current days strong! 💪"
    }*/
}
