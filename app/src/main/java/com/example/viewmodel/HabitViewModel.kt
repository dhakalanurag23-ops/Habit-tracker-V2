package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitPulseApplication
import com.example.ai.AnalyzedMealResult
import com.example.ai.GeminiAnalyzer
import com.example.ai.MonthlyAnalyticsReport
import com.example.ai.ParsedIntentResult
import com.example.ai.ParsedTaskItem
import com.example.model.AlarmImportance
import com.example.model.ChatSender
import com.example.model.CoachChatMessage
import com.example.model.DailyHealthSummary
import com.example.model.FoodEntry
import com.example.model.FutureSelfSimulation
import com.example.model.GamificationBadge
import com.example.model.HabitTask
import com.example.model.MealType
import com.example.model.PriorityLevel
import com.example.model.SideQuest
import com.example.model.TaskCategory
import com.example.model.UserGamificationState
import com.example.model.WaterLog
import com.example.model.calculateUserLevel
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.GlowAccentColor
import com.example.ui.theme.HabitPulseThemeConfig
import com.example.ui.theme.LayoutDensity
import com.example.util.AppLanguage
import com.example.util.BackupDataPayload
import com.example.util.GoogleEcosystemIntegration
import com.example.util.IconSwitcher
import com.example.util.TaskAlarmManager
import com.example.widget.TaskWidget
import com.example.model.GeminiPhase
import com.example.ui.components.BannerType
import com.example.ui.components.InAppBannerMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class FilterTab {
    ALL, HABITS, TASKS, HEALTH
}

data class HabitUiState(
    val items: List<HabitTask> = emptyList(),
    val filteredItems: List<HabitTask> = emptyList(),
    val foodLogs: List<FoodEntry> = emptyList(),
    val waterLogs: List<WaterLog> = emptyList(),
    val healthSummary: DailyHealthSummary = DailyHealthSummary(),
    val selectedFilter: FilterTab = FilterTab.ALL,
    val selectedCategory: TaskCategory? = null,
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val completionPercentage: Int = 0,
    val highestStreak: Int = 0,
    val isGridView: Boolean = false,
    val themeConfig: HabitPulseThemeConfig = HabitPulseThemeConfig(),
    val appLanguage: AppLanguage = AppLanguage.ENGLISH_US,
    val gamification: UserGamificationState = UserGamificationState(totalXp = 0, currentLevel = 1, levelName = "Novice Explorer", currentLevelXp = 0, nextLevelXp = 100, levelProgressPercentage = 0f, unlockedBadgeCount = 0),
    val customAppName: String = "HabitPulse",
    val customAppBadgeEmoji: String = "⚡",
    val isAnalyzingImage: Boolean = false,
    val parsedTasksFromImage: List<ParsedTaskItem>? = null,
    val analyzedMeal: AnalyzedMealResult? = null,
    val isGeneratingReport: Boolean = false,
    val monthlyReport: MonthlyAnalyticsReport? = null,
    val coachMessages: List<CoachChatMessage> = emptyList(),
    val isCoachRoastMode: Boolean = false,
    val isCoachTyping: Boolean = false,
    val geminiPhase: GeminiPhase = GeminiPhase.IDLE,
    val geminiPhaseMessage: String? = null,
    val sideQuests: List<SideQuest> = emptyList(),
    val futureSelfSimulation: FutureSelfSimulation? = null,
    val messageSnackbar: String? = null,
    val inAppBanner: InAppBannerMessage? = null,
    val isProcessingPrompt: Boolean = false
)

private data class FilterState(
    val tab: FilterTab,
    val category: TaskCategory?,
    val isGrid: Boolean
)

private data class AiState(
    val isAnalyzing: Boolean,
    val parsed: List<ParsedTaskItem>?,
    val analyzedMeal: AnalyzedMealResult?,
    val isGenerating: Boolean,
    val report: MonthlyAnalyticsReport?,
    val isProcessingPrompt: Boolean,
    val coachMessages: List<CoachChatMessage>,
    val isCoachRoastMode: Boolean,
    val isCoachTyping: Boolean,
    val geminiPhase: GeminiPhase,
    val geminiPhaseMessage: String?,
    val sideQuests: List<SideQuest>,
    val futureSelf: FutureSelfSimulation?
)

private data class AppConfigState(
    val theme: HabitPulseThemeConfig,
    val language: AppLanguage,
    val customAppName: String,
    val customBadgeEmoji: String
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HabitPulseApplication
    private val repository = app.repository
    private val settingsManager = com.example.data.SettingsManager(application)

    private val _filterTab = MutableStateFlow(FilterTab.ALL)
    private val _selectedCategory = MutableStateFlow<TaskCategory?>(null)
    private val _isGridView = MutableStateFlow(false)

    private val _themeConfig = settingsManager.themeConfigFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, HabitPulseThemeConfig()
    )
    private val _appLanguage = settingsManager.languageFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, AppLanguage.ENGLISH_US
    )
    private val _customAppName = settingsManager.customBrandingFlow.map { it.first }.stateIn(
        viewModelScope, SharingStarted.Eagerly, "HabitPulse"
    )
    private val _customBadgeEmoji = settingsManager.customBrandingFlow.map { it.second }.stateIn(
        viewModelScope, SharingStarted.Eagerly, "⚡"
    )

    private val _gamificationState = settingsManager.gamificationXpFlow.map { savedXp ->
        val (level, levelName) = calculateUserLevel(savedXp)
        val base = UserGamificationState(totalXp = savedXp, currentLevel = level, levelName = levelName, currentLevelXp = 0, nextLevelXp = 100, levelProgressPercentage = 0f, unlockedBadgeCount = 0)
        
        val updatedBadges = base.badges.map { badge ->
            when (badge.id) {
                "first_step" -> if (savedXp >= 10) badge.copy(isUnlocked = true, unlockedDate = "Unlocked") else badge
                "century_club" -> if (savedXp >= 100) badge.copy(isUnlocked = true, unlockedDate = "Unlocked") else badge
                "zen_grandmaster" -> if (savedXp >= 1000) badge.copy(isUnlocked = true, unlockedDate = "Unlocked") else badge
                else -> badge
            }
        }
        val unlockedCount = updatedBadges.count { it.isUnlocked }
        val nextLevelThreshold = when (level) {
            1 -> 100
            2 -> 250
            3 -> 500
            4 -> 1000
            5 -> 2000
            else -> 5000
        }
        val prevLevelThreshold = when (level) {
            1 -> 0
            2 -> 100
            3 -> 250
            4 -> 500
            5 -> 1000
            else -> 2000
        }
        val progress = ((savedXp - prevLevelThreshold).toFloat() / (nextLevelThreshold - prevLevelThreshold)).coerceIn(0f, 1f)
        
        base.copy(
            levelProgressPercentage = progress,
            unlockedBadgeCount = unlockedCount,
            badges = updatedBadges
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserGamificationState())

    private val _isAnalyzingImage = MutableStateFlow(false)
    private val _parsedTasks = MutableStateFlow<List<ParsedTaskItem>?>(null)
    private val _analyzedMeal = MutableStateFlow<AnalyzedMealResult?>(null)
    private val _isGeneratingReport = MutableStateFlow(false)
    private val _monthlyReport = MutableStateFlow<MonthlyAnalyticsReport?>(null)
    private val _isProcessingPrompt = MutableStateFlow(false)

    private val _coachMessages = MutableStateFlow<List<CoachChatMessage>>(emptyList())
    private val _isCoachRoastMode = MutableStateFlow(false)
    private val _isCoachTyping = MutableStateFlow(false)
    private val _geminiPhase = MutableStateFlow(GeminiPhase.IDLE)
    private val _geminiPhaseMessage = MutableStateFlow<String?>(null)
    private val _sideQuests = MutableStateFlow<List<SideQuest>>(emptyList())
    private val _futureSelf = MutableStateFlow<FutureSelfSimulation?>(null)

    private val _snackbar = MutableStateFlow<String?>(null)
    private val _inAppBanner = MutableStateFlow<InAppBannerMessage?>(null)

    private val todayDate = HabitTask.getCurrentDateString()

    private val _filterFlow = combine(_filterTab, _selectedCategory, _isGridView) { tab, cat, isGrid ->
        FilterState(tab, cat, isGrid)
    }

    private val _configFlow = combine(_themeConfig, _appLanguage, _customAppName, _customBadgeEmoji) { theme, lang, name, emoji ->
        AppConfigState(theme, lang, name, emoji)
    }

    private val _aiFlow = combine(
        combine(_isAnalyzingImage, _parsedTasks, _analyzedMeal, _isGeneratingReport, _monthlyReport) { a, b, c, d, e ->
            Triple(Pair(a, b), c, Pair(d, e))
        },
        combine(_isProcessingPrompt, _coachMessages, _isCoachRoastMode, _isCoachTyping, _sideQuests) { a, b, c, d, e ->
            Triple(Pair(a, b), Pair(c, d), e)
        },
        combine(_geminiPhase, _geminiPhaseMessage, _futureSelf) { phase, msg, futureSelf ->
            Triple(phase, msg, futureSelf)
        }
    ) { part1, part2, part3 ->
        AiState(
            isAnalyzing = part1.first.first,
            parsed = part1.first.second,
            analyzedMeal = part1.second,
            isGenerating = part1.third.first,
            report = part1.third.second,
            isProcessingPrompt = part2.first.first,
            coachMessages = part2.first.second,
            isCoachRoastMode = part2.second.first,
            isCoachTyping = part2.second.second,
            geminiPhase = part3.first,
            geminiPhaseMessage = part3.second,
            sideQuests = part2.third,
            futureSelf = part3.third
        )
    }

    val uiState: StateFlow<HabitUiState> = combine(
        repository.allItems,
        repository.getFoodLogsForDate(todayDate),
        repository.getWaterLogsForDate(todayDate),
        _filterFlow,
        combine(_configFlow, _gamificationState, _aiFlow, _snackbar, _inAppBanner) { config, gamification, ai, snackbar, banner ->
            Tuple5(config, gamification, ai, snackbar, banner)
        }
    ) { allItems, foodLogs, waterLogs, filterState, others ->
        val (config, gamification, aiState, snackbar, banner) = others

        val filtered = allItems.filter { item ->
            val matchesTab = when (filterState.tab) {
                FilterTab.ALL -> true
                FilterTab.HABITS -> item.isHabit
                FilterTab.TASKS -> !item.isHabit
                FilterTab.HEALTH -> false
            }
            val matchesCategory = filterState.category == null || item.getCategoryEnum() == filterState.category
            matchesTab && matchesCategory
        }

        val completed = allItems.count { it.isCompletedToday }
        val total = allItems.size
        val percentage = if (total > 0) (completed * 100) / total else 0
        val best = allItems.maxOfOrNull { it.bestStreak } ?: 0

        val totalCalories = foodLogs.sumOf { it.calories }
        val totalWater = waterLogs.sumOf { it.amountMl }
        val totalProtein = foodLogs.sumOf { it.proteinGrams }
        val totalCarbs = foodLogs.sumOf { it.carbsGrams }
        val totalFat = foodLogs.sumOf { it.fatGrams }

        val healthSummary = DailyHealthSummary(
            calorieGoal = 2000,
            caloriesConsumedToday = totalCalories,
            waterGoalMl = 2500,
            waterConsumedTodayMl = totalWater,
            totalProteinGrams = totalProtein,
            totalCarbsGrams = totalCarbs,
            totalFatGrams = totalFat
        )

        HabitUiState(
            items = allItems,
            filteredItems = filtered,
            foodLogs = foodLogs,
            waterLogs = waterLogs,
            healthSummary = healthSummary,
            selectedFilter = filterState.tab,
            selectedCategory = filterState.category,
            totalCount = total,
            completedCount = completed,
            completionPercentage = percentage,
            highestStreak = best,
            isGridView = filterState.isGrid,
            themeConfig = config.theme,
            appLanguage = config.language,
            gamification = gamification,
            customAppName = config.customAppName,
            customAppBadgeEmoji = config.customBadgeEmoji,
            isAnalyzingImage = aiState.isAnalyzing,
            parsedTasksFromImage = aiState.parsed,
            analyzedMeal = aiState.analyzedMeal,
            isGeneratingReport = aiState.isGenerating,
            monthlyReport = aiState.report,
            coachMessages = aiState.coachMessages,
            isCoachRoastMode = aiState.isCoachRoastMode,
            isCoachTyping = aiState.isCoachTyping,
            geminiPhase = aiState.geminiPhase,
            geminiPhaseMessage = aiState.geminiPhaseMessage,
            sideQuests = aiState.sideQuests,
            futureSelfSimulation = aiState.futureSelf,
            messageSnackbar = snackbar,
            inAppBanner = banner,
            isProcessingPrompt = aiState.isProcessingPrompt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState()
    )



    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsManager.updateLanguage(language) }
        _snackbar.value = "Language set to ${language.displayName}"
    }

    fun setFilterTab(tab: FilterTab) {
        _filterTab.value = tab
    }

    fun setSelectedCategory(category: TaskCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun setCustomBranding(name: String, emoji: String) {
        val newName = name.ifBlank { "HabitPulse" }
        val newEmoji = emoji.ifBlank { "⚡" }
        viewModelScope.launch { settingsManager.updateCustomBranding(newName, newEmoji) }
        _snackbar.value = "Updated app branding to '${newName}'"
    }

    fun toggleTask(id: Long) {
        viewModelScope.launch {
            val updated = repository.toggleCompletion(id)
            if (updated != null && updated.isCompletedToday) {
                val addedXp = if (updated.isHabit) 15 + (updated.streakCount * 5) else 10
                awardXp(addedXp, if (updated.isHabit) "Daily Habit Streak (+${addedXp} XP)" else "Task Done (+10 XP)")
                // Cancel active alarm and escalation if completed
                TaskAlarmManager.cancelTaskAlarm(getApplication(), id)
            }

            try {
                TaskWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            repository.insertWater(WaterLog(amountMl = amountMl))
            awardXp(10, "Logged +${amountMl}ml Hydration")
            showInAppBanner(
                InAppBannerMessage(
                    title = "Hydration Logged",
                    message = "Added +${amountMl}ml to your daily hydration.",
                    type = BannerType.SUCCESS
                )
            )
        }
    }

    fun deleteWater(log: WaterLog) {
        viewModelScope.launch {
            repository.deleteWater(log)
            showInAppBanner(
                InAppBannerMessage(
                    title = "Hydration Updated",
                    message = "Removed ${log.amountMl}ml entry.",
                    type = BannerType.INFO
                )
            )
        }
    }

    fun logFood(entry: FoodEntry) {
        viewModelScope.launch {
            repository.insertFood(entry)
            awardXp(15, "Logged meal: ${entry.name}")
            showInAppBanner(
                InAppBannerMessage(
                    title = "Meal Logged",
                    message = "Recorded '${entry.name}' (${entry.calories} kcal).",
                    type = BannerType.SUCCESS
                )
            )
        }
    }

    fun deleteFood(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFood(entry)
            showInAppBanner(
                InAppBannerMessage(
                    title = "Meal Removed",
                    message = "Deleted '${entry.name}'.",
                    type = BannerType.INFO
                )
            )
        }
    }

    fun analyzeMealPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzingImage.value = true
            _geminiPhase.value = GeminiPhase.THINKING
            _geminiPhaseMessage.value = "Analyzing meal photo with Gemini Vision..."
            val result = GeminiAnalyzer.analyzeMealImage(bitmap)
            _isAnalyzingImage.value = false
            _geminiPhase.value = GeminiPhase.IDLE
            _geminiPhaseMessage.value = null
            result.onSuccess { meal ->
                _analyzedMeal.value = meal
                showInAppBanner(
                    InAppBannerMessage(
                        title = "Meal Detected",
                        message = "${meal.name} (~${meal.calories} kcal). Confirm to log.",
                        type = BannerType.AI_ACTION
                    )
                )
            }.onFailure { err ->
                showInAppBanner(
                    InAppBannerMessage(
                        title = "Meal Scan Failed",
                        message = err.message ?: "Could not identify meal from photo.",
                        type = BannerType.ERROR
                    )
                )
            }
        }
    }

    fun confirmAnalyzedMeal() {
        val meal = _analyzedMeal.value ?: return
        logFood(
            FoodEntry(
                name = meal.name,
                calories = meal.calories,
                carbsGrams = meal.carbsGrams,
                proteinGrams = meal.proteinGrams,
                fatGrams = meal.fatGrams,
                mealType = meal.mealType.name
            )
        )
        _analyzedMeal.value = null
    }

    fun dismissAnalyzedMeal() {
        _analyzedMeal.value = null
    }

    // Gemini-Style Bottom Bar Natural Language & Audio Processor
    fun processPromptInput(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isProcessingPrompt.value = true
            val userMsg = CoachChatMessage(sender = ChatSender.USER, message = prompt)
            _coachMessages.value = _coachMessages.value + userMsg

            // Phase 1: Thinking
            _geminiPhase.value = GeminiPhase.THINKING
            _geminiPhaseMessage.value = "Interpreting natural language prompt..."
            delay(400)

            // Phase 2: Searching Context
            _geminiPhase.value = GeminiPhase.SEARCHING_CONTEXT
            _geminiPhaseMessage.value = "Analyzing local habits, streaks, and health logs..."
            
            val intent = GeminiAnalyzer.parsePromptToIntent(prompt)

            // Phase 3: Executing Action
            _geminiPhase.value = GeminiPhase.EXECUTING_ACTION
            when (intent) {
                is ParsedIntentResult.CreateTask -> {
                    _geminiPhaseMessage.value = "Scheduling ${intent.tasks.size} task(s)..."
                    importParsedTasks(intent.tasks)
                    val reply = "✨ I've scheduled ${intent.tasks.size} new item(s) for you:\n" +
                            intent.tasks.joinToString("\n") { "• ${it.title} (${it.category.displayName}, ${String.format("%02d:%02d", it.scheduledHour, it.scheduledMinute)})" }
                    val coachMsg = CoachChatMessage(sender = ChatSender.COACH, message = reply)
                    _coachMessages.value = _coachMessages.value + coachMsg
                    showInAppBanner(
                        InAppBannerMessage(
                            title = "Gemini Created Tasks",
                            message = "Added ${intent.tasks.size} task(s) to your daily routine.",
                            type = BannerType.AI_ACTION
                        )
                    )
                }
                is ParsedIntentResult.LogFood -> {
                    _geminiPhaseMessage.value = "Logging ${intent.food.name} (${intent.food.calories} kcal)..."
                    logFood(intent.food)
                    val reply = "🍽️ Logged **${intent.food.name}**:\n• Calories: ${intent.food.calories} kcal\n• Protein: ${intent.food.proteinGrams}g | Carbs: ${intent.food.carbsGrams}g | Fat: ${intent.food.fatGrams}g"
                    val coachMsg = CoachChatMessage(sender = ChatSender.COACH, message = reply)
                    _coachMessages.value = _coachMessages.value + coachMsg
                }
                is ParsedIntentResult.LogWater -> {
                    _geminiPhaseMessage.value = "Adding ${intent.amountMl}ml water..."
                    logWater(intent.amountMl)
                    val reply = "💧 Recorded +${intent.amountMl}ml of water towards your daily hydration goal!"
                    val coachMsg = CoachChatMessage(sender = ChatSender.COACH, message = reply)
                    _coachMessages.value = _coachMessages.value + coachMsg
                }
                is ParsedIntentResult.ChatReply -> {
                    _geminiPhaseMessage.value = "Formulating response..."
                    val coachMsg = CoachChatMessage(sender = ChatSender.COACH, message = intent.reply)
                    _coachMessages.value = _coachMessages.value + coachMsg
                }
            }

            _geminiPhase.value = GeminiPhase.COMPLETED
            _geminiPhaseMessage.value = null
            _isProcessingPrompt.value = false
        }
    }

    // Coach Chat
    fun sendCoachMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val userMsg = CoachChatMessage(sender = ChatSender.USER, message = text)
            _coachMessages.value = _coachMessages.value + userMsg
            _isCoachTyping.value = true

            _geminiPhase.value = GeminiPhase.THINKING
            _geminiPhaseMessage.value = if (_isCoachRoastMode.value) "Preparing unfiltered roast..." else "Analyzing your routines and history..."

            val allHabits = repository.getAllSnapshot()
            val allFoods = repository.getAllFoodSnapshot()
            val allWaters = repository.getAllWaterSnapshot()

            _geminiPhase.value = GeminiPhase.SEARCHING_CONTEXT
            _geminiPhaseMessage.value = "Querying streaks and habit consistency..."

            val reply = GeminiAnalyzer.generateCoachAdvice(
                history = _coachMessages.value,
                habits = allHabits,
                foods = allFoods,
                waters = allWaters,
                isRoastMode = _isCoachRoastMode.value
            )

            _isCoachTyping.value = false
            _geminiPhase.value = GeminiPhase.COMPLETED
            _geminiPhaseMessage.value = null

            val coachMsg = CoachChatMessage(
                sender = ChatSender.COACH,
                message = reply,
                isRoast = _isCoachRoastMode.value
            )
            _coachMessages.value = _coachMessages.value + coachMsg
        }
    }

    fun toggleCoachRoastMode(enabled: Boolean) {
        _isCoachRoastMode.value = enabled
        showInAppBanner(
            InAppBannerMessage(
                title = if (enabled) "🔥 Roast Mode Active" else "🌟 Supportive Mode Active",
                message = if (enabled) "AI Coach will not hold back on excuses." else "AI Coach will give encouraging guidance.",
                type = if (enabled) BannerType.WARNING else BannerType.INFO
            )
        )
    }

    // Future Self Simulation
    fun generateFutureSelf() {
        viewModelScope.launch {
            val habits = repository.getAllSnapshot()
            val foods = repository.getAllFoodSnapshot()
            val waters = repository.getAllWaterSnapshot()
            val simulation = GeminiAnalyzer.generateFutureSelfSimulation(habits, foods, waters)
            _futureSelf.value = simulation
        }
    }

    fun dismissFutureSelf() {
        _futureSelf.value = null
    }

    // Side Quests (Chaos Mode)
    fun generateSideQuests() {
        viewModelScope.launch {
            val habits = repository.getAllSnapshot()
            val quests = GeminiAnalyzer.generateSideQuests(habits)
            _sideQuests.value = quests
            _snackbar.value = "🎲 Generated ${quests.size} Chaos RPG Side Quests!"
        }
    }

    fun completeSideQuest(questId: String) {
        val current = _sideQuests.value
        val target = current.find { it.id == questId } ?: return
        if (target.isCompleted) return

        _sideQuests.value = current.map {
            if (it.id == questId) it.copy(isCompleted = true) else it
        }
        awardXp(target.xpReward, "Cleared Side Quest: ${target.title}")
        _snackbar.value = "🎉 Cleared '${target.title}' (+${target.xpReward} XP)!"
    }

    private fun awardXp(amount: Int, reason: String) {
        val current = _gamificationState.value
        val newTotal = current.totalXp + amount
        val (newLevel, newLevelTitle) = calculateUserLevel(newTotal)

        val updatedBadges = current.badges.map { badge ->
            when (badge.id) {
                "first_step" -> if (newTotal >= 10) badge.copy(isUnlocked = true, unlockedDate = "Today") else badge
                "century_club" -> if (newTotal >= 100) badge.copy(isUnlocked = true, unlockedDate = "Today") else badge
                "zen_grandmaster" -> if (newTotal >= 1000) badge.copy(isUnlocked = true, unlockedDate = "Today") else badge
                else -> badge
            }
        }

        val unlockedCount = updatedBadges.count { it.isUnlocked }
        val nextLevelThreshold = when (newLevel) {
            1 -> 100
            2 -> 250
            3 -> 500
            4 -> 1000
            5 -> 2000
            else -> 5000
        }
        val prevLevelThreshold = when (newLevel) {
            1 -> 0
            2 -> 100
            3 -> 250
            4 -> 500
            5 -> 1000
            else -> 2000
        }
        val progress = ((newTotal - prevLevelThreshold).toFloat() / (nextLevelThreshold - prevLevelThreshold)).coerceIn(0f, 1f)

        viewModelScope.launch { settingsManager.updateGamificationXp(newTotal) }

        if (newLevel > current.currentLevel) {
            val config = _themeConfig.value
            val accent = config.glowAccent
            showInAppBanner(InAppBannerMessage(
                title = "🏆 Leveled Up to $newLevel!",
                message = "Rank: $newLevelTitle",
                type = com.example.ui.components.BannerType.SUCCESS
            ))
        }
    }

    fun addOrUpdateHabit(
        id: Long = 0,
        title: String,
        description: String,
        category: TaskCategory,
        isHabit: Boolean,
        hour: Int,
        minute: Int,
        durationMinutes: Int,
        priority: PriorityLevel,
        alarmImportance: AlarmImportance = AlarmImportance.MEDIUM,
        hasAlarm: Boolean = false,
        scheduledDate: String = ""
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val newItem = HabitTask(
                    title = title,
                    description = description,
                    category = category.name,
                    isHabit = isHabit,
                    scheduledHour = hour,
                    scheduledMinute = minute,
                    durationMinutes = durationMinutes,
                    priority = priority.name,
                    alarmImportance = alarmImportance.name,
                    hasAlarm = hasAlarm,
                    scheduledDate = scheduledDate
                )
                val newId = repository.insert(newItem)
                val savedItem = newItem.copy(id = newId)
                if (hasAlarm) {
                    TaskAlarmManager.scheduleTaskAlarm(getApplication(), savedItem)
                }
                _snackbar.value = if (isHabit) "Added new habit '$title'!" else "Added new task '$title'!"
            } else {
                val existing = repository.getById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        title = title,
                        description = description,
                        category = category.name,
                        isHabit = isHabit,
                        scheduledHour = hour,
                        scheduledMinute = minute,
                        durationMinutes = durationMinutes,
                        priority = priority.name,
                        alarmImportance = alarmImportance.name,
                        hasAlarm = hasAlarm,
                        scheduledDate = scheduledDate
                    )
                    repository.update(updated)
                    if (hasAlarm) {
                        TaskAlarmManager.scheduleTaskAlarm(getApplication(), updated)
                    } else {
                        TaskAlarmManager.cancelTaskAlarm(getApplication(), id)
                    }
                    _snackbar.value = "Updated '$title'!"
                }
            }
            try {
                TaskWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteItem(item: HabitTask) {
        viewModelScope.launch {
            if (item.hasAlarm) {
                TaskAlarmManager.cancelTaskAlarm(getApplication(), item.id)
            }
            repository.delete(item)
            _snackbar.value = "Deleted '${item.title}'"
            try {
                TaskWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun setDesignAesthetic(aesthetic: DesignAesthetic, autoSwitchIcon: Boolean = true) {
        val newConfig = _themeConfig.value.copy(aesthetic = aesthetic)
        viewModelScope.launch { settingsManager.updateThemeConfig(newConfig) }
        if (autoSwitchIcon) {
            IconSwitcher.switchLauncherIconForAesthetic(getApplication(), aesthetic)
            _snackbar.value = "Aesthetic: ${aesthetic.title}. Launcher icon synced!"
        }
    }

    fun setLayoutDensity(density: LayoutDensity) {
        val newConfig = _themeConfig.value.copy(density = density)
        viewModelScope.launch { settingsManager.updateThemeConfig(newConfig) }
        _snackbar.value = "Density: ${density.title}"
    }

    fun setGlowAccent(accent: GlowAccentColor) {
        val newConfig = _themeConfig.value.copy(glowAccent = accent)
        viewModelScope.launch { settingsManager.updateThemeConfig(newConfig) }
        _snackbar.value = "Glow accent: ${accent.title}"
    }

    fun setGlowEnabled(enabled: Boolean) {
        val newConfig = _themeConfig.value.copy(isGlowEnabled = enabled)
        viewModelScope.launch { settingsManager.updateThemeConfig(newConfig) }
    }

    fun syncLauncherIconForCurrentAesthetic() {
        val currentAesthetic = _themeConfig.value.aesthetic
        IconSwitcher.switchLauncherIconForAesthetic(getApplication(), currentAesthetic)
        _snackbar.value = "Launcher icon synced to ${currentAesthetic.title}!"
    }

    fun restoreBackupPayload(payload: BackupDataPayload) {
        viewModelScope.launch {
            settingsManager.updateLanguage(payload.appLanguage)
            settingsManager.updateThemeConfig(payload.themeConfig)
            settingsManager.updateGamificationXp(payload.gamification.totalXp)
            repository.replaceAll(payload.items)

            for (item in payload.items) {
                if (item.hasAlarm) {
                    TaskAlarmManager.scheduleTaskAlarm(getApplication(), item)
                }
            }

            _snackbar.value = "Restored ${payload.items.size} tasks, habits, and profile successfully!"
            try {
                TaskWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            val all = repository.getAllSnapshot()
            for (item in all) {
                if (item.hasAlarm) {
                    TaskAlarmManager.cancelTaskAlarm(getApplication(), item.id)
                }
            }
            repository.deleteAll()
            settingsManager.updateGamificationXp(0)
            _snackbar.value = "Offline database completely erased."
            try {
                TaskWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun analyzeImageWithGemini(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzingImage.value = true
            val result = GeminiAnalyzer.parseImageToTasks(bitmap)
            _isAnalyzingImage.value = false
            result.onSuccess { tasks ->
                _parsedTasks.value = tasks
                _snackbar.value = "Gemini parsed ${tasks.size} actionable items from image!"
            }.onFailure { err ->
                _snackbar.value = "Photo to Task failed: ${err.message}"
            }
        }
    }

    fun importParsedTasks(tasks: List<ParsedTaskItem>) {
        viewModelScope.launch {
            val entities = tasks.map { item ->
                HabitTask(
                    title = item.title,
                    description = item.description,
                    category = item.category.name,
                    isHabit = item.isHabit,
                    scheduledHour = item.scheduledHour,
                    scheduledMinute = item.scheduledMinute,
                    durationMinutes = item.durationMinutes,
                    priority = item.priority.name,
                    alarmImportance = item.alarmImportance.name,
                    hasAlarm = item.alarmImportance != AlarmImportance.UNIMPORTANT,
                    scheduledDate = item.scheduledDate
                )
            }
            val insertedIds = repository.insertAll(entities)
            for ((idx, entity) in entities.withIndex()) {
                if (entity.hasAlarm) {
                    val id = insertedIds.getOrNull(idx) ?: 0L
                    TaskAlarmManager.scheduleTaskAlarm(getApplication(), entity.copy(id = id))
                }
            }
            _parsedTasks.value = null
            _snackbar.value = "Successfully imported ${tasks.size} tasks into your routine!"
            awardXp(tasks.size * 10, "Imported from Gemini")
            try {
                TaskWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun clearParsedTasks() {
        _parsedTasks.value = null
    }

    fun generateMonthlyReport() {
        viewModelScope.launch {
            _isGeneratingReport.value = true
            val items = repository.getAllSnapshot()
            val result = GeminiAnalyzer.generateMonthlyAnalytics(items)
            _isGeneratingReport.value = false
            result.onSuccess { report ->
                _monthlyReport.value = report
            }.onFailure { err ->
                _snackbar.value = "Report generation error: ${err.message}"
            }
        }
    }

    fun dismissMonthlyReport() {
        _monthlyReport.value = null
    }

    fun syncToCalendar(task: HabitTask) {
        GoogleEcosystemIntegration.syncTaskToGoogleCalendar(getApplication(), task)
    }

    fun exportTimetable() {
        viewModelScope.launch {
            val items = repository.getAllSnapshot()
            GoogleEcosystemIntegration.exportTimetableToGoogleSheets(getApplication(), items)
        }
    }

    fun showInAppBanner(banner: InAppBannerMessage) {
        _inAppBanner.value = banner
    }

    fun dismissInAppBanner() {
        _inAppBanner.value = null
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
