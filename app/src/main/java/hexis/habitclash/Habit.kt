package hexis.habitclash

/**
 * Stores habit information.
 * Used for saving and loading habits from Firebase.
 */

data class Habit(
    val id: String = "",                      // Unique identifier (Firebase ID)
    val userId: String = "",                  // ID of the user who created the habit
    val title: String = "",                   // Name of the habit
    val description: String = "",             // Details about the habit
    val category: String = "",                // E.g., Health, Study, etc.
    val frequency: String = "daily",          // Daily, weekly, etc.
    val goalCount: Int = 1,                   // Target number per frequency

    val isCompletedToday: Boolean = false,    // Whether completed today
    val startDate: Long = System.currentTimeMillis(),
    val lastCompleted: Long = 0,              // Timestamp of last completion
    val reminderTime: String? = null,         // E.g., "8:00 AM"
    val isArchived: Boolean = false           // If habit is paused
)
