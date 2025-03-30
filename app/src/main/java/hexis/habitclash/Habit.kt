package hexis.habitclash

/**
 * Stores habit information.
 * Used for saving and loading habits from Firebase.
 */
data class Habit(
    val title: String = "",
    val time: String = "",
    val completed: Boolean = false
)