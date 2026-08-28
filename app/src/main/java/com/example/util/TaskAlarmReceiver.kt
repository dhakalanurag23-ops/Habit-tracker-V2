package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.model.AlarmImportance
import com.example.model.HabitTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: TaskAlarmManager.ACTION_TRIGGER_TASK_ALARM
        val taskId = intent.getLongExtra(TaskAlarmManager.EXTRA_TASK_ID, 0L)
        val taskTitle = intent.getStringExtra(TaskAlarmManager.EXTRA_TASK_TITLE) ?: "Task Reminder"
        val isHabit = intent.getBooleanExtra(TaskAlarmManager.EXTRA_IS_HABIT, false)
        val importanceStr = intent.getStringExtra(TaskAlarmManager.EXTRA_ALARM_IMPORTANCE) ?: AlarmImportance.MEDIUM.name
        val importance = try {
            AlarmImportance.valueOf(importanceStr)
        } catch (e: Exception) {
            AlarmImportance.MEDIUM
        }

        Log.d(TAG, "onReceive action=$action for task $taskId: '$taskTitle'")

        when (action) {
            TaskAlarmManager.ACTION_SNOOZE_TASK_ALARM -> {
                // Dismiss existing notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(taskId.toInt())

                // Reschedule for 10 minutes later + schedule 15m escalation
                TaskAlarmManager.scheduleSnoozeAlarm(context, taskId, taskTitle, isHabit, importance.name, 10)
            }

            TaskAlarmManager.ACTION_MARK_TASK_DONE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(taskId.toInt())
                notificationManager?.cancel((taskId + 100000).toInt())

                // Cancel pending escalation
                TaskAlarmManager.cancelEscalatedMotivationAlarm(context, taskId)

                // Update database
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val task = db.habitDao().getById(taskId)
                        if (task != null && !task.isCompletedToday) {
                            val today = HabitTask.getCurrentDateString()
                            val updated = task.copy(
                                isCompletedToday = true,
                                streakCount = if (task.isHabit) task.streakCount + 1 else task.streakCount,
                                bestStreak = if (task.isHabit) maxOf(task.bestStreak, task.streakCount + 1) else task.bestStreak,
                                lastCompletedDate = today
                            )
                            db.habitDao().update(updated)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error marking task done from notification: ${e.message}", e)
                    }
                }
            }

            TaskAlarmManager.ACTION_TRIGGER_ESCALATED_MOTIVATION -> {
                // Check if task was completed in the meantime
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val task = db.habitDao().getById(taskId)
                        if (task != null && !task.isCompletedToday) {
                            showEscalatedMotivationNotification(context, taskId, taskTitle)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking task for motivation: ${e.message}", e)
                    }
                }
            }

            else -> {
                // ACTION_TRIGGER_TASK_ALARM (Stage 1)
                handleAlarmTrigger(context, taskId, taskTitle, isHabit, importance)
            }
        }
    }

    private fun handleAlarmTrigger(
        context: Context,
        taskId: Long,
        taskTitle: String,
        isHabit: Boolean,
        importance: AlarmImportance
    ) {
        // If Unimportant, don't play sound or heavy vibration
        if (importance == AlarmImportance.UNIMPORTANT) {
            showAlarmNotification(context, taskId, taskTitle, isHabit, importance)
            // Schedule Stage 2 escalation in 15 minutes
            TaskAlarmManager.scheduleEscalatedMotivationAlarm(context, taskId, taskTitle, 15)
            return
        }

        // For IMPORTANT, force volume up
        if (importance == AlarmImportance.IMPORTANT) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.let { am ->
                    val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    am.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not set max volume: ${e.message}")
            }
        }

        // Play custom bundled alarm sound for Basic, Medium, Important
        try {
            val mediaPlayer: MediaPlayer? = MediaPlayer.create(context, R.raw.alarm_sound)
            if (mediaPlayer != null) {
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(if (importance == AlarmImportance.IMPORTANT) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm audio: ${e.message}", e)
        }

        // Haptics/Vibration
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = when (importance) {
                    AlarmImportance.IMPORTANT -> longArrayOf(0, 500, 200, 500, 200, 800)
                    AlarmImportance.MEDIUM -> longArrayOf(0, 300, 200, 300)
                    else -> longArrayOf(0, 150)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed: ${e.message}")
        }

        // Post high-priority system notification with Snooze and Done actions
        showAlarmNotification(context, taskId, taskTitle, isHabit, importance)

        // Schedule Stage 2 Escalation in 15 minutes (if not checked off)
        TaskAlarmManager.scheduleEscalatedMotivationAlarm(context, taskId, taskTitle, 15)
    }

    private fun showAlarmNotification(
        context: Context,
        taskId: Long,
        taskTitle: String,
        isHabit: Boolean,
        importance: AlarmImportance
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = when (importance) {
            AlarmImportance.IMPORTANT -> "habit_task_alarms_important"
            AlarmImportance.UNIMPORTANT -> "habit_task_alarms_silent"
            else -> "habit_task_alarms_default"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                when (importance) {
                    AlarmImportance.IMPORTANT -> "🚨 Urgent Habit & Task Alarms"
                    AlarmImportance.UNIMPORTANT -> "Quiet Reminders"
                    else -> "Standard Habit & Task Reminders"
                },
                when (importance) {
                    AlarmImportance.IMPORTANT -> NotificationManager.IMPORTANCE_HIGH
                    AlarmImportance.UNIMPORTANT -> NotificationManager.IMPORTANCE_LOW
                    else -> NotificationManager.IMPORTANCE_DEFAULT
                }
            ).apply {
                description = "Notifications for scheduled habits and tasks"
                enableVibration(importance != AlarmImportance.UNIMPORTANT)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_OPENED_FROM_ALARM", true)
            putExtra("EXTRA_TASK_ID", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmManager.ACTION_SNOOZE_TASK_ALARM
            putExtra(TaskAlarmManager.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmManager.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskAlarmManager.EXTRA_IS_HABIT, isHabit)
            putExtra(TaskAlarmManager.EXTRA_ALARM_IMPORTANCE, importance.name)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 200000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done Action
        val doneIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmManager.ACTION_MARK_TASK_DONE
            putExtra(TaskAlarmManager.EXTRA_TASK_ID, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 300000).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm_sound}")

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (isHabit) "⏰ Daily Habit Reminder!" else "⏰ Action Task Due!")
            .setContentText(taskTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time to accomplish: $taskTitle. Keep up your daily momentum!"))
            .setPriority(
                when (importance) {
                    AlarmImportance.IMPORTANT -> NotificationCompat.PRIORITY_MAX
                    AlarmImportance.UNIMPORTANT -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 10m", snoozePendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Done ✔️", donePendingIntent)

        if (importance == AlarmImportance.IMPORTANT) {
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
            notificationBuilder.setSound(soundUri)
            notificationBuilder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))
        }

        notificationManager.notify(taskId.toInt(), notificationBuilder.build())
    }

    private fun showEscalatedMotivationNotification(
        context: Context,
        taskId: Long,
        taskTitle: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "habit_motivation_escalation"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "🌱 Motivational Follow-ups",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Guilt-free motivational reminders to protect your streak"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_OPENED_FROM_ALARM", true)
            putExtra("EXTRA_TASK_ID", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (taskId + 100000).toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done Action
        val doneIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmManager.ACTION_MARK_TASK_DONE
            putExtra(TaskAlarmManager.EXTRA_TASK_ID, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 400000).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val motivationalQuotes = listOf(
            "🌱 You've got this! Still time to knock out '$taskTitle'. Every small step counts!",
            "🔥 Protect your daily streak! Take 2 quick minutes to complete '$taskTitle'.",
            "✨ You are closer to your goals than you think. Finish '$taskTitle' and feel accomplished today!"
        )
        val quote = motivationalQuotes.random()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("🌱 Gentle Motivation: '$taskTitle'")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Mark Done ✔️", donePendingIntent)
            .build()

        notificationManager.notify((taskId + 100000).toInt(), notification)
        Log.d(TAG, "Posted Stage 2 Escalated Motivation notification for task $taskId")
    }

    companion object {
        private const val TAG = "TaskAlarmReceiver"
    }
}
