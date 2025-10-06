package hexis.habitclash

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.filter
import kotlin.collections.firstOrNull
import kotlin.collections.plus
import kotlin.collections.reversed
import kotlin.collections.sorted
import kotlin.collections.toSet
import kotlin.ranges.until

/**
 * Calculates streaks for habits based on completion dates.
 * Works with Firestore data structure.
 */
object StreakCalculator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Get today's date in standard format
     */
    fun getTodayDate(): String {
        return dateFormat.format(Date())
    }

    /**
     * Calculate current streak from completion dates
     */
    fun calculateCurrentStreak(completionDates: List<String>): Int {
        if (completionDates.isEmpty()) return 0

        // Sort dates in descending order (newest first)
        val sortedDates = completionDates.sorted().reversed()

        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)

        // Check if completed today or yesterday (grace period)
        val mostRecent = sortedDates.firstOrNull() ?: return 0

        // If not completed today, check yesterday
        if (mostRecent != today) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = dateFormat.format(calendar.time)

            if (mostRecent != yesterday) {
                return 0 // Streak broken
            }
            // Start counting from yesterday
        }

        // Count consecutive days backwards
        var streak = 0
        val completionSet = completionDates.toSet()
        var currentDate = mostRecent

        while (completionSet.contains(currentDate)) {
            streak++
            val date = dateFormat.parse(currentDate)
            val cal = Calendar.getInstance()
            cal.time = date!!
            cal.add(Calendar.DAY_OF_YEAR, -1)
            currentDate = dateFormat.format(cal.time)
        }

        return streak
    }

    /**
     * Calculate longest streak ever
     */
    fun calculateLongestStreak(completionDates: List<String>): Int {
        if (completionDates.isEmpty()) return 0

        val sorted = completionDates.sorted()
        if (sorted.size == 1) return 1

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sorted.size) {
            val prevDate = dateFormat.parse(sorted[i - 1])
            val currDate = dateFormat.parse(sorted[i])

            if (prevDate != null && currDate != null) {
                val daysDiff = TimeUnit.MILLISECONDS.toDays(
                    currDate.time - prevDate.time
                ).toInt()

                if (daysDiff == 1) {
                    currentStreak++
                    maxStreak = kotlin.comparisons.maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
            }
        }

        return maxStreak
    }

    /**
     * Check if habit was completed today
     */
    fun isCompletedToday(completionDates: List<String>): Boolean {
        return completionDates.contains(getTodayDate())
    }

    /**
     * Add today's completion
     */
    fun addTodayCompletion(completionDates: List<String>): List<String> {
        val today = getTodayDate()
        return if (!completionDates.contains(today)) {
            completionDates + today
        } else {
            completionDates
        }
    }

    /**
     * Remove today's completion
     */
    fun removeTodayCompletion(completionDates: List<String>): List<String> {
        val today = getTodayDate()
        return completionDates.filter { it != today }
    }

    /**
     * Get streak message based on current streak
     */
    fun getStreakMessage(currentStreak: Int, longestStreak: Int): String {
        return when {
            currentStreak == 0 -> "Start your streak today! 🎯"
            currentStreak == longestStreak && currentStreak >= 7 -> "🔥 Personal best! $currentStreak days!"
            currentStreak >= 100 -> "💯 Incredible! $currentStreak-day streak!"
            currentStreak >= 30 -> "🏆 Amazing! $currentStreak-day streak!"
            currentStreak >= 14 -> "⭐ Two weeks! $currentStreak days!"
            currentStreak >= 7 -> "🌟 One week! $currentStreak days!"
            currentStreak >= 3 -> "💪 $currentStreak-day streak!"
            else -> "🎯 $currentStreak day!"
        }
    }

    /**
     * Calculate 7-day completion rate
     */
    fun calculate7DayRate(completionDates: List<String>): Float {
        val calendar = Calendar.getInstance()
        val completionSet = completionDates.toSet()
        var completed = 0

        for (i in 0 until 7) {
            val date = dateFormat.format(calendar.time)
            if (completionSet.contains(date)) completed++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return (completed.toFloat() / 7f) * 100f
    }

    /**
     * Calculate 30-day completion rate
     */
    fun calculate30DayRate(completionDates: List<String>): Float {
        val calendar = Calendar.getInstance()
        val completionSet = completionDates.toSet()
        var completed = 0

        for (i in 0 until 30) {
            val date = dateFormat.format(calendar.time)
            if (completionSet.contains(date)) completed++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return (completed.toFloat() / 30f) * 100f
    }
}
