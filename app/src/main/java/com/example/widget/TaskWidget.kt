package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.model.HabitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 3. Resizable & Interactive Home Screen Widgets (Jetpack Glance)
 *
 * Supports Samsung One UI and Android 12+ dynamic widget sizing:
 * - Small / Compact (2x1, 2x2): Progress indicator + top priority task
 * - Expanded (4x2, 4x3, Samsung One UI full width): Complete scrollable task list with 1-tap check-off
 */
class TaskWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(140.dp, 100.dp), // Compact
            DpSize(260.dp, 180.dp), // Medium
            DpSize(360.dp, 260.dp)  // Expanded / Samsung One UI full width
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Query database snapshot for immediate display
        val database = AppDatabase.getDatabase(context)
        val habitDao = database.habitDao()
        val items = withContext(Dispatchers.IO) {
            habitDao.getAllSnapshot()
        }

        val completedCount = items.count { it.isCompletedToday }
        val totalCount = items.size
        val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        provideContent {
            GlanceTheme {
                WidgetContent(
                    items = items,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    progressPercent = progressPercent
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        items: List<HabitTask>,
        completedCount: Int,
        totalCount: Int,
        progressPercent: Int
    ) {
        val darkSurface = Color(0xFF0F172A)
        val cardSurface = Color(0xFF1E293B)
        val textPrimary = Color(0xFFF8FAFC)
        val textSecondary = Color(0xFF94A3B8)
        val accentCyan = Color(0xFF06B6D4)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(ColorProvider(darkSurface))
                .cornerRadius(24.dp)
                .padding(14.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Widget Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "HabitPulse",
                            style = TextStyle(
                                color = ColorProvider(textPrimary),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "$completedCount/$totalCount Done ($progressPercent%)",
                            style = TextStyle(
                                color = ColorProvider(accentCyan),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Open App Button
                    Button(
                        text = "Open",
                        onClick = actionStartActivity<MainActivity>(),
                        modifier = GlanceModifier.cornerRadius(12.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Scrollable Task / Habit List
                if (items.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All tasks completed! Tap Open to add more.",
                            style = TextStyle(
                                color = ColorProvider(textSecondary),
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        items(items) { habitTask ->
                            TaskWidgetItem(
                                item = habitTask,
                                cardSurface = cardSurface,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                accentCyan = accentCyan
                            )
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TaskWidgetItem(
        item: HabitTask,
        cardSurface: Color,
        textPrimary: Color,
        textSecondary: Color,
        accentCyan: Color
    ) {
        val itemKey = ActionParameters.Key<Long>("task_id")
        val toggleAction = actionRunCallback<ToggleTaskActionCallback>(
            actionParametersOf(itemKey to item.id)
        )

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(cardSurface))
                .cornerRadius(12.dp)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive CheckBox Action
            CheckBox(
                checked = item.isCompletedToday,
                onCheckedChange = toggleAction,
                modifier = GlanceModifier.size(24.dp)
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        color = ColorProvider(if (item.isCompletedToday) textSecondary else textPrimary),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Row {
                    if (item.isHabit) {
                        Text(
                            text = "🔥 ${item.streakCount}d streak • ",
                            style = TextStyle(
                                color = ColorProvider(accentCyan),
                                fontSize = 10.sp
                            )
                        )
                    }
                    Text(
                        text = item.getFormattedTime(),
                        style = TextStyle(
                            color = ColorProvider(textSecondary),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Interactive ActionCallback that updates Room Database on widget click and refreshes Glance widget
 */
class ToggleTaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val itemKey = ActionParameters.Key<Long>("task_id")
        val taskId = parameters[itemKey] ?: return

        val database = AppDatabase.getDatabase(context)
        val habitDao = database.habitDao()

        withContext(Dispatchers.IO) {
            val item = habitDao.getById(taskId)
            if (item != null) {
                val today = HabitTask.getCurrentDateString()
                val isNowCompleted = !item.isCompletedToday
                val newStreak = if (isNowCompleted) item.streakCount + 1 else maxOf(0, item.streakCount - 1)
                val bestStreak = maxOf(item.bestStreak, newStreak)

                val updated = item.copy(
                    isCompletedToday = isNowCompleted,
                    streakCount = if (item.isHabit) newStreak else item.streakCount,
                    bestStreak = if (item.isHabit) bestStreak else item.bestStreak,
                    lastCompletedDate = if (isNowCompleted) today else item.lastCompletedDate
                )
                habitDao.update(updated)
            }
        }

        // Update all widget instances
        TaskWidget().updateAll(context)
    }
}

/**
 * AppWidgetReceiver for Glance TaskWidget
 */
class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskWidget()
}
