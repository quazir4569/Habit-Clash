package hexis.habitclash

data class Habit(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val frequency: String = "Daily",
    val goalCount: Int = 1,

    val reminderTimes: List<String> = emptyList(),

    val reminderTime: String? = null,

    val completionDates: List<String> = listOf(), // For streak calculation
    val currentStreak: Int = 0,                   // For current streak value
    val longestStreak: Int = 0,                   // For best streak value
    val totalCompletions: Int = 0,                // Number of times habit completed
    val isCompletedToday: Boolean = false,        // Today's completion status
    val lastCompleted: Long = 0L                  // Timestamp of last completion
)
