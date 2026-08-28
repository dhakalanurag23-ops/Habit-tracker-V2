package com.example.util

import android.content.Context
import com.example.model.GamificationBadge
import com.example.model.HabitTask
import com.example.model.PriorityLevel
import com.example.model.TaskCategory
import com.example.model.UserGamificationState
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.GlowAccentColor
import com.example.ui.theme.HabitPulseThemeConfig
import com.example.ui.theme.LayoutDensity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupDataPayload(
    val exportedAt: String,
    val appLanguage: AppLanguage,
    val themeConfig: HabitPulseThemeConfig,
    val gamification: UserGamificationState,
    val items: List<HabitTask>
)

object DataBackupHelper {

    fun generateBackupJson(
        language: AppLanguage,
        themeConfig: HabitPulseThemeConfig,
        gamification: UserGamificationState,
        items: List<HabitTask>
    ): String {
        val root = JSONObject()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        root.put("version", 2)
        root.put("exportedAt", sdf.format(Date()))
        root.put("language", language.name)

        // Theme config
        val themeObj = JSONObject().apply {
            put("aesthetic", themeConfig.aesthetic.name)
            put("density", themeConfig.density.name)
            put("glowAccent", themeConfig.glowAccent.name)
            put("isGlowEnabled", themeConfig.isGlowEnabled)
        }
        root.put("theme", themeObj)

        // Gamification
        val gamificationObj = JSONObject().apply {
            put("totalXp", gamification.totalXp)
            put("currentLevel", gamification.currentLevel)
            put("levelName", gamification.levelName)
            put("unlockedBadgeCount", gamification.unlockedBadgeCount)
        }
        root.put("gamification", gamificationObj)

        // Items
        val itemsArray = JSONArray()
        for (item in items) {
            val itemObj = JSONObject().apply {
                put("title", item.title)
                put("description", item.description)
                put("category", item.category)
                put("isHabit", item.isHabit)
                put("streakCount", item.streakCount)
                put("bestStreak", item.bestStreak)
                put("isCompletedToday", item.isCompletedToday)
                put("targetDaysPerWeek", item.targetDaysPerWeek)
                put("scheduledHour", item.scheduledHour)
                put("scheduledMinute", item.scheduledMinute)
                put("durationMinutes", item.durationMinutes)
                put("priority", item.priority)
                put("lastCompletedDate", item.lastCompletedDate)
                put("completedHistoryJson", item.completedHistoryJson)
                put("hasAlarm", item.hasAlarm)
                put("scheduledDate", item.scheduledDate)
                put("createdAt", item.createdAt)
            }
            itemsArray.put(itemObj)
        }
        root.put("items", itemsArray)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): Result<BackupDataPayload> {
        return try {
            val root = JSONObject(jsonString)
            val exportedAt = root.optString("exportedAt", "Unknown")

            val langStr = root.optString("language", AppLanguage.ENGLISH_US.name)
            val language = try {
                AppLanguage.valueOf(langStr)
            } catch (e: Exception) {
                AppLanguage.ENGLISH_US
            }

            // Theme Config
            val themeObj = root.optJSONObject("theme")
            val aesthetic = try {
                DesignAesthetic.valueOf(themeObj?.optString("aesthetic", DesignAesthetic.LIQUID_GLASS.name) ?: DesignAesthetic.LIQUID_GLASS.name)
            } catch (e: Exception) {
                DesignAesthetic.LIQUID_GLASS
            }
            val density = try {
                LayoutDensity.valueOf(themeObj?.optString("density", LayoutDensity.MAXIMALISM.name) ?: LayoutDensity.MAXIMALISM.name)
            } catch (e: Exception) {
                LayoutDensity.MAXIMALISM
            }
            val glowAccent = try {
                GlowAccentColor.valueOf(themeObj?.optString("glowAccent", GlowAccentColor.CYAN_PULSE.name) ?: GlowAccentColor.CYAN_PULSE.name)
            } catch (e: Exception) {
                GlowAccentColor.CYAN_PULSE
            }
            val isGlow = themeObj?.optBoolean("isGlowEnabled", true) ?: true

            val themeConfig = HabitPulseThemeConfig(
                aesthetic = aesthetic,
                density = density,
                glowAccent = glowAccent,
                isGlowEnabled = isGlow
            )

            // Gamification
            val gamificationObj = root.optJSONObject("gamification")
            val totalXp = gamificationObj?.optInt("totalXp", 0) ?: 0
            val currentLevel = gamificationObj?.optInt("currentLevel", 1) ?: 1
            val levelName = gamificationObj?.optString("levelName", "Habit Novice") ?: "Habit Novice"
            val unlockedBadges = gamificationObj?.optInt("unlockedBadgeCount", 0) ?: 0

            val gamificationState = UserGamificationState(
                totalXp = totalXp,
                currentLevel = currentLevel,
                levelName = levelName,
                unlockedBadgeCount = unlockedBadges
            )

            // Items list
            val itemsArray = root.optJSONArray("items") ?: JSONArray()
            val parsedItems = mutableListOf<HabitTask>()

            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                parsedItems.add(
                    HabitTask(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        category = obj.optString("category", TaskCategory.ROUTINE.name),
                        isHabit = obj.optBoolean("isHabit", true),
                        streakCount = obj.optInt("streakCount", 0),
                        bestStreak = obj.optInt("bestStreak", 0),
                        isCompletedToday = obj.optBoolean("isCompletedToday", false),
                        targetDaysPerWeek = obj.optInt("targetDaysPerWeek", 7),
                        scheduledHour = obj.optInt("scheduledHour", 9),
                        scheduledMinute = obj.optInt("scheduledMinute", 0),
                        durationMinutes = obj.optInt("durationMinutes", 30),
                        priority = obj.optString("priority", PriorityLevel.MEDIUM.name),
                        lastCompletedDate = obj.optString("lastCompletedDate", ""),
                        completedHistoryJson = obj.optString("completedHistoryJson", "[]"),
                        hasAlarm = obj.optBoolean("hasAlarm", false),
                        scheduledDate = obj.optString("scheduledDate", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            Result.success(
                BackupDataPayload(
                    exportedAt = exportedAt,
                    appLanguage = language,
                    themeConfig = themeConfig,
                    gamification = gamificationState,
                    items = parsedItems
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
