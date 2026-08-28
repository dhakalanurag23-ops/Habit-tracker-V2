package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.model.AlarmImportance
import com.example.model.HabitTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TaskAlarmManager {

    private const val TAG = "TaskAlarmManager"
    const val ACTION_TRIGGER_TASK_ALARM = "com.example.ACTION_TRIGGER_TASK_ALARM"
    const val ACTION_SNOOZE_TASK_ALARM = "com.example.ACTION_SNOOZE_TASK_ALARM"
    const val ACTION_MARK_TASK_DONE = "com.example.ACTION_MARK_TASK_DONE"
    const val ACTION_TRIGGER_ESCALATED_MOTIVATION = "com.example.ACTION_TRIGGER_ESCALATED_MOTIVATION"

    const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
    const val EXTRA_IS_HABIT = "EXTRA_IS_HABIT"
    const val EXTRA_ALARM_IMPORTANCE = "EXTRA_ALARM_IMPORTANCE"

    fun scheduleTaskAlarm(context: Context, task: HabitTask) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val calendar = Calendar.getInstance().apply {
            if (task.scheduledDate.isNotBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val parsed = sdf.parse(task.scheduledDate)
                    if (parsed != null) {
                        time = parsed
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing scheduledDate: ${task.scheduledDate}", e)
                }
            }
            set(Calendar.HOUR_OF_DAY, task.scheduledHour)
            set(Calendar.MINUTE, task.scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If scheduled time has already passed today and no specific future date was given, schedule for tomorrow
            if (task.scheduledDate.isBlank() && timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val triggerTime = calendar.timeInMillis
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_TASK_ALARM
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_IS_HABIT, task.isHabit)
            putExtra(EXTRA_ALARM_IMPORTANCE, task.alarmImportance)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            flags
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for '${task.title}' at ${calendar.time} with importance ${task.alarmImportance}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm for '${task.title}'", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm: ${e.message}", e)
        }
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        taskId: Long,
        taskTitle: String,
        isHabit: Boolean,
        importance: String,
        snoozeMinutes: Int = 10
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_TASK_ALARM
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
            putExtra(EXTRA_IS_HABIT, isHabit)
            putExtra(EXTRA_ALARM_IMPORTANCE, importance)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            flags
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Snoozed alarm for '$taskTitle' by $snoozeMinutes minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule snooze: ${e.message}", e)
        }

        // Also schedule Stage 2 Escalation 15 minutes after snooze
        scheduleEscalatedMotivationAlarm(context, taskId, taskTitle, delayMinutes = snoozeMinutes + 15)
    }

    fun scheduleEscalatedMotivationAlarm(
        context: Context,
        taskId: Long,
        taskTitle: String,
        delayMinutes: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ESCALATED_MOTIVATION
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 100000).toInt(),
            intent,
            flags
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Scheduled Stage 2 Motivational Escalation for '$taskTitle' in $delayMinutes min")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule escalation alarm: ${e.message}", e)
        }
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_TASK_ALARM
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            flags
        )
        alarmManager.cancel(pendingIntent)

        // Also cancel escalation
        cancelEscalatedMotivationAlarm(context, taskId)
        Log.d(TAG, "Cancelled alarm & escalation for task ID: $taskId")
    }

    fun cancelEscalatedMotivationAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_ESCALATED_MOTIVATION
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 100000).toInt(),
            intent,
            flags
        )
        alarmManager.cancel(pendingIntent)
    }
}
