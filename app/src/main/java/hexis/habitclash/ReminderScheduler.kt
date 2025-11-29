package hexis.habitclash

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

object ReminderScheduler {

    const val CHANNEL_ID = "habit_reminder_channel"

    private fun ensureNotificationChannel(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName("Habit reminders")
            .setDescription("Notifications for your scheduled Habit Clash reminders")
            .build()

        manager.createNotificationChannel(channel)
    }

    // Map Morning / Afternoon / Evening into actual clock times
    private fun bucketToTime(bucket: String?): Pair<Int, Int> {
        return when (bucket) {
            "Morning" -> 8 to 0       // 8:00AM
            "Afternoon" -> 13 to 0    // 1:00PM
            "Evening" -> 20 to 0      // 8:00PM
            else -> 9 to 0            // default 09:00
        }
    }

    fun scheduleRemindersForHabit(
        context: Context,
        userId: String,
        habitId: String,
        habitTitle: String,
        reminderTime: String?,
        frequency: String,
        goalCount: Int
    ) {
        if (reminderTime == null) return

        ensureNotificationChannel(context)

        val (hour, minute) = bucketToTime(reminderTime)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Stable unique id per habit + bucket
        val notificationId = (habitId + reminderTime).hashCode()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habitId", habitId)
            putExtra("habitTitle", habitTitle)
            putExtra("timeLabel", reminderTime)
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // if time already passed today, start tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val interval = when (frequency) {
            "Weekly" -> AlarmManager.INTERVAL_DAY * 7
            "Monthly" -> AlarmManager.INTERVAL_DAY * 30
            else -> AlarmManager.INTERVAL_DAY
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            interval,
            pendingIntent
        )
    }

    fun cancelReminderForHabit(
        context: Context,
        habitId: String,
        reminderTime: String?
    ) {
        if (reminderTime == null) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationId = (habitId + reminderTime).hashCode()

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    // Used by BootReceiver to reschedule everything after reboot
    fun rescheduleAllForCurrentUser(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val habit = doc.toObject(Habit::class.java) ?: return@forEach
                    val reminderTime = habit.reminderTime
                    if (reminderTime != null) {
                        scheduleRemindersForHabit(
                            context = context,
                            userId = userId,
                            habitId = doc.id,
                            habitTitle = habit.title,
                            reminderTime = reminderTime,
                            frequency = habit.frequency,
                            goalCount = habit.goalCount
                        )
                    }
                }
            }
    }
}
