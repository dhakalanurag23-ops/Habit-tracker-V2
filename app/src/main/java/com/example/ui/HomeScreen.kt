package com.example.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.HabitTask
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.AiVisionDialog
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.ChaosSideQuestDialog
import com.example.ui.components.CustomBrandingDialog
import com.example.ui.components.EmptyZeroStateView
import com.example.ui.components.FilterTabRow
import com.example.ui.components.FutureSelfDialog
import com.example.ui.components.GamificationDialog
import com.example.ui.components.GeminiAssistantSheet
import com.example.ui.components.GeminiPromptBottomBar
import com.example.ui.components.HabitTaskItemCard
import com.example.ui.components.HealthNutritionCard
import com.example.ui.components.HeroProgressCard
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.MealSnapConfirmDialog
import com.example.ui.components.MonthlyReportDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TimetableDialog
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.LayoutDensity
import com.example.ui.theme.LocalThemeConfig
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings
import com.example.util.getAppStrings
import com.example.viewmodel.FilterTab
import com.example.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    initialOpenAiVision: Boolean = false,
    autoLaunchCamera: Boolean = false,
    onAiVisionOpenedHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent
    val strings = getAppStrings(uiState.appLanguage)
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<HabitTask?>(null) }
    var showAiVisionDialog by remember { mutableStateOf(false) }
    var showMonthlyReportDialog by remember { mutableStateOf(false) }
    var showTimetableDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGamificationDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showAiCoachDialog by remember { mutableStateOf(false) }
    var showSideQuestsDialog by remember { mutableStateOf(false) }
    var showFutureSelfDialog by remember { mutableStateOf(false) }
    var showBrandingDialog by remember { mutableStateOf(false) }

    // Meal photo camera capture launcher
    val mealCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.analyzeMealPhoto(bitmap)
        }
    }

    LaunchedEffect(initialOpenAiVision) {
        if (initialOpenAiVision) {
            showAiVisionDialog = true
            onAiVisionOpenedHandled()
        }
    }

    LaunchedEffect(uiState.messageSnackbar) {
        uiState.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        LocalAppLanguage provides uiState.appLanguage
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showBrandingDialog = true }
                        ) {
                            Text(
                                text = uiState.customAppBadgeEmoji,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.customAppName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        // 1. Grid/List view switcher
                        IconButton(
                            onClick = { viewModel.toggleGridView() },
                            modifier = Modifier.testTag("action_grid_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List View",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 2. Gamification (Trophy Room)
                        IconButton(
                            onClick = { showGamificationDialog = true },
                            modifier = Modifier.testTag("action_gamification")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = strings.rewardsAndBadges,
                                tint = glowAccent.primary
                            )
                        }

                        // 3. Monthly Review
                        IconButton(
                            onClick = {
                                viewModel.generateMonthlyReport()
                                showMonthlyReportDialog = true
                            },
                            modifier = Modifier.testTag("action_monthly_report")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = strings.insights,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 4. Offline Data Backup & Restore
                        IconButton(
                            onClick = { showBackupRestoreDialog = true },
                            modifier = Modifier.testTag("action_backup_restore")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = "Offline Backup & Restore",
                                tint = glowAccent.primary
                            )
                        }

                        // 5. Settings
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("action_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = strings.settingsTitle,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                // Official Gemini-Style Docked Prompt Input Bar
                GeminiPromptBottomBar(
                    onSendPrompt = { prompt ->
                        viewModel.processPromptInput(prompt)
                        showAiCoachDialog = true
                    },
                    onOpenCreateTask = {
                        itemToEdit = null
                        showAddEditDialog = true
                    },
                    onOpenPhotoToTask = { showAiVisionDialog = true },
                    onOpenMealSnap = { mealCameraLauncher.launch(null) },
                    onOpenSideQuests = {
                        viewModel.generateSideQuests()
                        showSideQuestsDialog = true
                    },
                    onOpenFutureSelf = {
                        viewModel.generateFutureSelf()
                        showFutureSelfDialog = true
                    },
                    onOpenAiCoach = { showAiCoachDialog = true },
                    isProcessing = uiState.isProcessingPrompt
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Non-truncating In-App Notification Banner
                InAppNotificationBanner(
                    banner = uiState.inAppBanner,
                    onDismiss = { viewModel.dismissInAppBanner() }
                )

                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val isExpandedLayout = maxWidth >= 720.dp

                if (isExpandedLayout) {
                    // DUAL-PANE RESPONSIVE TABLET / LANDSCAPE LAYOUT
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Pane: Controls, Stats & Health
                        Column(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HeroProgressCard(
                                totalCount = uiState.totalCount,
                                completedCount = uiState.completedCount,
                                completionPercentage = uiState.completionPercentage,
                                highestStreak = uiState.highestStreak,
                                userPoints = uiState.gamification.totalXp,
                                onOpenGamification = { showGamificationDialog = true }
                            )

                            // Health & Hydration Card
                            HealthNutritionCard(
                                summary = uiState.healthSummary,
                                foodLogs = uiState.foodLogs,
                                waterLogs = uiState.waterLogs,
                                onLogWater = { viewModel.logWater(it) },
                                onDeleteWater = { viewModel.deleteWater(it) },
                                onAddFood = { viewModel.logFood(it) },
                                onDeleteFood = { viewModel.deleteFood(it) },
                                onOpenMealSnap = { mealCameraLauncher.launch(null) }
                            )

                            FilterTabRow(
                                selectedTab = uiState.selectedFilter,
                                onTabSelected = { viewModel.setFilterTab(it) }
                            )

                            CategoryFilterRow(
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = { viewModel.setSelectedCategory(it) }
                            )
                        }

                        // Right Pane: Tasks & Habits Items List
                        Box(modifier = Modifier.weight(0.58f).fillMaxHeight()) {
                            if (uiState.items.isEmpty()) {
                                EmptyZeroStateView(
                                    onAddTask = {
                                        itemToEdit = null
                                        showAddEditDialog = true
                                    },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(themeConfig.density.itemSpacing)
                                ) {
                                    items(uiState.filteredItems, key = { it.id }) { item ->
                                        HabitTaskItemCard(
                                            item = item,
                                            onToggle = { viewModel.toggleTask(item.id) },
                                            onEdit = {
                                                itemToEdit = item
                                                showAddEditDialog = true
                                            },
                                            onDelete = { viewModel.deleteItem(item) },
                                            onCalendarSync = { viewModel.syncToCalendar(item) }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // COMPACT PHONE PORTRAIT LAYOUT
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(themeConfig.density.itemSpacing)
                        ) {
                            if (uiState.items.isEmpty()) {
                                // Zero State: clean blank screen centering prominent "Add your task here"
                                item {
                                    EmptyZeroStateView(
                                        onAddTask = {
                                            itemToEdit = null
                                            showAddEditDialog = true
                                        }
                                    )
                                }
                            } else {
                                // 1. Hero Dynamic Stats Ring Card
                                item {
                                    HeroProgressCard(
                                        totalCount = uiState.totalCount,
                                        completedCount = uiState.completedCount,
                                        completionPercentage = uiState.completionPercentage,
                                        highestStreak = uiState.highestStreak,
                                        userPoints = uiState.gamification.totalXp,
                                        onOpenGamification = { showGamificationDialog = true }
                                    )
                                }

                                // 2. Filter Tabs (All / Habits / Tasks)
                                item {
                                    FilterTabRow(
                                        selectedTab = uiState.selectedFilter,
                                        onTabSelected = { viewModel.setFilterTab(it) }
                                    )
                                }

                                // 3. Health & Calorie Tracker Module
                                item {
                                    HealthNutritionCard(
                                        summary = uiState.healthSummary,
                                        foodLogs = uiState.foodLogs,
                                        waterLogs = uiState.waterLogs,
                                        onLogWater = { viewModel.logWater(it) },
                                        onDeleteWater = { viewModel.deleteWater(it) },
                                        onAddFood = { viewModel.logFood(it) },
                                        onDeleteFood = { viewModel.deleteFood(it) },
                                        onOpenMealSnap = { mealCameraLauncher.launch(null) }
                                    )
                                }

                                // 4. Category Filter Chips
                                item {
                                    CategoryFilterRow(
                                        selectedCategory = uiState.selectedCategory,
                                        onCategorySelected = { viewModel.setSelectedCategory(it) }
                                    )
                                }

                                // 5. Habits / Tasks Items List
                                items(uiState.filteredItems, key = { it.id }) { item ->
                                    HabitTaskItemCard(
                                        item = item,
                                        onToggle = { viewModel.toggleTask(item.id) },
                                        onEdit = {
                                            itemToEdit = item
                                            showAddEditDialog = true
                                        },
                                        onDelete = { viewModel.deleteItem(item) },
                                        onCalendarSync = { viewModel.syncToCalendar(item) }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Dialog 1: Add / Edit Item (with Alarm and Date)
        if (showAddEditDialog) {
            AddEditTaskDialog(
                initialItem = itemToEdit,
                onDismiss = { showAddEditDialog = false },
                onSave = { title, desc, cat, isHabit, hour, min, dur, prio, hasAlarm, schedDate ->
                    viewModel.addOrUpdateHabit(
                        id = itemToEdit?.id ?: 0L,
                        title = title,
                        description = desc,
                        category = cat,
                        isHabit = isHabit,
                        hour = hour,
                        minute = min,
                        durationMinutes = dur,
                        priority = prio,
                        hasAlarm = hasAlarm,
                        scheduledDate = schedDate
                    )
                    showAddEditDialog = false
                }
            )
        }

        // Dialog 2: Photo to Task (Multimodal Image Parser)
        if (showAiVisionDialog) {
            AiVisionDialog(
                isAnalyzing = uiState.isAnalyzingImage,
                parsedTasks = uiState.parsedTasksFromImage,
                autoLaunchCamera = autoLaunchCamera,
                onDismiss = {
                    showAiVisionDialog = false
                    viewModel.clearParsedTasks()
                },
                onAnalyzeBitmap = { viewModel.analyzeImageWithGemini(it) },
                onImportTasks = {
                    viewModel.importParsedTasks(it)
                    showAiVisionDialog = false
                }
            )
        }

        // Dialog 3: Habit Insights & Review
        if (showMonthlyReportDialog) {
            MonthlyReportDialog(
                isGenerating = uiState.isGeneratingReport,
                report = uiState.monthlyReport,
                onDismiss = {
                    showMonthlyReportDialog = false
                    viewModel.dismissMonthlyReport()
                }
            )
        }

        // Dialog 4: Timetable & Schedule Export
        if (showTimetableDialog) {
            TimetableDialog(
                tasks = uiState.items,
                onDismiss = { showTimetableDialog = false },
                onExportToGoogleSheets = { viewModel.exportTimetable() },
                onSyncAllToGoogleCalendar = {
                    uiState.items.forEach { viewModel.syncToCalendar(it) }
                }
            )
        }

        // Dialog 5: Dedicated Settings & Decoupled Customization
        if (showSettingsDialog) {
            SettingsDialog(
                currentConfig = uiState.themeConfig,
                currentLanguage = uiState.appLanguage,
                onSelectLanguage = { lang -> viewModel.setLanguage(lang) },
                onSelectAesthetic = { aesthetic -> viewModel.setDesignAesthetic(aesthetic) },
                onSelectDensity = { density -> viewModel.setLayoutDensity(density) },
                onSelectGlowAccent = { accent -> viewModel.setGlowAccent(accent) },
                onToggleGlow = { enabled -> viewModel.setGlowEnabled(enabled) },
                onSyncLauncherIcon = { viewModel.syncLauncherIconForCurrentAesthetic() },
                onDismiss = { showSettingsDialog = false }
            )
        }

        // Dialog 6: Gamification (Trophy Room)
        if (showGamificationDialog) {
            GamificationDialog(
                gamificationState = uiState.gamification,
                onDismiss = { showGamificationDialog = false }
            )
        }

        // Dialog 7: Offline Data Backup & Restore
        if (showBackupRestoreDialog) {
            BackupRestoreDialog(
                language = uiState.appLanguage,
                themeConfig = uiState.themeConfig,
                gamification = uiState.gamification,
                items = uiState.items,
                onRestore = { payload -> viewModel.restoreBackupPayload(payload) },
                onResetAllData = { viewModel.resetAllData() },
                onDismiss = { showBackupRestoreDialog = false }
            )
        }

        // Dialog 8: Full-Screen Gemini Assistant Sheet
        if (showAiCoachDialog) {
            GeminiAssistantSheet(
                messages = uiState.coachMessages,
                currentPhase = uiState.geminiPhase,
                phaseStatusMessage = uiState.geminiPhaseMessage,
                isRoastMode = uiState.isCoachRoastMode,
                onToggleRoastMode = { viewModel.toggleCoachRoastMode(it) },
                onSendMessage = { viewModel.sendCoachMessage(it) },
                onDismiss = { showAiCoachDialog = false }
            )
        }

        // Dialog 9: Chaos Mode RPG Side Quests
        if (showSideQuestsDialog) {
            ChaosSideQuestDialog(
                quests = uiState.sideQuests,
                onGenerateMore = { viewModel.generateSideQuests() },
                onCompleteQuest = { viewModel.completeSideQuest(it) },
                onDismiss = { showSideQuestsDialog = false }
            )
        }

        // Dialog 10: 5-Year Future Self Simulator
        if (showFutureSelfDialog) {
            FutureSelfDialog(
                simulation = uiState.futureSelfSimulation,
                onSimulate = { viewModel.generateFutureSelf() },
                onDismiss = {
                    showFutureSelfDialog = false
                    viewModel.dismissFutureSelf()
                }
            )
        }

        // Dialog 11: AI Calorie Meal Scan Result Confirm
        uiState.analyzedMeal?.let { meal ->
            MealSnapConfirmDialog(
                analyzedMeal = meal,
                onConfirm = { viewModel.confirmAnalyzedMeal() },
                onDismiss = { viewModel.dismissAnalyzedMeal() }
            )
        }

        // Dialog 12: Custom App Branding
        if (showBrandingDialog) {
            CustomBrandingDialog(
                currentName = uiState.customAppName,
                currentEmoji = uiState.customAppBadgeEmoji,
                onSave = { name, emoji -> viewModel.setCustomBranding(name, emoji) },
                onDismiss = { showBrandingDialog = false }
            )
        }
    }
}
