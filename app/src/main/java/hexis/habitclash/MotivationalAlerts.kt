package hexis.habitclash

import android.content.Context
import android.widget.Toast

object MotivationalAlerts {

    // milestones for encouragement messages
    private val milestones = listOf(1, 3, 5, 7, 10, 14, 21, 30, 50, 100)

    // keeps track of the last shown milestone per habit to avoid duplicates
    private val shownMilestones = mutableMapOf<String, Int>()

    fun checkAndShow(context: Context, habitId: String, currentStreak: Int) {
        // Only trigger if it matches one of the milestones
        val milestoneHit = milestones.firstOrNull { it == currentStreak } ?: return

        val lastShown = shownMilestones[habitId] ?: 0
        if (currentStreak > lastShown) {
            val message = getMessage(currentStreak)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            shownMilestones[habitId] = currentStreak
        }
    }

    private fun getMessage(days: Int): String {
        return when (days) {
            1 -> "Good start! Day 1 complete"
            3 -> "Three days strong! Keep going"
            5 -> "Five days in — building a real habit"
            7 -> "One week of consistency! You’re crushing it"
            10 -> "10 days strong! Stay unstoppable!"
            14 -> "Two weeks down — incredible focus!"
            21 -> "21 days makes a habit — you’ve done it!"
            30 -> "30 days! That’s a full month of discipline"
            50 -> "50-day streak! You're a machine "
            100 -> "100 days of greatness! You’re elite now!"
            else -> "Keep going strong!"
        }
    }
}
