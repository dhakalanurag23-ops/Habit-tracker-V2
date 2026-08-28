package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskCategory(val displayName: String, val iconName: String, val colorHex: Long) {
    WORK("Work & Focus", "work", 0xFF3B82F6),
    HEALTH("Health & Fitness", "fitness", 0xFF10B981),
    MIND("Mindfulness", "self_improvement", 0xFF8B5CF6),
    LEARNING("Learning & Skills", "school", 0xFFF59E0B),
    ROUTINE("Daily Routine", "schedule", 0xFFEC4899)
}

enum class PriorityLevel(val label: String, val colorHex: Long) {
    HIGH("High Priority", 0xFFEF4444),
    MEDIUM("Medium", 0xFFF59E0B),
    LOW("Low", 0xFF10B981)
}

enum class AlarmImportance(val displayName: String, val description: String, val level: Int) {
    UNIMPORTANT("Unimportant", "Silent notification only", 1),
    BASIC("Basic", "Standard chime notification", 2),
    MEDIUM("Medium", "High priority notification with chime & vibration", 3),
    IMPORTANT("Important", "Max volume override, custom alarm audio & intense vibration", 4)
}

@Entity(tableName = "habits_and_tasks")
data class HabitTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = TaskCategory.ROUTINE.name,
    val isHabit: Boolean = true,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val targetDaysPerWeek: Int = 7,
    val scheduledHour: Int = 9,
    val scheduledMinute: Int = 0,
    val durationMinutes: Int = 30,
    val priority: String = PriorityLevel.MEDIUM.name,
    val alarmImportance: String = AlarmImportance.MEDIUM.name,
    val lastCompletedDate: String = "", // Format: "yyyy-MM-dd"
    val completedHistoryJson: String = "[]", // Stores past completion date strings
    val hasAlarm: Boolean = false,
    val scheduledDate: String = "", // Format: "yyyy-MM-dd" (empty = today/recurring)
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getCategoryEnum(): TaskCategory {
        return try {
            TaskCategory.valueOf(category)
        } catch (e: Exception) {
            TaskCategory.ROUTINE
        }
    }

    fun getPriorityEnum(): PriorityLevel {
        return try {
            PriorityLevel.valueOf(priority)
        } catch (e: Exception) {
            PriorityLevel.MEDIUM
        }
    }

    fun getAlarmImportanceEnum(): AlarmImportance {
        return try {
            AlarmImportance.valueOf(alarmImportance)
        } catch (e: Exception) {
            AlarmImportance.MEDIUM
        }
    }

    fun getFormattedTime(): String {
        val amPm = if (scheduledHour < 12) "AM" else "PM"
        val hour12 = if (scheduledHour % 12 == 0) 12 else scheduledHour % 12
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, scheduledMinute, amPm)
    }

    fun isCompletedOnDate(dateStr: String): Boolean {
        return completedHistoryJson.contains(dateStr)
    }

    companion object {
        fun getCurrentDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
