package hexis.habitclash

data class Habit(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val frequency: String = "daily",
    val goalCount: Int = 1,

    val isCompletedToday: Boolean = false,
    val startDate: Long = System.currentTimeMillis(),
    val lastCompleted: Long = 0,
    val reminderTime: String? = null,
    val isArchived: Boolean = false
)