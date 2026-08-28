package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.UserGamificationState
import com.example.ui.theme.DesignAesthetic
import com.example.ui.theme.GlowAccentColor
import com.example.ui.theme.HabitPulseThemeConfig
import com.example.ui.theme.LayoutDensity
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    private val THEME_AESTHETIC = stringPreferencesKey("theme_aesthetic")
    private val THEME_DENSITY = stringPreferencesKey("theme_density")
    private val THEME_GLOW = stringPreferencesKey("theme_glow")
    private val THEME_GLOW_ENABLED = booleanPreferencesKey("theme_glow_enabled")
    private val APP_LANGUAGE = stringPreferencesKey("app_language")
    private val CUSTOM_APP_NAME = stringPreferencesKey("custom_app_name")
    private val CUSTOM_BADGE_EMOJI = stringPreferencesKey("custom_badge_emoji")
    private val GAMIFICATION_XP = intPreferencesKey("gamification_total_xp")

    val themeConfigFlow: Flow<HabitPulseThemeConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val aesthetic = runCatching { DesignAesthetic.valueOf(preferences[THEME_AESTHETIC] ?: "") }.getOrDefault(DesignAesthetic.LIQUID_GLASS)
            val density = runCatching { LayoutDensity.valueOf(preferences[THEME_DENSITY] ?: "") }.getOrDefault(LayoutDensity.MAXIMALISM)
            val glow = runCatching { GlowAccentColor.valueOf(preferences[THEME_GLOW] ?: "") }.getOrDefault(GlowAccentColor.CYAN_PULSE)
            val isGlowEnabled = preferences[THEME_GLOW_ENABLED] ?: true
            HabitPulseThemeConfig(aesthetic, density, glow, isGlowEnabled)
        }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data
        .map { preferences ->
            runCatching { AppLanguage.valueOf(preferences[APP_LANGUAGE] ?: "") }.getOrDefault(AppLanguage.ENGLISH_US)
        }

    val customBrandingFlow: Flow<Pair<String, String>> = context.dataStore.data
        .map { preferences ->
            val name = preferences[CUSTOM_APP_NAME] ?: "HabitPulse"
            val emoji = preferences[CUSTOM_BADGE_EMOJI] ?: "⚡"
            Pair(name, emoji)
        }

    val gamificationXpFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[GAMIFICATION_XP] ?: 0
        }

    suspend fun updateThemeConfig(config: HabitPulseThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[THEME_AESTHETIC] = config.aesthetic.name
            preferences[THEME_DENSITY] = config.density.name
            preferences[THEME_GLOW] = config.glowAccent.name
            preferences[THEME_GLOW_ENABLED] = config.isGlowEnabled
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = language.name
        }
    }

    suspend fun updateCustomBranding(name: String, emoji: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_APP_NAME] = name
            preferences[CUSTOM_BADGE_EMOJI] = emoji
        }
    }

    suspend fun updateGamificationXp(xp: Int) {
        context.dataStore.edit { preferences ->
            preferences[GAMIFICATION_XP] = xp
        }
    }
}
