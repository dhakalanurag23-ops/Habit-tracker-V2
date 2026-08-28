package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MealType(val displayName: String, val iconEmoji: String) {
    BREAKFAST("Breakfast", "🍳"),
    LUNCH("Lunch", "🥗"),
    DINNER("Dinner", "🍲"),
    SNACK("Snack", "🍎")
}

@Entity(tableName = "food_logs")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val carbsGrams: Int = 0,
    val proteinGrams: Int = 0,
    val fatGrams: Int = 0,
    val mealType: String = MealType.LUNCH.name,
    val dateStr: String = HabitTask.getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getMealTypeEnum(): MealType {
        return try {
            MealType.valueOf(mealType)
        } catch (e: Exception) {
            MealType.LUNCH
        }
    }
}

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val dateStr: String = HabitTask.getCurrentDateString(),
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyHealthSummary(
    val calorieGoal: Int = 2000,
    val caloriesConsumedToday: Int = 0,
    val waterGoalMl: Int = 2500,
    val waterConsumedTodayMl: Int = 0,
    val totalProteinGrams: Int = 0,
    val totalCarbsGrams: Int = 0,
    val totalFatGrams: Int = 0
)

enum class QuestRarity(val label: String, val colorHex: Long, val badgeEmoji: String) {
    COMMON("Common", 0xFF94A3B8, "⚪"),
    RARE("Rare", 0xFF3B82F6, "🔷"),
    EPIC("Epic", 0xFF8B5CF6, "🔮"),
    LEGENDARY("Legendary", 0xFFF59E0B, "👑")
}

data class SideQuest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val xpReward: Int = 50,
    val rarity: QuestRarity = QuestRarity.COMMON,
    val isCompleted: Boolean = false,
    val memeFlavor: String = ""
)

data class FutureSelfSimulation(
    val title: String = "Cyber-Optimized 2031 Archetype",
    val vitalityScore: Int = 88,
    val glowUpPercentage: Int = 94,
    val energyLevel: String = "Supercharged",
    val physicalArchetype: String = "Peak Athletic Alchemist",
    val timelineNarrative: String = "In 5 years, your consistency in daily hydration and disciplined routines transformed your physical and cognitive stamina by 300%.",
    val humorousCaution: String = "Watch out: skipping water today might revert you to a caffeine-fueled goblin in 2029!",
    val recommendedMicroHabit: String = "Drink 500ml water right before bed and walk 15 minutes post-lunch."
)

enum class ChatSender {
    USER, COACH
}

data class CoachChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRoast: Boolean = false
)
