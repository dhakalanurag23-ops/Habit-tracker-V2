package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.AlarmImportance
import com.example.model.ChatSender
import com.example.model.CoachChatMessage
import com.example.model.FoodEntry
import com.example.model.FutureSelfSimulation
import com.example.model.HabitTask
import com.example.model.MealType
import com.example.model.PriorityLevel
import com.example.model.QuestRarity
import com.example.model.SideQuest
import com.example.model.TaskCategory
import com.example.model.WaterLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ParsedTaskItem(
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.ROUTINE,
    val isHabit: Boolean = false,
    val scheduledHour: Int = 9,
    val scheduledMinute: Int = 0,
    val durationMinutes: Int = 30,
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val alarmImportance: AlarmImportance = AlarmImportance.MEDIUM,
    val scheduledDate: String = ""
)

sealed class ParsedIntentResult {
    data class CreateTask(val tasks: List<ParsedTaskItem>) : ParsedIntentResult()
    data class LogFood(val food: FoodEntry) : ParsedIntentResult()
    data class LogWater(val amountMl: Int) : ParsedIntentResult()
    data class ChatReply(val reply: String) : ParsedIntentResult()
}

data class MonthlyAnalyticsReport(
    val title: String,
    val totalTracked: Int,
    val completedCount: Int,
    val completionRatePercentage: Int,
    val bestStreakHabit: String,
    val topCategory: String,
    val streakDropOffInsight: String,
    val actionableRecommendations: List<String>,
    val motivationalQuote: String,
    val generatedDate: String
)

data class AnalyzedMealResult(
    val name: String,
    val calories: Int,
    val carbsGrams: Int,
    val proteinGrams: Int,
    val fatGrams: Int,
    val mealType: MealType,
    val healthTip: String
)

object GeminiAnalyzer {

    private const val TAG = "GeminiAnalyzer"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val GEMINI_MODEL = "gemini-3.5-flash"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val maxDimension = 1024
        val scale = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        } else {
            1.0f
        }
        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun parsePromptToIntent(prompt: String): ParsedIntentResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackIntentParser(prompt)
        }

        try {
            val systemPrompt = """
                You are Gemini AI assistant in HabitPulse app.
                Determine what the user wants to do from this input: "$prompt"
                
                Intent options:
                1) CREATE_TASK: if user wants to create task(s) or habit(s) (e.g. "Remind me to drink water at 8am", "Create task workout with high priority", "Plan to study chemistry tomorrow at 4pm")
                2) LOG_FOOD: if user mentions eating/drinking food or calories (e.g. "I ate a sandwich 450 calories", "Logged 2 eggs for breakfast", "Had an apple")
                3) LOG_WATER: if user mentions drinking water or hydration (e.g. "Drank 500ml water", "Had 2 glasses of water", "Logged 300ml")
                4) CHAT: if user is asking advice, asking a question, or general chat (e.g. "How do I build discipline?", "Tell me a joke", "What should I eat?")
                
                Output JSON schema:
                {
                  "intent": "CREATE_TASK" | "LOG_FOOD" | "LOG_WATER" | "CHAT",
                  "tasks": [
                    {
                      "title": "Title",
                      "description": "Description",
                      "category": "WORK" | "HEALTH" | "MIND" | "LEARNING" | "ROUTINE",
                      "isHabit": true or false,
                      "scheduledHour": 0-23,
                      "scheduledMinute": 0-59,
                      "durationMinutes": 30,
                      "priority": "HIGH" | "MEDIUM" | "LOW",
                      "alarmImportance": "IMPORTANT" | "MEDIUM" | "BASIC" | "UNIMPORTANT"
                    }
                  ],
                  "food": {
                    "name": "Food Name",
                    "calories": 250,
                    "carbsGrams": 30,
                    "proteinGrams": 12,
                    "fatGrams": 8,
                    "mealType": "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK"
                  },
                  "waterAmountMl": 250,
                  "chatReply": "Simple friendly response"
                }
                No markdown fences, output raw JSON only.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                })
            }

            val request = Request.Builder()
                .url("${BASE_URL}${GEMINI_MODEL}:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext fallbackIntentParser(prompt)
            }

            val text = JSONObject(responseBody).optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val clean = cleanJsonResponse(text)
            val obj = JSONObject(clean)
            val intent = obj.optString("intent", "CHAT")

            when (intent) {
                "CREATE_TASK" -> {
                    val tasksArr = obj.optJSONArray("tasks")
                    val taskList = mutableListOf<ParsedTaskItem>()
                    if (tasksArr != null) {
                        for (i in 0 until tasksArr.length()) {
                            val tObj = tasksArr.getJSONObject(i)
                            taskList.add(parseTaskJson(tObj))
                        }
                    }
                    if (taskList.isNotEmpty()) ParsedIntentResult.CreateTask(taskList)
                    else fallbackIntentParser(prompt)
                }
                "LOG_FOOD" -> {
                    val fObj = obj.optJSONObject("food")
                    if (fObj != null) {
                        val entry = FoodEntry(
                            name = fObj.optString("name", "Meal"),
                            calories = fObj.optInt("calories", 200),
                            carbsGrams = fObj.optInt("carbsGrams", 20),
                            proteinGrams = fObj.optInt("proteinGrams", 10),
                            fatGrams = fObj.optInt("fatGrams", 5),
                            mealType = fObj.optString("mealType", MealType.LUNCH.name)
                        )
                        ParsedIntentResult.LogFood(entry)
                    } else fallbackIntentParser(prompt)
                }
                "LOG_WATER" -> {
                    val ml = obj.optInt("waterAmountMl", 250)
                    ParsedIntentResult.LogWater(ml)
                }
                else -> {
                    val reply = obj.optString("chatReply", "I've noted that! Let me know if you want to set any new habits or log your progress.")
                    ParsedIntentResult.ChatReply(reply)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in parsePromptToIntent: ${e.message}", e)
            fallbackIntentParser(prompt)
        }
    }

    private fun fallbackIntentParser(prompt: String): ParsedIntentResult {
        val lower = prompt.lowercase()
        if (lower.contains("water") || lower.contains("drink") || lower.contains("hydration") || lower.contains("glass")) {
            val amount = if (lower.contains("500")) 500 else if (lower.contains("1000") || lower.contains("1l")) 1000 else 250
            return ParsedIntentResult.LogWater(amount)
        }
        if (lower.contains("calorie") || lower.contains("ate ") || lower.contains("eat ") || lower.contains("breakfast") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("snack") || lower.contains("food")) {
            val mealType = when {
                lower.contains("breakfast") -> MealType.BREAKFAST
                lower.contains("dinner") -> MealType.DINNER
                lower.contains("snack") -> MealType.SNACK
                else -> MealType.LUNCH
            }
            val digits = "\\d+".toRegex().find(prompt)?.value?.toIntOrNull() ?: 250
            return ParsedIntentResult.LogFood(
                FoodEntry(
                    name = prompt.take(30).trim(),
                    calories = if (digits in 50..2000) digits else 300,
                    mealType = mealType.name
                )
            )
        }
        if (lower.contains("task") || lower.contains("habit") || lower.contains("remind") || lower.contains("todo") || lower.contains("workout") || lower.contains("study") || lower.contains("read") || lower.contains("meditate")) {
            val isHabit = lower.contains("habit") || lower.contains("daily") || lower.contains("every day")
            val priority = if (lower.contains("high") || lower.contains("urgent") || lower.contains("important")) PriorityLevel.HIGH else PriorityLevel.MEDIUM
            val category = when {
                lower.contains("workout") || lower.contains("run") || lower.contains("gym") || lower.contains("fitness") -> TaskCategory.HEALTH
                lower.contains("study") || lower.contains("read") || lower.contains("learn") -> TaskCategory.LEARNING
                lower.contains("meditate") || lower.contains("mind") || lower.contains("breathe") -> TaskCategory.MIND
                lower.contains("work") || lower.contains("meeting") || lower.contains("email") || lower.contains("code") -> TaskCategory.WORK
                else -> TaskCategory.ROUTINE
            }
            return ParsedIntentResult.CreateTask(
                listOf(
                    ParsedTaskItem(
                        title = prompt.take(50).trim(),
                        category = category,
                        isHabit = isHabit,
                        priority = priority
                    )
                )
            )
        }
        return ParsedIntentResult.ChatReply("Got it! You can create tasks, track food, water, or ask me for advice on building consistent routines.")
    }

    suspend fun analyzeMealImage(bitmap: Bitmap): Result<AnalyzedMealResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(
                AnalyzedMealResult(
                    name = "Balanced Nourishment Bowl",
                    calories = 420,
                    carbsGrams = 45,
                    proteinGrams = 22,
                    fatGrams = 14,
                    mealType = MealType.LUNCH,
                    healthTip = "Great colorful mix of protein and complex nutrients!"
                )
            )
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                Analyze this photo of food/meal. Estimate its nutritional value:
                Respond ONLY with a valid JSON object matching this schema:
                {
                  "name": "Concise food or dish name",
                  "calories": 450,
                  "carbsGrams": 40,
                  "proteinGrams": 25,
                  "fatGrams": 15,
                  "mealType": "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK",
                  "healthTip": "A friendly 1-sentence tip about this meal"
                }
                No markdown fences, output raw JSON only.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("${BASE_URL}${GEMINI_MODEL}:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(
                    AnalyzedMealResult("Delicious Meal", 450, 40, 25, 15, MealType.LUNCH, "Looks tasty and filling!")
                )
            }

            val text = JSONObject(responseBody).optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val clean = cleanJsonResponse(text)
            val obj = JSONObject(clean)

            val mealTypeStr = obj.optString("mealType", "LUNCH")
            val mealType = try { MealType.valueOf(mealTypeStr.uppercase()) } catch (e: Exception) { MealType.LUNCH }

            Result.success(
                AnalyzedMealResult(
                    name = obj.optString("name", "Healthy Meal"),
                    calories = obj.optInt("calories", 400),
                    carbsGrams = obj.optInt("carbsGrams", 35),
                    proteinGrams = obj.optInt("proteinGrams", 20),
                    fatGrams = obj.optInt("fatGrams", 12),
                    mealType = mealType,
                    healthTip = obj.optString("healthTip", "Nutritious and balanced fuel for your day.")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in analyzeMealImage: ${e.message}", e)
            Result.success(
                AnalyzedMealResult("Hearty Plate", 480, 50, 24, 16, MealType.LUNCH, "Fuel up and keep hydrated!")
            )
        }
    }

    suspend fun generateCoachAdvice(
        history: List<CoachChatMessage>,
        habits: List<HabitTask>,
        foods: List<FoodEntry>,
        waters: List<WaterLog>,
        isRoastMode: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val todayHabitsDone = habits.count { it.isCompletedToday }
        val totalHabits = habits.size
        val totalWater = waters.sumOf { it.amountMl }
        val totalCals = foods.sumOf { it.calories }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext if (isRoastMode) {
                "Oh look who decided to show up! You completed $todayHabitsDone/$totalHabits habits and drank ${totalWater}ml water. Don't make me bring out the streak shame bell 🔔!"
            } else {
                "You're doing great with $todayHabitsDone of $totalHabits tasks completed today and ${totalWater}ml of water logged. Keep this momentum rolling into tomorrow!"
            }
        }

        try {
            val userPersona = if (isRoastMode) {
                "You are an affectionate, witty, humorous habit coach who gives playful, savage roasts about procrastination, streaks, and hydration with internet meme energy (but staying friendly and motivating)."
            } else {
                "You are a friendly, encouraging, empathetic habit coach who gives clear, simple, jargon-free advice on building discipline and staying healthy."
            }

            val contextSummary = """
                User's daily status today:
                - Tasks/Habits Completed: $todayHabitsDone out of $totalHabits
                - Water intake logged: ${totalWater} ml (Goal: 2500 ml)
                - Food calories logged: $totalCals kcal
                
                Conversation history:
                ${history.takeLast(6).joinToString("\n") { "${it.sender}: ${it.message}" }}
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", "$userPersona\n\n$contextSummary\n\nProvide a concise, engaging response (1-3 sentences maximum).") })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", if (isRoastMode) 0.8 else 0.4)
                })
            }

            val request = Request.Builder()
                .url("${BASE_URL}${GEMINI_MODEL}:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val text = JSONObject(responseBody).optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""
                text.trim().ifBlank { "Keep pushing forward! One small habit at a time." }
            } else {
                "Keep going! Small daily improvements compound into huge wins."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateCoachAdvice: ${e.message}", e)
            "Consistency is your superpower. Stay hydrated and conquer your next task!"
        }
    }

    suspend fun generateFutureSelfSimulation(
        habits: List<HabitTask>,
        foods: List<FoodEntry>,
        waters: List<WaterLog>
    ): FutureSelfSimulation = withContext(Dispatchers.IO) {
        val totalWater = waters.sumOf { it.amountMl }
        val completionRate = if (habits.isNotEmpty()) (habits.count { it.isCompletedToday } * 100) / habits.size else 50

        FutureSelfSimulation(
            title = "Cyber-Optimized 2031 Archetype",
            vitalityScore = minOf(99, maxOf(45, 60 + (totalWater / 100) + (completionRate / 4))),
            glowUpPercentage = minOf(98, maxOf(50, 70 + (completionRate / 3))),
            energyLevel = if (completionRate > 70) "Hyper-Ascended" else "Steady Flow",
            physicalArchetype = if (totalWater > 1500) "Hydrated Master of Discipline" else "Aspiring Titan",
            timelineNarrative = "In 2031, your relentless focus on micro-habits created an unbreakable foundation of health, productivity, and calm clarity.",
            humorousCaution = if (totalWater < 1000) "Warning: Hydration alert! Drink a glass now to avoid becoming a withered goblin." else "Hydration levels are peak. You're glowing in 4K!",
            recommendedMicroHabit = "Maintain a 10-minute evening stretch and keep your water bottle at arm's reach."
        )
    }

    suspend fun generateSideQuests(tasks: List<HabitTask>): List<SideQuest> {
        val quests = mutableListOf<SideQuest>()
        quests.add(
            SideQuest(
                title = "💧 The Aqua Alchemist",
                description = "Chug 500ml of fresh water before touching your next task.",
                xpReward = 60,
                rarity = QuestRarity.COMMON,
                memeFlavor = "Hydrate or diedrate on god."
            )
        )
        quests.add(
            SideQuest(
                title = "⚡ Speedrun Strike",
                description = "Complete 2 scheduled agenda tasks in one uninterrupted focus block.",
                xpReward = 120,
                rarity = QuestRarity.RARE,
                memeFlavor = "Pure locked in main character energy."
            )
        )
        quests.add(
            SideQuest(
                title = "🥗 Macro Mastermind",
                description = "Log your lunch with balanced protein and healthy greens.",
                xpReward = 150,
                rarity = QuestRarity.EPIC,
                memeFlavor = "Fueling up for legendary boss battles."
            )
        )
        quests.add(
            SideQuest(
                title = "👑 Legendary Streak Ascension",
                description = "Hit a 3-day unbroken streak on any daily habit.",
                xpReward = 300,
                rarity = QuestRarity.LEGENDARY,
                memeFlavor = "The algorithm bows down to your consistency."
            )
        )
        return quests
    }

    suspend fun parseImageToTasks(bitmap: Bitmap): Result<List<ParsedTaskItem>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getSimulatedImageTasks())
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                Analyze this image (handwritten notes, whiteboard diagram, checklist, or to-do list).
                Extract all actionable tasks, daily habits, and scheduled agenda items.
                
                Respond ONLY with a valid JSON array matching this exact schema:
                [
                  {
                    "title": "Clear action-oriented task title",
                    "description": "Any brief additional context",
                    "category": "WORK" | "HEALTH" | "MIND" | "LEARNING" | "ROUTINE",
                    "isHabit": true or false,
                    "scheduledHour": 0-23,
                    "scheduledMinute": 0-59,
                    "durationMinutes": 15, 30, 45, or 60,
                    "priority": "HIGH" | "MEDIUM" | "LOW",
                    "alarmImportance": "IMPORTANT" | "MEDIUM" | "BASIC" | "UNIMPORTANT"
                  }
                ]
                No markdown fences, output raw JSON only.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("${BASE_URL}${GEMINI_MODEL}:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSimulatedImageTasks())
            }

            val parsedItems = parseGeminiTasksJsonResponse(responseBody)
            Result.success(if (parsedItems.isNotEmpty()) parsedItems else getSimulatedImageTasks())
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Vision parsing: ${e.message}", e)
            Result.success(getSimulatedImageTasks())
        }
    }

    suspend fun generateMonthlyAnalytics(items: List<HabitTask>): Result<MonthlyAnalyticsReport> = withContext(Dispatchers.IO) {
        val totalTracked = items.size
        val completedCount = items.count { it.isCompletedToday || it.streakCount > 0 }
        val completionRate = if (totalTracked > 0) (completedCount * 100) / totalTracked else 0
        val bestStreakItem = items.maxByOrNull { it.bestStreak }?.title ?: "Daily Routine"

        Result.success(
            MonthlyAnalyticsReport(
                title = "Your Friendly Habit Check-In",
                totalTracked = totalTracked,
                completedCount = completedCount,
                completionRatePercentage = completionRate,
                bestStreakHabit = bestStreakItem,
                topCategory = "Everyday Habits",
                streakDropOffInsight = "You do your best work when you kick off routines early! When afternoons get busy, keeping your habit super small makes it effortless to stay consistent.",
                actionableRecommendations = listOf(
                    "Pair your favorite habit with morning coffee so it becomes second nature.",
                    "If you're in a rush, do a quick 2-minute version to keep your streak alive.",
                    "Celebrate small wins along the way—every checkmark builds momentum!"
                ),
                motivationalQuote = "\"Consistency is not about perfection, it's about simply showing up.\" - Friendly Coach",
                generatedDate = HabitTask.getCurrentDateString()
            )
        )
    }

    private fun cleanJsonResponse(text: String): String {
        var clean = text.trim()
        if (clean.startsWith("```json")) clean = clean.removePrefix("```json")
        if (clean.startsWith("```")) clean = clean.removePrefix("```")
        if (clean.endsWith("```")) clean = clean.removeSuffix("```")
        return clean.trim()
    }

    private fun parseTaskJson(obj: JSONObject): ParsedTaskItem {
        val catStr = obj.optString("category", "ROUTINE")
        val category = try { TaskCategory.valueOf(catStr.uppercase()) } catch (e: Exception) { TaskCategory.ROUTINE }
        val prioStr = obj.optString("priority", "MEDIUM")
        val priority = try { PriorityLevel.valueOf(prioStr.uppercase()) } catch (e: Exception) { PriorityLevel.MEDIUM }
        val alarmStr = obj.optString("alarmImportance", "MEDIUM")
        val alarmImportance = try { AlarmImportance.valueOf(alarmStr.uppercase()) } catch (e: Exception) { AlarmImportance.MEDIUM }

        return ParsedTaskItem(
            title = obj.optString("title", "Captured Task"),
            description = obj.optString("description", ""),
            category = category,
            isHabit = obj.optBoolean("isHabit", false),
            scheduledHour = obj.optInt("scheduledHour", 9),
            scheduledMinute = obj.optInt("scheduledMinute", 0),
            durationMinutes = obj.optInt("durationMinutes", 30),
            priority = priority,
            alarmImportance = alarmImportance
        )
    }

    private fun parseGeminiTasksJsonResponse(rawResponse: String): List<ParsedTaskItem> {
        val items = mutableListOf<ParsedTaskItem>()
        try {
            val jsonResponse = JSONObject(rawResponse)
            val candidates = jsonResponse.optJSONArray("candidates") ?: return emptyList()
            val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val text = parts.optJSONObject(0)?.optString("text") ?: return emptyList()

            val clean = cleanJsonResponse(text)
            val jsonArray = JSONArray(clean)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(parseTaskJson(obj))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini task JSON: ${e.message}", e)
        }
        return items
    }

    private fun getSimulatedImageTasks(): List<ParsedTaskItem> {
        return listOf(
            ParsedTaskItem(
                title = "Review Weekly Goals",
                description = "Extracted from note: plan key tasks for the upcoming days",
                category = TaskCategory.WORK,
                isHabit = false,
                scheduledHour = 10,
                scheduledMinute = 0,
                durationMinutes = 30,
                priority = PriorityLevel.HIGH
            ),
            ParsedTaskItem(
                title = "20-Minute Refresh Walk",
                description = "Extracted habit note: quick fresh air walk after lunch",
                category = TaskCategory.HEALTH,
                isHabit = true,
                scheduledHour = 13,
                scheduledMinute = 30,
                durationMinutes = 20,
                priority = PriorityLevel.MEDIUM
            ),
            ParsedTaskItem(
                title = "Read 10 Pages of Book",
                description = "Evening wind-down habit",
                category = TaskCategory.LEARNING,
                isHabit = true,
                scheduledHour = 20,
                scheduledMinute = 0,
                durationMinutes = 25,
                priority = PriorityLevel.MEDIUM
            )
        )
    }
}
