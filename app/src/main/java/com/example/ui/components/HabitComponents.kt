package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HabitTask
import com.example.model.PriorityLevel
import com.example.model.TaskCategory
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.LayoutDensity
import com.example.ui.theme.LocalThemeConfig
import com.example.ui.theme.aestheticContainer
import com.example.ui.theme.googleAiGlow
import com.example.util.LocalAppStrings
import com.example.viewmodel.FilterTab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Top Hero Stats Card with Animated Completion Ring & Points / Streak Momentum
 */
@Composable
fun HeroProgressCard(
    totalCount: Int,
    completedCount: Int,
    completionPercentage: Int,
    highestStreak: Int,
    userPoints: Int = 120,
    onOpenGamification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent
    val density = themeConfig.density

    val animatedProgress by animateFloatAsState(
        targetValue = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
        animationSpec = tween(800),
        label = "progress_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aestheticContainer(
                aesthetic = themeConfig.aesthetic,
                glowAccent = glowAccent,
                hasGlow = themeConfig.isGlowEnabled,
                cornerRadius = density.cornerRadius
            )
            .padding(density.cardPadding)
            .testTag("hero_progress_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.dailyMomentum,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$completedCount",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " / $totalCount ${strings.completedSummary}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Streak & Points Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when (themeConfig.aesthetic) {
                            DesignAesthetic.NOTHING_UI -> Color(0xFF1F1F1F)
                            else -> glowAccent.primary.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(1.dp, glowAccent.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Streak Flame",
                                tint = if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color(0xFFE11D48) else glowAccent.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${strings.peakStreak}: $highestStreak ${strings.streakPill}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Clickable XP Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, glowAccent.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onOpenGamification() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$userPoints XP",
                                style = MaterialTheme.typography.labelLarge,
                                color = glowAccent.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Circular Progress Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(76.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(76.dp),
                    color = if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color(0xFFE11D48) else glowAccent.primary,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completionPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Filter Tabs (All / Habits / Tasks)
 */
@Composable
fun FilterTabRow(
    selectedTab: FilterTab,
    onTabSelected: (FilterTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val themeConfig = LocalThemeConfig.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val tabTitle = when (tab) {
                FilterTab.ALL -> strings.allAgenda
                FilterTab.HABITS -> strings.dailyHabits
                FilterTab.TASKS -> strings.actionTasks
                FilterTab.HEALTH -> "Health & Water"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) {
                            if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color(0xFF262626)
                            else MaterialTheme.colorScheme.primary
                        } else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp)
                    .testTag("tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color.White
                        else MaterialTheme.colorScheme.onPrimary
                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Category Chips Horizontal Scroll
 */
@Composable
fun CategoryFilterRow(
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(strings.allCategories, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("category_all")
            )
        }
        items(TaskCategory.values()) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                leadingIcon = {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color(category.colorHex)
                    )
                },
                label = { Text(category.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(category.colorHex).copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("category_${category.name.lowercase()}")
            )
        }
    }
}

/**
 * Clean Zero State View with Centered "Add your task here" button
 */
@Composable
fun EmptyZeroStateView(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(glowAccent.primary.copy(alpha = 0.12f))
                    .border(1.dp, glowAccent.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = glowAccent.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "No tasks or habits scheduled",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Start fresh by adding your first daily habit or task.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Prominent Centered Call to Action Button
            Button(
                onClick = onAddTask,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color(0xFFE11D48) else glowAccent.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .googleAiGlow(glowAccent, enabled = themeConfig.isGlowEnabled, cornerRadius = 16.dp)
                    .testTag("btn_zero_state_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add your task here",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * Habit and Task Item Card with dynamic theme styling and actions
 */
@Composable
fun HabitTaskItemCard(
    item: HabitTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCalendarSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent
    val density = themeConfig.density

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aestheticContainer(
                aesthetic = themeConfig.aesthetic,
                glowAccent = glowAccent,
                hasGlow = false,
                cornerRadius = density.cornerRadius
            )
            .clickable { onToggle() }
            .padding(density.cardPadding)
            .testTag("task_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark Action Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isCompletedToday) {
                            if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color(0xFFE11D48)
                            else glowAccent.primary
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        }
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (item.isCompletedToday) Color.Transparent else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onToggle() }
                    .testTag("checkbox_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (item.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata tags (Time, Alarm, Category, Streak)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Time pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${item.getFormattedTime()} (${item.durationMinutes}m)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Native Alarm Indicator Pill
                    if (item.hasAlarm) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Alarm Active",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Alarm",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    // Streak pill for habits
                    if (item.isHabit) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (themeConfig.aesthetic) {
                                DesignAesthetic.NOTHING_UI -> Color(0xFF262626)
                                else -> glowAccent.primary.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = "🔥 ${item.streakCount} ${strings.streakPill}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (themeConfig.aesthetic == DesignAesthetic.NOTHING_UI) Color(0xFFE11D48) else glowAccent.primary
                            )
                        }
                    }
                }
            }

            // Action Icons (Calendar Sync, Edit, Delete)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCalendarSync,
                    modifier = Modifier.size(34.dp).testTag("sync_calendar_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = strings.syncCalendarLabel,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp).testTag("edit_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.editItemTitle,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).testTag("delete_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.deleteLabel,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Add / Edit Habit Task Dialog with Alarm and Date configuration
 */
@Composable
fun AddEditTaskDialog(
    initialItem: HabitTask? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: TaskCategory,
        isHabit: Boolean,
        hour: Int,
        minute: Int,
        durationMinutes: Int,
        priority: PriorityLevel,
        hasAlarm: Boolean,
        scheduledDate: String
    ) -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.getCategoryEnum() ?: TaskCategory.ROUTINE) }
    var isHabit by remember { mutableStateOf(initialItem?.isHabit ?: true) }
    var scheduledHour by remember { mutableIntStateOf(initialItem?.scheduledHour ?: 9) }
    var scheduledMinute by remember { mutableIntStateOf(initialItem?.scheduledMinute ?: 0) }
    var durationMinutes by remember { mutableIntStateOf(initialItem?.durationMinutes ?: 30) }
    var selectedPriority by remember { mutableStateOf(initialItem?.getPriorityEnum() ?: PriorityLevel.MEDIUM) }
    var hasAlarm by remember { mutableStateOf(initialItem?.hasAlarm ?: false) }
    var scheduledDate by remember { mutableStateOf(initialItem?.scheduledDate ?: "") }

    val todayStr = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }
    val tomorrowStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(cal.time)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) strings.addItemTitle else strings.editItemTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text(strings.itemTitlePlaceholder) },
                    modifier = Modifier.fillMaxWidth().testTag("input_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    placeholder = { Text(strings.itemDescPlaceholder) },
                    modifier = Modifier.fillMaxWidth().testTag("input_description"),
                    maxLines = 2
                )

                // Type Toggle (Habit vs Task)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isHabit) strings.habitTypeToggle else strings.taskTypeToggle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isHabit,
                        onCheckedChange = { isHabit = it },
                        modifier = Modifier.testTag("switch_is_habit")
                    )
                }

                // Native System Alarm Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessAlarm,
                            contentDescription = null,
                            tint = if (hasAlarm) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Native System Alarm",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Plays audio beep & heads-up alarm",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = hasAlarm,
                        onCheckedChange = { hasAlarm = it },
                        modifier = Modifier.testTag("switch_has_alarm")
                    )
                }

                // Quick Date Selection Chips
                Text(
                    text = "Scheduled Date (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = scheduledDate == todayStr || scheduledDate.isBlank(),
                        onClick = { scheduledDate = todayStr },
                        label = { Text("Today", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = scheduledDate == tomorrowStr,
                        onClick = { scheduledDate = tomorrowStr },
                        label = { Text("Tomorrow", fontSize = 11.sp) }
                    )
                }

                // Category Selector
                Text(
                    text = strings.categoryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TaskCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                // Time Schedule selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = scheduledHour.toString(),
                        onValueChange = {
                            val v = it.toIntOrNull()
                            if (v != null && v in 0..23) scheduledHour = v
                        },
                        label = { Text(strings.hourLabel) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = scheduledMinute.toString(),
                        onValueChange = {
                            val v = it.toIntOrNull()
                            if (v != null && v in 0..59) scheduledMinute = v
                        },
                        label = { Text(strings.minuteLabel) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durationMinutes.toString(),
                        onValueChange = {
                            val v = it.toIntOrNull()
                            if (v != null && v in 5..480) durationMinutes = v
                        },
                        label = { Text(strings.durationLabel) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title,
                            description,
                            selectedCategory,
                            isHabit,
                            scheduledHour,
                            scheduledMinute,
                            durationMinutes,
                            selectedPriority,
                            hasAlarm,
                            scheduledDate
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("btn_save_task")
            ) {
                Text(strings.saveButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}

fun getCategoryIcon(category: TaskCategory): ImageVector {
    return when (category) {
        TaskCategory.WORK -> Icons.Default.Work
        TaskCategory.HEALTH -> Icons.Default.FitnessCenter
        TaskCategory.MIND -> Icons.Default.Psychology
        TaskCategory.LEARNING -> Icons.Default.School
        TaskCategory.ROUTINE -> Icons.Default.Schedule
    }
}
