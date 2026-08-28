package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import com.example.model.HabitTask
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 5. Google Ecosystem Integration
 *
 * - Google Calendar Sync: Add tasks/habits as scheduled events using Android's CalendarContract API
 * - Timetable Engine: Generate structured time-blocked schedules formatted for Google Sheets export
 */
object GoogleEcosystemIntegration {

    private const val TAG = "GoogleEcosystem"

    /**
     * Add a HabitTask as an event to Google Calendar via Android's CalendarContract Intent
     */
    fun syncTaskToGoogleCalendar(context: Context, task: HabitTask) {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, task.scheduledHour)
                set(Calendar.MINUTE, task.scheduledMinute)
                set(Calendar.SECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = startTime + (task.durationMinutes * 60 * 1000L)

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.TITLE, task.title)
                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "${task.description}\nCategory: ${task.getCategoryEnum().displayName}\nPriority: ${task.getPriorityEnum().label}\nManaged by HabitPulse"
                )
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Daily Habit Routine")
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                if (task.isHabit) {
                    // Repeat daily for habits
                    putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY;INTERVAL=1")
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Calendar intent: ${e.message}", e)
            Toast.makeText(context, "Could not open Google Calendar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generate a Google Sheets-compatible CSV timetable schedule
     */
    fun generateGoogleSheetsTimetableCsv(tasks: List<HabitTask>): String {
        val stringBuilder = StringBuilder()
        // CSV Header for Google Sheets
        stringBuilder.append("Start Time,End Time,Duration (Min),Task / Habit Title,Category,Type,Priority,Daily Streak,Status\n")

        val sortedTasks = tasks.sortedWith(compareBy({ it.scheduledHour }, { it.scheduledMinute }))

        for (task in sortedTasks) {
            val startFormatted = task.getFormattedTime()
            val endCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, task.scheduledHour)
                set(Calendar.MINUTE, task.scheduledMinute)
                add(Calendar.MINUTE, task.durationMinutes)
            }
            val endAmPm = if (endCalendar.get(Calendar.HOUR_OF_DAY) < 12) "AM" else "PM"
            val endHour12 = if (endCalendar.get(Calendar.HOUR_OF_DAY) % 12 == 0) 12 else endCalendar.get(Calendar.HOUR_OF_DAY) % 12
            val endFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", endHour12, endCalendar.get(Calendar.MINUTE), endAmPm)

            val safeTitle = "\"${task.title.replace("\"", "\"\"")}\""
            val category = "\"${task.getCategoryEnum().displayName}\""
            val type = if (task.isHabit) "Daily Habit" else "One-off Task"
            val priority = task.getPriorityEnum().label
            val streak = "${task.streakCount} days"
            val status = if (task.isCompletedToday) "Completed" else "Pending"

            stringBuilder.append("$startFormatted,$endFormatted,${task.durationMinutes},$safeTitle,$category,$type,$priority,$streak,$status\n")
        }

        return stringBuilder.toString()
    }

    /**
     * Export Timetable directly to Google Sheets / Share Sheet via Intent
     */
    fun exportTimetableToGoogleSheets(context: Context, tasks: List<HabitTask>) {
        try {
            val csvContent = generateGoogleSheetsTimetableCsv(tasks)
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val fileName = "HabitPulse_Timetable_$dateStr.csv"

            val cacheFile = File(context.cacheDir, fileName)
            FileWriter(cacheFile).use { it.write(csvContent) }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "HabitPulse Daily Timetable Schedule - Google Sheets Export")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is your structured time-blocked schedule formatted for Google Sheets import.\n\nSummary:\n" +
                            "Total Blocks: ${tasks.size}\n" +
                            "Generated: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}"
                )
                // Attach the plain text CSV as EXTRA_TEXT or content uri
                putExtra("android.intent.extra.STREAM_DATA", csvContent)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(Intent.createChooser(intent, "Open with Google Sheets or Share Timetable").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting timetable to Google Sheets: ${e.message}", e)
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
