package com.example.model

data class GamificationBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String = "",
    val category: String = "General",
    val xpReward: Int = 50
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val totalPoints: Int,
    val currentStreak: Int,
    val levelName: String,
    val isCurrentUser: Boolean = false,
    val isFriend: Boolean = false
)

data class UserGamificationState(
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val levelName: String = "Novice Explorer",
    val currentLevelXp: Int = 0,
    val nextLevelXp: Int = 100,
    val levelProgressPercentage: Float = 0f,
    val unlockedBadgeCount: Int = 0,
    val badges: List<GamificationBadge> = defaultBadges()
)

data class LevelMilestone(
    val level: Int,
    val title: String,
    val requiredXp: Int,
    val emoji: String,
    val perk: String
)

fun getLevelMilestones(): List<LevelMilestone> {
    return listOf(
        LevelMilestone(1, "Novice Explorer", 0, "🌱", "Beginning of your local habit journey"),
        LevelMilestone(2, "Apprentice Achiever", 100, "🎯", "Unlocks Side Quest Rarity upgrades"),
        LevelMilestone(3, "Consistency Master", 250, "🔥", "Custom theme glow enhancements"),
        LevelMilestone(4, "Flow State Veteran", 500, "⚡", "Advanced future-self projections"),
        LevelMilestone(5, "Habit Champion", 1000, "💎", "Mastery Badge status"),
        LevelMilestone(6, "Zen Grandmaster", 2000, "👑", "Ultimate lifestyle sovereignty")
    )
}

fun calculateUserLevel(totalXp: Int): Pair<Int, String> {
    return when {
        totalXp < 100 -> Pair(1, "Novice Explorer")
        totalXp < 250 -> Pair(2, "Apprentice Achiever")
        totalXp < 500 -> Pair(3, "Consistency Master")
        totalXp < 1000 -> Pair(4, "Flow State Veteran")
        totalXp < 2000 -> Pair(5, "Habit Champion")
        else -> Pair(6, "Zen Grandmaster")
    }
}

fun defaultBadges(): List<GamificationBadge> {
    return listOf(
        GamificationBadge(
            id = "first_step",
            title = "First Step",
            description = "Completed your very first task in HabitPulse",
            iconEmoji = "🎯",
            isUnlocked = false,
            unlockedDate = "",
            category = "Milestones",
            xpReward = 25
        ),
        GamificationBadge(
            id = "streak_starter",
            title = "3-Day Flame",
            description = "Maintained an unbroken daily routine for 3 consecutive days",
            iconEmoji = "🔥",
            isUnlocked = false,
            unlockedDate = "",
            category = "Streaks",
            xpReward = 50
        ),
        GamificationBadge(
            id = "century_club",
            title = "Century Club",
            description = "Accumulated over 100 XP points through daily consistency",
            iconEmoji = "💯",
            isUnlocked = false,
            unlockedDate = "",
            category = "Points",
            xpReward = 50
        ),
        GamificationBadge(
            id = "habit_machine",
            title = "7-Day Legend",
            description = "Achieved a legendary 7-day streak on any daily habit",
            iconEmoji = "⚡",
            isUnlocked = false,
            unlockedDate = "",
            category = "Streaks",
            xpReward = 100
        ),
        GamificationBadge(
            id = "early_bird",
            title = "Dawn Raider",
            description = "Checked off a scheduled morning habit before 8:30 AM",
            iconEmoji = "🌅",
            isUnlocked = false,
            unlockedDate = "",
            category = "Routines",
            xpReward = 50
        ),
        GamificationBadge(
            id = "ai_visionary",
            title = "AI Visionary",
            description = "Extracted and imported tasks using Photo to Task",
            iconEmoji = "📸",
            isUnlocked = false,
            unlockedDate = "",
            category = "Intelligence",
            xpReward = 75
        ),
        GamificationBadge(
            id = "health_tracker",
            title = "Wellness Pro",
            description = "Logged water intake and meal nutrition with Calorie AI",
            iconEmoji = "🥗",
            isUnlocked = false,
            unlockedDate = "",
            category = "Health",
            xpReward = 50
        ),
        GamificationBadge(
            id = "zen_grandmaster",
            title = "Grandmaster",
            description = "Surpassed 1000 XP in your personal productivity journey",
            iconEmoji = "👑",
            isUnlocked = false,
            unlockedDate = "",
            category = "Mastery",
            xpReward = 250
        )
    )
}
